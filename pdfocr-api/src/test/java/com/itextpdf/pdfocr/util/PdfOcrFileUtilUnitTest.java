/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.util;

import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PdfOcrFileUtilUnitTest extends ExtendedITextTest {

    @Test
    public void writeToTextFileTest() {
        File file = new File(PdfHelper.getImagesTestDirectory() + "writeToTextFileTest.txt");
        Assertions.assertDoesNotThrow(() -> PdfOcrFileUtil.writeToTextFile(file.getAbsolutePath(), "some text"));
        file.delete();
    }

    @Test
    public void writeToTextFileExceptionTest() {
        File nonExists = new File(PdfHelper.getImagesTestDirectory());
        Assertions.assertThrows(Exception.class,
                () -> PdfOcrFileUtil.writeToTextFile(nonExists.getAbsolutePath(), "some text"));
    }

    @Test
    public void writeToStreamTest() {
        File file = new File(PdfHelper.getImagesTestDirectory() + "writeToStreamTest.txt");
        Assertions.assertDoesNotThrow(() -> PdfOcrFileUtil.writeToStream(
                PdfOcrFileUtil.convertToOutputStream(file), "text"));
        file.delete();
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_OPEN_OUTPUT_STREAM,
            ignore = true))
    public void convertToOutputStreamExceptionTest() {
        File nonExists = new File(PdfHelper.getImagesTestDirectory());
        Assertions.assertThrows(Exception.class,
                () -> PdfOcrFileUtil.convertToOutputStream(nonExists).close());
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_OPEN_INPUT_STREAM,
            ignore = true))
    public void convertToInputStreamsExceptionTest() {
        File nonExists = new File(PdfHelper.getImagesTestDirectory() + "nonExistsFile");
        List<File> list = new ArrayList<>();
        list.add(nonExists);
        Assertions.assertThrows(Exception.class,
                () -> PdfOcrFileUtil.convertToInputStreams(list));
    }
}
