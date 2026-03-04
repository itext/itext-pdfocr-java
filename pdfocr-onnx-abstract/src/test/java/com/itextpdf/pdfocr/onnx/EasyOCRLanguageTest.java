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
import com.itextpdf.pdfocr.onnx.recognition.EasyOcrMapper;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag("IntegrationTest")
public class EasyOCRLanguageTest extends ExtendedITextTest {

    private static final String FONT_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/fonts/";
    private static final String TEST_DIRECTORY =
            "./src/test/resources/com/itextpdf/pdfocr/EasyOCRLanguageTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY =
            "./target/test/resources/com/itextpdf/pdfocr/EasyOCRLanguageTest/";

    private static final String EASY_DET = "./src/test/resources/com/itextpdf/pdfocr/models/easyocr/craft_mlt_25k.onnx";
    private static final String EASY_REC = "./src/test/resources/com/itextpdf/pdfocr/models/easyocr/recognition/";

    private static final Map<String, String> EASY_REC_PATHS = new HashMap<>();

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        List<File> fileList = new ArrayList<>();
        String[] entries = FileUtil.listFilesInDirectory(EASY_REC, false);
        for (String entryPath : entries) {
            if (entryPath.endsWith(".onnx")) {
                fileList.add(new File(entryPath));
            }
        }

        if (!fileList.isEmpty()) {
            for (File file : fileList) {
                EASY_REC_PATHS.put(file.getName(), file.getAbsolutePath());
            }
        }
    }

    @Test
    public void chineseTest() throws Exception {
        String modelName = "zh_sim_g2.onnx";
        runOcrTest(modelName,
                "chinese_01.jpg");
    }

    @Test
    public void french01Test() throws Exception {
        String modelName = "latin_g2.onnx";
        runOcrTest(modelName,
                "french_01.png");
    }

    @Test
    public void georgian01Test() throws Exception {
        String modelName = "cyrillic_g2.onnx";
        runOcrTest(modelName,
                "georgian_01.jpg");
    }

    @Test
    public void german01Test() throws Exception {
        String modelName = "latin_g2.onnx";
        runOcrTest(modelName,
                "german_01.jpg");
    }

    @Test
    public void greekTest() throws Exception {
        String modelName = "zh_sim_g2.onnx";
        runOcrTest(modelName,
                "greek_01.jpg");
    }

    @Test
    public void japanese01Test() throws Exception {
        String modelName = "japanese_g2.onnx";
        runOcrTest(modelName,
                "japanese_01.png");
    }

    @Test
    public void multiLanguageTest() throws Exception {
        String modelName = "latin_g2.onnx";
        runOcrTest(modelName,
                "multilang.jpg");
    }

    @Test
    public void russianTest() throws Exception {
        String modelName = "cyrillic_g2.onnx";
        runOcrTest(modelName,
                "russian.jpg");
    }

    @Test
    public void spanishTest() throws Exception {
        String modelName = "latin_g2.onnx";
        runOcrTest(modelName,
                "spanish_01.jpg");
    }

    private static EasyOcrMapper resolveMapper(String modelName) {
        if (modelName.contains("latin")) {
            return EasyOcrMapper.LATIN_G2;
        }
        if (modelName.contains("japanese")) {
            return EasyOcrMapper.JAPANESE_G2;
        }
        if (modelName.contains("zh")) {
            return EasyOcrMapper.ZH_SIM_G2;
        }
        if (modelName.contains("cyrillic")) {
            return EasyOcrMapper.CYRILLIC_G2;
        }
        throw new IllegalArgumentException("Cannot determine EasyOcrMapper for model: " + modelName);
    }

    private OcrPdfCreatorProperties createOcrProperties() {
        FontProvider fontProvider = new FontProvider();
        fontProvider.addDirectory(FONT_DIRECTORY);
        return new OcrPdfCreatorProperties()
                .setTextLayerName("Text1")
                .setTextColor(DeviceCmyk.MAGENTA).setFontProvider(fontProvider);
    }

    private void runOcrTest(String modelName, String imageFile) throws Exception {
        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.easyOcr(EASY_DET);
        EasyOcrMapper easyOcrMapper = resolveMapper(modelName);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.easyOcr(EASY_REC_PATHS.get(modelName),
                easyOcrMapper);
        try (OnnxOcrEngine engine = new OnnxOcrEngine(detectionPredictor, recognitionPredictor)) {
            String cleanModelName = modelName.replace(".onnx", "");
            String cleanImageName = imageFile.split("\\.")[0];

            String src = TEST_IMAGE_DIRECTORY + imageFile;
            String dest = TARGET_DIRECTORY + cleanModelName + "_" + cleanImageName + ".pdf";
            String cmpTxt = TEST_DIRECTORY + cleanImageName + ".txt";

            OnnxTestUtils.doOcrAndCreatePdf(src, dest, engine, createOcrProperties());
            OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.2);
        }
    }
}
