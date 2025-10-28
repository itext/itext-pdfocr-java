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
import com.itextpdf.pdfocr.onnxtr.recognition.Vocabulary;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;

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

    // 2 recognition models to be used in these tests
    // We use MULTILANG for the languages it supports
    private static final String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MULTILANG = TEST_DIRECTORY + "models/onnxtr-parseq-multilingual-v1.onnx";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @Test
    public void russianDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "russian.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        // Let's use multilang here though it doesn't support cyrillic
        IRecognitionPredictor recognitionPredictor =
                OnnxRecognitionPredictor.parSeq(MULTILANG, Vocabulary.LATIN_EXTENDED, 0);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("Heẞpocerw\nV\nWX\nBrim9me\nha\nXM3HL\n4CJTObeka\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void arabic1DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "arabic_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("13\n-\nA\n6\nSta:as)\n9\n4tj\n-\nlive,\nlaugh,\nlove\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void arabic2DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "arabic_02.png";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("Aysall\n&alll\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void bengaliDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "bengali_01.jpeg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("3(5\nT(3T\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void chineseDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "chinese_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("I\nK/i\n4\n\n-\nnI\nhao\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void engBmpDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.parSeq(MULTILANG);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("This\n1S\na\ntest\nmessage\n-./:\nfor\nOCR\nScanner\nTest\nBMPTest\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void frenchDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "french_01.png";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.parSeq(MULTILANG);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("RESTEZ\nCALME\nET\nPARLEZ\nEN\nFRANÇAIS\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void georgianDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "georgian_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("03960000\nL\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void germanDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "german_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.parSeq(MULTILANG);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("Das\nGeheimnis\ndes\nKònnens\nliegt\nim\nWollen.\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void greekDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "greek_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("0)\nP\n-\nV\n-\nE\nO\nN\n-\nM\nC\nA\nC)\nI\nI\nA\n$\n/\n7156W5\n$\nxabouxns\n2\n2\n7\nCTOS02u275\n2\n2\n/\nEXX2MG109\n$\ndycGuxns.\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void hindi1DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "hindi_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("o\n-\nG\ntT\ndes\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void hindi2DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "hindi_02.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage =OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("dloich\nSlaiai\nHindi\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void invoiceThaiDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "invoice_front_thai.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("QUANTITY\nDESCRIPTION\nUNIT\nPRICE\nTOTAL\n10\nLasers\n$3000\n$30000\n2\nBand-Aids\n$1\n$2\n5\naufnasi?\n$99999\n$499995\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void japaneseDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "japanese_01.png";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("B\n*\naa\n-\n-\na\nK\n*\n-\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void multiLangDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "multilang.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.parSeq(MULTILANG);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("The\n(quick)\n[brown]\n{fox}\njumps!\nOver\nthe\n$43,456.78\n<lazy>\n" +
                "#90\ndog\n&\nduck/goose,\nas\n12.5%\nof\nE-mai\nfrom\naspammer\n@website.com\nis\nspam.\nDer\n" +
                "schnelle\n\"J\nbraune\nFuchs\nspringt\nüber\nden\nfaulen\nHund.\nLe\nrenard\nbrun\n<rapide>\nsaute\n" +
                "par-dessus\nle\nchien\noaresseux.\nLa\nvolpe\nmarrone\nrapida\nsalta\nsopra\nil\ncane\npigro.\nEI\n" +
                "zorro\nmarron\nrapido\nsalta\nsobre\nel\nperro\nperezoso.\n%&'(\n4\nraposa\nmarrom\nrapida\nsalta\n" +
                "sobre\nO\ncao\npreguicoso.\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void spanishDoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "spanish_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.parSeq(MULTILANG);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals("Aquí\nhablamos\nespañol\n", textFromImage);

        ocrEngine.close();
    }

    @Test
    public void thai1DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "thai_01.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);
        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertEquals(
                "3581981\n1\n19n8\nA\nA\nI\na\n&\n\n19008791914497907597\n15790707047005\n19n8\n",
                textFromImage, textFromImage);

        ocrEngine.close();
    }

    @Test
    public void thai2DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "thai_02.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertTrue(textFromImage.contains("\nGNuwInygEMEMAnUONEDNMENAVouDaruRE\n"));
        Assertions.assertTrue(textFromImage.contains("\nWsruilunaMASIwEyuEAL\n"));
        Assertions.assertTrue(textFromImage.contains("\nMielwynanaur\n"));
        Assertions.assertTrue(textFromImage.contains("\nlnilounnenourwdryeuREnAOnaNADuiluwdrysnrailununeinnl\n"));

        ocrEngine.close();
    }

    @Test
    public void thai3DoImageOcrTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "thai_03.jpg";
        File imageFile = new File(src);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);

        String textFromImage = OnnxTestUtils.getTextFromImage(imageFile, ocrEngine);
        Assertions.assertTrue(textFromImage.contains("\ninneflsuauoniyadusunnaui\n"));
        Assertions.assertTrue(textFromImage.contains("\nae50wasDlgouwyWWMLTSUNR\n"));
        Assertions.assertTrue(textFromImage.contains("\n12051951A1Slnasu9as\n"));
        Assertions.assertTrue(textFromImage.contains("\nMosus\n"));

        ocrEngine.close();
    }
}
