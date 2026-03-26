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
import com.itextpdf.pdfocr.onnx.conf.paddle.model.CtcLabelDecode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DbPostProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ScoreMode;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PostProcessParserTest extends ExtendedITextTest {
    @Test
    public void parseDbPostProcessTest() {
        final DbPostProcess expected = new DbPostProcess(
                0.3F, 0.6F, 1.5F, 1000, false, ScoreMode.FAST, BoxType.QUAD
        );
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("name", "DBPostProcess");
        Assertions.assertEquals(expected, PostProcessParser.parse(mapping, "PostProcess"));
    }

    @Test
    public void parseCtcLabelDecodeTest() {
        final CtcLabelDecode expected = new CtcLabelDecode(new String[]{"a", "b"});
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("name", "CTCLabelDecode");
        mapping.put("character_dict", Arrays.asList("a", "b"));
        Assertions.assertEquals(expected, PostProcessParser.parse(mapping, "PostProcess"));
    }

    @Test
    public void parseInvalidRootTest() {
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PostProcessParser.parse("invalid_type", "PostProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PostProcess"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseInvalidNameTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("name", "something unsupported");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PostProcessParser.parse(mapping, "PostProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PostProcess.name"
                ),
                ex.getMessage()
        );
    }
}
