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

import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class PdfInputImageTest extends ExtendedITextTest {

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void corruptedImageTest() {
        File file = new File(PdfHelper.getImagesTestDirectory()
                + "corrupted.jpg");
        Exception e = Assertions.assertThrows(PdfOcrInputException.class,
                () -> PdfHelper.getTextFromPdf(file, "testCorruptedImage"));
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.CANNOT_READ_INPUT_IMAGE, e.getMessage());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void corruptedImageWithoutExtensionTest() {
        File file = new File(PdfHelper.getImagesTestDirectory()
                + "corrupted");
        Exception e = Assertions.assertThrows(PdfOcrInputException.class,
                () -> PdfHelper.getTextFromPdf(file, "testCorruptedImageWithoutExtension"));
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.CANNOT_READ_INPUT_IMAGE, e.getMessage());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void invalidPathWithDotTest() {
        File file = new File("test.Name");
        Exception e = Assertions.assertThrows(PdfOcrInputException.class,
                () -> PdfHelper.getTextFromPdf(file, "testInvalidPathWithDot"));
        Assertions.assertEquals(PdfOcrExceptionMessageConstant.CANNOT_READ_INPUT_IMAGE, e.getMessage());
    }
}
