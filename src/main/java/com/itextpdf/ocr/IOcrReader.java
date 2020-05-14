package com.itextpdf.ocr;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * {@link IOcrReader} interface is used for instantiating new OcrReader
 * objects.
 * {@link IOcrReader} interface provides possibility to perform OCR,
 * to read data from input files and to return the contained text in the
 * required format.
 */
public interface IOcrReader {

    /**
     * Reads data from the provided input image file and returns retrieved data
     * in the format described below.
     *
     * @param input input image {@link java.io.File}
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    Map<Integer, List<TextInfo>> doImageOcr(File input);

    /**
     * Reads data from the provided input image file and returns retrieved data
     * as string.
     *
     * @param input input image {@link java.io.File}
     * @param outputFormat {@link OutputFormat} for the result returned
     *                                         by {@link IOcrReader}
     * @return OCR result as a {@link java.lang.String} that is
     * returned after processing the given image
     */
    String doImageOcr(File input, OutputFormat outputFormat);

    /**
     * Enumeration of the possible types of text positioning.
     * It is used when there is possibility in selected Reader to process
     * the text by lines or by words and to return coordinates for the
     * selected type of item.
     * For tesseract this value makes sense only if selected
     * {@link OutputFormat} is {@link OutputFormat#HOCR}.
     */
    enum TextPositioning {
        /**
         * Text will be located by lines retrieved from hocr file.
         * (default value)
         */
        BY_LINES,
        /**
         * Text will be located by words retrieved from hocr file.
         */
        BY_WORDS
    }

    /**
     * Enumeration of the available output formats.
     * It is used when there is possibility in selected Reader to process input
     * file and to return result in the required output format.
     */
    enum OutputFormat {
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
}
