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
package com.itextpdf.pdfocr.onnx.util;

import com.itextpdf.pdfocr.onnx.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatExpr;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.RotatedRect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size2f;
import org.opencv.core.CvType;

/**
 * Static class with OpenCV utility functions.
 */
public final class OpenCvUtil {
    private OpenCvUtil() {
    }

    /**
     * Takes a two-dimensional MD-array and returns a binarized version of it
     * as an OpenCV Mat.
     *
     * @param hwMdArray MD-array to binarize
     * @param threshold Threshold for a value to be 0xFF instead of 0x00
     *
     * @return binarized version of the MD-array as an OpenCV Mat
     */
    public static Mat binarizeMdArray(FloatBufferMdArray hwMdArray, float threshold) {
        if (hwMdArray.getDimensionCount() != 2) {
            throw new IllegalArgumentException(
                    PdfOcrOnnxExceptionMessageConstant.HW_ARRAY_SHOULD_BE_TWO_DIMENSIONAL
            );
        }

        final int height = hwMdArray.getDimension(0);
        final int width = hwMdArray.getDimension(1);

        byte[] binaryData = new byte[height * width];
        float[] binaryArray = new float[height * width];
        hwMdArray.getData().get(binaryArray);
        for (int i = 0; i < height * width; i++) {
            binaryData[i] = binaryArray[i] >= threshold ? (byte) 0xFF : (byte) 0;
        }

        final Mat binaryImage = new Mat(height, width, CvType.CV_8U);
        if (binaryImage.isContinuous()) {
            binaryImage.data().put(binaryData);
        } else {
            for (int y = 0; y < height; ++y) {
                binaryImage.ptr(y).put(binaryData, y * width, width);
            }
        }
        return binaryImage;
    }

    /**
     * OpenCV minAreaRect, but returns the normalized rectangle immediately.
     * Equivalent to {@code normalizeRotatedRect(opencv_imgproc.minAreaRect(x))}.
     *
     * @param points vector of 2D points
     *
     * @return normalized min area rect
     */
    public static RotatedRect normalizedMinAreaRect(Mat points) {
        return normalizeRotatedRect(opencv_imgproc.minAreaRect(points));
    }

    /**
     * Normalizes RotatedRect, so that its angle is in the [-45; 45) range.
     *
     * <p>
     * We want our boxes to have the point order, so that it matches input image orientation.
     * Otherwise, the orientation detection model will get a different box, which is already
     * pre-rotated in some way. Here we will alter the rectangle, so that points would output the
     * expected order.
     *
     * <p>
     * This will make box have points in the following order, relative to the page: BL, TL, TR, BR.
     * Bottom as in bottom of the image, not the lowest Y coordinate.
     *
     * @param rect RotatedRect to normalize
     *
     * @return normalized RotatedRect
     */
    public static RotatedRect normalizeRotatedRect(RotatedRect rect) {
        final float angle = rect.angle();
        final float clampedAngle = MathUtil.euclideanModulo(angle, 360);
        /*
         * For 90 and 270 degrees need to swap sizes.
         */
        if ((45F <= clampedAngle && clampedAngle < 135F)
                || (225F <= clampedAngle && clampedAngle < 315F)) {
            final Size2f rectSize = rect.size();
            try {
                final float temp = rectSize.width();
                rectSize.width(rectSize.height());
                rectSize.height(temp);
                rect.size(rectSize);
            } finally {
                rectSize.close();
            }
            if (clampedAngle < 135F) {
                rect.angle(clampedAngle - 90F);
            } else {
                rect.angle(clampedAngle - 270F);
            }
        } else if (135F <= clampedAngle && clampedAngle < 225F) {
            rect.angle(clampedAngle - 180F);
        } else if (315F <= clampedAngle) {
            rect.angle(clampedAngle - 360F);
        } else {
            assert 0F <= clampedAngle && clampedAngle < 45F;
            rect.angle(clampedAngle);
        }
        return rect;
    }

    /**
     * Equivalent to calling OpenCV {@code minAreaRect}, followed by
     * {@code boxPoints}, but with resource handling taken care of.
     *
     * @param points points to get the rectangle for
     *
     * @return min area rectangle points
     */
    public static Mat minAreaRectBoxPoints(Mat points) {
        try (final RotatedRect rect = opencv_imgproc.minAreaRect(points)) {
            final Mat rectPoints = new Mat();
            try {
                opencv_imgproc.boxPoints(rect, rectPoints);
                return rectPoints;
            } catch (RuntimeException e) {
                rectPoints.close();
                throw e;
            }
        }
    }

    /**
     * Creates an OpenCV polygon, based on the results of a
     * {@code minAreaRectBoxPoints(points)} call, but with resource handling
     * taken care of.
     *
     * @param points points to get the rectangle for
     *
     * @return min area rectangle polygon
     */
    public static Mat minAreaRectBoxPoly(Mat points) {
        try (final Mat rectPoints = OpenCvUtil.minAreaRectBoxPoints(points);
             final Mat rectPointsInt = new Mat()) {
            // +0.5, so that values are rounded, not floored
            rectPoints.convertTo(rectPointsInt, CvType.CV_32S, 1, 0.5);
            return rectPointsInt.reshape(2, new int[]{4, 1});
        }
    }

    /**
     * Fill the polygon on the bitmap at a specific offset.
     *
     * @param bitmap bitmap to fill the polygon on
     * @param poly polygon to fill
     * @param color color to fill the polygon with
     * @param xOffset x offset for the polygon
     * @param yOffset y offset for the polygon
     */
    public static void fillPolyAtOffset(Mat bitmap, Mat poly, Scalar color, int xOffset, int yOffset) {
        try (final MatVector offsetPolys = makeOffsetPolys(poly, xOffset, yOffset)) {
            opencv_imgproc.fillPoly(bitmap, offsetPolys, color);
        }
    }

    private static MatVector makeOffsetPolys(Mat poly, int xOffset, int yOffset) {
        try (final Scalar offset = new Scalar(xOffset, yOffset);
             final Mat offsetPoly = add(poly, offset)) {
            return new MatVector(offsetPoly);
        }
    }

    private static Mat add(Mat a, Scalar s) {
        try (final MatExpr op = opencv_core.add(a, s)) {
            return op.asMat();
        }
    }
}
