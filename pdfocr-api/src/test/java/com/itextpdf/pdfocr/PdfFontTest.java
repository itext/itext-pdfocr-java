package com.itextpdf.pdfocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfTestUtils;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class PdfFontTest extends PdfTest {

    @Test
    public void testFontColor() throws IOException {
        String testName = "testFontColor";
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.CYAN;
        properties.setTextColor(color);

        createPdf(pdfPath, file, properties);

        ExtractionStrategy strategy = getExtractionStrategy(pdfPath, "Text1");
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
        String path = getImagesTestDirectory() + "numbers_01.jpg";
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setFontPath("font.ttf");
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);

        createPdf(pdfPath, file, properties);
        String result = getTextFromPdfLayer(pdfPath, "Text Layer");
        Assert.assertEquals(expectedOutput, result);
        Assert.assertEquals(ScaleMode.SCALE_TO_FIT, properties.getScaleMode());
    }

    @Test
    public void testDefaultFontInPdfARgb() throws IOException {
        String testName = "testDefaultFontInPdf";
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK),
                getRGBPdfOutputIntent());
        ExtractionStrategy strategy = getExtractionStrategy(pdfPath);

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
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setFontPath(path),
                getCMYKPdfOutputIntent());

        ExtractionStrategy strategy = getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @Test
    public void testCustomFontInPdf() throws IOException {
        String testName = "testDefaultFontInPdf";
        String imgPath = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(imgPath);

        createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setFontPath(getFreeSansFontPath()),
                getCMYKPdfOutputIntent());

        ExtractionStrategy strategy = getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
    }
}
