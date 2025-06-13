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
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.OnnxOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxTRRotationIntegrationTest extends ExtendedITextTest {
    private final static String FAST = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private final static String CRNNVGG16 = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxTRRotationIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxTRRotationIntegrationTest/";
    private final static String MOBILENETV3 = "./src/test/resources/com/itextpdf/pdfocr/models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);
    }
    @Test
    public void rotated90Test() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "90_degrees_rotated.jpg";
        String dest = TARGET_DIRECTORY + "rotated90Test.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotated90Test.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("90\ndegrees\nrotated\nimage", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotated180Test() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "180_degrees_rotated.jpg";
        String dest = TARGET_DIRECTORY + "rotated180Test.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotated180Test.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("180\ndegrees\nrotated\nimage", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotated270Test() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "270_degrees_rotated.jpg";
        String dest = TARGET_DIRECTORY + "rotated270Test.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotated270Test.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("270\ndegrees\nrotated\nimage", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotatedCapsLCTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedCapsLC.png";
        String dest = TARGET_DIRECTORY + "rotatedCapsLCTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotatedCapsLCTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("anD\nCapITALS\nlowerCaSE\nmix\nTEsTinG", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotatedColorsMixTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedColorsMix.png";
        String dest = TARGET_DIRECTORY + "rotatedColorsMixTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotatedColorsMixTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("TEXT\nColored\nMixed\nCOIORS\ntEXT\nReD", extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotatedColorsMix2Test() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedColorsMix2.png";
        String dest = TARGET_DIRECTORY + "rotatedColorsMix2Test.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotatedColorsMix2Test.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("a\ndoes\nthis\nwork?\nshould\nwe\n&%!Housten\nproblem.\nhave\nnot\nydpAl,-68/9SPEZL",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotatedBy90DegreesTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedBy90Degrees.png";
        String dest = TARGET_DIRECTORY + "rotatedBy90DegreesTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotatedBy90DegreesTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("270\nTEXT\nTEXT O\n90\nTEXT\nTEXT 180\n-",
                    extractionStrategy.getResultantText());
        }
    }

    @Test
    public void rotatedTextBasicTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedTextBasic.png";
        String dest = TARGET_DIRECTORY + "rotatedTextBasicTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_rotatedTextBasicTest.pdf";

        doOcrAndCreatePdf(src, dest, creatorProperties("Text1", DeviceCmyk.MAGENTA));
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            Assertions.assertEquals("Diagonal\nTxT\nTEST\nThis text IS\nsideways", extractionStrategy.getResultantText());
        }
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
