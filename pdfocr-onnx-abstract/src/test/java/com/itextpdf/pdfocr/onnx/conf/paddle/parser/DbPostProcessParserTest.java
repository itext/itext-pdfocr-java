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
import com.itextpdf.pdfocr.onnx.conf.paddle.model.BoxType;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DbPostProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ScoreMode;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
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
public class DbPostProcessParserTest extends ExtendedITextTest {
    @Test
    public void parseValidFullTest() {
        final DbPostProcess expected = new DbPostProcess(
                0.2F, 0.8F, 1.0F, 475, true, ScoreMode.SLOW, BoxType.POLY
        );
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("name", "DBPostProcess");
        mapping.put("thresh", "0.2");
        mapping.put("box_thresh", "0.8");
        mapping.put("unclip_ratio", "1");
        mapping.put("max_candidates", "475");
        mapping.put("use_dilation", "true");
        mapping.put("score_mode", "slow");
        mapping.put("box_type", "poly");
        Assertions.assertEquals(expected, DbPostProcessParser.parse(mapping, "PostProcess"));
    }

    @Test
    public void parseValidDefaultsTest() {
        final DbPostProcess expected = new DbPostProcess(
                0.3F, 0.6F, 1.5F, 1000, false, ScoreMode.FAST, BoxType.QUAD
        );
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("name", "DBPostProcess");
        Assertions.assertEquals(expected, DbPostProcessParser.parse(mapping, "PostProcess"));
    }

    public static Iterable<Object[]> parseInvalidFieldTestParams() {
        return Arrays.asList(new Object[][] {
                {"thresh"},
                {"box_thresh"},
                {"unclip_ratio"},
                {"max_candidates"},
                {"use_dilation"},
                {"score_mode"},
                {"box_type"},
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parseInvalidFieldTestParams")
    public void parseInvalidFieldTest(String field) {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("name", "DBPostProcess");
        mapping.put(field, "invalid_value");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> DbPostProcessParser.parse(mapping, "PostProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PostProcess." + field
                ),
                ex.getMessage()
        );
    }
}
