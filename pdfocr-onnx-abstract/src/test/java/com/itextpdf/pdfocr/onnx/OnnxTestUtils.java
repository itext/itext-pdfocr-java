/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.onnx.util.MathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Assertions;

public class OnnxTestUtils {

    public static ExtractionStrategy extractTextFromLayer(PdfDocument pdfDocument, int pageNr, String layerName) {
        ExtractionStrategy strategy = new ExtractionStrategy(layerName);
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(pageNr));
        return strategy;
    }

    protected static String getTextFromImage(File imageFile, IOcrEngine ocrEngine) {
        Map<Integer, List<TextInfo>> integerListMap = ocrEngine.doImageOcr(imageFile);
        return getStringFromListMap(integerListMap);
    }

    protected static void doOcrAndCreatePdf(String imagePath, String destPdfPath, IOcrEngine ocrEngine) throws IOException {
        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties()
                .setTextLayerName("Text1").setTextColor(DeviceCmyk.MAGENTA);
        doOcrAndCreatePdf(imagePath, destPdfPath, ocrEngine, ocrPdfCreatorProperties);
    }

    protected static void doOcrAndCreatePdf(String imagePath, String destPdfPath, IOcrEngine ocrEngine, OcrPdfCreatorProperties ocrPdfCreatorProperties) throws IOException {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(ocrEngine, ocrPdfCreatorProperties);
        try (PdfWriter writer = new PdfWriter(destPdfPath)) {
            ocrPdfCreator.createPdf(Collections.singletonList(new File(imagePath)), writer).close();
        }
    }

    protected static void extractTextAndCompare(String dest, String cmpTxt, String layerName, double expRelDistance) throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(dest))) {
            ExtractionStrategy extractionStrategy = OnnxTestUtils.extractTextFromLayer(pdfDocument, 1, layerName);
            Assertions.assertEquals(DeviceCmyk.MAGENTA, extractionStrategy.getFillColor());
            String outText = extractionStrategy.getResultantText();
            String cmpText = getCmpText(cmpTxt);
            double relativeDistance = (double) MathUtil.calculateLevenshteinDistance(cmpText, outText) / cmpText.length();
            Assertions.assertTrue(relativeDistance < expRelDistance, "Expected: \"" + cmpText + "\", but was: \"" + outText + "\"");
        }
    }

    private static String getCmpText(String txtPath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(txtPath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String getStringFromListMap(Map<Integer, List<TextInfo>> listMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Entry<Integer, List<TextInfo>> entry : listMap.entrySet()) {
            for (TextInfo textInfo : entry.getValue()) {
                if (textInfo.getText() != null) {
                    stringBuilder.append(textInfo.getText()).append('\n');
                }
            }
        }
        return stringBuilder.toString();
    }
}
