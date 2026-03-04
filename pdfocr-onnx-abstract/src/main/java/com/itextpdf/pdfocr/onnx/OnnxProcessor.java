/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
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
     * Image pixel to PDF point ratio.
     */
    private static final float PX_TO_PT = 0.75F;

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
             * Potential performance improvement (at least for GPU).
             *
             * There is a potential for performance improvements here. Currently, this mirrors the
             * behavior in OnnxTR/DocTR, where inputs for orientation and recognition models are
             * aggregated per input image.
             *
             * But, most of the time, this will not be enough to saturate the batch size fully.
             * Ideally, we should process all text boxes together, regardless of the origin image,
             * and then separate the results afterward.
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
                textInfos.add(new TextInfo(textString.get(i),
                        toPdfRectangle(textBoxes.get(i), image.getHeight()),
                        textOrientation));
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

    private static <E> List<E> toList(Iterator<E> iterator) {
        List<E> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}
