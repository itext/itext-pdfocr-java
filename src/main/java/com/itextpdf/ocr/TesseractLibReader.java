package com.itextpdf.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TesseractLibReader extends TesseractReader {

    /**
     * TesseractExecutableReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractLibReader.class);

    /**
     * Path to hocr config script.
     */
    private static final String PATH_TO_TESS_DATA = "src/main/resources/com/" +
            "itextpdf/ocr/tessdata/";

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFile - output file
     */
    public void doTesseractOcr(final File inputImage,
                               final File outputFile) {
        Tesseract tesseract = new Tesseract();
        tesseract.setHocr(true);
        tesseract.setDatapath(getTessData());
        if (!getLanguages().isEmpty()) {
            tesseract.setLanguage(String.join("+", getLanguages()));
        }
        if (getPageSegMode() != null) {
            tesseract.setPageSegMode(getPageSegMode());
        }

        validateLanguages();

        String result = null;
        try {
            result = tesseract.doOCR(inputImage);
        } catch (TesseractException e) {
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
                LOGGER.error("eng" + suffix + " doesn't exist in provided directory");
                throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                        .setMessageParams("eng" + suffix, getTessData());
            }
        } else {
            for (String lang : languages) {
                if (!new File(getTessData() + lang + suffix).exists()) {
                    LOGGER.error(lang + suffix + " doesn't exist in provided directory");
                    throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }

    /**
     * Get path to provided tess data directory or return default one.
     * @return String
     */
    private String getTessData() {
        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            return getPathToTessData();
        } else {
            return PATH_TO_TESS_DATA;
        }
    }
}
