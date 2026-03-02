/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnx.detection;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.onnx.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnx.detection.score.IScoreCalculator;
import com.itextpdf.pdfocr.onnx.detection.score.MeanScoreCalculator;
import com.itextpdf.pdfocr.onnx.util.MathUtil;
import com.itextpdf.pdfocr.onnx.util.OpenCvUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.Point2fVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RotatedRect;
import org.bytedeco.opencv.opencv_core.Size2f;
import org.opencv.core.CvType;

/**
 * Implementation of a text detection predictor post-processor, which is used
 * as a basis for creating post-processors for handling OnnxTR, EasyOCR and
 * PaddleOCR model outputs.
 *
 * <p>
 * Base implementation works somewhat like this:
 * <ol>
 *     <li>Model output is binarized to create a predictions mask.
 *     <li>Large-enough contours from the mask in the previous step are found.
 *     <li>Contours with less certainty score are discarded.
 *     <li>Remaining contours are wrapped into boxes with relative [0, 1] coordinates.
 * </ol>
 */
public abstract class BasicDetectionPostProcessor implements IDetectionPostProcessor {
    /**
     * Minimum size for the contour dimensions to not be immediately filtered.
     */
    private static final int MIN_CONTOUR_SIZE = 3;

    /**
     * Threshold value used, when binarizing a monochromatic image. If pixel
     * value is greater or equal to the threshold, it is mapped to 1, otherwise
     * it is mapped to 0.
     */
    private final float binarizationThreshold;
    /**
     * Score threshold for a detected box. If score is lower than this value,
     * the box gets discarded.
     */
    private final float scoreThreshold;
    /**
     * Maximum amount of text box contours, that will be handled in the post
     * processor.
     */
    private final int maxCandidates;

    /**
     * Creates a new post-processor.
     *
     * @param binarizationThreshold threshold value used, when binarizing a monochromatic image. If pixel value is
     *                              greater or equal to the threshold, it is mapped to 1, otherwise it is mapped to 0
     * @param scoreThreshold score threshold for a detected box. If score is lower than this value,
     *                       the box gets discarded
     * @param maxCandidates maximum amount of text box contours, that will be handled in the post processor
     */
    protected BasicDetectionPostProcessor(
            float binarizationThreshold,
            float scoreThreshold,
            int maxCandidates
    ) {
        this.binarizationThreshold = binarizationThreshold;
        this.scoreThreshold = scoreThreshold;
        this.maxCandidates = maxCandidates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point[]> process(BufferedImage input, FloatBufferMdArray output) {
        final FloatBufferMdArray preds = getPredsArray(output);
        final int height = preds.getDimension(0);
        final int width = preds.getDimension(1);
        final List<Point[]> boxes = new ArrayList<>();
        try (final MatVector contours = findTextContoursFromOutput(output)) {
            final long contourCount = Math.min(contours.size(), maxCandidates);
            for (long contourIdx = 0; contourIdx < contourCount; ++contourIdx) {
                try (final Mat contour = contours.get(contourIdx);
                     final Rect contourBox = opencv_imgproc.boundingRect(contour)) {
                    if (!isValidContour(contour, contourBox)) {
                        continue;
                    }

                    final float score = calcPredictionScore(preds, contour, contourBox);
                    if (score < scoreThreshold) {
                        continue;
                    }

                    boxes.add(calculateTextBox(contour, width, height));
                }
            }
        }
        return boxes;
    }

    /**
     * Returns the preds array from the output buffer.
     *
     * @param output output buffer from the model
     *
     * @return the preds array
     */
    protected FloatBufferMdArray getPredsArray(FloatBufferMdArray output) {
        // By default, we assume that output is just in CHW
        return output.getSubArray(0);
    }

    /**
     * Returns the array to be used, when building a mask for contour
     * detection.
     *
     * @param output output buffer from the model
     *
     * @return the array to build the mask from
     */
    protected FloatBufferMdArray getMaskSourceArray(FloatBufferMdArray output) {
        // By default, we want to build it from the preds array
        return getPredsArray(output);
    }

    /**
     * Extracts text contours from the provided 0 - 255 mask.
     *
     * @param mask mask to find contours in, can be modified, should not be closed
     *
     * @return found text contours
     */
    protected MatVector findTextContours(Mat mask) {
        final MatVector contours = new MatVector();
        opencv_imgproc.findContours(
                mask, contours, opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE
        );
        return contours;
    }

    /**
     * Returns whether the contour is good enough to be a text box. Called
     * before score calculations.
     *
     * @param contour contour to check
     * @param contourBox bounding box of the contour to check
     *
     * @return whether the contour is good enough to be a text box
     */
    protected boolean isValidContour(Mat contour, Rect contourBox) {
        // By default, skipping contours that are too small
        return contourBox.width() >= MIN_CONTOUR_SIZE && contourBox.height() >= MIN_CONTOUR_SIZE;
    }

    /**
     * Builds and return a mask for calculating prediction score for the
     * provided contour.
     *
     * <p>
     * Mask should adhere to the following requirements:
     * <ul>
     *     <li>Mask should have the same dimensions as the contour box.
     *     <li>Data type should be CV_8U.
     *     <li>Pixels, that should be counted towards the score, should have a
     *     non-zero value in the mask.
     * </ul>
     *
     * @param contour contour to build mask for
     * @param contourBox bounding box of the contour to build mask for
     *
     * @return the built mask
     */
    protected Mat buildTextContourPredictionMask(Mat contour, Rect contourBox) {
        final int x = contourBox.x();
        final int y = contourBox.y();
        final int height = contourBox.height();
        final int width = contourBox.width();
        final Mat mask = new Mat(height, width, CvType.CV_8U, org.bytedeco.opencv.opencv_core.AbstractScalar.ZERO);
        try {
            OpenCvUtil.fillPolyAtOffset(mask, contour, org.bytedeco.opencv.opencv_core.AbstractScalar.WHITE, -x, -y);
            return mask;
        } catch (RuntimeException e) {
            mask.close();
            throw e;
        }
    }

    /**
     * Creates a new score calculator for calculating score over a text
     * contour.
     *
     * @return a new score calculator
     */
    protected IScoreCalculator createScoreCalculator() {
        return new MeanScoreCalculator();
    }

    /**
     * Calculates the score sample value, based on a prediction value from the
     * buffer.
     *
     * @param pred prediction value to map
     *
     * @return mapped score
     */
    protected float mapPredToSample(float pred) {
        return pred;
    }

    /**
     * Calculates by how much the dimensions of a text box should be enlarged
     * compared to the ones gotten from the model output.
     *
     * @param width original width of the text box
     * @param height original height of the text box
     *
     * @return value to enlarge the dimensions by
     */
    protected double calcTextBoxEnlargement(double width, double height) {
        final double area = width * height;
        final double length = 2.0 * (width + height);
        return 2.0 * area / length;
    }

    /**
     * Find contours using the output of the ML model.
     *
     * @param output output buffer from the model
     *
     * @return found text contours
     */
    private MatVector findTextContoursFromOutput(FloatBufferMdArray output) {
        // Expecting CHW buffer here
        assert output.getDimensionCount() == 3;

        final FloatBufferMdArray maskSource = getMaskSourceArray(output);
        try (final Mat mask = OpenCvUtil.binarizeMdArray(maskSource, binarizationThreshold)) {
            return findTextContours(mask);
        }
    }

    /**
     * Calculates the prediction score for the text contour.
     *
     * @param preds original output predictions matrix
     * @param contour text contour to calculate score for
     * @param contourBox bounding box of the text contour to calculate score for
     *
     * @return the calculated score
     */
    private float calcPredictionScore(FloatBufferMdArray preds, Mat contour, Rect contourBox) {
        /*
         * Algorithm here is pretty simple. We go over all the points, marked
         * by the mask and calculate the mean prediction score value over the
         * original output array.
         */
        final IScoreCalculator scoreCalculator = createScoreCalculator();
        final int contourX = contourBox.x();
        final int contourY = contourBox.y();
        try (final Mat mask = buildTextContourPredictionMask(contour, contourBox);
             final UByteIndexer maskIndexer = mask.createIndexer()) {
            // Making sure we use correct boundaries for preds
            final int yEnd = Math.min(mask.rows(), preds.getDimension(0) - contourY);
            final int xEnd = Math.min(mask.cols(), preds.getDimension(1) - contourX);
            for (int y = 0; y < yEnd; ++y) {
                final FloatBufferMdArray predictionsRow = preds.getSubArray(y + contourY);
                for (int x = 0; x < xEnd; ++x) {
                    if (maskIndexer.get(y, x) == 0) {
                        continue;
                    }
                    final float sample = mapPredToSample(predictionsRow.getScalar(x + contourX));
                    scoreCalculator.observe(sample);
                }
            }
        }
        return scoreCalculator.calculate();
    }

    /**
     * Returns points of the text box quad, which has been padded using
     * {@code calcTextBoxEnlargement}.
     *
     * @param points points of the text box contour
     *
     * @return the padded text box
     */
    private Point2fVector getPaddedBox(Mat points) {
        try (final RotatedRect rect = OpenCvUtil.normalizedMinAreaRect(points)) {
            try (final Size2f rectSize = rect.size()) {
                final double rectWidth = rectSize.width();
                final double rectHeight = rectSize.height();
                final double expandAmount = calcTextBoxEnlargement(rectWidth, rectHeight);
                rectSize.width(Math.round(rectWidth + expandAmount));
                rectSize.height(Math.round(rectHeight + expandAmount));
            }
            final Point2fVector boxPoints = new Point2fVector(4);
            rect.points(boxPoints);
            return boxPoints;
        }
    }

    /**
     * Calculates the 0-1 relative coordinate text box based on the contour.
     *
     * @param points text box contour
     * @param width width of the predictions mask
     * @param height height of the predictions mask
     *
     * @return the calculated text box
     */
    private Point[] calculateTextBox(Mat points, int width, int height) {
        try (final Point2fVector cvBox = getPaddedBox(points)) {
            final Point[] textBox = new Point[4];
            for (int i = 0; i < 4; ++i) {
                try (final Point2f cvPoint = cvBox.get(i)) {
                    // Coordinates are relative on an [0, 1] scale, so that it
                    // is easier to map back to the input image.
                    textBox[i] = new Point(
                            MathUtil.clamp((double) cvPoint.x() / width, 0, 1),
                            MathUtil.clamp((double) cvPoint.y() / height, 0, 1)
                    );
                }
            }
            return textBox;
        }
    }
}
