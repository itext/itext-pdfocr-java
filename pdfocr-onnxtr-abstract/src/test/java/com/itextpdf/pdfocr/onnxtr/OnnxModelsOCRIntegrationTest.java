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
package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.EasyOcrMapper;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Tag("IntegrationTest")
public class OnnxModelsOCRIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxModelsOCRIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxModelsOCRIntegrationTest/";

    private static final String PADDLE_DET = "./src/test/resources/com/itextpdf/pdfocr/models/paddleocr/PP-OCRv5_mobile_det_infer/";
    private static final String PADDLE_REC = "./src/test/resources/com/itextpdf/pdfocr/models/paddleocr/PP-OCRv5_mobile_rec_infer/";
    private static final String EASY_DET = "./src/test/resources/com/itextpdf/pdfocr/models/easyocr/craft_mlt_25k.onnx";
    private static final String EASY_REC = "./src/test/resources/com/itextpdf/pdfocr/models/easyocr/latin_g2.onnx";
    private static final String DOCTR_DET = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
    private static final String DOCTR_REC = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";

    public enum OcrEngineType {
        PADDLE("PaddleOCR", () -> createPaddleOcrEngine()),
        EASY("EasyOCR", () -> createEasyOcrEngine()),
        DOCTR("DocTR", () -> createDocTrEngine());

        public volatile OnnxTrOcrEngine instance;
        private final String displayName;
        private final Supplier<OnnxTrOcrEngine> supplier;

        OcrEngineType(String displayName, Supplier<OnnxTrOcrEngine> supplier) {
            this.displayName = displayName;
            this.supplier = supplier;
        }

        public OnnxTrOcrEngine get() {
            if (this.instance == null) {
                synchronized (this) {
                    if (this.instance == null) {
                        this.instance = this.supplier.get();
                    }
                }
            }
            return this.instance;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public static OcrEngineType[] all() {
            return new OcrEngineType[]{PADDLE, EASY, DOCTR};
        }
    }

    private static OnnxTrOcrEngine createPaddleOcrEngine() {
        try {
            IDetectionPredictor paddleDetectionPredictor = OnnxDetectionPredictor.paddleOcr(PADDLE_DET);
            IRecognitionPredictor paddleRecognitionPredictor = OnnxRecognitionPredictor.paddleOcr(PADDLE_REC);
            return new OnnxTrOcrEngine(paddleDetectionPredictor, paddleRecognitionPredictor);
        } catch (IOException e) {
            // Shouldn't reach there.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static OnnxTrOcrEngine createEasyOcrEngine() {
        IDetectionPredictor easyDetectionPredictor = OnnxDetectionPredictor.easyOcr(EASY_DET);
        IRecognitionPredictor easyRecognitionPredictor = OnnxRecognitionPredictor.easyOcr(EASY_REC, EasyOcrMapper.LATIN_G2);
        return new OnnxTrOcrEngine(easyDetectionPredictor, easyRecognitionPredictor);
    }

    private static OnnxTrOcrEngine createDocTrEngine() {
        IDetectionPredictor docTrDetectionPredictor = OnnxDetectionPredictor.fast(DOCTR_DET);
        IRecognitionPredictor docTrRecognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(DOCTR_REC);
        return new OnnxTrOcrEngine(docTrDetectionPredictor, docTrRecognitionPredictor);
    }

    /**
     * Note, that ParameterizedTest will automatically close all autocloseable parameters.
     *
     * @return collection of {@link OnnxTrOcrEngine} instances and corresponding test names
     */
    public static Iterable<Object[]> ocrEngines() {
        return Arrays.stream(OcrEngineType.all())
                .map(type -> new Object[]{type})
                .collect(Collectors.toList());
    }

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        for (OcrEngineType engineType : OcrEngineType.all()) {
            if (engineType.instance != null) {
                engineType.instance.close();
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ocrEngines")
    public void bmpTest(OcrEngineType engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        String dest = TARGET_DIRECTORY + name + "_bmp.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + name + "_bmp.pdf";
        String cmpTxt = TEST_DIRECTORY + "bmp.txt";

        doOcrAndCreatePdf(src, dest, ocrEngine);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        extractTextAndCompare(dest, cmpTxt);
    }

    private void doOcrAndCreatePdf(String imagePath, String destPdfPath, IOcrEngine ocrEngine) throws IOException {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(ocrEngine, new OcrPdfCreatorProperties()
                .setTextLayerName("Text1").setTextColor(DeviceCmyk.MAGENTA));
        try (PdfWriter writer = new PdfWriter(destPdfPath)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(imagePath)), writer).close();
        }
    }

    private void extractTextAndCompare(String dest, String cmpTxt) throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, "Text1");
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            String outText = extractionStrategy.getResultantText();
            String cmpText = getCmpText(cmpTxt);
            double relativeDistance = (double) MathUtil.calculateLevenshteinDistance(cmpText, outText) / cmpText.length();
            Assertions.assertTrue(relativeDistance < 0.16, "Expected: \"" + cmpText + "\", but was: \"" + outText + "\"");
        }
    }

    private String getCmpText(String txtPath) throws IOException {
        int bytesCount = (int) new File(txtPath).length();
        char[] array = new char[bytesCount];
        try (InputStreamReader stream =
                     new InputStreamReader(Files.newInputStream(Paths.get(txtPath)), StandardCharsets.UTF_8)) {
            stream.read(array, 0, bytesCount);
            return new String(array);
        }
    }
}
