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

import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.OnnxOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxDoImageOcrRotatedTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxDoImageOcrRotatedTest";
    private static final String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MOBILENETV3 = "./src/test/resources/com/itextpdf/pdfocr/models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE.close();
    }

    @Test
    public void rotated270DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "270_degrees_rotated.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("image\ndegrees\n270\nrotated\n", textFromImage);
    }

    @Test
    public void rotated180DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "180_degrees_rotated.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("180\ndegrees\nrotated\nimage\n", textFromImage);
    }

    @Test
    public void rotated90DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "90_degrees_rotated.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("90\nrotated\ndegrees\nimage\n", textFromImage);
    }

    @Test
    public void rotatedTextBasicDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "rotatedTextBasic.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("TEST\nThis\ntext\nIS\nTxT\nsideways\nDiagonal\n", textFromImage);
    }

    @Test
    public void rotatedCapsLCDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "rotatedCapsLC.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("TEsTinG\nmix\nanD\nlowerCaSE\nCapITALS\n", textFromImage);
    }

    @Test
    public void rotatedColorsMixDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "rotatedColorsMix.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("ReD\nCOIORS\ntEXT\nMixed\nTEXT\nColored\n", textFromImage);
    }


    @Test
    public void rotatedColorsMix2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "rotatedColorsMix2.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("not\nhave\na\nproblem.\n&%!Housten\nwe\nshould\nydpAl,-68/9SPEZL\nwork?\nthis\ndoes\n", textFromImage);
    }

    @Test
    public void rotatedBy90DegreesTest() {
        String src = TEST_IMAGE_DIRECTORY + "rotatedBy90Degrees.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("TEXT\n180\n270\nTEXT\n-\n90\nTEXT\nTEXT\nO\n", textFromImage);
    }
}
