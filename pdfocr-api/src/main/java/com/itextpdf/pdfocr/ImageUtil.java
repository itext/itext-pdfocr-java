package com.itextpdf.pdfocr;

import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utilities class to work with images.
 * Class provides tools for basic image preprocessing.
 */
class ImageUtil {

    /**
     * Creates a new {@link ImageUtil} instance.
     */
    private ImageUtil() {
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
     * @param inputFile input image {@link File}
     * @return returns a {@link BufferedImage} as the result
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
}
