package com.itextpdf.ocr;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Interface for OcrReader classes.
 * <p>
 * IOcrReader interface provides possibility to perform OCR actions,
 * read data from input files and return contained text in the described format
 */
public interface IOcrReader {

    /**
     * Enum describing possible types of text positioning.
     *
     *
     * <li>{@link #BY_LINES}</li>
     * <li>{@link #BY_WORDS}</li>
     */
    enum TextPositioning {
        /**
         * BY_LINES (default value).
         * <p>
         * text will be located by lines retrieved from hocr file
         */
        BY_LINES,
        /**
         * BY_WORDS.
         * <p>
         * text will be located by words retrieved from hocr file
         */
        BY_WORDS
    }

    /**
     * Enum describing available output formats.
     *
     *
     * <li>{@link #TXT}</li>
     * <li>{@link #HOCR}</li>
     */
    enum OutputFormat {
        /**
         * HOCR.
         * <p>
         * Reader will produce XHTML output compliant
         * with the hOCR specification.
         * Output will be parsed and represented as List<TextInfo>
         */
        HOCR,
        /**
         * TXT.
         * <p>
         * Reader will produce plain txt file
         */
        TXT
    }

    /**
     * Reads data from the provided input image file and returns retrieved data
     * in the following format:
     * <p>
     * Map<Integer, List<TextInfo>>:
     * key: number of the page,
     * value: list of TextInfo elements where each list TextInfo element contains word
     * or line and its 4 coordinates(bbox).
     * (There will be parsed result in hOCR format produced by reader)
     *
     * @param input input file
     * @return Map<Integer, List<TextInfo>>
     */
    Map<Integer, List<TextInfo>> readDataFromInput(File input);

    /**
     * Reads data from the provided input image file and returns retrieved data
     * as string.
     *
     * @param input input file
     * @param outputFormat OutputFormat
     * @return List<TextInfo>
     */
    String readDataFromInput(File input, OutputFormat outputFormat);
}
