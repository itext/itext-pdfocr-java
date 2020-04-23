package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class TesseractLibIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractReader tesseractReader = new TesseractLibReader(getTessDataDirectory(),
                Collections.<String>singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, Arrays.<String>asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata", langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("spa_new"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata", langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    @Test
    public void testIncorrectLanguagesScript() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractReader tesseractReader = new TesseractLibReader(scriptTessDataDirectory,
                Collections.<String>singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, Arrays.<String>asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, new ArrayList<String>());
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
            tesseractReader.setPathToTessData(getTessDataDirectory());
        }
    }

    @Test
    public void testCorruptedImageAndCatchException() {
        try {
            File file = new File(testImagesDirectory
                    + "corrupted.jpg");
            TesseractReader tesseractReader = new TesseractLibReader(getTessDataDirectory());

            String realOutput = getTextFromPdf(tesseractReader, file);
            Assert.assertNotNull(realOutput);
            Assert.assertEquals("", realOutput);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_READ_INPUT_IMAGE,
                    e.getMessage());
        }
    }

    @Test
    public void testTextFromJPE() {
        String path = testImagesDirectory + "numbers_01.jpe";
        String expectedOutput = "619121";

        TesseractReader tesseractReader = new TesseractLibReader(getTessDataDirectory());
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }
}
