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
                OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testNullPathToTesseractExecutable() {
        junitExpectedException.expect(OCRException.class);
        junitExpectedException.expectMessage(OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(new TesseractExecutableReader(null,null), file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testEmptyPathToTesseractExecutable() {
        junitExpectedException.expect(OCRException.class);
        junitExpectedException.expectMessage(OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(new TesseractExecutableReader("", ""), file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.TESSERACT_FAILED,
                count = 1)
    })
    @Test
    public void testCLTesseractWithWrongCommand() {
        junitExpectedException.expect(OCRException.class);
        TesseractUtil.runCommand(Arrays.<String>asList("tesseract",
                "random.jpg"), false);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.TESSERACT_FAILED, count = 1)
    })
    @Test
    public void testCLTesseractWithNullCommand() {
        junitExpectedException.expect(OCRException.class);
        TesseractUtil.runCommand(null, false);
    }
}
