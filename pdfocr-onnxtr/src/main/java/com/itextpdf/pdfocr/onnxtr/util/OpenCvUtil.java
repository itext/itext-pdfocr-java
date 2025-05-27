/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
package com.itextpdf.pdfocr.onnxtr.util;

import org.bytedeco.opencv.opencv_core.RotatedRect;
import org.bytedeco.opencv.opencv_core.Size2f;

/**
 * Static class with OpenCV utility functions.
 */
public final class OpenCvUtil {
    private OpenCvUtil() {
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
            try (final Size2f rectSize = rect.size()) {
                final float tempWidth = rectSize.width();
                rectSize.width(rectSize.height());
                rectSize.height(tempWidth);
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
}
