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
package com.itextpdf.pdfocr.exceptions;

import com.itextpdf.test.ExtendedITextTest;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class PdfOcrExceptionTest extends ExtendedITextTest {

    @Test
    public void ocrExceptionThrowableConstructorTest() {
        Exception cause = new IOException();
        PdfOcrException exception = new PdfOcrException(cause);
        Assertions.assertEquals(cause, exception.getCause());
    }

    @Test
    public void ocrInputExceptionThrowableConstructorTest() {
        Exception cause = new IOException();
        PdfOcrException exception = new PdfOcrInputException(cause);
        Assertions.assertEquals(cause, exception.getCause());
    }

    @Test
    public void ocrInputExceptionStringConstructorTest() {
        String message = "test message";
        PdfOcrException exception = new PdfOcrInputException(message);
        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void ocrExceptiongetMessageParamsTest() {
        String message = "test message {0}";
        String param = "param";
        String expectedMessage = "test message param";
        PdfOcrException exception = new PdfOcrInputException(message);
        exception.setMessageParams(param);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }
}
