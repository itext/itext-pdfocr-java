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

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Tag("IntegrationTest")
public class OnnxTRIntegrationTest extends ExtendedITextTest {
    private static final String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxTRIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxTRIntegrationTest/";
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
    public void basicTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_04.png";
        String dest = TARGET_DIRECTORY + "basicTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_basicTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void bmpTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        String dest = TARGET_DIRECTORY + "bmpTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_bmpTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("This a test\n1S\nmessage for\n-\nOCR Scanner\nTest\nBMPTest",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void bmpByWordsTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        String dest = TARGET_DIRECTORY + "bmpTestByWords.pdf";
        String cmp = TEST_DIRECTORY + "cmp_bmpTestByWords.pdf";

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new OnnxTrOcrEngine(detectionPredictor, null,
                recognitionPredictor, new OnnxTrEngineProperties().setTextPositioning(TextPositioning.BY_WORDS)),
                creatorProperties("Text1", DeviceCmyk.MAGENTA));
        try (PdfWriter writer = new PdfWriter(dest)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(src)), writer).close();
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
    public void jfifTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_02.JFIF";
        String dest = TARGET_DIRECTORY + "jfifTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_jfifTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("Ihis a test\n1S\nmessage for\n-\nOCR Scanner\nTest",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void tiff10MBTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_03_10MB.tiff";
        String dest = TARGET_DIRECTORY + "tiff10MBTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_tiff10MBTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
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

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
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

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("619121", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void gifTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "weirdwords.gif";
        String dest = TARGET_DIRECTORY + "gifTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_gifTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("qwetyrtyqpwe-rty\nhe23llo", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void multipageTiffTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "multipage.tiff";
        String dest = TARGET_DIRECTORY + "multipageTiffTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_multipageTiffTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("Multipage\nTIFF\nExample\nPage\n1", extractionStrategy.getResultantText());

            extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 7, "Text1");
            // Model glitch
            Assertions.assertEquals("Multipage\nTIFF\nExample\nPage\n/", extractionStrategy.getResultantText());

            extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 9, "Text1");
            Assertions.assertEquals("Multipage\nTIFF\nExample\nPage 9", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void scannedTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "scanned_spa_01.png";
        String dest = TARGET_DIRECTORY + "scannedTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_scannedTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("1Y SI ENSAYARA COMO ACTUAR?\n" +
                    "Tanto peor, lo mejor es descansar y no pensar\n" +
                    "la fiesta, si se puede. No hay nada mas desalentador\n" +
                    "ver en las fiestas a jovenes con cara de lastima y\n" +
                    "iluslonadas y que se han pasado todo el dia tratando\n" +
                    "hallar lo mejor y la mas atractiva manera de pres\n" +
                    "tarse en publico. Hay que actuar con calma y no\n" +
                    "cansaremos de repetirlo, Lo mas importante es saber\n" +
                    "que se va a poner y tener todo a mano,\n" +
                    "Si intenta probar un nuevo lapiz labial para la a\n" +
                    "sion, asegurese que armonice con el vestido que lle\n" +
                    "rà. También el maquillaje de los ojos debe armonil\n" +
                    "con el conjunto,", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void halftoneTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "halftone.jpg";
        String dest = TARGET_DIRECTORY + "halftoneTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_halftoneTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

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

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void bengaliDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "bengali_01.jpeg";
        String dest = TARGET_DIRECTORY + "bengaliTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_bengaliTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void chineseDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "chinese_01.jpg";
        String dest = TARGET_DIRECTORY + "chineseTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_chineseTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void frenchDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "french_01.png";
        String dest = TARGET_DIRECTORY + "frenchTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_frenchTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void georgianDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "georgian_01.jpg";
        String dest = TARGET_DIRECTORY + "georgianTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_georgianTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void germanDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "german_01.jpg";
        String dest = TARGET_DIRECTORY + "germanTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_germanTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void greekDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "greek_01.jpg";
        String dest = TARGET_DIRECTORY + "greekTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_greekTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void hindiDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "hindi_01.jpg";
        String dest = TARGET_DIRECTORY + "hindiTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_hindiTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void japaneseDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "japanese_01.png";
        String dest = TARGET_DIRECTORY + "japaneseTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_japaneseTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void spanishDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "spanish_01.jpg";
        String dest = TARGET_DIRECTORY + "spanishTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_spanishTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void thaiDocTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "thai_01.jpg";
        String dest = TARGET_DIRECTORY + "thaiTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_thaiTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    private OcrPdfCreatorProperties creatorProperties(String layerName, Color color) {
        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName(layerName);
        ocrPdfCreatorProperties.setTextColor(color);
        return ocrPdfCreatorProperties;
    }

    private void doOcrAndCreatePdf(String imagePath, String destPdfPath,
            OcrPdfCreatorProperties ocrPdfCreatorProperties) throws IOException {
        OcrPdfCreator ocrPdfCreator =
                ocrPdfCreatorProperties != null ? new OcrPdfCreator(OCR_ENGINE, ocrPdfCreatorProperties)
                        : new OcrPdfCreator(OCR_ENGINE);
        try (PdfWriter writer = new PdfWriter(destPdfPath)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(imagePath)), writer).close();
        }
    }

    private void doOcrAndCreatePdf(String imagePath, String destPdfPath) throws IOException {
        doOcrAndCreatePdf(imagePath, destPdfPath, null);
    }
}
