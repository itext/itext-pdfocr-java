package com.itextpdf.pdfocr;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * {@link IOcrEngine} interface is used for instantiating new OcrReader
 * objects.
 * {@link IOcrEngine} interface provides possibility to perform OCR,
 * to read data from input files and to return the contained text in the
 * required format.
 */
public interface IOcrEngine {

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
     * Performs OCR using provided {@link IOcrEngine} for the given list of
     * input images and saves output to a text file using provided path.
     * Note that a human reading order is not guaranteed
     * due to possible specifics of input images (multi column layout, tables etc)
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param txtFile file to be created
     */
    void createTxtFile(List<File> inputImages, File txtFile);
}
