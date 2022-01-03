/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.helpers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;

import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.test.ExtendedITextTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfHelper {

    public static final String DEFAULT_IMAGE_NAME = "numbers_01.jpg";
    public static final String DEFAULT_TEXT = "619121";
    public static final String THAI_IMAGE_NAME = "thai.PNG";
    public static final String THAI_TEXT = "ป ระ เท ศ ไ";
    // directory with test files
    public static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    public static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfHelper.class);

    /**
     * Returns images test directory.
     */
    public static String getImagesTestDirectory() {
        return TEST_DIRECTORY + "images/";
    }

    /**
     * Returns path to default test image.
     */
    public static String getDefaultImagePath() {
        return getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
    }

    /**
     * Returns path to thai test image.
     */
    public static String getThaiImagePath() {
        return getImagesTestDirectory() + THAI_IMAGE_NAME;
    }

    /**
     * Returns path to test font.
     */
    public static String getFreeSansFontPath() {
        return TEST_DIRECTORY + "fonts/FreeSans.ttf";
    }

    /**
     * Returns path to test font.
     */
    public static String getKanitFontPath() {
        return TEST_DIRECTORY + "fonts/Kanit-Regular.ttf";
    }

    /**
     * Returns target directory (because target/test could not exist).
     */
    public static String getTargetDirectory() {
        if (!Files.exists(java.nio.file.Paths.get(TARGET_DIRECTORY))) {
            ExtendedITextTest.createDestinationFolder(TARGET_DIRECTORY);
        }
        return TARGET_DIRECTORY;
    }

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
     * Creates PDF rgb output intent for tests.
     */
    public static PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        String defaultRGBColorProfilePath = TEST_DIRECTORY + "profiles"
                        + "/sRGB_CS_profile.icm";
        InputStream is = new FileInputStream(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * Creates PDF cmyk output intent for tests.
     */
    public static PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        String defaultCMYKColorProfilePath = TEST_DIRECTORY
                + "profiles/CoatedFOGRA27.icc";
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
        ExtractionStrategy textExtractionStrategy = getExtractionStrategy(pdfPath, layerName, false);
        return textExtractionStrategy.getResultantText();
    }

    /**
     * Get text from layer specified by name from the first page.
     */
    public static String getTextFromPdfLayerUseActualText(String pdfPath,
            String layerName)
            throws IOException {
        ExtractionStrategy textExtractionStrategy = getExtractionStrategy(pdfPath, layerName, true);
        return textExtractionStrategy.getResultantText();
    }

    /**
     * Perform OCR with custom ocr engine using provided input image and set
     * of properties and save to the given path.
     */
    public static void createPdf(String pdfPath, File inputFile, OcrPdfCreatorProperties properties) {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(),
                properties);
        try (PdfWriter pdfWriter = getPdfWriter(pdfPath)) {
            ocrPdfCreator.createPdf(Collections.<File>singletonList(inputFile),
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
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(),
                properties);
        try (PdfWriter pdfWriter = getPdfWriter(pdfPath)) {
            ocrPdfCreator.createPdfA(Collections.<File>singletonList(inputFile),
                    pdfWriter, outputIntent).close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Retrieve text from specified page from given PDF document.
     */
    public static String getTextFromPdf(File file, String testName) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = getTargetDirectory() + testName + ".pdf";
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
        return getExtractionStrategy(pdfPath, null);
    }

    /**
     * Get extraction strategy for given document.
     */
    public static ExtractionStrategy getExtractionStrategy(String pdfPath,
            boolean useActualText)
            throws IOException {
        return getExtractionStrategy(pdfPath, "Text Layer", useActualText);
    }

    /**
     * Get extraction strategy for given document.
     */
    public static ExtractionStrategy getExtractionStrategy(String pdfPath,
            String layerName) throws IOException {
        return getExtractionStrategy(pdfPath, layerName, false);
    }

    /**
     * Get extraction strategy for given document.
     */
    public static ExtractionStrategy getExtractionStrategy(String pdfPath,
            String layerName, boolean useActualText)
            throws IOException {
        try (PdfReader readerPdf = new PdfReader(pdfPath);
                PdfDocument pdfDocument = new PdfDocument(readerPdf)) {
            ExtractionStrategy strategy = new ExtractionStrategy(layerName);
            strategy.setUseActualText(useActualText);
            PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);
            processor.processPageContent(pdfDocument.getFirstPage());
            return strategy;
        }
    }
}
