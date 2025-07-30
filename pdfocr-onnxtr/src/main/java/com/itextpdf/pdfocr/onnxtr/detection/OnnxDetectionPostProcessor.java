/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnxtr.detection;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;
import com.itextpdf.pdfocr.onnxtr.util.OpenCvUtil;

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
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size2f;
import org.opencv.core.CvType;

/**
 * Implementation of a text detection predictor post-processor, used for OnnxTR
 * model outputs.
 *
 * <p>
 * Current implementation works somewhat like this:
 * <ol>
 *     <li>Model output is binarized and then cleaned-up via erosion and dilation.
 *     <li>Large-enough contours from the image in the previous step are found.
 *     <li>Contours with less certainty score are discarded.
 *     <li>Remaining contours are wrapped into boxes with relative [0, 1] coordinates.
 * </ol>
 */
public class OnnxDetectionPostProcessor implements IDetectionPostProcessor {
    /**
     * Coefficient used to scale, how much a box is enlarged from the ones
     * found in a model output. The higher the value, the bigger the enlargement
     * is.
     */
    private static final float UNCLIP_RATIO = 1.5F;
    /**
     * Cached 3x3 kernel, which is used in morphological operations.
     */
    private static final Mat OPENING_KERNEL = new Mat(3, 3, CvType.CV_8U, new Scalar(1));

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
     * Creates a new post-processor.
     *
     * @param binarizationThreshold threshold value used, when binarizing a monochromatic image. If pixel value is
     *                              greater or equal to the threshold, it is mapped to 1, otherwise it is mapped to 0
     * @param scoreThreshold score threshold for a detected box. If score is lower than this value,
     *                       the box gets discarded
     */
    public OnnxDetectionPostProcessor(float binarizationThreshold, float scoreThreshold) {
        this.binarizationThreshold = binarizationThreshold;
        this.scoreThreshold = scoreThreshold;
    }

    /**
     * Creates a new post-processor with the default threshold values.
     */
    public OnnxDetectionPostProcessor() {
        this(0.1F, 0.1F);
    }

    @Override
    public List<Point[]> process(BufferedImage input, FloatBufferMdArray output) {
        final int height = output.getDimension(1);
        final int width = output.getDimension(2);
        final List<Point[]> boxes = new ArrayList<>();
        // TODO DEVSIX-9153: Ideally we would want to either cache the score mask (as model
        //       dimensions won't change) or use a smaller mask with only the
        //       contour. Though based on profiling, it doesn't look like it is
        //       that bad, when it is only once per input image.
        try (final Mat scoreMask = new Mat(height, width, CvType.CV_8U, new Scalar(0));
             final MatVector contours = findTextContours(output, binarizationThreshold)) {
            final long contourCount = contours.size();
            for (long contourIdx = 0; contourIdx < contourCount; ++contourIdx) {
                try (final Mat contour = contours.get(contourIdx);
                     final Rect contourBox = opencv_imgproc.boundingRect(contour)) {
                    // Skip, if contour is too small
                    if (contourBox.width() < 2 || contourBox.height() < 2) {
                        continue;
                    }

                    final float score = getPredictionScore(scoreMask, output, contour, contourBox);
                    if (score < scoreThreshold) {
                        continue;
                    }

                    boxes.add(calculateTextBox(contour, width, height));
                }
            }
        }
        return boxes;
    }

    private static MatVector findTextContours(FloatBufferMdArray chwMdArray, float binarizationThreshold) {
        try (final Mat binaryImage = binarizeImage(chwMdArray, binarizationThreshold)) {
            opencv_imgproc.morphologyEx(
                    binaryImage, binaryImage, opencv_imgproc.MORPH_OPEN, OPENING_KERNEL
            );
            final MatVector contours = new MatVector();
            opencv_imgproc.findContours(
                    binaryImage, contours, opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE
            );
            return contours;
        }
    }

    private static Mat binarizeImage(FloatBufferMdArray chwMdArray, float binarizationThreshold) {
        assert chwMdArray.getDimensionCount() == 3 && chwMdArray.getDimension(0) == 1;

        final FloatBufferMdArray hwMdArray = chwMdArray.getSubArray(0);
        final int height = hwMdArray.getDimension(0);
        final int width = hwMdArray.getDimension(1);
        final Mat binaryImage = new Mat(height, width, CvType.CV_8U);
        try (final UByteIndexer binaryImageIndexer = binaryImage.createIndexer()) {
            for (int y = 0; y < height; y++) {
                final FloatBufferMdArray predictionsRow = hwMdArray.getSubArray(y);
                for (int x = 0; x < width; ++x) {
                    final float prediction = predictionsRow.getScalar(x);
                    binaryImageIndexer.put(y, x, prediction >= binarizationThreshold ? (byte) 1 : (byte) 0);
                }
            }
        }
        return binaryImage;
    }

    private static float getPredictionScore(
            Mat scoreMask,
            FloatBufferMdArray predictions,
            Mat contour,
            Rect contourBox
    ) {
        /*
         * Algorithm here is pretty simple. We go over all the points, painted
         * by the contour shape, and calculate the mean prediction score
         * value over the original normalized output array.
         */
        final FloatBufferMdArray hwMdArray = predictions.getSubArray(0);
        final int height = hwMdArray.getDimension(0);
        final int width = hwMdArray.getDimension(1);
        double sum = 0;
        long nonZeroCount = 0;
        try (final UByteIndexer maskIndexer = scoreMask.createIndexer()) {
            try (final MatVector polys = new MatVector(contour)) {
                opencv_imgproc.fillPoly(scoreMask, polys, Scalar.ONE);
            }
            final int yBegin = Math.max(0, contourBox.y());
            final int yEnd = Math.min(height, contourBox.y() + contourBox.height());
            final int xBegin = Math.max(0, contourBox.x());
            final int xEnd = Math.min(width, contourBox.x() + contourBox.width());
            for (int y = yBegin; y < yEnd; ++y) {
                final FloatBufferMdArray predictionsRow = hwMdArray.getSubArray(y);
                for (int x = xBegin; x < xEnd; ++x) {
                    if (maskIndexer.get(y, x) != 1) {
                        continue;
                    }
                    final float prediction = predictionsRow.getScalar(x);
                    if (prediction > 0) {
                        sum += prediction;
                        ++nonZeroCount;
                    }
                    maskIndexer.put(y, x, 0);
                }
            }
        }
        // Should not happen
        if (nonZeroCount == 0) {
            return 0;
        }
        return (float) (sum / nonZeroCount);
    }

    private static Point2fVector getPaddedBox(Mat points) {
        try (final RotatedRect rect = OpenCvUtil.normalizeRotatedRect(opencv_imgproc.minAreaRect(points))) {
            try (final Size2f rectSize = rect.size()) {
                final float rectWidth = rectSize.width();
                final float rectHeight = rectSize.height();
                final float area = (rectWidth + 1) * (rectHeight + 1);
                final float length = 2 * (rectWidth + rectHeight + 1);
                final float expandAmount = 2 * (area * UNCLIP_RATIO / length);
                rectSize.width(Math.round(rectWidth + expandAmount));
                rectSize.height(Math.round(rectHeight + expandAmount));
            }
            final Point2fVector boxPoints = new Point2fVector(4);
            rect.points(boxPoints);
            return boxPoints;
        }
    }

    private static Point[] calculateTextBox(Mat points, int width, int height) {
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
