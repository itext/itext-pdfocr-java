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
 * Tesseract Utils class.
 * Here are listed all methods that have to be ported to .Net manually.
 */
public final class TesseractUtil {

    /**
     * Path to the file with the default font.
     */
    public static final String FONT_RESOURCE_PATH = "com/itextpdf/ocr/fonts/";

    /**
     * Utils logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractUtil.class);

    /**
     * List of page of processing image.
     */
    private static List<BufferedImage> imagePages =
            Collections.<BufferedImage>emptyList();

    /**
     * Private constructor for util class.
     */
    private TesseractUtil() {
    }

    /**
     * Retrieve list of pages from provided image.
     * @param inputFile {@link java.io.File}
     */
    public static void initializeImagesListFromTiff(
            final File inputFile) {
        try (InputStream is =
                new FileInputStream(inputFile.getAbsolutePath())) {
            imagePages = Imaging
                    .getAllBufferedImages(is,
                            inputFile.getAbsolutePath());
        } catch (ImageReadException | IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    LogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE,
                    inputFile.getAbsolutePath(),
                    e.getMessage()));
        }
    }

    /**
     * Get list of page of processing image.
     * @return List<BufferedImage>
     */
    public static List<BufferedImage> getListOfPages() {
        return new ArrayList<BufferedImage>(imagePages);
    }

    /**
     * Run given command in command line.
     *
     * @param command List<String>
     * @param isWindows boolean
     * @throws OCRException if command failed
     */
    public static void runCommand(final List<String> command,
            final boolean isWindows) throws OCRException {
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
                        .format(LogMessageConstant.TESSERACT_FAILED,
                                String.join(" ", command)));
                throw new OCRException(OCRException.TESSERACT_FAILED);
            }

            process.destroy();
        } catch (NullPointerException | IOException | InterruptedException e) {
            LOGGER.error(MessageFormatUtil
                    .format(LogMessageConstant.TESSERACT_FAILED,
                            e.getMessage()));
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new OCRException(OCRException.TESSERACT_FAILED);
        }
    }

    /**
     * Reads required page from provided tiff image.
     *
     * @param inputFile {@link java.io.File}
     * @param pageNumber int
     * @return {@link net.sourceforge.lept4j.Pix}
     */
    public static Pix readPixPageFromTiff(final File inputFile,
            final int pageNumber) {
        // read image
        Pixa pixa = Leptonica.INSTANCE
                .pixaReadMultipageTiff(inputFile.getAbsolutePath());
        int size = pixa.n;
        // in case page number is incorrect
        if (pageNumber >= size) {
            LOGGER.warn(MessageFormatUtil
                    .format(
                            LogMessageConstant.PAGE_NUMBER_IS_INCORRECT,
                            pageNumber,
                            inputFile.getAbsolutePath()));
            return null;
        }
        Pix pix = Leptonica.INSTANCE.pixaGetPix(pixa, pageNumber, 1);
        destroyPixa(pixa);
        // return required page to be preprocessed
        return pix;
    }

    /**
     * Performs default image preprocessing
     * and saves result to temporary file.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix}
     * @return {@link java.lang.String} Path to create preprocessed image
     */
    public static String preprocessPixAndSave(Pix pix,
            final boolean isCustomDictionaryUsed) {
        // preprocess image
        pix = preprocessPix(pix, isCustomDictionaryUsed);
        // save preprocessed file
        String tmpFileName = getTempDir()
                + UUID.randomUUID().toString() + ".png";
        int formatPng = 3;
        Leptonica.INSTANCE.pixWritePng(tmpFileName, pix, formatPng);

        // destroying
        destroyPix(pix);
        return tmpFileName;
    }

    /**
     * Performs default image preprocessing.
     * It includes the following actions:
     * - remove alpha channel
     * - convert to grayscale
     * - thresholding
     * - basic deskewing
     *
     * @param pix {@link net.sourceforge.lept4j.Pix}
     * @return {@link net.sourceforge.lept4j.Pix}
     */
    public static Pix preprocessPix(Pix pix,
            final boolean isCustomDictionaryUsed) {
        pix = Leptonica.INSTANCE.pixRemoveAlpha(pix);
        pix = convertToGrayscale(pix);
        pix = otsuImageThresholding(pix, isCustomDictionaryUsed);
        return pix;
    }

    /**
     * Convert Leptonica <code>Pix</code> to grayscale.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} source
     * @return {@link net.sourceforge.lept4j.Pix} output
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
     * Perform Leptonica Otsu adaptive image thresholding.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} source
     * @return {@link net.sourceforge.lept4j.Pix} output
     */
    public static Pix otsuImageThresholding(final Pix pix,
            final boolean isCustomDictionaryUsed) {
        if (pix != null) {
            PointerByReference pointer = new PointerByReference();
            float scorefract = isCustomDictionaryUsed ? 0.0f : 0.1f;
            Leptonica.INSTANCE
                    .pixOtsuAdaptiveThreshold(pix, pix.w, pix.h,
                            0, 0, scorefract,
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
     * Destroy pix object.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix}
     */
    public static void destroyPix(Pix pix) {
        if (pix != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pix.getPointer());
            Leptonica.INSTANCE.pixDestroy(pRef);
        }
    }

    /**
     * Destroy pixa object.
     *
     * @param pixa {@link net.sourceforge.lept4j.Pixa}
     */
    public static void destroyPixa(Pixa pixa) {
        if (pixa != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pixa.getPointer());
            Leptonica.INSTANCE.pixaDestroy(pRef);
        }
    }

    /**
     * Get png image format in required format.
     * @return {@link java.lang.String}
     */
    public static String getPngImageFormat() {
        return ImageFormats.PNG.getName();
    }

    /**
     * Setting tesseract properties.
     * In java: path to tess data, languages, psm
     * In .Net: psm
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     * @param tessData {@link java.lang.String}
     * @param languages {@link java.lang.String}
     * @param pageSegMode {@link java.lang.Integer}
     * @param userWordsFilePath {@link java.lang.String}
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
     * Create tesseract instance without parameters.
     *
     * @param isWindows boolean
     * @return {@link net.sourceforge.tess4j.ITesseract}
     */
    public static ITesseract createTesseractInstance(final boolean isWindows) {
        if (isWindows) {
            return new Tesseract1();
        } else {
            return new Tesseract();
        }
    }

    /**
     * Create tesseract instance with parameters.
     *
     * @param tessData {@link java.lang.String}
     * @param languages {@link java.lang.String}
     * @param isWindows boolean
     * @param userWordsFilePath {@link java.lang.String}
     * @return {@link net.sourceforge.tess4j.ITesseract}
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
     * Perform ocr for the provided image
     * and return result as string in required format.
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     * @param image {@link java.awt.image.BufferedImage}
     * @param outputFormat {@link OutputFormat}
     * @return {@link java.lang.String}
     * @throws {@link net.sourceforge.tess4j.TesseractException}
     */
    public static String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final BufferedImage image, final OutputFormat outputFormat)
            throws TesseractException {
        String result = tesseractInstance.doOCR(image);
        // setting default oem after processing
        tesseractInstance.setOcrEngineMode(3);
        return result;
    }

    /**
     * Perform ocr for the provided image file
     * and return result as string in required format.
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     * @param image {@link java.io.File}
     * @param outputFormat {@link OutputFormat}
     * @return {@link java.lang.String}
     * @throws {@link net.sourceforge.tess4j.TesseractException}
     */
    public static String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final File image, final OutputFormat outputFormat)
            throws TesseractException {
        String result = tesseractInstance.doOCR(image);
        // setting default oem after processing
        tesseractInstance.setOcrEngineMode(3);
        return result;
    }

    /**
     * Perform ocr for the provided Pix object
     * and return result as string in required format.
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     * @param pix {@link net.sourceforge.lept4j.Pix}
     * @param outputFormat {@link OutputFormat}
     * @return {@link java.lang.String}
     * @throws {@link net.sourceforge.tess4j.TesseractException}
     */
    public static String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final Pix pix, final OutputFormat outputFormat)
            throws TesseractException, IOException {
        BufferedImage bufferedImage = convertPixToImage(pix);
        return getOcrResultAsString(tesseractInstance,
                bufferedImage, outputFormat);
    }

    /**
     * Return true if tesseract instance is disposed.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     * @return boolean
     */
    public static boolean isTesseractInstanceDisposed(
            final ITesseract tesseractInstance) {
        return false;
    }

    /**
     * Dispose Tesseract instance.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     */
    public static void disposeTesseractInstance(
            final ITesseract tesseractInstance) {
    }

    /**
     * Converts <code>BufferedImage</code> to Leptonica <code>Pix</code>.
     *
     * @param bufferedImage {@link java.awt.image.BufferedImage}
     * @return Pix {@link net.sourceforge.lept4j.Pix}
     * @throws IOException if it's not possible to convert
     */
    public static Pix convertImageToPix(
            final BufferedImage bufferedImage)
            throws IOException {
        return convertBufferedImageToPix(bufferedImage);
    }

    /**
     * Read Pix from file or convert from buffered image.
     *
     * @param inputFile {@link java.io.File}
     * @return Pix {@link net.sourceforge.lept4j.Pix}
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
                                    LogMessageConstant.READING_IMAGE_AS_PIX,
                                    inputFile.getAbsolutePath(),
                                    e.getMessage()));
            pix = Leptonica.INSTANCE.pixRead(inputFile.getAbsolutePath());
        }
        return pix;
    }

    /**
     * Converts Leptonica Pix to image.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix}
     * @return {@link java.awt.image.BufferedImage}
     * @throws IOException IOException
     */
    public static BufferedImage convertPixToImage(final Pix pix)
            throws IOException {
        int format_png = 3;
        return convertPixToBufferedImage(pix, format_png);
    }

    /**
     * Converts <code>BufferedImage</code> to Leptonica <code>Pix</code>.
     *
     * @param bufferedImage {@link java.awt.image.BufferedImage}
     * @return {@link net.sourceforge.lept4j.Pix}
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
     * Converts Leptonica <code>Pix</code> to <code>BufferedImage</code>.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix}
     * @param format int
     * @return {@link java.awt.image.BufferedImage}
     * @throws IOException IOException
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
     * Get system temporary directory.
     *
     * @return String
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
}
