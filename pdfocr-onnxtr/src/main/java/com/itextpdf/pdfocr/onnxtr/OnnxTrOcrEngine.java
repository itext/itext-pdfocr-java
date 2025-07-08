package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;
import com.itextpdf.pdfocr.util.PdfOcrTextBuilder;
import com.itextpdf.pdfocr.util.TiffImageUtil;
import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link IOcrEngine} implementation, based on OnnxTR/DocTR machine learning OCR projects.
 *
 * <p>
 * NOTE: {@link OnnxTrOcrEngine} instance shall be closed after all usages to avoid native allocations leak.
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
     * Multiplier, which controls the overlap between splits. Factor of 1 means, that there will be no overlap.
     *
     * <p>
     * This is for cases, when a split happens in the middle of a character. With some overlap, at least one of the
     * sub-images will contain the character in full.
     */
    private static final float SPLIT_CROPS_DILATION_FACTOR = 1.4F;

    /**
     * Text detector. For an input image it outputs a list of text boxes.
     */
    private final IDetectionPredictor detectionPredictor;

    /**
     * Text orientation predictor. For an input image, which is a tight crop of text, it outputs its orientation
     * in 90 degrees steps. Can be null.
     */
    private final IOrientationPredictor orientationPredictor;

    /**
     * Text recognizer. For an input image, which is a tight crop of text, it outputs the displayed string.
     */
    private final IRecognitionPredictor recognitionPredictor;

    /**
     * Create a new OCR engine with the provided predictors.
     *
     * @param detectionPredictor   text detector. For an input image it outputs a list of text boxes
     * @param orientationPredictor text orientation predictor. For an input image, which is a tight  crop of text,
     *                             it outputs its orientation in 90 degrees steps. Can be null, in that case all text
     *                             is assumed to be upright
     * @param recognitionPredictor text recognizer. For an input image, which is a tight crop of text, it outputs the
     *                             displayed string
     */
    public OnnxTrOcrEngine(IDetectionPredictor detectionPredictor, IOrientationPredictor orientationPredictor,
                           IRecognitionPredictor recognitionPredictor) {
        this.detectionPredictor = Objects.requireNonNull(detectionPredictor);
        this.orientationPredictor = orientationPredictor;
        this.recognitionPredictor = Objects.requireNonNull(recognitionPredictor);
    }

    /**
     * Create a new OCR engine with the provided predictors, without text orientation prediction.
     *
     * @param detectionPredictor text detector. For an input image it outputs a list of text boxes
     * @param recognitionPredictor text recognizer. For an input image, which is a tight crop of text,
     *                             it outputs the displayed string
     */
    public OnnxTrOcrEngine(IDetectionPredictor detectionPredictor, IRecognitionPredictor recognitionPredictor) {
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
        final List<BufferedImage> images = getImages(input);
        final Map<Integer, List<TextInfo>> result = new HashMap<>(images.size());
        int imageIndex = 0;
        final Iterator<List<Point[]>> textBoxGenerator = detectionPredictor.predict(images);
        while (textBoxGenerator.hasNext()) {
            /*
             * TODO DEVSIX-9153: Potential performance improvement (at least for GPU).
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
                textInfos.add(new TextInfo(textString.get(i),
                        toPdfRectangle(textBoxes.get(i), image.getHeight()),
                        textOrientation));
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
        StringBuilder content = new StringBuilder();
        for (File inputImage : inputImages) {
            Map<Integer, List<TextInfo>> outputMap = doImageOcr(inputImage, ocrProcessContext);
            content.append(PdfOcrTextBuilder.buildText(outputMap));
        }
        writeToTextFile(txtFile.getAbsolutePath(), content.toString());
    }

    @Override
    public boolean isTaggingSupported() {
        return false;
    }

    /**
     * Writes provided {@link java.lang.String} to text file using provided path.
     *
     * @param path path as {@link java.lang.String} to file to be created
     * @param data text data in required format as {@link java.lang.String}
     */
    private static void writeToTextFile(final String path, final String data) {
        try (Writer writer = new OutputStreamWriter(FileUtil.getFileOutputStream(path), StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            throw new PdfOcrException(MessageFormatUtil.format(PdfOcrExceptionMessageConstant.CANNOT_WRITE_TO_FILE,
                    path, e.getMessage()), e);
        }
    }

    /**
     * Runs text recognition on the provided text images.
     *
     * @param textImages images with text to recognize
     *
     * @return list of strings, recognized in the images
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
     * @param textImages text images to rotate
     * @param textOrientations orientations of text images. Should be the same size as textImages
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
     * @param images text images to split
     *
     * @return a list with image splits together with a map to restore them back
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
     *
     * <p>
     * TODO DEVSIX-9153 This code is pretty much 1-to-1 to what is in OnnxTR. Logic is not that trivial.
     *
     * @param collector string builder collector, which contains the current left part of the string
     * @param nextString next string to add to the collector
     */
    private static void mergeStrings(StringBuilder collector, String nextString) {
        // Comments are also pretty much copies from OnnxTR...
        final int commonLength = Math.min(collector.length(), nextString.length());
        final double[] scores = new double[commonLength];
        for (int i = 0; i < commonLength; ++i) {
            // TODO DEVSIX-9153: org.apache.commons.commons-text is used only for this, but
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
     * @param polygon polygon to convert
     * @param imageHeight height of the image (to change the y origin)
     *
     * @return a bounding box in PDF points
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
        try {
            if (TiffImageUtil.isTiffImage(input)) {
                List<BufferedImage> images = TiffImageUtil.getAllImages(input);
                if (images.size() == 0) {
                    throw new PdfOcrInputException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE);
                }

                return images;
            } else {
                BufferedImage image = ImageIO.read(input);
                if (image == null) {
                    throw new PdfOcrInputException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE);
                }

                return Collections.singletonList(image);
            }
        } catch (Exception e) {
            throw new PdfOcrInputException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE, e);
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

        /**
         * Creates new {@link SplitResult} instance.
         *
         * @param capacity capacity of the list of sub-images
         */
        public SplitResult(int capacity) {
            this.splitImages = new ArrayList<>(capacity);
            this.restoreMap = new int[capacity];
        }
    }
}
