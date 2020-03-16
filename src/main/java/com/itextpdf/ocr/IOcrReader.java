package com.itextpdf.ocr;

import java.io.File;
import java.io.InputStream;
import java.util.List;

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
     * <li>{@link #byLines}</li>
     * <li>{@link #byWords}</li>
     */
    enum TextPositioning {
        /**
         * byLines (default value).
         * <p>
         * text will be located by lines retrieved from hocr file
         */
        byLines,
        /**
         * byWords.
         * <p>
         * text will be located by words retrieved from hocr file
         */
        byWords
    }

    /**
     * Enum describing available output formats.
     *
     *
     * <li>{@link #txt}</li>
     * <li>{@link #hocr}</li>
     */
    enum OutputFormat {
        /**
         * hocr.
         * <p>
         * Reader will produce XHTML output compliant
         * with the hOCR specification.
         * Output will be parsed and represented as List<TextInfo>
         */
        hocr,
        /**
         * txt.
         * <p>
         * Reader will produce plain txt file
         */
        txt
    }

    /**
     * Reads data from the provided input image file and returns retrieved data
     * in the following format:
     * <p>
     * List<TextInfo> where each list TextInfo element contains word
     * or line and its 4 coordinates(bbox).
     * (There will be parsed result in hOCR format produced by reader)
     *
     * @param input input file
     * @return List<TextInfo>
     */
    List<TextInfo> readDataFromInput(File input);

    /**
     * Reads data from the provided input image file and returns retrieved data
     * as string.
     *
     * @param input input file
     * @param outputFormat OutputFormat
     * @return List<TextInfo>
     */
    String readDataFromInput(File input, OutputFormat outputFormat);

    /**
     * Reads data from input stream and returns retrieved data
     * in the following format:
     * <p>
     * List<TextInfo> where each list TextInfo element contains word
     * or line and its 4 coordinates(bbox).
     *
     * @param is InputStream
     * @return List<TextInfo>
     */
    List<TextInfo> readDataFromInput(InputStream is,
            OutputFormat outputFormat);
}
