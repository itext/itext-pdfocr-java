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
package com.itextpdf.pdfocr.onnxtr.conf.paddle.parser;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnxtr.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("UnitTest")
public class DecodeImageParserTest extends ExtendedITextTest {
    @Test
    public void parseValidFullTest() {
        final DecodeImage expected = new DecodeImage(true, ImgMode.GRAY);
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("channel_first", "true");
        mapping.put("img_mode", "GRAY");
        Assertions.assertEquals(expected, DecodeImageParser.parse(mapping, "Op"));
    }

    @Test
    public void parseValidDefaultsTest() {
        final DecodeImage expected = new DecodeImage(false, ImgMode.BGR);
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        Assertions.assertEquals(expected, DecodeImageParser.parse(mapping, "Op"));
        // null should also work
        Assertions.assertEquals(expected, DecodeImageParser.parse(null, "Op"));
    }

    @Test
    public void parseInvalidRootTest() {
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> DecodeImageParser.parse("invalid_value", "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op"
                ),
                ex.getMessage()
        );
    }

    public static Iterable<Object[]> parseInvalidFieldTestParams() {
        return Arrays.asList(new Object[][] {
                {"channel_first"},
                {"img_mode"},
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parseInvalidFieldTestParams")
    public void parseInvalidFieldTest(String field) {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put(field, "invalid_value");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> DecodeImageParser.parse(mapping, "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op." + field
                ),
                ex.getMessage()
        );
    }
}
