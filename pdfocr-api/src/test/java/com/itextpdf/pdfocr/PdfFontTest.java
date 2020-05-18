package com.itextpdf.pdfocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.helpers.TestDirectoryUtils;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfFontTest extends ExtendedITextTest {

    @Test
    public void testFontColor() throws IOException {
        String testName = "testFontColor";
        String path = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.CYAN;
        properties.setTextColor(color);

        PdfHelper.createPdf(pdfPath, file, properties);

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath, "Text1");
        com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
        Assert.assertEquals(color, fillColor);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.CannotReadProvidedFont, count = 1)
    })
    @Test
    public void testInvalidFont() throws IOException {
        String testName = "testImageWithoutText";
        String expectedOutput = "619121";
        String path = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setFontPath("font.ttf");
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);

        PdfHelper.createPdf(pdfPath, file, properties);
        String result = PdfHelper.getTextFromPdfLayer(pdfPath, "Text Layer");
        Assert.assertEquals(expectedOutput, result);
        Assert.assertEquals(ScaleMode.SCALE_TO_FIT, properties.getScaleMode());
    }

    @Test
    public void testDefaultFontInPdfARgb() throws IOException {
        String testName = "testDefaultFontInPdf";
        String path = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        PdfHelper.createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK),
                PdfHelper.getRGBPdfOutputIntent());
        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = com.itextpdf.io.IOException.TypeOfFontIsNotRecognized, count = 1)
    })
    @Test
    public void testInvalidCustomFontInPdfACMYK() throws IOException {
        String testName = "testInvalidCustomFontInPdf";
        String path = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        PdfHelper.createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setFontPath(path),
                PdfHelper.getCMYKPdfOutputIntent());

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @Test
    public void testCustomFontInPdf() throws IOException {
        String testName = "testDefaultFontInPdf";
        String imgPath = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(imgPath);

        PdfHelper.createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties()
                        .setFontPath(TestDirectoryUtils.getFreeSansFontPath()),
                PdfHelper.getCMYKPdfOutputIntent());

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
    }
}
