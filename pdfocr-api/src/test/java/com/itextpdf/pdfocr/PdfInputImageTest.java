package com.itextpdf.pdfocr;

import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PdfInputImageTest extends PdfTest {
    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @LogMessages(messages = {
            @LogMessage(messageTemplate = LogMessageConstant.CannotReadInputImage,
                    count = 1)
    })
    @Test
    public void testCorruptedImage() {
        junitExpectedException.expect(OcrException.class);
        File file = new File(getImagesTestDirectory()
                + "corrupted.jpg");
        String realOutput = getTextFromPdf(file, "testCorruptedImage");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = LogMessageConstant.CannotReadInputImage, count = 1)
    })
    @Test
    public void testCorruptedImageWithoutExtension() {
        junitExpectedException.expect(OcrException.class);

        File file = new File(getImagesTestDirectory()
                + "corrupted");
        String realOutput = getTextFromPdf(file, "testCorruptedImageWithoutExtension");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }
}
