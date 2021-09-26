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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.pdfa.exceptions.PdfAConformanceException;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
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
public class PdfA3uTest extends ExtendedITextTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @Test
    public void testPdfA3uWithNullIntent() throws IOException {
        String testName = "testPdfA3uWithNullIntent";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.BLACK);
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);

        PdfHelper.createPdfA(pdfPath, new File(path), properties, null);
        String result = PdfHelper.getTextFromPdfLayer(pdfPath, null);
        Assert.assertEquals(PdfHelper.DEFAULT_TEXT, result);
        Assert.assertEquals(ScaleMode.SCALE_TO_FIT, properties.getScaleMode());
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException()
            throws IOException {
        junitExpectedException.expect(com.itextpdf.kernel.exceptions.PdfException.class);
        junitExpectedException.expectMessage(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT);

        String testName = "testIncompatibleOutputIntentAndFontColorSpaceException";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTextColor(DeviceCmyk.BLACK);

        PdfHelper.createPdfA(pdfPath, new File(path),
                ocrPdfCreatorProperties,
                PdfHelper.getRGBPdfOutputIntent());
    }

    @Test
    public void testPdfA3DefaultMetadata() throws IOException {
        String testName = "testPdfDefaultMetadata";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLACK);

        PdfHelper.createPdfA(pdfPath, file,
                ocrPdfCreatorProperties,
                PdfHelper.getRGBPdfOutputIntent());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        Assert.assertEquals("en-US",
                pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(null,
                pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U,
                pdfDocument.getReader().getPdfAConformanceLevel());

        pdfDocument.close();
    }

    @Test
    public void testPdfCustomMetadata() throws IOException {
        String testName = "testPdfCustomMetadata";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        String locale = "nl-BE";
        properties.setPdfLang(locale);
        String title = "Title";
        properties.setTitle(title);

        PdfHelper.createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties(properties),
                PdfHelper.getCMYKPdfOutputIntent());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertEquals(locale,
                pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(title,
                pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U,
                pdfDocument.getReader().getPdfAConformanceLevel());

        pdfDocument.close();
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT, count = 1)
    })
    @Test
    public void testNonCompliantThaiPdfA() throws IOException {
        junitExpectedException.expect(PdfOcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                MessageFormatUtil.format(PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, 3611)));

        String testName = "testNonCompliantThaiPdfA";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLACK);

        PdfHelper.createPdfA(pdfPath, new File(path),
                ocrPdfCreatorProperties,
                PdfHelper.getRGBPdfOutputIntent());
    }

    @Test
    public void testCompliantThaiPdfA() throws IOException {
        String testName = "testCompliantThaiPdfA";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLACK);

        FontProvider fontProvider = new FontProvider("Kanit");
        fontProvider.addFont(PdfHelper.getKanitFontPath());
        PdfOcrFontProvider pdfOcrFontProvider = new PdfOcrFontProvider(
                fontProvider.getFontSet(), "Kanit");
        ocrPdfCreatorProperties.setFontProvider(pdfOcrFontProvider);

        PdfHelper.createPdfA(pdfPath, new File(path), ocrPdfCreatorProperties,
                PdfHelper.getRGBPdfOutputIntent());

        String resultWithActualText = PdfHelper
                .getTextFromPdfLayerUseActualText(pdfPath, null);
        Assert.assertEquals(PdfHelper.THAI_TEXT, resultWithActualText);

        String resultWithoutUseActualText = PdfHelper.getTextFromPdfLayer(pdfPath,
                null);
        Assert.assertEquals(PdfHelper.THAI_TEXT, resultWithoutUseActualText);
        Assert.assertEquals(resultWithoutUseActualText, resultWithActualText);

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("Kanit"));
        Assert.assertTrue(font.isEmbedded());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT, count = 1)
    })
    @Test
    public void testPdfACreateWithoutPdfLangProperty()
            throws IOException {
        junitExpectedException.expect(PdfOcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                PdfOcrLogMessageConstant.PDF_LANGUAGE_PROPERTY_IS_NOT_SET));

        String testName = "testPdfACreateWithoutPdfLangProperty";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdfA(pdfPath, new File(path),
                new OcrPdfCreatorProperties(),
                PdfHelper.getRGBPdfOutputIntent());
    }
}
