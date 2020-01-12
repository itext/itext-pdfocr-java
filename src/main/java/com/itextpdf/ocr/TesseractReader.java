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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.sourceforge.lept4j.ILeptonica.REMOVE_CMAP_TO_GRAYSCALE;

public abstract class TesseractReader implements IOcrReader {

    /**
     * Path to default tess data script.
     */
    public static final String PATH_TO_TESS_DATA = "src/main/resources/com/" +
            "itextpdf/ocr/tessdata/";

    /**
     * Path to quiet config script.
     */
    public static final String PATH_TO_QUIET_SCRIPT = "src/main/resources/com/itextpdf/"
            + "ocr/configs/quiet";

    /**
     * TesseractReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractReader.class);

    /**
     * List of languages required for ocr for provided images.
     */
    private List<String> languages = Collections.emptyList();

    /**
     * Path to directory with tess data.
     */
    private String tessDataDir;

    /**
     * Page Segmentation Mode
     */
    private Integer pageSegMode;

    /**
     * Type of current OS.
     */
    private String osType;

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFile - output file
     */
    public abstract void doTesseractOcr(final File inputImage,
                                        final File outputFile);
    /**
     * Set list of languages required for provided images.
     *
     * @param requiredLanguages List<String>
     */
    public final void setLanguages(final List<String> requiredLanguages) {
        languages = Collections.unmodifiableList(requiredLanguages);
    }

    /**
     * Get list of languages required for provided images.
     *
     * @return List<String>
     */
    public final List<String> getLanguages() {
        return new ArrayList<>(languages);
    }

    /**
     * Set path to directory with tess data.
     *
     * @param tessData String
     */
    public final void setPathToTessData(final String tessData) {
        tessDataDir = tessData;
    }

    /**
     * Get path to directory with tess data.
     *
     * @return String
     */
    public final String getPathToTessData() {
        return tessDataDir;
    }

    /**
     * Set Page Segmentation Mode.
     *
     * @param mode Integer
     */
    public final void setPageSegMode(final Integer mode) {
        pageSegMode = mode;
    }

    /**
     * Get Page Segmentation Mode.
     *
     * @return Integer pageSegMode
     */
    public final Integer getPageSegMode() {
        return pageSegMode;
    }

    /**
     * Set type of current OS.
     *
     * @param os String
     */
    public final void setOsType(final String os) {
        osType = os;
    }

    /**
     * Get type of current OS.
     *
     * @return String
     */
    public final String getOsType() {
        return osType;
    }

    /**
     * Reads data from input stream and returns retrieved data
     * in the following format:
     *
     * List<TextInfo> where each list TextInfo element contains word
     * or line and its 4 coordinates(bbox).
     *
     * @param is InputStream
     * @return List<TextInfo>
     */
    public final List<TextInfo> readDataFromInput(final InputStream is) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads data from the provided input image file and returns retrieved
     * data in the following format:
     * List<Map.Entry<String, List<Float>>> where each list element
     * Map.Entry<String, List<Float>> contains word or line as a key
     * and its 4 coordinates(bbox) as a values.
     *
     * @param input File
     * @return List<TextInfo>
     */
    public final List<TextInfo> readDataFromInput(final File input) {
        List<TextInfo> words = new ArrayList<>();
        try {
            File tmpFile = File.createTempFile(UUID.randomUUID().toString(),
                    ".hocr");
            doTesseractOcr(input, tmpFile);
            if (tmpFile.exists()) {
                words = UtilService.parseHocrFile(tmpFile);

                LOGGER.info(words.size() + " word(s) were read");
            } else {
                LOGGER.error("Error occurred. File wasn't created "
                        + tmpFile.getAbsolutePath());
            }

            if (!tmpFile.delete()) {
                LOGGER.error("File " + tmpFile.getAbsolutePath()
                        + " cannot be deleted");
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred:" + e.getLocalizedMessage());
        }

        return words;
    }

    /**
     * Get path to provided tess data directory or return default one.
     * @return String
     */
    String getTessData() {
        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            return getPathToTessData();
        } else {
            return PATH_TO_TESS_DATA;
        }
    }

    public static BufferedImage preprocess(String inputPath)
            throws IOException {
        Leptonica instance = Leptonica.INSTANCE;
        Pix pix = instance.pixRead(inputPath);

        pix = instance.pixRemoveAlpha(pix);
        int a = instance.pixGetDepth(pix);

        if (a == 32) {
            pix = instance.pixConvertRGBToGrayFast(pix);
        } else {
            pix = instance.pixRemoveColormap(pix, REMOVE_CMAP_TO_GRAYSCALE);
        }

        PointerByReference pointer = new PointerByReference();
        instance.pixOtsuAdaptiveThreshold(pix, pix.w, pix.h, 0, 0, 0,
                null, pointer);
        Pix thresholdPix = new Pix(pointer.getValue());
        if (thresholdPix.w > 0 && thresholdPix.h > 0) {
            pix = thresholdPix;
        }

        pix = instance.pixDeskew(pix, 0);

        int format = getFormat(inputPath);

        instance.pixWritePng("deskew.png", pix, format);
        BufferedImage bi = convertPixToImage(pix, format);
        PointerByReference pRef = new PointerByReference();
        pRef.setValue(pix.getPointer());
        instance.pixDestroy(pRef);
        return bi;
    }

    /**
     * Converts Leptonica <code>Pix</code> to <code>BufferedImage</code>.
     *
     * @param pix source pix
     * @return BufferedImage output image
     * @throws IOException
     */
    public static BufferedImage convertPixToImage(Pix pix, int format)
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

    public static int getFormat(String inputPath) {
        String ext = FilenameUtils.getExtension(inputPath);
        String formatName = "IFF_";
        if (ext.toLowerCase().contains("jpg") ||
                ext.toLowerCase().contains("jpeg") ||
                ext.toLowerCase().contains("jfif")) {
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
            LOGGER.error(formatName + " does not exist: " +
                    e.getLocalizedMessage());
        }
        return format;
    }

    /**
     * Return 'true' if current OS is windows
     * otherwise 'false'.
     *
     * @return boolean
     */
    public boolean isWindows() {
        return getOsType().toLowerCase().contains("win");
    }

    /**
     * Check type of current OS and return it (mac, win, linux).
     *
     * @return String
     */
    public String identifyOSType() {
        String os = System.getProperty("os.name");
        LOGGER.info("Using System Property: " + os);
        return os.toLowerCase();
    }
}
