package com.itextpdf.ocr;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Category(IntegrationTest.class)
public class PdfA3UIntegrationTest extends AbstractIntegrationTest {

    public final String testFontPath = "src/test/resources/com/itextpdf/ocr/PTSans_bold.ttf";

    /*@Test
    public void testDefaultFontInPdf() throws IOException {
        String path = directory + "example_01.BMP";
        String pdfPath = directory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), createPdfOutputIntent());

        assert doc != null;
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("PTSans-Regular"));
        Assert.assertTrue(font.isEmbedded());

        String realOutputHocr = getTextFromPdfFile(file);

        deleteFile(pdfPath);
    }

    @Test
    public void testCustomFontInPdf() throws IOException {
        String path = directory + "numbers_01.jpg";
        String pdfPath = directory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        pdfRenderer.setFontPath(testFontPath);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), createPdfOutputIntent());

        assert doc != null;
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("PTSans-Bold"));
        Assert.assertTrue(font.isEmbedded());

        deleteFile(pdfPath);
    }

    @Test
    public void testInvalidCustomFontInPdf() throws IOException {
        String path = directory + "numbers_01.jpg";
        String pdfPath = directory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        pdfRenderer.setFontPath(path);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), createPdfOutputIntent());

        assert doc != null;
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("PTSans-Regular"));
        Assert.assertTrue(font.isEmbedded());

        deleteFile(pdfPath);
    }

    @Test
    public void testPdfDefaultMetadata() throws IOException {
        String path = directory + "numbers_01.jpg";
        String pdfPath = directory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), createPdfOutputIntent());

        assert doc != null;
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        Assert.assertEquals("en-US", pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals("", pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U, pdfDocument.getReader().getPdfAConformanceLevel());

        deleteFile(pdfPath);
    }

    @Test
    public void testPdfCustomMetadata() throws IOException {
        String path = directory + "numbers_01.jpg";
        String pdfPath = directory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        String locale = "nl-BE";
        pdfRenderer.setPdfLang(locale);
        String title = "Title";
        pdfRenderer.setTitle(title);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), createPdfOutputIntent());

        assert doc != null;
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertEquals(locale, pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(title, pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U, pdfDocument.getReader().getPdfAConformanceLevel());

        deleteFile(pdfPath);
    }*/

}
