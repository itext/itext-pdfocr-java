package com.itextpdf.pdfocr;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ApiTest extends ExtendedITextTest {

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
