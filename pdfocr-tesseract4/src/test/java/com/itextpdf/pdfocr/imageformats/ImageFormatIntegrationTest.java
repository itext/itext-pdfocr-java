package com.itextpdf.pdfocr.imageformats;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
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

public abstract class ImageFormatIntegrationTest extends AbstractIntegrationTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    Tesseract4OcrEngine tesseractReader;

    public ImageFormatIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testBMPText() {
        String path = testImagesDirectory + "example_01.BMP";
        String expectedOutput = "This is a test message for OCR Scanner Test";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.<String>singletonList("eng"));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        realOutputHocr = realOutputHocr.replaceAll("[‘]", "");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void testBMPText02() {
        String path = testImagesDirectory + "englishText.bmp";
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
        String expectedPdfPath = testDocumentsDirectory + filename + ".pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".JFIF",
                resultPdfPath, null, DeviceCmyk.MAGENTA);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");
    }

    @Test
    public void testTextFromJPG() {
        String path = testImagesDirectory + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJPE() {
        String path = testImagesDirectory + "numbers_01.jpe";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromTIF() {
        String path = testImagesDirectory + "numbers_01.tif";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testBigTiffWithoutPreprocessing() {
        String path = testImagesDirectory + "example_03_10MB.tiff";
        String expectedOutput = "Image File Format";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.<String>singletonList("eng"));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputMultipagesTIFFWithPreprocessing() {
        String path = testImagesDirectory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 5";

        File file = new File(path);

        String realOutputHocr = getTextFromPdf(tesseractReader, file, 5,
                Collections.<String>singletonList("eng"));
        Assert.assertNotNull(realOutputHocr);
        Assert.assertEquals(expectedOutput, realOutputHocr);
    }

    @Test
    public void testInputMultipagesTIFFWithoutPreprocessing() {
        String path = testImagesDirectory + "multipage.tiff";
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
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CannotReadInputImage, count = 1)
    })
    @Test
    public void testInputWrongFormat() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectInputImageFormat,
                        "txt"));
        File file = new File(testImagesDirectory + "example.txt");
        getTextFromPdf(tesseractReader, file);
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String testName = "compareNumbersJPG";
        String filename = "numbers_01";
        String expectedPdfPath = testDocumentsDirectory + filename + ".pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_WORDS));
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".jpg",
                resultPdfPath);
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_LINES));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");
    }
}
