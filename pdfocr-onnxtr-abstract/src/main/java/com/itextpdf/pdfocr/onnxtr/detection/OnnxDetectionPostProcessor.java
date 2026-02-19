/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnxtr.detection;

import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
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
public class OnnxDetectionPostProcessor extends BasicDetectionPostProcessor {
    /**
     * Cached 3x3 kernel, which is used in morphological operations.
     */
    private static final Mat OPENING_KERNEL = new Mat(3, 3, CvType.CV_8U,
            new org.bytedeco.opencv.opencv_core.Scalar(1.0, 1.0, 1.0, 1.0));

    /**
     * Creates a new post-processor.
     *
     * @param binarizationThreshold threshold value used, when binarizing a monochromatic image. If pixel value is
     *                              greater or equal to the threshold, it is mapped to 1, otherwise it is mapped to 0
     * @param scoreThreshold score threshold for a detected box. If score is lower than this value,
     *                       the box gets discarded
     */
    public OnnxDetectionPostProcessor(float binarizationThreshold, float scoreThreshold) {
        super(adaptBinarizationThreshold(binarizationThreshold), scoreThreshold, Integer.MAX_VALUE);
    }

    /**
     * Creates a new post-processor with the default threshold values.
     */
    public OnnxDetectionPostProcessor() {
        this(0.1F, 0.1F);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MatVector findTextContours(Mat mask) {
        // OnnxTR runs an opening operation over the mask before searching for
        // contours
        opencv_imgproc.morphologyEx(mask, mask, opencv_imgproc.MORPH_OPEN, OPENING_KERNEL);
        return super.findTextContours(mask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected float mapPredToSample(float pred) {
        // OnnxTR uses `expit` to normalize prediction values, as it is not
        // done in the models themselves
        return MathUtil.expit(pred);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double calcTextBoxEnlargement(double width, double height) {
        final double area = (width + 1.0) * (height + 1.0);
        final double length = 2.0 * (width + height + 1.0);
        // Minimized from `2 * area * unclipRatio / length` with unclipRatio as 1.5
        return 3.0 * area / length;
    }

    private static float adaptBinarizationThreshold(float value) {
        // We will actually use `logit` value of the OnnxTR threshold, this
        // way we won't need to run `expit` over the whole output buffer
        // beforehand for binarization
        return (float) MathUtil.logit(MathUtil.clamp(value, 0., 1.));
    }
}
