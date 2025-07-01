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

class OnnxTRTestUtil {
    static void testNormalizeRotatedRect(
            float originalAngle,
            float newWidth,
            float newHeight,
            float newAngle
    ) {
        try (final Point2f center = new Point2f(0, 0);
             final Size2f size = new Size2f(5, 10);
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
