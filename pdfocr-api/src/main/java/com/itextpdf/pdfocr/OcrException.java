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

import com.itextpdf.io.util.MessageFormatUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Exception class for custom exceptions.
 */
public class OcrException extends RuntimeException {

    public static final String CANNOT_READ_INPUT_IMAGE =
            "Cannot read input image";
    public static final String CANNOT_RESOLVE_PROVIDED_FONTS = "Cannot resolve "
            + "any of provided fonts. Please check provided FontProvider.";
    public static final String CANNOT_CREATE_PDF_DOCUMENT = "Cannot create "
            + "PDF document: {0}";
    private List<String> messageParams;

    /**
     * Creates a new OcrException.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public OcrException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new OcrException.
     *
     * @param msg the detail message.
     */
    public OcrException(String msg) {
        super(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return this.messageParams != null && this.messageParams.size() != 0
                ? MessageFormatUtil
                        .format(super.getMessage(), this.getMessageParams())
                : super.getMessage();
    }

    /**
     * Gets additional params for Exception message.
     */
    protected Object[] getMessageParams() {
        Object[] parameters = new Object[this.messageParams.size()];

        for(int i = 0; i < this.messageParams.size(); ++i) {
            parameters[i] = this.messageParams.get(i);
        }

        return parameters;
    }

    /**
     * Sets additional params for Exception message.
     *
     * @param messageParams additional params.
     * @return object itself.
     */
    public OcrException setMessageParams(String... messageParams) {
        this.messageParams = Arrays.<String>asList(messageParams);
        return this;
    }
}
