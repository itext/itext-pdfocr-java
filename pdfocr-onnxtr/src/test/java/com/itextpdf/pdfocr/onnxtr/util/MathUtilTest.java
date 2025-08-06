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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class MathUtilTest extends ExtendedITextTest {
    @Test
    public void argmaxWithInvalidArgs() {
        Exception exception = Assertions.assertThrows(
                Exception.class,
                () -> MathUtil.argmax(null)
        );
        Assertions.assertTrue(exception instanceof NullPointerException
                || exception instanceof IllegalArgumentException);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> MathUtil.argmax(new float[0])
        );
    }

    @Test
    public void argmaxWithValidArgs() {
        Assertions.assertEquals(0, MathUtil.argmax(new float[] {1}));
        Assertions.assertEquals(1, MathUtil.argmax(new float[] {1, 3, 2}));
        Assertions.assertEquals(1, MathUtil.argmax(new float[] {1, 3, 3}));
    }

    @Test
    public void clampWithInvalidArgs() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtil.clamp(2, 3, 1));
    }

    @Test
    public void clampWithValidArgs() {
        Assertions.assertEquals(1.1, MathUtil.clamp(1.0, 1.1, 1.9));
        Assertions.assertEquals(1.5, MathUtil.clamp(1.5, 1.1, 1.9));
        Assertions.assertEquals(1.9, MathUtil.clamp(2.0, 1.1, 1.9));
    }

    @Test
    public void logitTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtil.logit(-0.1F));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.logit(0F));
        Assertions.assertEquals(0F, MathUtil.logit(0.5F));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.logit(1F));
        Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtil.logit(1.1F));
    }

    @Test
    public void levenshteinDistanceTest(){
        Assertions.assertEquals(5, MathUtil.calculateLevenshteinDistance("kitten", "meat"));
        Assertions.assertEquals(1, MathUtil.calculateLevenshteinDistance("kitten", "kitte"));
        Assertions.assertEquals(7, MathUtil.calculateLevenshteinDistance("kitten", "testString"));
        Assertions.assertEquals(10, MathUtil.calculateLevenshteinDistance("", "testString"));
        Assertions.assertEquals(6, MathUtil.calculateLevenshteinDistance("kitten", ""));
        Assertions.assertEquals(0, MathUtil.calculateLevenshteinDistance("", ""));
        Assertions.assertEquals(0, MathUtil.calculateLevenshteinDistance(null, null));
        Assertions.assertEquals(10, MathUtil.calculateLevenshteinDistance(null, "testString"));
    }
}
