/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class PdfFontTest extends ExtendedITextTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

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
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.PROVIDED_FONT_PROVIDER_IS_INVALID, count = 1),
        @LogMessage(messageTemplate = OcrException.CANNOT_CREATE_PDF_DOCUMENT, count = 1)
    })
    @Test
    public void testInvalidFontWithInvalidDefaultFontFamily()
            throws IOException {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                OcrException.CANNOT_CREATE_PDF_DOCUMENT,
                OcrException.CANNOT_RESOLVE_PROVIDED_FONTS));

        String testName = "testInvalidFontWithInvalidDefaultFontFamily";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        FontProvider pdfOcrFontProvider = new FontProvider("Font");
        pdfOcrFontProvider.getFontSet().addFont("font.ttf", PdfEncodings.IDENTITY_H, "Font");

        properties.setFontProvider(pdfOcrFontProvider, "Font");
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

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLACK);

        PdfHelper.createPdfA(pdfPath, file,
                ocrPdfCreatorProperties,
                PdfHelper.getRGBPdfOutputIntent());
        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @Test
    public void testInvalidCustomFontInPdfACMYK() throws IOException {
        String testName = "testInvalidCustomFontInPdf";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setFontProvider(new PdfOcrFontProvider());

        PdfHelper.createPdfA(pdfPath, file,
                ocrPdfCreatorProperties,
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

        FontProvider fontProvider = new FontProvider("FreeSans");
        fontProvider.getFontSet().addFont(PdfHelper.getFreeSansFontPath(), PdfEncodings.IDENTITY_H, "FreeSans");

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setFontProvider(fontProvider, "FreeSans");

        PdfHelper.createPdfA(pdfPath, file,
                ocrPdfCreatorProperties,
                PdfHelper.getCMYKPdfOutputIntent());

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 7)
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

    @Test
    public void testReusingFontProvider() throws IOException {
        String testName = "testReusingFontProvider";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPathA3u = PdfHelper.getTargetDirectory() + testName + "_a3u.pdf";
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        FontProvider fontProvider = new FontProvider("FreeSans");
        fontProvider.addFont(PdfHelper.getFreeSansFontPath());
        PdfOcrFontProvider pdfOcrFontProvider = new PdfOcrFontProvider(
                fontProvider.getFontSet(), "FreeSans");

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setFontProvider(pdfOcrFontProvider);

        PdfHelper.createPdfA(pdfPathA3u, file, ocrPdfCreatorProperties,
                PdfHelper.getCMYKPdfOutputIntent());

        PdfHelper.createPdf(pdfPath, file, ocrPdfCreatorProperties);

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPathA3u);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
        Assert.assertEquals(PdfHelper.DEFAULT_TEXT, strategy.getResultantText());

        strategy = PdfHelper.getExtractionStrategy(pdfPath);
        font = strategy.getPdfFont();
        fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
        Assert.assertEquals(PdfHelper.DEFAULT_TEXT, strategy.getResultantText());
    }
}
