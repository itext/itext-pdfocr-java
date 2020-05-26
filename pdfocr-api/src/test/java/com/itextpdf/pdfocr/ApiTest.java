package com.itextpdf.pdfocr;

import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.PdfHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ApiTest {

    @Test
    public void testTextInfo() {
        String path = PdfHelper.getDefaultImagePath();
        Map<Integer, List<TextInfo>> result = new CustomOcrEngine().doImageOcr(new File(path));
        Assert.assertEquals(1, result.size());

        TextInfo textInfo = new TextInfo();
        textInfo.setText("text");
        textInfo.setBbox(Arrays.<Float>asList(204.0f, 158.0f, 742.0f, 294.0f));
        int page = 2;
        result.put(page, Collections.<TextInfo>singletonList(textInfo));

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(textInfo.getText(), result.get(page).get(0).getText());
        Assert.assertEquals(textInfo.getBbox().size(), result.get(page).get(0).getBbox().size());
    }
}
