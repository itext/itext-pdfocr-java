package com.itextpdf.pdfocr;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfTestUtils;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(IntegrationTest.class)
public class PdfTest extends ExtendedITextTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfTest.class);

    public static final String DEFAULT_IMAGE_NAME = "numbers_01.jpg";
    public static final String DEFAULT_EXPECTED_RESULT = "619121";

    /**
     * Return images test directory.
     *
     * @return String
     */
    protected static String getImagesTestDirectory() {
        return PdfTestUtils.getCurrentDirectory() + "images/";
    }

    protected String getFreeSansFontPath() {
        return PdfTestUtils.getCurrentDirectory() + "fonts/FreeSans.ttf";
    }

    /**
     * Create pdfWriter using provided path to destination file.
     */
    protected PdfWriter getPdfWriter(String pdfPath) throws FileNotFoundException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Create pdfWriter.
     */
    protected PdfWriter getPdfWriter() {
        return new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Creates pdf rgb output intent for tests.
     */
    protected PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        String defaultRGBColorProfilePath =
                PdfTestUtils.getCurrentDirectory() + "profiles"
                        + "/sRGB_CS_profile.icm";
        InputStream is = new FileInputStream(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * Creates pdf cmyk output intent for tests.
     */
    protected  PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        String defaultCMYKColorProfilePath =
                PdfTestUtils.getCurrentDirectory() + "profiles/CoatedFOGRA27"
                        + ".icc";
        InputStream is = new FileInputStream(defaultCMYKColorProfilePath);
        return new PdfOutputIntent("Custom",
                "","http://www.color.org",
                "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * Get text from layer specified by name from the first page.
     */
    protected String getTextFromPdfLayer(String pdfPath, String layerName)
            throws IOException {
        ExtractionStrategy textExtractionStrategy = getExtractionStrategy(pdfPath, layerName);
        return textExtractionStrategy.getResultantText();
    }

    /**
     * Perform OCR with custom ocr engine using provided input image and set
     * of properties and save to the given path.
     */
    protected void createPdf(String pdfPath, File inputFile,
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
    protected void createPdfA(String pdfPath, File inputFile,
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
    protected String getTextFromPdf(File file, String testName) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
            createPdf(pdfPath, file, new OcrPdfCreatorProperties());
            result = getTextFromPdfLayer(pdfPath, "Text Layer");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            deleteFile(pdfPath);
        }

        return result;
    }

    /**
     * Retrieve image BBox rectangle from the first page from given pdf document.
     */
    protected Rectangle getImageBBoxRectangleFromPdf(String path)
            throws IOException {
        ExtractionStrategy extractionStrategy = getExtractionStrategy(path, "Image Layer");
        return extractionStrategy.getImageBBoxRectangle();
    }

    /**
     * Get extraction strategy for given document.
     */
    protected ExtractionStrategy getExtractionStrategy(String pdfPath)
            throws IOException {
        return getExtractionStrategy(pdfPath, "Text Layer");
    }

    /**
     * Get extraction strategy for given document.
     */
    protected ExtractionStrategy getExtractionStrategy(String pdfPath,
            String layerName)
            throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        ExtractionStrategy strategy = new ExtractionStrategy(layerName);
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();
        return strategy;
    }

    /**
     * Delete file using provided path.
     */
    protected void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()
                    && Files.exists(java.nio.file.Paths.get(filePath))) {
                Files.delete(java.nio.file.Paths.get(filePath));
            }
        } catch (IOException | SecurityException e) {
            LOGGER.info(MessageFormatUtil.format(
                    "Cannot delete file {0} : {1}",
                    filePath,
                    e.getMessage()));
        }
    }
}
