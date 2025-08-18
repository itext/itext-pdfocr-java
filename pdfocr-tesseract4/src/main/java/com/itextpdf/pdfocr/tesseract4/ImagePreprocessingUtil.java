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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrInputTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.util.TiffImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.Pix;
import org.slf4j.LoggerFactory;

/**
 * Utilities class to work with images.
 * Class provides tools for basic image preprocessing.
 */
class ImagePreprocessingUtil {

    /**
     * Creates a new {@link ImagePreprocessingUtil} instance.
     */
    private ImagePreprocessingUtil() {
    }

    /**
     * Counts number of pages in the provided tiff image.
     *
     * @param inputImage input image {@link java.io.File}
     * @return number of pages in the provided TIFF image
     * @throws IOException if error occurred during creating a
     * {@link com.itextpdf.io.source.IRandomAccessSource} based on a filename
     * string
     */
    static int getNumberOfPageTiff(final File inputImage)
            throws IOException {
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(
                new RandomAccessSourceFactory()
                        .createBestSource(
                                inputImage.getAbsolutePath()));
        int numOfPages = TiffImageData.getNumberOfPages(raf);
        raf.close();
        return numOfPages;
    }

    /**
     * Reads provided image file using stream.
     *
     * @param inputFile input image {@link java.io.File}
     * @return returns a {@link java.awt.image.BufferedImage} as the result
     * @throws IllegalArgumentException if error occurred during reading a file
     * @throws IOException if error occurred during reading a file
     */
    static BufferedImage readImageFromFile(final File inputFile)
            throws IllegalArgumentException, IOException {
        FileInputStream is = new FileInputStream(inputFile.getAbsolutePath());
        BufferedImage bi = ImageIO.read(is);
        is.close();
        return bi;
    }

    /**
     * Reads input file as Leptonica {@link net.sourceforge.lept4j.Pix} and
     * converts it to {@link java.awt.image.BufferedImage}.
     *
     * @param inputImage input image {@link java.io.File}
     * @return returns a {@link java.awt.image.BufferedImage} as the result
     * @throws IOException is error occurred during conversion from
     * {@link net.sourceforge.lept4j.Pix} to
     * {@link java.awt.image.BufferedImage}
     */
    static BufferedImage readAsPixAndConvertToBufferedImage(
            final File inputImage)
            throws IOException {
        Pix pix = TesseractOcrUtil.readPixFromFile(inputImage);
        BufferedImage img = TesseractOcrUtil.convertPixToImage(pix);
        TesseractOcrUtil.destroyPix(pix);
        return img;
    }

    /**
     * Performs basic image preprocessing using buffered image (if provided).
     * Preprocessed image will be saved in temporary directory.
     *
     * @param inputFile input image {@link File}
     * @param pageNumber number of page to be preprocessed
     * @param imagePreprocessingOptions {@link ImagePreprocessingOptions}
     * @return created preprocessed image as {@link net.sourceforge.lept4j.Pix}
     * @throws PdfOcrTesseract4Exception if it was not possible to read or convert input file
     */
    static Pix preprocessImage(final File inputFile, final int pageNumber,
            final ImagePreprocessingOptions imagePreprocessingOptions) throws PdfOcrTesseract4Exception {

        Pix pix;
        // read image
        if (TiffImageUtil.isTiffImage(inputFile)) {
            pix = TesseractOcrUtil.readPixPageFromTiff(inputFile, pageNumber - 1);
        } else {
            pix = TesseractOcrUtil.readPix(inputFile);
        }
        if (pix == null) {
            throw new PdfOcrInputTesseract4Exception(
                    PdfOcrTesseract4ExceptionMessageConstant.CANNOT_READ_PROVIDED_IMAGE)
                    .setMessageParams(inputFile.getAbsolutePath());
        }
        Pix preprocessedPix = TesseractOcrUtil.preprocessPix(pix, imagePreprocessingOptions);
        if (!TesseractOcrUtil.samePix(pix, preprocessedPix)) {
            TesseractOcrUtil.destroyPix(pix);
        }
        return preprocessedPix;
    }

    /**
     * Reads input image as a {@link java.awt.image.BufferedImage}.
     * If it is not possible to read {@link java.awt.image.BufferedImage} from
     * input file, image will be read as a {@link net.sourceforge.lept4j.Pix}
     * and then converted to {@link java.awt.image.BufferedImage}.
     * @param inputImage original input image
     * @return input image as a {@link java.awt.image.BufferedImage}
     */
    static BufferedImage readImage(File inputImage) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImagePreprocessingUtil
                    .readAsPixAndConvertToBufferedImage(
                            inputImage);
        } catch (IllegalArgumentException | IOException ex) {
            LoggerFactory.getLogger(ImagePreprocessingUtil.class)
                    .info(MessageFormatUtil.format(
                            Tesseract4LogMessageConstant
                                    .CANNOT_READ_INPUT_IMAGE,
                            ex.getMessage()));
        }
        if (bufferedImage == null) {
            try {
                bufferedImage = ImagePreprocessingUtil
                        .readImageFromFile(inputImage);
            } catch (IllegalArgumentException | IOException ex) {
                LoggerFactory.getLogger(ImagePreprocessingUtil.class).info(
                        MessageFormatUtil.format(
                                Tesseract4LogMessageConstant
                                        .CANNOT_CREATE_BUFFERED_IMAGE,
                                ex.getMessage()));
            }
        }
        return bufferedImage;
    }
}
