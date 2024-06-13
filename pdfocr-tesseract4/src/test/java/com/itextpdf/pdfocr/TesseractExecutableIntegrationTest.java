/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.pdfocr;

import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class TesseractExecutableIntegrationTest extends IntegrationTestHelper {

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                PdfOcrTesseract4ExceptionMessageConstant.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testNullPathToTesseractExecutable() {
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            Tesseract4ExecutableOcrEngine tesseractExecutableReader =
                    new Tesseract4ExecutableOcrEngine(
                            new Tesseract4OcrEngineProperties());
            tesseractExecutableReader.setPathToExecutable(null);
            getTextFromPdf(tesseractExecutableReader, file);
        });

        Assertions.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE,
                exception.getMessage());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                PdfOcrTesseract4ExceptionMessageConstant.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE, count = 1)
    })
    @Test
    public void testEmptyPathToTesseractExecutable() {
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class,
                () -> getTextFromPdf(new Tesseract4ExecutableOcrEngine("", new Tesseract4OcrEngineProperties()), file));
        Assertions.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE,
                exception.getMessage());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate =
                Tesseract4LogMessageConstant.COMMAND_FAILED, count = 1),
        @LogMessage(messageTemplate =
                PdfOcrTesseract4ExceptionMessageConstant.TESSERACT_NOT_FOUND, count = 1)
    })
    @Test
    public void testIncorrectPathToTesseractExecutable() {
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> getTextFromPdf(
                new Tesseract4ExecutableOcrEngine("path\\to\\executable\\", new Tesseract4OcrEngineProperties()),
                file));
        Assertions.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.TESSERACT_NOT_FOUND, exception.getMessage());
    }
}
