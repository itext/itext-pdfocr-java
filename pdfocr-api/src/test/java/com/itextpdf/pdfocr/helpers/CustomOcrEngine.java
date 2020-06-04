package com.itextpdf.pdfocr.helpers;

import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrEngineProperties;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOcrEngine implements IOcrEngine {

    private OcrEngineProperties ocrEngineProperties;

    public CustomOcrEngine() {
    }

    public CustomOcrEngine(OcrEngineProperties ocrEngineProperties) {
        this.ocrEngineProperties = new OcrEngineProperties(ocrEngineProperties);
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        Map<Integer, List<TextInfo>> result =
                new HashMap<Integer, List<TextInfo>>();
        String text = PdfHelper.DEFAULT_TEXT;
        if (input.getAbsolutePath().contains(PdfHelper.THAI_IMAGE_NAME)) {
            text = PdfHelper.THAI_TEXT;
        }
        TextInfo textInfo = new TextInfo(text,
                Arrays.<Float>asList(204.0f, 158.0f, 742.0f, 294.0f));
        result.put(1, Collections.<TextInfo>singletonList(textInfo));
        return result;
    }

    @Override
    public void createTxt(List<File> inputImages, File txtFile) {
    }

    public OcrEngineProperties getOcrEngineProperties() {
        return ocrEngineProperties;
    }

    public void setOcrEngineProperties(
            OcrEngineProperties ocrEngineProperties) {
        this.ocrEngineProperties = ocrEngineProperties;
    }
}
