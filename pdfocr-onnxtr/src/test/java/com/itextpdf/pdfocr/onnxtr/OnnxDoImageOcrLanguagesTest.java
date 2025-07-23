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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxDoImageOcrLanguagesTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxDoImageOcrLanguagesTest";
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
    public void arabic1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "arabic_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("13\n-\n4\n6\nSta:as)\n9\n4at\n-\nlive,\nlaugh,\nlove\n", textFromImage);
    }

    @Test
    public void arabic2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "arabic_02.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Aysall\n&alll\n", textFromImage);
    }

    @Test
    public void bengaliDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "bengali_01.jpeg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("3(5T\n*T(3T\n", textFromImage);
    }

    @Test
    public void chineseDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "chinese_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("I\nK/i\n4\n\n-\nnI\nhao\n", textFromImage);
    }

    @Test
    public void engBmpDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("This\n1S\na\ntest\nmessage\n-\nfor\nOCR\nScanner\nTest\nBMPTest\n", textFromImage);
    }

    @Test
    public void frenchDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "french_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("RESTEZ\nCALME\nET\nPARLEZ\nEN\nFRANÇAIS\n", textFromImage);
    }

    @Test
    public void georgianDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "georgian_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("03960000\nL\n", textFromImage);
    }

    @Test
    public void germanDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "german_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Das\nGeheimnis\ndes\nKonnens\nliegt\n1m\nWollen.\n", textFromImage);
    }

    @Test
    public void greekDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "greek_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals(")\nP\n-\ny\n-\nE\nC\nN\nC\nM\n-\nA\nC\nI\nI\nA\n$\n/\n7156W5\n$\nxaboluxns\n2\n2\n14\nCTOS02u27\n2\n9\n-\nEXX2MG10\n$\ndycGuxns.\n", textFromImage);
    }

    @Test
    public void hindi1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "hindi_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("o\n-\nG\ntT\ndeass\n", textFromImage);
    }

    @Test
    public void hindi2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "hindi_02.jpg";
        File imageFile = new File(src);

        String textFromImage =OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("dloich\nSloial\nHindi\n", textFromImage);
    }

    @Test
    public void invoiceThaiDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "invoice_front_thai.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("QUANTITY\nDESCRIPTION\nUNIT\nPRICE\nTOTAL\n10\nLasers\n$3000\n$30000\n2\nBand-Aids\n$1\n$2\n5\naufnasi?\n$99999\n$499995\n", textFromImage);
    }

    @Test
    public void japaneseDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "japanese_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("B\n*\n&\n-\n-\nE\nD\nX\n*\n-\n", textFromImage);
    }

    @Test
    public void multiLangDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "multilang.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("The\n(quick)\n[brown]\nffox)\njumps!\nOver\nthe\n$43,456.78\n<lazy>\n" +
                "#90\ndog\n&\nduck/goose,\nas\n12.5%\nof\nE-mail\nfrom\naspammer\n@website.com\nis\nspam.\nDer\n" +
                "schnelle\n33\nbraune\nFuchs\nspringt\nuber\nden\nfaulen\nHund.\nLe\nrenard\nbrun\n<rapiden\nsaute\n" +
                "par-dessus\nle\nchien\nparesseux.\nLa\nvolpe\nmarrone\nrapida\nsalta\nsopra\nil\ncane\npigro.\nEl\n" +
                "zorro\nmarron\nrapido\nsalta\nsobre\nel\nperro\nperezoso.\n-\nA\nraposa\nmarrom\nrapida\nsalta\n" +
                "sobre\nO\ncao\npreguicoso.\n", textFromImage);
    }

    @Test
    public void spanishDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "spanish_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Aqui\nhablamos\nespafiol\n", textFromImage);
    }

    @Test
    public void thai1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals(
                "3581991\n1\n19n8\nA\nA\nI\na\n&\n1\n19008791914497907597\n15790707047005\n19n8\n", textFromImage, textFromImage);
    }

    @Test
    public void thai2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_02.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertTrue(textFromImage.contains("\nGNwwInygEnAnUONEDNMENEnMEVouDoruRE\n"));
        Assertions.assertTrue(textFromImage.contains("\nWsruilunaMASIwOyuEAL\n"));
        Assertions.assertTrue(textFromImage.contains("\nwialwwnnaurw)\n"));
        Assertions.assertTrue(textFromImage.contains("\nLmniloumwnounguwyuEREngenananuidwwéryenshuluwenennl\n"));
    }

    @Test
    public void thai3DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_03.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertTrue(textFromImage.contains("\ninneflsuauoniyadusunnaui\n"));
        Assertions.assertTrue(textFromImage.contains("\nae50wasDlgouwyWWMLTSUNR\n"));
        Assertions.assertTrue(textFromImage.contains("\n12051951A1Slnasu9as\n"));
        Assertions.assertTrue(textFromImage.contains("\nMosus\n"));
    }
}
