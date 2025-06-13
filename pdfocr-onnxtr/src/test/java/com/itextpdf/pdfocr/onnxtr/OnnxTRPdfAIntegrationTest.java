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

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxTRPdfAIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxTRPdfAIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxTRPdfAIntegrationTest/";
    private static final String COLOR_PROFILE_PATH = "./src/test/resources/com/itextpdf/pdfocr/profiles/";
    private final static String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private final static String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);
    }

    @Test
    public void rgbPdfATest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        String dest = TARGET_DIRECTORY + "rgbpdfA.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rgbpdfA.pdf";

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTitle("");
        ocrPdfCreatorProperties.setTextLayerName("Text1");
        ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLUE);

        InputStream is = FileUtil.getInputStreamForFile(COLOR_PROFILE_PATH + "sRGB_CS_profile.icm");
        PdfOutputIntent outputIntent = new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);

        doOcrAndCreatePdf(src, dest, ocrPdfCreatorProperties, outputIntent);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceRgb.BLUE, extractionStrategy.getFillColor());
            Assertions.assertEquals("This\n1S test\na\nfor\nmessage\n-\nOCR\nScanner\nTest\nBMPTest",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void cmykPdfATest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "numbers_01.jpg";
        String dest = TARGET_DIRECTORY + "cmykpdfA.pdf";
        String cmp = TEST_DIRECTORY + "cmp_cmykpdfA.pdf";

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTitle("");
        ocrPdfCreatorProperties.setTextLayerName("Text1");
        ocrPdfCreatorProperties.setTextColor(DeviceCmyk.MAGENTA);

        InputStream is = FileUtil.getInputStreamForFile(COLOR_PROFILE_PATH + "CoatedFOGRA27.icc");
        PdfOutputIntent outputIntent = new PdfOutputIntent("Custom",
                "", "http://www.color.org",
                "Coated FOGRA27 (ISO 12647 - 2:2004)", is);

        doOcrAndCreatePdf(src, dest, ocrPdfCreatorProperties, outputIntent);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("619121",
                    extractionStrategy.getResultantText());
        }
    }

    private void doOcrAndCreatePdf(String imagePath, String destPdfPath,
            OcrPdfCreatorProperties ocrPdfCreatorProperties, PdfOutputIntent pdfOutputIntent) throws IOException {
        OcrPdfCreator ocrPdfCreator =
                ocrPdfCreatorProperties != null ? new OcrPdfCreator(OCR_ENGINE, ocrPdfCreatorProperties)
                        : new OcrPdfCreator(OCR_ENGINE);
        try (PdfWriter writer = new PdfWriter(destPdfPath)) {
            ocrPdfCreator.createPdfA(Collections.singletonList(new File(imagePath)), writer, pdfOutputIntent).close();
        }
    }
}
