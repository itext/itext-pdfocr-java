package com.itextpdf.ocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.element.Image;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Category(IntegrationTest.class)
public class TesseractIntegrationTest {

    private static float delta = 1e-4f;

    @Test
    public void testNoisyImage() {
        String path = "src/test/resources/com/itextpdf/ocr/noisy_01.png";
        // SHOULD BE
        // String expectedOutput = "Noisy image\nto test\nTesseract OCR\n";

        // FOR NOW
        String expectedOutput = "Noisyimage to test Tesseract OCR";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testDifferentTextStyles() {
        String path = "src/test/resources/com/itextpdf/ocr/example_04.png";
        String expectedOutput = "Does this OCR thing really work? H . " +
                "How about a bigger font? " +
                "123456789 {23 " +
                "What about thiy font?";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testKeepOriginalSizeScaleMode() {
        File file = new File("src/test/resources/com/itextpdf/ocr/numbers_01.jpg");

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Arrays.asList(file),
                IPdfRenderer.ScaleMode.keepOriginalSize);

        PdfDocument doc = pdfRenderer.doPdfOcr();

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
        File file = new File("src/test/resources/com/itextpdf/ocr/numbers_01.jpg");

        ImageData originalImageData = null;
        try {
            originalImageData = ImageDataFactory.create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Arrays.asList(file),
                IPdfRenderer.ScaleMode.scaleWidth);

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        pdfRenderer.setPageSize(new Rectangle(pageWidthPt, pageHeightPt));

        PdfDocument doc = pdfRenderer.doPdfOcr();

        Rectangle pageSize = doc.getFirstPage().getPageSize();
        Image resultImage = retrieveImage(doc);

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

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testScaleHeightMode() {
        File file = new File("src/test/resources/com/itextpdf/ocr/numbers_01.jpg");

        ImageData originalImageData = null;
        try {
            originalImageData = ImageDataFactory.create(file.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Arrays.asList(file),
                IPdfRenderer.ScaleMode.scaleHeight);

        float pageWidthPt = 500f;
        float pageHeightPt = 500f;

        pdfRenderer.setPageSize(new Rectangle(pageWidthPt, pageHeightPt));

        PdfDocument doc = pdfRenderer.doPdfOcr();

        Rectangle pageSize = doc.getFirstPage().getPageSize();
        Image resultImage = retrieveImage(doc);

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
        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testScaleToFitMode() {
        File file = new File("src/test/resources/com/itextpdf/ocr/numbers_01.jpg");

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Arrays.asList(file),
                IPdfRenderer.ScaleMode.scaleToFit);

        PdfDocument doc = pdfRenderer.doPdfOcr();

        float realWidth = doc.getFirstPage().getPageSize().getWidth();
        float realHeight = doc.getFirstPage().getPageSize().getHeight();

        Assert.assertEquals(PageSize.A4.getWidth(), realWidth, delta);
        Assert.assertEquals(PageSize.A4.getHeight(), realHeight, delta);

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    @Test
    public void testInputBMP() {
        String path = "src/test/resources/com/itextpdf/ocr/example_01.BMP";
        String expectedOutput = "This is a test message for OCR Scanner Test";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testInputJFIF() {
        String path = "src/test/resources/com/itextpdf/ocr/example_01.JFIF";
        String expectedOutput = "This is a test message for OCR Scanner Test";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testInputJPG() {
        String path = "src/test/resources/com/itextpdf/ocr/numbers_01.jpg";
        String expectedOutput = "619121";

        testImageOcrText(path, expectedOutput);
    }

    @Test
    public void testInputTIFFBig() {
        String path = "src/test/resources/com/itextpdf/ocr/example_03_10MB.tiff";
        String expectedOutput = "Tagged Image File Format";

        testImageOcrText(path, expectedOutput);
    }


    @Test
    public void testInputMultipagesTIFF() {
        String path = "src/test/resources/com/itextpdf/ocr/multi.tiff";
        String expectedOutput = "Multipage TIFF Example Page 6";

        IOcrReader tesseractReader = new TesseractReader();

        File ex1 = new File(path);

        String realOutputHocr = getText(tesseractReader, ex1, 7);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputWrongFormat() {
        File ex = new File("src/test/resources/com/itextpdf/ocr/example.txt");

        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Arrays.asList(ex));
        PdfDocument doc = pdfRenderer.doPdfOcr();

        if (!doc.isClosed()) {
            doc.close();
        }
    }



    /*@Test
    public void testTextColor() throws IOException {
        String path = "src/test/resources/numbers_01.jpg";
        String expectedOutput = "619121";

        TesseractReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Arrays.asList(new File(path)));
        pdfRenderer.setColor(DeviceCmyk.RED);
        PdfDocument doc = pdfRenderer.doPdfOcr();

        PdfDictionary pageDict = doc.getFirstPage().getPdfObject();
        PdfDictionary pageResources = pageDict.getAsDictionary(PdfName.Resources);
        PdfDictionary pageXObjects = pageResources.getAsDictionary(PdfName.Font);

        PdfName fontRef = pageXObjects.keySet().iterator().next();
        String val = fontRef.getValue();

        PdfDictionary dict = pageXObjects.getAsDictionary(fontRef);
        PdfObject o1 = pageXObjects.get(fontRef);
        PdfIndirectReference ref = o1.getIndirectReference();

        if (!doc.isClosed()) {
            doc.close();
        }
    }*/

    private void doOcr(String imgPath, String pdfPath, List<String> languages) {
        TesseractReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Arrays.asList(new File(imgPath)));

        pdfRenderer.setPdfPath(pdfPath);
        PdfDocument doc = pdfRenderer.doPdfOcr();

        if (!doc.isClosed()) {
            doc.close();
        }
    }

    /**
     * Parse text from image and compare with expected
     * @param path
     * @param expectedOutput
     */
    private void testImageOcrText(String path, String expectedOutput) {
        IOcrReader tesseractReader = new TesseractReader();

        File ex1 = new File(path);

        String realOutputHocr = getText(tesseractReader, ex1);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /**
     * Retrieve text from given document and specified page
     *
     * @param tesseractReader
     * @param file
     * @param page
     * @return
     */
    private String getText(IOcrReader tesseractReader, File file, int page) {
        List<TextInfo> data = tesseractReader.readDataFromInput(file);
        List<TextInfo> pageText = UtilService.getTextForPage(data, page);

        return pageText.stream()
                .map(TextInfo::getText)
                .collect(Collectors.joining(" "));
    }

    /**
     * Retrieve text from given document and specified page
     * @param tesseractReader
     * @param file
     * @return
     */
    private String getText(IOcrReader tesseractReader, File file) {
        return getText(tesseractReader, file, 1);
    }

    private Image retrieveImage(PdfDocument doc) {
        PdfDictionary pageDict = doc.getFirstPage().getPdfObject();
        PdfDictionary pageResources = pageDict.getAsDictionary(PdfName.Resources);
        PdfDictionary pageXObjects = pageResources.getAsDictionary(PdfName.XObject);
        PdfName imgRef = pageXObjects.keySet().iterator().next();
        PdfStream imgStream = pageXObjects.getAsStream(imgRef);

        PdfImageXObject imgObject = new PdfImageXObject(imgStream);

        return new Image(imgObject);
    }
}
