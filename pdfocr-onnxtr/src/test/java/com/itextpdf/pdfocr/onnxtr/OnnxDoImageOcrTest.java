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
        Assertions.assertEquals("font?\nabowt\nWhat\ntiy\n123456789\n13\nbigger\na\nabout\nfont?"
                + "\nHow\nHi\nreally\nthing\nwork?\nOCR\nthis\nDoes\n", textFromImage);
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
        Assertions.assertEquals("Sharks\n$499995\n$99999\nBand-Aids\n2\n$2\n$1\nLasers\n10\n"
                + "$30000\n$3000\nQUANTITY\nTOTAL\nPRICE\nUNIT\nDESCRIPTION\n\nV\nreceipt\non\nR\nVebsite\n#7394009320"
                + "\n3Vi\nDue\nDelivery\nAl\nform\nES\nS/\nNUMBER\nP.O\nON\nRSC\nTERMS\nPOINT\nF.O.B\nVIA\nSHIPPED\n"
                + "REQUISITIONER\nMUST\nITEMS\nASSEMBLED\nFULLY\nDELIVERED\nBE\nINSTRUCTIONS\nSPFCIAI\nOR\nAMENTS\nC"
                + "\n+351\n111\n111\n911\n+351\n111\n111\n911\nLand\nLala\nLand\nLala\nWonderfulStreet\nStreet\n"
                + "Wonderfu\nTycoor\nCorp.\nTycoon\nCorp\nLe\nemos\nLemos\nAndré\nAndré\nTO:\nTO:\nSHIP\n270\n9\n+32"
                + "\nFax\n00\n00\n6/30/2020\nDATE:\n22\n22\n292\n9\n+32\nPhone\n#100\nINVOICE\nNowhere\nof\nMiddle\nwe"
                + "\ndream\nYou\nit\nenable\nit\nSilliness\nEnablers\nINVOICE\n", textFromImage);
    }

    @Test
    public void noisyDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "noisy_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Tesseract\nOCR\nto\ntest\nimage\nNoisy\n", textFromImage);
    }

    @Test
    public void numbersDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "nümbérs.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("619121\n", textFromImage);
    }

    @Test
    @Disabled("This test is failing on java 8 with ImageIO exception. In newer versions it works that is why we don't" +
            " want to use Leptonica or any other 3rd-party to read such images.")
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
        Assertions.assertEquals("conjunto,\ncon\nel\narmonil\ndebe\nojos\nlos\nmaquillaje\nde\nel"
                + "\nTambién\nrà.\nque\nlle\nvestido\ncon\narmonice\nque\nel\nasegurese\nsion,\npara\na\nla\nlabial"
                + "\nnuevo\nlapiz\nun\nprobar\nintenta\nSi\nmano,\na\ntodo\ny\ntener\nponer\na\nva\nse\nque\nes\nsaber"
                + "\nimportante\nmas\nrepetirlo,\nLo\ncansaremos\nde\nno\ny\ncon\ncalma\nque\nactuar\nHay\npublico."
                + "\nen\ntarse\npres\nmanera\nde\natractiva\nmas\ny\nmejor\nla\nhallar\nlo\ntratando\ndia\nel\ntodo\n"
                + "pasado\nque\nse\ny\nhan\niluslonadas\ny\ncara\nlastima\ncon\nde\njovenes\na\nen\nfiestas\nver\nlas"
                + "\ndesalentador\nmas\nnada\nhay\npuede.\nse\nNo\nfiesta,\nsi\nla\npensar\nno\ny\nes\ndescansar\nmejor"
                + "\npeor,\nlo\nTanto\nACTUAR?\nCOMO\nENSAYARA\nSI\n1Y\n\n", textFromImage);
    }

    @Test
    public void weirdWordsDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "weirdwords.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("qwetyrtyqpwe-rty\nhe23llo\n", textFromImage);
    }

    @Test
    public void corruptedBmpDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_05_corrupted.bmp";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("a\nTest\nThis\nis\na\nTest\nThis\nis\na\nTest\nThis\nis\na\n"
                + "Test\nThis\nis\na\nTest\nis\nThis\na\nTest\nis\nThis\n", textFromImage);
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
