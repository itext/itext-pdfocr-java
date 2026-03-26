/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.onnx.detection;

import com.itextpdf.pdfocr.onnx.util.OpenCvUtil;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.opencv.core.CvType;

/**
 * Implementation of a text detection predictor post-processor, used for
 * PaddleOCR model outputs.
 */
public class PaddleOcrDetectionPostProcessor extends BasicDetectionPostProcessor {
    /**
     * Coefficient used to scale, how much a box is enlarged from the ones
     * found in a model output. The higher the value, the bigger the
     * enlargement is.
     */
    private final float unclipRatio;

    /**
     * Creates a new post-processor.
     *
     * @param thresh threshold value used, when binarizing a monochromatic image. If pixel value is greater or equal
     *               to the threshold, it is mapped to 1, otherwise it is mapped to 0
     * @param boxThresh score threshold for a detected box. If score is lower than this value, the box gets discarded
     * @param unclipRatio coefficient used to scale, how much a box is enlarged from the ones found in a model output.
     *                    The higher the value, the bigger the enlargement is
     * @param maxCandidates maximum amount of text box contours, that will be handled in the post processor
     */
    public PaddleOcrDetectionPostProcessor(float thresh, float boxThresh, float unclipRatio, int maxCandidates) {
        super(thresh, boxThresh, maxCandidates);
        this.unclipRatio = unclipRatio;
    }

    /**
     * Creates a new post-processor with the default parameters.
     */
    public PaddleOcrDetectionPostProcessor() {
        this(0.3F, 0.6F, 1.5F, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Mat buildTextContourPredictionMask(Mat contour, Rect contourBox) {
        /*
         * In PaddleOCR, by default, the prediction score is calculated not
         * over the original text contour (which they mark as "slow"), but
         * over its min area rectangle (which they mark as "fast").
         *
         * Here we are not, actually, doing that, but we are calculating over
         * the min area rectangle, which was truncated by the bounding box for
         * the original contour, which can be smaller. The results are pretty
         * similar, and it is easier to abstract it away like this.
         *
         * Somewhat ironically, in our case this algorithm is "slow", while
         * the contour one is "fast". This is because we don't have a min area
         * rectangle at this point in the calculations yet, so we are forced
         * to calculate it for score. But in PaddleOCR it was already
         * calculated. It still seems worthwhile to use this, as it seems to
         * filter out some of the noise in the results.
         */
        final int x = contourBox.x();
        final int y = contourBox.y();
        final int height = contourBox.height();
        final int width = contourBox.width();
        final Mat mask = new Mat(height, width, CvType.CV_8U, org.bytedeco.opencv.opencv_core.AbstractScalar.ZERO);
        try {
            try (final Mat poly = OpenCvUtil.minAreaRectBoxPoly(contour)){
                OpenCvUtil.fillPolyAtOffset(mask, poly, org.bytedeco.opencv.opencv_core.AbstractScalar.WHITE, -x, -y);
            }
            return mask;
        } catch (RuntimeException e) {
            mask.close();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double calcTextBoxEnlargement(double width, double height) {
        return unclipRatio * super.calcTextBoxEnlargement(width, height);
    }
}
