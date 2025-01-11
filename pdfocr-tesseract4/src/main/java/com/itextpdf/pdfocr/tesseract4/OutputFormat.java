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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.TextInfo;

/**
 * Enumeration of the available output formats.
 * It is used when there is possibility in selected Reader to process input
 * file and to return result in the required output format.
 */
public enum OutputFormat {
    /**
     * Reader will produce XHTML output compliant
     * with the hOCR specification.
     * Output will be parsed and represented as {@link java.util.List} of
     * {@link TextInfo} objects
     */
    HOCR,
    /**
     * Reader will produce plain txt file.
     */
    TXT
}
