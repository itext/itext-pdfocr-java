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

import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.DocumentProperties;
import com.itextpdf.kernel.pdf.PdfAConformance;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfa.PdfADocument;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.CustomProductAwareOcrEngine;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.helpers.TestProcessProperties;
import com.itextpdf.pdfocr.helpers.TestStructureDetectionOcrEngine;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class ApiTest extends ExtendedITextTest {

    public static final String DESTINATION_FOLDER = "./target/test/com/itextpdf/pdfocr";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(DESTINATION_FOLDER);
    }

    @Test
    public void createPdfWithFileTest() {
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setMetaInfo(new DummyMetaInfo());
        OcrPdfCreator pdfCreator = new OcrPdfCreator(new CustomOcrEngine(), props);
        try (PdfDocument pdf = pdfCreator.createPdf(
                Collections.<File>singletonList(new File(PdfHelper.getDefaultImagePath())),
                PdfHelper.getPdfWriter(),
                new DocumentProperties().setEventCountingMetaInfo(new DummyMetaInfo())
        )) {
            String contentBytes = new String(pdf.getPage(1).getContentBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(contentBytes.contains("<00190014001c001400150014>"));
        }
    }

    @Test
    public void createPdfFileWithFileTest() throws IOException {
        String output = DESTINATION_FOLDER + "createPdfFileWithFileTest.pdf";

        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setMetaInfo(new DummyMetaInfo());
        OcrPdfCreator pdfCreator = new OcrPdfCreator(new CustomOcrEngine(), props);
        pdfCreator.createPdfFile(
                Collections.<File>singletonList(new File(PdfHelper.getDefaultImagePath())),
                new File(output));

        try (PdfDocument pdf = new PdfDocument(new PdfReader(output))) {
            String contentBytes = new String(pdf.getPage(1).getContentBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(contentBytes.contains("<00190014001c001400150014>"));
        }
    }

    @Test
    public void createPdfAWithFileTest() throws FileNotFoundException {
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setMetaInfo(new DummyMetaInfo())
                .setPdfLang("en-US");
        OcrPdfCreator pdfCreator = new OcrPdfCreator(new CustomOcrEngine(), props);
        try (PdfDocument pdf = pdfCreator.createPdfA(
                Collections.<File>singletonList(new File(PdfHelper.getDefaultImagePath())),
                PdfHelper.getPdfWriter(),
                new DocumentProperties().setEventCountingMetaInfo(new DummyMetaInfo()),
                PdfHelper.getRGBPdfOutputIntent()
        )) {
            String contentBytes = new String(pdf.getPage(1).getContentBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(contentBytes.contains("<00190014001c001400150014>"));
            Assertions.assertTrue(pdf instanceof PdfADocument);
        }
    }

    @Test
    public void createPdfAFileWithFileTest() throws IOException {
        String output = DESTINATION_FOLDER + "createPdfAFileWithFileTest.pdf";
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setMetaInfo(new DummyMetaInfo())
                .setPdfLang("en-US");
        OcrPdfCreator pdfCreator = new OcrPdfCreator(new CustomOcrEngine(), props);
        pdfCreator.createPdfAFile(
                Collections.<File>singletonList(new File(PdfHelper.getDefaultImagePath())),
                new File(output),
                PdfHelper.getRGBPdfOutputIntent());
        try (PdfDocument pdf = new PdfDocument(new PdfReader(output))) {
            String contentBytes = new String(pdf.getPage(1).getContentBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(contentBytes.contains("<00190014001c001400150014>"));
            PdfAConformance cl = pdf.getReader().getPdfConformance().getAConformance();
            Assertions.assertEquals(PdfAConformance.PDF_A_3U.getLevel(), cl.getLevel());
            Assertions.assertEquals(PdfAConformance.PDF_A_3U.getPart(), cl.getPart());
        }
    }

    @Test
    public void createPdfAFileWithFileNoMetaTest() throws IOException {
        String output = DESTINATION_FOLDER + "createPdfAFileWithFileNoMetaTest.pdf";
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setPdfLang("en-US");
        OcrPdfCreator pdfCreator = new OcrPdfCreator(new CustomOcrEngine(), props);
        pdfCreator.createPdfAFile(
                Collections.<File>singletonList(new File(PdfHelper.getDefaultImagePath())),
                new File(output),
                PdfHelper.getRGBPdfOutputIntent());
        try (PdfDocument pdf = new PdfDocument(new PdfReader(output))) {
            String contentBytes = new String(pdf.getPage(1).getContentBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(contentBytes.contains("<00190014001c001400150014>"));
            PdfAConformance cl = pdf.getReader().getPdfConformance().getAConformance();
            Assertions.assertEquals(PdfAConformance.PDF_A_3U.getLevel(), cl.getLevel());
            Assertions.assertEquals(PdfAConformance.PDF_A_3U.getPart(), cl.getPart());
        }
    }

    @Test
    public void createPdfAFileWithFileProductAwareEngineTest() throws IOException {
        String output = DESTINATION_FOLDER + "createPdfAFileWithFileProductAwareEngineTest.pdf";
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setPdfLang("en-US");
        CustomProductAwareOcrEngine ocrEngine = new CustomProductAwareOcrEngine();
        OcrPdfCreator pdfCreator = new OcrPdfCreator(ocrEngine, props);
        pdfCreator.createPdfAFile(
                Collections.<File>singletonList(new File(PdfHelper.getDefaultImagePath())),
                new File(output),
                PdfHelper.getRGBPdfOutputIntent());

        Assertions.assertTrue(ocrEngine.isGetMetaInfoContainerTriggered());
    }

    @Test
    public void testTextInfo() {
        String path = PdfHelper.getDefaultImagePath();
        Map<Integer, List<TextInfo>> result = new CustomOcrEngine().doImageOcr(new File(path));
        Assertions.assertEquals(1, result.size());

        TextInfo textInfo = new TextInfo();
        textInfo.setText("text");
        textInfo.setBboxRect(new Rectangle(204.0f, 158.0f, 538.0f, 136.0f));
        int page = 2;
        result.put(page, Collections.<TextInfo>singletonList(textInfo));

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(textInfo.getText(), result.get(page).get(0).getText());
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
        Assertions.assertTrue(fontName.contains("LiberationSans"));
    }

    @Test
    public void testImageRotationHandler() {
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
            properties.setImageRotationHandler(new NotImplementedImageRotationHandler());
            String testName = "testSetAndGetImageRotationHandler";
            String path = PdfHelper.getImagesTestDirectory() + "90_degrees_rotated.jpg";
            String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

            PdfHelper.createPdf(pdfPath, new File(path),
                    properties);

            Assertions.assertNotNull(properties.getImageRotationHandler());
        });

        Assertions.assertEquals("applyRotation is not implemented", exception.getMessage());
    }

    @Test
    public void testImageRotationHandlerForTiff() {
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
            properties.setImageRotationHandler(new NotImplementedImageRotationHandler());
            String testName = "testSetAndGetImageRotationHandler";
            String path = PdfHelper.getImagesTestDirectory() + "multipage.tiff";
            String pdfPath = PdfHelper.getTargetDirectory() + testName + ".pdf";

            PdfHelper.createPdf(pdfPath, new File(path),
                    properties);

            Assertions.assertNotNull(properties.getImageRotationHandler());
        });

        Assertions.assertEquals("applyRotation is not implemented", exception.getMessage());
    }

    @Test
    public void testTableStructureTree() throws IOException, InterruptedException {
        String pdfPath = PdfHelper.getTargetDirectory() + "tableStructureTree.pdf";
        // Image doesn't really matter here
        String input = PdfHelper.getImagesTestDirectory() + "numbers_01.jpg";
        IOcrEngine ocrEngine = new TestStructureDetectionOcrEngine();

        OcrPdfCreatorProperties creatorProperties = new OcrPdfCreatorProperties();
        creatorProperties.setTextColor(DeviceRgb.RED);
        creatorProperties.setTagged(true);
        OcrPdfCreator pdfCreator = new OcrPdfCreator(ocrEngine, creatorProperties);
        TestProcessProperties processProperties = new TestProcessProperties(5, 6, 50, 15, 100, 200);

        try (PdfWriter pdfWriter = PdfHelper.getPdfWriter(pdfPath)) {
            pdfCreator.createPdf(Collections.<File>singletonList(
                    new File(input)), pdfWriter, new DocumentProperties(), processProperties).close();
        }

        Assertions.assertNull(new CompareTool()
                .compareByContent(pdfPath, PdfHelper.TEST_DIRECTORY + "cmp_tableStructureTree.pdf", PdfHelper.getTargetDirectory(), "diff_"));
    }

    @Test
    public void testTaggingNotSupported() {
        String input = PdfHelper.getImagesTestDirectory() + "numbers_01.jpg";
        String pdfPath = PdfHelper.getTargetDirectory() + "taggingNotSupported.pdf";

        Exception e = Assertions.assertThrows(PdfOcrException.class,
                () -> PdfHelper.createPdf(pdfPath, new File(input), new OcrPdfCreatorProperties().setTagged(true))
        );
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.TAGGING_IS_NOT_SUPPORTED, e.getMessage());
    }

    static class NotImplementedImageRotationHandler implements IImageRotationHandler {
        @Override
        public ImageData applyRotation(ImageData imageData) {
            throw new RuntimeException("applyRotation is not implemented");
        }
    }

    private static class DummyMetaInfo implements IMetaInfo {
    }
}
