package com.itextpdf.pdfocr.pdflayers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public abstract class PdfLayersIntegrationTest extends AbstractIntegrationTest {

    Tesseract4OcrEngine tesseractReader;

    public PdfLayersIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testTextFromPdfLayersFromMultiPageTiff() throws IOException {
        String testName = "testTextFromPdfLayersFromMultiPageTiff";
        boolean preprocess =
                tesseractReader.getTesseract4OcrEngineProperties().isPreprocessingImages();
        String path = testImagesDirectory + "multipage.tiff";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        int numOfPages = doc.getNumberOfPages();
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assert.assertEquals(numOfPages * 2, layers.size());
        Assert.assertEquals("Image Layer",
                layers.get(2).getPdfObject().get(PdfName.Name).toString());
        Assert.assertEquals("Text Layer",
                layers.get(3).getPdfObject().get(PdfName.Name).toString());

        doc.close();

        // Text layer should contain all text
        // Image layer shouldn't contain any text
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 5";
        Assert.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, "Text Layer", 5));
        Assert.assertEquals("",
                getTextFromPdfLayer(pdfPath,
                        "Image Layer", 5));
        Assert.assertFalse(tesseractReader.getTesseract4OcrEngineProperties().isPreprocessingImages());
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(preprocess));
    }

    @Test
    public void testTextFromPdfLayersFromMultiPagePdf() throws IOException {
        String testName = "testTextFromPdfLayersFromMultiPagePdf";
        String pdfPath = getTargetDirectory() + testName + ".pdf";

        List<File> files = Arrays.<File>asList(
                new File(testImagesDirectory + "german_01.jpg"),
                new File(testImagesDirectory + "noisy_01.png"),
                new File(testImagesDirectory + "numbers_01.jpg"),
                new File(testImagesDirectory + "example_04.png")
        );

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageLayerName("image");
        properties.setTextLayerName("text");

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader, properties);
        PdfDocument doc = ocrPdfCreator.createPdf(files, getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        int numOfPages = doc.getNumberOfPages();
        Assert.assertEquals(numOfPages, files.size());
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assert.assertEquals(numOfPages * 2, layers.size());
        Assert.assertEquals("image",
                layers.get(2).getPdfObject().get(PdfName.Name).toString());
        Assert.assertEquals("text",
                layers.get(3).getPdfObject().get(PdfName.Name).toString());

        doc.close();

        // Text layer should contain all text
        // Image layer shouldn't contain any text
        String expectedOutput = "619121";
        Assert.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, "text", 3));
        Assert.assertEquals("",
                getTextFromPdfLayer(pdfPath, "image", 3));
    }
}
