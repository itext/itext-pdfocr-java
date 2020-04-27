package com.itextpdf.ocr.general;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.layout.element.Image;
import com.itextpdf.ocr.AbstractIntegrationTest;
import com.itextpdf.ocr.IOcrReader;
import com.itextpdf.ocr.IPdfRenderer;
import com.itextpdf.ocr.LogMessageConstant;
import com.itextpdf.ocr.OCRException;
import com.itextpdf.ocr.PdfRenderer;
import com.itextpdf.ocr.TesseractReader;
import com.itextpdf.ocr.TextInfo;
import com.itextpdf.ocr.UtilService;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTesseractIntegrationTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BasicTesseractIntegrationTest.class);

    TesseractReader tesseractReader;
    String parameter;

    public BasicTesseractIntegrationTest(String type) {
        parameter = type;
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testFontColorInMultiPagePdf() throws IOException {
        String path = testImagesDirectory + "multipage.tiff";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        try {
            tesseractReader.setPathToTessData(getTessDataDirectory());
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Collections.<File>singletonList(file));
            pdfRenderer.setTextLayerName("Text1");
            com.itextpdf.kernel.colors.Color color = DeviceCmyk.MAGENTA;
            pdfRenderer.setTextColor(color);

            PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

            Assert.assertNotNull(doc);
            doc.close();

            PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

            ExtractionStrategy strategy = new ExtractionStrategy("Text1");
            PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

            processor.processPageContent(pdfDocument.getPage(1));

            com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
            Assert.assertEquals(fillColor, color);

            pdfDocument.close();
        } finally {
            deleteFile(pdfPath);
        }
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
            LOGGER.error(e.getMessage());
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
            LOGGER.error(e.getMessage());
        }

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        com.itextpdf.kernel.geom.Rectangle pageSize =
                new com.itextpdf.kernel.geom.Rectangle(pageWidthPt, pageHeightPt);
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

            Assert.assertEquals(resultPageWidth, pageWidthPt, delta);
            Assert.assertEquals(resultPageHeight, pageHeightPt, delta);
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
            LOGGER.error(e.getMessage());
        }

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        com.itextpdf.kernel.geom.Rectangle pageSize =
                new com.itextpdf.kernel.geom.Rectangle(pageWidthPt, pageHeightPt);
        Image resultImage = getImageFromPdf(tesseractReader, file,
                IPdfRenderer.ScaleMode.scaleHeight, pageSize);

        if (originalImageData != null) {
            float originalImageHeight = UtilService
                    .getPoints(originalImageData.getHeight());
            float originalImageWidth = UtilService
                    .getPoints(originalImageData.getWidth());

            float resultPageWidth = pageSize.getWidth();
            float resultPageHeight = pageSize.getHeight();

            Assert.assertEquals(resultPageWidth, pageWidthPt, delta);
            Assert.assertEquals(resultPageHeight, pageHeightPt, delta);
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
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.CYAN;
        pdfRenderer.setTextColor(color);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());

        try {
            com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
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
            LOGGER.error(e.getMessage());
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

    @LogMessages(messages = {
        @LogMessage(messageTemplate = LogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 1)
    })
    @Test
    public void testInputInvalidImage() throws IOException {
        File file1 = new File(testImagesDirectory + "example.txt");
        File file2 = new File(testImagesDirectory
                + "example_05_corrupted.bmp");
        File file3 = new File(testImagesDirectory
                + "numbers_02.jpg");

        try {
            tesseractReader.setPathToTessData(getTessDataDirectory());
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Arrays.<File>asList(file3, file1, file2, file3));

            pdfRenderer.doPdfOcr(getPdfWriter());
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_INPUT_IMAGE_FORMAT,
                            "txt");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = OCRException.CANNOT_FIND_PATH_TO_TESSDATA, count = 1),
        @LogMessage(messageTemplate = OCRException.INCORRECT_LANGUAGE, count = 2)
    })
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
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            "test/");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        try {
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            String expectedMsg = MessageFormatUtil
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng.traineddata",
                            scriptTessDataDirectory);
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
        Map<Integer, List<TextInfo>> data = tesseractReader.readDataFromInput(file);
        List<TextInfo> pageText = data.get(page);

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
