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

import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.image.ImageType;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.IProductAware;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.PdfOcrMetaInfoContainer;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.tesseract4.actions.data.PdfOcrTesseract4ProductData;
import com.itextpdf.pdfocr.tesseract4.actions.events.PdfOcrTesseract4ProductEvent;
import com.itextpdf.pdfocr.tesseract4.exceptions.Tesseract4OcrException;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;

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
public abstract class AbstractTesseract4OcrEngine implements IOcrEngine, IProductAware {

    /**
     * Supported image formats.
     */
    private static final Set<ImageType> SUPPORTED_IMAGE_FORMATS =
            Collections.unmodifiableSet(new HashSet<>(
                    Arrays.<ImageType>asList(ImageType.BMP, ImageType.PNG,
                            ImageType.TIFF, ImageType.JPEG)));

    Set<UUID> processedUUID = new HashSet<>();

    /**
     * Set of properties.
     */
    private Tesseract4OcrEngineProperties tesseract4OcrEngineProperties;

    private ThreadLocal<IMetaInfo> threadLocalMetaInfo = new ThreadLocal<>();

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
        doTesseractOcr(inputImage, outputFile, outputFormat, new OcrProcessContext(new Tesseract4EventHelper()));
    }

    /**
     * Performs tesseract OCR for the first (or for the only) image page.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFile output file for the result for the first page
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @param ocrProcessContext ocr process context
     */
    public void doTesseractOcr(File inputImage, File outputFile,
            OutputFormat outputFormat,
            OcrProcessContext ocrProcessContext) {
        doTesseractOcr(inputImage, Collections.<File>singletonList(outputFile),
                outputFormat, 1, ocrProcessContext.getOcrEventHelper());
    }

    /**
     * Performs OCR using provided {@link IOcrEngine} for the given list of
     * input images and saves output to a text file using provided path.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param txtFile file to be created
     */
    public void createTxtFile(final List<File> inputImages, final File txtFile) {
        createTxtFile(inputImages, txtFile, new OcrProcessContext(new Tesseract4EventHelper()));
    }

    /**
     * Performs OCR using provided {@link IOcrEngine} for the given list of
     * input images and saves output to a text file using provided path.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param txtFile file to be created
     * @param ocrProcessContext ocr process context
     */
    public void createTxtFile(final List<File> inputImages, final File txtFile,
            final OcrProcessContext ocrProcessContext) {
        LoggerFactory.getLogger(getClass())
                .info(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.START_OCR_FOR_IMAGES,
                        inputImages.size()));

        StringBuilder content = new StringBuilder();
        for (File inputImage : inputImages) {
            content.append(doImageOcr(inputImage, OutputFormat.TXT, ocrProcessContext));
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
        verifyImageFormatValidity(input);
        return ((TextInfoTesseractOcrResult)processInputFiles(input,
                OutputFormat.HOCR, new Tesseract4EventHelper())).getTextInfos();
    }

    /**
     * Reads data from the provided input image file and returns retrieved
     * data in the format described below.
     *
     * @param input input image {@link java.io.File}
     * @param ocrProcessContext ocr process context
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    public final Map<Integer, List<TextInfo>> doImageOcr(
            final File input,
            OcrProcessContext ocrProcessContext) {
        verifyImageFormatValidity(input);
        return ((TextInfoTesseractOcrResult)processInputFiles(input,
                OutputFormat.HOCR, ocrProcessContext.getOcrEventHelper())).getTextInfos();
    }

    /**
     * Reads data from the provided input image file and returns retrieved
     * data as string.
     *
     * @param input input image {@link java.io.File}
     * @param outputFormat return {@link OutputFormat} result
     * @param ocrProcessContext ocr process context
     * @return OCR result as a {@link java.lang.String} that is
     * returned after processing the given image
     */
    public final String doImageOcr(final File input,
            final OutputFormat outputFormat,
            final OcrProcessContext ocrProcessContext) {
        String result = "";
        verifyImageFormatValidity(input);
        ITesseractOcrResult processedData = processInputFiles(input, outputFormat, ocrProcessContext.getOcrEventHelper());
        if (processedData != null) {
            if (outputFormat.equals(OutputFormat.TXT)) {
                result = ((StringTesseractOcrResult)processedData).getData();
            } else {
                StringBuilder outputText = new StringBuilder();
                Map<Integer, List<TextInfo>> outputMap =
                        ((TextInfoTesseractOcrResult)processedData).getTextInfos();
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
     * Reads data from the provided input image file and returns retrieved
     * data as string.
     *
     * @param input        input image {@link java.io.File}
     * @param outputFormat return {@link OutputFormat} result
     *
     * @return OCR result as a {@link java.lang.String} that is
     * returned after processing the given image
     */
    public final String doImageOcr(final File input,
            final OutputFormat outputFormat) {
        return doImageOcr(input, outputFormat, new OcrProcessContext(
                new Tesseract4EventHelper()));
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
     * {@inheritDoc}
     */
    @Override
    public PdfOcrMetaInfoContainer getMetaInfoContainer() {
        return new PdfOcrMetaInfoContainer(new Tesseract4MetaInfo());
    }

    @Override
    public ProductData getProductData() {
        return PdfOcrTesseract4ProductData.getInstance();
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
    void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat,
            int pageNumber, AbstractPdfOcrEventHelper eventHelper) {
        doTesseractOcr(inputImage, outputFiles, outputFormat, pageNumber, true, eventHelper);
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
     * @param dispatchEvent indicates if event needs to be dispatched
     * @param eventHelper event helper
     */
    abstract void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat,
            int pageNumber, boolean dispatchEvent,
            AbstractPdfOcrEventHelper eventHelper);

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

    PdfOcrTesseract4ProductEvent onEvent(final AbstractPdfOcrEventHelper eventHelper) {
        // usage event
        PdfOcrTesseract4ProductEvent event =
                PdfOcrTesseract4ProductEvent.createProcessImageEvent(eventHelper.getSequenceId(),
                        null, eventHelper.getConfirmationType());
        eventHelper.onEvent(event);
        return event;
    }

    void onEventStatistics(final AbstractPdfOcrEventHelper eventHelper) {
        eventHelper.onEvent(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.DATA,
                PdfOcrTesseract4ProductData.getInstance()));
    }

    /**
     * Reads data from the provided input image file.
     *
     * @param input input image {@link File}
     * @param outputFormat {@link OutputFormat} for the result returned
     *                                         by {@link IOcrEngine}
     * @param eventHelper event helper
     * @return {@link ITesseractOcrResult} instance, either {@link StringTesseractOcrResult}
     *     if output format is TXT, or {@link TextInfoTesseractOcrResult} if the output format is HOCR
     */
    private ITesseractOcrResult processInputFiles(
            final File input, final OutputFormat outputFormat,
            final AbstractPdfOcrEventHelper eventHelper) {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();
        StringBuilder data = new StringBuilder();
        List<File> tempFiles = new ArrayList<File>();
        ITesseractOcrResult result = null;
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

                doTesseractOcr(input, tempFiles, outputFormat, page, true, eventHelper);
                if (outputFormat.equals(OutputFormat.HOCR)) {
                    List<File> tempTxtFiles = null;
                    if (getTesseract4OcrEngineProperties().isUseTxtToImproveHocrParsing()) {
                        tempTxtFiles = new ArrayList<>();
                        for (int i = 0; i < numOfFiles; i++) {
                            tempTxtFiles.add(createTempFile(".txt"));
                        }
                        doTesseractOcr(input, tempTxtFiles, OutputFormat.TXT, page, false, eventHelper);
                    }
                    Map<Integer, List<TextInfo>> pageData = TesseractHelper
                            .parseHocrFile(tempFiles, tempTxtFiles,
                                    getTesseract4OcrEngineProperties());

                    if (getTesseract4OcrEngineProperties()
                            .isPreprocessingImages()) {
                        imageData.put(page, pageData.get(1));
                    } else {
                        imageData = pageData;
                    }
                    result = new TextInfoTesseractOcrResult(imageData);
                } else {
                    for (File tmpFile : tempFiles) {
                        if (Files.exists(
                                java.nio.file.Paths
                                        .get(tmpFile.getAbsolutePath()))) {
                            data.append(TesseractHelper.readTxtFile(tmpFile));
                        }
                    }
                    result = new StringTesseractOcrResult(data.toString());
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
        return result;
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
        ImageType type = ImagePreprocessingUtil.getImageType(image);
        boolean isValid = SUPPORTED_IMAGE_FORMATS.contains(type);
        if (!isValid) {
            LoggerFactory.getLogger(getClass()).error(MessageFormatUtil
                    .format(Tesseract4LogMessageConstant
                                    .CANNOT_READ_INPUT_IMAGE,
                            image.getAbsolutePath()));
            throw new Tesseract4OcrException(
                    Tesseract4OcrException.INCORRECT_INPUT_IMAGE_FORMAT)
                    .setMessageParams(image.getName());
        }
    }

    interface ITesseractOcrResult {
    }

    static class StringTesseractOcrResult implements ITesseractOcrResult {
        private String data;

        StringTesseractOcrResult(String data) {
            this.data = data;
        }

        String getData() {
            return data;
        }
    }

    static class TextInfoTesseractOcrResult implements ITesseractOcrResult {
        private Map<Integer, List<TextInfo>> textInfos;

        TextInfoTesseractOcrResult(Map<Integer, List<TextInfo>> textInfos) {
            this.textInfos = textInfos;
        }

        Map<Integer, List<TextInfo>> getTextInfos() {
            return this.textInfos;
        }
    }
}
