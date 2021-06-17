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
package com.itextpdf.pdfocr;

public class PdfOcrLogMessageConstant {
    public static final String CANNOT_READ_INPUT_IMAGE =
            "Cannot read input image {0}";
    public static final String PROVIDED_FONT_PROVIDER_IS_INVALID =
            "Provided FontProvider is invalid. Please check that it contains "
                    + "valid fonts and default font family name.";
    public static final String CANNOT_READ_DEFAULT_FONT =
            "Cannot default read font: {0}";
    public static final String CANNOT_ADD_DATA_TO_PDF_DOCUMENT =
            "Cannot add data to PDF document: {1}";
    public static final String START_OCR_FOR_IMAGES =
            "Starting ocr for {0} image(s)";
    public static final String NUMBER_OF_PAGES_IN_IMAGE =
            "Image {0} contains {1} page(s)";
    public static final String COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER =
            "Could not find a glyph corresponding to Unicode character {0} "
                    + "in any of the fonts";
    public static final String PDF_LANGUAGE_PROPERTY_IS_NOT_SET =
            "PDF language property is not set";

    private PdfOcrLogMessageConstant() {
    }
}
