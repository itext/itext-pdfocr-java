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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.DefaultOrtSessionOptionsCreator;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class OnnxRecognitionPredictorUnitTest extends ExtendedITextTest {
    @Test
    public void nullModelPathTest() {
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.crnnVgg16(null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.crnnVgg16(null, null));

        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.crnnMobileNetV3(null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.crnnMobileNetV3(null, null));

        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.master(null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.master(null, null));

        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.parSeq(null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.parSeq(null, null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.parSeq(null, Vocabulary.LATIN_EXTENDED, 0));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.parSeq(null, Vocabulary.LATIN_EXTENDED, 0, null));

        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.sar(null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.sar(null, null));

        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.viTstr(null));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.viTstr(null, null));

        Assertions.assertThrows(Exception.class, () -> OnnxRecognitionPredictor.paddleOcr(null));
        Assertions.assertThrows(Exception.class, () -> OnnxRecognitionPredictor.paddleOcr(null, new DefaultOrtSessionOptionsCreator()));
        Assertions.assertThrows(Exception.class, () -> OnnxRecognitionPredictor.paddleOcr(null, ""));
        Assertions.assertThrows(Exception.class, () -> OnnxRecognitionPredictor.paddleOcr(null, "", null));

        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.easyOcr(null, EasyOcrMapper.LATIN_G2));
        Assertions.assertThrows(NullPointerException.class, () -> OnnxRecognitionPredictor.easyOcr(null, EasyOcrMapper.LATIN_G2, null));
    }
}
