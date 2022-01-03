/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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

import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class PdfInputImageTest extends ExtendedITextTest {
    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testCorruptedImage() {
        junitExpectedException.expect(PdfOcrInputException.class);
        File file = new File(PdfHelper.getImagesTestDirectory()
                + "corrupted.jpg");
        String realOutput = PdfHelper.getTextFromPdf(file, "testCorruptedImage");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testCorruptedImageWithoutExtension() {
        junitExpectedException.expect(PdfOcrInputException.class);

        File file = new File(PdfHelper.getImagesTestDirectory()
                + "corrupted");
        String realOutput = PdfHelper.getTextFromPdf(file, "testCorruptedImageWithoutExtension");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testInvalidImagePathWithoutDot() {
        junitExpectedException.expect(PdfOcrInputException.class);

        File file = new File("testName");
        String realOutput = PdfHelper.getTextFromPdf(file, "testInvalidImagePathWithoutDot");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testInvalidImagePathWithDot() {
        junitExpectedException.expect(PdfOcrInputException.class);

        File file = new File("test.Name");
        String realOutput = PdfHelper.getTextFromPdf(file, "testInvalidImagePathWithDot");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    @Test
    public void testValidImageWithoutExtension() {
        junitExpectedException.expect(PdfOcrInputException.class);

        File file = new File(PdfHelper.getImagesTestDirectory() + "numbers_01");
        String realOutput = PdfHelper.getTextFromPdf(file, "testValidImageWithoutExtension");
        Assert.assertNotNull(realOutput);
        Assert.assertEquals("", realOutput);
    }
}
