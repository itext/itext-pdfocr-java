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
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ImgMode;
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

@Tag("UnitTest")
public class ParserUtilTest extends ExtendedITextTest {
    @Test
    public void castToStringArrayTest() {
        // All strings are valid
        Assertions.assertArrayEquals(
                new String[]{"a", "b", "c"},
                ParserUtil.castToStringArray(Arrays.asList((Object)"a", "b", "c"), "Obj")
        );
        // null instead of collection
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.castToStringArray(null, "Obj")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj"
                ),
                ex.getMessage()
        );
        // null inside collection
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.castToStringArray(Arrays.asList((Object)"a", null, "c"), "Obj")
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.1"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultFloatTest() {
        // Key exists and valid
        Assertions.assertEquals(3.14F, ParserUtil.getOrDefault(mapOf("k", "3.14"), "Obj", "k", 1.0F));
        Assertions.assertEquals(3.14F, ParserUtil.getOrDefault(mapOf("k", 3.14), "Obj", "k", 1.0F));
        // Key does not exist
        Assertions.assertEquals(1.0F, ParserUtil.getOrDefault(mapOf("a", "3.14"), "Obj", "b", 1.0F));
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "x.xx"), "Obj", "k", 1.0F)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", 1.0F)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultFloatArrayTest() {
        final float[] defaultValue = new float[]{-1.0F, 0.0F, 1.0F};
        // Key exists and valid
        Assertions.assertArrayEquals(
                new float[]{3.14F, 13.37F},
                ParserUtil.getOrDefault(
                        mapOf("k", Arrays.asList("3.14", "13.37")), "Obj", "k", defaultValue
                )
        );
        // Key does not exist
        Assertions.assertSame(
                defaultValue,
                ParserUtil.getOrDefault(
                        mapOf("a", Arrays.asList("3.14", "13.37")), "Obj", "b", defaultValue
                )
        );
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(
                        mapOf("k", Arrays.asList("3.14", "x.xx")), "Obj", "k", defaultValue
                )
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k.1"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "[]"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultIntTest() {
        // Key exists and valid
        Assertions.assertEquals(42, ParserUtil.getOrDefault(mapOf("k", "42"), "Obj", "k", -1));
        Assertions.assertEquals(42, ParserUtil.getOrDefault(mapOf("k", 42), "Obj", "k", -1));
        // Key does not exist
        Assertions.assertEquals(-1, ParserUtil.getOrDefault(mapOf("a", "42"), "Obj", "b", -1));
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "xx"), "Obj", "k", -1)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", -1)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultIntArrayTest() {
        final int[] defaultValue = new int[]{-1, 0, 1};
        // Key exists and valid
        Assertions.assertArrayEquals(
                new int[]{42, 777},
                ParserUtil.getOrDefault(
                        mapOf("k", Arrays.asList("42", "777")), "Obj", "k", defaultValue
                )
        );
        // Key does not exist
        Assertions.assertSame(
                defaultValue,
                ParserUtil.getOrDefault(
                        mapOf("a", Arrays.asList("42", "777")), "Obj", "b", defaultValue
                )
        );
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(
                        mapOf("k", Arrays.asList("42", "xxx")), "Obj", "k", defaultValue
                )
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k.1"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "[]"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultBooleanTest() {
        // Key exists and valid
        Assertions.assertFalse(ParserUtil.getOrDefault(mapOf("k", "false"), "Obj", "k", true));
        Assertions.assertTrue(ParserUtil.getOrDefault(mapOf("k", "true"), "Obj", "k", false));
        Assertions.assertFalse(ParserUtil.getOrDefault(mapOf("k", Boolean.FALSE), "Obj", "k", true));
        Assertions.assertTrue(ParserUtil.getOrDefault(mapOf("k", Boolean.TRUE), "Obj", "k", false));
        // Key does not exist
        Assertions.assertFalse(ParserUtil.getOrDefault(mapOf("a", "true"), "Obj", "b", false));
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "xxxxx"), "Obj", "k", false)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", false)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultScoreModeTest() {
        final ScoreMode defaultValue = ScoreMode.SLOW;
        // Key exists and valid
        Assertions.assertEquals(
                ScoreMode.FAST,
                ParserUtil.getOrDefault(mapOf("k", "fast"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                ScoreMode.SLOW,
                ParserUtil.getOrDefault(mapOf("k", "slow"), "Obj", "k", defaultValue)
        );
        // Key does not exist
        Assertions.assertEquals(
                defaultValue,
                ParserUtil.getOrDefault(mapOf("a", "fast"), "Obj", "b", defaultValue)
        );
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "xxxx"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultBoxTypeTest() {
        final BoxType defaultValue = BoxType.POLY;
        // Key exists and valid
        Assertions.assertEquals(
                BoxType.QUAD,
                ParserUtil.getOrDefault(mapOf("k", "quad"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                BoxType.POLY,
                ParserUtil.getOrDefault(mapOf("k", "poly"), "Obj", "k", defaultValue)
        );
        // Key does not exist
        Assertions.assertEquals(
                defaultValue,
                ParserUtil.getOrDefault(mapOf("a", "quad"), "Obj", "b", defaultValue)
        );
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "xxx"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    @Test
    public void getOrDefaultImgModeTest() {
        final ImgMode defaultValue = ImgMode.BGR;
        // Key exists and valid
        Assertions.assertEquals(
                ImgMode.GRAY,
                ParserUtil.getOrDefault(mapOf("k", "GRAY"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                ImgMode.RGB,
                ParserUtil.getOrDefault(mapOf("k", "RGB"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                ImgMode.BGR,
                ParserUtil.getOrDefault(mapOf("k", "BGR"), "Obj", "k", defaultValue)
        );
        // Key does not exist
        Assertions.assertEquals(
                defaultValue,
                ParserUtil.getOrDefault(mapOf("a", "RGB"), "Obj", "b", defaultValue)
        );
        // Key exists and invalid
        Throwable ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", "XXX"), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
        ex = Assertions.assertThrows(
                ConfigParserException.class,
                () -> ParserUtil.getOrDefault(mapOf("k", null), "Obj", "k", defaultValue)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, "Obj.k"
                ),
                ex.getMessage()
        );
    }

    private static Map<Object, Object> mapOf(Object... kvPairs) {
        assert kvPairs.length % 2 == 0;

        final Map<Object, Object> obj = new HashMap<Object, Object>(kvPairs.length / 2);
        for (int i = 0; i < kvPairs.length; i += 2) {
            obj.put(kvPairs[i], kvPairs[i + 1]);
        }
        return obj;
    }
}
