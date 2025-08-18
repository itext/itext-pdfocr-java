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
package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPostProcessor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictorProperties;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnxtr.orientation.DefaultOrientationMapper;
import com.itextpdf.pdfocr.onnxtr.recognition.CrnnPostProcessor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictorProperties;
import com.itextpdf.pdfocr.onnxtr.recognition.Vocabulary;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

@Tag("UnitTest")
public class OnnxTRUnitTest extends ExtendedITextTest {
    private static final String BASE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String FAST = BASE_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private static final String TIFF = BASE_DIRECTORY + "images/two_pages.tiff";

    @Test
    public void emptyImageInputTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () ->
                BufferedImageUtil.toBchwInput(new ArrayList<>(), new OnnxInputProperties(mean, std, shape, true)));
        Assertions.assertEquals(PdfOcrOnnxTrExceptionMessageConstant.SHAPE_IS_NOT_VALID, e.getMessage());
    }

    @Test
    public void tooManyImagesTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{1, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () ->
                BufferedImageUtil.toBchwInput(OnnxTrOcrEngine.getImages(new File(TIFF)),
                        new OnnxInputProperties(mean, std, shape, true)));
        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrOnnxTrExceptionMessageConstant.TOO_MANY_IMAGES, 2, 1),
                e.getMessage());
    }

    @Test
    public void invalidOrientationTest() {
        Exception e = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> new DefaultOrientationMapper().map(4));
        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrOnnxTrExceptionMessageConstant.INDEX_OUT_OF_BOUNDS, 4),
                e.getMessage());
    }

    @Test
    public void invalidModelPathTest() {
        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> OnnxDetectionPredictor.fast("invalid"));
        Assertions.assertEquals(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_INIT_ONNX_RUNTIME_SESSION,
                e.getMessage());
    }

    @Test
    public void invalidModelTest() {
        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> OnnxRecognitionPredictor.crnnVgg16(FAST));
        Assertions.assertEquals(PdfOcrOnnxTrExceptionMessageConstant.MODEL_DID_NOT_PASS_VALIDATION, e.getMessage());
    }

    @Test
    public void compareDetectionPredictorPropertiesTest() {
        String model = "model";
        OnnxDetectionPredictorProperties properties = OnnxDetectionPredictorProperties.dbNet(model);
        Assertions.assertEquals(properties.hashCode(), OnnxDetectionPredictorProperties.dbNet(model).hashCode());
        Assertions.assertEquals(properties, OnnxDetectionPredictorProperties.dbNet(model));
        Assertions.assertNotEquals(properties, OnnxRecognitionPredictorProperties.crnnVgg16(model));
        Assertions.assertEquals(properties, properties);
        Assertions.assertNotEquals(properties, null);
        Assertions.assertNotEquals(properties, OnnxDetectionPredictorProperties.dbNet("model2"));
        OnnxInputProperties inputProperties = new OnnxInputProperties(
                new float[]{1F, 1F, 1F},
                new float[]{1F, 1F, 1F},
                new long[]{512, 3, 32, 128},
                false
        );
        Assertions.assertNotEquals(properties, new OnnxDetectionPredictorProperties(model, inputProperties,
                new OnnxDetectionPostProcessor()));
        Assertions.assertNotEquals(new OnnxDetectionPredictorProperties(model, inputProperties,
                new OnnxDetectionPostProcessor(1F, 1F)), new OnnxDetectionPredictorProperties(model,
                inputProperties, new OnnxDetectionPostProcessor(2F, 2F)));
    }

    @Test
    public void compareRecognitionPredictorPropertiesTest() {
        OnnxRecognitionPredictorProperties properties = OnnxRecognitionPredictorProperties.crnnMobileNetV3("model");
        Assertions.assertNotEquals(properties.hashCode(),
                OnnxRecognitionPredictorProperties.crnnMobileNetV3("model").hashCode());
        Assertions.assertNotEquals(properties, OnnxRecognitionPredictorProperties.crnnMobileNetV3("model"));
        Assertions.assertEquals(properties, properties);
        Assertions.assertNotEquals(properties, null);
        OnnxInputProperties inputProperties = new OnnxInputProperties(
                new float[]{1F, 1F, 1F},
                new float[]{1F, 1F, 1F},
                new long[]{512, 3, 32, 128},
                false
        );
        Assertions.assertNotEquals(properties, new OnnxRecognitionPredictorProperties("model", inputProperties,
                new CrnnPostProcessor(Vocabulary.LEGACY_FRENCH)));
        Assertions.assertNotEquals(new OnnxRecognitionPredictorProperties("model", inputProperties,
                new CrnnPostProcessor(Vocabulary.FRENCH)), new OnnxRecognitionPredictorProperties("model",
                inputProperties, new CrnnPostProcessor(Vocabulary.ENGLISH)));
    }
}
