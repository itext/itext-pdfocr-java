package com.itextpdf.pdfocr.helpers;

import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOcrEngine implements IOcrEngine {

    public CustomOcrEngine() {
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        // mocked data for "numbers_01.jpg" image
        Map<Integer, List<TextInfo>> result =
                new HashMap<Integer, List<TextInfo>>();
        TextInfo textInfo = new TextInfo("619121",
                Arrays.<Float>asList(204.0f, 158.0f, 742.0f, 294.0f));
        result.put(1, Collections.<TextInfo>singletonList(textInfo));
        return result;
    }

    @Override
    public void createTxt(List<File> inputImages, String path) {
    }
}
