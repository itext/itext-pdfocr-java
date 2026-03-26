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
import com.itextpdf.pdfocr.onnx.conf.paddle.model.NormalizeImage;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
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
public class NormalizeImageParserTest extends ExtendedITextTest {
    @Test
    public void parseValidFullTest() {
        final NormalizeImage expected = new NormalizeImage(
                new float[]{-1.0F, 0.0F, 1.0F},
                new float[]{2.0F, 4.0F, 8.0F}
        );
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("mean", Arrays.asList("-1.", "0.", "1"));
        mapping.put("std", Arrays.asList("2.", "4.", "8"));
        Assertions.assertEquals(expected, NormalizeImageParser.parse(mapping, "Op"));
    }

    @Test
    public void parseValidDefaultsTest() {
        final NormalizeImage expected = new NormalizeImage(
                new float[]{0.485F, 0.456F, 0.406F},
                new float[]{0.229F, 0.224F, 0.225F}
        );
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        Assertions.assertEquals(expected, NormalizeImageParser.parse(mapping, "Op"));
        // null should also work
        Assertions.assertEquals(expected, NormalizeImageParser.parse(null, "Op"));
    }

    @Test
    public void parseInvalidRootTest() {
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> NormalizeImageParser.parse("invalid_value", "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op"
                ),
                ex.getMessage()
        );
    }

    public static Iterable<Object[]> parseInvalidFieldTestParams() {
        return Arrays.asList(new Object[][] {
                {"mean"},
                {"std"},
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parseInvalidFieldTestParams")
    public void parseInvalidFieldTest(String field) {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put(field, "invalid_value");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> NormalizeImageParser.parse(mapping, "Op")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "Op." + field
                ),
                ex.getMessage()
        );
    }
}
