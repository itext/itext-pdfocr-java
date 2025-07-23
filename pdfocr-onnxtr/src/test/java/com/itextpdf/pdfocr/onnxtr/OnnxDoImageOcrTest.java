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

import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

@Tag("IntegrationTest")
public class OnnxDoImageOcrTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxDoImageOcrTest";
    private static final String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE.close();
    }

    @Test
    public void basicDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_04.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Does\nthis\nOCR\nthing\nreally\nwork?\nHi\nHow\nabout\na\nbigger\nfont?\n" +
                "123456789\n13\nWhat\nabowt\ntiy\nfont?\n", textFromImage);
    }

    @Test
    public void numbersJPEDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.jpe";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    public void bogusTextDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "bogusText.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Bladeblabla\n", textFromImage);
    }

    @Test
    public void halftoneDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "halftone.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Silliness\nEnablers\nINVOICE\nYou\ndream\nit\nwe\nenable\nit\nMiddle\nof\n" +
                "Nowhere\nPhone\n+32\n9\n292\n22\n22\nINVOICE\n#100\nFax\n+32\n9\n270\n00\n00\nDATE:\n6/30/2020\n" +
                "TO:\nSHIP\nTO:\nAndré\nLe\nemos\nAndré\nLemos\nTycoon\nCorp.\nTycoor\nCorp\nWonderfulStreet\n" +
                "Wonderfu\nStreet\nLala\nLand\nLala\nLand\n+351\n911\n111\n111\n+351\n911\n111\n111\nC\nAMENTS\n" +
                "OR\nSPFCIAI\nINSTRUCTIONS\nITEMS\nMUST\nBE\nDELIVERED\nFULLY\nASSEMBLED\nS/\nES\nRSC\nON\nP.O\n" +
                "NUMBER\nREQUISITIONER\nSHIPPED\nVIA\nF.O.B\nPOINT\nTERMS\n3Vi\n#7394009320\nV\nVebsite\nform\n" +
                "Al\nR\nDelivery\nDue\non\nreceipt\n\nQUANTITY\nDESCRIPTION\nUNIT\nPRICE\nTOTAL\n10\nLasers\n$3000\n" +
                "$30000\n2\nBand-Aids\n$1\n$2\nSharks\n$99999\n$499995\n", textFromImage);
    }

    @Test
    public void noisyDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "noisy_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Noisy\nimage\nto\ntest\nTesseract\nOCR\n", textFromImage);
    }

    @Test
    public void numbersDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "nümbérs.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    @Disabled("This test is failing on java 8 with ImageIO exception. In newer versions it works that is why we don't want to use Leptonica or any other 3rd-party to read such images.")
    public void numbers2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "numbers_02.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("-\n-\n&\n56\n0\n01\n12345\n", textFromImage);
    }

    @Test
    public void pantoneBlueDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "pantone_blue.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("", textFromImage);
    }

    @Test
    public void scannedDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "scanned_spa_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("\n1Y\nSI\nENSAYARA\nCOMO\nACTUAR?\nTanto\npeor,\nlo\nmejor\nes\ndescansar\n" +
                "y\nno\npensar\nla\nfiesta,\nsi\nse\npuede.\nNo\nhay\nnada\nmas\ndesalentador\nver\nen\nlas\n" +
                "fiestas\na\njovenes\ncon\ncara\nde\nlastima\ny\niluslonadas\ny\nque\nse\nhan\npasado\ntodo\nel\n" +
                "dia\ntratando\nhallar\nlo\nmejor\ny\nla\nmas\natractiva\nmanera\nde\npres\ntarse\nen\npublico.\n" +
                "Hay\nque\nactuar\ncon\ncalma\ny\nno\ncansaremos\nde\nrepetirlo,\nLo\nmas\nimportante\nes\nsaber\n" +
                "que\nse\nva\na\nponer\ny\ntener\ntodo\na\nmano,\nSi\nintenta\nprobar\nun\nnuevo\nlapiz\nlabial\n" +
                "para\nla\na\nsion,\nasegurese\nque\narmonice\ncon\nel\nvestido\nque\nlle\nrà.\nTambién\nel\n" +
                "maquillaje\nde\nlos\nojos\ndebe\narmonil\ncon\nel\nconjunto,\n", textFromImage);
    }

    @Test
    public void weirdWordsDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "weirdwords.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("he23llo\nqwetyrtyqpwe-rty\n", textFromImage);
    }

    @Test
    public void corruptedBmpDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_05_corrupted.bmp";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("This\nis\na\nTest\nThis\nis\na\nTest\nThis\nis\na\nTest\nThis\nis\na\n" +
                "Test\nThis\nis\na\nTest\nThis\nis\na\nTest\n", textFromImage);
    }

    @Test
    public void corruptedDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "corrupted.jpg";
        File imageFile = new File(src);
        Exception e = Assertions.assertThrows(PdfOcrInputException.class,
                () -> OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE));
        Assertions.assertEquals(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE, e.getMessage());
    }
}
