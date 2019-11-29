package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfa.PdfAConformanceException;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfA3UIntegrationTest extends AbstractIntegrationTest {

    public final String testFontPath = "src/test/resources/com/itextpdf/ocr/PTSans_bold.ttf";

    @Test
    public void testPdfA3uWithoutIntentException() throws IOException {
        String path = testDirectory + "example_01.BMP";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        try {
            File file = new File(path);

            IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.singletonList(file));

            PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), true);
        } catch (Exception e) {
            deleteFile(pdfPath);
            Assert.assertEquals(Exception.OUTPUT_INTENT_CANNOT_BE_NULL, e.getMessage());
        }
    }

    @Test
    public void testPdfA3uWithNullIntentException() throws IOException {
        String path = testDirectory + "example_01.BMP";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        try {
            File file = new File(path);
            IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.singletonList(file));

            PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath),
                    true, null);
        } catch (Exception e) {
            deleteFile(pdfPath);
            Assert.assertEquals(Exception.OUTPUT_INTENT_CANNOT_BE_NULL, e.getMessage());
        }
    }

    @Test
    public void testNotPdfA3uWithIntent() throws IOException {
        String path = testDirectory + "numbers_02.jpg";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        // PdfA3u should not be created as 'createdPdfA3u' flag is false
        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), false, getCMYKPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertNotEquals(PdfAConformanceLevel.PDF_A_3U, pdfDocument.getReader().getPdfAConformanceLevel());

        pdfDocument.close();
        deleteFile(pdfPath);
    }

    @Test
    public void testIncompatibleOutputIntentAndFontColorSpaceException() throws IOException {
        String path = testDirectory + "example_01.BMP";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        try {
            File file = new File(path);
            IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.singletonList(file), DeviceCmyk.BLACK);

            PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath),
                    true, getRGBPdfOutputIntent());
            Assert.assertNotNull(doc);
            doc.close();
        } catch (com.itextpdf.kernel.PdfException e) {
            deleteFile(pdfPath);
            Assert.assertEquals(PdfAConformanceException.DEVICECMYK_MAY_BE_USED_ONLY_IF_THE_FILE_HAS_A_CMYK_PDFA_OUTPUT_INTENT_OR_DEFAULTCMYK_IN_USAGE_CONTEXT, e.getMessage());
        }
    }

    @Test
    public void testDefaultFontInPdf() throws IOException {
        String path = testDirectory + "example_01.BMP";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file), DeviceRgb.BLACK);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), true, getRGBPdfOutputIntent());
        Assert.assertNotNull(doc);
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
    public void testCustomFontInPdf() throws IOException {
        String path = testDirectory + "numbers_01.jpg";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        pdfRenderer.setFontPath(testFontPath);
        pdfRenderer.setDefaultFontPath(path);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), true, getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("PTSans-Bold"));
        Assert.assertTrue(font.isEmbedded());
        Assert.assertEquals(testFontPath, pdfRenderer.getFontPath());

        deleteFile(pdfPath);
    }

    @Test
    public void testInvalidCustomFontInPdf() throws IOException {
        String path = testDirectory + "numbers_01.jpg";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        pdfRenderer.setFontPath(path);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), true, getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
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
    public void testInvalidFontTwice() {
        String path = testDirectory + "numbers_01.jpg";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        try{
            File file = new File(path);
            IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
            PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.singletonList(file));
            pdfRenderer.setFontPath(path);
            pdfRenderer.setDefaultFontPath(path);

            PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath),
                    true, getCMYKPdfOutputIntent());

            Assert.assertNotNull(doc);
            doc.close();
        } catch (com.itextpdf.io.IOException | IOException e) {
            deleteFile(pdfPath);
        }
    }

    @Test
    public void testPdfDefaultMetadata() throws IOException {
        String path = testDirectory + "example_04.png";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file), DeviceRgb.BLACK);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), true, getRGBPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        Assert.assertEquals("en-US", pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals("", pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U, pdfDocument.getReader().getPdfAConformanceLevel());

        deleteFile(pdfPath);
    }

    @Test
    public void testPdfCustomMetadata() throws IOException {
        String path = testDirectory + "numbers_02.jpg";
        String pdfPath = testDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        String locale = "nl-BE";
        pdfRenderer.setPdfLang(locale);
        String title = "Title";
        pdfRenderer.setTitle(title);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), true, getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertEquals(locale, pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(title, pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U, pdfDocument.getReader().getPdfAConformanceLevel());

        deleteFile(pdfPath);
    }

    @Test
    public void comparePdfA3uCMYKColorSpaceSpanishJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = testDirectory + filename + "_a3u.pdf";
        String resultPdfPath = testDirectory + filename + "_a3u_created.pdf";

        TesseractReader tesseractReader = new TesseractReader(getTesseractDirectory());
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(new File(testDirectory + filename + ".jpg")));

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(resultPdfPath),
                true, getCMYKPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void comparePdfA3uRGBSpanishJPG() throws IOException, InterruptedException {
        String filename = "spanish_01";
        String expectedPdfPath = testDirectory + filename + "_a3u.pdf";
        String resultPdfPath = testDirectory + filename + "_a3u_created.pdf";

        TesseractReader tesseractReader = new TesseractReader(getTesseractDirectory());
        tesseractReader.setPathToTessData(tessDataDirectory);
        tesseractReader.setLanguages(Collections.singletonList("spa"));

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(new File(testDirectory + filename + ".jpg")));
        pdfRenderer.setFontColor(DeviceRgb.BLACK);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(resultPdfPath),
                true, getRGBPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }
}
