package com.itextpdf.pdfocr;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfTestUtils;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.UUID;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(IntegrationTest.class)
public class PdfTest extends ExtendedITextTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfTest.class);

    public static final String MOCKED_IMAGE_NAME = "numbers_01.jpg";
    public static final String MOCKED_EXPECTED_RESULT = "619121";

    /**
     * Return images test directory.
     *
     * @return String
     */
    protected static String getImagesTestDirectory() {
        return PdfTestUtils.getCurrentDirectory() + "images"
                + java.io.File.separatorChar;
    }

    /**
     * Create pdfWriter using provided path to destination file.
     */
    protected PdfWriter getPdfWriter(String pdfPath)
            throws FileNotFoundException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Delete file using provided path.
     */
    protected static void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()
                    && Files.exists(java.nio.file.Paths.get(filePath))) {
                Files.delete(java.nio.file.Paths.get(filePath));
            }
        } catch (IOException | SecurityException e) {
            LOGGER.info(MessageFormatUtil.format(
                            "Cannot delete file {0}: {1}",
                            filePath,
                            e.getMessage()));
        }
    }

    /**
     * Retrieve text from given pdf document.
     */
    protected String getTextFromPdf(File inputFile,
            OcrPdfCreatorProperties properties) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = getImagesTestDirectory() + UUID.randomUUID().toString() +
                    ".pdf";
            doPdfOcr(pdfPath, inputFile, properties);
            result = getTextFromPdfLayer(pdfPath, properties.getTextLayerName());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            deleteFile(pdfPath);
        }

        return result;
    }

    /**
     * Retrieve text from given pdf document.
     */
    protected String getTextFromPdf(File inputFile,
            OcrPdfCreatorProperties properties, String layerName) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = getImagesTestDirectory() + UUID.randomUUID().toString() +
                    ".pdf";
            doPdfOcr(pdfPath, inputFile, properties);
            result = getTextFromPdfLayer(pdfPath, layerName);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            deleteFile(pdfPath);
        }

        return result;
    }

    /**
     * Retrieve text from given pdf document.
     */
    protected void doPdfOcr(String pdfPath, File inputFile,
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
     * Get text from layer specified by name from the first page.
     */
    protected String getTextFromPdfLayer(String pdfPath, String layerName)
            throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy textExtractionStrategy = new ExtractionStrategy(
                layerName);

        PdfCanvasProcessor processor = new PdfCanvasProcessor(
                textExtractionStrategy);
        processor.processPageContent(pdfDocument.getFirstPage());

        pdfDocument.close();
        return textExtractionStrategy.getResultantText();
    }
}
