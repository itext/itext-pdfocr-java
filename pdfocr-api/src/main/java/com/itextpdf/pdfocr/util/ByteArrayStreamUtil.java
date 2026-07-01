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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This file is a helper class for internal usage only.
 * Be aware that its API and functionality may be changed in future.
 */
public final class ByteArrayStreamUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayStreamUtil.class);

    private ByteArrayStreamUtil() {
        // do nothing
    }
    /**
     * Wraps {@link InputStream} into {@link ByteArrayInputStream}.
     *
     * @param input {@link InputStream} to be wrapped
     *
     * @return new {@link ByteArrayInputStream} backed by provided {@link InputStream}
     */
    public static ByteArrayInputStream createByteArrayInputStream(InputStream input) {
        if (input instanceof ByteArrayInputStream) {
            return (ByteArrayInputStream) input;
        }
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;

            while ((nRead = input.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return new ByteArrayInputStream(buffer.toByteArray());
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(PdfOcrLogMessageConstant.CANNOT_READ_INPUT_STREAM,
                    e.getMessage()));
            throw new PdfOcrInputException(PdfOcrExceptionMessageConstant.CANNOT_READ_INPUT_STREAM, e);
        }
    }
}
