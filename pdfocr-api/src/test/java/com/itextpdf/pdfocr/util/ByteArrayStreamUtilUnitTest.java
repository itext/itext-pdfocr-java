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

import com.itextpdf.io.source.ByteUtils;
import com.itextpdf.test.ExtendedITextTest;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class ByteArrayStreamUtilUnitTest extends ExtendedITextTest {

    @Test
    public void byteArrayInputStreamReadTest() throws Exception {
        byte[] data = ByteUtils.getIsoBytes("Hello Test");

        ByteArrayInputStream bis = ByteArrayStreamUtil.createByteArrayInputStream(new ByteArrayInputStream(data));
        byte[] buffer = new byte[8];
        byte[] secondBuffer = new byte[8];
        bis.read(buffer, 0, buffer.length);
        bis.reset();

        bis.read(secondBuffer, 0, secondBuffer.length);
        Assertions.assertArrayEquals(secondBuffer, buffer);
    }
}
