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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxMultiFilesIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxMultiFilesIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxMultiFilesIntegrationTest/";
    private static final String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
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
    public void multiFilesTest() throws IOException, InterruptedException {
        List<File> files = Arrays.<File>asList(
                new File(TEST_IMAGE_DIRECTORY + "german_01.jpg"),
                new File(TEST_IMAGE_DIRECTORY + "noisy_01.png"),
                new File(TEST_IMAGE_DIRECTORY + "nümbérs.jpg"),
                new File(TEST_IMAGE_DIRECTORY + "example_04.png")
        );

        String dest = TARGET_DIRECTORY + "multiFiles.pdf";
        String cmp = TEST_DIRECTORY + "cmp_multiFiles.pdf";


        OcrPdfCreatorProperties properties = creatorProperties("Text1", "Image1", DeviceCmyk.CYAN);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE, properties);
        try (PdfWriter writer = new PdfWriter(dest)) {
            ocrPdfCreator.createPdf(files, writer).close();
        }

        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    private OcrPdfCreatorProperties creatorProperties(String textLayerName, String imageLayerName, Color color) {
        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName(textLayerName);
        ocrPdfCreatorProperties.setTextColor(color);
        ocrPdfCreatorProperties.setImageLayerName(imageLayerName);
        return ocrPdfCreatorProperties;
    }
}
