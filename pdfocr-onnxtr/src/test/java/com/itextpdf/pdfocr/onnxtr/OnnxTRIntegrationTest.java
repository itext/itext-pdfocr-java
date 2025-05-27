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
import com.itextpdf.io.util.UrlUtil;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.OnnxOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Tag("IntegrationTest")
public class OnnxTRIntegrationTest extends ExtendedITextTest {
    public static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    public static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {
        String src = TEST_DIRECTORY + "images/example_04.png";
        String dest = TARGET_DIRECTORY + "basicTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_basicTest.pdf";
        System.out.println("Out pdf: " + UrlUtil.getNormalizedFileUriString(dest));

        final String fast = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
        final String crnnVgg16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
        final String mobileNetV3 = TEST_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(fast);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(mobileNetV3);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(crnnVgg16);

        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(ocrEngine);
        try (PdfWriter writer = new PdfWriter(dest)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(src)), writer).close();
        }
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @Test
    public void createTxtFileNullEventHelperTest() throws IOException {
        String src = TEST_DIRECTORY + "images/numbers_01.jpg";
        File imgFile = new File(src);
        String src1 = TEST_DIRECTORY + "images/270_degrees_rotated.jpg";
        File imgFile1 = new File(src1);
        String src2 = TEST_DIRECTORY + "images/example_04.png";
        File imgFile2 = new File(src2);
        final String fast = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
        final String crnnVgg16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
        final String mobileNetV3 = TEST_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(fast);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(mobileNetV3);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(crnnVgg16);

        OnnxTrOcrEngine ocrEngine = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);
        ocrEngine.createTxtFile(Arrays.asList(imgFile, imgFile1, imgFile2), FileUtil.createTempFile("test", ".txt"),
                new OcrProcessContext(null));
    }
}
