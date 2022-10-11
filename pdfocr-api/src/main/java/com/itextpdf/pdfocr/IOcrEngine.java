/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
     * Reads data from the provided input image file and returns retrieved data
     * in the format described below.
     *
     * @param input input image {@link java.io.File}
     * @param ocrProcessContext ocr processing context
     *
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext);

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

    /**
     * Performs OCR using provided {@link IOcrEngine} for the given list of
     * input images and saves output to a text file using provided path.
     * Note that a human reading order is not guaranteed
     * due to possible specifics of input images (multi column layout, tables etc)
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param txtFile file to be created
     * @param ocrProcessContext ocr processing context
     */
    void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext);
}
