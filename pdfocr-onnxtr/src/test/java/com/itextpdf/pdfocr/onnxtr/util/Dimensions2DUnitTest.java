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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

@Tag("UnitTest")
public class Dimensions2DUnitTest extends ExtendedITextTest {

    public static Iterable<Object[]> equalsFalse() {
        return Arrays.asList(new Object[][]{
                {new Dimensions2D(100, 20), null},
                {new Dimensions2D(100, 20), "different class"},
                {new Dimensions2D(100, 700), new Dimensions2D(100, 800)},
                {new Dimensions2D(100, 800), new Dimensions2D(200, 800)},
                {new Dimensions2D(100, 700), new Dimensions2D(200, 800)},
        });
    }

    public static Iterable<Object[]> equalsTrue() {
        Dimensions2D dimensions2D = new Dimensions2D(100, 200);
        return Arrays.asList(new Object[][]{
                {dimensions2D, new Dimensions2D(100, 200)},
                {dimensions2D, dimensions2D},
        });
    }

    @ParameterizedTest(name = "first: {0}, second: {1}")
    @MethodSource("equalsFalse")
    public void equalsNegativeTest(Dimensions2D first, Object second) {
        Assertions.assertNotEquals(first, second);
    }

    @ParameterizedTest(name = "first: {0}, second: {1}")
    @MethodSource("equalsTrue")
    public void equalsPositiveTest(Dimensions2D first, Dimensions2D second) {
        Assertions.assertEquals(first, second);
    }
}
