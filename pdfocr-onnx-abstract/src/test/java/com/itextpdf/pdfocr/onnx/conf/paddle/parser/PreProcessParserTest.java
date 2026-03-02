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
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DetResizeForTest;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.NormalizeImage;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.PreProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.RecResizeImg;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.TransformOp;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PreProcessParserTest extends ExtendedITextTest {
    @Test
    public void parseValidFullTest() {
        final PreProcess expected = new PreProcess(new TransformOp[]{
                new DecodeImage(false, ImgMode.BGR),
                new DetResizeForTest(null, false),
                new NormalizeImage(
                        new float[]{0.485F, 0.456F, 0.406F},
                        new float[]{0.229F, 0.224F, 0.225F}
                ),
                new RecResizeImg(new int[]{3, 48, 320}),
        });
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        final ArrayList<Object> ops = new ArrayList<Object>();
        ops.add(createEmptyOp("DecodeImage"));
        ops.add(createEmptyOp("DetResizeForTest"));
        ops.add(createEmptyOp("NormalizeImage"));
        ops.add(createEmptyOp("RecResizeImg"));
        // The ones below are ignored
        ops.add(createEmptyOp("DetLabelEncode"));
        ops.add(createEmptyOp("KeepKeys"));
        ops.add(createEmptyOp("MultiLabelEncode"));
        ops.add(createEmptyOp("ToCHWImage"));
        mapping.put("transform_ops", ops);
        Assertions.assertEquals(expected, PreProcessParser.parse(mapping, "PreProcess"));
    }

    @Test
    public void parseInvalidRootTest() {
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse("invalid_type", "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PreProcess"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseUnexpectedKeyTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("transform_ops", new ArrayList<Object>());
        mapping.put("unexpected", "key");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse(mapping, "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_CONFIG_KEY,
                        "PreProcess.unexpected"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseInvalidOpsTypeTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        mapping.put("transform_ops", new HashMap<Object, Object>());
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse(mapping, "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PreProcess.transform_ops"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseUnexpectedOpTypeTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        final ArrayList<Object> ops = new ArrayList<Object>();
        ops.add(new ArrayList<Object>());
        mapping.put("transform_ops", ops);
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse(mapping, "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PreProcess.transform_ops.0"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseInvalidOpKeyTypeTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        final ArrayList<Object> ops = new ArrayList<Object>();
        final Map<Object, Object> op = new HashMap<Object, Object>();
        op.put(42, new HashMap<Object, Object>());
        ops.add(op);
        mapping.put("transform_ops", ops);
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse(mapping, "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PreProcess.transform_ops.0"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseUnexpectedOpKeyTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        final ArrayList<Object> ops = new ArrayList<Object>();
        final Map<Object, Object> op = new HashMap<Object, Object>();
        op.put("DecodeImage", new HashMap<Object, Object>());
        op.put("unexpected", "key");
        ops.add(op);
        mapping.put("transform_ops", ops);
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse(mapping, "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PreProcess.transform_ops.0"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseUnexpectedOpTest() {
        final Map<Object, Object> mapping = new HashMap<Object, Object>();
        final ArrayList<Object> ops = new ArrayList<Object>();
        final Map<Object, Object> op = new HashMap<Object, Object>();
        op.put("UnexpectedOp", new HashMap<Object, Object>());
        ops.add(op);
        mapping.put("transform_ops", ops);
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> PreProcessParser.parse(mapping, "PreProcess")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PreProcess.transform_ops.0"
                ),
                ex.getMessage()
        );
    }

    private static Map<Object, Object> createEmptyOp(String name) {
        final Map<Object, Object> mapping = new HashMap<Object, Object>(1);
        mapping.put(name, new HashMap<Object, Object>());
        return mapping;
    }
}
