package com.itextpdf.ocr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tesseract reader class.
 * <p>
 * This class provides possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems)
 * <p>
 * This class provides possibility to perform OCR, read data from input files
 * and return contained text in the described format
 * <p>
 * This class provides possibilities to set type of current os,
 * required languages for OCR for input images,
 * set path to directory with tess data.
 */
public abstract class TesseractReader implements IOcrReader {

    /**
     * Path to quiet config script.
     */
    public static final String PATH_TO_QUIET_SCRIPT = "src/main/resources/"
            + "com/itextpdf/ocr/configs/quiet";

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
     * Page Segmentation Mode.
     */
    private Integer pageSegMode;

    /**
     * Type of current OS.
     */
    private String osType;

    /**
     * "True" - if images need to be preprocessed, otherwise - false.
     * By default - true.
     */
    private boolean preprocessingImages = true;

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFile - output file
     */
    public abstract void doTesseractOcr(File inputImage,
            File outputFile);

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
     * Set true if images need to be preprocessed, otherwise - false.
     *
     * @param preprocess boolean
     */
    public final void setPreprocessingImages(final boolean preprocess) {
        preprocessingImages = preprocess;
    }

    /**
     * @return true if images need to be preprocessed, otherwise - false
     */
    public final boolean isPreprocessingImages() {
        return preprocessingImages;
    }

    /**
     * Reads data from input stream and returns retrieved data
     * in the following format:
     * <p>
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
     *
     * @return String
     */
    String getTessData() {
        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            return getPathToTessData();
        } else {
            throw new OCRException(OCRException.CANNOT_FIND_PATH_TO_TESSDATA);
        }
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

    /**
     * Validate provided languages and
     * check if they exist in provided tess data directory.
     */
    public void validateLanguages() {
        String suffix = ".traineddata";
        List<String> languages = getLanguages();
        if (languages.isEmpty()) {
            if (!new File(getTessData() + File.separator + "eng" + suffix).exists()) {
                LOGGER.error("eng" + suffix
                        + " doesn't exist in provided directory");
                throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                        .setMessageParams("eng" + suffix, getTessData());
            }
        } else {
            for (String lang : languages) {
                if (!new File(getTessData() + File.separator + lang + suffix)
                        .exists()) {
                    LOGGER.error(lang + suffix
                            + " doesn't exist in provided directory");
                    throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }
}
