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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Tag("IntegrationTest")
public class OnnxTRIntegrationTest extends ExtendedITextTest {
    private final static String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private final static String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
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
            Assertions.assertEquals("Ihis\n1S test\na\nfor\nmessage\n-\nOCR\nScanner\nTest",
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
            Assertions.assertEquals("File\nFormat\nTagged Image", extractionStrategy.getResultantText());
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
            Assertions.assertEquals("he23llo\nqwetyrtyqpwe-rty", extractionStrategy.getResultantText());
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
            Assertions.assertEquals("Multipage\nTIFF\nExample\n1\nPage", extractionStrategy.getResultantText());

            extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 7, "Text1");
            // Model glitch
            Assertions.assertEquals("Multipage\nTIFF\nExample\n/\nPage", extractionStrategy.getResultantText());

            extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 9, "Text1");
            Assertions.assertEquals("Multipage\nTIFF\nExample\n9\nPage", extractionStrategy.getResultantText());
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
            Assertions.assertEquals("SI\n1Y ENSAYARA\nCOMO\nACTUAR?\nTanto\nlo\npeor,\nmejor es\ndescansar\n"
                    + "no\ny\npensar\nla\nsi\nse\nfiesta,\npuede. No\nnada\nhay\nmas\ndesalentador\nver las\nen\n"
                    + "fiestas\na\njovenes\ncon\ncara de\nlastima\ny\niluslonadas\nse han\ny que\npasado todo\nel dia\n"
                    + "tratando\nhallar lo\nla\nmejor\natractiva\nmas\ny\nmanera\nde\npres\ntarse en\npublico.\nactuar"
                    + "\nHay\nque\ncon calma\nno\ny\ncansaremos\nde\nrepetirlo, Lo\nmas\nimportante\nes saber\nque se\n"
                    + "va a\ntener\nponer\ny todo\na\nmano,\nSi\nintenta\nprobar\nun\nnuevo\nlapiz\nlabial\nla\npara a"
                    + "\nsion,\nasegurese\nque\narmonice\ncon\nel vestido\nlle\nque\nTambién\nrà.\nel\nmaquillaje\nde\n"
                    + "los\ndebe\nojos\narmonil\ncon el\nconjunto,", extractionStrategy.getResultantText());
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
            Assertions.assertEquals("INVOICE\nSilliness\nEnablers\nit\n"
                            + "You dream we enable\nit\nNowhere\nMiddle\nof\nINVOICE #100\n9\n22 22\nPhone +32 "
                            + "292\nDATE:\n"
                            + "6/30/2020\n9\n00 00\nFax +32 270\nTO: SHIP TO:\nAndré Lemos\nLe\nAndré emos\nTycoor Corp"
                            + "\nTycoon Corp.\nWonderfu Street\nWonderfulStreet\nLand\nLala Land\nLala\n111\n911\n"
                            + "+351 911 111 111 +351 111\nAMENTS SPFCIAI INSTRUCTIONS\nC\nOR\nITEMS FULLY\nDELIVERED "
                            + "ASSEMBLED"
                            + "\nBE\nMUST\nPOINT TERMS\nSHIPPED VIA F.O.B\nREQUISITIONER\nS/ ES ON\nRSC P.O "
                            + "NUMBER\nDue\nR\n"
                            + "V Al on\n3Vi Vebsite form receipt\n#7394009320 Delivery\nUNIT PRICE "
                            + "TOTAL\nDESCRIPTION\nQUANTITY"
                            + "\n$3000 $30000\n10\nLasers\n$1 $2\nBand-Aids\n2\n$499995\n$99999\nSharks"
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
