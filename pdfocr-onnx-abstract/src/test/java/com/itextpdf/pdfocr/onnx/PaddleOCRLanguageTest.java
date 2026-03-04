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

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Tag("IntegrationTest")
public class PaddleOCRLanguageTest extends ExtendedITextTest {

    private static final String FONT_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/fonts/";
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/PaddleOCRLanguageTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY =
            "./target/test/resources/com/itextpdf/pdfocr/PaddleOCRLanguageTest/";

    private static final String PADDLE_DET =
            "./src/test/resources/com/itextpdf/pdfocr/models/paddleocr/PP-OCRv5_mobile_det_infer/";
    private static final String PADDLE_REC =
            "./src/test/resources/com/itextpdf/pdfocr/models/paddleocr/recognition/";

    private static final Map<String, String> PADDLE_REC_PATHS = new HashMap<>();

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        String[] entries = FileUtil.listDirectoriesInDirectory(PADDLE_REC, false);

        for (String entryPath : entries) {
            if (FileUtil.directoryExists(entryPath)) {
                File dir = new File(entryPath);
                PADDLE_REC_PATHS.put(dir.getName(), dir.getAbsolutePath());
            }
        }
    }

    @Test
    public void arabicTest() throws Exception {
        String modelName = "arabic_PP-OCRv3_mobile_rec_infer";
        runOcrTest(modelName, "arabic_01.jpg");
    }

    @Test
    public void bengaliTest() throws Exception {
        String modelName = "devanagari_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "bengali_01.jpeg");
    }

    @Test
    public void chineseTest() throws Exception {
        String modelName = "chinese_cht_PP-OCRv3_mobile_rec_infer";
        runOcrTest(modelName, "chinese_01.jpg");
    }

    @Test
    public void french01Test() throws Exception {
        String modelName = "cyrillic_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "french_01.png");
    }

    @Test
    public void georgian01Test() throws Exception {
        String modelName = "cyrillic_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "georgian_01.jpg");
    }

    @Test
    public void german01Test() throws Exception {
        String modelName = "PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "german_01.jpg");
    }

    @Test
    public void greekTest() throws Exception {
        String modelName = "th_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "greek_01.jpg");
    }

    @Test
    public void hindi02Test() throws Exception {
        String modelName = "devanagari_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "hindi_02.jpg");
    }

    @Test
    public void japanese01Test() throws Exception {
        String modelName = "japan_PP-OCRv3_mobile_rec_infer";
        runOcrTest(modelName, "japanese_01.png");
    }

    @Test
    public void multiLanguageTest() throws Exception {
        String modelName = "PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "multilang.jpg");
    }

    @Test
    public void russianTest() throws Exception {
        String modelName = "cyrillic_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "russian.jpg");
    }

    @Test
    public void spanishTest() throws Exception {
        String modelName = "PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "spanish_01.jpg");
    }

    @Test
    public void thaiTest() throws Exception {
        String modelName = "th_PP-OCRv5_mobile_rec_infer";
        runOcrTest(modelName, "thai_01.jpg");
    }

    private void runOcrTest(String modelName, String imageFile) throws Exception {
        IDetectionPredictor paddleDetectionPredictor = OnnxDetectionPredictor.paddleOcr(PADDLE_DET);
        IRecognitionPredictor paddleRecognitionPredictor =
                OnnxRecognitionPredictor.paddleOcr(PADDLE_REC_PATHS.get(modelName));

        String cleanModelName = modelName.replace(".onnx", "");
        String imageName = imageFile.split("\\.")[0];

        String src = TEST_IMAGE_DIRECTORY + imageFile;
        String dest = TARGET_DIRECTORY + cleanModelName + "_" + imageName + ".pdf";
        String cmpTxt = TEST_DIRECTORY + imageName + ".txt";

        try (OnnxOcrEngine ocrEngine = new OnnxOcrEngine(paddleDetectionPredictor, paddleRecognitionPredictor)) {
            OnnxTestUtils.doOcrAndCreatePdf(src, dest, ocrEngine, createOcrProperties());
            OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.2);
        }
    }

    private OcrPdfCreatorProperties createOcrProperties() {
        FontProvider fontProvider = new FontProvider();
        fontProvider.addDirectory(FONT_DIRECTORY);
        return new OcrPdfCreatorProperties()
                .setTextLayerName("Text1")
                .setTextColor(DeviceCmyk.MAGENTA).setFontProvider(fontProvider);
    }
}