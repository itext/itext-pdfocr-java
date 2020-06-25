/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
    Authors: iText Software.

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

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ApiTest extends IntegrationTestHelper {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

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

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testDoTesseractOcrForIncorrectImageForExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                Tesseract4OcrException.CANNOT_READ_PROVIDED_IMAGE,
                new File(TEST_IMAGES_DIRECTORY + "numbers_01")
                        .getAbsolutePath()));
        String path = TEST_IMAGES_DIRECTORY + "numbers_01";
        File imgFile = new File(path);

        Tesseract4ExecutableOcrEngine engine =
                new Tesseract4ExecutableOcrEngine(getTesseractDirectory(),
                        new Tesseract4OcrEngineProperties()
                                .setPathToTessData(getTessDataDirectory()));
        engine.doTesseractOcr(imgFile, null, OutputFormat.HOCR);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.TESSERACT_FAILED),
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.TESSERACT_FAILED)
    })
    @Test
    public void testOcrResultForSinglePageForNullImage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.TESSERACT_FAILED);
        Tesseract4LibOcrEngine tesseract4LibOcrEngine = getTesseract4LibOcrEngine();
        tesseract4LibOcrEngine.setTesseract4OcrEngineProperties(
                new Tesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        tesseract4LibOcrEngine.initializeTesseract(OutputFormat.TXT);
        tesseract4LibOcrEngine.doTesseractOcr(null, null, OutputFormat.HOCR);
    }

    @Test
    public void testDoTesseractOcrForNonAsciiPathForExecutable() {
        String path = TEST_IMAGES_DIRECTORY + "tèst/noisy_01.png";
        File imgFile = new File(path);
        File outputFile = new File(TesseractOcrUtil.getTempFilePath("test",
                ".hocr"));

        Tesseract4OcrEngineProperties properties = new Tesseract4OcrEngineProperties();
        properties.setPathToTessData(getTessDataDirectory());
        properties.setPreprocessingImages(false);
        Tesseract4ExecutableOcrEngine engine =
                new Tesseract4ExecutableOcrEngine(getTesseractDirectory(),
                        properties);
        engine.doTesseractOcr(imgFile, outputFile, OutputFormat.HOCR);
        Assert.assertTrue(Files.exists(Paths.get(outputFile.getAbsolutePath())));
        TesseractHelper.deleteFile(outputFile.getAbsolutePath());
        Assert.assertFalse(Files.exists(Paths.get(outputFile.getAbsolutePath())));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE),
        @LogMessage(messageTemplate = Tesseract4OcrException.TESSERACT_FAILED),
        @LogMessage(messageTemplate = Tesseract4OcrException.TESSERACT_NOT_FOUND),
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.COMMAND_FAILED)
    }, ignore = true)
    @Test
    public void testDoTesseractOcrForExecutableForWin() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        testSettingOsName("win");
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE),
        @LogMessage(messageTemplate = Tesseract4OcrException.TESSERACT_FAILED),
        @LogMessage(messageTemplate = Tesseract4OcrException.TESSERACT_NOT_FOUND),
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.COMMAND_FAILED)
    }, ignore = true)
    @Test
    public void testDoTesseractOcrForExecutableForLinux() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        testSettingOsName("linux");
    }

    private void testSettingOsName(String osName) {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File imgFile = new File(path);

        String tesseractDirectory = getTesseractDirectory();
        String osPropertyName = System.getProperty("os.name") == null ? "OS" : "os.name";
        String os = System.getProperty(osPropertyName);
        System.setProperty(osPropertyName, osName);

        try {
            Tesseract4OcrEngineProperties properties = new Tesseract4OcrEngineProperties();
            properties.setPathToTessData(getTessDataDirectory());
            Tesseract4ExecutableOcrEngine engine =
                    new Tesseract4ExecutableOcrEngine(tesseractDirectory,
                            properties);

            engine.doTesseractOcr(imgFile, null, OutputFormat.HOCR);
        } finally {
            System.setProperty(osPropertyName, os);
        }
    }
}
