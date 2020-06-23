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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link AbstractTesseract4OcrEngine} for tesseract OCR.
 *
 * This class provides possibilities to use features of "tesseract"
 * using tess4j.
 *
 * Please note that this class is not thread-safe, in other words this Tesseract engine cannot
 * be used for multithreaded processing. You should create one instance per thread
 */
public class Tesseract4LibOcrEngine extends AbstractTesseract4OcrEngine {

    /**
     * {@link net.sourceforge.tess4j.ITesseract} Instance.
     * (depends on OS type)
     */
    private ITesseract tesseractInstance = null;

    /**
     * Creates a new {@link Tesseract4LibOcrEngine} instance.
     *
     * @param tesseract4OcrEngineProperties set of properteis
     */
    public Tesseract4LibOcrEngine(
            final Tesseract4OcrEngineProperties tesseract4OcrEngineProperties) {
        super(tesseract4OcrEngineProperties);
        tesseractInstance = TesseractOcrUtil
                .initializeTesseractInstance(isWindows(), null,
                        null, null);
    }

    /**
     * Gets tesseract instance.
     *
     * @return initialized {@link net.sourceforge.tess4j.ITesseract} instance
     */
    public ITesseract getTesseractInstance() {
        return tesseractInstance;
    }

    /**
     * Initializes instance of tesseract if it haven't been already
     * initialized or it have been disposed and sets all the required
     * properties.
     *
     * @param outputFormat selected {@link OutputFormat} for tesseract
     */
    public void initializeTesseract(final OutputFormat outputFormat) {
        if (getTesseractInstance() == null
                || TesseractOcrUtil
                .isTesseractInstanceDisposed(getTesseractInstance())) {
            tesseractInstance = TesseractOcrUtil
                    .initializeTesseractInstance(isWindows(), getTessData(),
                            getLanguagesAsString(),
                            getTesseract4OcrEngineProperties()
                                    .getPathToUserWordsFile());
        }
        getTesseractInstance()
                .setTessVariable("tessedit_create_hocr",
                        outputFormat.equals(OutputFormat.HOCR) ? "1" : "0");
        getTesseractInstance().setTessVariable("user_defined_dpi", "300");
        if (getTesseract4OcrEngineProperties()
                .getPathToUserWordsFile() != null) {
            getTesseractInstance()
                    .setTessVariable("load_system_dawg", "0");
            getTesseractInstance()
                    .setTessVariable("load_freq_dawg", "0");
            getTesseractInstance()
                    .setTessVariable("user_words_suffix",
                            getTesseract4OcrEngineProperties()
                                    .getDefaultUserWordsSuffix());
            getTesseractInstance()
                    .setTessVariable("user_words_file",
                            getTesseract4OcrEngineProperties()
                                    .getPathToUserWordsFile());
        }

        TesseractOcrUtil.setTesseractProperties(getTesseractInstance(),
                getTessData(), getLanguagesAsString(),
                getTesseract4OcrEngineProperties().getPageSegMode(),
                getTesseract4OcrEngineProperties().getPathToUserWordsFile());
    }

    /**
     * Performs tesseract OCR using wrapper for Tesseract OCR API for the selected page
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
     */
    void doTesseractOcr(final File inputImage,
            final List<File> outputFiles, final OutputFormat outputFormat,
            final int pageNumber) {
        try {
            validateLanguages(getTesseract4OcrEngineProperties()
                    .getLanguages());
            initializeTesseract(outputFormat);

            // if preprocessing is not needed and provided image is tiff,
            // the image will be paginated and separate pages will be OCRed
            List<String> resultList = new ArrayList<String>();
            if (!getTesseract4OcrEngineProperties().isPreprocessingImages()
                    && ImagePreprocessingUtil.isTiffImage(inputImage)) {
                resultList = getOcrResultForMultiPage(inputImage,
                        outputFormat);
            } else {
                resultList.add(getOcrResultForSinglePage(inputImage,
                        outputFormat, pageNumber));
            }

            // list of result strings is written to separate files
            // (one for each page)
            for (int i = 0; i < resultList.size(); i++) {
                String result = resultList.get(i);
                File outputFile = i >= outputFiles.size()
                        ? null : outputFiles.get(i);
                if (result != null && outputFile != null) {
                    try (Writer writer = new OutputStreamWriter(
                            new FileOutputStream(outputFile.getAbsolutePath()),
                            StandardCharsets.UTF_8)) {
                        writer.write(result);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(getClass()).error(
                                MessageFormatUtil.format(
                                        Tesseract4LogMessageConstant
                                                .CANNOT_WRITE_TO_FILE,
                                        e.getMessage()));
                        throw new Tesseract4OcrException(
                                Tesseract4OcrException.TESSERACT_FAILED);
                    }
                }
            }
        } catch (Tesseract4OcrException e) {
            LoggerFactory.getLogger(getClass())
                    .error(e.getMessage());
            throw new Tesseract4OcrException(e.getMessage(), e);
        } finally {
            if (tesseractInstance != null) {
                TesseractOcrUtil.disposeTesseractInstance(tesseractInstance);
            }
            if (getTesseract4OcrEngineProperties().getPathToUserWordsFile()
                    != null
                    && getTesseract4OcrEngineProperties().isUserWordsFileTemporary()) {
                TesseractHelper.deleteFile(
                        getTesseract4OcrEngineProperties()
                                .getPathToUserWordsFile());
            }
        }
    }

    /**
     * Gets OCR result from provided multi-page image and returns result as
     * list of strings for each page. This method is used for tiff images
     * when preprocessing is not needed.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return list of result string that will be written to a temporary files
     * later
     */
    private List<String> getOcrResultForMultiPage(final File inputImage,
            final OutputFormat outputFormat) {
        List<String> resultList = new ArrayList<String>();
        try {
            initializeTesseract(outputFormat);
            TesseractOcrUtil util = new TesseractOcrUtil();
            util.initializeImagesListFromTiff(inputImage);
            int numOfPages = util.getListOfPages().size();
            for (int i = 0; i < numOfPages; i++) {
                String result = util.getOcrResultAsString(
                        getTesseractInstance(),
                        util.getListOfPages().get(i),
                        outputFormat);
                resultList.add(result);
            }
        } catch (TesseractException e) {
            String msg = MessageFormatUtil
                    .format(Tesseract4LogMessageConstant.TESSERACT_FAILED,
                            e.getMessage());
            LoggerFactory.getLogger(getClass())
                    .error(msg);
            throw new Tesseract4OcrException(
                    Tesseract4OcrException
                            .TESSERACT_FAILED);
        } finally {
            TesseractOcrUtil
                    .disposeTesseractInstance(getTesseractInstance());
        }
        return resultList;
    }

    /**
     * Gets OCR result from provided single page image and preprocesses it if
     * it is needed.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @param pageNumber number of page to be OCRed
     * @return result as string that will be written to a temporary file later
     */
    private String getOcrResultForSinglePage(final File inputImage,
            final OutputFormat outputFormat,
            final int pageNumber) {
        String result = null;
        try {
            // preprocess if required
            if (getTesseract4OcrEngineProperties().isPreprocessingImages()) {
                // preprocess and try to ocr
                result = new TesseractOcrUtil().getOcrResultAsString(
                        getTesseractInstance(),
                        ImagePreprocessingUtil
                                .preprocessImage(inputImage, pageNumber),
                        outputFormat);
            }
            if (result == null) {
                BufferedImage bufferedImage = ImagePreprocessingUtil
                        .readImage(inputImage);
                if (bufferedImage != null) {
                    try {
                        result = new TesseractOcrUtil()
                                .getOcrResultAsString(getTesseractInstance(),
                                        bufferedImage, outputFormat);
                    } catch (Exception e) { // NOSONAR
                        LoggerFactory.getLogger(getClass())
                                .info(MessageFormatUtil.format(
                                        Tesseract4LogMessageConstant
                                                .CANNOT_PROCESS_IMAGE,
                                        e.getMessage()));
                    }
                }
                if (result == null) {
                    // perform ocr using original input image
                    result = new TesseractOcrUtil()
                            .getOcrResultAsString(getTesseractInstance(),
                                    inputImage, outputFormat);
                }
            }
        } catch (Exception e) { // NOSONAR
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil
                            .format(Tesseract4LogMessageConstant
                                            .TESSERACT_FAILED,
                                    e.getMessage()));
            throw new Tesseract4OcrException(
                    Tesseract4OcrException
                            .TESSERACT_FAILED);
        }

        return result;
    }
}
