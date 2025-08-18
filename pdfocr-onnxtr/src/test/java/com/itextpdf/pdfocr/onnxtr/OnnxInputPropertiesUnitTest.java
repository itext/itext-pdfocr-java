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
package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class OnnxInputPropertiesUnitTest extends ExtendedITextTest {
    @Test
    public void unexpectedMeanChannelCountTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MEAN_CHANNEL_COUNT, 3), e.getMessage());
    }

    @Test
    public void unexpectedStdChannelCountTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F, 0.772F};
        long[] shape = new long[]{2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_STD_CHANNEL_COUNT, 3), e.getMessage());
    }

    @Test
    public void unexpectedShapeSizeTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 3, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_SHAPE_SIZE, 4), e.getMessage());
    }

    @Test
    public void unexpectedShapeChannelCountTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 4, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(PdfOcrOnnxTrExceptionMessageConstant.MODEL_ONLY_SUPPORTS_RGB, e.getMessage());
    }

    @Test
    public void unexpectedDimensionValueTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{-2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_DIMENSION_VALUE, -2), e.getMessage());
    }
}
