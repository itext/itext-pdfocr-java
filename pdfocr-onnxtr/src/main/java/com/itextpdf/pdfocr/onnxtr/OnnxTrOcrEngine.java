package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * {@link IOcrEngine} implementation, based on OnnxTR/DocTR machine learning OCR projects.
 */
public class OnnxTrOcrEngine implements IOcrEngine, AutoCloseable {
    /**
     * Image pixel to PDF point ratio.
     */
    private static final float PX_TO_PT = 0.75F;

    /**
     * Aspect ratio, at which a text box is split for better text recognition.
     */
    private static final float SPLIT_CROPS_MAX_RATIO = 8;
    /**
     * Target aspect ratio for the text box splits.
     */
    private static final float SPLIT_CROPS_TARGET_RATIO = 6;
    /**
     * Multiplier, which controls the overlap between splits. Factor of 1 means, that there will
     * be no overlap.
     * <p>
     * This is for cases, when a split happens in the middle of a character. With some overlap, at
     * least one of the sub-images will contain the character in full.
     * </p>
     */
    private static final float SPLIT_CROPS_DILATION_FACTOR = 1.4F;

    /**
     * Text detector. For an input image it outputs a list of text boxes.
     */
    private final IDetectionPredictor detectionPredictor;
    /**
     * Text orientation predictor. For an input image, which is a tight crop of text, it outputs
     * its orientation in 90 degrees steps. Can be null.
     */
    private final IOrientationPredictor orientationPredictor;
    /**
     * Text recognizer. For an input image, which is a tight crop of text, it outputs the displayed
     * string.
     */
    private final IRecognitionPredictor recognitionPredictor;

    /**
     * Create a new OCR engine with the provided predictors.
     *
     * @param detectionPredictor   Text detector. For an input image it outputs a list of text boxes.
     * @param orientationPredictor Text orientation predictor. For an input image, which is a tight
     *                             crop of text, it outputs its orientation in 90 degrees steps. Can
     *                             be null, in that case all text is assumed to be upright.
     * @param recognitionPredictor Text recognizer. For an input image, which is a tight crop of
     *                             text, it outputs the displayed string.
     */
    public OnnxTrOcrEngine(
            IDetectionPredictor detectionPredictor,
            IOrientationPredictor orientationPredictor,
            IRecognitionPredictor recognitionPredictor
    ) {
        this.detectionPredictor = Objects.requireNonNull(detectionPredictor);
        this.orientationPredictor = orientationPredictor;
        this.recognitionPredictor = Objects.requireNonNull(recognitionPredictor);
    }

    /**
     * Create a new OCR engine with the provided predictors, without text
     * orientation prediction.
     *
     * @param detectionPredictor   Text detector. For an input image it outputs a list of text boxes.
     * @param recognitionPredictor Text recognizer. For an input image, which is a tight crop of
     *                             text, it outputs the displayed string.
     */
    public OnnxTrOcrEngine(
            IDetectionPredictor detectionPredictor,
            IRecognitionPredictor recognitionPredictor
    ) {
        this(detectionPredictor, null, recognitionPredictor);
    }

    @Override
    public void close() throws Exception {
        detectionPredictor.close();
        if (orientationPredictor != null) {
            orientationPredictor.close();
        }
        recognitionPredictor.close();
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        return doImageOcr(input, null);
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
        /*
         * TODO: Make this interface better.
         *
         * There are two problems. First, we get only one image per call. But the text detector
         * can batch multiple images and for on them at once, which is a performance improvement,
         * at least on GPU.
         *
         * Second problem is that it forces all OCR engines to reimplement image reading code.
         * Image reading should happen on a layer higher, so that the code is common. This should
         * also be a performance improvement, since images get read again anyway to create the
         * final PDF.
         */
        final List<BufferedImage> images = getImages(input);
        final Map<Integer, List<TextInfo>> result = new HashMap<>(images.size());
        int imageIndex = 0;
        final Iterator<List<Point[]>> textBoxGenerator = detectionPredictor.predict(images);
        while (textBoxGenerator.hasNext()) {
            /*
             * TODO: Potential performance improvement (at least for GPU).
             *
             * There is a potential for performance improvements here. Currently, this mirrors the
             * behavior in OnnxTR/DocTR, where inputs for orientation and recognition models are
             * aggregated per input image.
             *
             * But, most of the time, this will not be enough to saturate the batch size fully.
             * Ideally, we should process all text boxes together, regardless of the origin image,
             * and then separate the results afterwards.
             */
            final BufferedImage image = images.get(imageIndex);
            final List<Point[]> textBoxes = textBoxGenerator.next();
            final List<BufferedImage> textImages = BufferedImageUtil.extractBoxes(image, textBoxes);
            List<TextOrientation> textOrientations = null;
            if (orientationPredictor != null) {
                textOrientations = toList(orientationPredictor.predict(textImages));
                correctOrientations(textImages, textOrientations);
            }
            final List<String> textString = recognizeText(textImages);
            final List<TextInfo> textInfos = new ArrayList<>(textBoxes.size());
            for (int i = 0; i < textBoxes.size(); ++i) {
                TextOrientation textOrientation = TextOrientation.HORIZONTAL;
                if (textOrientations != null) {
                    textOrientation = textOrientations.get(i);
                }
                textInfos.add(new TextInfo(
                        textString.get(i),
                        /*
                         * FIXME: Why not return rectangles in image pixels?..
                         *
                         * Seems odd, that an OCR engine should be concerned by PDF specific. It
                         * would make sense for an engine to return results, which could be directly
                         * applied to images inputs instead...
                         */
                        toPdfRectangle(textBoxes.get(i), image.getHeight()),
                        textOrientation
                ));
            }
            result.put(imageIndex + 1, textInfos);
            ++imageIndex;
        }
        return result;
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile) {
        createTxtFile(inputImages, txtFile, null);
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext) {
        /*
         * TODO: Implement this interface.
         *
         * With how this engine is build, there is no concept of "lines" or "paragraphs". It just
         * find boxes with text and recognizes them.
         *
         * OnnxTR/DocTR doesn't have a text output. But they have a doc builder, which sorts out
         * recognized text boxes into blocks/lines/words/... for output.
         *
         * Ideally we would want a default implementation, which just takes doImageOcr output and
         * builds a text document out of it. Some of this already happens with Tesseract, IIRC.
         */
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTaggingSupported() {
        return false;
    }

    /**
     * Runs text recognition on the provided text images.
     *
     * @param textImages Images with text to recognize.
     *
     * @return List of strings, recognized in the images.
     */
    private List<String> recognizeText(List<BufferedImage> textImages) {
        // For better recognition results we want to split text images to have better aspect ratios
        final SplitResult split = splitTextImages(textImages);
        final Iterator<String> recognitionIterator = recognitionPredictor.predict(split.splitImages);
        // And now we merge results back
        final List<String> textStrings = new ArrayList<>(split.restoreMap.length);
        for (int j = 0; j < split.restoreMap.length; ++j) {
            int stringPartsLeft = split.restoreMap[j];
            final String testString;
            if (stringPartsLeft == 1) {
                testString = recognitionIterator.next();
            } else {
                final StringBuilder sb = new StringBuilder();
                while (stringPartsLeft > 0) {
                    mergeStrings(sb, recognitionIterator.next());
                    --stringPartsLeft;
                }
                testString = sb.toString();
            }
            textStrings.add(testString);
        }
        return textStrings;
    }

    /**
     * Rotates all images in the text image list, so that they are upright, based on the found text
     * orientation information.
     *
     * @param textImages       Text images to rotate.
     * @param textOrientations Orientations of text images. Should be the same size as textImages.
     */
    private static void correctOrientations(List<BufferedImage> textImages, List<TextOrientation> textOrientations) {
        assert textImages.size() == textOrientations.size();

        for (int i = 0; i < textImages.size(); ++i) {
            textImages.set(i, BufferedImageUtil.rotate(textImages.get(i), textOrientations.get(i)));
        }
    }

    /**
     * Splits text images to smaller images with better aspect ratios.
     *
     * @param images Text images to split.
     *
     * @return A list with image splits together with a map to restore them back.
     */
    private static SplitResult splitTextImages(List<BufferedImage> images) {
        final SplitResult result = new SplitResult(images.size());
        for (int i = 0; i < images.size(); ++i) {
            final BufferedImage image = images.get(i);
            final int width = image.getWidth();
            final int height = image.getHeight();
            final float aspectRatio = (float) width / height;
            if (aspectRatio < SPLIT_CROPS_MAX_RATIO) {
                result.splitImages.add(image);
                result.restoreMap[i] = 1;
                continue;
            }

            // For some reason here is truncation in OnnxTR...
            final int splitCount = (int) Math.ceil(aspectRatio / SPLIT_CROPS_TARGET_RATIO);
            final float rawSplitWidth = (float) width / splitCount;
            final float targetSplitHalfWidth = (SPLIT_CROPS_DILATION_FACTOR * rawSplitWidth) / 2;
            int nonEmptySplitCount = 0;
            for (int j = 0; j < splitCount; ++j) {
                final float center = (j + 0.5F) * rawSplitWidth;
                final int minX = Math.max(0, (int) Math.floor(center - targetSplitHalfWidth));
                final int maxX = Math.min(width - 1, (int) Math.ceil(center + targetSplitHalfWidth));
                final int currentSplitWidth = maxX - minX;
                if (currentSplitWidth == 0) {
                    continue;
                }
                ++nonEmptySplitCount;
                result.splitImages.add(image.getSubimage(minX, 0, currentSplitWidth, height));
            }
            result.restoreMap[i] = nonEmptySplitCount;
        }
        return result;
    }

    /**
     * Merges strings, collected from splits of text images.
     * <p>
     * This code is pretty much 1-to-1 to what is in OnnxTR. Logic is not that trivial...
     * </p>
     *
     * @param collector  String builder collector, which contains the current left part of the string.
     * @param nextString Next string to add to the collector.
     */
    private static void mergeStrings(StringBuilder collector, String nextString) {
        // Comments are also pretty much copies from OnnxTR...
        final int commonLength = Math.min(collector.length(), nextString.length());
        final double[] scores = new double[commonLength];
        for (int i = 0; i < commonLength; ++i) {
            // FIXME: org.apache.commons.commons-text is used only for this, but
            //        since Levenshtein distance is relatively trivial, might be
            //        better to just reimplement it
            scores[i] = LevenshteinDistance.getDefaultInstance().apply(
                    collector.substring(collector.length() - i - 1),
                    nextString.substring(0, i + 1)
            ) / (i + 1.0);
        }

        int index = 0;
        // Comparing floats to 0 is fine here, as it only happens, when the
        // integer nominator (i.e. Levenshtein distance) was 0
        if (commonLength > 1 && scores[0] == 0 && scores[1] == 0) {
            // Edge case (split in the middle of char repetitions): if it starts with 2 or more 0

            // Compute n_overlap (number of overlapping chars, geometrically determined)
            final int overlap = Math.round(
                    nextString.length() *
                            (OnnxTrOcrEngine.SPLIT_CROPS_DILATION_FACTOR - 1)
                            / OnnxTrOcrEngine.SPLIT_CROPS_DILATION_FACTOR
            );
            // Find the number of consecutive zeros in the scores list
            // Impossible to have a zero after a non-zero score in that case
            final int zeros = (int) Arrays.stream(scores).filter(x -> x == 0).count();
            index = Math.min(zeros, overlap);
        } else {
            // Common case: choose the min score index
            double minScore = 1.0;
            for (int i = 0; i < commonLength; ++i) {
                if (scores[i] < minScore) {
                    minScore = scores[i];
                    index = i + 1;
                }
            }
        }

        if (index == 0) {
            collector.append(nextString);
        } else {
            collector.setLength(Math.max(0, collector.length() - 1));
            collector.append(nextString, index - 1, nextString.length());
        }
    }

    /**
     * Convert a text polygon to a bounding box in PDF points.
     *
     * @param polygon     Polygon to convert.
     * @param imageHeight Height of the image (to change the y origin).
     *
     * @return A bounding box in PDF points.
     */
    private static Rectangle toPdfRectangle(Point[] polygon, int imageHeight) {
        float minX = (float) polygon[0].getX();
        float maxX = minX;
        float minY = (float) polygon[0].getY();
        float maxY = minY;
        for (int i = 1; i < polygon.length; ++i) {
            final float x = (float) polygon[i].getX();
            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
            final float y = (float) polygon[i].getY();
            if (y < minY) {
                minY = y;
            } else if (y > maxY) {
                maxY = y;
            }
        }
        return new Rectangle(
                PX_TO_PT * minX,
                PX_TO_PT * (imageHeight - maxY),
                PX_TO_PT * (maxX - minX),
                PX_TO_PT * (maxY - minY)
        );
    }

    private static List<BufferedImage> getImages(File input) {
        // TODO: As mentioned before, this should be abstracted away from OcrEngine.
        try {
            return Collections.singletonList(ImageIO.read(input));
        } catch (IOException e) {
            throw new PdfOcrException("Failed to read image", e);
        }
    }

    private static <E> List<E> toList(Iterator<E> iterator) {
        final List<E> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    /**
     * Contains results of a text image split.
     */
    private static class SplitResult {
        /**
         * List of sub-images, that the original images were split into.
         */
        public final List<BufferedImage> splitImages;
        /**
         * A map of splits. Array length is equal to the original image count. Each element defines
         * how many sub-images were generated from each original image.
         */
        public final int[] restoreMap;

        public SplitResult(int capacity) {
            this.splitImages = new ArrayList<>(capacity);
            this.restoreMap = new int[capacity];
        }
    }
}
