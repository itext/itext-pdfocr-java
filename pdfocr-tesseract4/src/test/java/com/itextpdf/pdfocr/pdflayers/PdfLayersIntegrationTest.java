/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

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
package com.itextpdf.pdfocr.pdflayers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class PdfLayersIntegrationTest extends IntegrationTestHelper {

    AbstractTesseract4OcrEngine tesseractReader;

    public PdfLayersIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testTextFromPdfLayersFromMultiPageTiff() throws IOException {
        String testName = "testTextFromPdfLayersFromMultiPageTiff";
        boolean preprocess =
                tesseractReader.getTesseract4OcrEngineProperties().isPreprocessingImages();
        String path = TEST_IMAGES_DIRECTORY + "multîpage.tiff";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextLayerName("Text Layer");
        properties.setImageLayerName("Image Layer");
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader, properties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(2, layers.size());
        Assertions.assertEquals("Image Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertEquals("Text Layer",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());

        doc.close();

        // Text layer should contain all text
        // Image layer shouldn't contain any text
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 5";
        Assertions.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, "Text Layer", 5));
        Assertions.assertEquals("",
                getTextFromPdfLayer(pdfPath,
                        "Image Layer", 5));
        Assertions.assertFalse(tesseractReader.getTesseract4OcrEngineProperties().isPreprocessingImages());
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(preprocess));
    }

    @Test
    public void testTextFromMultiPageTiff() throws IOException {
        String testName = "testTextFromMultiPageTiff";
        boolean preprocess =
                tesseractReader.getTesseract4OcrEngineProperties().isPreprocessingImages();
        String path = TEST_IMAGES_DIRECTORY + "multîpage.tiff";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

        Assertions.assertNotNull(doc);
        int numOfPages = doc.getNumberOfPages();
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(0, layers.size());


        doc.close();

        // Text layer should contain all text
        // Image layer shouldn't contain any text
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 5";
        Assertions.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, null, 5));
        Assertions.assertFalse(tesseractReader.getTesseract4OcrEngineProperties().isPreprocessingImages());
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(preprocess));
    }

    @Test
    public void testTextFromPdfLayersFromMultiPagePdf() throws IOException {
        String testName = "testTextFromPdfLayersFromMultiPagePdf";
        String pdfPath = getTargetDirectory() + testName + ".pdf";

        List<File> files = Arrays.<File>asList(
                new File(TEST_IMAGES_DIRECTORY + "german_01.jpg"),
                new File(TEST_IMAGES_DIRECTORY + "tèst/noisy_01.png"),
                new File(TEST_IMAGES_DIRECTORY + "nümbérs.jpg"),
                new File(TEST_IMAGES_DIRECTORY + "example_04.png")
        );

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageLayerName("image");
        properties.setTextLayerName("text");

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader, properties);
        PdfDocument doc = ocrPdfCreator.createPdf(files, getPdfWriter(pdfPath));

        Assertions.assertNotNull(doc);
        int numOfPages = doc.getNumberOfPages();
        Assertions.assertEquals(numOfPages, files.size());
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(numOfPages * 2, layers.size());
        Assertions.assertEquals("image",
                layers.get(2).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertEquals("text",
                layers.get(3).getPdfObject().get(PdfName.Name).toString());

        doc.close();

        // Text layer should contain all text
        // Image layer shouldn't contain any text
        String expectedOutput = "619121";
        Assertions.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, "text", 3));
        Assertions.assertEquals("",
                getTextFromPdfLayer(pdfPath, "image", 3));
    }
}
