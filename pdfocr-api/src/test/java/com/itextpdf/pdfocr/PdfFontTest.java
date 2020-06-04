package com.itextpdf.pdfocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
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
        String path = PdfHelper.getImagesTestDirectory() + "multipage.tiff";
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);
        properties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.CYAN;
        properties.setTextColor(color);

        PdfHelper.createPdf(pdfPath, file, properties);

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath, "Text1");
        com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
        Assert.assertEquals(color, fillColor);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_PROVIDED_FONT, count = 1)
    })
    @Test
    public void testInvalidFont() throws IOException {
        String testName = "testImageWithoutText";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setFontPath("font.ttf");
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);

        PdfHelper.createPdf(pdfPath, file, properties);
        String result = PdfHelper.getTextFromPdfLayer(pdfPath, null);
        Assert.assertEquals(PdfHelper.DEFAULT_TEXT, result);
        Assert.assertEquals(ScaleMode.SCALE_TO_FIT, properties.getScaleMode());
    }

    @Test
    public void testDefaultFontInPdfARgb() throws IOException {
        String testName = "testDefaultFontInPdf";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
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
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
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
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        PdfHelper.createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties()
                        .setFontPath(PdfHelper.getFreeSansFontPath()),
                PdfHelper.getCMYKPdfOutputIntent());

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 1)
    })
    @Test
    public void testThaiImageWithNotDefGlyphs() throws IOException {
        String testName = "testThaiImageWithNotDefGlyphs";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdf(pdfPath, new File(path),
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK));

        String resultWithActualText = PdfHelper
                .getTextFromPdfLayerUseActualText(pdfPath, null);
        Assert.assertEquals(PdfHelper.THAI_TEXT.replace(" ", ""),
                resultWithActualText.replace(" ", ""));

        String resultWithoutUseActualText = PdfHelper.getTextFromPdfLayer(pdfPath,
                null);
        Assert.assertNotEquals(PdfHelper.THAI_TEXT, resultWithoutUseActualText);
        Assert.assertNotEquals(resultWithoutUseActualText, resultWithActualText);
    }
}
