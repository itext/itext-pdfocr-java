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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.OcrException;

public class Tesseract4OcrException extends OcrException {
    public static final String INCORRECT_INPUT_IMAGE_FORMAT =
            "{0} format is not supported.";
    public static final String INCORRECT_LANGUAGE =
            "{0} does not exist in {1}";
    public static final String LANGUAGE_IS_NOT_IN_THE_LIST =
            "Provided list of languages doesn't contain {0} language";
    public static final String CANNOT_READ_PROVIDED_IMAGE =
            "Cannot read input image {0}";
    public static final String TESSERACT_FAILED = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String TESSERACT_LIB_NOT_INSTALLED = "Tesseract failed. "
            + "Please ensure you have tesseract library installed";
    public static final String TESSERACT_LIB_NOT_INSTALLED_WIN = "Tesseract failed. "
            + "Please ensure you have latest Visual C++ Redistributable installed";
    public static final String TESSERACT_NOT_FOUND = "Tesseract failed. "
            + "Please check that tesseract is installed and provided path to "
            + "tesseract executable directory is correct";
    public static final String CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE =
            "Cannot find path to tesseract executable.";
    public static final String PATH_TO_TESS_DATA_DIRECTORY_IS_INVALID =
            "Provided path to tess data directory does not exist or it is "
                    + "an invalid directory";
    public static final String PATH_TO_TESS_DATA_IS_NOT_SET =
            "Path to tess data directory cannot be null and must be set "
                    + "to a valid directory";

    /**
     * Creates a new TesseractException.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public Tesseract4OcrException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new TesseractException.
     *
     * @param msg the detail message.
     */
    public Tesseract4OcrException(String msg) {
        super(msg);
    }
}
