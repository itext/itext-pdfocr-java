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
package com.itextpdf.pdfocr.onnx.cpu;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.onnx.OnnxTrEngineProperties;
import com.itextpdf.pdfocr.onnx.OnnxTrOcrEngine;
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnx.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnx.orientation.OnnxOrientationPredictor;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.text.TextPositioning;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Tag("IntegrationTest")
public class IntegrationPdfOcrOnnxTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TEST_PDFS_DIRECTORY = TEST_DIRECTORY + "pdfs/";

    private static final String SOURCE_FOLDER = TEST_DIRECTORY + "IntegrationPdfOcrOnnxTest/";
    private static final String DESTINATION_FOLDER = "./target/test/resources/com/itextpdf/pdfocr/IntegrationPdfOcrOnnxTest/";

    private static final String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MOBILENETV3 = TEST_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";

    private static OnnxTrOcrEngine OCR_ENGINE_MAKE_PDF_SEARCHABLE;
    private static OnnxTrOcrEngine OCR_ENGINE_IMAGE_OCR;
    private static OnnxTrOcrEngine OCR_ENGINE_CREATE_PDF;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(DESTINATION_FOLDER);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        OCR_ENGINE_MAKE_PDF_SEARCHABLE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor,
                recognitionPredictor);

        OCR_ENGINE_IMAGE_OCR = new OnnxTrOcrEngine(detectionPredictor, null, recognitionPredictor,
                new OnnxTrEngineProperties().setTextPositioning(TextPositioning.BY_WORDS));

        OCR_ENGINE_CREATE_PDF = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE_MAKE_PDF_SEARCHABLE.close();
        OCR_ENGINE_IMAGE_OCR.close();
        OCR_ENGINE_CREATE_PDF.close();
    }

    @Test
    public void makePdfSearchableBasicTest() throws IOException, InterruptedException {
        makePdfSearchable("numbers");
    }

    @Test
    public void makePdfSearchablePageRotatedTest() throws IOException, InterruptedException {
        makePdfSearchable("pageRotation");
    }

    @Test
    public void imageOcrBasicTest() {
        String src = TEST_IMAGE_DIRECTORY + "example_01.BMP";
        File imageFile = new File(src);

        String textFromImage = getTextFromImage(imageFile, OCR_ENGINE_IMAGE_OCR);
        Assertions.assertEquals("Ihis\n1S\na\ntest\nmessage\n-\nfor\nOCR\nScanner\nTest\n", textFromImage);
    }

    @Test
    public void createPdfBasicTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "example_04.png";
        String dest = DESTINATION_FOLDER + "basicTest.pdf";
        String cmp = SOURCE_FOLDER + "cmp_basicTest.pdf";

        doOcrAndCreatePdf(src, dest);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, DESTINATION_FOLDER, "diff_"));
    }

    private static void doOcrAndCreatePdf(String imagePath, String destPdfPath) throws IOException {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE_CREATE_PDF);
        try (PdfWriter writer = new PdfWriter(destPdfPath)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(imagePath)), writer).close();
        }
    }

    private static String getTextFromImage(File imageFile, IOcrEngine ocrEngine) {
        Map<Integer, List<TextInfo>> integerListMap = ocrEngine.doImageOcr(imageFile);
        return getStringFromListMap(integerListMap);
    }

    private static String getStringFromListMap(Map<Integer, List<TextInfo>> listMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<Integer, List<TextInfo>> entry : listMap.entrySet()) {
            for (TextInfo textInfo : entry.getValue()) {
                if (textInfo.getText() != null) {
                    stringBuilder.append(textInfo.getText()).append('\n');
                }
            }
        }
        return stringBuilder.toString();
    }

    private void makePdfSearchable(String fileName) throws IOException, InterruptedException {
        String srcPath = TEST_PDFS_DIRECTORY + fileName + ".pdf";
        String outPath = DESTINATION_FOLDER + fileName + ".pdf";
        String cmpPath = SOURCE_FOLDER + "cmp_" + fileName + ".pdf";

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE_MAKE_PDF_SEARCHABLE,
                new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.MAGENTA));
        ocrPdfCreator.makePdfSearchable(new File(srcPath), new File(outPath));
        Assertions.assertNull(new CompareTool().compareByContent(outPath, cmpPath, DESTINATION_FOLDER, "diff_"));
    }
}
