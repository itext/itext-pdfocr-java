package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
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
     *
     * @param inputPath String
     * @param bufferedImage BufferedImage
     * @return BufferedImage
     * @throws IOException IOException
     */
    public static BufferedImage preprocessImage(final String inputPath,
            final BufferedImage bufferedImage)
            throws IOException {
        String extension = getExtension(inputPath);

        Leptonica instance = Leptonica.INSTANCE;
        // read image
        Pix pix = null;
        if (bufferedImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);

            ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
            NativeSize nativeSize = new NativeSize(baos.toByteArray().length);
            pix = instance.pixReadMem(byteBuffer, nativeSize);
        } else {
            pix = instance.pixRead(inputPath);
            if (pix == null && extension.toLowerCase().contains("tif")) {
                pix = instance.pixReadTiff(inputPath, 0);
            }
        }

        int format = getFormat(extension);
        return preprocessPixToBufferedImage(pix, format);
    }

    /**
     * Performs basic image preprocessing.
     *
     * @param inputPath String
     * @return BufferedImage
     * @throws IOException IOException
     */
    public static BufferedImage preprocessImage(final String inputPath)
            throws IOException {
        return preprocessImage(inputPath, null);
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
     * @param format
     * @return
     * @throws IOException
     */
    private static BufferedImage preprocessPixToBufferedImage(Pix pix,
            final int format) throws IOException {
        Leptonica instance = Leptonica.INSTANCE;
        pix = instance.pixRemoveAlpha(pix);

        pix = convertToGrayscale(pix);

        pix = otsuImageThresholding(pix);

        pix = instance.pixDeskew(pix, 0);

        instance.pixWritePng("deskew.png", pix, format);
        BufferedImage bi = convertPixToImage(pix, format);

        // destroying
        if (pix != null) {
            PointerByReference pRef = new PointerByReference();
            pRef.setValue(pix.getPointer());
            instance.pixDestroy(pRef);
        }
        return bi;
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
                        net.sourceforge.lept4j.ILeptonica.REMOVE_CMAP_TO_GRAYSCALE);
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
