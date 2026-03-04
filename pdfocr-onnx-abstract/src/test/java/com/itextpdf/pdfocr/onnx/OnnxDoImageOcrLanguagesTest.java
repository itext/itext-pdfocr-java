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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.util.OcrEngineType;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

@Tag("IntegrationTest")
public class OnnxDoImageOcrLanguagesTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxDoImageOcrLanguagesTest";
    private static final String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";

    // We use MULTILANG for the languages it supports.
    private static final String MULTILANG = TEST_DIRECTORY + "models/onnxtr-parseq-multilingual-v1.onnx";
    private static OnnxOcrEngine MULTILANG_ENGINE;
    private static OnnxOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.parSeq(MULTILANG);
        MULTILANG_ENGINE = new OnnxOcrEngine(detectionPredictor, recognitionPredictor);
        OCR_ENGINE = OcrEngineType.DOCTR.get();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        MULTILANG_ENGINE.close();
    }

    @Test
    public void russianDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "russian.jpg";
        File imageFile = new File(src);

        // Let's use multilang here though it doesn't support cyrillic
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, MULTILANG_ENGINE);
        Assertions.assertEquals("Heẞpocerw\nV\nWX\nBrim9me\nha\nXM3HL\n4CJTObeka\n", textFromImage);
    }

    @Test
    public void arabic1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "arabic_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("13\n-\n6\nSta:as)\n9\n4tj\n-\nlive,\nlaugh,\nlove\nA\n", textFromImage);
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
        Assertions.assertEquals("3(5\nT(3T\n", textFromImage);
    }

    @Test
    public void chineseDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "chinese_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("I\n4\n\n-\nnI\nK/i\nhao\n", textFromImage);
    }

    @Test
    public void engBmpDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        File imageFile = new File(src);
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, MULTILANG_ENGINE);
        Assertions.assertEquals("This\n1S\na\ntest\nmessage\nfor\n-./:\nOCR\nScanner\nTest\nBMPTest\n", textFromImage);
    }

    @Test
    public void frenchDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "french_01.png";
        File imageFile = new File(src);
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, MULTILANG_ENGINE);
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
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, MULTILANG_ENGINE);
        Assertions.assertEquals("Das\nGeheimnis\ndes\nKònnens\nliegt\nim\nWollen.\n", textFromImage);
    }

    @Test
    public void greekDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "greek_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertTrue(textFromImage.contains("dycGuxns"));
    }

    @Test
    public void hindi1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "hindi_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("-\nG\ntT\ndes\no\n", textFromImage);
    }

    @Test
    public void hindi2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "hindi_02.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals("dloich\nSlaiai\nHindi\n", textFromImage);
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
        Assertions.assertEquals("B\n*\n-\na\naa\nK\n*\n-\n-\n", textFromImage);
    }

    @Test
    public void multiLangDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "multilang.jpg";
        File imageFile = new File(src);
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, MULTILANG_ENGINE);

        Assertions.assertEquals("The\n(quick)\n[brown]\n{fox}\njumps!\nOver\nthe\n$43,456.78\n<lazy>\n#90\ndog\n" +
                "&\nduck/goose,\nas\n12.5%\nof\nE-mai\nfrom\naspammer\n@website.com\nis\nspam.\nDer\nschnelle\n" +
                "\"J\nbraune\nFuchs\nspringt\nüber\nden\nfaulen\nHund.\nLe\nrenard\nbrun\n<rapide>\nsaute\npar-" +
                "dessus\nle\nchien\noaresseux.\nLa\nvolpe\nmarrone\nrapida\nsalta\nsopra\nil\ncane\npigro.\nEI\n" +
                "zorro\nmarron\nrapido\nsalta\nsobre\nel\nperro\nperezoso.\n%&'(\nraposa\nmarrom\nrapida\nsalta\n" +
                "sobre\nO\ncao\npreguicoso.\n4\n", textFromImage);
    }

    @Test
    public void spanishDoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "spanish_01.jpg";
        File imageFile = new File(src);
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, MULTILANG_ENGINE);

        Assertions.assertEquals("Aquí\nhablamos\nespañol\n", textFromImage);
    }

    @Test
    public void thai1DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_01.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertEquals(
                "3581981\n1\n19n8\nA\nA\nI\na\n&\n\n19008791914497907597\n15790707047005\n19n8\n",
                textFromImage, textFromImage);
    }

    @Test
    public void thai2DoImageOcrTest() {
        String src = TEST_IMAGE_DIRECTORY + "thai_02.jpg";
        File imageFile = new File(src);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, OCR_ENGINE);
        Assertions.assertTrue(textFromImage.contains("\nGNuwInygEMEMAnUONEDNMENAVouDaruRE\n"));
        Assertions.assertTrue(textFromImage.contains("\nWsruilunaMASIwEyuEAL\n"));
        Assertions.assertTrue(textFromImage.contains("\nMielwynanaur\n"));
        Assertions.assertTrue(textFromImage.contains("\nlnilounnenourwdryeuREnAOnaNADuiluwdrysnrailununeinnl\n"));
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
