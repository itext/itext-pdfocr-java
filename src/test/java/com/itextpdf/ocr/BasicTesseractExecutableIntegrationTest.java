package com.itextpdf.ocr;

import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class BasicTesseractExecutableIntegrationTest extends AbstractIntegrationTest {

     @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractExecutableReader tesseractReader = new TesseractExecutableReader(
                getTessDataDirectory());

        tesseractReader.setPathToExecutable(getTesseractDirectory());
        getTextFromPdf(tesseractReader, file, Collections.singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, Arrays.asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata",
                            langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, Collections.singletonList("spa_new"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata",
                            langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        getTextFromPdf(tesseractReader, file, Collections.singletonList("Georgian"));

        try {
            getTextFromPdf(tesseractReader, file, Collections.singletonList("English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata",
                            scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            tesseractReader.setPathToTessData(scriptTessDataDirectory);
            getTextFromPdf(tesseractReader, file, Arrays.asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata",
                            scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            tesseractReader.setPathToTessData(scriptTessDataDirectory);
            getTextFromPdf(tesseractReader, file, new ArrayList<>());
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());

        Assert.assertEquals(getTesseractDirectory(),
                tesseractReader.getPathToExecutable());

        Assert.assertEquals(getTessDataDirectory(),
                 tesseractReader.getPathToTessData());
    }

    @Test
    public void testIncorrectPathToTessData() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        TesseractReader tesseractReader = new TesseractExecutableReader(getTesseractDirectory(),
                "", Collections.singletonList("eng"));

        try {
            Assert.assertEquals("", tesseractReader.getPathToTessData());
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSDATA, e.getMessage());
        }

        try {
            tesseractReader.setPathToTessData("/test");
            getTextFromPdf(tesseractReader, file, Collections.singletonList("eng"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            "/test");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @Test
    public void testIncorrectPathToTesseractExecutable() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        try {
            getTextFromPdf(new TesseractExecutableReader(null), file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE,
                    e.getMessage());
        }

        try {
            getTextFromPdf(new TesseractExecutableReader(""), file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE,
                    e.getMessage());
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
}
