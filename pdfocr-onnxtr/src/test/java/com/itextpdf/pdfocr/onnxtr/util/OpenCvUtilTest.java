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

import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.RotatedRect;
import org.bytedeco.opencv.opencv_core.Size2f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenCvUtilTest {
    private static final float WIDTH = 5;
    private static final float HEIGHT = 10;

    @Test
    void normalizeRotatedRect() {
        for (int rotationIdx = -2; rotationIdx <= 2; ++rotationIdx) {
            final float baseAngle = rotationIdx * 360;
            testNormalizeRotatedRect(baseAngle + 0, WIDTH, HEIGHT, 0);
            testNormalizeRotatedRect(baseAngle + 30, WIDTH, HEIGHT, 30);
            testNormalizeRotatedRect(baseAngle + 60, HEIGHT, WIDTH, -30);
            testNormalizeRotatedRect(baseAngle + 90, HEIGHT, WIDTH, 0);
            testNormalizeRotatedRect(baseAngle + 120, HEIGHT, WIDTH, 30);
            testNormalizeRotatedRect(baseAngle + 150, WIDTH, HEIGHT, -30);
            testNormalizeRotatedRect(baseAngle + 180, WIDTH, HEIGHT, 0);
            testNormalizeRotatedRect(baseAngle + 210, WIDTH, HEIGHT, 30);
            testNormalizeRotatedRect(baseAngle + 240, HEIGHT, WIDTH, -30);
            testNormalizeRotatedRect(baseAngle + 270, HEIGHT, WIDTH, 0);
            testNormalizeRotatedRect(baseAngle + 300, HEIGHT, WIDTH, 30);
            testNormalizeRotatedRect(baseAngle + 330, WIDTH, HEIGHT, -30);
        }
    }

    private void testNormalizeRotatedRect(
            float originalAngle,
            float newWidth,
            float newHeight,
            float newAngle
    ) {
        try (final Point2f center = new Point2f(0, 0);
             final Size2f size = new Size2f(WIDTH, HEIGHT);
             final RotatedRect rect = new RotatedRect(center, size, originalAngle)) {
            OpenCvUtil.normalizeRotatedRect(rect);
            try (final Size2f newSize = rect.size()) {
                Assertions.assertEquals(newWidth, newSize.width(), 1e-6);
                Assertions.assertEquals(newHeight, newSize.height(), 1e-6);
            }
            Assertions.assertEquals(newAngle, rect.angle(), 1e-6);
        }
    }
}
