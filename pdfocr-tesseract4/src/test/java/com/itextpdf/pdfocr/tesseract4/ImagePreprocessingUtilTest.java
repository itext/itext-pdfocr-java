package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.IntegrationTestHelper;

import java.io.File;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ImagePreprocessingUtilTest extends IntegrationTestHelper{

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

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
}
