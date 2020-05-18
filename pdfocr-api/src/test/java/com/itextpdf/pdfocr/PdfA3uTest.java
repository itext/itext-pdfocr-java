package com.itextpdf.pdfocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.pdfa.PdfAConformanceException;
import com.itextpdf.pdfocr.helpers.PdfTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PdfA3uTest extends PdfTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @Test
    public void testPdfA3uWithNullIntent() throws IOException {
        String testName = "testPdfA3uWithNullIntent";
        String imgPath = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.BLACK);
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);

        createPdfA(pdfPath, new File(imgPath), properties, null);
        String result = getTextFromPdfLayer(pdfPath, "Text Layer");
        Assert.assertEquals(DEFAULT_EXPECTED_RESULT, result);
        Assert.assertEquals(ScaleMode.SCALE_TO_FIT, properties.getScaleMode());
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException()
            throws IOException {
        junitExpectedException.expect(com.itextpdf.kernel.PdfException.class);
        junitExpectedException.expectMessage(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT);

        String testName = "testIncompatibleOutputIntentAndFontColorSpaceException";
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";

        createPdfA(pdfPath, new File(path),
                new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.BLACK),
                getRGBPdfOutputIntent());
    }

    @Test
    public void testPdfA3DefaultMetadata() throws IOException {
        String testName = "testPdfDefaultMetadata";
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK),
                getRGBPdfOutputIntent());

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
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        String locale = "nl-BE";
        properties.setPdfLang(locale);
        String title = "Title";
        properties.setTitle(title);

        createPdfA(pdfPath, file,
                new OcrPdfCreatorProperties(properties),
                getCMYKPdfOutputIntent());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertEquals(locale,
                pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(title,
                pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U,
                pdfDocument.getReader().getPdfAConformanceLevel());

        pdfDocument.close();
    }
}
