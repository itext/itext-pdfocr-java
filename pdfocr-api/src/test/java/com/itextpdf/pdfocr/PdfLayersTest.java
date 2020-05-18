package com.itextpdf.pdfocr;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.PdfTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class PdfLayersTest extends PdfTest {

    @Test
    public void testPdfLayersWithDefaultNames() {
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        File file = new File(path);

        PdfRenderer pdfRenderer = new PdfRenderer(new CustomOcrEngine());
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        getPdfWriter());

        Assert.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assert.assertEquals(2, layers.size());
        Assert.assertEquals("Image Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assert.assertEquals("Text Layer",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());
        doc.close();
    }

    @Test
    public void testPdfLayersWithCustomNames() {
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageLayerName("name image 1");
        properties.setTextLayerName("name text 1");

        PdfRenderer pdfRenderer = new PdfRenderer(new CustomOcrEngine(), properties);
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        getPdfWriter());

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

        doc.close();
    }

    @Test
    public void testTextFromPdfLayers() throws IOException {
        String testName = "testTextFromPdfLayers";
        String path = getImagesTestDirectory() + DEFAULT_IMAGE_NAME;
        String pdfPath = PdfTestUtils.getCurrentDirectory() + testName + ".pdf";
        File file = new File(path);

        PdfRenderer pdfRenderer = new PdfRenderer(new CustomOcrEngine());
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

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

        Assert.assertEquals(DEFAULT_EXPECTED_RESULT,
                getTextFromPdfLayer(pdfPath, "Text Layer"));
        Assert.assertEquals("",
                getTextFromPdfLayer(pdfPath, "Image Layer"));
    }
}
