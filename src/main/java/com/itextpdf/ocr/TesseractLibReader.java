package com.itextpdf.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.LoggerFactory;

/**
 * Tesseract Library Reader class.
 * (extends TesseractReader class)
 * <p>
 * This class provides possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems)
 * <p>
 * This class provides possibility to perform OCR, read data from input files
 * and return contained text in the described format
 * <p>
 * This class provides possibilities to set type of current os,
 * required languages for OCR for input images,
 * set path to directory with tess data.
 */
public class TesseractLibReader extends TesseractReader {

    /**
     * Tesseract Instance.
     * (depends on OS type)
     */
    private ITesseract tesseractInstance = null;

    /**
     * TesseractLibReader constructor with path to tess data directory.
     *
     * @param tessDataPath String
     */
    public TesseractLibReader(final String tessDataPath) {
        setOsType(identifyOSType());
        setTesseractInstance();
        setPathToTessData(tessDataPath);
    }

    /**
     * TesseractLibReader constructor with path to tess data directory,
     * list of languages and path to tess data directory.
     *
     * @param languagesList List<String>
     * @param tessDataPath  String
     */
    public TesseractLibReader(final String tessDataPath,
            final List<String> languagesList) {
        setOsType(identifyOSType());
        setTesseractInstance();
        setPathToTessData(tessDataPath);
        setLanguages(Collections.<String>unmodifiableList(languagesList));
    }

    /**
     * Set tesseract instance depending on the OS type.
     */
    public void setTesseractInstance() {
        tesseractInstance = TesseractUtil.createTesseractInstance(isWindows());
    }

    /**
     * Get tesseract instance depending on the OS type.
     * If instance is null, it will be initialized with parameters
     *
     * @return ITesseract
     */
    public ITesseract getTesseractInstance() {
        if (tesseractInstance == null
                || TesseractUtil
                .isTesseractInstanceDisposed(tesseractInstance)) {
            tesseractInstance = TesseractUtil
                    .initializeTesseractInstanceWithParameters(
                    getTessData(), getLanguagesAsString(),
                    isWindows(), getUserWordsFilePath());
        }
        return tesseractInstance;
    }

    /**
     * Initialize instance of tesseract and set all the required properties.
     *
     * @param outputFormat OutputFormat
     */
    public void initializeTesseract(final OutputFormat outputFormat) {
        setTesseractInstance();

        getTesseractInstance()
                .setTessVariable("tessedit_create_hocr",
                        outputFormat.equals(OutputFormat.hocr) ? "1" : "0");
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
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFiles - list of output file (one for each page)
     * @param outputFormat - output format
     * @param pageNumber - int
     */
    public void doTesseractOcr(final File inputImage,
            final List<File> outputFiles, final OutputFormat outputFormat,
            final int pageNumber) {
        try {
            validateLanguages(getLanguagesAsList());
            initializeTesseract(outputFormat);

            // if proprocessing is not needed and provided image is tiff,
            // the image will be paginated and separate pages will be OCRed
            List<String> resultList = new ArrayList<String>();
            if (!isPreprocessingImages() && ImageUtil.isTiffImage(inputImage)) {
                resultList = getOCRResultForMultiPage(inputImage, outputFormat);
            } else {
                resultList.add(getOCRResultForSinglePage(inputImage,
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
                        LoggerFactory.getLogger(getClass())
                                .error("Cannot write to file: " + e.getMessage());
                        throw new OCRException(
                                OCRException.TESSERACT_FAILED)
                                .setMessageParams("Cannot write to file "
                                        + outputFile.getAbsolutePath());
                    }
                } else {
                    LoggerFactory.getLogger(getClass())
                            .info("OCR result is NULL for "
                            + inputImage.getAbsolutePath());
                }
            }

            TesseractUtil.disposeTesseractInstance(getTesseractInstance());
        } catch (OCRException e) {
            LoggerFactory.getLogger(getClass())
                    .error(e.getMessage());
            throw new OCRException(e.getMessage(), e);
        } finally {
            if (getUserWordsFilePath() != null) {
                UtilService.deleteFile(getUserWordsFilePath());
            }
        }
    }

    /**
     * Get ocr result from provided multipage image and
     * and return result ad list fo strings for each page.
     * (this method is used when preprocessing is not needed)
     *
     * @param inputImage File
     * @param outputFormat OutputFormat
     * @return List<String>
     */
    private List<String> getOCRResultForMultiPage(final File inputImage,
            final OutputFormat outputFormat) {
        List<String> resultList = new ArrayList<String>();
        try {
            TesseractUtil.initializeImagesListFromTiff(inputImage);
            int numOfPages = TesseractUtil.getListOfPages().size();
            for (int i = 0; i < numOfPages; i++) {
                initializeTesseract(outputFormat);
                String result = TesseractUtil.getOcrResultAsString(
                        getTesseractInstance(),
                        TesseractUtil.getListOfPages().get(i),
                        outputFormat);
                resultList.add(result);
                TesseractUtil.disposeTesseractInstance(getTesseractInstance());
            }
        } catch (TesseractException e) {
            String msg = MessageFormat
                    .format(OCRException.TESSERACT_FAILED,
                            e.getMessage());
            LoggerFactory.getLogger(getClass())
                    .error(msg);
            throw new OCRException(OCRException.TESSERACT_FAILED)
                    .setMessageParams(e.getMessage());
        }
        return resultList;
    }

    /**
     * Get ocr result from provided single page image
     * and preprocess it if needed.
     *
     * @param inputImage File
     * @param outputFormat OutputFormat
     * @param pageNumber int
     * @return String
     */
    private String getOCRResultForSinglePage(final File inputImage,
            final OutputFormat outputFormat,
            final int pageNumber) {
        String result = null;
        File preprocessed = null;
        try {
            // preprocess if required
            if (isPreprocessingImages()) {
                preprocessed = new File(ImageUtil.preprocessImage(inputImage, pageNumber));
            }
            if (!isPreprocessingImages() || preprocessed == null) {
                // try to open as buffered image if it's not a tiff image
                BufferedImage bufferedImage = null;
                try {
                    try {
                        bufferedImage = ImageUtil
                                .readImageFromFile(inputImage);
                    } catch (IllegalArgumentException | IOException ex) {
                        LoggerFactory.getLogger(getClass())
                                .info("Cannot create a buffered image "
                                + "from the input image: "
                                + ex.getMessage());
                        bufferedImage = ImageUtil
                                .readAsPixAndConvertToBufferedImage(inputImage);
                    }
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass())
                            .info("Cannot read image: " + ex.getMessage());
                }
                if (bufferedImage != null) {
                    try {
                        result = TesseractUtil
                                .getOcrResultAsString(getTesseractInstance(),
                                        bufferedImage, outputFormat);
                    } catch (TesseractException e) {
                        LoggerFactory.getLogger(getClass())
                                .info("Cannot process image: " + e.getMessage());
                    }
                }
                if (result == null) {
                    result = TesseractUtil
                            .getOcrResultAsString(getTesseractInstance(),
                                    inputImage, outputFormat);
                }
            } else {
                result = TesseractUtil
                        .getOcrResultAsString(getTesseractInstance(),
                                preprocessed, outputFormat);
            }
        } catch (TesseractException e) {
            String msg = MessageFormat
                    .format(OCRException.TESSERACT_FAILED,
                            e.getMessage());
            LoggerFactory.getLogger(getClass())
                    .error(msg);
            throw new OCRException(OCRException.TESSERACT_FAILED)
                    .setMessageParams(e.getMessage());
        } finally {
            if (preprocessed != null) {
                UtilService.deleteFile(preprocessed.getAbsolutePath());
            }
        }

        return result;
    }
}
