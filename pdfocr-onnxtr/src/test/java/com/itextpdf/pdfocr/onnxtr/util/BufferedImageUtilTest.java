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

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;
import com.itextpdf.test.ExtendedITextTest;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class BufferedImageUtilTest extends ExtendedITextTest {
    @Test
    public void toBchwInputRgbBasicTest() {
        final long[] expectedShape = new long[]{2, 3, 1, 2};
        final float[] expectedData = new float[]{
                 0.616073F, -0.211813F,
                -2.294872F,  0.148567F,
                -0.015602F,  0.549441F,

                -0.308642F,  0.199710F,
                -0.228507F,  0.978130F,
                 0.815096F,  0.532574F,
        };
        final List<BufferedImage> images = Arrays.asList(
                newRgbImage(2, 1, new int[]{
                        0xBF2220, 0x14C4A6,
                }),
                newRgbImage(2, 1, new int[]{
                        0x00ABE5, 0x69FBA2,
                })
        );
        final OnnxInputProperties props = new OnnxInputProperties(
                new float[]{0.25F, 0.73F, 0.14F},
                new float[]{0.81F, 0.26F, 0.93F},
                new long[]{4, 3, 1, 2},
                false
        );
        toBchwInputBasicTest(expectedShape, expectedData, images, props);
    }

    private static void toBchwInputBasicTest(
            long[] expectedShape,
            float[] expectedData,
            Collection<BufferedImage> images,
            OnnxInputProperties props
    ) {
        final FloatBufferMdArray result = BufferedImageUtil.toBchwInput(images, props);
        Assertions.assertArrayEquals(expectedShape, result.getShape());
        final float[] actualData = new float[12];
        result.getData().get(actualData);
        Assertions.assertArrayEquals(expectedData, actualData, 1E-6F);
    }

    private static BufferedImage newRgbImage(int width, int height, int[] pixels) {
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final WritableRaster raster = img.getRaster();
        raster.setDataElements(0, 0, width, height, pixels);
        return img;
    }
}