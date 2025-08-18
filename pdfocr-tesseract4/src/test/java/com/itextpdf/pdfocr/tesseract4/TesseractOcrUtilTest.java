/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TesseractOcrUtilTest extends IntegrationTestHelper {

    @Test
    public void testTesseract4OcrForPix()
            throws TesseractException, IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_02.jpg";
        String expected = "0123456789";
        File imgFile = new File(path);

        Pix pix = TesseractOcrUtil.readPix(imgFile);
        Tesseract4LibOcrEngine tesseract4LibOcrEngine = getTesseract4LibOcrEngine();
        tesseract4LibOcrEngine.setTesseract4OcrEngineProperties(
                new Tesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        tesseract4LibOcrEngine.initializeTesseract(OutputFormat.TXT);

        String result = new TesseractOcrUtil().getOcrResultAsString(
                tesseract4LibOcrEngine.getTesseractInstance(),
                pix, OutputFormat.TXT);
        TesseractOcrUtil.destroyPix(pix);
        Assertions.assertTrue(result.contains(expected));
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
        Assertions.assertTrue(result.contains(expected));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.PAGE_NUMBER_IS_INCORRECT)
    })
    @Test
    public void testReadingSecondPageFromOnePageTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03_10MB.tiff";
        File imgFile = new File(path);
        Pix page = TesseractOcrUtil.readPixPageFromTiff(imgFile, 2);
        Assertions.assertNull(page);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE),
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.PAGE_NUMBER_IS_INCORRECT)
    })
    @Test
    public void testReadingPageFromInvalidTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03.tiff";
        File imgFile = new File(path);
        Pix page = TesseractOcrUtil.readPixPageFromTiff(imgFile, 0);
        Assertions.assertNull(page);
    }

    @Test
    public void testPreprocessingConditions() throws IOException {
        Pix pix = null;
        Assertions.assertNull(TesseractOcrUtil.convertToGrayscale(pix));
        Assertions.assertNull(TesseractOcrUtil.otsuImageThresholding(pix, new ImagePreprocessingOptions()));
        Assertions.assertNull(TesseractOcrUtil.convertPixToImage(pix));
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
        Assertions.assertNull(new TesseractOcrUtil()
                .getOcrResultAsString(
                        tesseract4LibOcrEngine.getTesseractInstance(),
                        pix, OutputFormat.HOCR));
        File file = null;
        Assertions.assertNull(new TesseractOcrUtil()
                .getOcrResultAsString(
                        tesseract4LibOcrEngine.getTesseractInstance(),
                        file, OutputFormat.HOCR));
        BufferedImage bi = null;
        Assertions.assertNull(new TesseractOcrUtil()
                .getOcrResultAsString(
                        tesseract4LibOcrEngine.getTesseractInstance(),
                        bi, OutputFormat.HOCR));
    }

    @Test
    public void testImageSavingAsPng() throws IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String tmpFileName = getTargetDirectory() + "testImageSavingAsPng.png";
        Assertions.assertFalse(Files.exists(Paths.get(tmpFileName)));
        BufferedImage bi = ImageIO.read(new FileInputStream(path));
        TesseractOcrUtil.saveImageToTempPngFile(tmpFileName, bi);
        Assertions.assertTrue(Files.exists(Paths.get(tmpFileName)));
        TesseractHelper.deleteFile(tmpFileName);
        Assertions.assertFalse(Files.exists(Paths.get(tmpFileName)));
    }

    @Test
    public void testNullSavingAsPng() {
        String tmpFileName = TesseractOcrUtil.getTempFilePath(
                getTargetDirectory() + "/testNullSavingAsPng", ".png");
        TesseractOcrUtil.saveImageToTempPngFile(tmpFileName, null);
        Assertions.assertFalse(Files.exists(Paths.get(tmpFileName)));

        TesseractOcrUtil.savePixToPngFile(tmpFileName, null);
        Assertions.assertFalse(Files.exists(Paths.get(tmpFileName)));
    }

    @Test
    public void testPixSavingAsPng() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String tmpFileName = getTargetDirectory() + "testPixSavingAsPng.png";
        Assertions.assertFalse(Files.exists(Paths.get(tmpFileName)));
        Pix pix = TesseractOcrUtil.readPix(new File(path));
        TesseractOcrUtil.savePixToPngFile(tmpFileName, pix);
        TesseractOcrUtil.destroyPix(pix);
        Assertions.assertTrue(Files.exists(Paths.get(tmpFileName)));
        TesseractHelper.deleteFile(tmpFileName);
        Assertions.assertFalse(Files.exists(Paths.get(tmpFileName)));
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


    @Test
    public void testDetectImageRotationAndFix() throws Exception {
        String path = TEST_IMAGES_DIRECTORY + "90_degrees_rotated.jpg";
        TesseractOcrUtil.detectRotation(new File(path));
        ImageData imageData = ImageDataFactory.create(path);
        int rotation = TesseractOcrUtil.detectRotation(imageData);
        Assertions.assertEquals(90, rotation);
        imageData = TesseractOcrUtil.applyRotation(imageData);
        rotation = TesseractOcrUtil.detectRotation(imageData);
        Assertions.assertEquals(0, rotation);

        path = TEST_IMAGES_DIRECTORY + "180_degrees_rotated.jpg";
        TesseractOcrUtil.detectRotation(new File(path));
        imageData = ImageDataFactory.create(path);
        rotation = TesseractOcrUtil.detectRotation(imageData);
        Assertions.assertEquals(180, rotation);
        imageData = TesseractOcrUtil.applyRotation(imageData);
        rotation = TesseractOcrUtil.detectRotation(imageData);
        Assertions.assertEquals(0, rotation);

        path = TEST_IMAGES_DIRECTORY + "270_degrees_rotated.jpg";
        TesseractOcrUtil.detectRotation(new File(path));
        imageData = ImageDataFactory.create(path);
        rotation = TesseractOcrUtil.detectRotation(imageData);
        Assertions.assertEquals(270, rotation);
        imageData = TesseractOcrUtil.applyRotation(imageData);
        rotation = TesseractOcrUtil.detectRotation(imageData);
        Assertions.assertEquals(0, rotation);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    }, ignore = true)
    @Test
    public void testDetectImageRotationNegativeCases() {
        String path = TEST_IMAGES_DIRECTORY + "90_degrees_rotated.jpg_broken_path";
        int rotation = TesseractOcrUtil.detectRotation(new File(path));
        Assertions.assertEquals(0, rotation);

        byte[] data = "broken image".getBytes();
        rotation = TesseractOcrUtil.detectRotation(data);
        Assertions.assertEquals(0, rotation);
    }
}
