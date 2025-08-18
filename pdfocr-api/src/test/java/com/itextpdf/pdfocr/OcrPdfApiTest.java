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
package com.itextpdf.pdfocr;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.exceptions.IoExceptionMessageConstant;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.pdfa.VeraPdfValidator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OcrPdfApiTest extends ExtendedITextTest {

    public static final String DESTINATION_FOLDER = PdfHelper.TARGET_DIRECTORY + "OcrPdfApiTest/";
    public static final String REFERENCE_FOLDER = PdfHelper.TEST_DIRECTORY + "OcrPdfApiTest/";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(DESTINATION_FOLDER);
    }

    @Test
    public void notExtractableImageTest() {
        // Take something else for the test when we start supporting it
        Exception e = Assertions.assertThrows(com.itextpdf.io.exceptions.IOException.class,
                () -> makeSearchable("deviceN8bit5Channels"));
        Assertions.assertEquals(MessageFormatUtil.format(
                IoExceptionMessageConstant.COLOR_SPACE_IS_NOT_SUPPORTED, "/DeviceN"), e.getMessage());
    }

    @Test
    public void notExistingFileTest() {
        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> makeSearchable("notExistingFile"));
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.IO_EXCEPTION_OCCURRED, e.getMessage());
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.MAGENTA);
        makeSearchable("randomImage", "basic", properties);
    }

    @Test
    public void textLayerTest() throws IOException, InterruptedException {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.MAGENTA);
        properties.setTextLayerName("text");
        makeSearchable("randomImage", "textLayer", properties);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.PAGE_SIZE_IS_NOT_APPLIED),
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.IMAGE_LAYER_NAME_IS_NOT_APPLIED)
    })
    public void notRelevantPropertiesTest() throws IOException, InterruptedException {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.MAGENTA);
        // Page size and image layer should not take any effect
        properties.setPageSize(new Rectangle(500, 500));
        properties.setImageLayerName("image");
        makeSearchable("randomImage", "notRelevantProperties", properties);
    }

    @Test
    public void titleAndLangTest() throws IOException, InterruptedException {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.MAGENTA);
        properties.setTitle("Title");
        properties.setPdfLang("de-DE");

        makeSearchable("randomImage", "titleAndLang", properties);
    }

    @Test
    public void noReaderTest() {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));

        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> makeSearchable("noReader", pdfDoc));
        Assertions.assertEquals(
                PdfOcrExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE, e.getMessage());
    }

    @Test
    public void noWriterTest() throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(PdfHelper.getPdfsTestDirectory() + "randomImage.pdf"));

        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> makeSearchable("noWriter", pdfDoc));
        Assertions.assertEquals(
                PdfOcrExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE, e.getMessage());
    }

    @Test
    public void taggedPdfTest() {
        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> makeSearchable("pdfUA"));
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.TAGGED_PDF_IS_NOT_SUPPORTED, e.getMessage());
    }

    @Test
    public void pdfA3bTest() {
        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> makeSearchable("pdfA3b"));
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.PDFA_IS_NOT_SUPPORTED, e.getMessage());
    }

    @Test
    public void pdfA3bNoValidationTest() throws IOException, InterruptedException {
        String path = PdfHelper.getPdfsTestDirectory() + "pdfA3b.pdf";
        String expectedPdfPath = REFERENCE_FOLDER + "cmp_pdfA3b.pdf";
        String resultPdfPath = DESTINATION_FOLDER + "pdfA3b.pdf";

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.MAGENTA);
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties) {
            @Override
            protected void validateInputPdfDocument(PdfDocument pdfDoc) {
            }
        };
        ocrPdfCreator.makePdfSearchable(new File(path), new File(resultPdfPath), null);

        Assertions.assertNull(
                new CompareTool().compareByContent(resultPdfPath, expectedPdfPath, DESTINATION_FOLDER, "diff_"));
        new VeraPdfValidator().validate(resultPdfPath);
    }

    private static void makeSearchable(String fileName) throws InterruptedException, IOException {
        makeSearchable(fileName, null, new OcrPdfCreatorProperties());
    }

    private static void makeSearchable(String fileName, String outFileName, OcrPdfCreatorProperties properties)
            throws InterruptedException, IOException {
        if (outFileName == null) {
            outFileName = fileName;
        }
        String path = PdfHelper.getPdfsTestDirectory() + fileName + ".pdf";
        String expectedPdfPath = REFERENCE_FOLDER + "cmp_" + outFileName + ".pdf";
        String resultPdfPath = DESTINATION_FOLDER + outFileName + ".pdf";

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        ocrPdfCreator.makePdfSearchable(new File(path), new File(resultPdfPath));

        Assertions.assertNull(
                new CompareTool().compareByContent(resultPdfPath, expectedPdfPath, DESTINATION_FOLDER, "diff_"));
    }

    private static void makeSearchable(String fileName, PdfDocument pdfDoc)
            throws InterruptedException, IOException {
        String expectedPdfPath = REFERENCE_FOLDER + "cmp_" + fileName + ".pdf";
        String resultPdfPath = DESTINATION_FOLDER + fileName + ".pdf";

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        ocrPdfCreator.makePdfSearchable(pdfDoc);

        Assertions.assertNull(
                new CompareTool().compareByContent(resultPdfPath, expectedPdfPath, DESTINATION_FOLDER, "diff_"));
    }
}
