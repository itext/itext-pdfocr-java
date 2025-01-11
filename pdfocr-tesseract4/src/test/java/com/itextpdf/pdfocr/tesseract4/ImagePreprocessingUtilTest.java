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
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.sourceforge.lept4j.Pix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImagePreprocessingUtilTest extends IntegrationTestHelper{

    @Test
    public void testCheckForInvalidTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_04.png";
        File imgFile = new File(path);
        Assertions.assertFalse(ImagePreprocessingUtil.isTiffImage(imgFile));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testReadingInvalidImagePath() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_02";
        File imgFile = new File(path);
        Assertions.assertThrows(PdfOcrTesseract4Exception.class,
                () -> ImagePreprocessingUtil.preprocessImage(imgFile, 1, new ImagePreprocessingOptions()));
    }

    @Test
    public void testImagePreprocessingOptions() throws IOException {
        String sourceImg = TEST_IMAGES_DIRECTORY + "thai_02.jpg";
        String processedImg = getTargetDirectory() + "thai_02_processed.jpg";
        String compareImg = TEST_IMAGES_DIRECTORY + "thai_02_cmp_01.jpg";

        Pix pix = ImagePreprocessingUtil.preprocessImage(new File(sourceImg),
                1,
                new ImagePreprocessingOptions());
        TesseractOcrUtil.savePixToPngFile(processedImg, pix);
        TesseractOcrUtil.destroyPix(pix);
        compareImagesWithPrecision(compareImg, processedImg, 0.1);

        compareImg = TEST_IMAGES_DIRECTORY + "thai_02_cmp_02.jpg";
        pix = ImagePreprocessingUtil.preprocessImage(new File(sourceImg),
                1,
                new ImagePreprocessingOptions()
                        .setTileWidth(300)
                        .setTileHeight(300));
        TesseractOcrUtil.savePixToPngFile(processedImg, pix);
        TesseractOcrUtil.destroyPix(pix);
        compareImagesWithPrecision(compareImg, processedImg, 0.1);

        compareImg = TEST_IMAGES_DIRECTORY + "thai_02_cmp_03.jpg";
        pix = ImagePreprocessingUtil.preprocessImage(new File(sourceImg),
                1,
                new ImagePreprocessingOptions()
                        .setTileWidth(300)
                        .setTileHeight(300)
                        .setSmoothTiling(false));
        TesseractOcrUtil.savePixToPngFile(processedImg, pix);
        TesseractOcrUtil.destroyPix(pix);
        compareImagesWithPrecision(compareImg, processedImg, 0.1);
    }

    static private void compareImagesWithPrecision(String img1, String img2, double precisionPercents) throws IOException {
        ImageData imageData1 = ImageDataFactory.create(img1);
        ImageData imageData2 = ImageDataFactory.create(img2);

        Assertions.assertEquals(0, Float.compare(imageData1.getWidth(), imageData2.getWidth()));
        Assertions.assertEquals(0, Float.compare(imageData1.getHeight(), imageData2.getHeight()));

        BufferedImage image1 = ImagePreprocessingUtil.readImageFromFile(new File(img1));
        BufferedImage image2 = ImagePreprocessingUtil.readImageFromFile(new File(img2));

        int inconsistentPixelsCount = 0;
        for (int x = 0; x < imageData1.getWidth(); x++) {
            for (int y = 0; y < imageData1.getHeight(); y++) {
                if (TesseractOcrUtil.getImagePixelColor(image1, x, y) != TesseractOcrUtil.getImagePixelColor(image2, x, y)) {
                    inconsistentPixelsCount++;
                }
            }
        }

        float differencePercentage = (float)(100 * inconsistentPixelsCount) / (imageData1.getWidth() * imageData1.getHeight());
        Assertions.assertTrue(differencePercentage < precisionPercents);
    }

}
