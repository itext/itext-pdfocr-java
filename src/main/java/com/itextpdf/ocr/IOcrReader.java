package com.itextpdf.ocr;

import java.io.File;
import java.util.List;

/**
 * Interface for OcrReader classes.
 *
 * IOcrReader interface provides possibility to perform OCR actions,
 * read data from input files and return contained text in the described format
 */
public interface IOcrReader {

    /**
     * Reads data from the provided input image file and returns retrieved data
     * in the following format:
     *
     * List<TextInfo> where each list TextInfo element contains word
     * or line and its 4 coordinates(bbox).
     *
     * @param input input file
     * @return List<TextInfo>
     */
    List<TextInfo> readDataFromInput(File input);
}
