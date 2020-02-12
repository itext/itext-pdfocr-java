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
public class BasicTesseractLibIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractReader tesseractReader = new TesseractLibReader(getTessDataDirectory(),
                Collections.singletonList("spa"));
        getTextFromPdf(tesseractReader, file, Collections.singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, Arrays.asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata", langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, Collections.singletonList("spa_new"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "spa_new.traineddata", langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        getTextFromPdf(tesseractReader, file, Collections.singletonList("Georgian"));
        try {
            getTextFromPdf(tesseractReader, file, Collections.singletonList("English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, Arrays.asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "English.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, new ArrayList<>());
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata", scriptTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
            tesseractReader.setPathToTessData(getTessDataDirectory());
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
