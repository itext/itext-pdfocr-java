package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ImageFormatIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void compareBMP() throws IOException, InterruptedException {
        String filename = "example_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".BMP", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareJFIF() throws IOException, InterruptedException {
        String filename = "example_02";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".JFIF", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
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
        String expectedOutput = "Multipage\nTIFF\nExample\n5\nPage";

        File file = new File(path);
        String realOutputHocr = getTextFromPdf(file, 5);
        Assert.assertNotNull(realOutputHocr);
        Assert.assertEquals(expectedOutput, realOutputHocr);
    }

    @Test
    public void testInputWrongFormat() {
        try {
            File file = new File(testImagesDirectory + "example.txt");
            String realOutput = getTextFromPdf(file);
            Assert.assertNotNull(realOutput);
            Assert.assertEquals("", realOutput);
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_INPUT_IMAGE_FORMAT,
                            "txt");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".jpg",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareBigTiff() throws IOException, InterruptedException {
        String filename = "example_03_10MB";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".tiff",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        String filename = "scanned_eng_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".png",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareMultiPageEngTiff() throws IOException,
            InterruptedException {
        String filename = "multipage";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".tiff",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }
}
