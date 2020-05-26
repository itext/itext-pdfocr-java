package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.pdfocr.IOcrEngine;

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
 * The implementation of {@link Tesseract4OcrEngine} for tesseract OCR.
 *
 * This class provides possibilities to use features of "tesseract"
 * using tess4j.
 */
public class Tesseract4LibOcrEngine extends Tesseract4OcrEngine {

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
        tesseractInstance =
                TesseractOcrUtil.initializeTesseractInstance(isWindows());
    }

    /**
     * Gets tesseract instance depending on the OS type.
     * If instance is null or it was already disposed, it will be initialized
     * with parameters.
     *
     * @return initialized {@link net.sourceforge.tess4j.ITesseract} instance
     */
    public ITesseract getTesseractInstance() {
        if (tesseractInstance == null
                || TesseractOcrUtil
                .isTesseractInstanceDisposed(tesseractInstance)) {
            tesseractInstance = TesseractOcrUtil
                    .initializeTesseractInstance(
                    getTessData(), getLanguagesAsString(),
                    isWindows(), getTesseract4OcrEngineProperties()
                                    .getPathToUserWordsFile());
        }
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
        try {
            validateLanguages(getTesseract4OcrEngineProperties().getLanguages());
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
                        String msg = MessageFormatUtil.format(
                                Tesseract4LogMessageConstant.TesseractFailed,
                                "Cannot write to file: "
                                        + e.getMessage());
                        LoggerFactory.getLogger(getClass())
                                .error(msg);
                        throw new Tesseract4OcrException(
                                Tesseract4OcrException.TesseractFailed);
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
                    != null) {
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
            TesseractOcrUtil util = new TesseractOcrUtil();
            util.initializeImagesListFromTiff(inputImage);
            int numOfPages = util.getListOfPages().size();
            for (int i = 0; i < numOfPages; i++) {
                try {
                    initializeTesseract(outputFormat);
                    String result = util.getOcrResultAsString(
                            getTesseractInstance(),
                            util.getListOfPages().get(i),
                            outputFormat);
                    resultList.add(result);
                } finally {
                    TesseractOcrUtil
                            .disposeTesseractInstance(getTesseractInstance());
                }
            }
        } catch (TesseractException e) {
            String msg = MessageFormatUtil
                    .format(Tesseract4LogMessageConstant.TesseractFailed,
                            e.getMessage());
            LoggerFactory.getLogger(getClass())
                    .error(msg);
            throw new Tesseract4OcrException(
                    Tesseract4OcrException
                            .TesseractFailed);
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
        File preprocessed = null;
        try {
            // preprocess if required
            if (getTesseract4OcrEngineProperties().isPreprocessingImages()) {
                preprocessed = new File(
                        ImagePreprocessingUtil
                                .preprocessImage(inputImage, pageNumber));
            }
            if (!getTesseract4OcrEngineProperties().isPreprocessingImages()
                    || preprocessed == null) {
                // try to open as buffered image if it's not a tiff image
                BufferedImage bufferedImage = null;
                try {
                    try {
                        bufferedImage = ImagePreprocessingUtil
                                .readImageFromFile(inputImage);
                    } catch (IllegalArgumentException | IOException ex) {
                        LoggerFactory.getLogger(getClass()).info(
                                MessageFormatUtil.format(
                                        Tesseract4LogMessageConstant
                                                .CannotCreateBufferedImage,
                                        ex.getMessage()));
                        bufferedImage = ImagePreprocessingUtil
                                .readAsPixAndConvertToBufferedImage(
                                        inputImage);
                    }
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass())
                            .info(MessageFormatUtil.format(
                                    Tesseract4LogMessageConstant.CannotReadInputImage,
                                    ex.getMessage()));
                }
                if (bufferedImage != null) {
                    try {
                        result = new TesseractOcrUtil()
                                .getOcrResultAsString(getTesseractInstance(),
                                        bufferedImage, outputFormat);
                    } catch (TesseractException e) {
                        LoggerFactory.getLogger(getClass())
                                .info(MessageFormatUtil.format(
                                        Tesseract4LogMessageConstant.CannotProcessImage,
                                        e.getMessage()));
                    }
                }
                if (result == null) {
                    result = new TesseractOcrUtil()
                            .getOcrResultAsString(getTesseractInstance(),
                                    inputImage, outputFormat);
                }
            } else {
                result = new TesseractOcrUtil()
                        .getOcrResultAsString(getTesseractInstance(),
                                preprocessed, outputFormat);
            }
        } catch (TesseractException e) {
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil
                            .format(Tesseract4LogMessageConstant.TesseractFailed,
                                    e.getMessage()));
            throw new Tesseract4OcrException(
                    Tesseract4OcrException
                            .TesseractFailed);
        } finally {
            if (preprocessed != null) {
                TesseractHelper.deleteFile(preprocessed.getAbsolutePath());
            }
        }

        return result;
    }
}
