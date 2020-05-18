package com.itextpdf.pdfocr.general;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrException;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.PdfRenderer;
import com.itextpdf.pdfocr.ScaleMode;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.tesseract4.OutputFormat;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTesseractIntegrationTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BasicTesseractIntegrationTest.class);

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    Tesseract4OcrEngine tesseractReader;

    @Before
    public void initTessDataPath() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        ocrEngineProperties.setLanguages(new ArrayList<String>());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    public BasicTesseractIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void testFontColorInMultiPagePdf() throws IOException {
        String testName = "testFontColorInMultiPagePdf";
        String path = testImagesDirectory + "multipage.tiff";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.MAGENTA;
        ocrPdfCreatorProperties.setTextColor(color);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                ocrPdfCreatorProperties);
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(1));

        com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
        Assert.assertEquals(fillColor, color);

        pdfDocument.close();
    }

    @Test
    public void testKeepOriginalSizeScaleMode() throws IOException {
        String filePath = testImagesDirectory + "numbers_01.jpg";
        File file = new File(filePath);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);

        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        getPdfWriter());

        Assert.assertNotNull(doc);

        ImageData imageData = ImageDataFactory.create(file.getAbsolutePath());

        float imageWidth = getPoints(imageData.getWidth());
        float imageHeight = getPoints(imageData.getHeight());
        float realWidth = doc.getFirstPage().getPageSize().getWidth();
        float realHeight = doc.getFirstPage().getPageSize().getHeight();

        Assert.assertEquals(imageWidth, realWidth, delta);
        Assert.assertEquals(imageHeight, realHeight, delta);

        doc.close();
    }

    @Test
    public void testScaleWidthMode() throws IOException {
        String testName = "testScaleWidthMode";
        String srcPath = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";

        File file = new File(srcPath);

        float pageWidthPt = 400f;
        float pageHeightPt = 400f;

        com.itextpdf.kernel.geom.Rectangle pageSize =
                new com.itextpdf.kernel.geom.Rectangle(pageWidthPt, pageHeightPt);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setScaleMode(ScaleMode.SCALE_WIDTH);
        properties.setPageSize(pageSize);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);

        PdfDocument doc = pdfRenderer.createPdf(
                Collections.<File>singletonList(file), getPdfWriter(pdfPath));
        doc.close();

        com.itextpdf.kernel.geom.Rectangle rect = getImageBBoxRectangleFromPdf(pdfPath);
        ImageData originalImageData = ImageDataFactory.create(file.getAbsolutePath());

        // page size should be equal to the result image size
        // result image height should be equal to the value that
        // was set as page height result image width should be scaled
        // proportionally according to the provided image height
        // and original image size
        Assert.assertEquals(pageHeightPt, rect.getHeight(), delta);
        Assert.assertEquals(originalImageData.getWidth() / originalImageData.getHeight(),
                rect.getWidth() / rect.getHeight(), delta);
    }

    @Test
    public void testScaleHeightMode() throws IOException {
        String testName = "testScaleHeightMode";
        String srcPath = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";

        File file = new File(srcPath);

        float pageWidthPt = 400f;
        float pageHeightPt = 400f;

        com.itextpdf.kernel.geom.Rectangle pageSize =
                new com.itextpdf.kernel.geom.Rectangle(pageWidthPt, pageHeightPt);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setScaleMode(ScaleMode.SCALE_HEIGHT);
        properties.setPageSize(pageSize);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);

        PdfDocument doc = pdfRenderer.createPdf(
                Collections.<File>singletonList(file), getPdfWriter(pdfPath));
        doc.close();

        com.itextpdf.kernel.geom.Rectangle rect = getImageBBoxRectangleFromPdf(pdfPath);
        ImageData originalImageData = ImageDataFactory.create(file.getAbsolutePath());

        Assert.assertEquals(pageWidthPt, rect.getWidth(), delta);
        Assert.assertEquals(originalImageData.getWidth() / originalImageData.getHeight(),
                rect.getWidth() / rect.getHeight(), delta);
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
    public void testPantoneImage() {
        String filePath = testImagesDirectory + "pantone_blue.jpg";
        String expected = "";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(filePath));
        Assert.assertEquals(expected, realOutputHocr);
    }

    @Test
    public void testDifferentTextStyles() {
        String path = testImagesDirectory + "example_04.png";
        String expectedOutput = "How about a bigger font?";

        testImageOcrText(tesseractReader, path, expectedOutput);
    }

    @Test
    public void testFontColor() throws IOException {
        String testName = "testFontColor";
        String path = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.CYAN;
        properties.setTextColor(color);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();

        com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
        Assert.assertEquals(color, fillColor);
    }

    @Test
    public void testImageWithoutText() throws IOException {
        String testName = "testImageWithoutText";
        String filePath = testImagesDirectory + "pantone_blue.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(filePath);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);

        pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        new PdfWriter(pdfPath)).close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();

        Assert.assertEquals("", strategy.getResultantText());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CannotReadInputImage, count = 1)
    })
    @Test
    public void testInputInvalidImage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectInputImageFormat,
                        "txt"));

        File file1 = new File(testImagesDirectory + "example.txt");
        File file2 = new File(testImagesDirectory
                + "example_05_corrupted.bmp");
        File file3 = new File(testImagesDirectory
                + "numbers_02.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader);

        pdfRenderer.createPdf(Arrays.<File>asList(file3, file1, file2, file3), getPdfWriter());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.CannotFindPathToTessDataDirectory, count = 1)
    })
    @Test
    public void testNullPathToTessData() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CannotFindPathToTessDataDirectory);
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(null));
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.IncorrectLanguage, count
                = 1)
    })
    @Test
    public void testPathToTessDataWithoutData() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectLanguage,
                        "eng.traineddata",
                        "test/"));

        File file = new File(testImagesDirectory + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData("test/"));
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.CannotFindPathToTessDataDirectory, count
                = 1)
    })
    @Test
    public void testIncorrectPathToTessData3() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CannotFindPathToTessDataDirectory);

        File file = new File(testImagesDirectory + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(""));
        getTextFromPdf(tesseractReader, file);

        Assert.assertEquals("",
                tesseractReader.getTesseract4OcrEngineProperties().getPathToTessData());
    }

    @Test
    public void testSimpleTextOutput() {
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        String expectedOutput = "619121";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
        Assert.assertTrue(result.contains(expectedOutput));
    }

    @Test
    public void testTxtStringOutput() {
        File file = new File(testImagesDirectory + "multipage.tiff");
        List<String> expectedOutput = Arrays.<String>asList(
                "Multipage\nTIFF\nExample\nPage 1",
                "Multipage\nTIFF\nExample\nPage 2",
                "Multipage\nTIFF\nExample\nPage 4",
                "Multipage\nTIFF\nExample\nPage 5",
                "Multipage\nTIFF\nExample\nPage 6",
                "Multipage\nTIFF\nExample\nPage /",
                "Multipage\nTIFF\nExample\nPage 8",
                "Multipage\nTIFF\nExample\nPage 9"
        );

        String result = tesseractReader.doImageOcr(file, OutputFormat.TXT);
        for (String line : expectedOutput) {
            Assert.assertTrue(result.replaceAll("\r", "").contains(line));
        }
    }

    @Test
    public void testHocrStringOutput() {
        File file = new File(testImagesDirectory + "multipage.tiff");
        List<String> expectedOutput = Arrays.<String>asList(
                "Multipage\nTIFF\nExample\nPage 1",
                "Multipage\nTIFF\nExample\nPage 2",
                "Multipage\nTIFF\nExample\nPage 4",
                "Multipage\nTIFF\nExample\nPage 5",
                "Multipage\nTIFF\nExample\nPage 6",
                "Multipage\nTIFF\nExample\nPage /",
                "Multipage\nTIFF\nExample\nPage 8",
                "Multipage\nTIFF\nExample\nPage 9"
        );

        String result = tesseractReader.doImageOcr(file, OutputFormat.HOCR);
        for (String line : expectedOutput) {
            Assert.assertTrue(result.replaceAll("\r", "").contains(line));
        }
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CannotReadProvidedFont, count = 1)
    })
    @Test
    public void testInvalidFont() throws IOException {
        String testName = "testImageWithoutText";
        String expectedOutput = "619121";
        String path = testImagesDirectory + "numbers_01.jpg";
        String pdfPath = testImagesDirectory + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setFontPath("font.ttf");
        properties.setScaleMode(ScaleMode.SCALE_TO_FIT);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        String result = getTextFromPdfLayer(pdfPath, "Text Layer", 1);
        Assert.assertEquals(expectedOutput, result);
        Assert.assertEquals(ScaleMode.SCALE_TO_FIT,
                pdfRenderer.getOcrPdfCreatorProperties().getScaleMode());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.IncorrectLanguage,
                    count = 1)
    })
    @Test
    public void testIncorrectLanguage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectLanguage,
                        "spa_new.traineddata", langTessDataDirectory));
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("spa_new"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.IncorrectLanguage,
                    count = 1)
    })
    @Test
    public void testListOfLanguagesWithOneIncorrectLanguage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectLanguage,
                        "spa_new.traineddata",
                        langTessDataDirectory));
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        getTextFromPdf(tesseractReader, file, Arrays.<String>asList("spa", "spa_new", "spa_old"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.IncorrectLanguage,
                    count = 1)
    })
    @Test
    public void testIncorrectScriptsName() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectLanguage,
                        "English.traineddata",
                        scriptTessDataDirectory));

        File file = new File(testImagesDirectory + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(scriptTessDataDirectory));
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("English"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.IncorrectLanguage, count
                    = 1)
    })
    @Test
    public void testListOfScriptsWithOneIncorrect() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.IncorrectLanguage,
                        "English.traineddata",
                        scriptTessDataDirectory));

        File file = new File(testImagesDirectory + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(scriptTessDataDirectory));
        getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("Georgian", "Japanese", "English"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CannotReadInputImage, count
                    = 1)
    })
    @Test
    public void testCorruptedImage() {
        junitExpectedException.expect(OcrException.class);

        File file = new File(testImagesDirectory
                + "corrupted.jpg");
        String realOutput = getTextFromPdf(tesseractReader, file);
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CannotReadInputImage, count = 1)
    })
    @Test
    public void testCorruptedImageWithoutExtesion() {
        junitExpectedException.expect(OcrException.class);

        File file = new File(testImagesDirectory
                + "corrupted");
        String realOutput = getTextFromPdf(tesseractReader, file);
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    /**
     * Parse text from image and compare with expected.
     */
    private void testImageOcrText(Tesseract4OcrEngine tesseractReader, String path,
                                  String expectedOutput) {
        File ex1 = new File(path);

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                ex1);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /**
     * Parse text from given image using tesseract.
     */
    private String getTextUsingTesseractFromImage(IOcrEngine tesseractReader,
                                                  File file) {
        int page = 1;
        Map<Integer, List<TextInfo>> data = tesseractReader.doImageOcr(file);
        List<TextInfo> pageText = data.get(page);

        if (pageText == null || pageText.size() == 0) {
            pageText = new ArrayList<TextInfo>();
            TextInfo textInfo = new TextInfo();
            textInfo.setCoordinates(Arrays.<Float>asList(0f, 0f, 0f, 0f));
            textInfo.setText("");
            pageText.add(textInfo);
        }
        Assert.assertEquals(4,
                pageText.get(0).getCoordinates().size());

        StringBuilder stringBuilder = new StringBuilder();
        for (TextInfo text : pageText) {
            stringBuilder.append(text.getText());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().trim();
    }
}
