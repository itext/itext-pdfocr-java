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
import com.itextpdf.ocr.OCRException;
import com.itextpdf.ocr.PdfRenderer;
import com.itextpdf.ocr.TesseractReader;
import com.itextpdf.pdfa.PdfAConformanceException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public abstract class PdfA3UIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;
    String parameter;

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
                ScaleMode.scaleToFit);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), null);
        doc.close();

        String result = getTextFromPdfLayer(pdfPath, "Text Layer", 1);
        Assert.assertEquals(expected, result);

        deleteFile(pdfPath);
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException()
            throws IOException {
        String path = testImagesDirectory + "example_01.BMP";

        try {
            File file = new File(path);

            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.<File>singletonList(file), DeviceCmyk.BLACK);

            pdfRenderer.doPdfOcr(getPdfWriter(), getRGBPdfOutputIntent());
        } catch (com.itextpdf.kernel.PdfException e) {
            Assert.assertEquals(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT, e.getMessage());
        }
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

    @Test
    public void testInvalidFontTwice() {
        String path = testImagesDirectory + "numbers_01.jpg";

        try {
            File file = new File(path);

            PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.<File>singletonList(file));
            pdfRenderer.setFontPath(path);

            pdfRenderer.doPdfOcr(getPdfWriter(), getCMYKPdfOutputIntent());
        } catch (OCRException | IOException e) {
            Assert.assertEquals(OCRException.CANNOT_READ_FONT, e.getMessage());
        }
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
            pdfRenderer.setScaleMode(IPdfRenderer.ScaleMode.keepOriginalSize);
            pdfRenderer.setOcrReader(tesseractReader);

            tesseractReader.setTextPositioning(TextPositioning.byWords);
            Assert.assertEquals(tesseractReader, pdfRenderer.getOcrReader());
            PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(resultPdfPath), getCMYKPdfOutputIntent());
            Assert.assertNotNull(doc);
            doc.close();

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    testDocumentsDirectory, "diff_");
        } finally {
            deleteFile(resultPdfPath);
            Assert.assertEquals(TextPositioning.byWords, tesseractReader.getTextPositioning());
            tesseractReader.setTextPositioning(TextPositioning.byLines);
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
            pdfRenderer.setScaleMode(IPdfRenderer.ScaleMode.keepOriginalSize);

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

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));

        String locale = "nl-BE";
        pdfRenderer.setPdfLang(locale);
        String title = "Title";
        pdfRenderer.setTitle(title);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath), getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertEquals(locale,
                pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(title, pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U,
                pdfDocument.getReader().getPdfAConformanceLevel());

        pdfDocument.close();
        deleteFile(pdfPath);
    }
}
