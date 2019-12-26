package com.itextpdf.ocr;

import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Category(IntegrationTest.class)
public class BasicTesseractLibIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractReader tesseractReader = new TesseractLibReader();
        getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                    Arrays.asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata", langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                    Collections.singletonList("spa_new"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata", langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                Collections.singletonList("Georgian"));
        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    Collections.singletonList("English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    Arrays.asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    new ArrayList<>());
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    @Test
    public void testIncorrectPathToTessData() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        TesseractReader tesseractReader = new TesseractLibReader();
        try {
            getTextFromPdf(tesseractReader, file, "test/",
                    Collections.singletonList("eng"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata", "test/");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            "src/main/resources/com/itextpdf/ocr/tessdata");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }
}
