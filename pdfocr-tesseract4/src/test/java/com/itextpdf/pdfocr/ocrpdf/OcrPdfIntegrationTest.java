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
package com.itextpdf.pdfocr.ocrpdf;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.commons.utils.StringNormalizer;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.logs.KernelLogMessageConstant;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrInputTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.test.LogLevelConstants;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class OcrPdfIntegrationTest extends IntegrationTestHelper {

    private static final String TARGET_DIRECTORY = getTargetDirectory() + "OcrPdfIntegrationTest/";
    private static final String CMP_DIRECTORY = TEST_DOCUMENTS_DIRECTORY + "OcrPdfIntegrationTest/";
    private final AbstractTesseract4OcrEngine tesseractReader;
    private final String testType;

    public OcrPdfIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
        this.testType = StringNormalizer.toLowerCase(type.toString());
    }

    @BeforeAll
    public static void init() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @BeforeEach
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
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
        // Tesseract doesn't return textangle, that is why the resulting text is not rotated here
        makeSearchable("rotated");
    }

    @Test
    public void mixedRotationTest() throws IOException, InterruptedException {
        makeSearchable("mixedRotation");
    }

    @Test
    public void notRecognizableTest() throws IOException, InterruptedException {
        makeSearchable("notRecognizable");
    }

    @Test
    public void imageIntersectionTest() throws IOException, InterruptedException {
        makeSearchable("imageIntersection");
    }

    @Test
    public void whiteTextTest() throws IOException, InterruptedException {
        // Not OCRed by tesseract
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
    public void skewedRotated45Test() throws IOException, InterruptedException {
        makeSearchable("skewedRotated45");
    }


    @Test
    public void multiFilesTest() throws IOException, InterruptedException {
        List<File> files = Arrays.<File>asList(
                new File(TEST_IMAGES_DIRECTORY + "german_01.jpg"),
                new File(TEST_IMAGES_DIRECTORY + "noisy_01.png"),
                new File(TEST_IMAGES_DIRECTORY + "nümbérs.jpg")
        );

        String resultPdfPath = TARGET_DIRECTORY + "multiFiles_" + testType + ".pdf";
        String expectedPdfPath = CMP_DIRECTORY + "cmp_multiFiles.pdf";

        OcrPdfCreatorProperties properties = creatorProperties("Text1", "Image1", DeviceCmyk.CYAN);
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader, properties);
        try (PdfWriter writer = new PdfWriter(resultPdfPath)) {
            ocrPdfCreator.createPdf(files, writer).close();
        }

        Assertions.assertNull(new CompareTool().compareByContent(resultPdfPath, expectedPdfPath,
                getTargetDirectory(), "diff_"));
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = KernelLogMessageConstant.JPXDECODE_FILTER_DECODING, logLevel = LogLevelConstants.INFO),
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_OCR_IMAGE, logLevel = LogLevelConstants.ERROR),
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE, logLevel = LogLevelConstants.ERROR),
    })
    public void jpeg2000Test() {
        Exception e = Assertions.assertThrows(PdfOcrInputTesseract4Exception.class,
                () -> makeSearchableWithoutCompare("jpeg2000"));
        String message = e.getMessage();
        // Exception message is each run unique and looks like
        // "pdfocr_img_f31dce56-6917-49ac-b437-92d296936f5413047335812688118701.jp2 format is not supported."
        message = ".jp2 " + message.substring(message.indexOf("format"));
        Assertions.assertEquals(
                MessageFormatUtil.format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_INPUT_IMAGE_FORMAT, ".jp2"),
                message);
    }

    private String makeSearchableWithoutCompare(String fileName) {
        String path = TEST_PDFS_DIRECTORY + fileName + ".pdf";
        String resultPdfPath = TARGET_DIRECTORY + fileName + "_" + testType + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                Collections.<String>singletonList("eng"), null, DeviceCmyk.MAGENTA, false, false);
        return resultPdfPath;
    }

    private void makeSearchable(String fileName) throws InterruptedException, IOException {
        String resultPdfPath = makeSearchableWithoutCompare(fileName);
        String expectedPdfPath = CMP_DIRECTORY + fileName + ".pdf";

        Assertions.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    private OcrPdfCreatorProperties creatorProperties(String textLayerName, String imageLayerName, Color color) {
        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName(textLayerName);
        ocrPdfCreatorProperties.setTextColor(color);
        ocrPdfCreatorProperties.setImageLayerName(imageLayerName);
        return ocrPdfCreatorProperties;
    }
}
