package com.itextpdf.ocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.layout.element.Image;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class BasicTesseractIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;
    String parameter;

    public BasicTesseractIntegrationTest(TesseractReader reader, String param) {
        tesseractReader = reader;
        parameter = param;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.<Object[]>asList(
                new Object[][] { {
                        new TesseractExecutableReader(getTesseractDirectory(),
                                getTessDataDirectory()),
                        "executable"
                    }, {
                        new TesseractLibReader(getTessDataDirectory()),
                        "lib"
                    }
            });
    }

    @Test
    public void testFontColorInMultiPagePdf() throws IOException {
        String path = testImagesDirectory + "multipage.tiff";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        pdfRenderer.setTextLayerName("Text1");
        Color color = DeviceCmyk.MAGENTA;
        pdfRenderer.setTextColor(color);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(1));

        Color fillColor = strategy.getFillColor();
        Assert.assertEquals(fillColor, color);

        pdfDocument.close();
        deleteFile(pdfPath);
    }

    @Test
    public void testKeepOriginalSizeScaleMode() throws IOException {
        String filePath = testImagesDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        pdfRenderer.setScaleMode(IPdfRenderer.ScaleMode.keepOriginalSize);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter());

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
            Assert.assertEquals(IPdfRenderer.ScaleMode.keepOriginalSize,
                    pdfRenderer.getScaleMode());
        }

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testScaleWidthMode() throws IOException {
        String filePath = testImagesDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        ImageData originalImageData = null;
        try {
            originalImageData = ImageDataFactory
                    .create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        Rectangle pageSize = new Rectangle(pageWidthPt, pageHeightPt);
        Image resultImage = getImageFromPdf(tesseractReader, file,
                IPdfRenderer.ScaleMode.scaleWidth, pageSize);

        // page size should be equal to the result image size
        // result image height should be equal to the value that
        // was set as page height result image width should be scaled
        // proportionally according to the provided image height
        // and original image size
        if (originalImageData != null) {
            float originalImageHeight = UtilService
                    .getPoints(originalImageData.getHeight());
            float originalImageWidth = UtilService
                    .getPoints(originalImageData.getWidth());

            float resultPageWidth = pageSize.getWidth();
            float resultPageHeight = pageSize.getHeight();

            float resultImageHeight = UtilService
                    .getPoints(resultImage.getImageHeight());
            float resultImageWidth = UtilService
                    .getPoints(resultImage.getImageWidth());

            float expectedImageWidth = originalImageWidth * resultPageHeight
                    / originalImageHeight;

            Assert.assertEquals(resultPageWidth, pageWidthPt, delta);
            Assert.assertEquals(resultPageHeight, pageHeightPt, delta);

//            Assert.assertEquals(resultPageHeight, resultImageHeight, delta);
//            Assert.assertEquals(expectedImageWidth, resultImageWidth, delta);
        }
    }

    @Test
    public void testScaleHeightMode() throws IOException {
        String filePath = testImagesDirectory + "numbers_01.jpg";
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
        Image resultImage = getImageFromPdf(tesseractReader, file,
                IPdfRenderer.ScaleMode.scaleHeight, pageSize);

        if (originalImageData != null) {
            float originalImageHeight = UtilService
                    .getPoints(originalImageData.getHeight());
            float originalImageWidth = UtilService
                    .getPoints(originalImageData.getWidth());

            float resultPageWidth = pageSize.getWidth();
            float resultPageHeight = pageSize.getHeight();

            float resultImageHeight = UtilService
                    .getPoints(resultImage.getImageHeight());
            float resultImageWidth = UtilService
                    .getPoints(resultImage.getImageWidth());

            float expectedImageHeight = originalImageHeight * resultPageWidth
                    / originalImageWidth;

            Assert.assertEquals(resultPageWidth, pageWidthPt, delta);
            Assert.assertEquals(resultPageHeight, pageHeightPt, delta);

//            Assert.assertEquals(resultPageWidth, resultImageWidth, delta);
//            Assert.assertEquals(expectedImageHeight, resultImageHeight, delta);
        }
    }

    @Test
    public void testScaleToFitMode() throws IOException {
        String filePath = testImagesDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter());

        Assert.assertNotNull(doc);

        float realPageWidth = doc.getFirstPage().getPageSize().getWidth();
        float realPageHeight = doc.getFirstPage().getPageSize().getHeight();

        Assert.assertEquals(PageSize.A4.getWidth(), realPageWidth, delta);
        Assert.assertEquals(PageSize.A4.getHeight(), realPageHeight, delta);

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testNoisyImage() {
        String path = testImagesDirectory + "noisy_01.png";
        String expectedOutput1 = "Noisyimage to test Tesseract OCR";
        String expectedOutput2 = "Noisy image to test Tesseract OCR";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(path));
        Assert.assertTrue(realOutputHocr.equals(expectedOutput1) ||
                realOutputHocr.equals(expectedOutput2));
    }

    @Test
    public void testDifferentTextStyles() {
        String path = testImagesDirectory + "example_04.png";
        String expectedOutput = "How about a bigger font?";

        testImageOcrText(tesseractReader, path, expectedOutput);
    }

    @Test
    public void testFontColor() throws IOException {
        String path = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));
        pdfRenderer.setTextLayerName("Text1");
        Color color = DeviceCmyk.CYAN;
        pdfRenderer.setTextColor(color);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        try {
            Color fillColor = strategy.getFillColor();
            Assert.assertEquals(color, fillColor);
        } finally {
            pdfDocument.close();
            deleteFile(pdfPath);
        }
    }

    @Test
    public void testImageWithoutText() throws IOException {
        String filePath = testImagesDirectory + "pantone_blue.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(filePath);

        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file));

        PdfDocument doc = pdfRenderer.doPdfOcr(new PdfWriter(pdfPath));

        Assert.assertNotNull(doc);

        ImageData imageData = null;
        try {
            imageData = ImageDataFactory.create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        PageSize defaultPageSize = PageSize.A4;
        Image resultImage = getImageFromPdf(tesseractReader, file,
                IPdfRenderer.ScaleMode.scaleToFit, defaultPageSize);

        if (imageData != null) {
            float imageWidth = UtilService.getPoints(imageData.getWidth());
            float imageHeight = UtilService.getPoints(imageData.getHeight());
            float realImageWidth = resultImage.getImageWidth();
            float realImageHeight = resultImage.getImageHeight();

            float realWidth = doc.getFirstPage().getPageSize().getWidth();
            float realHeight = doc.getFirstPage().getPageSize().getHeight();

            Assert.assertEquals(imageWidth / imageHeight,
                    realImageWidth / realImageHeight, delta);
            Assert.assertEquals(defaultPageSize.getHeight(), realHeight, delta);
            Assert.assertEquals(defaultPageSize.getWidth(), realWidth, delta);
        }

        if (!doc.isClosed()) {
            doc.close();
        }

        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        Assert.assertEquals("", strategy.getResultantText());
        pdfDocument.close();
        deleteFile(pdfPath);
    }

    @Test
    public void testInputInvalidImage() throws IOException {
        File file1 = new File(testImagesDirectory + "example.txt");
        File file2 = new File(testImagesDirectory
                + "example_05_corrupted.bmp");
        File file3 = new File(testImagesDirectory
                + "numbers_02.jpg");

        try {
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Arrays.<File>asList(file3, file1, file2, file3));

            pdfRenderer.doPdfOcr(getPdfWriter());
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_INPUT_IMAGE_FORMAT,
                            "txt");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    @Test
    public void testIncorrectPathToTessData() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        try {
            tesseractReader.setPathToTessData(null);
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.CANNOT_FIND_PATH_TO_TESSDATA, e.getMessage());
            tesseractReader.setPathToTessData(getTessDataDirectory());
        }

        try {
            tesseractReader.setPathToTessData("test/");
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            "test/");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        tesseractReader.setPathToTessData(getTessDataDirectory());
        try {
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            langTessDataDirectory);
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @Test
    public void testSimpleTextOutput() {
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        String expectedOutput = "619121";

        String result = getOCRedTextFromTextFile(tesseractReader, imgPath);
        Assert.assertTrue(result.contains(expectedOutput));
    }

    /**
     * Parse text from image and compare with expected.
     *
     * @param path
     * @param expectedOutput
     */
    private void testImageOcrText(TesseractReader tesseractReader, String path,
                                  String expectedOutput) {
        File ex1 = new File(path);

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                ex1);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /**
     * Parse text from given image using tesseract.
     *
     * @param tesseractReader
     * @param file
     * @return
     */
    private String getTextUsingTesseractFromImage(IOcrReader tesseractReader,
                                                  File file) {
        int page = 1;
        List<TextInfo> data = tesseractReader.readDataFromInput(file);
        List<TextInfo> pageText = UtilService.getTextForPage(data, page);

        if (pageText.size() > 0) {
            Assert.assertEquals(4,
                    pageText.get(0).getCoordinates().size());
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (TextInfo text : pageText) {
            stringBuilder.append(text.getText());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().trim();
    }
}
