package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class OnnxTestUtils {

    public static String getTextFromLayer(PdfDocument pdfDocument, int pageNr, String layerName) {
        ExtractionStrategy extractionStrategy = extractTextFromLayer(pdfDocument, pageNr, layerName);
        return extractionStrategy.getResultantText();
    }

    public static ExtractionStrategy extractTextFromLayer(PdfDocument pdfDocument, int pageNr, String layerName) {
        ExtractionStrategy strategy = new ExtractionStrategy(layerName);
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(pageNr));
        return strategy;
    }

    protected static String getTextFromImage(File imageFile, OnnxTrOcrEngine ocrEngine) {
        Map<Integer, List<TextInfo>> integerListMap = ocrEngine.doImageOcr(imageFile);
        return getStringFromListMap(integerListMap);
    }

    private static String getStringFromListMap(Map<Integer, List<TextInfo>> listMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Entry<Integer, List<TextInfo>> entry : listMap.entrySet()) {
            for (TextInfo textInfo : entry.getValue()) {
                if (textInfo.getText() != null) {
                    stringBuilder.append(textInfo.getText()).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }
}
