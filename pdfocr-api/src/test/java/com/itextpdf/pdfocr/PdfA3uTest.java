/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

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
import com.itextpdf.kernel.pdf.PdfAConformance;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.pdfa.exceptions.PdfaExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class PdfA3uTest extends ExtendedITextTest {

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
        Assertions.assertEquals(PdfHelper.DEFAULT_TEXT, result);
        Assertions.assertEquals(ScaleMode.SCALE_TO_FIT, properties.getScaleMode());
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException() {
        Exception exception = Assertions.assertThrows(com.itextpdf.kernel.exceptions.PdfException.class, () -> {
            String testName = "testIncompatibleOutputIntentAndFontColorSpaceException";
            String path = PdfHelper.getDefaultImagePath();
            String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

            OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
            ocrPdfCreatorProperties.setPdfLang("en-US");
            ocrPdfCreatorProperties.setTextColor(DeviceCmyk.BLACK);

            PdfHelper.createPdfA(pdfPath, new File(path),
                    ocrPdfCreatorProperties,
                    PdfHelper.getRGBPdfOutputIntent());
        });

        Assertions.assertEquals(
                PdfaExceptionMessageConstant.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT,
                exception.getMessage());
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

        Assertions.assertEquals("en-US",
                pdfDocument.getCatalog().getLang().toString());
        Assertions.assertEquals(null,
                pdfDocument.getDocumentInfo().getTitle());
        Assertions.assertEquals(PdfAConformance.PDF_A_3U,
                pdfDocument.getReader().getPdfConformance().getAConformance());

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
        Assertions.assertEquals(locale,
                pdfDocument.getCatalog().getLang().toString());
        Assertions.assertEquals(title,
                pdfDocument.getDocumentInfo().getTitle());
        Assertions.assertEquals(PdfAConformance.PDF_A_3U,
                pdfDocument.getReader().getPdfConformance().getAConformance());

        pdfDocument.close();
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT, count = 1)
    })
    @Test
    public void testNonCompliantThaiPdfA() throws IOException {
        Exception exception = Assertions.assertThrows(PdfOcrException.class, () -> {
            String testName = "testNonCompliantThaiPdfA";
            String path = PdfHelper.getThaiImagePath();
            String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

            OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
            ocrPdfCreatorProperties.setPdfLang("en-US");
            ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLACK);

            PdfHelper.createPdfA(pdfPath, new File(path),
                    ocrPdfCreatorProperties,
                    PdfHelper.getRGBPdfOutputIntent());
        });

        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                MessageFormatUtil.format(PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, 3611)),
                exception.getMessage());
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
        Assertions.assertEquals(PdfHelper.THAI_TEXT, resultWithActualText);

        String resultWithoutUseActualText = PdfHelper.getTextFromPdfLayer(pdfPath,
                null);
        Assertions.assertEquals(PdfHelper.THAI_TEXT, resultWithoutUseActualText);
        Assertions.assertEquals(resultWithoutUseActualText, resultWithActualText);

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);
        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assertions.assertTrue(fontName.contains("Kanit"));
        Assertions.assertTrue(font.isEmbedded());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT, count = 1)
    })
    @Test
    public void testPdfACreateWithoutPdfLangProperty() {
        Exception exception = Assertions.assertThrows(PdfOcrException.class, () -> {
            String testName = "testPdfACreateWithoutPdfLangProperty";
            String path = PdfHelper.getThaiImagePath();
            String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

            PdfHelper.createPdfA(pdfPath, new File(path),
                    new OcrPdfCreatorProperties(),
                    PdfHelper.getRGBPdfOutputIntent());
        });

        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                PdfOcrLogMessageConstant.PDF_LANGUAGE_PROPERTY_IS_NOT_SET), exception.getMessage());
    }
}
