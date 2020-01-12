package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Category(IntegrationTest.class)
public class BasicTesseractExecutableIntegrationTest extends AbstractIntegrationTest {

     @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractExecutableReader tesseractReader = new TesseractExecutableReader();
        tesseractReader.setPathToExecutable(getTesseractDirectory());
        getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                    Arrays.asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                    Collections.singletonList("spa_new"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                Collections.singletonList("Georgian"));
        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    Collections.singletonList("English"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    Arrays.asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    new ArrayList<>());
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        Assert.assertEquals(getTesseractDirectory(),
                tesseractReader.getPathToExecutable());
    }

    @Test
    public void testIncorrectPathToTessData() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        try {
            getTextFromPdf(tesseractReader, file, "test/",
                    Collections.singletonList("eng"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }
    }

    @Test
    public void testRunningTesseractCmd() {
        try {
            UtilService.runCommand(Arrays.asList("tesseract",
                    "random.jpg"), false);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            UtilService.runCommand(null, false);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }
    }

    @Test
    public void compareJapanesePdf() throws IOException, InterruptedException {
        String filename = "japanese_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                langTessDataDirectory, Arrays.asList("jpn"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }
}
