package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class TesseractExecutableIntegrationTest extends AbstractIntegrationTest {

    @LogMessages(messages = {
        @LogMessage(messageTemplate = OCRException.INCORRECT_LANGUAGE, count = 2)
    })
    @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractExecutableReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory(), getTessDataDirectory());

        tesseractReader.setPathToExecutable(getTesseractDirectory());
        tesseractReader.setPathToScript(getPathToHocrScript());
        try {
            getTextFromPdf(tesseractReader, file, Arrays.<String>asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata",
                            langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("spa_new"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata",
                            langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        Assert.assertEquals(getTesseractDirectory(),
                tesseractReader.getPathToExecutable());

        Assert.assertEquals(getPathToHocrScript(),
                 tesseractReader.getPathToScript());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = OCRException.INCORRECT_LANGUAGE, count = 3)
    })
    @Test
    public void testIncorrectLanguagesScripts() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractExecutableReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory(), getTessDataDirectory());

        tesseractReader.setPathToScript(getPathToHocrScript());
        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        try {
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata",
                            scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            tesseractReader.setPathToTessData(scriptTessDataDirectory);
            getTextFromPdf(tesseractReader, file,
                    Arrays.<String>asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata",
                            scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            tesseractReader.setPathToTessData(scriptTessDataDirectory);
            getTextFromPdf(tesseractReader, file, new ArrayList<String>());
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
            tesseractReader.setPathToTessData(getTessDataDirectory());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());

        Assert.assertEquals(getTesseractDirectory(),
                tesseractReader.getPathToExecutable());

        Assert.assertEquals(getPathToHocrScript(),
                tesseractReader.getPathToScript());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 1)
    })
    @Test
    public void testCorruptedImageAndCatchException() {
        File file = new File(testImagesDirectory
                + "corrupted.jpg");
        try {
            TesseractExecutableReader tesseractReader = new TesseractExecutableReader(
                    getTesseractDirectory(), getTessDataDirectory());

            String realOutput = getTextFromPdf(tesseractReader, file);
            Assert.assertNotNull(realOutput);
            Assert.assertEquals("", realOutput);
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.CANNOT_READ_PROVIDED_IMAGE,
                            file.getAbsolutePath());
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = OCRException.CANNOT_FIND_PATH_TO_TESSDATA, count = 1),
        @LogMessage(messageTemplate = OCRException.INCORRECT_LANGUAGE, count = 1)
    })
    @Test
    public void testIncorrectPathToTessData() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        TesseractExecutableReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory(),
                "", Collections.<String>singletonList("eng"));

        try {
            Assert.assertEquals("", tesseractReader.getPathToTessData());
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSDATA, e.getMessage());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());

        try {
            tesseractReader.setPathToTessData("/test");
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            "/test");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 2)
    })
    @Test
    public void testIncorrectPathToTesseractExecutable() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        try {
            getTextFromPdf(new TesseractExecutableReader(null,null), file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE,
                    e.getMessage());
        }

        try {
            getTextFromPdf(new TesseractExecutableReader("", ""), file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE,
                    e.getMessage());
        }
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.TESSERACT_FAILED, count = 2)
    })
    @Test
    public void testRunningTesseractCmd() {
        boolean catched = false;
        try {
            TesseractUtil.runCommand(Arrays.<String>asList("tesseract",
                    "random.jpg"), false);
        } catch (OCRException e) {
            catched = true;
        }

        Assert.assertTrue(catched);
        catched = false;
        try {
            TesseractUtil.runCommand(null, false);
        } catch (OCRException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
    }
}
