package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ApiTest extends AbstractIntegrationTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

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

    @Test
    public void testCheckForInvalidTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03_10MB";
        File imgFile = new File(path);
        Assert.assertFalse(ImagePreprocessingUtil.isTiffImage(imgFile));
    }

    @Test
    public void testReadingInvalidImagePath() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        String path = TEST_IMAGES_DIRECTORY + "numbers_02";
        File imgFile = new File(path);
        ImagePreprocessingUtil.preprocessImage(imgFile, 1);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.PATH_TO_TESS_DATA_IS_NOT_SET)
    })
    @Test
    public void testDefaultTessDataPathValidationForLib() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.PATH_TO_TESS_DATA_IS_NOT_SET);
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File imgFile = new File(path);

        Tesseract4LibOcrEngine engine =
                new Tesseract4LibOcrEngine(new Tesseract4OcrEngineProperties());
        engine.doImageOcr(imgFile);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.PATH_TO_TESS_DATA_IS_NOT_SET)
    })
    @Test
    public void testDefaultTessDataPathValidationForExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.PATH_TO_TESS_DATA_IS_NOT_SET);
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File imgFile = new File(path);

        Tesseract4ExecutableOcrEngine engine =
                new Tesseract4ExecutableOcrEngine(getTesseractDirectory(),
                        new Tesseract4OcrEngineProperties());
        engine.doImageOcr(imgFile);
    }
}
