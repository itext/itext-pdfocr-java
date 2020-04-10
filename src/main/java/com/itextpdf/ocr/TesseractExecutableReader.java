package com.itextpdf.ocr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tesseract Executable Reader class.
 * (extends Tesseract Reader class)
 * <p>
 * This class provides possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems)
 * <p>
 * This class provides possibility to perform OCR, read data from input files
 * and return contained text in the described format
 * <p>
 * This class provides possibilities to set type of current os,
 * required languages for OCR for input images,
 * set path to directory with tess data and set path
 * to the tesseract executable
 * <p>
 * Please note that It's assumed that "tesseract" is already
 * installed in the system
 */
public class TesseractExecutableReader extends TesseractReader {

    /**
     * TesseractExecutableReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractExecutableReader.class);

    /**
     * Path to the script.
     */
    private String pathToScript;

    /**
     * Path to the tesseract executable.
     * By default it's assumed that "tesseract" already exists in the PATH
     */
    private String pathToExecutable;

    /**
     * TesseractExecutableReader constructor with path to tess data directory.
     *
     * @param tessDataPath String
     */
    public TesseractExecutableReader(final String tessDataPath) {
        setPathToExecutable("tesseract");
        setOsType(identifyOSType());
        setPathToTessData(tessDataPath);
    }

    /**
     * TesseractExecutableReader constructor with path to executable and
     * path to tess data directory.
     *
     * @param executablePath String
     * @param tessDataPath String
     */
    public TesseractExecutableReader(final String executablePath,
            final String tessDataPath) {
        setPathToExecutable(executablePath);
        setOsType(identifyOSType());
        setPathToTessData(tessDataPath);
    }

    /**
     * TesseractExecutableReader constructor with path to executable,
     * list of languages and path to tess data directory.
     *
     * @param path          String
     * @param languagesList List<String>
     * @param tessDataPath  String
     */
    public TesseractExecutableReader(final String path,
            final String tessDataPath,
            final List<String> languagesList) {
        setPathToExecutable(path);
        setLanguages(Collections.<String>unmodifiableList(languagesList));
        setPathToTessData(tessDataPath);
        setOsType(identifyOSType());
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
     * Set path to script.
     *
     * @param path String
     */
    public final void setPathToScript(final String path) {
        pathToScript = path;
    }

    /**
     * Get path to script.
     *
     * @return String
     */
    public final String getPathToScript() {
        return pathToScript;
    }

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFiles - list of output files (one for each page)
     *        for tesseract executable only the first file is required
     * @param outputFormat - output format
     * @param pageNumber - number of page to be OCRed
     */
    public void doTesseractOcr(final File inputImage,
            final List<File> outputFiles, final OutputFormat outputFormat,
            final int pageNumber) {
        List<String> command = new ArrayList<String>();
        String imagePath = inputImage.getAbsolutePath();

        try {
            // path to tesseract executable
            addPathToExecutable(command);
            // path to tess data
            addTessData(command);

            // validate languages before preprocessing started
            validateLanguages(getLanguagesAsList());

            // preprocess input file if needed and add it
            imagePath = preprocessImage(inputImage, pageNumber);
            addInputFile(command, imagePath);
            // output file
            addOutputFile(command, outputFiles.get(0), outputFormat);
            // page segmentation mode
            addPageSegMode(command);
            // add user words if needed
            addUserWords(command);
            // required languages
            addLanguages(command);
            if (outputFormat.equals(OutputFormat.hocr)) {
                // path to hocr script
                setHocrOutput(command);
            }
            addPathToScript(command);

            TesseractUtil.runCommand(command, isWindows());
        } catch (OCRException e) {
            LOGGER.error("Running tesseract executable failed: " + e);
            throw new OCRException(e.getMessage());
        } finally {
            if (imagePath != null && isPreprocessingImages()
                    && !inputImage.getAbsolutePath().equals(imagePath)) {
                UtilService.deleteFile(imagePath);
            }
            if (getUserWordsFilePath() != null) {
                UtilService.deleteFile(getUserWordsFilePath());
            }
        }
    }

    /**
     * Add path to tesseract executable.
     *
     * @param command List<String>
     * @throws OCRException if path to executable is not set
     */
    private void addPathToExecutable(final List<String> command)
            throws OCRException {
        // path to tesseract executable cannot be uninitialized
        if (getPathToExecutable() == null
                || getPathToExecutable().isEmpty()) {
            throw new OCRException(
                    OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        } else {
            command.add(addQuotes(getPathToExecutable()));
        }
    }

    /**
     * Set hocr output format.
     *
     * @param command List<String>
     */
    private void setHocrOutput(final List<String> command) {
        command.add("-c");
        command.add("tessedit_create_hocr=1");
    }

    /**
     * Add path to script.
     *
     * @param command List<String>
     */
    private void addPathToScript(final List<String> command) {
        if (getPathToScript() != null
                && !getPathToScript().isEmpty()) {
            command.add(addQuotes(getPathToScript()));
        }
    }

    /**
     * Add path to user-words file for tesseract executable.
     *
     * @param command List<String>
     */
    private void addUserWords(final List<String> command) {
        if (getUserWordsFilePath() != null
                && !getUserWordsFilePath().isEmpty()) {
            command.add("--user-words");
            command.add(addQuotes(getUserWordsFilePath()));
            command.add("--oem");
            command.add("0");
        }
    }

    /**
     * Add path to tess data.
     *
     * @param command List<String>
     */
    private void addTessData(final List<String> command) {
        if (getPathToTessData() != null
                && !getPathToTessData().isEmpty()) {
            command.add("--tessdata-dir");
            command.add(addQuotes(getTessData()));
        }
    }

    /**
     * Add select Page Segmentation Mode as parameter.
     *
     * @param command List<String>
     */
    private void addPageSegMode(final List<String> command) {
        if (getPageSegMode() != null) {
            command.add("--psm");
            command.add(String.valueOf(getPageSegMode()));
        }
    }

    /**
     * Add list pf selected languages as parameter.
     *
     * @param command List<String>
     */
    private void addLanguages(final List<String> command) {
        if (getLanguagesAsList().size() > 0) {
            command.add("-l");
            command.add(getLanguagesAsString());
        }
    }

    /**
     * Preprocess input image (if needed) and add path to this file.
     *
     * @param command List<String>
     * @param imagePath path to file
     */
    private void addInputFile(final List<String> command,
            final String imagePath) {
        command.add(addQuotes(imagePath));
    }

    /**
     * Add path to temporary output file.
     *
     * @param command    List<String>
     * @param outputFile output file
     * @param outputFormat output format
     */
    private void addOutputFile(final List<String> command,
            final File outputFile, final OutputFormat outputFormat) {
        String extension = outputFormat.equals(OutputFormat.hocr)
                ? ".hocr" : ".txt";
        String fileName = new String(
                outputFile.getAbsolutePath().toCharArray(), 0,
                outputFile.getAbsolutePath().indexOf(extension));
        LOGGER.info("Temp path: " + outputFile.toString());
        command.add(addQuotes(fileName));
    }

    /**
     * Surrounds given string with quotes.
     *
     * @param value String
     * @return String in quotes
     */
    private String addQuotes(final String value) {
        return "\"" + value + "\"";
    }

    /**
     * Preprocess given image if it is needed.
     *
     * @param inputImage original input image
     * @param pageNumber number of page to be OCRed
     * @return path to output image
     */
    private String preprocessImage(final File inputImage,
            final int pageNumber) {
        String path = inputImage.getAbsolutePath();
        if (isPreprocessingImages()) {
            path = ImageUtil.preprocessImage(inputImage, pageNumber);
        }
        return path;
    }
}
