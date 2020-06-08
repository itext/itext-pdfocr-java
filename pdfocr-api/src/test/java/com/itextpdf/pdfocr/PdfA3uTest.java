package com.itextpdf.pdfocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.font.FontSelector;
import com.itextpdf.pdfa.PdfAConformanceException;
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
import org.verapdf.gf.model.factory.fonts.FontFactory;

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
        junitExpectedException.expect(com.itextpdf.kernel.PdfException.class);
        junitExpectedException.expectMessage(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT);

        String testName = "testIncompatibleOutputIntentAndFontColorSpaceException";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdfA(pdfPath, new File(path),
                new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.BLACK),
                PdfHelper.getRGBPdfOutputIntent());
    }

    @Test
    public void testPdfA3DefaultMetadata() throws IOException {
        String testName = "testPdfDefaultMetadata";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        PdfHelper.createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK),
                PdfHelper.getRGBPdfOutputIntent());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        Assert.assertEquals("en-US",
                pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals("",
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
        @LogMessage(messageTemplate = OcrException.CANNOT_CREATE_PDF_DOCUMENT, count = 1)
    })
    @Test
    public void testNonCompliantThaiPdfA() throws IOException {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                OcrException.CANNOT_CREATE_PDF_DOCUMENT,
                MessageFormatUtil.format(PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, 3611)));

        String testName = "testNonCompliantThaiPdfA";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdfA(pdfPath, new File(path),
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK),
                PdfHelper.getRGBPdfOutputIntent());
    }

    @Test
    public void testCompliantThaiPdfA() throws IOException {
        String testName = "testCompliantThaiPdfA";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
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
}
