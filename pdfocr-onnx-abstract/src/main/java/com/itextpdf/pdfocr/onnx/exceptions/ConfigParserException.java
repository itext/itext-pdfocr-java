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
package com.itextpdf.pdfocr.onnx.exceptions;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;

/**
 * Exception class for exceptions during configuration file parsing.
 */
public class ConfigParserException extends PdfOcrException {

    /**
     * Creates new {@link ConfigParserException} instance.
     *
     * @param message exception message
     */
    protected ConfigParserException(String message) {
        super(message);
    }

    /**
     * Creates an exception for cases, when an unexpected value was found in
     * a configuration file mapping.
     *
     * @param key key under which the value is located
     *
     * @return the created exception
     */
    public static ConfigParserException unexpectedValueForKey(String key) {
        return new ConfigParserException(
                MessageFormatUtil.format(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_VALUE_FOR_CONFIG_KEY, key)
        );
    }

    /**
     * Creates an exception for cases, when an unexpected key was found in
     * a configuration file mapping.
     *
     * @param key key which was found
     *
     * @return the created exception
     */
    public static ConfigParserException unexpectedKey(String key) {
        return new ConfigParserException(
                MessageFormatUtil.format(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_CONFIG_KEY, key)
        );
    }
}
