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
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.OnnxOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

@Tag("IntegrationTest")
public class OnnxTRCmykIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxTRCmykIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxTRCmykIntegrationTest/";
    private static final String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MOBILENETV3 = "./src/test/resources/com/itextpdf/pdfocr/models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor,
                recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE.close();
    }

    @Test
    public void rainbowInvertedCmykTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rainbow_inverted_cmyk.jpg";
        String dest = TARGET_DIRECTORY + "rainbowInvertedCmykTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rainbowInvertedCmykTest.txt";

        if (isFixedInJdk()) {
            doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
            try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
                ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
                Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
                Assertions.assertEquals(getCmpText(cmpTxt), extractionStrategy.getResultantText());
            }
        } else {
            Exception e = Assertions.assertThrows(Exception.class, () -> doOcrAndCreatePdf(src, dest, null));
            Assertions.assertEquals("Failed to read image.", e.getMessage());
        }
    }

    @Test
    public void rainbowAdobeCmykTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rainbow_adobe_cmyk.jpg";
        String dest = TARGET_DIRECTORY + "rainbowAdobeCmykTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rainbowAdobeCmykTest.txt";

        if (isFixedInJdk()) {
            doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
            try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
                ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
                Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
                double relativeDistance = (double) MathUtil.calculateLevenshteinDistance(getCmpText(cmpTxt),
                        extractionStrategy.getResultantText()) / getCmpText(cmpTxt).length();
                Assertions.assertTrue(relativeDistance < 0.05);
            }
        } else {
            Exception e = Assertions.assertThrows(Exception.class, () -> doOcrAndCreatePdf(src, dest, null));
            Assertions.assertEquals("Failed to read image.", e.getMessage());
        }
    }

    @Test
    public void rainbowCmykNoProfileTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rainbow_cmyk_inverted_no_profile.jpg";
        String dest = TARGET_DIRECTORY + "rainbowCmykNoProfileTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rainbowCmykNoProfileTest.txt";

        if (isFixedInJdk()) {
            doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
            try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
                ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
                Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
                Assertions.assertEquals(getCmpText(cmpTxt), extractionStrategy.getResultantText());
            }
        } else {
            Exception e = Assertions.assertThrows(Exception.class, () -> doOcrAndCreatePdf(src, dest, null));
            Assertions.assertEquals("Failed to read image.", e.getMessage());
        }
    }

    private static boolean isFixedInJdk() {
        //Fixed CMYK bug https://bugs.openjdk.org/browse/JDK-8274735 for openJDK:
        //jdk8 from 351 onwards, for jdk11 from 16 onwards and for jdk17 starting from 4.
        //Amazon corretto jdk started support CMYK for JPEG from 11 version.
        //Temurin 8 does not support CMYK for JPEG either.
        String versionStr = System.getProperty("java.version");
        String vendorStr = System.getProperty("java.vendor");
        boolean isFixed = false;
        int majorVer = getMajorVer(versionStr);
        String[] split = versionStr.split("[._-]");
        int minorVer = Integer.parseInt(split[split.length - 1]);

        switch (majorVer) {
            case 8:
                if ("Amazon.com Inc.".equals(vendorStr) || "Temurin".equals(vendorStr)) {
                    return false;
                }

                isFixed = minorVer >= 351;
                break;
            case 11:
                isFixed = minorVer >= 16;
                break;
            case 17:
                isFixed = minorVer >= 4;
                break;
            default:
                isFixed = true;
        }

        return isFixed;
    }

    private static int getMajorVer(String versionStr) {
        int majorVer = 0;
        String[] split = versionStr.split("\\.");
        if (versionStr.startsWith("1.")) {
            //jdk versions 1 - 8 have 1. as prefix
            majorVer = Integer.parseInt(split[1]);
        } else {
            majorVer = Integer.parseInt(split[0]);
        }
        return majorVer;
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

    private String getCmpText(String txtPath) throws IOException {
        int bytesCount = (int) new File(txtPath).length();
        char[] array = new char[bytesCount];
        try (InputStreamReader stream = new InputStreamReader(Files.newInputStream(Paths.get(txtPath)))) {
            stream.read(array, 0, bytesCount);
            return new String(array);
        }
    }

}
