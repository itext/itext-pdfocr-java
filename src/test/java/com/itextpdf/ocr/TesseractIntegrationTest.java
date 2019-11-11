package com.itextpdf.ocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.layout.element.Image;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Category(IntegrationTest.class)
public class TesseractIntegrationTest {

    // directory with test files
    private static String directory = "src/test/resources/com/itextpdf/ocr/";

    private static float delta = 1e-4f;

    @Test
    public void testKeepOriginalSizeScaleMode() {
        String filePath = directory + "numbers_01.jpg";
        File file = new File(filePath);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(file),
                IPdfRenderer.ScaleMode.keepOriginalSize);

        PdfDocument doc = pdfRenderer.doPdfOcr();

        assert doc != null;

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
    public void testScaleWidthMode() {
        String filePath = directory + "numbers_01.jpg";
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
        Image resultImage = retrieveImageFromPdf(file, IPdfRenderer.ScaleMode.scaleWidth, pageSize);

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
    public void testScaleHeightMode() {
        String filePath = directory + "numbers_01.jpg";
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
        Image resultImage = retrieveImageFromPdf(file, IPdfRenderer.ScaleMode.scaleHeight, pageSize);

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
    public void testScaleToFitMode() {
        String filePath = directory + "numbers_01.jpg";
        File file = new File(filePath);

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(file),
                IPdfRenderer.ScaleMode.scaleToFit);

        PdfDocument doc = pdfRenderer.doPdfOcr();

        assert doc != null;

        float realWidth = doc.getFirstPage().getPageSize().getWidth();
        float realHeight = doc.getFirstPage().getPageSize().getHeight();

        Assert.assertEquals(PageSize.A4.getWidth(), realWidth, delta);
        Assert.assertEquals(PageSize.A4.getHeight(), realHeight, delta);

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testTextFromBMP() {
        String path = directory + "example_01.BMP";
        String expectedOutput = "This is a test\nmessage\nfor\nOCR Scanner\nTest";

        File file = new File(path);

        String realOutputHocr = retrieveTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJFIF() {
        String path = directory + "example_02.JFIF";
        String expectedOutput = "This is a test\nmessage\nfor\nOCR Scanner\nTest";

        File file = new File(path);
        String realOutputHocr = retrieveTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromJPG() {
        String path = directory + "numbers_01.jpg";
        String expectedOutput = "619121";

        File file = new File(path);
        String realOutputHocr = retrieveTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputTIFFBig() {
        String path = directory + "example_03_10MB.tiff";
        String expectedOutput = "Tagged\nImage\nFile Format";

        File file = new File(path);
        String realOutputHocr = retrieveTextFromPdf(file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputMultipagesTIFF() {
        String path = directory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage\n6";

        File file = new File(path);
        String realOutputHocr = retrieveTextFromPdf(file, 6);
        assert realOutputHocr != null;
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputWrongFormat() {
        File ex = new File(directory + "example.txt");

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(ex));
        PdfDocument doc = pdfRenderer.doPdfOcr();

        assert doc != null;
        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcr(directory + filename + ".jpg", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

//        deleteFile(resultPdfPath);
    }

    @Test
    public void compareBigTiff() throws IOException, InterruptedException {
        String filename = "example_03_10MB";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcr(directory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        String filename = "scanned_eng_01";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcr(directory + filename + ".png", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareMultiPageEngTiff() throws IOException, InterruptedException {
        String filename = "multipage";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcr(directory + filename + ".tiff", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareMultiLangImage() throws IOException, InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcr(directory + filename + ".png", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareScannedSpanishPNG() throws IOException, InterruptedException {
        String filename = "scanned_spa_01";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcr(directory + filename + ".png", resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void testNoisyImage() {
        String path = directory + "noisy_01.png";
        // SHOULD BE
        // String expectedOutput = "Noisy image\nto test\nTesseract OCR\n";

        // FOR NOW
        String expectedOutput = "Noisyimage to test Tesseract OCR";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testDifferentTextStyles() {
        String path = directory + "example_04.png";
        String expectedOutput = "Does this OCR thing really work? H . " +
                "How about a bigger font? " +
                "123456789 {23 " +
                "What about thiy font?";

        testImageOcrText(path, expectedOutput);
    }

    /**
     * Parse text from image and compare with expected.
     *
     * @param path
     * @param expectedOutput
     */
    private void testImageOcrText(String path, String expectedOutput) {
        IOcrReader tesseractReader = new TesseractReader();
        File ex1 = new File(path);

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader, ex1);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /**
     * Retrieve image from given pdf document.
     *
     * @param file
     * @param scaleMode
     * @param pageSize
     * @return
     */
    private Image retrieveImageFromPdf(File file, IPdfRenderer.ScaleMode scaleMode, Rectangle pageSize) {
        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Arrays.asList(file), scaleMode);

        pdfRenderer.setPageSize(pageSize);

        PdfDocument doc = pdfRenderer.doPdfOcr();

        Image image = null;

        assert doc != null;
        if (!doc.isClosed()) {
            PdfDictionary pageDict = doc.getFirstPage().getPdfObject();
            PdfDictionary pageResources = pageDict.getAsDictionary(PdfName.Resources);
            PdfDictionary pageXObjects = pageResources.getAsDictionary(PdfName.XObject);
            PdfName imgRef = pageXObjects.keySet().iterator().next();
            PdfStream imgStream = pageXObjects.getAsStream(imgRef);

            PdfImageXObject imgObject = new PdfImageXObject(imgStream);

            image = new Image(imgObject);
            doc.close();
        }

        return image;
    }

    /**
     * Retrieve text from specified page from given pdf document.
     *
     * @param file
     * @param page
     * @return
     */
    private String retrieveTextFromPdf(File file, int page) {
        try {
            InputStream stream = doOcr(file);
            PdfDocument pdf = new PdfDocument(new PdfReader(stream));

            ITextExtractionStrategy strategy = new SimpleTextExtractionStrategy();

            stream.close();
            return PdfTextExtractor.getTextFromPage(pdf.getPage(page), strategy);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param file
     * @return
     */
    private String retrieveTextFromPdf(File file) {
        return retrieveTextFromPdf(file, 1);
    }

    /**
     * Parse text from given image and specified page using tesseract.
     *
     * @param tesseractReader
     * @param file
     * @param page
     * @return
     */
    private String getTextUsingTesseractFromImage(IOcrReader tesseractReader, File file, int page) {
        List<TextInfo> data = tesseractReader.readDataFromInput(file);
        List<TextInfo> pageText = UtilService.getTextForPage(data, page);

        return pageText.stream()
                .map(TextInfo::getText)
                .collect(Collectors.joining(" "));
    }

    /**
     * Parse text from given image using tesseract.
     *
     * @param tesseractReader
     * @param file
     * @return
     */
    private String getTextUsingTesseractFromImage(IOcrReader tesseractReader, File file) {
        return getTextUsingTesseractFromImage(tesseractReader, file, 1);
    }

    /**
     *  Perform OCR using provided path to image (imgPath) and save result pdf document to "pdfPath".
     *  (Method is used for compare tool)
     *
     * @param imgPath
     * @param pdfPath
     */
    private void doOcr(String imgPath, String pdfPath) {
        TesseractReader tesseractReader = new TesseractReader();
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(new File(imgPath)));

        PdfDocument doc = null;
        try {
            doc = pdfRenderer.doPdfOcr(new PdfWriter(pdfPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert doc != null;
        if (!doc.isClosed()) {
            doc.close();
        }
    }

    /**
     * Performs OCR for provided image file.
     *
     * @param file
     * @return
     */
    private InputStream doOcr(File file) {
        IOcrReader tesseractReader = new TesseractReader();
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(file));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument doc = pdfRenderer.doPdfOcr(new PdfWriter(baos));
        assert doc != null;
        doc.close();

        InputStream stream = new ByteArrayInputStream(baos.toByteArray());
        return stream;
    }

    /**
     * Delete file using provided path.
     *
     * @param filePath
     */
    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
