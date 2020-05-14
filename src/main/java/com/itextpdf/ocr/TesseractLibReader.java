package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TesseractReader} for tesseract OCR.
 *
 * This class provides possibilities to use features of "tesseract"
 * using tess4j.
 */
public class TesseractLibReader extends TesseractReader {

    /**
     * {@link net.sourceforge.tess4j.ITesseract} Instance.
     * (depends on OS type)
     */
    private ITesseract tesseractInstance = null;

    /**
     * Creates a new {@link TesseractLibReader} instance.
     *
     * @param tessDataPath path to tess data directory
     */
    public TesseractLibReader(final String tessDataPath) {
        setOsType(identifyOsType());
        setPathToTessData(tessDataPath);
        TesseractUtil.initializeTesseractInstance(isWindows());
    }

    /**
     * Creates a new {@link TesseractLibReader} instance.
     *
     * @param languagesList list of required languages
     * @param tessDataPath  path to tess data directory
     */
    public TesseractLibReader(final String tessDataPath,
            final List<String> languagesList) {
        setOsType(identifyOsType());
        setPathToTessData(tessDataPath);
        setLanguages(Collections.<String>unmodifiableList(languagesList));
        TesseractUtil.initializeTesseractInstance(isWindows());
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
                || TesseractUtil
                .isTesseractInstanceDisposed(tesseractInstance)) {
            tesseractInstance = TesseractUtil
                    .initializeTesseractInstance(
                    getTessData(), getLanguagesAsString(),
                    isWindows(), getUserWordsFilePath());
        }
        return tesseractInstance;
    }

    /**
     * Initializes instance of tesseract if it haven't been already
     * initialized or it have been disposed and sets all the required
     * properties.
     *
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     */
    public void initializeTesseract(final OutputFormat outputFormat) {
        getTesseractInstance()
                .setTessVariable("tessedit_create_hocr",
                        outputFormat.equals(OutputFormat.HOCR) ? "1" : "0");
        getTesseractInstance().setTessVariable("user_defined_dpi", "300");
        if (getUserWordsFilePath() != null) {
            getTesseractInstance()
                    .setTessVariable("load_system_dawg", "0");
            getTesseractInstance()
                    .setTessVariable("load_freq_dawg", "0");
            getTesseractInstance()
                    .setTessVariable("user_words_suffix",
                            DEFAULT_USER_WORDS_SUFFIX);
            getTesseractInstance()
                    .setTessVariable("user_words_file",
                            getUserWordsFilePath());
        }

        TesseractUtil.setTesseractProperties(getTesseractInstance(),
                getTessData(), getLanguagesAsString(), getPageSegMode(),
                getUserWordsFilePath());
    }

    /**
     * Performs tesseract OCR using command line tool for the selected page
     * of input image (by default 1st).
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFiles {@link java.util.List} of output files
     *                                          (one per each page)
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     * @param pageNumber number of page to be processed
     */
    public void doTesseractOcr(final File inputImage,
            final List<File> outputFiles, final OutputFormat outputFormat,
            final int pageNumber) {
        try {
            validateLanguages(getLanguagesAsList());
            initializeTesseract(outputFormat);

            // if preprocessing is not needed and provided image is tiff,
            // the image will be paginated and separate pages will be OCRed
            List<String> resultList = new ArrayList<String>();
            if (!isPreprocessingImages()
                    && ImageUtil.isTiffImage(inputImage)) {
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
                                LogMessageConstant.TesseractFailed,
                                "Cannot write to file: "
                                        + e.getMessage());
                        LoggerFactory.getLogger(getClass())
                                .error(msg);
                        throw new OcrException(OcrException.TesseractFailed);
                    }
                }
            }
        } catch (OcrException e) {
            LoggerFactory.getLogger(getClass())
                    .error(e.getMessage());
            throw new OcrException(e.getMessage(), e);
        } finally {
            if (tesseractInstance != null) {
                TesseractUtil.disposeTesseractInstance(tesseractInstance);
            }
            if (getUserWordsFilePath() != null) {
                UtilService.deleteFile(getUserWordsFilePath());
            }
        }
    }

    /**
     * Gets OCR result from provided multi-page image and returns result as
     * list of strings for each page. This method is used for tiff images
     * when preprocessing is not needed.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     * @return list of result string that will be written to a temporary files
     * later
     */
    private List<String> getOcrResultForMultiPage(final File inputImage,
            final OutputFormat outputFormat) {
        List<String> resultList = new ArrayList<String>();
        try {
            TesseractUtil util = new TesseractUtil();
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
                    TesseractUtil
                            .disposeTesseractInstance(getTesseractInstance());
                }
            }
        } catch (TesseractException e) {
            String msg = MessageFormatUtil
                    .format(LogMessageConstant.TesseractFailed,
                            e.getMessage());
            LoggerFactory.getLogger(getClass())
                    .error(msg);
            throw new OcrException(OcrException.TesseractFailed);
        }
        return resultList;
    }

    /**
     * Gets OCR result from provided single page image and preprocesses it if
     * it is needed.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
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
            if (isPreprocessingImages()) {
                preprocessed = new File(
                        ImageUtil.preprocessImage(inputImage, pageNumber));
            }
            if (!isPreprocessingImages() || preprocessed == null) {
                // try to open as buffered image if it's not a tiff image
                BufferedImage bufferedImage = null;
                try {
                    try {
                        bufferedImage = ImageUtil
                                .readImageFromFile(inputImage);
                    } catch (IllegalArgumentException | IOException ex) {
                        LoggerFactory.getLogger(getClass()).info(
                                MessageFormatUtil.format(
                                        LogMessageConstant
                                                .CannotCreateBufferedImage,
                                        ex.getMessage()));
                        bufferedImage = ImageUtil
                                .readAsPixAndConvertToBufferedImage(
                                        inputImage);
                    }
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass())
                            .info(MessageFormatUtil.format(
                                    LogMessageConstant.CannotReadInputImage,
                                    ex.getMessage()));
                }
                if (bufferedImage != null) {
                    try {
                        result = new TesseractUtil()
                                .getOcrResultAsString(getTesseractInstance(),
                                        bufferedImage, outputFormat);
                    } catch (TesseractException e) {
                        LoggerFactory.getLogger(getClass())
                                .info(MessageFormatUtil.format(
                                        LogMessageConstant.CannotProcessImage,
                                        e.getMessage()));
                    }
                }
                if (result == null) {
                    result = new TesseractUtil()
                            .getOcrResultAsString(getTesseractInstance(),
                                    inputImage, outputFormat);
                }
            } else {
                result = new TesseractUtil()
                        .getOcrResultAsString(getTesseractInstance(),
                                preprocessed, outputFormat);
            }
        } catch (TesseractException e) {
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil
                            .format(LogMessageConstant.TesseractFailed,
                                    e.getMessage()));
            throw new OcrException(OcrException.TesseractFailed);
        } finally {
            if (preprocessed != null) {
                UtilService.deleteFile(preprocessed.getAbsolutePath());
            }
        }

        return result;
    }
}
