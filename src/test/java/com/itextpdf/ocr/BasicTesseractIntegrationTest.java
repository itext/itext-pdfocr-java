package com.itextpdf.ocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.layout.element.Image;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Category(IntegrationTest.class)
public class BasicTesseractIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testKeepOriginalSizeScaleMode() throws IOException {
        String filePath = testDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(file),
                IPdfRenderer.ScaleMode.keepOriginalSize);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(), false);

        Assert.assertNotNull(doc);

        ImageData imageData = null;
        try {
            imageData = ImageDataFactory.create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (imageData != null) {
            float imageWidth = UtilService.getPoints(imageData.getWidth());
            float imageHeight = UtilService.getPoints(imageData.getHeight());
            float realWidth = doc.getFirstPage().getPageSize().getWidth();
            float realHeight = doc.getFirstPage().getPageSize().getHeight();

            Assert.assertEquals(imageWidth, realWidth, delta);
            Assert.assertEquals(imageHeight, realHeight, delta);
        }

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testScaleWidthMode() throws IOException {
        String filePath = testDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        ImageData originalImageData = null;
        try {
            originalImageData = ImageDataFactory.create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        Rectangle pageSize = new Rectangle(pageWidthPt, pageHeightPt);
        Image resultImage = getImageFromPdf(file, IPdfRenderer.ScaleMode.scaleWidth, pageSize);

        // page size should be equal to the result image size
        // result image height should be equal to the value that was set as page height
        // result image width should be scaled proportionally according to the provided image height
        // and original image size
        if (originalImageData != null) {
            float originalImageHeight = UtilService.getPoints(originalImageData.getHeight());
            float originalImageWidth = UtilService.getPoints(originalImageData.getWidth());

            float resultPageWidth = pageSize.getWidth();
            float resultPageHeight = pageSize.getHeight();

            float resultImageHeight = UtilService.getPoints(resultImage.getImageHeight());
            float resultImageWidth = UtilService.getPoints(resultImage.getImageWidth());

            float expectedImageWidth = originalImageWidth * resultPageHeight / originalImageHeight;

            Assert.assertEquals(resultPageWidth, expectedImageWidth, delta);
            Assert.assertEquals(resultPageHeight, pageHeightPt, delta);

            Assert.assertEquals(resultPageWidth, resultImageWidth, delta);

            Assert.assertEquals(resultImageHeight, resultPageHeight, delta);
            Assert.assertEquals(resultImageWidth, expectedImageWidth, delta);
        }
    }

    @Test
    public void testScaleHeightMode() throws IOException {
        String filePath = testDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        ImageData originalImageData = null;
        try {
            originalImageData = ImageDataFactory.create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        Rectangle pageSize = new Rectangle(pageWidthPt, pageHeightPt);
        Image resultImage = getImageFromPdf(file, IPdfRenderer.ScaleMode.scaleHeight, pageSize);

        if (originalImageData != null) {
            float originalImageHeight = UtilService.getPoints(originalImageData.getHeight());
            float originalImageWidth = UtilService.getPoints(originalImageData.getWidth());

            float resultPageWidth = pageSize.getWidth();
            float resultPageHeight = pageSize.getHeight();

            float resultImageHeight = UtilService.getPoints(resultImage.getImageHeight());
            float resultImageWidth = UtilService.getPoints(resultImage.getImageWidth());

            float expectedImageHeight = originalImageHeight * resultPageWidth / originalImageWidth;

            Assert.assertEquals(resultPageWidth, pageWidthPt, delta);
            Assert.assertEquals(resultPageHeight, resultImageHeight, delta);

            Assert.assertEquals(resultPageWidth, resultImageWidth, delta);

            Assert.assertEquals(resultImageHeight, expectedImageHeight, delta);
            Assert.assertEquals(resultImageWidth, pageWidthPt, delta);
        }
    }

    @Test
    public void testScaleToFitMode() throws IOException {
        String filePath = testDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(file),
                IPdfRenderer.ScaleMode.scaleToFit);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(), false);

        Assert.assertNotNull(doc);

        float realWidth = doc.getFirstPage().getPageSize().getWidth();
        float realHeight = doc.getFirstPage().getPageSize().getHeight();

        Assert.assertEquals(PageSize.A4.getWidth(), realWidth, delta);
        Assert.assertEquals(PageSize.A4.getHeight(), realHeight, delta);

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testNoisyImage() {
        String path = testDirectory + "noisy_01.png";
        String expectedOutput = "Noisyimage to test Tesseract OCR";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testDifferentTextStyles() {
        String path = testDirectory + "example_04.png";
        String expectedOutput = "Does this OCR thing really work? H . " +
                "How about a bigger font? " +
                "123456789 {23 " +
                "What about thiy font?";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testFontColor() throws IOException {
        String path = testDirectory + "numbers_01.jpg";
        String pdfPath = testDirectory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        pdfRenderer.setTextLayerName("Text1");
        Color color = DeviceCmyk.CYAN;
        pdfRenderer.setFontColor(color);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), false);

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        Color fillColor = strategy.getFillColor();
        Color strokeColor = strategy.getFillColor();

        Assert.assertEquals(fillColor, color);
        Assert.assertEquals(strokeColor, color);

        deleteFile(pdfPath);
    }

    @Test
    public void testFontColorInMultiPagePdf() throws IOException {
        String path = testDirectory + "multipage.tiff";
        String pdfPath = testDirectory + "test.pdf";
        File file = new File(path);

        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));
        pdfRenderer.setTextLayerName("Text1");
        Color color = DeviceCmyk.MAGENTA;
        pdfRenderer.setFontColor(color);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), false);

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(3));

        Color fillColor = strategy.getFillColor();
        Color strokeColor = strategy.getFillColor();

        Assert.assertEquals(fillColor, color);
        Assert.assertEquals(strokeColor, color);

        deleteFile(pdfPath);
    }

    /**
     * Parse text from image and compare with expected.
     *
     * @param path
     * @param expectedOutput
     */
    private void testImageOcrText(String path, String expectedOutput) {
        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        File ex1 = new File(path);

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader, ex1);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /**
     * Parse text from given image using tesseract.
     *
     * @param tesseractReader
     * @param file
     * @return
     */
    private String getTextUsingTesseractFromImage(IOcrReader tesseractReader, File file) {
        int page = 1;
        List<TextInfo> data = tesseractReader.readDataFromInput(file);
        List<TextInfo> pageText = UtilService.getTextForPage(data, page);

        return pageText.stream()
                .map(TextInfo::getText)
                .collect(Collectors.joining(" "));
    }
}
