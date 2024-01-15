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
package com.itextpdf.pdfocr.exceptions;

import com.itextpdf.commons.exceptions.ITextException;
import com.itextpdf.commons.utils.MessageFormatUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Exception class for custom exceptions.
 */
public class PdfOcrException extends ITextException {

    private List<String> messageParams;

    /**
     * Creates a new {@link PdfOcrException}.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public PdfOcrException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new {@link PdfOcrException}.
     *
     * @param msg the detail message.
     */
    public PdfOcrException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@link PdfOcrException}.
     *
     * @param e the cause
     *          which is saved for later retrieval
     *          by {@link #getCause()} method).
     */
    public PdfOcrException(Throwable e) {
        super(e);
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
     *
     * @return params for exception message
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
    public PdfOcrException setMessageParams(String... messageParams) {
        this.messageParams = Arrays.<String>asList(messageParams);
        return this;
    }
}
