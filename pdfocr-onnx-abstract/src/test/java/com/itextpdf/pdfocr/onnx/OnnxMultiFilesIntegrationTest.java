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

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.onnx.util.OcrEngineType;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Tag("IntegrationTest")
public class OnnxMultiFilesIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxMultiFilesIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxMultiFilesIntegrationTest/";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @Test
    @LogMessages(messages = {@LogMessage(messageTemplate =
            PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, ignore = true)
    })
    public void multiFilesTest() throws IOException, InterruptedException {
        List<File> files = Arrays.<File>asList(
                new File(TEST_IMAGE_DIRECTORY + "german_01.jpg"),
                new File(TEST_IMAGE_DIRECTORY + "noisy_01.png"),
                new File(TEST_IMAGE_DIRECTORY + "nümbérs.jpg"),
                new File(TEST_IMAGE_DIRECTORY + "example_04.png")
        );

        String dest = TARGET_DIRECTORY + "multiFiles.pdf";
        String cmp = TEST_DIRECTORY + "cmp_multiFiles.pdf";

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OcrEngineType.PADDLE.get(), creatorProperties());
        try (PdfWriter writer = new PdfWriter(dest)) {
            ocrPdfCreator.createPdf(files, writer).close();
        }

        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
    }

    private OcrPdfCreatorProperties creatorProperties() {
        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName("Text1");
        ocrPdfCreatorProperties.setTextColor(DeviceCmyk.CYAN);
        ocrPdfCreatorProperties.setImageLayerName("Image1");
        return ocrPdfCreatorProperties;
    }
}
