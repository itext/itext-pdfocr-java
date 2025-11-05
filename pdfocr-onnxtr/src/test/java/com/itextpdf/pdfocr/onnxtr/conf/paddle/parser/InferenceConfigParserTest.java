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
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.BoxType;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DbPostProcess;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DetResizeForTest;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.InferenceConfig;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.NormalizeImage;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.PreProcess;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.ScoreMode;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.TransformOp;
import com.itextpdf.pdfocr.onnxtr.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class InferenceConfigParserTest extends ExtendedITextTest {
    private static final String BASE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_YAML_CONFIG =
            BASE_DIRECTORY + "models/PP-OCRv5_mobile_det_infer/inference.yml";

    @Test
    public void parseValidTest() throws IOException {
        final InferenceConfig expected = new InferenceConfig(
                new PreProcess(new TransformOp[]{
                        new DecodeImage(false, ImgMode.BGR),
                        new DetResizeForTest(null, false),
                        new NormalizeImage(
                                new float[]{0.485F, 0.456F, 0.406F},
                                new float[]{0.229F, 0.224F, 0.225F}
                        )
                }),
                new DbPostProcess(0.3F, 0.6F, 1.5F, 1000, false, ScoreMode.FAST, BoxType.QUAD)
        );
        final InferenceConfig actual;
        try (final InputStream stream = Files.newInputStream(Paths.get(TEST_YAML_CONFIG))) {
            actual = InferenceConfigParser.parse(stream);
        }
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void parseInvalidRootTest() {
        final ByteArrayInputStream stream = createTestStream("\"string\"");
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> InferenceConfigParser.parse(stream)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "<root>"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void parseInvalidPreProcessTest() {
        final ByteArrayInputStream stream = createTestStream(
                "PreProcess:\n" +
                "  unexpected: ~\n" +
                "PostProcess:\n" +
                "  name: DBPostProcess\n"
        );
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> InferenceConfigParser.parse(stream)
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
    public void parseInvalidPostProcessTest() {
        final ByteArrayInputStream stream = createTestStream(
                "PreProcess:\n" +
                "  transform_ops:\n" +
                "  - DetLabelEncode: null\n" +
                "PostProcess:\n" +
                "  name: Unsupported\n"
        );
        final Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> InferenceConfigParser.parse(stream)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY,
                        "PostProcess.name"
                ),
                ex.getMessage()
        );
    }

    private static ByteArrayInputStream createTestStream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
}
