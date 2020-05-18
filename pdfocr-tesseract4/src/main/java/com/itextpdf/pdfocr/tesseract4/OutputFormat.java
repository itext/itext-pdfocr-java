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
