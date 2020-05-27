package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.util.MessageFormatUtil;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class to work with tesseract command line tool and image
 * preprocessing using {@link net.sourceforge.lept4j.ILeptonica}.
 * These all methods have to be ported to .Net manually.
 */
class TesseractOcrUtil {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractOcrUtil.class);

    /**
     * List of pages of the image that is being processed.
     */
    private List<BufferedImage> imagePages =
            Collections.<BufferedImage>emptyList();

    /**
     * Creates a new {@link TesseractOcrUtil} instance.
     */
    TesseractOcrUtil() {
    }

    /**
     * Reads required page from provided tiff image.
     *
     * @param inputFile input image as {@link java.io.File}
     * @param pageNumber number of page
     * @return result {@link net.sourceforge.lept4j.Pix} object created from
     * given image
     */
    static Pix readPixPageFromTiff(final File inputFile,
            final int pageNumber) {
        Pix pix = null;
        Pixa pixa = null;
        try {
            // read image
            pixa = Leptonica.INSTANCE
                    .pixaReadMultipageTiff(inputFile.getAbsolutePath());
            int size = pixa.n;
            // in case page number is incorrect
            if (pageNumber >= size) {
                LOGGER.warn(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.PAGE_NUMBER_IS_INCORRECT,
                        pageNumber,
                        inputFile.getAbsolutePath()));
                return null;
            }
            // copy selected pix form pixa
            pix = Leptonica.INSTANCE.pixaGetPix(pixa, pageNumber,
                    Leptonica.L_COPY);
        } finally {
            destroyPixa(pixa);
        }
        // return required page to be preprocessed
        return pix;
    }

    /**
     * Performs default image preprocessing and saves result to a temporary
     * file.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return path to a created preprocessed image file
     * as {@link java.lang.String}
     */
    static String preprocessPixAndSave(Pix pix) {
        // save preprocessed file
        String tmpFileName = getTempDir()
                + UUID.randomUUID().toString() + ".png";
        try {
            // preprocess image
            pix = preprocessPix(pix);
            Leptonica.INSTANCE.pixWritePng(tmpFileName, pix,
                    ILeptonica.IFF_PNG);
        } finally {
            // destroying
            destroyPix(pix);
        }
        return tmpFileName;
    }

    /**
     * Performs default image preprocessing.
     * It includes the following actions:
     * converting to grayscale,
     * thresholding.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    static Pix preprocessPix(Pix pix) {
        pix = convertToGrayscale(pix);
        pix = otsuImageThresholding(pix);
        return pix;
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix} to grayscale.
     * In .Net image is converted only if this is 32bpp image. In java image is
     * converted anyway using different Leptonica methods depending on
     * image depth.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    static Pix convertToGrayscale(final Pix pix) {
        Leptonica instance = Leptonica.INSTANCE;
        if (pix != null) {
            int depth = instance.pixGetDepth(pix);

            if (depth == 32) {
                return instance.pixConvertRGBToLuminance(pix);
            } else {
                return instance.pixRemoveColormap(pix,
                        instance.REMOVE_CMAP_TO_GRAYSCALE);
            }
        } else {
            return pix;
        }
    }

    /**
     * Performs Leptonica Otsu adaptive image thresholding using
     * {@link net.sourceforge.lept4j.Leptonica#pixOtsuAdaptiveThreshold}
     * method.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return {@link net.sourceforge.lept4j.Pix} object after thresholding
     */
    static Pix otsuImageThresholding(final Pix pix) {
        if (pix != null) {
            Pix thresholdPix = null;
            if (pix.d == 8) {
                PointerByReference pointer = new PointerByReference();
                Leptonica.INSTANCE
                        .pixOtsuAdaptiveThreshold(pix, pix.w, pix.h,
                                0, 0, 0,
                                null, pointer);
                thresholdPix = new Pix(pointer.getValue());
                if (thresholdPix.w > 0 && thresholdPix.h > 0) {
                    // destroying original pix
                    destroyPix(pix);
                    return thresholdPix;
                } else {
                    LOGGER.info(MessageFormatUtil.format(
                            Tesseract4LogMessageConstant.CANNOT_BINARIZE_IMAGE,
                            pix.d));
                    // destroying created PointerByReference object
                    destroyPix(thresholdPix);
                    return pix;
                }
            } else {
                LOGGER.info(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.CANNOT_BINARIZE_IMAGE,
                        pix.d));
                return pix;
            }
        } else {
            return pix;
        }
    }

    /**
     * Destroys {@link net.sourceforge.lept4j.Pix} object.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be destroyed
     */
    static void destroyPix(Pix pix) {
        if (pix != null) {
            Leptonica.INSTANCE.lept_free(pix.getPointer());
        }
    }

    /**
     * Destroys {@link net.sourceforge.lept4j.Pixa} object.
     *
     * @param pixa {@link net.sourceforge.lept4j.Pixa} object to be destroyed
     */
    static void destroyPixa(Pixa pixa) {
        if (pixa != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pixa.getPointer());
            Leptonica.INSTANCE.pixaDestroy(pRef);
        }
    }

    /**
     * Sets tesseract properties.
     * The following properties are set in this method:
     * In java: path to tess data, languages, psm
     * In .Net: psm
     * This means that other properties have been set during the
     * initialization of tesseract instance previously or tesseract library
     * doesn't provide such possibilities in api for .Net or java.
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract} object
     * @param tessData path to tess data directory
     * @param languages list of languages in required format
     *                  as {@link java.lang.String}
     * @param pageSegMode page segmentation mode {@link java.lang.Integer}
     * @param userWordsFilePath path to a temporary file with user words
     */
    static void setTesseractProperties(
            final ITesseract tesseractInstance,
            final String tessData, final String languages,
            final Integer pageSegMode, final String userWordsFilePath) {
        tesseractInstance.setDatapath(tessData);
        tesseractInstance.setLanguage(languages);
        if (pageSegMode != null) {
            tesseractInstance.setPageSegMode(pageSegMode);
        }
        tesseractInstance.setOcrEngineMode(userWordsFilePath != null ? 0 : 3);
    }

    /**
     * Creates tesseract instance with parameters.
     * Method is used to initialize tesseract instance with parameters if it
     * haven't been initialized yet.
     * In this method in java 'tessData', 'languages' and 'userWordsFilePath'
     * properties are unused as they will be set using setters in
     * {@link #setTesseractProperties} method. In .Net all these properties
     * are needed to be provided in tesseract constructor in order to
     * initialize tesseract instance. Thus, tesseract initialization takes
     * place in {@link Tesseract4LibOcrEngine#Tesseract4LibOcrEngine} constructor in
     * java, but in .Net it happens only after all properties are validated,
     * i.e. just before OCR process.
     *
     * @param isWindows true is current os is windows
     * @param tessData path to tess data directory
     * @param languages list of languages in required format as
     *                  {@link java.lang.String}
     * @param userWordsFilePath path to a temporary file with user words
     * @return initialized {@link net.sourceforge.tess4j.ITesseract} object
     */
    static ITesseract initializeTesseractInstance(final boolean isWindows,
            final String tessData, final String languages,
            final String userWordsFilePath) {
        if (isWindows) {
            return new Tesseract1();
        } else {
            return new Tesseract();
        }
    }

    /**
     * Returns true if tesseract instance has been already disposed.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to check
     * @return true if tesseract instance is disposed.
     */
    static boolean isTesseractInstanceDisposed(
            final ITesseract tesseractInstance) {
        return false;
    }

    /**
     * Disposes {@link net.sourceforge.tess4j.ITesseract} instance.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to dispose
     */
    static void disposeTesseractInstance(
            final ITesseract tesseractInstance) {
    }

    /**
     * Converts {@link java.awt.image.BufferedImage} to
     * {@link net.sourceforge.lept4j.Pix}.
     *
     * @param bufferedImage input image as {@link java.awt.image.BufferedImage}
     * @return Pix result converted {@link net.sourceforge.lept4j.Pix} object
     * @throws IOException if it's not possible to convert
     */
    static Pix convertImageToPix(
            final BufferedImage bufferedImage)
            throws IOException {
        Pix pix = null;
        if (bufferedImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);

            ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
            NativeSize nativeSize = new NativeSize(baos.toByteArray().length);
            pix = Leptonica.INSTANCE.pixReadMem(byteBuffer, nativeSize);
        }

        return pix;
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix}
     * to {@link java.awt.image.BufferedImage} with
     * {@link net.sourceforge.lept4j.ILeptonica#IFF_PNG} image format.
     *
     * @param pix input {@link net.sourceforge.lept4j.Pix} object
     * @return result {@link java.awt.image.BufferedImage} object
     * @throws IOException if it is not possible to convert
     */
    static BufferedImage convertPixToImage(final Pix pix)
            throws IOException {
        if (pix != null) {
            Leptonica instance = Leptonica.INSTANCE;
            BufferedImage bi = null;
            PointerByReference pdata = new PointerByReference();
            try {
                NativeSizeByReference psize = new NativeSizeByReference();
                instance.pixWriteMem(pdata, psize, pix, ILeptonica.IFF_PNG);
                byte[] b = pdata.getValue().getByteArray(0,
                        psize.getValue().intValue());
                try (InputStream in = new ByteArrayInputStream(b)) {
                    bi = ImageIO.read(in);
                }
            } finally {
                instance.lept_free(pdata.getValue());
            }
            return bi;
        } else {
            return null;
        }
    }

    /**
     * Gets current system temporary directory.
     *
     * @return path to system temporary directory
     */
    static String getTempDir() {
        String tempDir = System.getProperty("java.io.tmpdir");
        try {
            Path tempPath = Files.createTempDirectory("pdfocr");
            tempDir = tempPath.toString();
        } catch (IOException e) {
            LOGGER.info(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_GET_TEMPORARY_DIRECTORY,
                    e.getMessage()));
            if (tempDir == null) {
                tempDir = "";
            }
        }
        return tempDir;
    }

    /**
     * Retrieves list of pages from provided image as list of
     * {@link java.awt.image.BufferedImage}, one per page and updates
     * this list for the image using {@link #setListOfPages} method.
     *
     * @param inputFile input image {@link java.io.File}
     */
    void initializeImagesListFromTiff(
            final File inputFile) {
        try (InputStream is =
                new FileInputStream(inputFile.getAbsolutePath())) {
            setListOfPages(Imaging
                    .getAllBufferedImages(is,
                            inputFile.getAbsolutePath()));
        } catch (ImageReadException | IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE,
                    inputFile.getAbsolutePath(),
                    e.getMessage()));
        }
    }

    /**
     * Gets list of page of processing image as list of
     * {@link java.awt.image.BufferedImage}, one per page.
     *
     * @return result {@link java.util.List} of pages
     */
    List<BufferedImage> getListOfPages() {
        return new ArrayList<BufferedImage>(imagePages);
    }

    /**
     * Sets list of page of processing image as list of
     * {@link java.awt.image.BufferedImage}, one per page.
     *
     * @param listOfPages list of {@link java.awt.image.BufferedImage} for
     *                    each page.
     */
    void setListOfPages(final List<BufferedImage> listOfPages) {
        imagePages = Collections.<BufferedImage>unmodifiableList(listOfPages);
    }

    /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link OutputFormat} is used in .Net version,
     * in java output format should already be set)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param image input {@link java.awt.image.BufferedImage} to be processed
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final BufferedImage image, final OutputFormat outputFormat)
            throws TesseractException {
        return tesseractInstance.doOCR(image);
    }

    /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link OutputFormat} is used in .Net version, in java output format
     * should already be set)
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param image input image as {@link java.io.File} to be
     *              processed
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final File image, final OutputFormat outputFormat)
            throws TesseractException {
        return tesseractInstance.doOCR(image);
    }

     /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link OutputFormat} is used in .Net version, in java output format
      * should already be set)
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param pix input image as {@link net.sourceforge.lept4j.Pix} to be
     *              processed
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     * @throws IOException if it is not possible to convert input image
     */
    String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final Pix pix, final OutputFormat outputFormat)
            throws TesseractException, IOException {
        BufferedImage bufferedImage = convertPixToImage(pix);
        return getOcrResultAsString(tesseractInstance,
                bufferedImage, outputFormat);
    }
}
