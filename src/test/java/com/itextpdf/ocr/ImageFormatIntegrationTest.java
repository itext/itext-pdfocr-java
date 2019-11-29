package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ImageFormatIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testTextFromBMP() {
        String path = testDirectory + "example_01.BMP";
        String expectedOutput = "This is a test\nfor\nmessage\nOCR Scanner\nTest";

        File file = new File(path);

        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJFIF() {
        String path = testDirectory + "example_02.JFIF";
        String expectedOutput = "This is a test\nfor\nmessage\nOCR Scanner\nTest";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJPG() {
        String path = testDirectory + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /*@Test
    public void testInputTIFFBig() {
        String path = testDirectory + "example_03_10MB.tiff";
        String expectedOutput = "File Format\nImage\nTagged";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }*/

    /*@Test
    public void testInputMultipagesTIFF() {
        String path = testDirectory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage\n5";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file, 5);
        Assert.assertNotNull(realOutputHocr);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }*/

    @Test
    public void testInputWrongFormat() {
        File file = new File(testDirectory + "example.txt");

        String realOutput = getTextFromPdf(file);
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = testDirectory + filename + ".pdf";
        String resultPdfPath = testDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testDirectory + filename + ".jpg", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareBigTiff() throws IOException, InterruptedException {
        String filename = "example_03_10MB";
        String expectedPdfPath = testDirectory + filename + ".pdf";
        String resultPdfPath = testDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testDirectory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        String filename = "scanned_eng_01";
        String expectedPdfPath = testDirectory + filename + ".pdf";
        String resultPdfPath = testDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testDirectory + filename + ".png", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareMultiPageEngTiff() throws IOException, InterruptedException {
        String filename = "multipage";
        String expectedPdfPath = testDirectory + filename + ".pdf";
        String resultPdfPath = testDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testDirectory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }
}
