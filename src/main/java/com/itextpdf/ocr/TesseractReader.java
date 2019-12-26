package com.itextpdf.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class TesseractReader implements IOcrReader {

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
}