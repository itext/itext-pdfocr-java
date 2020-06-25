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
package com.itextpdf.pdfocr;

import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class TesseractExecutableIntegrationTest extends IntegrationTestHelper {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testNullPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Tesseract4ExecutableOcrEngine tesseractExecutableReader =
                new Tesseract4ExecutableOcrEngine(
                        new Tesseract4OcrEngineProperties());
        tesseractExecutableReader.setPathToExecutable(null);
        getTextFromPdf(tesseractExecutableReader, file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testEmptyPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        getTextFromPdf(new Tesseract4ExecutableOcrEngine("", new Tesseract4OcrEngineProperties()), file);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4LogMessageConstant.COMMAND_FAILED, count = 1),
        @LogMessage(messageTemplate =
                Tesseract4OcrException.TESSERACT_NOT_FOUND, count = 1)
    })
    @Test
    public void testIncorrectPathToTesseractExecutable() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.TESSERACT_NOT_FOUND);
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        getTextFromPdf(new Tesseract4ExecutableOcrEngine("path\\to\\executable\\", new Tesseract4OcrEngineProperties()), file);
    }
}
