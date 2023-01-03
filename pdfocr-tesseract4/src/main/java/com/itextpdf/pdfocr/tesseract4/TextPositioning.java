/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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

/**
 * Enumeration of the possible types of text positioning.
 * It is used when there is possibility in selected Reader to process
 * the text by lines or by words and to return coordinates for the
 * selected type of item.
 * For tesseract this value makes sense only if selected
 * {@link OutputFormat} is {@link OutputFormat#HOCR}.
 */
public enum TextPositioning {
    /**
     * Text will be located by lines retrieved from hocr file.
     * (default value)
     */
    BY_LINES,
    /**
     * Text will be located by words retrieved from hocr file.
     */
    BY_WORDS,
    /**
     * Similar to BY_WORDS mode, but top and bottom of word BBox are inherited from line.
     */
    BY_WORDS_AND_LINES,
}
