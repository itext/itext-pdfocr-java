/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.pdfocr.imageformats;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class ImageFormatIntegrationTest extends IntegrationTestHelper {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    AbstractTesseract4OcrEngine tesseractReader;
    String testType;

    public ImageFormatIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
        this.testType = type.toString().toLowerCase();
    }

    @Before
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void compareBmp() throws IOException, InterruptedException {
        String testName = "compareBmp";
        String fileName = "example_01";
        String path = TEST_IMAGES_DIRECTORY + fileName + ".BMP";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + fileName + "_" + testType + ".pdf";
        String resultPdfPath = getTargetDirectory() + fileName + "_" + testName + "_" + testType + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                Collections.<String>singletonList("eng"), DeviceCmyk.MAGENTA);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void testBMPText() {
        String path = TEST_IMAGES_DIRECTORY + "example_01.BMP";
        String expectedOutput = "This is a test message for OCR Scanner Test";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.<String>singletonList("eng"));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        realOutputHocr = realOutputHocr.replaceAll("[‘]", "");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void compareBmp02() throws IOException, InterruptedException {
        String testName = "compareBmp02";
        String fileName = "englishText";
        String path = TEST_IMAGES_DIRECTORY + fileName + ".bmp";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + fileName + "_" + testType + ".pdf";
        String resultPdfPath = getTargetDirectory() + fileName + "_" + testName + "_" + testType + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                Collections.<String>singletonList("eng"), DeviceCmyk.MAGENTA);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void testBMPText02() {
        String path = TEST_IMAGES_DIRECTORY + "englishText.bmp";
        String expectedOutput = "This is a test message for OCR Scanner Test BMPTest";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.<String>singletonList("eng"));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void compareJFIF() throws IOException, InterruptedException {
        String testName = "compareJFIF";
        String filename = "example_02";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + ".pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".JFIF",
                resultPdfPath, null, DeviceCmyk.MAGENTA);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void compareJpg() throws IOException, InterruptedException {
        String testName = "compareJpg";
        String fileName = "numbers_02";
        String path = TEST_IMAGES_DIRECTORY + fileName + ".jpg";
        String pdfName = fileName + "_" + testName + "_" + testType + ".pdf";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + pdfName;
        String resultPdfPath = getTargetDirectory() + pdfName;

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                null, DeviceCmyk.BLACK);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void testTextFromJPG() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader
                        .getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void compareJpe() throws IOException, InterruptedException {
        String testName = "compareJpe";
        String fileName = "numbers_01";
        String path = TEST_IMAGES_DIRECTORY + fileName + ".jpe";
        String pdfName = fileName + "_" + testName + "_" + testType + ".pdf";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + pdfName;
        String resultPdfPath = getTargetDirectory() + pdfName;

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                null, DeviceCmyk.BLACK);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void testTextFromJPE() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpe";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void compareTif() throws IOException, InterruptedException {
        String testName = "compareTif";
        String fileName = "numbers_01";
        String path = TEST_IMAGES_DIRECTORY + fileName + ".tif";
        String pdfName = fileName + "_" + testName + "_" + testType + ".pdf";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + pdfName;
        String resultPdfPath = getTargetDirectory() + pdfName;

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                null, DeviceCmyk.BLACK);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void testTextFromTIF() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.tif";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testBigTiffWithoutPreprocessing() {
        String path = TEST_IMAGES_DIRECTORY + "example_03_10MB.tiff";
        String expectedOutput = "Image File Format";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false)
                        .setPageSegMode(null));
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.<String>singletonList("eng"));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void compareMultipagesTIFFWithPreprocessing() throws IOException, InterruptedException {
        String testName = "compareMultipagesTIFFWithPreprocessing";
        String fileName = "multipage";
        String path = TEST_IMAGES_DIRECTORY + fileName + ".tiff";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + fileName + "_" + testType + ".pdf";
        String resultPdfPath = getTargetDirectory() + fileName + "_" + testName + "_" + testType + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                Collections.<String>singletonList("eng"), DeviceCmyk.BLACK);

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }

    @Test
    public void testInputMultipagesTIFFWithPreprocessing() {
        String path = TEST_IMAGES_DIRECTORY + "multîpage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 5";

        File file = new File(path);

        String realOutputHocr = getTextFromPdf(tesseractReader, file, 5,
                Collections.<String>singletonList("eng"));
        Assert.assertNotNull(realOutputHocr);
        Assert.assertEquals(expectedOutput, realOutputHocr);
    }

    @Test
    public void testInputMultipagesTIFFWithoutPreprocessing() {
        String path = TEST_IMAGES_DIRECTORY + "multîpage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 3";

        File file = new File(path);

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        String realOutputHocr = getTextFromPdf(tesseractReader, file, 3,
                Collections.<String>singletonList("eng"));
        Assert.assertNotNull(realOutputHocr);
        Assert.assertEquals(expectedOutput, realOutputHocr);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 1)
    })
    @Test
    public void testInputWrongFormat() {
        junitExpectedException.expect(PdfOcrTesseract4Exception.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_INPUT_IMAGE_FORMAT,
                        "wierdwords.gif"));
        File file = new File(TEST_IMAGES_DIRECTORY + "wierdwords.gif");
        getTextFromPdf(tesseractReader, file);
    }

    @Test
    public void testSupportedImageWithIncorrectTypeInName() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.nnn";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testJpgWithoutPreprocessing() {
        String path = TEST_IMAGES_DIRECTORY + "nümbérs.jpg";
        String expectedOutput = "619121";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.<String>singletonList("eng"));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String testName = "compareNumbersJPG";
        String filename = "nümbérs";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + "numbers_01.pdf";
        String resultPdfPath = getTargetDirectory() + "numbers_01_" + testName + ".pdf";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_WORDS));
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg",
                resultPdfPath);
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_LINES));

        Assert.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }
}
