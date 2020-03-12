package com.itextpdf.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.imaging.ImageFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import static com.itextpdf.ocr.ImageUtil.preprocessTiffImage;

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
     * TesseractLibReader constructor with path to tess data directory.
     */
    public TesseractLibReader(String tessDataPath) {
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
     * @param outputFormat - output format
     */
    public void doTesseractOcr(File inputImage,
            final File outputFile, final OutputFormat outputFormat) {
        getTesseractInstance().setDatapath(getTessData());
        getTesseractInstance()
                .setTessVariable("tessedit_create_hocr",
                        outputFormat.equals(OutputFormat.hocr) ? "1" : "0");
        if (getUserWordsFilePath() != null) {
            getTesseractInstance().setTessVariable("load_system_dawg", "0");
            getTesseractInstance().setTessVariable("load_freq_dawg", "0");
            getTesseractInstance().setTessVariable("user_words_suffix",
                    DEFAULT_USER_WORDS_SUFFIX);
            getTesseractInstance().setTessVariable("user_words_file",
                    getUserWordsFilePath());
        }

        if (getLanguages().size() > 0) {
            getTesseractInstance()
                    .setLanguage(String.join("+", getLanguages()));
        }
        if (getPageSegMode() != null) {
            getTesseractInstance()
                    .setPageSegMode(getPageSegMode());
        }

        validateLanguages(getLanguages());

        String result = null;
        File tmpFile = null;
        BufferedImage img = null;
        try {
            LOGGER.info("Preprocessing image " + inputImage.getAbsolutePath() + ": " + isPreprocessingImages());
            // preprocess if required
            if (isPreprocessingImages()) {
                try {
                    String extension = ImageUtil.getExtension(inputImage.getAbsolutePath());
                    if (extension.toLowerCase().contains("tif")) {
                        tmpFile = ImageUtil.preprocessTiffImage(inputImage);
                    } else {
                        int format = ImageUtil.getFormat(extension);
                        Pix resultPix = ImageUtil.preprocessImageToPix(inputImage);
                        tmpFile = File.createTempFile(UUID.randomUUID().toString(),
                                ".png");
                        LOGGER.info("Creating tmp preprocessed file "
                                + tmpFile.getAbsolutePath());
                        try {
                            img = ImageUtil.convertPixToImage(resultPix, format);
                            if (img != null) {
                                tmpFile = null;
                                LOGGER.info("Saved BufferedImage");
                            } else {
                                Leptonica.INSTANCE.pixWritePng(tmpFile.getAbsolutePath(), resultPix, format);
                                LOGGER.info("Saved Pix");
                            }
                        } catch (IOException e) {
                            LOGGER.warn("Cannot convert pix to "
                                    + "buffered image after converting: "
                                    + e.getMessage());
                            Leptonica.INSTANCE.pixWritePng(tmpFile.getAbsolutePath(), resultPix, format);
                            LOGGER.info("Saved Pix");
                        }
                        ImageUtil.destroyPix(resultPix);
                    }
                } catch (IOException | NullPointerException e) {
                    LOGGER.warn("Cannot preprocess image: " + inputImage.getAbsolutePath()
                            + " with error " + e.getMessage());
                }
            }
            // perform OCR
            if ((img == null && tmpFile == null) || !isPreprocessingImages()) {
                result = getTesseractInstance().doOCR(inputImage);
            } else if (img != null) {
                result = getTesseractInstance().doOCR(img);
            } else {
                result = getTesseractInstance().doOCR(tmpFile);
            }
        } catch (TesseractException e) {
            LOGGER.error("OCR failed: " + e.getMessage());
            throw new OCRException(OCRException.TESSERACT_FAILED);
        } finally {
            if (tmpFile != null) {
                UtilService.deleteFile(tmpFile);
            }
        }

        if (result != null) {
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(outputFile.getAbsolutePath()),
                    StandardCharsets.UTF_8)) {
                writer.write(result);
            } catch (IOException e) {
                LOGGER.error("Cannot write to file: " + e.getMessage());
                throw new OCRException(OCRException.TESSERACT_FAILED_WITH_REASON)
                        .setMessageParams("Cannot write to file "
                                + outputFile.getAbsolutePath());
            }
        } else {
            LOGGER.warn("OCR result is NULL for " + inputImage.getAbsolutePath());
        }

        if (getUserWordsFilePath() != null) {
            UtilService.deleteFile(new File(getUserWordsFilePath()));
        }
    }
}
