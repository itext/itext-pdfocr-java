package com.itextpdf.ocr;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class PdfLayersIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;

    public PdfLayersIntegrationTest(TesseractReader reader) {
        tesseractReader = reader;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.<Object[]>asList(
                new Object[][] { {
                        new TesseractExecutableReader(getTesseractDirectory(),
                                getTessDataDirectory())
                    }, {
                        new TesseractLibReader(getTessDataDirectory())
                    }
                });
    }

    @Test
    public void testPdfLayersWithDefaultNames() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);
        pdfRenderer.setInputImages(Collections.<File>singletonList(file));
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
        String path = testImagesDirectory + "numbers_01.jpg";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);
        pdfRenderer.setInputImages(Collections.<File>singletonList(file));

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

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

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
        String pdfPath = testDocumentsDirectory + UUID.randomUUID().toString() + ".pdf";
        File file = new File(path);

        tesseractReader.setPreprocessingImages(false);
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

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
        Assert.assertFalse(tesseractReader.isPreprocessingImages());

        deleteFile(pdfPath);
    }

    // TODO
   /* @Test
    public void testTextFromPdfLayersFromMultiPagePdf() throws IOException {
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";

        List<File> files = Arrays.asList(
                new File(testImagesDirectory + "german_01.jpg"),
                new File(testImagesDirectory + "noisy_01.png"),
                new File(testImagesDirectory + "numbers_01.jpg"),
                new File(testImagesDirectory + "example_04.png")
        );

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, files);
        pdfRenderer.setImageLayerName("image");
        pdfRenderer.setTextLayerName("text");
        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

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
    */
}
