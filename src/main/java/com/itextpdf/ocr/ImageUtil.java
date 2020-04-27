package com.itextpdf.ocr;

import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;

/**
 * Image Util class.
 * <p>
 * Class provides tool for basic image preprocessing.
 */
public final class ImageUtil {

    /**
     * Private constructor for util class.
     */
    private ImageUtil() {
    }

    /**
     * Performs basic image preprocessing using buffered image (if provided).
     * Preprocessed image file will be saved in temporary directory
     * (warning will be logged if file isn't deleted)
     *
     * @param inputFile File
     * @param pageNumber int
     * @return String
     */
    public static String preprocessImage(final File inputFile,
            final int pageNumber) throws OCRException {
        Pix pix = null;
        // read image
        if (isTiffImage(inputFile)) {
            pix = TesseractUtil.readPixPageFromTiff(inputFile,
                    pageNumber - 1);
        } else {
            pix = TesseractUtil.readPix(inputFile);
        }
        if (pix == null) {
            throw new OCRException(OCRException.CANNOT_READ_PROVIDED_IMAGE)
                    .setMessageParams(inputFile.getAbsolutePath());
        }
        return TesseractUtil.preprocessPixAndSave(pix);
    }

    /**
     * Return true if provided image has 'tiff'
     * or 'tif' extension, otherwise - false.
     *
     * @param inputImage File
     * @return boolean
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
     * Count number of page in provided tiff image.
     *
     * @param inputImage File
     * @return int
     * @throws IOException IOException
     */
    public static int getNumberOfPageTiff(File inputImage)
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
     * Read image file from input stream from using provided file.
     *
     * @param inputFile File
     * @return BufferedImage
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IOException IOException
     */
    public static BufferedImage readImageFromFile(final File inputFile)
            throws IllegalArgumentException, IOException {
        return ImageIO.read(
                new FileInputStream(inputFile.getAbsolutePath()));
    }

    /**
     * Reading file a Leptonica pix and converting it to image.
     *
     * @param inputImage File
     * @return BufferedImage
     * @throws IOException IOException
     */
    public static BufferedImage readAsPixAndConvertToBufferedImage(
            final File inputImage)
            throws IOException {
        Pix pix = Leptonica.INSTANCE
                .pixRead(inputImage.getAbsolutePath());
        return TesseractUtil.convertPixToImage(pix);
    }
}

