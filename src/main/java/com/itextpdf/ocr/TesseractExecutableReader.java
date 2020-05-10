package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Tesseract Executable Reader class.
 * (extends Tesseract Reader class)
 *
 * This class provides possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems)
 *
 * This class provides possibility to perform OCR, read data from input files
 * and return contained text in the described format
 *
 * This class provides possibilities to set type of current os,
 * required languages for OCR for input images,
 * set path to directory with tess data and set path
 * to the tesseract executable
 *
 * Please note that It's assumed that "tesseract" is already
 * installed in the system
 */
public class TesseractExecutableReader extends TesseractReader {

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
     * Create new TesseractExecutableReader.
     *
     * @param tessDataPath {@link java.lang.String}
     */
    public TesseractExecutableReader(final String tessDataPath) {
        setPathToExecutable("tesseract");
        setOsType(identifyOSType());
        setPathToTessData(tessDataPath);
    }

    /**
     * Create new TesseractExecutableReader.
     *
     * @param executablePath {@link java.lang.String}
     * @param tessDataPath {@link java.lang.String}
     */
    public TesseractExecutableReader(final String executablePath,
            final String tessDataPath) {
        setPathToExecutable(executablePath);
        setOsType(identifyOSType());
        setPathToTessData(tessDataPath);
    }

    /**
     * Create new TesseractExecutableReader.
     *
     * @param path          {@link java.lang.String}
     * @param languagesList {@link java.util.List}
     * @param tessDataPath  {@link java.lang.String}
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
     * @param path {@link java.lang.String}
     */
    public final void setPathToExecutable(final String path) {
        pathToExecutable = path;
    }

    /**
     * Get path to tesseract executable.
     *
     * @return {@link java.lang.String}
     */
    public final String getPathToExecutable() {
        return pathToExecutable;
    }

    /**
     * Set path to script.
     *
     * @param path {@link java.lang.String}
     */
    public final void setPathToScript(final String path) {
        pathToScript = path;
    }

    /**
     * Get path to script.
     *
     * @return {@link java.lang.String}
     */
    public final String getPathToScript() {
        return pathToScript;
    }

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage {@link java.io.File}
     * @param outputFiles {@link java.util.List} of output files
     *        (one for each page)
     *        for tesseract executable only the first file is required
     * @param outputFormat {@link java.io.File}
     * @param pageNumber int, number of page to be OCRed
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
            if (outputFormat.equals(OutputFormat.HOCR)) {
                // path to hocr script
                setHocrOutput(command);
            }
            addPathToScript(command);

            TesseractUtil.runCommand(command, isWindows());
        } catch (OCRException e) {
            LoggerFactory.getLogger(getClass())
                    .error(e.getMessage());
            throw new OCRException(e.getMessage(), e);
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
     */
    private void setHocrOutput(final List<String> command) {
        command.add("-c");
        command.add("tessedit_create_hocr=1");
    }

    /**
     * Add path to script.
     */
    private void addPathToScript(final List<String> command) {
        if (getPathToScript() != null
                && !getPathToScript().isEmpty()) {
            command.add(addQuotes(getPathToScript()));
        }
    }

    /**
     * Add path to user-words file for tesseract executable.
     */
    private void addUserWords(final List<String> command) {
        if (isCustomDictionaryUsed()) {
            command.add("--user-words");
            command.add(addQuotes(getUserWordsFilePath()));
            command.add("--oem");
            command.add("0");
        }
    }

    /**
     * Add path to tess data.
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
     */
    private void addPageSegMode(final List<String> command) {
        if (getPageSegMode() != null) {
            command.add("--psm");
            command.add(String.valueOf(getPageSegMode()));
        }
    }

    /**
     * Add list pf selected languages as parameter.
     */
    private void addLanguages(final List<String> command) {
        if (getLanguagesAsList().size() > 0) {
            command.add("-l");
            command.add(getLanguagesAsString());
        }
    }

    /**
     * Preprocess input image (if needed) and add path to this file.
     */
    private void addInputFile(final List<String> command,
            final String imagePath) {
        command.add(addQuotes(imagePath));
    }

    /**
     * Add path to temporary output file.
     */
    private void addOutputFile(final List<String> command,
            final File outputFile, final OutputFormat outputFormat) {
        String extension = outputFormat.equals(OutputFormat.HOCR)
                ? ".hocr" : ".txt";
        String fileName = new String(
                outputFile.getAbsolutePath().toCharArray(), 0,
                outputFile.getAbsolutePath().indexOf(extension));
        LoggerFactory.getLogger(getClass()).info(
                MessageFormatUtil.format(
                        LogMessageConstant.CREATED_TEMPORARY_FILE,
                        outputFile.getAbsolutePath()));
        command.add(addQuotes(fileName));
    }

    /**
     * Surrounds given string with quotes.
     */
    private String addQuotes(final String value) {
        return "\"" + value + "\"";
    }

    /**
     * Preprocess given image if it is needed.
     *
     * @param inputImage {@link java.io.File} original input image
     * @param pageNumber int, number of page to be OCRed
     * @return {@link java.lang.String} path to output image
     */
    private String preprocessImage(final File inputImage,
            final int pageNumber) throws OCRException {
        String path = inputImage.getAbsolutePath();
        if (isPreprocessingImages()) {
            path = ImageUtil.preprocessImage(inputImage, pageNumber,
                    isCustomDictionaryUsed());
        }
        return path;
    }
}
