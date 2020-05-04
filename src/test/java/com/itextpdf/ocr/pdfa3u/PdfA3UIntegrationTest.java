package com.itextpdf.ocr.pdfa3u;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.ocr.AbstractIntegrationTest;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
import com.itextpdf.ocr.IPdfRenderer;
import com.itextpdf.ocr.IPdfRenderer.ScaleMode;
import com.itextpdf.ocr.LogMessageConstant;
import com.itextpdf.ocr.OCRException;
import com.itextpdf.ocr.PdfRenderer;
import com.itextpdf.ocr.TesseractReader;
import com.itextpdf.pdfa.PdfAConformanceException;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class PdfA3UIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;
    String parameter;

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    public PdfA3UIntegrationTest(String type) {
        parameter = type;
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testPdfA3uWithNullIntent() throws IOException {
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        File file = new File(imgPath);
        String expected = "619121";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file), DeviceCmyk.BLACK,
                ScaleMode.SCALE_TO_FIT);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), null);
        doc.close();

        String result = getTextFromPdfLayer(pdfPath, "Text Layer", 1);
        Assert.assertEquals(expected, result);

        deleteFile(pdfPath);
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException()
            throws IOException {
        junitExpectedException.expect(com.itextpdf.kernel.PdfException.class);
        junitExpectedException.expectMessage(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT);

        String path = testImagesDirectory + "example_01.BMP";
        File file = new File(path);
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file), DeviceCmyk.BLACK);
        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(),
                getRGBPdfOutputIntent());
        doc.close();
    }

    @Test
    public void testDefaultFontInPdf() throws IOException {
        String path = testImagesDirectory + "example_01.BMP";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file), DeviceRgb.BLACK);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), getRGBPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy(
                "Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());

        pdfDocument.close();
        deleteFile(pdfPath);
    }

    @Test
    public void testCustomFontInPdf() throws IOException {
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(imgPath);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        pdfRenderer.setFontPath(freeSansFontPath);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy(
                "Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("FreeSans"));
        Assert.assertTrue(font.isEmbedded());
        Assert.assertEquals(freeSansFontPath, pdfRenderer.getFontPath());

        pdfDocument.close();
        deleteFile(pdfPath);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = com.itextpdf.io.IOException.TypeOfFontIsNotRecognized, count = 1)
    })
    @Test
    public void testInvalidCustomFontInPdf() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        pdfRenderer.setFontPath(path);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy(
                "Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
        Assert.assertTrue(font.isEmbedded());

        pdfDocument.close();
        deleteFile(pdfPath);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.CANNOT_READ_PROVIDED_FONT, count = 1)
    })
    @Test
    public void testInvalidFont() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        File file = new File(path);
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        pdfRenderer.setFontPath(path);
        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(), getCMYKPdfOutputIntent());
        doc.close();
    }

    @Test
    public void testPdfDefaultMetadata() throws IOException {
        String path = testImagesDirectory + "example_04.png";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file), DeviceRgb.BLACK);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), getRGBPdfOutputIntent());

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
        deleteFile(pdfPath);
    }

    @Test
    public void comparePdfA3uCMYKColorSpaceSpanishJPG() throws IOException,
            InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = testDocumentsDirectory + filename + "_a3u.pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_a3u_created.pdf";

        try {
            PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.<File>singletonList(
                            new File(testImagesDirectory
                                    + filename + ".jpg")));
            pdfRenderer.setScaleMode(IPdfRenderer.ScaleMode.KEEP_ORIGINAL_SIZE);
            pdfRenderer.setOcrReader(tesseractReader);

            tesseractReader.setTextPositioning(TextPositioning.BY_WORDS);
            Assert.assertEquals(tesseractReader, pdfRenderer.getOcrReader());
            PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(resultPdfPath), getCMYKPdfOutputIntent());
            Assert.assertNotNull(doc);
            doc.close();

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    testDocumentsDirectory, "diff_");
        } finally {
            deleteFile(resultPdfPath);
            Assert.assertEquals(TextPositioning.BY_WORDS, tesseractReader.getTextPositioning());
            tesseractReader.setTextPositioning(TextPositioning.BY_LINES);
        }
    }

    @Test
    public void comparePdfA3uRGBSpanishJPG()
            throws IOException, InterruptedException {
        String filename = "spanish_01";
        String expectedPdfPath = testDocumentsDirectory + filename + "_a3u.pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_a3u_created.pdf";

        tesseractReader.setPathToTessData(langTessDataDirectory);
        tesseractReader.setLanguages(Collections.<String>singletonList("spa"));

        try {
            PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.<File>singletonList(
                            new File(testImagesDirectory + filename
                                    + ".jpg")));
            pdfRenderer.setTextColor(DeviceRgb.BLACK);
            pdfRenderer.setScaleMode(IPdfRenderer.ScaleMode.KEEP_ORIGINAL_SIZE);

            PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(resultPdfPath),
                    getRGBPdfOutputIntent());
            Assert.assertNotNull(doc);
            doc.close();

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    testDocumentsDirectory, "diff_");
        } finally {
            deleteFile(resultPdfPath);
        }
    }

    @Test
    public void testPdfCustomMetadata() throws IOException {
        String path = testImagesDirectory + "numbers_02.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        try {
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.<File>singletonList(file));

            String locale = "nl-BE";
            pdfRenderer.setPdfLang(locale);
            String title = "Title";
            pdfRenderer.setTitle(title);

            PdfDocument doc = pdfRenderer
                    .doPdfOcr(getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

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
        } finally {
            deleteFile(pdfPath);
        }
    }
}
