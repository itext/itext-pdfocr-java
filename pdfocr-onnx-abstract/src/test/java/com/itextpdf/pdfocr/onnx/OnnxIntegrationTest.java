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

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnx.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.text.TextPositioning;
import com.itextpdf.pdfocr.onnx.util.OcrEngineType;
import com.itextpdf.pdfocr.util.PdfOcrTextBuilder;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag("IntegrationTest")
public class OnnxIntegrationTest extends ExtendedITextTest {
    private static final String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxIntegrationTest/";
    private static OnnxOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        OCR_ENGINE = OcrEngineType.DOCTR.get();
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_04.png";
        String dest = TARGET_DIRECTORY + "basicTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_basicTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void jfifTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_02.JFIF";
        String dest = TARGET_DIRECTORY + "jfifTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_jfifTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("Test\nmessage for\nOCR Scanner\nIhis a test\n1S\n-",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void tiff10MBTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_03_10MB.tiff";
        String dest = TARGET_DIRECTORY + "tiff10MBTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_tiff10MBTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("Tagged Image File Format", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void jpeTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.jpe";
        String dest = TARGET_DIRECTORY + "jpeTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_jpeTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("619121", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void nnnTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.nnn";
        String dest = TARGET_DIRECTORY + "nnnTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_nnnTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("619121", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void scannedTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "scanned_spa_01.png";
        String dest = TARGET_DIRECTORY + "scannedTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("la\n" +
                    "se No\n" +
                    "-\n" +
                    "AY SI ENSAYARA COMO ACTUAR?\n" +
                    "Tanto peor, lo mejor es descansar y no\n" +
                    "fiesta, si pensar\n" +
                    "puede. hay nada mas desalentador\n" +
                    "ver en las fiestas a jovenes con cara de lastima y\n" +
                    "el\n" +
                    "iluslonadas y se pasado todo\n" +
                    "que han dia tratando\n" +
                    "hallar lo mejor y la mas atractiva manera de pres\n" +
                    "tarse en publico. Hay que actuar con calma y no\n" +
                    "cansaremos de repetirlo, Lo mas importante es saber\n" +
                    "que se va a poner y tener todo a mano,\n" +
                    "Si intenta probar un nuevo lapiz labial para la a\n" +
                    "sion, asegurese que armonice con vestido lle\n" +
                    "-\n" +
                    "También el el\n" +
                    "rà. que\n" +
                    "maquillaje de los ojos debe armoni\n" +
                    "con el conjunto.", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void halftoneTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "halftone.jpg";
        String dest = TARGET_DIRECTORY + "halftoneTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("Silliness Enablers INVOICE\nYou dream it we enable it\n" +
                            "Middle of Nowhere\nPhone +32 9 292 22 22 INVOICE #100\n" +
                            "Fax +32 9 270 00 00 DATE: 6/30/2020\nTO: SHIP TO:\nAndré André Lemos\n" +
                            "Le emos\nTycoon Corp. Tycoor Corp\nWonderfulStreet Wonderfu Street\n" +
                            "Lala Land Lala Land\n+351 911 111 111 +351 911 111 111\n" +
                            "C AMENTS OR SPFCIAI INSTRUCTIONS\nITEMS MUST BE DELIVERED FULLY ASSEMBLED\n" +
                            "ON P.O NUMBER REQUISITIONER SHIPPED VIA F.O.B POINT TERMS\nS/ ES RSC\n" +
                            "3Vi #7394009320 V Vebsite form Al R Delivery Due on receipt\n" +
                            "QUANTITY DESCRIPTION UNIT PRICE TOTAL\n10 Lasers $3000 $30000\n" +
                            "2 Band-Aids $1 $2\nSharks $99999 $499995"
                    , extractionStrategy.getResultantText());
        }
    }

    @Test
    public void arabicDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "arabic_01.jpg";
        String dest = TARGET_DIRECTORY + "arabicTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_arabicTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void bengaliDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "bengali_01.jpeg";
        String dest = TARGET_DIRECTORY + "bengaliTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_bengaliTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void chineseDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "chinese_01.jpg";
        String dest = TARGET_DIRECTORY + "chineseTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_chineseTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void frenchDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "french_01.png";
        String dest = TARGET_DIRECTORY + "frenchTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_frenchTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void georgianDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "georgian_01.jpg";
        String dest = TARGET_DIRECTORY + "georgianTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_georgianTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void germanDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "german_01.jpg";
        String dest = TARGET_DIRECTORY + "germanTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_germanTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void hindiDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "hindi_01.jpg";
        String dest = TARGET_DIRECTORY + "hindiTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_hindiTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void japaneseDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "japanese_01.png";
        String dest = TARGET_DIRECTORY + "japaneseTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_japaneseTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void spanishDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "spanish_01.jpg";
        String dest = TARGET_DIRECTORY + "spanishTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_spanishTest.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void bmpByWordsTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        String dest = TARGET_DIRECTORY + "bmpTestByWords.pdf";
        String cmp = TEST_DIRECTORY + "cmp_bmpTestByWords.pdf";

        OnnxDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        OnnxRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        try (OnnxOcrEngine onnxOcrEngine = new RotationAgnosticOnnxOcrEngine(detectionPredictor, null,
                recognitionPredictor, new OnnxEngineProperties().setTextPositioning(TextPositioning.BY_WORDS))) {
            OnnxTestUtils.doOcrAndCreatePdf(src, dest, onnxOcrEngine);
        }

        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("This\n1S test\na\nfor\nmessage\n-\nOCR\nScanner\nTest\nBMPTest",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void obliqueLinesTest() throws Exception {
        String src = TEST_IMAGE_DIRECTORY + "obliqueLines.png";
        String dest = TARGET_DIRECTORY + "obliqueLines.pdf";
        String cmp = TEST_DIRECTORY + "cmp_obliqueLines.pdf";

        OnnxDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        OnnxRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        try (OnnxOcrEngine onnxOcrEngine = new RotationAgnosticOnnxOcrEngine(detectionPredictor, null,
                recognitionPredictor, new OnnxEngineProperties().setTextPositioning(TextPositioning.BY_LINES))) {
            OnnxTestUtils.doOcrAndCreatePdf(src, dest, onnxOcrEngine);
        }

        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    /**
     * Implementation of the {@link OnnxOcrEngine} supporting only 0, 90, 180 and 270 degrees text rotation.
     */
    public static class RotationAgnosticOnnxOcrEngine extends OnnxOcrEngine {

        /**
         * Create a new OCR engine with the provided predictors.
         *
         * @param detectionPredictor text detector. For an input image it outputs a list of text boxes
         * @param orientationPredictor text orientation predictor. For an input image, which is a tight  crop of text,
         * it outputs its orientation in 90 degrees steps. Can be null, in that case all text
         * is assumed to be upright
         * @param recognitionPredictor text recognizer. For an input image, which is a tight crop of text, it outputs the
         * displayed string
         * @param properties set of properties
         */
        public RotationAgnosticOnnxOcrEngine(IDetectionPredictor detectionPredictor,
                                             IOrientationPredictor orientationPredictor,
                                             IRecognitionPredictor recognitionPredictor,
                                             OnnxEngineProperties properties) {
            super(detectionPredictor, orientationPredictor, recognitionPredictor, properties);
        }

        @Override
        public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
            return PdfOcrTextBuilder.correctRotationAngle(super.doImageOcr(input, ocrProcessContext));
        }
    }
}
