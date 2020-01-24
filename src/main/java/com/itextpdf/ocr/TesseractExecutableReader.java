package com.itextpdf.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

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
     * TesseractExecutableReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractExecutableReader.class);

    /**
     * Path to hocr config script.
     */
    private static final String PATH_TO_HOCR_SCRIPT = "src/main/resources/"
            + "com/itextpdf/ocr/configs/hocr";

    /**
     * Path to the tesseract executable.
     * By default it's assumed that "tesseract" already exists in the PATH
     */
    private String pathToExecutable;

    /**
     * TesseractReader constructor.
     */
    public TesseractExecutableReader() {
        setPathToExecutable("tesseract");
        setOsType(identifyOSType());
    }

    /**
     * TesseractReader constructor with path to executable.
     *
     * @param path String
     */
    public TesseractExecutableReader(final String path) {
        setPathToExecutable(path);
        setOsType(identifyOSType());
    }

    /**
     * TesseractReader constructor with path to executable,
     * list of languages and path to tessData directory.
     *
     * @param path          String
     * @param languagesList List<String>
     * @param tessData      String
     */
    public TesseractExecutableReader(final String path,
                                     final List<String> languagesList,
                                     final String tessData) {
        setPathToExecutable(path);
        setLanguages(Collections.unmodifiableList(languagesList));
        setPathToTessData(tessData);
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
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFile - output file
     */
    public void doTesseractOcr(final File inputImage,
            final File outputFile) {

        String path = preprocessImage(inputImage.getAbsolutePath());

        // path to tesseract executable cannot be uninitialized
        if (pathToExecutable == null || pathToExecutable.isEmpty()) {
            throw new OCRException(
                    OCRException.CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
        }
        List<String> command = new ArrayList<>();

        command.add(addQuotes(pathToExecutable));

        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            command.addAll(Arrays.asList(
                    "--tessdata-dir", addQuotes(getTessData())
            ));
        }

        command.add(addQuotes(path));
        command.add(addQuotes(getOutputFile(outputFile)));

        if (getPageSegMode() != null) {
            command.addAll(
                    Arrays.asList("--psm", getPageSegMode().toString()));
        }

        if (!getLanguages().isEmpty()) {
            command.addAll(
                    Arrays.asList("-l",
                            String.join("+", getLanguages()))
            );
        }

        command.add(PATH_TO_HOCR_SCRIPT);
        command.add(PATH_TO_QUIET_SCRIPT);

        UtilService.runCommand(command, isWindows());
    }

    /**
     * Get name of the output temp file without extension.
     *
     * @param outputFile File
     * @return String
     */
    private String getOutputFile(final File outputFile) {
        String extension = ".hocr";
        String fileName = outputFile.getAbsolutePath()
                .substring(0, outputFile.getAbsolutePath().indexOf(extension));
        LOGGER.info("Temp path: " + outputFile.toString());
        return fileName;
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
                String extension = FilenameUtils.getExtension(path);
                BufferedImage original = ImageUtil.preprocessImage(path);
                File outputFile = File.createTempFile("output",
                        "." + extension);
                String output = outputFile.getAbsolutePath();
                ImageIO.write(original, extension, outputFile);
                path = output;
                original.flush();
            } catch (IOException e) {
                LOGGER.error("Error while preprocessing image: "
                        + e.getLocalizedMessage());
            }
        }
        return path;
    }
}
