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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class OnnxRecognitionPredictorPropertiesTest extends ExtendedITextTest {
    private static final String BASE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String CRNNVGG16 = BASE_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MOBILENETV3 = BASE_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";

    @Test
    public void testSameModels() {
        OnnxRecognitionPredictorProperties first = OnnxRecognitionPredictorProperties.crnnVgg16(CRNNVGG16);
        Assertions.assertEquals(first, first);
    }

    @Test
    public void testEqualModels() {
        OnnxRecognitionPredictorProperties first = OnnxRecognitionPredictorProperties.crnnVgg16(CRNNVGG16);
        OnnxRecognitionPredictorProperties second = OnnxRecognitionPredictorProperties.crnnVgg16(CRNNVGG16);
        //They will still have different IRecognitionPostProcessor
        Assertions.assertNotEquals(first, second);
    }

    @Test
    public void testEqualWithDifferentObject() {
        OnnxRecognitionPredictorProperties first = OnnxRecognitionPredictorProperties.crnnVgg16(CRNNVGG16);
        String second = "test";
        Assertions.assertNotEquals(first, second);
    }

    @Test
    public void testNotEqualModels() {
        OnnxRecognitionPredictorProperties first = OnnxRecognitionPredictorProperties.crnnVgg16(CRNNVGG16);
        OnnxRecognitionPredictorProperties second = OnnxRecognitionPredictorProperties.crnnMobileNetV3(MOBILENETV3);
        Assertions.assertNotEquals(first, second);
    }
}
