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
package com.itextpdf.pdfocr.events;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Category(IntegrationTest.class)
public class EventCountingLibTest extends EventCountingTest {
    public EventCountingLibTest() {
        super(ReaderType.LIB);
    }

    @Test
    @LogMessages(messages = {@LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE)})
    public void testEventCountingCustomMetaInfoError() {
        File img = new File(TEST_IMAGES_DIRECTORY + "numbers_101.jpg");

        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException
                .expectMessage(MessageFormatUtil
                        .format(Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE, img.getAbsolutePath()));

        super.testEventCountingCustomMetaInfoError();
    }
}
