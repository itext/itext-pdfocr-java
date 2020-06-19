package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Test;

public class TesseractOcrUtilTest extends IntegrationTestHelper {

    @Test
    public void testTesseract4OcrForPix()
            throws TesseractException, IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_02.jpg";
        String expected = "0123456789";
        File imgFile = new File(path);

        Pix pix = ImagePreprocessingUtil.readPix(imgFile);
        Tesseract4LibOcrEngine tesseract4LibOcrEngine = getTesseract4LibOcrEngine();
        tesseract4LibOcrEngine.setTesseract4OcrEngineProperties(
                new Tesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        tesseract4LibOcrEngine.initializeTesseract(OutputFormat.TXT);

        String result = new TesseractOcrUtil().getOcrResultAsString(
                tesseract4LibOcrEngine.getTesseractInstance(),
                pix, OutputFormat.TXT);
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void testGetOcrResultAsStringForFile()
            throws TesseractException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String expected = "619121";
        File imgFile = new File(path);

        Tesseract4LibOcrEngine tesseract4LibOcrEngine = getTesseract4LibOcrEngine();
        tesseract4LibOcrEngine.setTesseract4OcrEngineProperties(
                new Tesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        tesseract4LibOcrEngine.initializeTesseract(OutputFormat.TXT);

        String result = new TesseractOcrUtil().getOcrResultAsString(
                tesseract4LibOcrEngine.getTesseractInstance(),
                imgFile, OutputFormat.TXT);
        Assert.assertTrue(result.contains(expected));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.PAGE_NUMBER_IS_INCORRECT)
    })
    @Test
    public void testReadingSecondPageFromOnePageTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03_10MB.tiff";
        File imgFile = new File(path);
        Pix page = TesseractOcrUtil.readPixPageFromTiff(imgFile, 2);
        Assert.assertNull(page);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE)
    })
    @Test
    public void testReadingPageFromInvalidTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03.tiff";
        File imgFile = new File(path);
        Pix page = TesseractOcrUtil.readPixPageFromTiff(imgFile, 0);
        Assert.assertNull(page);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE)
    })
    @Test
    public void testInitializeImagesListFromInvalidTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03.tiff";
        File imgFile = new File(path);
        TesseractOcrUtil tesseractOcrUtil = new TesseractOcrUtil();
        tesseractOcrUtil.initializeImagesListFromTiff(imgFile);
        Assert.assertEquals(0, tesseractOcrUtil.getListOfPages().size());
    }

    @Test
    public void testPreprocessingConditions() throws IOException {
        Pix pix = null;
        Assert.assertNull(TesseractOcrUtil.convertToGrayscale(pix));
        Assert.assertNull(TesseractOcrUtil.otsuImageThresholding(pix));
        Assert.assertNull(TesseractOcrUtil.convertPixToImage(pix));
        TesseractOcrUtil.destroyPix(pix);
    }

    @Test
    public void testOcrResultConditions() throws IOException,
            TesseractException {
        Tesseract4LibOcrEngine tesseract4LibOcrEngine = getTesseract4LibOcrEngine();
        tesseract4LibOcrEngine.setTesseract4OcrEngineProperties(
                new Tesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        tesseract4LibOcrEngine.initializeTesseract(OutputFormat.HOCR);

        Pix pix = null;
        Assert.assertNull(new TesseractOcrUtil()
                .getOcrResultAsString(
                        tesseract4LibOcrEngine.getTesseractInstance(),
                        pix, OutputFormat.HOCR));
        File file = null;
        Assert.assertNull(new TesseractOcrUtil()
                .getOcrResultAsString(
                        tesseract4LibOcrEngine.getTesseractInstance(),
                        file, OutputFormat.HOCR));
        BufferedImage bi = null;
        Assert.assertNull(new TesseractOcrUtil()
                .getOcrResultAsString(
                        tesseract4LibOcrEngine.getTesseractInstance(),
                        bi, OutputFormat.HOCR));
    }

    @Test
    public void testImageSavingAsPng() throws IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String tmpFileName = getTargetDirectory() + "testImageSavingAsPng.png";
        Assert.assertFalse(Files.exists(Paths.get(tmpFileName)));
        BufferedImage bi = ImageIO.read(new FileInputStream(path));
        TesseractOcrUtil.saveImageToTempPngFile(tmpFileName, bi);
        Assert.assertTrue(Files.exists(Paths.get(tmpFileName)));
        TesseractHelper.deleteFile(tmpFileName);
        Assert.assertFalse(Files.exists(Paths.get(tmpFileName)));
    }

    @Test
    public void testNullSavingAsPng() {
        String tmpFileName = TesseractOcrUtil.getTempFilePath(
                getTargetDirectory() + "/testNullSavingAsPng", ".png");
        TesseractOcrUtil.saveImageToTempPngFile(tmpFileName, null);
        Assert.assertFalse(Files.exists(Paths.get(tmpFileName)));

        TesseractOcrUtil.savePixToTempPngFile(tmpFileName, null);
        Assert.assertFalse(Files.exists(Paths.get(tmpFileName)));
    }

    @Test
    public void testPixSavingAsPng() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String tmpFileName = getTargetDirectory() + "testPixSavingAsPng.png";
        Assert.assertFalse(Files.exists(Paths.get(tmpFileName)));
        Pix pix = ImagePreprocessingUtil.readPix(new File(path));
        TesseractOcrUtil.savePixToTempPngFile(tmpFileName, pix);
        Assert.assertTrue(Files.exists(Paths.get(tmpFileName)));
        TesseractHelper.deleteFile(tmpFileName);
        Assert.assertFalse(Files.exists(Paths.get(tmpFileName)));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_PROCESS_IMAGE)
    })
    @Test
    public void testImageSavingAsPngWithError() throws IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        BufferedImage bi = ImageIO.read(new FileInputStream(path));
        TesseractOcrUtil.saveImageToTempPngFile(null, bi);
    }
}
