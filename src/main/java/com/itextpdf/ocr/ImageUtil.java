package com.itextpdf.ocr;

import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.ptr.PointerByReference;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Image Util class.
 *
 * Class provides tool for basic image preprocessing.
 */
public class ImageUtil {

    /**
     * ImageUtil logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ImageUtil.class);

    /**
     * Performs default image preprocessing.
     * It includes the following actions:
     *  - remove alpha channel
     *  - convert to grayscale
     *  - thresholding
     *  - basic deskewing
     *
     * @param inputPath String
     * @return BufferedImage
     * @throws IOException IOException
     */
    public static BufferedImage preprocessImage(final String inputPath)
            throws IOException {
        Leptonica instance = Leptonica.INSTANCE;
        Pix pix = instance.pixRead(inputPath);

        pix = instance.pixRemoveAlpha(pix);

        pix = convertToGrayscale(pix);

        pix = otsuImageThresholding(pix);

        pix = instance.pixDeskew(pix, 0);

        int format = getFormat(inputPath);

        instance.pixWritePng("deskew.png", pix, format);
        BufferedImage bi = convertPixToImage(pix, format);

        // destroying
        PointerByReference pRef = new PointerByReference();
        pRef.setValue(pix.getPointer());
        instance.pixDestroy(pRef);
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
        int depth = instance.pixGetDepth(pix);

        if (depth == 32) {
            return instance.pixConvertRGBToGrayFast(pix);
        } else {
            return instance.pixRemoveColormap(pix,
                    net.sourceforge.lept4j.ILeptonica.REMOVE_CMAP_TO_GRAYSCALE);
        }
    }

    /**
     * Perform Leptonica Otsu adaptive image thresholding.
     *
     * @param pix source pix
     * @return Pix output pix
     */
    public static Pix otsuImageThresholding(final Pix pix) {
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
    }

    /**
     * Converts Leptonica <code>Pix</code> to <code>BufferedImage</code>.
     *
     * @param pix source pix
     * @param format int
     * @return BufferedImage output image
     * @throws IOException IOException
     */
    public static BufferedImage convertPixToImage(final Pix pix,
                                                  final int format)
            throws IOException {
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
    }

    /**
     * Identify image format for Leptonica.
     *
     * @param inputPath String
     * @return int
     */
    private static int getFormat(final String inputPath) {
        String ext = FilenameUtils.getExtension(inputPath);
        String formatName = "IFF_";
        if (ext.toLowerCase().contains("jpg")
                || ext.toLowerCase().contains("jpeg")
                || ext.toLowerCase().contains("jfif")) {
            formatName += "JFIF_JPEG";
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
