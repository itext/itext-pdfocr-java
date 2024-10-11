/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.pdfocr;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class PdfLayersTest extends ExtendedITextTest {

    @Test
    public void testPdfLayersWithDefaultNames() {
        String path = PdfHelper.getDefaultImagePath();
        File file = new File(path);

        OcrEngineProperties ocrEngineProperties = new OcrEngineProperties();
        ocrEngineProperties.setLanguages(
                Collections.<String>singletonList("eng"));
        CustomOcrEngine engine = new CustomOcrEngine(ocrEngineProperties);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(engine);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file),
                        PdfHelper.getPdfWriter());

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(0, layers.size());
        doc.close();

        Assertions.assertEquals(engine, ocrPdfCreator.getOcrEngine());
        Assertions.assertEquals(1, engine.getOcrEngineProperties().getLanguages().size());
        Assertions.assertEquals("eng", engine.getOcrEngineProperties().getLanguages().get(0));
    }

    @Test
    public void testPdfLayersWithCustomNames() {
        String path = PdfHelper.getDefaultImagePath();
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageLayerName("name image 1");
        properties.setTextLayerName("name text 1");

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file),
                        PdfHelper.getPdfWriter());

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(2, layers.size());
        Assertions.assertEquals("name image 1",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(0).isOn());
        Assertions.assertEquals("name text 1",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(1).isOn());

        doc.close();
    }

    @Test
    public void testTextFromPdfLayers() throws IOException {
        String testName = "testTextFromPdfLayers";
        String path = PdfHelper.getDefaultImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageLayerName("Image Layer");
        properties.setTextLayerName("Text Layer");
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file), PdfHelper.getPdfWriter(pdfPath));

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(2, layers.size());
        Assertions.assertEquals("Image Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(0).isOn());
        Assertions.assertEquals("Text Layer",
                layers.get(1).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(1).isOn());

        doc.close();

        Assertions.assertEquals(PdfHelper.DEFAULT_TEXT,
                PdfHelper.getTextFromPdfLayer(pdfPath, "Text Layer"));
        Assertions.assertEquals("",
                PdfHelper.getTextFromPdfLayer(pdfPath, "Image Layer"));
    }

    @Test
    public void testPdfLayersWithImageLayerOnly() {
        String path = PdfHelper.getDefaultImagePath();
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageLayerName("Image Layer");

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file),
                        PdfHelper.getPdfWriter());

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(1, layers.size());
        Assertions.assertEquals("Image Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(0).isOn());

        doc.close();
    }

    @Test
    public void testPdfLayersWithTextLayerOnly() {
        String path = PdfHelper.getDefaultImagePath();
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextLayerName("Text Layer");

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file),
                        PdfHelper.getPdfWriter());

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(1, layers.size());
        Assertions.assertEquals("Text Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(0).isOn());

        doc.close();
    }

    @Test
    public void testPdfLayersWithTextAndImageLayerWithTheSameName() {
        String path = PdfHelper.getDefaultImagePath();
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextLayerName("Mixed Layer");
        properties.setImageLayerName("Mixed Layer");

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(new CustomOcrEngine(), properties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file),
                        PdfHelper.getPdfWriter());

        Assertions.assertNotNull(doc);
        List<PdfLayer> layers = doc.getCatalog()
                .getOCProperties(true).getLayers();

        Assertions.assertEquals(1, layers.size());
        Assertions.assertEquals("Mixed Layer",
                layers.get(0).getPdfObject().get(PdfName.Name).toString());
        Assertions.assertTrue(layers.get(0).isOn());

        doc.close();
    }


}
