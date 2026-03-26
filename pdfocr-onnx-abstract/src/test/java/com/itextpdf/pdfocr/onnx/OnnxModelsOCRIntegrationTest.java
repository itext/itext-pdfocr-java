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

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.onnx.util.OcrEngineType;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Tag("IntegrationTest")
public class OnnxModelsOCRIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxModelsOCRIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxModelsOCRIntegrationTest/";

    public static Iterable<Object[]> ocrEngines() {
        return Arrays.stream(OcrEngineType.all())
                .map(type -> new Object[]{type})
                .collect(Collectors.toList());
    }

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ocrEngines")
    public void bmpTest(OcrEngineType engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String src = TEST_IMAGE_DIRECTORY + "englishText.bmp";
        String dest = TARGET_DIRECTORY + name + "_bmp.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + name + "_bmp.pdf";
        String cmpTxt = TEST_DIRECTORY + "bmp" + name + ".txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, ocrEngine);
        OnnxTestUtils.comparePdfs(dest, cmp, TARGET_DIRECTORY);

        extractTextAndCompare(dest, cmpTxt);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ocrEngines")
    public void invoiceThaiTest(OcrEngineType engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String cmp = TEST_DIRECTORY + "cmp_" + name + "_invoice_front_thai.pdf";
        String src = TEST_IMAGE_DIRECTORY + "invoice_front_thai.jpg";
        String dest = TARGET_DIRECTORY + name + "_invoice_front_thai.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, ocrEngine);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ocrEngines")
    public void weirdWordsGifTest(OcrEngineType engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String src = TEST_IMAGE_DIRECTORY + "weirdwords.gif";
        String dest = TARGET_DIRECTORY + name + "_weirdwords.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + name + "_weirdwords.pdf";
        String cmpTxt = TEST_DIRECTORY + "weirdwords.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, ocrEngine);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));

        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.7);
    }

    private void extractTextAndCompare(String dest, String cmpTxt) throws IOException {
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.16);
    }
}