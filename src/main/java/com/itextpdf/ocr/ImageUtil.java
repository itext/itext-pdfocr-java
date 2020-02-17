package com.itextpdf.ocr;

import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;
import org.apache.commons.io.FilenameUtils;
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
        String extension = getExtension(inputFile.getAbsolutePath());
        int format = getFormat(extension);

        if (extension.toLowerCase().contains("tif")) {
            return preprocessTiffImage(inputFile);
        } else {
            Leptonica instance = Leptonica.INSTANCE;
            // read image
            Pix pix = instance.pixRead(inputFile.getAbsolutePath());
            // preprocess image
            pix = preprocessPix(pix);

            // save preprocessed file
            File tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".png");
            instance.pixWritePng(tmpFile.getAbsolutePath(), pix, format);

            // destroying
            if (pix != null) {
                PointerByReference pRef = new PointerByReference();
                pRef.setValue(pix.getPointer());
                instance.pixDestroy(pRef);
            }
            return tmpFile;
        }
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
                LOGGER.warn("Cannot preprocess file " + inputFile.getAbsolutePath());
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
     * Performs default image preprocessing.
     * It includes the following actions:
     * - remove alpha channel
     * - convert to grayscale
     * - thresholding
     * - basic deskewing
     *
     * @param pix
     * @return
     * @throws IOException
     */
    private static Pix preprocessPix(Pix pix) {
        Leptonica instance = Leptonica.INSTANCE;
        pix = instance.pixRemoveAlpha(pix);
        pix = convertToGrayscale(pix);
        pix = otsuImageThresholding(pix);
        pix = instance.pixDeskew(pix, 0);
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
                return instance.pixRemoveColormap(pix, instance.REMOVE_CMAP_TO_GRAYSCALE);
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
     * @param path
     * @return
     */
    public static String getExtension(String path) {
        String extension = FilenameUtils.getExtension(path);
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

    /**
     * Identify image format for Leptonica.
     *
     * @param ext String
     * @return int
     */
    private static int getFormat(final String ext) {
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
                    + e.getLocalizedMessage());
        }
        return format;
    }
}
