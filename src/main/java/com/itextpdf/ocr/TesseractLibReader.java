package com.itextpdf.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

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
     * TesseractExecutableReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractLibReader.class);

    /**
     * Tesseract Instance.
     * (depends on OS type)
     */
    private ITesseract tesseractInstance;

    /**
     * TesseractReader constructor.
     */
    public TesseractLibReader() {
        setOsType(identifyOSType());
        setTesseractInstance();
    }

    /**
     * Set tesseract instance depending on the OS type.
     */
    public void setTesseractInstance() {
        if (isWindows()) {
            tesseractInstance = new Tesseract1();
        } else {
            tesseractInstance = new Tesseract();
        }
    }

    /**
     * Get tesseract instance depending on the OS type.
     *
     * @return ITesseract
     */
    public ITesseract getTesseractInstance() {
        return tesseractInstance;
    }

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFile - output file
     */
    public void doTesseractOcr(final File inputImage,
            final File outputFile) {
        getTesseractInstance().setDatapath(getTessData());
        getTesseractInstance()
                .setConfigs(Collections.singletonList(PATH_TO_QUIET_SCRIPT));
        getTesseractInstance()
                .setTessVariable("tessedit_create_hocr", "1");

        if (!getLanguages().isEmpty()) {
            getTesseractInstance()
                    .setLanguage(String.join("+", getLanguages()));
        }
        if (getPageSegMode() != null) {
            getTesseractInstance()
                    .setPageSegMode(getPageSegMode());
        }

        validateLanguages();

        String result = null;
        try {
            if (isPreprocessingImages()) {
                BufferedImage preprocessed = ImageUtil
                        .preprocessImage(inputImage.getAbsolutePath());
                result = getTesseractInstance().doOCR(preprocessed);
            } else {
                result = getTesseractInstance().doOCR(inputImage);
            }
        } catch (TesseractException | IOException e) {
            LOGGER.error("OCR failed: " + e.getLocalizedMessage());
            throw new OCRException(OCRException.TESSERACT_FAILED_WITH_REASON)
                    .setMessageParams("OCR failed");
        }

        try (BufferedWriter writer = Files
                .newBufferedWriter(outputFile.toPath())) {
            writer.write(result);
        } catch (IOException e) {
            LOGGER.error("Cannot write to file: " + e.getLocalizedMessage());
            throw new OCRException(OCRException.TESSERACT_FAILED_WITH_REASON)
                    .setMessageParams("Cannot write to file "
                            + outputFile.getAbsolutePath());
        }
    }

    /**
     * Validate provided languages and
     * check if they exist in provided tess data directory.
     */
    private void validateLanguages() {
        String suffix = ".traineddata";
        List<String> languages = getLanguages();
        if (languages.isEmpty()) {
            if (!new File(getTessData() + "eng" + suffix).exists()) {
                LOGGER.error("eng" + suffix
                        + " doesn't exist in provided directory");
                throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                        .setMessageParams("eng" + suffix, getTessData());
            }
        } else {
            for (String lang : languages) {
                if (!new File(getTessData() + lang + suffix)
                        .exists()) {
                    LOGGER.error(lang + suffix
                            + " doesn't exist in provided directory");
                    throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }
}
