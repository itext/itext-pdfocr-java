package com.itextpdf.ocr;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Interface for OcrReader classes.
 *
 * IOcrReader interface provides possibility to perform OCR actions,
 * read data from input files and return contained text in the described format
 */
public interface IOcrReader {

    /**
     * Enum describing possible types of text positioning.
     *
     *
     * {@link #BY_LINES}
     * {@link #BY_WORDS}
     */
    enum TextPositioning {
        /**
         * BY_LINES (default value).
         *
         * text will be located by lines retrieved from hocr file
         */
        BY_LINES,
        /**
         * BY_WORDS.
         *
         * text will be located by words retrieved from hocr file
         */
        BY_WORDS
    }

    /**
     * Enum describing available output formats.
     *
     * {@link #TXT}
     * {@link #HOCR}
     */
    enum OutputFormat {
        /**
         * HOCR.
         *
         * Reader will produce XHTML output compliant
         * with the hOCR specification.
         * Output will be parsed and represented as {@link java.util.List}
         */
        HOCR,
        /**
         * TXT.
         *
         * Reader will produce plain txt file
         */
        TXT
    }

    /**
     * Reads data from the provided input image file and returns retrieved data
     * in the following format:
     *
     * Map<Integer, List<TextInfo>>:
     * key: number of the page,
     * value: list of {@link TextInfo} elements where
     * each {@link TextInfo} element contains a word or a line
     * and its 4 coordinates(bbox).
     * (There will be parsed result in hOCR format produced by reader)
     *
     * @param input input file
     * @return Map<Integer, List<{@link TextInfo}>>
     */
    Map<Integer, List<TextInfo>> readDataFromInput(File input);

    /**
     * Reads data from the provided input image file and returns retrieved data
     * as string.
     *
     * @param input {@link java.io.File}
     * @param outputFormat {@link OutputFormat}
     * @return {@link java.lang.String}
     */
    String readDataFromInput(File input, OutputFormat outputFormat);
}
