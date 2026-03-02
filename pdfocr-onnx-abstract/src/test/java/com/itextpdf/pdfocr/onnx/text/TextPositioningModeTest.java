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
package com.itextpdf.pdfocr.onnx.text;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Tag("IntegrationTest")
public class TextPositioningModeTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/text/TextPositioningModeTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/text/TextPositioningModeTest/";

    public static Iterable<Object[]> parameters() {
        return Arrays.stream(OcrEngineTypeWithTextPositioning.all())
                .map(type -> new Object[]{type})
                .collect(Collectors.toList());
    }

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        for (OcrEngineTypeWithTextPositioning engineType : OcrEngineTypeWithTextPositioning.all()) {
            if (engineType.instance != null) {
                engineType.instance.close();
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parameters")
    public void linesTest(OcrEngineTypeWithTextPositioning engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String src = TEST_IMAGE_DIRECTORY + "lines.png";
        String dest = TARGET_DIRECTORY + name + "_lines.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + name + "_lines.pdf";

        doOcrAndCreatePdf(src, dest, ocrEngine);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parameters")
    public void obliqueLinesTest(OcrEngineTypeWithTextPositioning engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String src = TEST_IMAGE_DIRECTORY + "obliqueLines.png";
        String dest = TARGET_DIRECTORY + name + "_obliqueLines.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + name + "_obliqueLines.pdf";

        doOcrAndCreatePdf(src, dest, ocrEngine);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parameters")
    public void linesWithSpacesTest(OcrEngineTypeWithTextPositioning engineType) throws IOException, InterruptedException {
        IOcrEngine ocrEngine = engineType.get();
        String name = engineType.getDisplayName();

        String src = TEST_IMAGE_DIRECTORY + "linesWithSpaces.png";
        String dest = TARGET_DIRECTORY + name + "_linesWithSpaces.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + name + "_linesWithSpaces.pdf";

        doOcrAndCreatePdf(src, dest, ocrEngine);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    private void doOcrAndCreatePdf(String imagePath, String destPdfPath, IOcrEngine ocrEngine) throws IOException {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(ocrEngine, new OcrPdfCreatorProperties()
                .setTextLayerName("Text1")
                .setTextColor(ColorConstants.MAGENTA)
                .setTextBBoxColor(ColorConstants.GREEN));
        try (PdfWriter writer = new PdfWriter(destPdfPath)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(imagePath)), writer).close();
        }
    }
}
