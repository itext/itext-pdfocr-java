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

import com.itextpdf.kernel.colors.DeviceCmyk;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OcrPdfTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OcrPdfTest/";
    private static final String TEST_PDFS_DIRECTORY = TEST_DIRECTORY + "../pdfs/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OcrPdfTest/";
    private static final String FAST = TEST_DIRECTORY + "../models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = TEST_DIRECTORY + "../models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MOBILENETV3 = TEST_DIRECTORY + "../models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE.close();
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {
        makeSearchable("numbers");
    }

    @Test
    public void pageRotationTest() throws IOException, InterruptedException {
        makeSearchable("pageRotation");
    }

    @Test
    public void twoImagesTest() throws IOException, InterruptedException {
        makeSearchable("2images");
    }

    @Test
    public void twoPagesTest() throws IOException, InterruptedException {
        makeSearchable("2pages");
    }

    @Test
    public void rotatedTest() throws IOException, InterruptedException {
        makeSearchable("rotated");
    }

    @Test
    public void mixedRotationTest() throws IOException, InterruptedException {
        makeSearchable("mixedRotation");
    }

    @Test
    public void notRecognizableTest() throws IOException, InterruptedException {
        // OnnxTr engine could recognize
        makeSearchable("notRecognizable");
    }

    @Test
    public void imageIntersectionTest() throws IOException, InterruptedException {
        makeSearchable("imageIntersection");
    }

    @Test
    public void whiteTextTest() throws IOException, InterruptedException {
        // OCRed by onnxtr. Almost good, w is OCRed as rotated m.
        // If you don't use orientation predictor, the result becomes very good.
        makeSearchable("whiteText");
    }

    @Test
    public void changedImageProportionTest() throws IOException, InterruptedException {
        makeSearchable("changedImageProportion");
    }

    @Test
    public void textWithImagesTest() throws IOException, InterruptedException {
        makeSearchable("textWithImages");
    }

    @Test
    public void invisibleTextImageTest() throws IOException, InterruptedException {
        makeSearchable("invisibleTextImage");
    }

    @Test
    public void layersTest() throws IOException, InterruptedException {
        OcrPdfCreatorProperties ocrPdfCreatorProperties =
                new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.MAGENTA).setTextLayerName("Text");
        makeSearchable("2pages", "layers", ocrPdfCreatorProperties);
    }

    @Test
    public void skewedRotated45Test() throws IOException, InterruptedException {
        makeSearchable("skewedRotated45");
    }

    private void makeSearchable(String fileName) throws IOException, InterruptedException {
        makeSearchable(fileName, fileName, null);
    }

    private void makeSearchable(String fileName, String outFileName, OcrPdfCreatorProperties ocrPdfCreatorProperties)
            throws IOException, InterruptedException {
        String srcPath = TEST_PDFS_DIRECTORY + fileName + ".pdf";
        String outPath = TARGET_DIRECTORY + outFileName + ".pdf";
        String cmpPath = TEST_DIRECTORY + "cmp_" + outFileName + ".pdf";

        if (ocrPdfCreatorProperties == null) {
            ocrPdfCreatorProperties = new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.MAGENTA);
        }
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE, ocrPdfCreatorProperties);
        ocrPdfCreator.makePdfSearchable(new File(srcPath), new File(outPath));
        Assertions.assertNull(new CompareTool().compareByContent(outPath, cmpPath, TARGET_DIRECTORY, "diff_"));
    }
}
