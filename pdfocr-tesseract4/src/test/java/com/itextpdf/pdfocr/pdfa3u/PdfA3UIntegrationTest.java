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
}
