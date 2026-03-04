/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.onnx.actions.events.PdfOcrOnnxProductEvent;
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.util.BufferedImageUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class containing OCRing methods adapted from <a href="https://github.com/felixdittrich92/OnnxTR">OnnxTR</a>.
 */
class OnnxProcessor {

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

    OnnxProcessor(IDetectionPredictor detectionPredictor, IOrientationPredictor orientationPredictor,
                    IRecognitionPredictor recognitionPredictor) {
        this.detectionPredictor = detectionPredictor;
        this.orientationPredictor = orientationPredictor;
        this.recognitionPredictor = recognitionPredictor;
    }

    Map<Integer, List<TextInfo>> doOcr(List<BufferedImage> images, OcrProcessContext ocrProcessContext) {
        final Map<Integer, List<TextInfo>> result = new HashMap<>(images.size());
        int imageIndex = 0;
        Iterator<List<Point[]>> textBoxGenerator = detectionPredictor.predict(images);
        while (textBoxGenerator.hasNext()) {
            AbstractPdfOcrEventHelper eventHelper = ocrProcessContext.getOcrEventHelper() == null ?
                    new OnnxEventHelper() : ocrProcessContext.getOcrEventHelper();
            // Usage event.
            PdfOcrOnnxProductEvent event = PdfOcrOnnxProductEvent.createProcessImageOnnxEvent(
                    eventHelper.getSequenceId(), null, eventHelper.getConfirmationType());
            eventHelper.onEvent(event);
            /*
             * Currently, inputs for orientation and recognition models are aggregated per input image.
             * Most of the time, this is enough to saturate the batch size fully for real use cases
             * (for example, 64 words for DocTR or 6 lines for PaddleOcr).
             * If we process all text boxes together, regardless of the origin image, and then separate
             * the results afterward, the performance improvement is not noticeable.
             */
            BufferedImage image = images.get(imageIndex);
            List<Point[]> textBoxes = textBoxGenerator.next();
            List<BufferedImage> textImages = BufferedImageUtil.extractBoxes(image, textBoxes);
            List<TextOrientation> textOrientations = null;
            if (orientationPredictor != null) {
                textOrientations = toList(orientationPredictor.predict(textImages));
                correctOrientations(textImages, textOrientations);
            }
            List<String> textString = toList(recognitionPredictor.predict(textImages));
            List<TextInfo> textInfos = new ArrayList<>(textBoxes.size());
            for (int i = 0; i < textBoxes.size(); ++i) {
                TextOrientation textOrientation = TextOrientation.HORIZONTAL;
                if (textOrientations != null) {
                    textOrientation = textOrientations.get(i);
                }
                Point[] textPoints = getTextPoints(textBoxes.get(i), textOrientation);
                textInfos.add(new TextInfo().setText(textString.get(i))
                        .setPixelTextPoints(textPoints, image.getHeight()));
            }
            result.put(imageIndex + 1, textInfos);
            ++imageIndex;

            // Here can be statistics event sending.

            // Confirm on_demand event.
            if (event.getConfirmationType() == EventConfirmationType.ON_DEMAND) {
                eventHelper.onEvent(new ConfirmEvent(event));
            }
        }

        return result;
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
     * Reorders textBox points to be in lower-left based order relative to text.
     *
     * @param textBox arbitrarily rotated quadrilateral representing text bounding points with the next order
     * relative to x and y axes directions: 0 - lower-left, 1 - upper-left, 2 - upper-right, 3 - lower-right point
     * @param textOrientation {@link TextOrientation} to determine same points order, but relative to text itself. So
     * for example for 90 degrees rotated text initial lower-left point will be upper-left relative to text
     *
     * @return array of 4 {@link Point}s describing text bbox (0 - lower-left, 1 - upper-left,
     * 2 - upper-right, 3 - lower-right point relative to text)
     */
    private static Point[] getTextPoints(Point[] textBox, TextOrientation textOrientation) {
        Point[] rotatedTextBox;
        switch (textOrientation) {
            case HORIZONTAL_ROTATED_90:
                rotatedTextBox = new Point[]{textBox[3], textBox[0], textBox[1], textBox[2]};
                break;
            case HORIZONTAL_ROTATED_180:
                rotatedTextBox = new Point[]{textBox[2], textBox[3], textBox[0], textBox[1]};
                break;
            case HORIZONTAL_ROTATED_270:
                rotatedTextBox = new Point[]{textBox[1], textBox[2], textBox[3], textBox[0]};
                break;
            case HORIZONTAL:
            default:
                rotatedTextBox = textBox;
                break;
        }
        return rotatedTextBox;
    }

    private static <E> List<E> toList(Iterator<E> iterator) {
        List<E> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}
