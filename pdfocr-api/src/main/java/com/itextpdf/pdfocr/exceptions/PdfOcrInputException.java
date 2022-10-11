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
package com.itextpdf.pdfocr.exceptions;

/**
 * Exception class for input related exceptions.
 */
public class PdfOcrInputException extends PdfOcrException {

    /**
     * Creates a new {@link PdfOcrInputException}.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public PdfOcrInputException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new {@link PdfOcrInputException}.
     *
     * @param msg the detail message.
     */
    public PdfOcrInputException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@link PdfOcrInputException}.
     *
     * @param e the cause
     *          which is saved for later retrieval
     *          by {@link #getCause()} method).
     */
    public PdfOcrInputException(Throwable e) {
        super(e);
    }

}
