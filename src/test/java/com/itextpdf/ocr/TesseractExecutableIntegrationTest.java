package com.itextpdf.ocr;

import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.util.Arrays;
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
                OcrException.CannotFindPathToTesseractExecutable, count = 1)
    })
    @Test
    public void testNullPathToTesseractExecutable() {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(OcrException.CannotFindPathToTesseractExecutable);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        TesseractExecutableReader tesseractExecutableReader =
                new TesseractExecutableReader(null);
        tesseractExecutableReader.setPathToExecutable(null);
        getTextFromPdf(tesseractExecutableReader, file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                OcrException.CannotFindPathToTesseractExecutable, count = 1)
    })
    @Test
    public void testEmptyPathToTesseractExecutable() {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(OcrException.CannotFindPathToTesseractExecutable);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(new TesseractExecutableReader("", ""), file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.TesseractFailed,
                count = 1)
    })
    @Test
    public void testCLTesseractWithWrongCommand() {
        junitExpectedException.expect(OcrException.class);
        TesseractUtil.runCommand(Arrays.<String>asList("tesseract",
                "random.jpg"), false);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.TesseractFailed, count = 1)
    })
    @Test
    public void testCLTesseractWithNullCommand() {
        junitExpectedException.expect(OcrException.class);
        TesseractUtil.runCommand(null, false);
    }
}
