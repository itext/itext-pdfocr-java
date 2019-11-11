package com.itextpdf.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tesseract reader class.
 *
 * This class provides possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems)
 *
 * This class provides possibility to perform OCR, read data from input files
 * and return contained text in the described format
 *
 * This class provides possibilities to set type of current os,
 * required scripts and language for OCR for input images,
 * set path to directory with tess data and set path
 * to the tesseract executable
 *
 * Please note that It's assumed that "tesseract" is already
 * installed in the system
 *
 */
public class TesseractReader implements IOcrReader {

    /**
     * Path to hocr config script.
     */
    private static String pathToHocr = "src/main/resources/com/itextpdf/ocr/configs/hocr";

    /**
     *  Type of current OS.
     */
    private String osType;

    /**
     *  List of languages required for ocr for provided images.
     */
    private List<String> languages;

    /**
     *  List of scripts required for ocr for provided images.
     */
    private List<String> scripts;

    /**
     *  Path to directory with tess data.
     */
    private String tessDataDir;

    /**
     *  Path to the tesseract executable.
     *  By default it's assumed that "tesseract" already exists in the PATH
     */
    private String pathToExecutable;

    /**
     * TesseractReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractReader.class);

    /**
     * TesseractReader constructor.
     */
    public TesseractReader() {
        pathToExecutable = "tesseract";
        osType = identifyOSType();
    }

    /**
     * TesseractReader constructor with path to executable.
     *
     * @param path String
     */
    public TesseractReader(final String path) {
        pathToExecutable = path;
        osType = identifyOSType();
    }

    /**
     * TesseractReader constructor with path to executable,
     * list of languages, scripts and path to tessData directory.
     *
     * @param path String
     * @param languagesList List<String>
     * @param scriptsList List<String>
     * @param tessData String
     */
    public TesseractReader(final String path, final List<String> languagesList,
                           final List<String> scriptsList,
                           final String tessData) {
        pathToExecutable = path;
        languages = languagesList;
        scripts = scriptsList;
        tessDataDir = tessData;
        osType = identifyOSType();
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
     * Set list of languages required for provided images.
     *
     * @param requiredLanguages List<String>
     */
    public final void setLanguages(final List<String> requiredLanguages) {
        languages = requiredLanguages;
    }

    /**
     * Get list of languages required for provided images.
     *
     * @return List<String>
     */
    public final List<String> getLanguages() {
        return languages;
    }

    /**
     * Set list of scripts required for provided images.
     *
     * @param requiredScripts List<String>
     */
    public final void setScripts(final List<String> requiredScripts) {
        scripts = requiredScripts;
    }

    /**
     * Get list of scripts required for provided images.
     *
     * @return List<String>
     */
    public final List<String> getScripts() {
        return scripts;
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
     * Set path to tesseract executable.
     * By default it's assumed that "tesseract" already exists in the PATH
     *
     * @param path String
     */
    public final void setPathToExecutable(final String path) {
        pathToExecutable = path;
    }

    /**
     * Get path to tesseract executable.
     *
     * @return String
     */
    public final String getPathToExecutable() {
        return pathToExecutable;
    }

    /**
     * Reads data from the provided input image file and returns retrieved
     * data in the following format:
     * List<Map.Entry<String, List<Integer>>> where each list element
     * Map.Entry<String, List<Integer>> contains word or line as a key
     * and its 4 coordinates(bbox) as a values.
     *
     * @param input File
     * @return List<TextInfo>
     */
    public final List<TextInfo> readDataFromInput(final File input) {
        String type = "hocr";

        List<TextInfo> words = new ArrayList<>();
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            // File outputHocr = File.createTempFile(UUID.randomUUID().toString(), "hocr");
            String tmpPath = "src/test/resources/com/itextpdf/ocr/tmp";

            File tmpFile = null;

            LOGGER.info("Temp path: " + tmpPath + "." + type);
            if (doTesseractOcr(input.getAbsolutePath(), tmpPath)) {
                tmpFile = new File(tmpPath + "." + type);
                if (tmpFile.exists()) {
                    words = UtilService.parseHocrFile(tmpFile);

                    LOGGER.info(words.size() + " word(s) were read");
                } else {
                    LOGGER.error("Error occurred. File wasn't created "
                            + tmpFile.getAbsolutePath());
                }
            } else {
                LOGGER.error("Cannot read data from output");
            }

            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred:" + e.getLocalizedMessage());
        }

        return words;
    }

    /**
     * Perform tesseract OCR.
     *
     * @param inputPath - path to the file with input image
     * @param outputPath String
     * @return true if tesseract OCR action succeeded, false - if not
     */
    public final boolean doTesseractOcr(final String inputPath,
                                  final String outputPath) {
        System.setProperty("java.awt.headless", "true");

        // path to tesseract executable cannot be uninitialized
        if (pathToExecutable == null || pathToExecutable.isEmpty()) {
            return false;
        }
        List<String> command = new ArrayList<>();

        command.add("\"" + pathToExecutable + "\"");

        if (tessDataDir != null && !tessDataDir.isEmpty()) {
            command.addAll(
                    Arrays.asList("--tessdata-dir", "\"" + tessDataDir + "\""));
        }

        command.addAll(
                Arrays.asList("\"" + inputPath + "\"",
                        "\"" + outputPath + "\""));

        if (languages != null && !languages.isEmpty()) {
            command.addAll(
                    Arrays.asList("-l", String.join("+", languages)));
        }

        command.add(pathToHocr);
        command.add("quiet");

        return UtilService.runCommand(command, isWindows());
    }

    /**
     * Check type of current OS and return it (mac, win, linux).
     *
     * @return String
     */
    private String identifyOSType() {
        String os = System.getProperty("os.name");
        LOGGER.info("Using System Property: " + os);
        return os.toLowerCase();
    }

    /**
     * Return 'true' if current OS is windows
     * otherwise 'false'.
     *
     * @return boolean
     */
    private boolean isWindows() {
        return osType.toLowerCase().contains("win");
    }
}

/**
 * TextInfo class.
 *
 * This class describes item of text info retrieved
 * from HOCR file after parsing
 *
 */
class TextInfo {

    /**
     * Contains word or line.
     */
    private String text;

    /**
     * Contains 4 coordinates: bbox parameters.
     */
    private List<Integer> coordinates;

    /**
     * Number of page for given text.
     */
    private Integer page;

    /**
     * TextInfo Constructor.
     *
     * @param newText String
     * @param newPage Integer
     * @param newCoordinates List<Integer>
     */
    TextInfo(final String newText, final Integer newPage,
             final List<Integer> newCoordinates) {
        text = newText;
        page = newPage;
        coordinates = newCoordinates;
    }

    /**
     * Text element.
     *
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Text element.
     *
     * @param newText String
     */
    public void setText(final String newText) {
        text = newText;
    }

    /**
     * Page of the word/text.
     *
     * @return Integer
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Page of the word/text.
     *
     * @param newPage Integer
     */
    public void setPage(final Integer newPage) {
        page = newPage;
    }

    /**
     * Bbox coordinates.
     *
     * @return List<Integer>
     */
    public List<Integer> getCoordinates() {
        return coordinates;
    }

    /**
     * Bbox coordinates.
     *
     * @param newCoordinates List<Integer>
     */
    public void setCoordinates(final List<Integer> newCoordinates) {
        coordinates = newCoordinates;
    }
}
