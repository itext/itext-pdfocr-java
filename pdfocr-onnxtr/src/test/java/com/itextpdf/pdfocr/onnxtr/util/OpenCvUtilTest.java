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

import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class OpenCvUtilTest extends ExtendedITextTest {
    private static final float WIDTH = 5;
    private static final float HEIGHT = 10;

    @Test
    public void normalizeRotatedRect() {
        for (int rotationIdx = -2; rotationIdx <= 2; ++rotationIdx) {
            final float baseAngle = rotationIdx * 360;
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 0, WIDTH, HEIGHT, 0);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 30, WIDTH, HEIGHT, 30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 60, HEIGHT, WIDTH, -30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 90, HEIGHT, WIDTH, 0);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 120, HEIGHT, WIDTH, 30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 150, WIDTH, HEIGHT, -30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 180, WIDTH, HEIGHT, 0);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 210, WIDTH, HEIGHT, 30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 240, HEIGHT, WIDTH, -30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 270, HEIGHT, WIDTH, 0);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 300, HEIGHT, WIDTH, 30);
            OnnxTRTestUtil.testNormalizeRotatedRect(baseAngle + 330, WIDTH, HEIGHT, -30);
        }
    }
}
