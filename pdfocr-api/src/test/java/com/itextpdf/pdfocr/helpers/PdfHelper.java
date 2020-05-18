package com.itextpdf.pdfocr.helpers;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.PdfRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfHelper {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfHelper.class);

    /**
     *
     * Create pdfWriter using provided path to destination file.
     */
    public static PdfWriter getPdfWriter(String pdfPath) throws FileNotFoundException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Create pdfWriter.
     */
    public static PdfWriter getPdfWriter() {
        return new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Creates pdf rgb output intent for tests.
     */
    public static PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        String defaultRGBColorProfilePath =
                TestDirectoryUtils.getCurrentDirectory() + "profiles"
                        + "/sRGB_CS_profile.icm";
        InputStream is = new FileInputStream(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * Creates pdf cmyk output intent for tests.
     */
    public static PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        String defaultCMYKColorProfilePath =
                TestDirectoryUtils.getCurrentDirectory() + "profiles/CoatedFOGRA27"
                        + ".icc";
        InputStream is = new FileInputStream(defaultCMYKColorProfilePath);
        return new PdfOutputIntent("Custom",
                "","http://www.color.org",
                "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * Get text from layer specified by name from the first page.
     */
    public static String getTextFromPdfLayer(String pdfPath,
            String layerName)
            throws IOException {
        ExtractionStrategy textExtractionStrategy = getExtractionStrategy(pdfPath, layerName);
        return textExtractionStrategy.getResultantText();
    }

    /**
     * Perform OCR with custom ocr engine using provided input image and set
     * of properties and save to the given path.
     */
    public static void createPdf(String pdfPath, File inputFile,
            OcrPdfCreatorProperties properties) {
        PdfRenderer pdfRenderer = new PdfRenderer(new CustomOcrEngine(),
                properties);
        try (PdfWriter pdfWriter = getPdfWriter(pdfPath)) {
            pdfRenderer.createPdf(Collections.<File>singletonList(inputFile),
                    pdfWriter).close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Perform OCR with custom ocr engine using provided input image and set
     * of properties and save to the given path.
     */
    public static void createPdfA(String pdfPath, File inputFile,
            OcrPdfCreatorProperties properties, PdfOutputIntent outputIntent) {
        PdfRenderer pdfRenderer = new PdfRenderer(new CustomOcrEngine(),
                properties);
        try (PdfWriter pdfWriter = getPdfWriter(pdfPath)) {
            pdfRenderer.createPdfA(Collections.<File>singletonList(inputFile),
                    pdfWriter, outputIntent).close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Retrieve text from specified page from given pdf document.
     */
    public static String getTextFromPdf(File file, String testName) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";
            createPdf(pdfPath, file, new OcrPdfCreatorProperties());
            result = getTextFromPdfLayer(pdfPath, "Text Layer");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    /**
     * Get extraction strategy for given document.
     */
    public static ExtractionStrategy getExtractionStrategy(String pdfPath)
            throws IOException {
        return getExtractionStrategy(pdfPath, "Text Layer");
    }

    /**
     * Get extraction strategy for given document.
     */
    public static ExtractionStrategy getExtractionStrategy(String pdfPath,
            String layerName)
            throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        ExtractionStrategy strategy = new ExtractionStrategy(layerName);
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();
        return strategy;
    }
}
