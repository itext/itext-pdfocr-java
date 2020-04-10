package com.itextpdf.ocr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tesseract reader class.
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
public abstract class TesseractReader implements IOcrReader {

    /**
     * Default suffix for user-word file.
     * (e.g. name: 'eng.user-words')
     */
    public static final String DEFAULT_USER_WORDS_SUFFIX = "user-words";

    /**
     * TesseractReader logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractReader.class);

    /**
     * List of languages required for ocr for provided images.
     */
    private List<String> languages = Collections.<String>emptyList();

    /**
     * Path to directory with tess data.
     */
    private String tessDataDir;

    /**
     * Page Segmentation Mode.
     */
    private Integer pageSegMode;

    /**
     * Type of current OS.
     */
    private String osType;

    /**
     * "True" - if images need to be preprocessed, otherwise - false.
     * By default - true.
     */
    private boolean preprocessingImages = true;

    /**
     * Default text positioning is by lines.
     */
    private TextPositioning textPositioning = TextPositioning.byLines;

    /**
     * Path to the file containing user words.
     * Each word should on new line , file should end with a newline.
     */
    private String userWordsFile = null;

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage - input image file
     * @param outputFiles - list of output file (one per each page)
     * @param outputFormat - output format
     * @param pageNumber - int
     */
    public abstract void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat,
            int pageNumber);

    /**
     * Set list of languages required for provided images.
     *
     * @param requiredLanguages List<String>
     */
    public final void setLanguages(final List<String> requiredLanguages) {
        languages = Collections.<String>unmodifiableList(requiredLanguages);
    }

    /**
     * Get list of languages required for provided images.
     *
     * @return List<String>
     */
    public final List<String> getLanguagesAsList() {
        return new ArrayList<String>(languages);
    }

    /**
     * Get list of languages converted to a string
     * in format required by tesseract.
     * @return String
     */
    public final String getLanguagesAsString() {
        if (getLanguagesAsList().size() > 0) {
            return String.join("+", getLanguagesAsList());
        } else {
            return "eng";
        }
    }

    /**
     * Set path to directory with tess data.
     *
     * @param tessData String
     */
    public final void setPathToTessData(final String tessData) {
        tessDataDir = tessData;
    }

    /**
     * Get path to directory with tess data.
     *
     * @return String
     */
    public final String getPathToTessData() {
        return tessDataDir;
    }

    /**
     * Set Page Segmentation Mode.
     *
     * @param mode Integer
     */
    public final void setPageSegMode(final Integer mode) {
        pageSegMode = mode;
    }

    /**
     * Get Page Segmentation Mode.
     *
     * @return Integer pageSegMode
     */
    public final Integer getPageSegMode() {
        return pageSegMode;
    }

    /**
     * Set type of current OS.
     *
     * @param os String
     */
    public final void setOsType(final String os) {
        osType = os;
    }

    /**
     * Get type of current OS.
     *
     * @return String
     */
    public final String getOsType() {
        return osType;
    }

    /**
     * Set true if images need to be preprocessed, otherwise - false.
     *
     * @param preprocess boolean
     */
    public final void setPreprocessingImages(final boolean preprocess) {
        preprocessingImages = preprocess;
    }

    /**
     * @return true if images need to be preprocessed, otherwise - false
     */
    public final boolean isPreprocessingImages() {
        return preprocessingImages;
    }

    /**
     * Set text positioning (by lines or by words).
     *
     * @param positioning TextPositioning
     */
    public final void setTextPositioning(final TextPositioning positioning) {
        textPositioning = positioning;
    }

    /**
     * @return text positioning
     */
    public final TextPositioning getTextPositioning() {
        return textPositioning;
    }

    /**
     * Reads data from input stream and returns retrieved data
     * in the following format:
     * <p>
     * List<TextInfo> where each list TextInfo element contains word
     * or line and its 4 coordinates(bbox).
     *
     * @param is InputStream
     * @param outputFormat OutputFormat
     * @return List<TextInfo>
     */
    public final List<TextInfo> readDataFromInput(final InputStream is,
            final OutputFormat outputFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads data from the provided input image file and
     * returns retrieved data as a string.
     *
     * @param input File
     * @param outputFormat OutputFormat
     * @return String
     */
    public final String readDataFromInput(final File input,
            final OutputFormat outputFormat) {
        StringBuilder data = new StringBuilder();
        try {
            // image needs to be paginated only if it's tiff
            // or preprocessing isn't required
            int realNumOfPages = !ImageUtil.isTiffImage(input)
                    ? 1 : ImageUtil.getNumberOfPageTiff(input);
            int numOfPages = isPreprocessingImages() ? realNumOfPages : 1;
            int numOfFiles = isPreprocessingImages() ? 1 : realNumOfPages;

            for (int page = 1; page <= numOfPages; page++) {
                List<File> tempFiles = new ArrayList<File>();
                for (int i = 0; i < numOfFiles; i++) {
                    tempFiles.add(createTempFile(".txt"));
                }

                doTesseractOcr(input, tempFiles, OutputFormat.txt, page);
                for (File tmpFile : tempFiles) {
                    if (Files.exists(
                            java.nio.file.Paths
                                    .get(tmpFile.getAbsolutePath()))) {
                        data.append(UtilService.readTxtFile(tmpFile));
                    } else {
                        LOGGER.error("Error occurred. File wasn't created "
                                + tmpFile.getAbsolutePath());
                    }
                }

                for (File file : tempFiles) {
                    UtilService.deleteFile(file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred: " + e.getMessage());
        }

        return data.toString();
    }

    /**
     * Reads data from the provided input image file and returns
     * retrieved data in the following format:
     * Map<Integer, List<TextInfo>>:
     * key: number of page,
     * value: list of TextInfo objects where each list element
     * Map.Entry<String, List<Float>> contains word or line as a key
     * and its 4 coordinates(bbox) as a values.
     *
     * @param input File
     * @return Map<Integer, List<TextInfo>>
     */
    public final Map<Integer, List<TextInfo>> readDataFromInput(final File input) {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();
        try {
            // image needs to be paginated only if it's tiff
            // or preprocessing isn't required
            int realNumOfPages = !ImageUtil.isTiffImage(input)
                    ? 1 : ImageUtil.getNumberOfPageTiff(input);
            int numOfPages = isPreprocessingImages() ? realNumOfPages : 1;
            int numOfFiles = isPreprocessingImages() ? 1 : realNumOfPages;

            for (int page = 1; page <= numOfPages; page++) {
                List<File> tempFiles = new ArrayList<File>();
                for (int i = 0; i < numOfFiles; i++) {
                    tempFiles.add(createTempFile(".hocr"));
                }

                doTesseractOcr(input, tempFiles, OutputFormat.hocr, page);
                Map<Integer, List<TextInfo>> pageData = UtilService.parseHocrFile(tempFiles,
                        getTextPositioning());

                LOGGER.info((pageData.keySet().size() > 1
                        ? pageData.keySet().size() : page)
                        + " page(s) were read");
                if (isPreprocessingImages()) {
                    imageData.put(page, TesseractUtil.getValueByKey(pageData, 1));
                } else {
                    imageData = pageData;
                }

                for (File file : tempFiles) {
                    UtilService.deleteFile(file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred: " + e.getMessage());
        }

        return imageData;
    }

    /**
     * Using provided list of words there will be created
     * temporary file containing words (one per line) which
     * ends with a new line character. Train data for provided language
     * should exist in specified tess data directory.
     *
     * @param language String
     * @param userWords List<String>
     */
    public void setUserWords(final String language,
                             final List<String> userWords) {
        if (userWords == null || userWords.size() == 0) {
            userWordsFile = null;
        } else {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (String word : userWords) {
                    byte[] bytesWord = word.getBytes();
                    baos.write(bytesWord, 0, bytesWord.length);
                    byte[] bytesSeparator = System.lineSeparator()
                            .getBytes();
                    baos.write(bytesSeparator, 0, bytesSeparator.length);
                }
                InputStream inputStream = new ByteArrayInputStream(
                        baos.toByteArray());
                baos.close();
                setUserWords(language, inputStream);
            } catch (IOException e) {
                LOGGER.warn("Cannot use custom user words: " + e.getMessage());
            }
        }
    }

    /**
     * Using provided input stream there will be created
     * temporary file (with name 'language.user-words')
     * containing words (one per line) which ends with
     * a new line character. Train data for provided language
     * should exist in specified tess data directory.
     *
     * @param language String
     * @param inputStream InputStream
     */
    public void setUserWords(final String language,
            final InputStream inputStream) {
        String userWordsFileName = TesseractUtil.getTempDir()
                + java.io.File.separatorChar
                + language + "." + DEFAULT_USER_WORDS_SUFFIX;
        if (!getLanguagesAsList().contains(language)) {
            if ("eng".equals(language.toLowerCase())) {
                List<String> languagesList = getLanguagesAsList();
                languagesList.add(language);
                setLanguages(languagesList);
            } else {
                throw new OCRException(
                        OCRException.LANGUAGE_IS_NOT_IN_THE_LIST)
                        .setMessageParams(language);
            }
        }
        validateLanguages(Collections.<String>singletonList(language));
        try (OutputStreamWriter writer =
                new FileWriter(userWordsFileName)) {
            Reader reader = new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8);
            int data;
            while ((data = reader.read()) != -1) {
                writer.write(data);
            }
            writer.write(System.lineSeparator());
            userWordsFile = userWordsFileName;
        } catch (IOException e) {
            userWordsFile = null;
            LOGGER.warn("Cannot use custom user words: " + e.getMessage());
        }
    }

    /**
     * Return path to the user words file if exists, otherwise null.
     *
     * @return String
     */
    public final String getUserWordsFilePath() {
        return userWordsFile;
    }

    /**
     * Get path to provided tess data directory or return default one.
     *
     * @return String
     */
    String getTessData() {
        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            return getPathToTessData();
        } else {
            throw new OCRException(OCRException.CANNOT_FIND_PATH_TO_TESSDATA);
        }
    }

    /**
     * Return 'true' if current OS is windows
     * otherwise 'false'.
     *
     * @return boolean
     */
    public boolean isWindows() {
        return getOsType().toLowerCase().contains("win");
    }

    /**
     * Check type of current OS and return it (mac, win, linux).
     *
     * @return String
     */
    public String identifyOSType() {
        String os = System.getProperty("os.name") == null
                ? System.getProperty("OS") : System.getProperty("os.name");
        LOGGER.info("Using System Property: " + os);
        return os.toLowerCase();
    }

    /**
     * Validate provided languages and
     * check if they exist in provided tess data directory.
     * @param languagesList List<String>
     */
    public void validateLanguages(final List<String> languagesList) {
        String suffix = ".traineddata";
        if (languagesList.size() == 0) {
            if (!new File(getTessData()
                    + java.io.File.separatorChar + "eng" + suffix).exists()) {
                LOGGER.error("eng" + suffix
                        + " doesn't exist in provided directory");
                throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                        .setMessageParams("eng" + suffix, getTessData());
            }
        } else {
            for (String lang : languagesList) {
                if (!new File(getTessData()
                        + java.io.File.separatorChar + lang + suffix)
                        .exists()) {
                    LOGGER.error(lang + suffix
                            + " doesn't exist in provided directory");
                    throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }

    /**
     * Create temporary file with given extension.
     *
     * @param extension String
     * @return File
     */
    private File createTempFile(final String extension) {
        String tmpFileName = TesseractUtil.getTempDir()
                + UUID.randomUUID().toString() + extension;
        return new File(tmpFileName);
    }
}
