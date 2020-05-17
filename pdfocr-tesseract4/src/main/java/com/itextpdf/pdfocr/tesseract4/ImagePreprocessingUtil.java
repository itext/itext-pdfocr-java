package com.itextpdf.pdfocr.tesseract4;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;

/**
 * Utilities class to work with images.
 * Class provides tools for basic image preprocessing.
 */
public final class ImagePreprocessingUtil {

    /**
     * Creates a new {@link ImagePreprocessingUtil} instance.
     */
    private ImagePreprocessingUtil() {
    }

    /**
     * Performs basic image preprocessing using buffered image (if provided).
     * Preprocessed image will be saved in temporary directory.
     *
     * @param inputFile input image {@link java.io.File}
     * @param pageNumber number of page to be preprocessed
     * @return path to the created preprocessed image file as
     * {@link java.lang.String}
     * @throws Tesseract4OcrException if it was not possible to read or convert
     * input file
     */
    public static String preprocessImage(final File inputFile,
            final int pageNumber) throws Tesseract4OcrException {
        Pix pix = null;
        // read image
        if (isTiffImage(inputFile)) {
            pix = TesseractOcrUtil.readPixPageFromTiff(inputFile,
                    pageNumber - 1);
        } else {
            pix = TesseractOcrUtil.readPix(inputFile);
        }
        if (pix == null) {
            throw new Tesseract4OcrException(
                    Tesseract4OcrException.CannotReadProvidedImage)
                    .setMessageParams(inputFile.getAbsolutePath());
        }
        return TesseractOcrUtil.preprocessPixAndSave(pix);
    }

    /**
     * Checks whether image format is TIFF.
     *
     * @param inputImage input image {@link java.io.File}
     * @return true if provided image has 'tiff' or 'tif' extension
     */
    public static boolean isTiffImage(final File inputImage) {
        int index = inputImage.getAbsolutePath().lastIndexOf('.');
        if (index > 0) {
            String extension = new String(
                    inputImage.getAbsolutePath().toCharArray(), index + 1,
                    inputImage.getAbsolutePath().length() - index - 1);
            return extension.toLowerCase().contains("tif");
        }
        return false;
    }

    /**
     * Reads provided image file using stream.
     *
     * @param inputFile input image {@link java.io.File}
     * @return returns a {@link java.awt.image.BufferedImage} as the result
     * @throws IllegalArgumentException if error occurred during reading a file
     * @throws IOException if error occurred during reading a file
     */
    public static BufferedImage readImageFromFile(final File inputFile)
            throws IllegalArgumentException, IOException {
        return ImageIO.read(
                new FileInputStream(inputFile.getAbsolutePath()));
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
    public static BufferedImage readAsPixAndConvertToBufferedImage(
            final File inputImage)
            throws IOException {
        Pix pix = Leptonica.INSTANCE
                .pixRead(inputImage.getAbsolutePath());
        return TesseractOcrUtil.convertPixToImage(pix);
    }
}
