package com.itextpdf.pdfocr.pdfa3u;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfa.PdfAConformanceException;
import com.itextpdf.pdfocr.LogMessageConstant;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.PdfRenderer;
import com.itextpdf.pdfocr.ScaleMode;
import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class PdfA3UIntegrationTest extends AbstractIntegrationTest {

    Tesseract4OcrEngine tesseractReader;

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    public PdfA3UIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testPdfA3uWithNullIntent() throws IOException {
        String testName = "testPdfA3uWithNullIntent";
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        File file = new File(imgPath);
        String expected = "619121";
        String pdfPath = testImagesDirectory + testName + ".pdf";

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextColor(DeviceCmyk.BLACK);
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);

        pdfRenderer.createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath), null).close();

        String result = getTextFromPdfLayer(pdfPath, "Text Layer", 1);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException()
            throws IOException {
        junitExpectedException.expect(com.itextpdf.kernel.PdfException.class);
        junitExpectedException.expectMessage(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT);

        String path = testImagesDirectory + "example_01.BMP";
        File file = new File(path);
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.BLACK));
        PdfDocument doc = pdfRenderer.createPdfA(
                        Collections.<File>singletonList(file),
                        getPdfWriter(), getRGBPdfOutputIntent());
        doc.close();
    }

    @Test
    public void testDefaultFontInPdf() throws IOException {
        String testName = "testDefaultFontInPdf";
        String path = testImagesDirectory + "example_01.BMP";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK));

        PdfDocument doc =
                pdfRenderer.createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath), getRGBPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy(
                "Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @Test
    public void testCustomFontInPdf() throws IOException {
        String testName = "testDefaultFontInPdf";
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(imgPath);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setFontPath(freeSansFontPath));
        PdfDocument doc =
                pdfRenderer.createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy(
                "Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
        Assert.assertEquals(freeSansFontPath,
                pdfRenderer.getOcrPdfCreatorProperties().getFontPath());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = com.itextpdf.io.IOException.TypeOfFontIsNotRecognized, count = 1)
    })
    @Test
    public void testInvalidCustomFontInPdf() throws IOException {
        String testName = "testInvalidCustomFontInPdf";
        String path = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setFontPath(path));

        PdfDocument doc =
                pdfRenderer.createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy(
                "Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.CannotReadProvidedFont, count = 1)
    })
    @Test
    public void testInvalidFont() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        File file = new File(path);
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setFontPath(path));
        PdfDocument doc =
                pdfRenderer.createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(), getCMYKPdfOutputIntent());
        doc.close();
    }

    @Test
    public void testPdfDefaultMetadata() throws IOException {
        String testName = "testPdfDefaultMetadata";
        String path = testImagesDirectory + "example_04.png";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK));

        PdfDocument doc =
                pdfRenderer.createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath), getRGBPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

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
    public void comparePdfA3uCMYKColorSpaceSpanishJPG() throws IOException,
            InterruptedException {
        String testName = "comparePdfA3uCMYKColorSpaceSpanishJPG";
        String filename = "numbers_01";
        String expectedPdfPath = testDocumentsDirectory + filename + "_a3u.pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_" + testName + "_a3u_created.pdf";

        try {
            PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);

            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setTextPositioning(TextPositioning.BY_WORDS));
            Assert.assertEquals(tesseractReader, pdfRenderer.getOcrReader());
            pdfRenderer.setOcrReader(tesseractReader);
            PdfDocument doc =
                    pdfRenderer.createPdfA(
                            Collections.<File>singletonList(
                            new File(testImagesDirectory
                                    + filename + ".jpg")),
                            getPdfWriter(resultPdfPath),
                            getCMYKPdfOutputIntent());
            Assert.assertNotNull(doc);
            doc.close();

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    testDocumentsDirectory, "diff_");
        } finally {
            Assert.assertEquals(TextPositioning.BY_WORDS,
                    tesseractReader.getTesseract4OcrEngineProperties().getTextPositioning());
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setTextPositioning(TextPositioning.BY_LINES));
        }
    }

    @Test
    public void comparePdfA3uRGBSpanishJPG()
            throws IOException, InterruptedException {
        String testName = "comparePdfA3uRGBSpanishJPG";
        String filename = "spanish_01";
        String expectedPdfPath = testDocumentsDirectory + filename + "_a3u.pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_" + testName + "_a3u_created.pdf";

        Tesseract4OcrEngineProperties properties =
                new Tesseract4OcrEngineProperties(tesseractReader.getTesseract4OcrEngineProperties());
        properties.setPathToTessData(langTessDataDirectory);
        properties.setLanguages(Collections.<String>singletonList("spa"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK));

        PdfDocument doc = pdfRenderer.createPdfA(
                Collections.<File>singletonList(
                        new File(testImagesDirectory + filename
                                + ".jpg")), getPdfWriter(resultPdfPath),
                getRGBPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");
    }

    @Test
    public void testPdfCustomMetadata() throws IOException {
        String testName = "testPdfCustomMetadata";
        String path = testImagesDirectory + "numbers_02.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        String locale = "nl-BE";
        properties.setPdfLang(locale);
        String title = "Title";
        properties.setTitle(title);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties(properties));
        PdfDocument doc = pdfRenderer
                .createPdfA(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

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
