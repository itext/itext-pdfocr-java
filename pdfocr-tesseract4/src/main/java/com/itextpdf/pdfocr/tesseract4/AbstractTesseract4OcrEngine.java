/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link IOcrEngine}.
 *
 * This class provides possibilities to perform OCR, to read data from input
 * files and to return contained text in the required format.
 * Also there are possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems).
 */
public abstract class AbstractTesseract4OcrEngine implements IOcrEngine {

    /**
     * Supported image formats.
     */
    private static final Set<String> SUPPORTED_IMAGE_FORMATS =
            Collections.unmodifiableSet(new HashSet<>(
                    Arrays.<String>asList("bmp", "png", "tiff", "tif", "jpeg",
                            "jpg", "jpe", "jfif")));

    /**
     * Set of properties.
     */
    private Tesseract4OcrEngineProperties tesseract4OcrEngineProperties;

    public AbstractTesseract4OcrEngine(
            Tesseract4OcrEngineProperties tesseract4OcrEngineProperties) {
        this.tesseract4OcrEngineProperties = tesseract4OcrEngineProperties;
    }

    /**
     * Performs tesseract OCR for the first (or for the only) image page.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFile output file for the result for the first page
     * @param outputFormat selected {@link OutputFormat} for tesseract
     */
    public void doTesseractOcr(File inputImage, File outputFile,
            OutputFormat outputFormat) {
        doTesseractOcr(inputImage, Collections.<File>singletonList(outputFile),
                outputFormat, 1);
    }

    /**
     * Performs OCR using provided {@link IOcrEngine} for the given list of
     * input images and saves output to a text file using provided path.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param txtFile file to be created
     */
    public void createTxtFile(final List<File> inputImages, final File txtFile) {
        LoggerFactory.getLogger(getClass())
                .info(MessageFormatUtil.format(
                Tesseract4LogMessageConstant.START_OCR_FOR_IMAGES,
                inputImages.size()));

        StringBuilder content = new StringBuilder();
        for (File inputImage : inputImages) {
            content.append(doImageOcr(inputImage, OutputFormat.TXT));
        }

        // write to file
        TesseractHelper.writeToTextFile(txtFile.getAbsolutePath(),
                content.toString());
    }

    /**
     * Gets properties for {@link AbstractTesseract4OcrEngine}.
     *
     * @return set properties {@link Tesseract4OcrEngineProperties}
     */
    public final Tesseract4OcrEngineProperties getTesseract4OcrEngineProperties() {
        return tesseract4OcrEngineProperties;
    }

    /**
     * Sets properties for {@link AbstractTesseract4OcrEngine}.
     *
     * @param tesseract4OcrEngineProperties set of properties
     * {@link Tesseract4OcrEngineProperties} for {@link AbstractTesseract4OcrEngine}
     */
    public final void setTesseract4OcrEngineProperties(
            final Tesseract4OcrEngineProperties tesseract4OcrEngineProperties) {
        this.tesseract4OcrEngineProperties = tesseract4OcrEngineProperties;
    }

    /**
     * Gets list of languages concatenated with "+" symbol to a string
     * in format required by tesseract.
     * @return {@link java.lang.String} of concatenated languages
     */
    public final String getLanguagesAsString() {
        if (getTesseract4OcrEngineProperties().getLanguages().size() > 0) {
            return String.join("+",
                    getTesseract4OcrEngineProperties().getLanguages());
        } else {
            return getTesseract4OcrEngineProperties().getDefaultLanguage();
        }
    }

    /**
     * Reads data from the provided input image file and returns retrieved
     * data in the format described below.
     *
     * @param input input image {@link java.io.File}
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    public final Map<Integer, List<TextInfo>> doImageOcr(
            final File input) {
        Map<Integer, List<TextInfo>> result = new LinkedHashMap<Integer,
                List<TextInfo>>();
        verifyImageFormatValidity(input);
        Map<String, Map<Integer, List<TextInfo>>> processedData =
                processInputFiles(input, OutputFormat.HOCR);
        if (processedData != null && processedData.size() > 0) {
            List<String> keys = new ArrayList<String>(
                    processedData.keySet());
            result = processedData.get(keys.get(0));
        }
        return result;
    }

    /**
     * Reads data from the provided input image file and returns retrieved
     * data as string.
     *
     * @param input input image {@link java.io.File}
     *
     * @param outputFormat return {@link OutputFormat} result
     * @return OCR result as a {@link java.lang.String} that is
     * returned after processing the given image
     */
    public final String doImageOcr(final File input,
            final OutputFormat outputFormat) {
        String result = "";
        verifyImageFormatValidity(input);
        Map<String, Map<Integer, List<TextInfo>>> processedData =
                processInputFiles(input, outputFormat);
        if (processedData != null && processedData.size() > 0) {
            List<String> keys = new ArrayList<String>(
                    processedData.keySet());
            if (outputFormat.equals(OutputFormat.TXT)) {
                result = keys.get(0);
            } else {
                StringBuilder outputText = new StringBuilder();
                Map<Integer, List<TextInfo>> outputMap =
                        processedData.get(keys.get(0));
                for (int page : outputMap.keySet()) {
                    StringBuilder pageText = new StringBuilder();
                    for (TextInfo textInfo : outputMap.get(page)) {
                        pageText.append(textInfo.getText());
                        pageText.append(System.lineSeparator());
                    }
                    outputText.append(pageText);
                    outputText.append(System.lineSeparator());
                }
                result = outputText.toString();
            }
        }
        return result;
    }

    /**
     * Checks current os type.
     *
     * @return boolean true is current os is windows, otherwise - false
     */
    public boolean isWindows() {
        return identifyOsType().toLowerCase().contains("win");
    }

    /**
     * Identifies type of current OS and return it (win, linux).
     *
     * @return type of current os as {@link java.lang.String}
     */
    public String identifyOsType() {
        String os = System.getProperty("os.name") == null
                ? System.getProperty("OS") : System.getProperty("os.name");
        return os.toLowerCase();
    }

    /**
     * Validates list of provided languages and
     * checks if they all exist in given tess data directory.
     *
     * @param languagesList {@link java.util.List} of provided languages
     * @throws Tesseract4OcrException if tess data wasn't found for one of the
     * languages from the provided list
     */
    public void validateLanguages(final List<String> languagesList)
            throws Tesseract4OcrException {
        String suffix = ".traineddata";
        if (languagesList.size() == 0) {
            if (!new File(getTessData()
                    + java.io.File.separatorChar
                    + getTesseract4OcrEngineProperties().getDefaultLanguage()
                    + suffix)
                    .exists()) {
                throw new Tesseract4OcrException(
                        Tesseract4OcrException.INCORRECT_LANGUAGE)
                        .setMessageParams(
                                getTesseract4OcrEngineProperties()
                                        .getDefaultLanguage()
                                        + suffix,
                                getTessData());
            }
        } else {
            for (String lang : languagesList) {
                if (!new File(getTessData()
                        + java.io.File.separatorChar + lang + suffix)
                        .exists()) {
                    throw new Tesseract4OcrException(
                            Tesseract4OcrException.INCORRECT_LANGUAGE)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }

    /**
     * Performs tesseract OCR using command line tool
     * or a wrapper for Tesseract OCR API.
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
     */
    abstract void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat,
            int pageNumber);

    /**
     * Reads data from the provided input image file.
     *
     * @param input input image {@link java.io.File}
     * @param outputFormat {@link OutputFormat} for the result returned
     *                                         by {@link IOcrEngine}
     * @return Map<String, Map<Integer, List<TextInfo>>>
     *     if output format is txt,
     *     result is key of the returned map(String),
     *     otherwise - the value (Map<Integer, List<{@link TextInfo}>)
     */
    Map<String, Map<Integer, List<TextInfo>>> processInputFiles(
            final File input, final OutputFormat outputFormat) {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();
        StringBuilder data = new StringBuilder();
        List<File> tempFiles = new ArrayList<File>();
        try {
            // image needs to be paginated only if it's tiff
            // or preprocessing isn't required
            int realNumOfPages = !ImagePreprocessingUtil.isTiffImage(input)
                    ? 1 : ImagePreprocessingUtil.getNumberOfPageTiff(input);
            int numOfPages =
                    getTesseract4OcrEngineProperties().isPreprocessingImages()
                    ? realNumOfPages : 1;
            int numOfFiles =
                    getTesseract4OcrEngineProperties().isPreprocessingImages()
                    ? 1 : realNumOfPages;

            for (int page = 1; page <= numOfPages; page++) {
                String extension = outputFormat.equals(OutputFormat.HOCR)
                        ? ".hocr" : ".txt";
                for (int i = 0; i < numOfFiles; i++) {
                    tempFiles.add(createTempFile(extension));
                }

                doTesseractOcr(input, tempFiles, outputFormat, page);
                if (outputFormat.equals(OutputFormat.HOCR)) {
                    Map<Integer, List<TextInfo>> pageData = TesseractHelper
                            .parseHocrFile(tempFiles,
                                    getTesseract4OcrEngineProperties()
                                            .getTextPositioning());

                    if (getTesseract4OcrEngineProperties()
                            .isPreprocessingImages()) {
                        imageData.put(page, pageData.get(1));
                    } else {
                        imageData = pageData;
                    }
                } else {
                    for (File tmpFile : tempFiles) {
                        if (Files.exists(
                                java.nio.file.Paths
                                        .get(tmpFile.getAbsolutePath()))) {
                            data.append(TesseractHelper.readTxtFile(tmpFile));
                        }
                    }
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil.format(
                            Tesseract4LogMessageConstant.CANNOT_OCR_INPUT_FILE,
                            e.getMessage()));
        } finally {
            for (File file : tempFiles) {
                TesseractHelper.deleteFile(file.getAbsolutePath());
            }
        }

        Map<String, Map<Integer, List<TextInfo>>> result =
                new LinkedHashMap<String, Map<Integer, List<TextInfo>>>();
        result.put(data.toString(), imageData);
        return result;
    }

    /**
     * Gets path to provided tess data directory.
     *
     * @return path to provided tess data directory as
     * {@link java.lang.String}
     */
    String getTessData() {
        if (getTesseract4OcrEngineProperties().getPathToTessData() == null) {
            throw new Tesseract4OcrException(Tesseract4OcrException
                    .PATH_TO_TESS_DATA_IS_NOT_SET);
        } else {
            return getTesseract4OcrEngineProperties().getPathToTessData()
                    .getAbsolutePath();
        }
    }

    /**
     * Creates a temporary file with given extension.
     *
     * @param extension file extension for a new file {@link java.lang.String}
     * @return a new created {@link java.io.File} instance
     */
    private File createTempFile(final String extension) {
        String tmpFileName = TesseractOcrUtil.getTempFilePath(
                UUID.randomUUID().toString(), extension);
        return new File(tmpFileName);
    }

    /**
     * Validates input image format.
     * Allowed image formats are listed
     * in {@link AbstractTesseract4OcrEngine#SUPPORTED_IMAGE_FORMATS}
     *
     * @param image input image {@link java.io.File}
     * @throws Tesseract4OcrException if image format is invalid
     */
    private void verifyImageFormatValidity(final File image)
            throws Tesseract4OcrException {
        boolean isValid = false;
        String extension = "incorrect extension";
        int index = image.getAbsolutePath().lastIndexOf('.');
        if (index > 0) {
            extension = new String(image.getAbsolutePath().toCharArray(),
                    index + 1,
                    image.getAbsolutePath().length() - index - 1);
            for (String format : SUPPORTED_IMAGE_FORMATS) {
                if (format.equals(extension.toLowerCase())) {
                    isValid = true;
                    break;
                }
            }
        }
        if (!isValid) {
            LoggerFactory.getLogger(getClass()).error(MessageFormatUtil
                    .format(Tesseract4LogMessageConstant
                                    .CANNOT_READ_INPUT_IMAGE,
                            image.getAbsolutePath()));
            throw new Tesseract4OcrException(
                    Tesseract4OcrException.INCORRECT_INPUT_IMAGE_FORMAT)
                    .setMessageParams(extension);
        }
    }
}
