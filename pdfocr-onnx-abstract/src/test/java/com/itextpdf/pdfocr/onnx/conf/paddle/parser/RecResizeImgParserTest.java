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
package com.itextpdf.pdfocr.onnx.conf.paddle.parser;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.RecResizeImg;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class RecResizeImgParserTest extends ExtendedITextTest {
    @Test
    public void parseValidFullTest() {
        final RecResizeImg expected = new RecResizeImg(new int[]{3, 64, 480});
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("image_shape", Arrays.asList("3", "64", "480"));
        Assertions.assertEquals(expected, RecResizeImgParser.parse(mapping, "Op"));
    }

    @Test
    public void parseValidDefaultsTest() {
        final RecResizeImg expected = new RecResizeImg(new int[]{3, 48, 320});
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        Assertions.assertEquals(expected, RecResizeImgParser.parse(mapping, "Op"));
        // null should also work
        Assertions.assertEquals(expected, RecResizeImgParser.parse(null, "Op"));
    }

    @Test
    public void parseInvalidRootTest() {
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> RecResizeImgParser.parse("invalid_value", "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseInvalidImageShapeTypeTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("image_shape", "invalid_value");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> RecResizeImgParser.parse(mapping, "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op.image_shape"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseInvalidImageShapeLengthTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("image_shape", Arrays.asList("3", "48"));
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> RecResizeImgParser.parse(mapping, "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op.image_shape"
                ),
                ex.getMessage()
        );
    }
}
