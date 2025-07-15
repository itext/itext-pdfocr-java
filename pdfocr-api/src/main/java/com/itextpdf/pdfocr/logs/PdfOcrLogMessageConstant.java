/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
package com.itextpdf.pdfocr.logs;

/**
 * Class that bundles all the log message templates as constants.
 */
public class PdfOcrLogMessageConstant {

    /** The constant CANNOT_READ_INPUT_IMAGE. */
    public static final String CANNOT_READ_INPUT_IMAGE = "Cannot read input image {0}";

    /** The constant PROVIDED_FONT_PROVIDER_IS_INVALID. */
    public static final String PROVIDED_FONT_PROVIDER_IS_INVALID =
            "Provided FontProvider is invalid. Please check that it contains valid fonts and default font family name.";

    /** The constant CANNOT_READ_DEFAULT_FONT. */
    public static final String CANNOT_READ_DEFAULT_FONT = "Cannot default read font: {0}";

    /** The constant CANNOT_ADD_DATA_TO_PDF_DOCUMENT. */
    public static final String CANNOT_ADD_DATA_TO_PDF_DOCUMENT = "Cannot add data to PDF document: {1}";

    /** The constant START_OCR_FOR_IMAGES. */
    public static final String START_OCR_FOR_IMAGES = "Starting ocr for {0} image(s)";

    /** The constant NUMBER_OF_PAGES_IN_IMAGE. */
    public static final String NUMBER_OF_PAGES_IN_IMAGE = "Image {0} contains {1} page(s)";

    /** The constant COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER. */
    public static final String COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER =
            "Could not find a glyph corresponding to Unicode character {0} in any of the fonts";

    /** The constant PDF_LANGUAGE_PROPERTY_IS_NOT_SET. */
    public static final String PDF_LANGUAGE_PROPERTY_IS_NOT_SET = "PDF language property is not set";

    public static final String CANNOT_RETRIEVE_PAGES_FROM_IMAGE = "Cannot get pages from image {0}: {1}";

    public static final String PAGE_SIZE_IS_NOT_APPLIED = "Page size has no effect when pdf file is being OCRed";

    public static final String IMAGE_LAYER_NAME_IS_NOT_APPLIED =
            "Image layer name has no effect when pdf file is being OCRed";

    private PdfOcrLogMessageConstant() {
        //Private constructor will prevent the instantiation of this class directly
    }
}
