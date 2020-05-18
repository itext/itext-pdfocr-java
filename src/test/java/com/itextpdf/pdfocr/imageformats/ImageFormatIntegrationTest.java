package com.itextpdf.ocr.imageformats;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.ocr.AbstractIntegrationTest;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
import com.itextpdf.ocr.LogMessageConstant;
import com.itextpdf.ocr.OcrException;
import com.itextpdf.ocr.TesseractReader;
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

    TesseractReader tesseractReader;

    @Before
    public void restoreTesseractReaderPreprocessingImagesState() {
        tesseractReader.setPreprocessingImages(true);
    }

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
        String resultPdfPath = testDocumentsDirectory + filename + "_" + testName + "_created.pdf";

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
    public void testTextFromPNM() {
        String path = testImagesDirectory + "numbers_01.pnm";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromPPM() {
        String path = testImagesDirectory + "numbers_01.ppm";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertEquals(realOutputHocr, expectedOutput);
    }

    @Test
    public void testTextFromPPM02() {
        String path = testImagesDirectory + "englishText.ppm";
        String expectedOutput = "This is a test message for OCR Scanner Test PPMTest";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void testTextFromPPMWithoutPreprocessing() {
        String path = testImagesDirectory + "numbers_01.ppm";
        String expectedOutput = "619121";

        tesseractReader.setPreprocessingImages(false);
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertEquals(realOutputHocr, expectedOutput);
    }

    @Test
    public void testTextFromPGM() {
        String path = testImagesDirectory + "numbers_01.pgm";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromPGM02() {
        String path = testImagesDirectory + "englishText.pgm";
        String expectedOutput = "This is a test message for OCR Scanner Test PGMTest";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void testTextFromPBM() {
        String path = testImagesDirectory + "numbers_01.pbm";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromPBM02() {
        String path = testImagesDirectory + "englishText.pbm";
        String expectedOutput = "This is a test message for OCR Scanner Test PBMTest";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void testBigTiffWithoutPreprocessing() {
        String path = testImagesDirectory + "example_03_10MB.tiff";
        String expectedOutput = "Image File Format";

        tesseractReader.setPreprocessingImages(false);
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

        tesseractReader.setPreprocessingImages(false);
        String realOutputHocr = getTextFromPdf(tesseractReader, file, 3,
                Collections.<String>singletonList("eng"));
        Assert.assertNotNull(realOutputHocr);
        Assert.assertEquals(expectedOutput, realOutputHocr);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.CannotReadInputImage, count = 1)
    })
    @Test
    public void testInputWrongFormat() {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(OcrException.IncorrectInputImageFormat,
                        "txt"));
        File file = new File(testImagesDirectory + "example.txt");
        getTextFromPdf(tesseractReader, file);
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String testName = "compareNumbersJPG";
        String filename = "numbers_01";
        String expectedPdfPath = testDocumentsDirectory + filename + ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_" + testName + "_created.pdf";

        tesseractReader.setTextPositioning(TextPositioning.BY_WORDS);
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".jpg",
                resultPdfPath);
        tesseractReader.setTextPositioning(TextPositioning.BY_LINES);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");
    }
}
