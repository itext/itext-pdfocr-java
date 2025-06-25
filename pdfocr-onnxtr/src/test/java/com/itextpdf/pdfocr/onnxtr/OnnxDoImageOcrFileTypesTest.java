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
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxDoImageOcrFileTypesTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxDoImageOcrFileTypesTest";
    private final static String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private final static String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);
    }

    @Test
    public void numbersJPEDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.jpe";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    public void exampleBMPDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_01.BMP";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Test\nOCR\nScanner\n-\nmessage\nfor\n1S\na\ntest\nIhis\n", textFromImage);
    }

    @Test
    public void exampleJFIFDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_02.JFIF";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Test\nOCR\nScanner\n-\nmessage\nfor\n1S\na\ntest\nIhis\n", textFromImage);
    }

    @Test
    public void example10mvTIFFDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_03_10MB.tiff";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Image\nTagged\nFormat\nFile\n", textFromImage);
    }

    @Test
    public void multipageTiffDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "multipage.tiff";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("1\nPage\nExample\nTIFF\nMultipage\n" +
                "-\n2\nPage\nExample\nTIFF\nMultipage\n" +
                "Page\n3\nExample\nTIFF\nMultipage\n" +
                "4\nPage\nExample\nTIFF\nMultipage\n" +
                "Page5\nExample\nTIFF\nMultipage\n" +
                "Page\n6\nExample\nTIFF\nMultipage\n" +
                "/\nPage\nExample\nTIFF\nMultipage\n" +
                "8\nPage\nExample\nTIFF\nMultipage\n" +
                "Page\n9\nExample\nTIFF\nMultipage\n", textFromImage);
    }

    @Test
    public void numbersJpgDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    public void numbersNnnDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.nnn";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    public void numbersTifDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.tif";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    public void gifDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "weirdwords.gif";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("qwetyrtyqpwe-rty\nhe23llo\n", textFromImage);
    }
}
