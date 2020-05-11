package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.ocr.IOcrReader.OutputFormat;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class to work with tesseract command line tool and image
 * preprocessing using {@link net.sourceforge.lept4j.ILeptonica}.
 * These all methods have to be ported to .Net manually.
 */
public final class TesseractUtil {

    /**
     * Path to the file with the default font.
     */
    public static final String FONT_RESOURCE_PATH = "com/itextpdf/ocr/fonts/";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractUtil.class);

    /**
     * List of pages of the image that is being processed.
     */
    private List<BufferedImage> imagePages = Collections.<BufferedImage>emptyList();

    /**
     * Creates a new {@link TesseractUtil} instance.
     */
    public TesseractUtil() {
    }

    /**
     * Runs given command.
     *
     * @param command {@link java.util.List} of command line arguments
     * @param isWindows true is current os is windows
     * @throws OcrException if provided command failed
     */
    public static void runCommand(final List<String> command,
            final boolean isWindows) throws OcrException {
        Process process = null;
        try {
            if (isWindows) {
                ProcessBuilder pb = new ProcessBuilder(command); //NOSONAR
                process = pb.start();
            } else {
                ProcessBuilder pb = new ProcessBuilder(
                        "bash", "-c", //NOSONAR
                        String.join(" ", command)); //NOSONAR
                pb.redirectErrorStream(true);
                process = pb.start();
            }
            int result = process.waitFor();

            if (result != 0) {
                LOGGER.error(MessageFormatUtil
                        .format(LogMessageConstant.TesseractFailed,
                                String.join(" ", command)));
                throw new OcrException(OcrException.TesseractFailed);
            }

            process.destroy();
        } catch (NullPointerException | IOException | InterruptedException e) {
            LOGGER.error(MessageFormatUtil
                    .format(LogMessageConstant.TesseractFailed,
                            e.getMessage()));
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new OcrException(OcrException.TesseractFailed);
        }
    }

    /**
     * Reads required page from provided tiff image.
     *
     * @param inputFile input image as {@link java.io.File}
     * @param pageNumber number of page
     * @return result {@link net.sourceforge.lept4j.Pix} object created from
     * given image
     */
    public static Pix readPixPageFromTiff(final File inputFile,
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
                LOGGER.warn(MessageFormatUtil
                        .format(
                                LogMessageConstant.PageNumberIsIncorrect,
                                pageNumber,
                                inputFile.getAbsolutePath()));
                return null;
            }
            pix = Leptonica.INSTANCE.pixaGetPix(pixa, pageNumber, 1);
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
     * @return path to a created preprocessed image file as
     * {@link java.lang.String}
     */
    public static String preprocessPixAndSave(Pix pix) {
        // save preprocessed file
        String tmpFileName = getTempDir()
                + UUID.randomUUID().toString() + ".png";
        try {
            // preprocess image
            pix = preprocessPix(pix);
            int formatPng = 3;
            Leptonica.INSTANCE.pixWritePng(tmpFileName, pix, formatPng);
        } finally {
            // destroying
            destroyPix(pix);
        }
        return tmpFileName;
    }

    /**
     * Performs default image preprocessing.
     * It includes the following actions:
     * removing alpha channel,
     * converting to grayscale,
     * thresholding.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    public static Pix preprocessPix(Pix pix) {
        pix = Leptonica.INSTANCE.pixRemoveAlpha(pix);
        pix = convertToGrayscale(pix);
        pix = otsuImageThresholding(pix);
        return pix;
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix} to grayscale.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    public static Pix convertToGrayscale(final Pix pix) {
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
            return null;
        }
    }

    /**
     * Performs Leptonica Otsu adaptive image thresholding using
     * {@link net.sourceforge.lept4j.Leptonica#pixOtsuAdaptiveThreshold} method
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return {@link net.sourceforge.lept4j.Pix} object after thresholding
     */
    public static Pix otsuImageThresholding(final Pix pix) {
        if (pix != null) {
            PointerByReference pointer = new PointerByReference();
            Leptonica.INSTANCE
                    .pixOtsuAdaptiveThreshold(pix, pix.w, pix.h,
                            0, 0, 0,
                            null, pointer);
            Pix thresholdPix = new Pix(pointer.getValue());
            if (thresholdPix.w > 0 && thresholdPix.h > 0) {
                return thresholdPix;
            } else {
                return pix;
            }
        } else {
            return null;
        }
    }

    /**
     * Destroys {@link net.sourceforge.lept4j.Pix} object.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be destroyed
     */
    public static void destroyPix(Pix pix) {
        if (pix != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pix.getPointer());
            Leptonica.INSTANCE.pixDestroy(pRef);
        }
    }

    /**
     * Destroys {@link net.sourceforge.lept4j.Pixa} object.
     *
     * @param pixa {@link net.sourceforge.lept4j.Pixa} object to be destroyed
     */
    public static void destroyPixa(Pixa pixa) {
        if (pixa != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pixa.getPointer());
            Leptonica.INSTANCE.pixaDestroy(pRef);
        }
    }

    /**
     * Utility method to get png image format as needed.
     *
     * @return {@link org.apache.commons.imaging.ImageFormats#PNG} as
     * {@link java.lang.String}
     */
    public static String getPngImageFormat() {
        return ImageFormats.PNG.getName();
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
    public static void setTesseractProperties(
            final ITesseract tesseractInstance,
            final String tessData, final String languages,
            final Integer pageSegMode, final String userWordsFilePath) {
        tesseractInstance.setDatapath(tessData);
        tesseractInstance.setLanguage(languages);
        if (pageSegMode != null) {
            tesseractInstance.setPageSegMode(pageSegMode);
        }
        if (userWordsFilePath != null) {
            tesseractInstance.setOcrEngineMode(0);
        }
    }

    /**
     * Creates tesseract instance without parameters (used in java).
     *
     * @param isWindows true is current os is windows
     * @return created {@link net.sourceforge.tess4j.ITesseract} object
     */
    public static ITesseract createTesseractInstance(final boolean isWindows) {
        if (isWindows) {
            return new Tesseract1();
        } else {
            return new Tesseract();
        }
    }

    /**
     * Creates tesseract instance with parameters.
     *
     * @param tessData path to tess data directory
     * @param languages list of languages in required format as
     *                  {@link java.lang.String}
     * @param isWindows true is current os is windows
     * @param userWordsFilePath path to a temporary file with user words
     * @return initialized {@link net.sourceforge.tess4j.ITesseract} object
     */
    public static ITesseract initializeTesseractInstanceWithParameters(
            final String tessData, final String languages,
            final boolean isWindows, final String userWordsFilePath) {
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
    public static boolean isTesseractInstanceDisposed(
            final ITesseract tesseractInstance) {
        return false;
    }

    /**
     * Disposes {@link net.sourceforge.tess4j.ITesseract} instance.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to dispose
     */
    public static void disposeTesseractInstance(
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
    public static Pix convertImageToPix(
            final BufferedImage bufferedImage)
            throws IOException {
        return convertBufferedImageToPix(bufferedImage);
    }

    /**
     * Reads {@link net.sourceforge.lept4j.Pix} from input file or, if
     * this is not possible, reads input file as
     * {@link java.awt.image.BufferedImage} and then converts to
     * {@link net.sourceforge.lept4j.Pix}.
     *
     * @param inputFile input image {@link java.io.File}
     * @return Pix result {@link net.sourceforge.lept4j.Pix} object from
     * input file
     */
    public static Pix readPix(final File inputFile) {
        Pix pix = null;
        try {
            BufferedImage bufferedImage = ImageUtil
                    .readImageFromFile(inputFile);
            if (bufferedImage != null) {
                pix = convertImageToPix(bufferedImage);
            } else {
                pix = Leptonica.INSTANCE.pixRead(inputFile.getAbsolutePath());
            }
        } catch (IllegalArgumentException | IOException e) {
            LoggerFactory.getLogger(ImageUtil.class)
                    .info(MessageFormatUtil
                            .format(
                                    LogMessageConstant.ReadingImageAsPix,
                                    inputFile.getAbsolutePath(),
                                    e.getMessage()));
            pix = Leptonica.INSTANCE.pixRead(inputFile.getAbsolutePath());
        }
        return pix;
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix} to
     * {@link java.awt.image.BufferedImage}.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix}
     * @param format image format as int,
     *               @see net.sourceforge.lept4j.ILeptonica for all
     *               allowed formats (e.g.
     *               {@link net.sourceforge.lept4j.ILeptonica#IFF_BMP},
     *               {@link net.sourceforge.lept4j.ILeptonica#IFF_JFIF_JPEG},
     *               {@link net.sourceforge.lept4j.ILeptonica#IFF_PNG},
     *               {@link net.sourceforge.lept4j.ILeptonica#IFF_PNM},
     *               {@link net.sourceforge.lept4j.ILeptonica#IFF_TIFF})
     * @return result png {@link java.awt.image.BufferedImage} object or null
     * if input  {@link net.sourceforge.lept4j.Pix} is null
     * @throws IOException if it is not possible to convert
     */
    public static BufferedImage convertPixToBufferedImage(final Pix pix,
            final int format)
            throws IOException {
        if (pix != null) {
            PointerByReference pdata = new PointerByReference();
            NativeSizeByReference psize = new NativeSizeByReference();

            Leptonica instance = Leptonica.INSTANCE;

            instance.pixWriteMem(pdata, psize, pix, format);
            byte[] b = pdata.getValue().getByteArray(0,
                    psize.getValue().intValue());
            InputStream in = new ByteArrayInputStream(b);
            BufferedImage bi = ImageIO.read(in);
            in.close();
            instance.lept_free(pdata.getValue());
            return bi;
        } else {
            return null;
        }
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
    public static BufferedImage convertPixToImage(final Pix pix)
            throws IOException {
        int format_png = 3;
        return convertPixToBufferedImage(pix, format_png);
    }

    /**
     * Converts {@link java.awt.image.BufferedImage}
     * to {@link net.sourceforge.lept4j.Pix}.
     *
     * @param bufferedImage input {@link java.awt.image.BufferedImage} object
     * @return input {@link net.sourceforge.lept4j.Pix} object
     * @throws IOException if it's not possible to convert
     */
    public static Pix convertBufferedImageToPix(
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
     * Gets current system temporary directory.
     *
     * @return path to system temporary directory
     */
    public static String getTempDir() {
        String tempDir = System.getProperty("java.io.tmpdir") == null
                ? System.getProperty("TEMP")
                : System.getProperty("java.io.tmpdir");
        if (!(tempDir.endsWith("/") || tempDir.endsWith("\\"))) {
            tempDir = tempDir + java.io.File.separatorChar;
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
    public void initializeImagesListFromTiff(
            final File inputFile) {
        try (InputStream is =
                new FileInputStream(inputFile.getAbsolutePath())) {
            setListOfPages(Imaging
                    .getAllBufferedImages(is,
                            inputFile.getAbsolutePath()));
        } catch (ImageReadException | IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    LogMessageConstant.CannotRetrievePagesFromImage,
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
    public List<BufferedImage> getListOfPages() {
        return new ArrayList<BufferedImage>(imagePages);
    }

    /**
     * Sets list of page of processing image as list of
     * {@link java.awt.image.BufferedImage}, one per page.
     *
     * @param listOfPages list of {@link java.awt.image.BufferedImage} for
     *                    each page.
     */
    public void setListOfPages(final List<BufferedImage> listOfPages) {
        imagePages = Collections.<BufferedImage>unmodifiableList(listOfPages);
    }

    /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link IOcrReader.OutputFormat} is used in .Net version,
     * in java output format should already be set)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param image input {@link java.awt.image.BufferedImage} to be processed
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                      tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    public String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final BufferedImage image, final OutputFormat outputFormat)
            throws TesseractException {
        String result = tesseractInstance.doOCR(image);
        // setting default oem after processing
        tesseractInstance.setOcrEngineMode(3);
        return result;
    }

    /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link IOcrReader.OutputFormat} is used in .Net version,
     * in java output format should already be set)
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param image input image as {@link java.io.File} to be
     *              processed
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    public String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final File image, final OutputFormat outputFormat)
            throws TesseractException {
        String result = tesseractInstance.doOCR(image);
        // setting default oem after processing
        tesseractInstance.setOcrEngineMode(3);
        return result;
    }

     /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link IOcrReader.OutputFormat} is used in .Net version,
     * in java output format should already be set)
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param pix input image as {@link net.sourceforge.lept4j.Pix} to be
     *              processed
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    public String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final Pix pix, final OutputFormat outputFormat)
            throws TesseractException, IOException {
        BufferedImage bufferedImage = convertPixToImage(pix);
        return getOcrResultAsString(tesseractInstance,
                bufferedImage, outputFormat);
    }
}
