package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.util.MessageFormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link Tesseract4OcrEngine} for tesseract OCR.
 *
 * This class provides possibilities to use features of "tesseract" CL tool
 * (optical character recognition engine for various operating systems).
 * Please note that it's assumed that "tesseract" has already been
 * installed locally.
 */
public class Tesseract4ExecutableOcrEngine extends Tesseract4OcrEngine {

    /**
     * Path to the tesseract executable.
     * By default it's assumed that "tesseract" already exists in the "PATH".
     */
    private String pathToExecutable;

    /**
     * Creates a new {@link Tesseract4ExecutableOcrEngine} instance.
     *
     * @param tesseract4OcrEngineProperties set of properties
     */
    public Tesseract4ExecutableOcrEngine(
            final Tesseract4OcrEngineProperties tesseract4OcrEngineProperties) {
        super(tesseract4OcrEngineProperties);
        setPathToExecutable("tesseract");
    }

    /**
     * Creates a new {@link Tesseract4ExecutableOcrEngine} instance.
     *
     * @param executablePath path to tesseract executable
     * @param tesseract4OcrEngineProperties set of properties
     */
    public Tesseract4ExecutableOcrEngine(final String executablePath,
            final Tesseract4OcrEngineProperties tesseract4OcrEngineProperties) {
        super(tesseract4OcrEngineProperties);
        setPathToExecutable(executablePath);
    }

    /**
     * Gets path to tesseract executable.
     *
     * @return path to tesseract executable
     */
    public final String getPathToExecutable() {
        return pathToExecutable;
    }

    /**
     * Sets path to tesseract executable.
     * By default it's assumed that "tesseract" already exists in the "PATH".
     *
     * @param path path to tesseract executable
     */
    public final void setPathToExecutable(final String path) {
        pathToExecutable = path;
    }

    /**
     * Performs tesseract OCR using command line tool for the selected page
     * of input image (by default 1st).
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFiles {@link java.util.List} of output files
     *                                          (one per each page)
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @param pageNumber number of page to be processed
     */
    public void doTesseractOcr(final File inputImage,
            final List<File> outputFiles, final OutputFormat outputFormat,
            final int pageNumber) {
        List<String> command = new ArrayList<String>();
        String imagePath = null;
        try {
            imagePath = inputImage.getAbsolutePath();
            // path to tesseract executable
            addPathToExecutable(command);
            // path to tess data
            addTessData(command);

            // validate languages before preprocessing started
            validateLanguages(getTesseract4OcrEngineProperties().getLanguages());

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
            // set default user defined dpi
            addDefaultDpi(command);

            TesseractOcrUtil.runCommand(command, isWindows());
        } catch (Tesseract4OcrException e) {
            LoggerFactory.getLogger(getClass())
                    .error(e.getMessage());
            throw new Tesseract4OcrException(e.getMessage(), e);
        } finally {
            try {
                if (imagePath != null
                        && getTesseract4OcrEngineProperties().isPreprocessingImages()
                        && !inputImage.getAbsolutePath().equals(imagePath)) {
                    TesseractHelper.deleteFile(imagePath);
                }
            } catch (SecurityException e) {
                LoggerFactory.getLogger(getClass())
                        .error(MessageFormatUtil.format(
                                Tesseract4LogMessageConstant.CannotDeleteFile,
                                imagePath, e.getMessage()));
            }
            try {
                if (getTesseract4OcrEngineProperties()
                        .getPathToUserWordsFile() != null) {
                    TesseractHelper.deleteFile(
                            getTesseract4OcrEngineProperties()
                                    .getPathToUserWordsFile());
                }
            } catch (SecurityException e) {
                LoggerFactory.getLogger(getClass())
                        .error(MessageFormatUtil.format(
                                Tesseract4LogMessageConstant.CannotDeleteFile,
                                getTesseract4OcrEngineProperties()
                                        .getPathToUserWordsFile(),
                                e.getMessage()));
            }
        }
    }

    /**
     * Adds path to tesseract executable to the command.
     *
     * @param command result command as list of strings
     * @throws Tesseract4OcrException if path to tesseract executable wasn't found
     */
    private void addPathToExecutable(final List<String> command)
            throws Tesseract4OcrException {
        // path to tesseract executable cannot be uninitialized
        if (getPathToExecutable() == null
                || getPathToExecutable().isEmpty()) {
            throw new Tesseract4OcrException(
                    Tesseract4OcrException.CannotFindPathToTesseractExecutable);
        } else {
            command.add(addQuotes(getPathToExecutable()));
        }
    }

    /**
     * Sets hocr output format.
     *
     * @param command result command as list of strings
     */
    private void setHocrOutput(final List<String> command) {
        command.add("-c");
        command.add("tessedit_create_hocr=1");
    }

    /**
     * Add path to user-words file for tesseract executable.
     *
     * @param command result command as list of strings
     */
    private void addUserWords(final List<String> command) {
        if (getTesseract4OcrEngineProperties().getPathToUserWordsFile() != null
                && !getTesseract4OcrEngineProperties()
                .getPathToUserWordsFile().isEmpty()) {
            command.add("--user-words");
            command.add(addQuotes(getTesseract4OcrEngineProperties()
                    .getPathToUserWordsFile()));
            command.add("--oem");
            command.add("0");
        }
    }

    /**
     * Set default DPI for image.
     *
     * @param command result command as list of strings
     */
    private void addDefaultDpi(final List<String> command) {
        command.add("-c");
        command.add("user_defined_dpi=300");
    }

    /**
     * Adds path to tess data to the command list.
     *
     * @param command result command as list of strings
     */
    private void addTessData(final List<String> command) {
        if (getTesseract4OcrEngineProperties().getPathToTessData() != null
                && !getTesseract4OcrEngineProperties().getPathToTessData().isEmpty()) {
            command.add("--tessdata-dir");
            command.add(addQuotes(getTessData()));
        }
    }

    /**
     * Adds selected Page Segmentation Mode as parameter.
     *
     * @param command result command as list of strings
     */
    private void addPageSegMode(final List<String> command) {
        if (getTesseract4OcrEngineProperties().getPageSegMode() != null) {
            command.add("--psm");
            command.add(String.valueOf(getTesseract4OcrEngineProperties().getPageSegMode()));
        }
    }

    /**
     * Add list of selected languages concatenated to a string as parameter.
     *
     * @param command result command as list of strings
     */
    private void addLanguages(final List<String> command) {
        if (getTesseract4OcrEngineProperties().getLanguages().size() > 0) {
            command.add("-l");
            command.add(getLanguagesAsString());
        }
    }

    /**
     * Adds path to the input image file.
     *
     * @param command result command as list of strings
     * @param imagePath path to the input image file as string
     */
    private void addInputFile(final List<String> command,
            final String imagePath) {
        command.add(addQuotes(imagePath));
    }

    /**
     * Adds path to temporary output file with result.
     *
     * @param command result command as list of strings
     * @param outputFile output file with result
     * @param outputFormat selected {@link OutputFormat} for tesseract
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
                        Tesseract4LogMessageConstant.CreatedTemporaryFile,
                        outputFile.getAbsolutePath()));
        command.add(addQuotes(fileName));
    }

    /**
     * Surrounds given string with quotes.
     *
     * @param value string to be wrapped into quotes
     * @return wrapped string
     */
    private String addQuotes(final String value) {
        return "\"" + value + "\"";
    }

    /**
     * Preprocess given image if it is needed.
     *
     * @param inputImage original input image {@link java.io.File}
     * @param pageNumber number of page to be OCRed
     * @return path to output image as {@link java.lang.String}
     * @throws Tesseract4OcrException if preprocessing caanot be done or file is invalid
     */
    private String preprocessImage(final File inputImage,
            final int pageNumber) throws Tesseract4OcrException {
        String path = inputImage.getAbsolutePath();
        if (getTesseract4OcrEngineProperties().isPreprocessingImages()) {
            path = ImagePreprocessingUtil.preprocessImage(inputImage, pageNumber);
        }
        return path;
    }
}
