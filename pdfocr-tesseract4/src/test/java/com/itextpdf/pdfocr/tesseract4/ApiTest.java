/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ApiTest extends IntegrationTestHelper {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_IS_NOT_SET)
    })
    @Test
    public void testDefaultTessDataPathValidationForLib() {
        junitExpectedException.expect(PdfOcrTesseract4Exception.class);
        junitExpectedException.expectMessage(PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_IS_NOT_SET);
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File imgFile = new File(path);

        Tesseract4LibOcrEngine engine =
                new Tesseract4LibOcrEngine(new Tesseract4OcrEngineProperties());
        engine.doImageOcr(imgFile);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_IS_NOT_SET)
    })
    @Test
    public void testDefaultTessDataPathValidationForExecutable() {
        junitExpectedException.expect(PdfOcrTesseract4Exception.class);
        junitExpectedException.expectMessage(PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_IS_NOT_SET);
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File imgFile = new File(path);

        Tesseract4ExecutableOcrEngine engine = new Tesseract4ExecutableOcrEngine(new Tesseract4OcrEngineProperties());
        engine.doImageOcr(imgFile);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 2)
    })
    @Test
    public void testDoTesseractOcrForIncorrectImageForExecutable() {
        junitExpectedException.expect(PdfOcrTesseract4Exception.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                PdfOcrTesseract4ExceptionMessageConstant.CANNOT_READ_PROVIDED_IMAGE,
                new File(TEST_IMAGES_DIRECTORY + "numbers_01")
                        .getAbsolutePath()));
        String path = TEST_IMAGES_DIRECTORY + "numbers_01";
        File imgFile = new File(path);

        Tesseract4ExecutableOcrEngine engine =
                new Tesseract4ExecutableOcrEngine(
                        new Tesseract4OcrEngineProperties().setPathToTessData(getTessDataDirectory()));
        engine.doTesseractOcr(imgFile, null, OutputFormat.HOCR);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE),
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.TESSERACT_FAILED),
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.TESSERACT_FAILED)
    })
    @Test
    public void testOcrResultForSinglePageForNullImage() {
        junitExpectedException.expect(PdfOcrTesseract4Exception.class);
        junitExpectedException.expectMessage(PdfOcrTesseract4ExceptionMessageConstant.TESSERACT_FAILED);
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
        Tesseract4ExecutableOcrEngine engine = new Tesseract4ExecutableOcrEngine(properties);
        engine.doTesseractOcr(imgFile, outputFile, OutputFormat.HOCR);
        Assert.assertTrue(Files.exists(Paths.get(outputFile.getAbsolutePath())));
        TesseractHelper.deleteFile(outputFile.getAbsolutePath());
        Assert.assertFalse(Files.exists(Paths.get(outputFile.getAbsolutePath())));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_PARSE_NODE_BBOX, count = 4)
    })
    @Test
    public void testDetectAndFixBrokenBBoxes() throws IOException {
        File hocrFile = new File(TEST_DOCUMENTS_DIRECTORY + "broken_bboxes.hocr");
        Map<Integer, List<TextInfo>> parsedHocr = TesseractHelper.parseHocrFile(Collections.singletonList(hocrFile),
                null,
                new Tesseract4OcrEngineProperties().setTextPositioning(TextPositioning.BY_WORDS_AND_LINES));
        TextInfo textInfo = parsedHocr.get(1).get(1);

        Assert.assertEquals(287.25, (float)textInfo.getBboxRect().getLeft(), 0.1);
        Assert.assertEquals(136.5f, (float)textInfo.getBboxRect().getBottom(), 0.1);
        Assert.assertEquals(385.5, (float)textInfo.getBboxRect().getRight(), 0.1);
        Assert.assertEquals(162.75, (float)textInfo.getBboxRect().getTop(), 0.1);
    }

}
