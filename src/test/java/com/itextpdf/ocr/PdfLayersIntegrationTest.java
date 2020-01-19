package com.itextpdf.ocr;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfLayersIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testPdfLayersWithDefaultNames() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);
        pdfRenderer.setInputImages(Collections.singletonList(file));
        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter());

        Assert.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assert.assertEquals(2, layers.size());
        Assert.assertEquals("Image Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assert.assertEquals("Text Layer",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());
        Assert.assertEquals(1, pdfRenderer.getInputImages().size());
        doc.close();
    }

    @Test
    public void testPdfLayersWithCustomNames() throws IOException {
        String path = testImagesDirectory + "numbers_02.jpg";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);
        pdfRenderer.setInputImages(Collections.singletonList(file));

        pdfRenderer.setImageLayerName("name image 1");
        pdfRenderer.setTextLayerName("name text 1");

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter());

        // setting layer's name after ocr was done, name shouldn't change
        pdfRenderer.setImageLayerName("name image 100500");

        Assert.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assert.assertEquals(2, layers.size());
        Assert.assertEquals("name image 1",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assert.assertTrue(layers.get(0).isOn());
        Assert.assertEquals("name text 1",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());
        Assert.assertTrue(layers.get(1).isOn());
        Assert.assertEquals(1, pdfRenderer.getInputImages().size());

        doc.close();
    }

    @Test
    public void testTextFromPdfLayers() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assert.assertEquals(2, layers.size());
        Assert.assertEquals("Image Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assert.assertTrue(layers.get(0).isOn());
        Assert.assertEquals("Text Layer",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());
        Assert.assertTrue(layers.get(1).isOn());

        doc.close();

        // Text layer should contain all text
        // Image layer shouldn't contain any text
        String expectedOutput = "619121";
        Assert.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, "Text Layer", 1));
        Assert.assertEquals("",
                getTextFromPdfLayer(pdfPath,
                        "Image Layer", 1));

        deleteFile(pdfPath);
    }

    @Test
    public void testTextFromPdfLayersFromMultiPageTiff() throws IOException {
        String path = testImagesDirectory + "multipage.tiff";
        String pdfPath = testPdfDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath));

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
        String expectedOutput = "Multipage\nTIFF\nExample\n5\nPage";
        Assert.assertEquals(expectedOutput,
                getTextFromPdfLayer(pdfPath, "Text Layer", 5));
        Assert.assertEquals("",
                getTextFromPdfLayer(pdfPath,
                        "Image Layer", 5));

        deleteFile(pdfPath);
    }

    @Test
    public void testTextFromPdfLayersFromMultiPagePdf() throws IOException {
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";

        List<File> files = Arrays.asList(
                new File(testImagesDirectory + "example_01.BMP"),
                new File(testImagesDirectory + "example_02.JFIF"),
                new File(testImagesDirectory + "numbers_01.jpg"),
                new File(testImagesDirectory + "example_04.png")
        );

        IOcrReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, files);
        pdfRenderer.setImageLayerName("image");
        pdfRenderer.setTextLayerName("text");
        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath));

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
        Assert.assertEquals(4, pdfRenderer.getInputImages().size());

        deleteFile(pdfPath);
    }

    @Test
    public void testInputInvalidImage() throws IOException {
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";

        File file1 = new File(testImagesDirectory + "example.txt");
        File file2 = new File(testImagesDirectory
                + "example_05_corrupted.bmp");
        File file3 = new File(testImagesDirectory
                + "numbers_02.jpg");
        try {
            IOcrReader tesseractReader = new TesseractExecutableReader(
                    getTesseractDirectory());
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Arrays.asList(file3, file1, file2, file3));

            pdfRenderer.doPdfOcr(getPdfWriter());
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_INPUT_IMAGE_FORMAT,
                            "txt");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }
}
