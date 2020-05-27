package com.itextpdf.pdfocr;

import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class PdfInputImageTest extends ExtendedITextTest {
    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE,
                    count = 1)
    })
    @Test
    public void testCorruptedImage() {
        junitExpectedException.expect(OcrException.class);
        File file = new File(PdfHelper.getImagesTestDirectory()
                + "corrupted.jpg");
        String realOutput = PdfHelper.getTextFromPdf(file, "testCorruptedImage");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 1)
    })
    @Test
    public void testCorruptedImageWithoutExtension() {
        junitExpectedException.expect(OcrException.class);

        File file = new File(PdfHelper.getImagesTestDirectory()
                + "corrupted");
        String realOutput = PdfHelper.getTextFromPdf(file, "testCorruptedImageWithoutExtension");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }
}
