package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.ptr.PointerByReference;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;
import org.apache.commons.imaging.ImageFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Image Util class.
 * <p>
 * Class provides tool for basic image preprocessing.
 */
public class ImageUtil {

    /**
     * ImageUtil logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ImageUtil.class);

    /**
     * Performs basic image preprocessing using buffered image (if provided).
     * Preprocessed image file will be saved in temporary directory
     * (warning will be logged if file isn't deleted)
     *
     * @param inputFile File
     * @return List<BufferedImage>
     * @throws IOException IOException
     */
    public static File preprocessImage(final File inputFile)
            throws IOException {
        // read image
        Pix pix = readPix(inputFile);

        String extension = getExtension(inputFile.getAbsolutePath());
        int format = getFormat(extension);

        // preprocess image
        pix = preprocessPix(pix);

        BufferedImage bi = convertPixToImage(pix, format);

        // save preprocessed file
        File tmpFile = File.createTempFile(UUID.randomUUID().toString(),
                ".png");
        Leptonica.INSTANCE.pixWritePng( tmpFile.getAbsolutePath(), pix, format);

        // destroying
        if (pix != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pix.getPointer());
            Leptonica.INSTANCE.pixDestroy(pRef);
        }
        return tmpFile;
    }

    public static Pix readPix(final File inputFile) {
        Pix pix = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(inputFile);
            if (bufferedImage != null) {
                pix = convertBufferedImageToPix(bufferedImage);
            } else {
                LOGGER.info("Reading pix from file");
                pix = Leptonica.INSTANCE.pixRead(inputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.warn("Reading pix from file: " + e.getMessage());
            pix = Leptonica.INSTANCE.pixRead(inputFile.getAbsolutePath());
        }

        return pix;
    }

    public static Pix convertBufferedImageToPix(final BufferedImage bufferedImage) throws IOException {
        Pix pix = null;
        if (bufferedImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);

            ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
            NativeSize nativeSize = new NativeSize(baos.toByteArray().length);
            pix = Leptonica.INSTANCE.pixReadMem(byteBuffer, nativeSize);
            LOGGER.info("Reading BI from file and converted to pix");
        }

        return pix;
    }

    /**
     * Performs basic image preprocessing using buffered image (if provided).
     * Preprocessed image file will be saved in temporary directory
     * (warning will be logged if file isn't deleted)
     *
     * @param inputFile File
     * @return List<BufferedImage>
     * @throws IOException IOException
     */
    public static File preprocessTiffImage(final File inputFile)
            throws IOException {
        Leptonica instance = Leptonica.INSTANCE;
        // read image
        Pixa pixa = instance.pixaReadMultipageTiff(inputFile.getAbsolutePath());
        int size = pixa.n;
        Pixa newpixa = instance.pixaCreate(size);

        // preprocess images
        for (int i = 0; i < size; ++i) {
            Pix pix = instance.pixaGetPix(pixa, i, 1);
            // pix format IFF_TIFF = 4
            pix = preprocessPix(pix);
            int error = instance.pixaAddPix(newpixa, pix, 1);
            // if there was any error, preprocessing will be stopped
            if (error == 1) {
                LOGGER.warn("Cannot preprocess file "
                        + inputFile.getAbsolutePath());
                return null;
            }
        }
        // save preprocessed file
        String extension = getExtension(inputFile.getAbsolutePath());
        File tmpFile = File.createTempFile(UUID.randomUUID().toString(),
                "." + extension);
        instance.pixaWriteMultipageTiff(tmpFile.getAbsolutePath(), newpixa);
        return tmpFile;
    }

    /**
     * Save pix or pix converted to buffered image to temporary file
     *
     * @param pix Pix
     * @param format int
     * @return File
     * @throws IOException if file wasn't created
     */
    public static File saveImgToTempFile(Pix pix, int format) throws IOException {
        Leptonica instance = Leptonica.INSTANCE;

        File tmpFile = File.createTempFile(UUID.randomUUID().toString(),
                ".png");
        LOGGER.info("Creating tmp preprocessed file "
                + tmpFile.getAbsolutePath());
        try {
            BufferedImage img = convertPixToImage(pix, format);
            if (img != null) {
                ImageIO.write(img, String.valueOf(ImageFormats.PNG), tmpFile);
                LOGGER.info("Saved BufferedImage");
            } else {
                instance.pixWritePng(tmpFile.getAbsolutePath(), pix, format);
                LOGGER.info("Saved Pix");
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot convert pix to "
                    + "buffered image after converting: "
                    + e.getMessage());
            instance.pixWritePng(tmpFile.getAbsolutePath(), pix, format);
            LOGGER.info("Saved Pix");
        }
        return tmpFile;
    }

    /**
     * Performs default image preprocessing.
     * It includes the following actions:
     * - remove alpha channel
     * - convert to grayscale
     * - thresholding
     * - basic deskewing
     *
     * @param pix Pix
     * @return Pix
     */
    private static Pix preprocessPix(Pix pix) {
        Leptonica instance = Leptonica.INSTANCE;
        pix = instance.pixRemoveAlpha(pix);
        pix = convertToGrayscale(pix);
        pix = otsuImageThresholding(pix);
        pix = instance.pixDeskew(pix, 0);
        // pix = instance.pixScale(pix, 2, 2);
        return pix;
    }

    /**
     * Convert Leptonica <code>Pix</code> to grayscale.
     *
     * @param pix source pix
     * @return Pix output pix
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
            return pix;
        }
    }

    /**
     * Perform Leptonica Otsu adaptive image thresholding.
     *
     * @param pix source pix
     * @return Pix output pix
     */
    public static Pix otsuImageThresholding(final Pix pix) {
        if (pix != null) {
            PointerByReference pointer = new PointerByReference();
            Leptonica.INSTANCE
                    .pixOtsuAdaptiveThreshold(pix, pix.w, pix.h, 0, 0, 0,
                            null, pointer);
            Pix thresholdPix = new Pix(pointer.getValue());
            if (thresholdPix.w > 0 && thresholdPix.h > 0) {
                return thresholdPix;
            } else {
                return pix;
            }
        } else {
            return pix;
        }
    }

    /**
     * Get file extension for later usage.
     *
     * @param path String
     * @return String
     */
    public static String getExtension(final String path) {
        int index = path.lastIndexOf('.');
        if (index > 0) {
            String extension = new String(path.toCharArray(), index + 1,
                    path.length() - index - 1);
            if (extension.toLowerCase().contains("jpg")
                    || extension.toLowerCase().contains("jpeg")
                    || extension.toLowerCase().contains("jpe")
                    || extension.toLowerCase().contains("jfif")) {
                return "jpeg";
            } else if (extension.toLowerCase().contains("pbm")
                    || extension.toLowerCase().contains("pgm")
                    || extension.toLowerCase().contains("pnm")
                    || extension.toLowerCase().contains("ppm")) {
                return "pnm";
            } else {
                return extension;
            }
        }
        return "";
    }

    /**
     * Identify image format for Leptonica.
     *
     * @param ext String
     * @return int
     */
    static int getFormat(final String ext) {
        String formatName = "IFF_";
        if (ext.toLowerCase().contains("jpg")
                || ext.toLowerCase().contains("jpeg")
                || ext.toLowerCase().contains("jpe")
                || ext.toLowerCase().contains("jfif")) {
            formatName += "JFIF_JPEG";
        } else if (ext.toLowerCase().contains("tif")) {
            formatName += "TIFF";
        } else {
            formatName += ext.toUpperCase();
        }

        int format = 0;
        Field field = null;
        try {
            field = ILeptonica.class.getField(formatName);
            if (field.getType() == int.class) {
                format = field.getInt(null);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error(formatName + " does not exist: "
                    + e.getMessage());
        }
        return format;
    }

    public static boolean isTiffImage(File inputImage) {
        int index = inputImage.getAbsolutePath().lastIndexOf('.');
        if (index > 0) {
            String extension = new String(inputImage.getAbsolutePath().toCharArray(),
                    index + 1,
                    inputImage.getAbsolutePath().length() - index - 1);
            return extension.toLowerCase().contains("tif");
        }
        return false;
    }

    /**
     * Converts Leptonica <code>Pix</code> to <code>BufferedImage</code>.
     *
     * @param pix    source pix
     * @param format int
     * @return BufferedImage output image
     * @throws IOException IOException
     */
    public static BufferedImage convertPixToImage(final Pix pix,
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
}
