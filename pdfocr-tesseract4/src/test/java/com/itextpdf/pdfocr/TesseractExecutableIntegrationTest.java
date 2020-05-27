package com.itextpdf.pdfocr;

import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class TesseractExecutableIntegrationTest extends AbstractIntegrationTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testNullPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        Tesseract4ExecutableOcrEngine tesseractExecutableReader =
                new Tesseract4ExecutableOcrEngine(
                        new Tesseract4OcrEngineProperties());
        tesseractExecutableReader.setPathToExecutable(null);
        getTextFromPdf(tesseractExecutableReader, file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testEmptyPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(new Tesseract4ExecutableOcrEngine("", new Tesseract4OcrEngineProperties()), file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4LogMessageConstant.COMMAND_FAILED, count = 1),
        @LogMessage(messageTemplate =
                Tesseract4OcrException.TESSERACT_NOT_FOUND, count = 1)
    })
    @Test
    public void testIncorrectPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.TESSERACT_NOT_FOUND);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(new Tesseract4ExecutableOcrEngine("path\\to\\executable\\", new Tesseract4OcrEngineProperties()), file);
    }
}
