/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.utils.MessageFormatUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.tesseract4.actions.events.PdfOcrTesseract4ProductEvent;
import com.itextpdf.pdfocr.tesseract4.exceptions.Tesseract4OcrException;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;

import net.sourceforge.lept4j.Pix;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link AbstractTesseract4OcrEngine} for tesseract OCR.
 *
 * This class provides possibilities to use features of "tesseract" CL tool
 * (optical character recognition engine for various operating systems).
 * Please note that it's assumed that "tesseract" has already been
 * installed locally.
 */
public class Tesseract4ExecutableOcrEngine extends AbstractTesseract4OcrEngine {

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
     * Please note that list of output files is accepted instead of a single file because
     * page number parameter is not respected in case of TIFF images not requiring preprocessing.
     * In other words, if the passed image is the TIFF image and according to the {@link Tesseract4OcrEngineProperties}
     * no preprocessing is needed, each page of the TIFF image is OCRed and the number of output files in the list
     * is expected to be same as number of pages in the image, otherwise, only one file is expected
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFiles {@link java.util.List} of output files
     *                                          (one per each page)
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @param pageNumber number of page to be processed
     * @param dispatchEvent indicates if event needs to be dispatched
     * @param eventHelper event helper
     */
    void doTesseractOcr(final File inputImage,
            final List<File> outputFiles, final OutputFormat outputFormat,
            final int pageNumber, final boolean dispatchEvent,
            AbstractPdfOcrEventHelper eventHelper) {
        List<String> params = new ArrayList<String>();
        String execPath = null;
        String imagePath = null;
        String workingDirectory = null;
        PdfOcrTesseract4ProductEvent event = null;
        if (eventHelper == null) {
            eventHelper = new Tesseract4EventHelper();
        }
        if (dispatchEvent) {
            event = onEvent(eventHelper);
        }
        try {
            imagePath = inputImage.getAbsolutePath();
            // path to tesseract executable
            if (getPathToExecutable() == null
                    || getPathToExecutable().isEmpty()) {
                throw new Tesseract4OcrException(
                        Tesseract4OcrException
                                .CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE);
            } else {
                if (isWindows()) {
                    execPath = addQuotes(getPathToExecutable());
                } else {
                    execPath = getPathToExecutable();
                }
            }
            checkTesseractInstalled(execPath);
            // path to tess data
            addTessData(params);

            // validate languages before preprocessing started
            validateLanguages(getTesseract4OcrEngineProperties()
                    .getLanguages());

            // preprocess input file if needed
            imagePath = preprocessImage(inputImage, pageNumber);

            // get the input file parent directory as working directory
            // as tesseract cannot parse non ascii characters in input path
            String imageParentDir = TesseractOcrUtil.getParentDirectoryFile(imagePath);
            String replacement = isWindows() ? "" : "/";
            workingDirectory = imageParentDir.replace("file:///", replacement)
                    .replace("file:/", replacement);

            // input file
            addInputFile(params, imagePath);
            // output file
            addOutputFile(params, outputFiles.get(0), outputFormat,
                    imagePath);
            // page segmentation mode
            addPageSegMode(params);
            // add user words if needed
            addUserWords(params, imagePath);
            // required languages
            addLanguages(params);

            addOutputFormat(params, outputFormat);

            addPreserveInterwordSpaces(params);

            // set default user defined dpi
            addDefaultDpi(params);

            // run tesseract process
            TesseractHelper.runCommand(execPath, params, workingDirectory);

            // statistics event
            onEventStatistics(eventHelper);

            // confrim on_demand event
            if (event != null && event.getConfirmationType() == EventConfirmationType.ON_DEMAND) {
                EventManager.getInstance().onEvent(new ConfirmEvent(event));
            }
        } catch (Tesseract4OcrException e) {
            LoggerFactory.getLogger(getClass())
                    .error(e.getMessage());
            throw new Tesseract4OcrException(e.getMessage(), e);
        } finally {
            try {
                if (imagePath != null
                        && !inputImage.getAbsolutePath().equals(imagePath)) {
                    TesseractHelper.deleteFile(imagePath);
                }
            } catch (SecurityException e) {
                LoggerFactory.getLogger(getClass())
                        .error(MessageFormatUtil.format(
                                Tesseract4LogMessageConstant.CANNOT_DELETE_FILE,
                                imagePath, e.getMessage()));
            }
            try {
                if (getTesseract4OcrEngineProperties()
                        .getPathToUserWordsFile() != null
                        && getTesseract4OcrEngineProperties().isUserWordsFileTemporary()) {
                    TesseractHelper.deleteFile(
                            getTesseract4OcrEngineProperties()
                                    .getPathToUserWordsFile());
                }
            } catch (SecurityException e) {
                LoggerFactory.getLogger(getClass())
                        .error(MessageFormatUtil.format(
                                Tesseract4LogMessageConstant.CANNOT_DELETE_FILE,
                                getTesseract4OcrEngineProperties()
                                        .getPathToUserWordsFile(),
                                e.getMessage()));
            }
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
     * Sets preserve_interword_spaces option.
     *
     * @param command result command as list of strings
     */
    private void addPreserveInterwordSpaces(final List<String> command) {
        if (getTesseract4OcrEngineProperties().isUseTxtToImproveHocrParsing()) {
            command.add("-c");
            command.add("preserve_interword_spaces=1");
        }
    }

    /**
     * Add output format.
     *
     * @param command result command as list of strings
     * @param outputFormat output format
     */
    private void addOutputFormat(final List<String> command, OutputFormat outputFormat) {
        if (outputFormat == OutputFormat.HOCR) {
            setHocrOutput(command);
        }
    }

    /**
     * Add path to user-words file for tesseract executable.
     *
     * @param command result command as list of strings
     */
    private void addUserWords(final List<String> command,
            final String imgPath) {
        if (getTesseract4OcrEngineProperties().getPathToUserWordsFile() != null
                && !getTesseract4OcrEngineProperties()
                .getPathToUserWordsFile().isEmpty()) {
            File userWordsFile = new File(getTesseract4OcrEngineProperties()
                    .getPathToUserWordsFile());
            // Workaround for a non-ASCII characters in path
            // Currently works only if the user words (or output files) reside in the same directory as the input image
            // Leaves only a filename in this case, otherwise - absolute path to output file
            String filePath = areEqualParentDirectories(imgPath,
                    userWordsFile.getAbsolutePath())
                    ? userWordsFile.getName()
                    : userWordsFile.getAbsolutePath();

            command.add("--user-words");
            command.add(addQuotes(filePath));
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
        command.add("--tessdata-dir");
        command.add(addQuotes(getTessData()));
    }

    /**
     * Adds selected Page Segmentation Mode as parameter.
     *
     * @param command result command as list of strings
     */
    private void addPageSegMode(final List<String> command) {
        if (getTesseract4OcrEngineProperties().getPageSegMode() != null) {
            command.add("-c");
            command.add("tessedit_pageseg_mode=" + getTesseract4OcrEngineProperties().getPageSegMode());
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
        command.add(addQuotes(new File(imagePath).getName()));
    }

    /**
     * Adds path to temporary output file with result.
     *
     * @param command result command as list of strings
     * @param outputFile output file with result
     * @param outputFormat selected {@link OutputFormat} for tesseract
     */
    private void addOutputFile(final List<String> command,
            final File outputFile, final OutputFormat outputFormat,
            final String inputImagePath) {
        String extension = outputFormat.equals(OutputFormat.HOCR)
                ? ".hocr" : ".txt";
        try {
            // Workaround for a non-ASCII characters in path
            // Currently works only if the user words (or output files) reside in the same directory as the input image
            // Leaves only a filename in this case, otherwise - absolute path to output file
            String filePath = areEqualParentDirectories(inputImagePath,
                    outputFile.getAbsolutePath())
                    ? outputFile.getName()
                    : outputFile.getAbsolutePath();
            String fileName = new String(
                    filePath.toCharArray(), 0,
                    filePath.indexOf(extension));
            LoggerFactory.getLogger(getClass()).info(
                    MessageFormatUtil.format(
                            Tesseract4LogMessageConstant.CREATED_TEMPORARY_FILE,
                            outputFile.getAbsolutePath()));
            command.add(addQuotes(fileName));
        } catch (Exception e) { // NOSONAR
            throw new Tesseract4OcrException(Tesseract4OcrException
                    .TESSERACT_FAILED);
        }
    }

    /**
     * Surrounds given string with quotes.
     *
     * @param value string to be wrapped into quotes
     * @return wrapped string
     */
    private String addQuotes(final String value) {
        // choosing correct quotes for system
        if (isWindows()) {
            return "\"" + value + "\"";
        } else {
            return "'" + value + "'";
        }
    }

    /**
     * Preprocess given image if it is needed.
     *
     * @param inputImage original input image {@link java.io.File}
     * @param pageNumber number of page to be OCRed
     * @return path to output image as {@link java.lang.String}
     * @throws Tesseract4OcrException if preprocessing cannot be done or file
     * is invalid
     */
    private String preprocessImage(final File inputImage,
            final int pageNumber) throws Tesseract4OcrException {
        String tmpFileName = TesseractOcrUtil
                .getTempFilePath(UUID.randomUUID().toString(),
                        getExtension(inputImage));
        String path = inputImage.getAbsolutePath();
        try {
            if (getTesseract4OcrEngineProperties().isPreprocessingImages()) {
                Pix pix = ImagePreprocessingUtil
                        .preprocessImage(inputImage, pageNumber,
                                getTesseract4OcrEngineProperties().getImagePreprocessingOptions());
                TesseractOcrUtil.savePixToPngFile(tmpFileName, pix);
                if (!Files.exists(Paths.get(tmpFileName))) {
                    BufferedImage img = TesseractOcrUtil.convertPixToImage(pix);
                    if (img != null) {
                        TesseractOcrUtil.saveImageToTempPngFile(tmpFileName,
                                img);
                    }
                }
            }
            if (!getTesseract4OcrEngineProperties().isPreprocessingImages()
                    || !Files.exists(Paths.get(tmpFileName))) {
                TesseractOcrUtil.createTempFileCopy(path, tmpFileName);
            }
            if (Files.exists(Paths.get(tmpFileName))) {
                path = tmpFileName;
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil.format(
                            Tesseract4LogMessageConstant
                                    .CANNOT_READ_INPUT_IMAGE,
                            e.getMessage()));
        }
        return path;
    }

    /**
     * Check whether tesseract executable is installed on the machine and
     * provided path to tesseract executable is correct.
     * @param execPath path to tesseract executable
     * @throws Tesseract4OcrException if tesseract is not installed or
     * provided path to tesseract executable is incorrect,
     * i.e. running "{@link #getPathToExecutable()} --version" command failed.
     */
    private void checkTesseractInstalled(String execPath)
            throws Tesseract4OcrException {
        try {
            TesseractHelper.runCommand(execPath,
                    Collections.<String>singletonList("--version"));
        } catch (Tesseract4OcrException e) {
            throw new Tesseract4OcrException(
                    Tesseract4OcrException.TESSERACT_NOT_FOUND, e);
        }
    }

    /**
     * Gets input image file extension.
     *
     * @param inputImage input  file
     * @return file extension as a {@link java.lang.String}
     */
    private String getExtension(File inputImage) {
        if (inputImage != null) {
            int index = inputImage.getAbsolutePath().lastIndexOf('.');
            if (index > 0) {
                String extension = new String(
                        inputImage.getAbsolutePath().toCharArray(), index,
                        inputImage.getAbsolutePath().length() - index);
                return extension.toLowerCase();
            }
        }
        return ".png";
    }

    /**
     * Checks whether parent directories are equal for the passed file paths.
     *
     * @param firstPath path to the first file
     * @param secondPath path to the second file
     * @return true if parent directories are equal, otherwise - false
     */
    private boolean areEqualParentDirectories(final String firstPath,
            final String secondPath) {
        String firstParentDir = TesseractOcrUtil.getParentDirectoryFile(firstPath);
        String secondParentDir = TesseractOcrUtil
                .getParentDirectoryFile(secondPath);
        return firstParentDir != null
                && firstParentDir.equals(secondParentDir);
    }
}
