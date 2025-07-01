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
        Assertions.assertEquals("love\nlaugh,\nlive,\n-\n4\n6\n-\n9\n13\nSta:as)\n4at\n", textFromImage);
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
        Assertions.assertEquals("*T(3T\n3(5T\n", textFromImage);
    }

    @Test
    public void chineseDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "chinese_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("nI\nhao\n-\nK/i\n\n4\nI\n", textFromImage);
    }

    @Test
    public void engBmpDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("BMPTest\nTest\nOCR\nScanner\n-\nmessage\nfor\n1S\na\ntest\nThis\n", textFromImage);
    }

    @Test
    public void frenchDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "french_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("FRANÇAIS\nPARLEZ\nEN\nET\nCALME\nRESTEZ\n", textFromImage);
    }

    @Test
    public void georgianDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "georgian_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("L\n03960000\n", textFromImage);
    }

    @Test
    public void germanDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "german_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("1m\nWollen.\nliegt\nKonnens\ndes\nDas\nGeheimnis\n", textFromImage);
    }

    @Test
    public void greekDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "greek_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("EXX2MG10\n9\n-\n$\ndycGuxns.\n2\n7156W5\n14\nCTOS02u27"
                + "\nxaboluxns\n$\n$\n/\n2\n2\n-\nA\nC\nM\nC\nA\nI\nI\n-\n)\ny\nP\nC\n-\nE\nN\n", textFromImage);
    }

    @Test
    public void hindi1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "hindi_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("deass\n-\nG\ntT\no\n", textFromImage);
    }

    @Test
    public void hindi2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "hindi_02.jpg";
        File imageFile = new File(src);

        String textFromImage =OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("Hindi\ndloich\nSloial\n", textFromImage);
    }

    @Test
    public void invoiceThaiDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "invoice_front_thai.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("aufnasi?\n$499995\n$99999\n5\nBand-Aids\n2\n$2\n$1\nLasers\n"
                + "$30000\n$3000\n10\nTOTAL\nPRICE\nUNIT\nDESCRIPTION\nQUANTITY\n", textFromImage);
    }

    @Test
    public void japaneseDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "japanese_01.png";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("X\n-\n*\nD\n-\n-\nB\n*\n&\nE\n", textFromImage);
    }

    @Test
    public void multiLangDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "multilang.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("cao\npreguicoso.\nO\nsobre\nsalta\nraposa\nperezoso.\nmarrom\n-"
                + "\nrapida\nA\nperro\nmarron\nsobre\nsalta\nrapido\nel\nsopra\nzorro\ncane\npigro.\nsalta\nEl\nil\n"
                + "marrone\nparesseux.\nrapida\nvolpe\nLa\npar-dessus\n<rapiden\nsaute\nchien\nle\nrenard\nbrun\nLe\n"
                + "Hund.\nfaulen\nuber\nden\nspringt\nbraune\nschnelle\nFuchs\nDer\n33\nspam.\naspammer\n@website.com"
                + "\nfrom\nis\nas\nduck/goose,\nE-mail\n12.5%\nof\n&\ndog\n<lazy>\n$43,456.78\nOver\n#90\nthe\njumps!"
                + "\n[brown]\n(quick)\nffox)\nThe\n", textFromImage);
    }

    @Test
    public void spanishDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "spanish_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("espafiol\nhablamos\nAqui\n", textFromImage);
    }

    @Test
    public void thai1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals(
                "19008791914497907597\n15790707047005\n19n8\n3581991\n19n8\n1\n&\na\nI\n1\nA\nA\n", textFromImage, textFromImage);
    }

    @Test
    public void thai2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_02.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertTrue(textFromImage.contains("GNwwInygEnAnUONEDNMENEnMEVouDoruRE\nWsruilunaMASIwOyuEAL"
                + "\nwialwwnnaurw)\nLmniloumwnounguwyuEREngenananuidwwéryenshuluwenennl"));
    }

    @Test
    public void thai3DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_03.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertTrue(textFromImage.contains("bn\nOimnsAa\nAEnEyna\ninneflsuauoniyadusunnaui\n"
                + "ae50wasDlgouwyWWMLTSUNR\nlaiA\n12051951A1Slnasu9as\nMosus\nLYSSAW"));
    }
}
