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
package com.itextpdf.pdfocr.onnx.merging;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.test.ExtendedITextTest;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class EasyOcrTextBoxMergerTest extends ExtendedITextTest {
    @Test
    public void basic() {
        final List<Point[]> detectedTextBoxes = Arrays.asList(
                // Three aligned rectangles close on the first line
                new Point[]{
                        new Point(0.15, 0.10),
                        new Point(0.15, 0.05),
                        new Point(0.25, 0.05),
                        new Point(0.25, 0.10),
                },
                new Point[]{
                        new Point(0.05, 0.10),
                        new Point(0.05, 0.05),
                        new Point(0.10, 0.05),
                        new Point(0.10, 0.10),
                },
                new Point[]{
                        new Point(0.30, 0.10),
                        new Point(0.30, 0.05),
                        new Point(0.45, 0.05),
                        new Point(0.45, 0.10),
                },
                // Sloped rectangle on the first line
                new Point[]{
                        new Point(0.525, 0.150),
                        new Point(0.500, 0.100),
                        new Point(0.575, 0.050),
                        new Point(0.600, 0.100),
                },
                // Two separated aligned rectangles on the second line
                new Point[]{
                        new Point(0.05, 0.25),
                        new Point(0.05, 0.20),
                        new Point(0.20, 0.20),
                        new Point(0.20, 0.25),
                },
                new Point[]{
                        new Point(0.35, 0.25),
                        new Point(0.35, 0.20),
                        new Point(0.55, 0.20),
                        new Point(0.55, 0.25),
                },
                // Single aligned vertical rectangle on the third line
                new Point[]{
                        new Point(0.05, 0.45),
                        new Point(0.05, 0.35),
                        new Point(0.10, 0.35),
                        new Point(0.10, 0.45),
                }
        );
        final List<Point[]> expectedResult = Arrays.asList(
                // Sloped rectangle on the first line
                new Point[]{
                        new Point(0.5215, 0.1571),
                        new Point(0.4921, 0.1000),
                        new Point(0.5785, 0.0429),
                        new Point(0.6079, 0.1000),
                },
                // Three merged aligned rectangles on the first line
                new Point[]{
                        new Point(0.0450, 0.1050),
                        new Point(0.0450, 0.0450),
                        new Point(0.4550, 0.0450),
                        new Point(0.4550, 0.1050),
                },
                // Two separated aligned rectangles on the second line
                new Point[]{
                        new Point(0.0450, 0.2550),
                        new Point(0.0450, 0.1950),
                        new Point(0.2050, 0.1950),
                        new Point(0.2050, 0.2550),
                },
                new Point[]{
                        new Point(0.3450, 0.2550),
                        new Point(0.3450, 0.1950),
                        new Point(0.5550, 0.1950),
                        new Point(0.5550, 0.2550),
                },
                // Single aligned vertical rectangle on the third line
                new Point[]{
                        new Point(0.0450, 0.4550),
                        new Point(0.0450, 0.3450),
                        new Point(0.1050, 0.3450),
                        new Point(0.1050, 0.4550),
                }
        );
        final List<Point[]> actualResult = new EasyOcrTextBoxMerger().process(detectedTextBoxes);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void noAlignedBoxes() {
        final List<Point[]> detectedTextBoxes = Arrays.asList(
                // Two sloped rectangles
                new Point[]{
                        new Point(0.525, 0.150),
                        new Point(0.500, 0.100),
                        new Point(0.575, 0.050),
                        new Point(0.600, 0.100),
                },
                new Point[]{
                        new Point(0.100, 0.150),
                        new Point(0.050, 0.050),
                        new Point(0.100, 0.025),
                        new Point(0.150, 0.125),
                }
        );
        final List<Point[]> expectedResult = Arrays.asList(
                // Two sloped rectangles
                new Point[]{
                        new Point(0.5215, 0.1571),
                        new Point(0.4921, 0.1000),
                        new Point(0.5785, 0.0429),
                        new Point(0.6079, 0.1000),
                },
                new Point[]{
                        new Point(0.1000, 0.1579),
                        new Point(0.0437, 0.0453),
                        new Point(0.1000, 0.0171),
                        new Point(0.1563, 0.1297),
                }
        );
        final List<Point[]> actualResult = new EasyOcrTextBoxMerger().process(detectedTextBoxes);
        assertEquals(expectedResult, actualResult);
    }

    private static void assertEquals(List<Point[]> expected, List<Point[]> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); ++i) {
            final Point[] expectedBox = expected.get(i);
            final Point[] actualBox = actual.get(i);
            Assertions.assertEquals(expectedBox.length, actualBox.length);
            for (int j = 0; j < expectedBox.length; ++j) {
                final Point expectedPoint = expectedBox[j];
                final Point actualPoint = actualBox[j];
                Assertions.assertEquals(expectedPoint.getX(), actualPoint.getX(), 1E-4);
                Assertions.assertEquals(expectedPoint.getY(), actualPoint.getY(), 1E-4);
            }
        }
    }
}
