package com.itextpdf.pdfocr;

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
                Tesseract4OcrException.CannotFindPathToTesseractExecutable, count = 1)
    })
    @Test
    public void testNullPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CannotFindPathToTesseractExecutable);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        Tesseract4ExecutableOcrEngine tesseractExecutableReader =
                new Tesseract4ExecutableOcrEngine(
                        new Tesseract4OcrEngineProperties());
        tesseractExecutableReader.setPathToExecutable(null);
        getTextFromPdf(tesseractExecutableReader, file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4OcrException.CannotFindPathToTesseractExecutable, count = 1)
    })
    @Test
    public void testEmptyPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CannotFindPathToTesseractExecutable);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(new Tesseract4ExecutableOcrEngine("", new Tesseract4OcrEngineProperties()), file);
    }
}
