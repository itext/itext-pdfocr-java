package com.itextpdf.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
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
     * Path to hocr config script.
     */
    private static final String DEFAULT_PATH_TO_HOCR_SCRIPT = "src/main/resources/"
            + "com/itextpdf/ocr/configs/hocr";

    /**
     * Path to the hocr script.
     * There will be used default path to hocr script if this is not set.
     */
    private String pathToHocrScript;

    /**
     * Path to the tesseract executable.
     * By default it's assumed that "tesseract" already exists in the PATH
     */
    private String pathToExecutable;

    /**
     * TesseractExecutableReader constructor with path to tess data directory.
     */
    public TesseractExecutableReader(String tessDataPath) {
        setPathToExecutable("tesseract");
        setOsType(identifyOSType());
        setPathToTessData(tessDataPath);
    }

    /**
     * TesseractExecutableReader constructor with path to executable and
     * path to tess data directory.
     *
     * @param executablePath path to tesseract executable
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
        setLanguages(Collections.unmodifiableList(languagesList));
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
     * Set path to hocr script.
     * There will be used default path to hocr script if this is not set.
     *
     * @param pathToHocrScript String
     */
    public final void setPathToHocrScript(String pathToHocrScript) {
        this.pathToHocrScript = pathToHocrScript;
    }

    /**
     * Get path to hocr script.
     *
     * @return String
     */
    public final String getPathToHocrScript() {
        return pathToHocrScript;
    }

    /**
     * Get path to default hocr script.
     *
     * @return String
     */
    public final String getDefaultPathToHocrScript() {
        return DEFAULT_PATH_TO_HOCR_SCRIPT;
    }

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFile - output file
     */
    public void doTesseractOcr(final File inputImage,
            final File outputFile) {
        List<String> command = new ArrayList<>();

        // path to tesseract executable
        addPathToExecutable(command);
        // path to tess data
        addTessData(command);
        // preprocess input file and add it
        addInputFile(command, inputImage);
        // output file
        addOutputFile(command, outputFile);
        // page segmentation mode
        addPageSegMode(command);
        // required languages
        addLanguages(command);
        // path to hocr script
        addPathToHocrScript(command);

        UtilService.runCommand(command, isWindows());
    }

    /**
     * Add path to tesseract executable.
     *
     * @param command List<String>
     * @throws OCRException if path to executable is not set
     */
    private void addPathToExecutable(List<String> command) throws OCRException {
        // path to tesseract executable cannot be uninitialized
        if (getPathToExecutable() == null || getPathToExecutable().isEmpty()) {
            throw new OCRException(
                    OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        } else {
            command.add(addQuotes(getPathToExecutable()));
        }
    }

    /**
     * Add path to hocr script for tesseract executable.
     *
     * @param command List<String>
     */
    private void addPathToHocrScript(List<String> command) {
        if (getPathToHocrScript() != null && !getPathToHocrScript().isEmpty()) {
            command.add(getPathToHocrScript());
        } else {
            command.add(getDefaultPathToHocrScript());
        }
    }

    /**
     * Add path to tess data.
     *
     * @param command List<String>
     */
    private void addTessData(List<String> command) {
        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            command.addAll(Arrays.asList("--tessdata-dir",
                    addQuotes(getTessData())));
        }
    }

    /**
     * Add select Page Segmentation Mode as parameter.
     *
     * @param command List<String>
     */
    private void addPageSegMode(List<String> command) {
        if (getPageSegMode() != null) {
            command.addAll(Arrays.asList("--psm", getPageSegMode().toString()));
        }
    }

    /**
     * Add list pf selected languages as parameter.
     *
     * @param command List<String>
     */
    private void addLanguages(List<String> command) {
        if (!getLanguages().isEmpty()) {
            validateLanguages();
            command.addAll(Arrays.asList("-l",
                    String.join("+", getLanguages())));
        }
    }

    /**
     * Preprocess input image (if needed) and add path to this file
     *
     * @param command List<String>
     * @param image   input file
     */
    private void addInputFile(List<String> command, final File image) {
        String path = preprocessImage(image.getAbsolutePath());
        command.add(addQuotes(path));
    }

    /**
     * Add path to temporary output file.
     *
     * @param command    List<String>
     * @param outputFile output file
     */
    private void addOutputFile(List<String> command, final File outputFile) {
        String extension = ".hocr";
        String fileName = outputFile.getAbsolutePath()
                .substring(0, outputFile.getAbsolutePath().indexOf(extension));
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
     * @param path path to original input image
     * @return path to output image
     */
    private String preprocessImage(String path) {
        if (isPreprocessingImages()) {
            try {
                String extension = ImageUtil.getExtension(path);
                BufferedImage original = null;
                BufferedImage preprocessed = null;
                try {
                    original = ImageIO.read(new File(path));
                    if (original != null) {
                        preprocessed = ImageUtil.preprocessImage(path, original);
                    } else {
                        preprocessed = ImageUtil.preprocessImage(path);
                    }
                } catch (IOException e) {
                    LOGGER.info("Cannot read from file: " + e.getLocalizedMessage());
                    preprocessed = ImageUtil.preprocessImage(path);
                }
                if (preprocessed != null) {
                    File outputFile = File.createTempFile("output",
                            "." + extension);
                    String output = outputFile.getAbsolutePath();
                    ImageIO.write(preprocessed, extension, outputFile);
                    path = output;
                    preprocessed.flush();
                    if (original != null) {
                        original.flush();
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error while preprocessing image: "
                        + e.getLocalizedMessage());
            }
        }
        return path;
    }
}
