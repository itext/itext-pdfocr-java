/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

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

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class ApiTest extends ExtendedITextTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();


    @Test
    public void testTextInfo() {
        String path = PdfHelper.getDefaultImagePath();
        Map<Integer, List<TextInfo>> result = new CustomOcrEngine().doImageOcr(new File(path));
        Assert.assertEquals(1, result.size());

        TextInfo textInfo = new TextInfo();
        textInfo.setText("text");
        textInfo.setBboxRect(new Rectangle(204.0f, 158.0f, 538.0f, 136.0f));
        int page = 2;
        result.put(page, Collections.<TextInfo>singletonList(textInfo));

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(textInfo.getText(), result.get(page).get(0).getText());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 7)
    })
    @Test
    public void testThaiImageWithNotDefGlyphs() throws IOException {
        String testName = "testThaiImageWithNotdefGlyphs";
        String path = PdfHelper.getThaiImagePath();
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdf(pdfPath, new File(path),
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK));

        ExtractionStrategy strategy = PdfHelper.getExtractionStrategy(pdfPath);

        PdfFont font = strategy.getPdfFont();
        String fontName = font.getFontProgram().getFontNames().getFontName();
        Assert.assertTrue(fontName.contains("LiberationSans"));
    }

    @Test
    public void testImageRotationHandler() {
        junitExpectedException.expect(RuntimeException.class);
        junitExpectedException
                .expectMessage("applyRotation is not implemented");

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageRotationHandler(new NotImplementedImageRotationHandler());
        String testName = "testSetAndGetImageRotationHandler";
        String path = PdfHelper.getImagesTestDirectory() + "90_degrees_rotated.jpg";
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdf(pdfPath, new File(path),
                properties);

        Assert.assertNotNull(properties.getImageRotationHandler());
    }

    @Test
    public void testImageRotationHandlerForTiff() {
        junitExpectedException.expect(RuntimeException.class);
        junitExpectedException
                .expectMessage("applyRotation is not implemented");

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setImageRotationHandler(new NotImplementedImageRotationHandler());
        String testName = "testSetAndGetImageRotationHandler";
        String path = PdfHelper.getImagesTestDirectory() + "multipage.tiff";
        String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

        PdfHelper.createPdf(pdfPath, new File(path),
                properties);

        Assert.assertNotNull(properties.getImageRotationHandler());
    }

    static class NotImplementedImageRotationHandler implements IImageRotationHandler {
        @Override
        public ImageData applyRotation(ImageData imageData) {
            throw new RuntimeException("applyRotation is not implemented");
        }
    }
}
