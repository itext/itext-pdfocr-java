package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

@Category(IntegrationTest.class)
public class ImageFormatIntegrationTest extends AbstractIntegrationTest {
    /*@Test
    public void testTextFromBMP() {
        String path = directory + "example_01.BMP";
        String expectedOutput = "This is a test\nmessage\nfor\nOCR Scanner\nTest";

        File file = new File(path);

        String realOutputHocr = getTextFromPdfFile(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJFIF() {
        String path = directory + "example_02.JFIF";
        String expectedOutput = "This is a test\nmessage\nfor\nOCR Scanner\nTest";

        File file = new File(path);
        String realOutputHocr = getTextFromPdfFile(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJPG() {
        String path = directory + "numbers_01.jpg";
        String expectedOutput = "619121";

        File file = new File(path);
        String realOutputHocr = getTextFromPdfFile(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputTIFFBig() {
        String path = directory + "example_03_10MB.tiff";
        String expectedOutput = "Tagged\nImage\nFile Format";

        File file = new File(path);
        String realOutputHocr = getTextFromPdfFile(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputMultipagesTIFF() {
        String path = directory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage\n6";

        File file = new File(path);
        String realOutputHocr = getTextFromPdfFile(file, 6);
        assert realOutputHocr != null;
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputWrongFormat() {
        File file = new File(directory + "example.txt");

        String realOutput = getTextFromPdfFile(file);
        assert realOutput != null;
        Assert.assertEquals("", realOutput);
    }

    @Test
    public void testInputInvalidImage() throws IOException {
        File file1 = new File(directory + "example.txt");
        File file2 = new File(directory + "example_05_corrupted.bmp");
        File file3 = new File(directory + "numbers_01.jpg");

        String expectedPage3 = "619121";

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Arrays.asList(file1, file2, file3));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(baos), false);
        assert doc != null;
        doc.close();

        InputStream stream = new ByteArrayInputStream(baos.toByteArray());
        PdfDocument pdf = new PdfDocument(new PdfReader(stream));
        stream.close();

        ITextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
        String realPage1 = PdfTextExtractor.getTextFromPage(pdf.getPage(1), strategy);
        String realPage2 = PdfTextExtractor.getTextFromPage(pdf.getPage(2), strategy);
        String realPage3 = PdfTextExtractor.getTextFromPage(pdf.getPage(3), strategy);

        Assert.assertEquals("", realPage1);
        Assert.assertEquals("", realPage2);
        Assert.assertEquals(expectedPage3, realPage3);
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcrAndSaveToPath(directory + filename + ".jpg", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareBigTiff() throws IOException, InterruptedException {
        String filename = "example_03_10MB";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcrAndSaveToPath(directory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        String filename = "scanned_eng_01";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcrAndSaveToPath(directory + filename + ".png", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareMultiPageEngTiff() throws IOException, InterruptedException {
        String filename = "multipage";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcrAndSaveToPath(directory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }*/
}
