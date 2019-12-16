package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ImageFormatIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testTextFromBMP() {
        String path = testImagesDirectory + "example_01.BMP";
        String expectedOutput = "This is\ntest\na\nfor\nmessage\nScanner\nOCR\nTest";

        File file = new File(path);

        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJFIF() {
        String path = testImagesDirectory + "example_02.JFIF";
        String expectedOutput = "This is\ntest\na\nfor\nmessage\nScanner\nOCR\nTest";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJPG() {
        String path = testImagesDirectory + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputTIFFBig() {
        String path = testImagesDirectory + "example_03_10MB.tiff";
        String expectedOutput = "Tagged\nFile\nFormat\nImage";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputMultipagesTIFF() {
        String path = testImagesDirectory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage\n4";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file, 4);
        Assert.assertNotNull(realOutputHocr);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputWrongFormat() {
        File file = new File(testImagesDirectory + "example.txt");

        String realOutput = getTextFromPdf(file);
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".jpg", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareBigTiff() throws IOException, InterruptedException {
        String filename = "example_03_10MB";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        String filename = "scanned_eng_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".png", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareMultiPageEngTiff() throws IOException, InterruptedException {
        String filename = "multipage";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }
}
