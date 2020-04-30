package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;

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
import org.slf4j.LoggerFactory;

/**
 * Tesseract reader class.
 *
 * This class provides possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems)
 *
 * This class provides possibility to perform OCR, read data from input files
 * and return contained text in the described format
 *
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
    private TextPositioning textPositioning = TextPositioning.BY_LINES;

    /**
     * Path to the file containing user words.
     * Each word should on new line , file should end with a newline.
     */
    private String userWordsFile = null;

    /**
     * Perform tesseract OCR.
     *
     * @param inputImage {@link java.io.File} input image file
     * @param outputFiles {@link java.util.List} list of output file
     *                                          (one per each page)
     * @param outputFormat {@link OutputFormat}
     * @param pageNumber int
     */
    public abstract void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat,
            int pageNumber);

    /**
     * Set list of languages required for provided images.
     *
     * @param requiredLanguages {@link java.util.List}
     */
    public final void setLanguages(final List<String> requiredLanguages) {
        languages = Collections.<String>unmodifiableList(requiredLanguages);
    }

    /**
     * Get list of languages required for provided images.
     *
     * @return {@link java.util.List}
     */
    public final List<String> getLanguagesAsList() {
        return new ArrayList<String>(languages);
    }

    /**
     * Get list of languages converted to a string
     * in format required by tesseract.
     * @return {@link java.lang.String}
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
     * @param tessData {@link java.lang.String}
     */
    public final void setPathToTessData(final String tessData) {
        tessDataDir = tessData;
    }

    /**
     * Get path to directory with tess data.
     *
     * @return {@link java.lang.String}
     */
    public final String getPathToTessData() {
        return tessDataDir;
    }

    /**
     * Set Page Segmentation Mode.
     *
     * @param mode {@link java.lang.Integer}
     */
    public final void setPageSegMode(final Integer mode) {
        pageSegMode = mode;
    }

    /**
     * Get Page Segmentation Mode.
     *
     * @return {@link java.lang.Integer} pageSegMode
     */
    public final Integer getPageSegMode() {
        return pageSegMode;
    }

    /**
     * Set type of current OS.
     *
     * @param os {@link java.lang.String}
     */
    public final void setOsType(final String os) {
        osType = os;
    }

    /**
     * Get type of current OS.
     *
     * @return {@link java.lang.String}
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
     * @param positioning {@link TextPositioning}
     */
    public final void setTextPositioning(final TextPositioning positioning) {
        textPositioning = positioning;
    }

    /**
     * @return {@link TextPositioning}
     */
    public final TextPositioning getTextPositioning() {
        return textPositioning;
    }

    /**
     * Reads data from the provided input image file and
     * returns retrieved data as a string.
     *
     * @param input {@link java.io.File}
     * @param outputFormat {@link OutputFormat}
     *        "txt" output format:
     *              tesseract performs ocr and returns output in txt format
     *         "hocr" output format:
     *              tesseract performs ocr and returns output in hocr format,
     *              then result text is extracted
     * @return {@link java.lang.String}
     */
    public final String readDataFromInput(final File input,
            final OutputFormat outputFormat) {
        Map<String, Map<Integer, List<TextInfo>>> result =
                processInputFiles(input, outputFormat);
        if (result != null && result.size() > 0) {
            List<String> keys = new ArrayList<String>(result.keySet());
            if (outputFormat.equals(OutputFormat.TXT)) {
                return keys.get(0);
            } else {
                StringBuilder outputText = new StringBuilder();
                Map<Integer, List<TextInfo>> outputMap =
                        result.get(keys.get(0));
                for (int page : outputMap.keySet()) {
                    StringBuilder pageText = new StringBuilder();
                    for (TextInfo textInfo : outputMap.get(page)) {
                        pageText.append(textInfo.getText());
                        pageText.append(System.lineSeparator());
                    }
                    outputText.append(pageText);
                    outputText.append(System.lineSeparator());
                }
                return outputText.toString();
            }
        } else {
            return "";
        }
    }

    /**
     * Reads data from the provided input image file and returns
     * retrieved data in the following format:
     * Map<Integer, List<TextInfo>>:
     * key: number of page,
     * value: list of {@link TextInfo} objects where each list element
     * Map.Entry<String, List<Float>> contains word or line as a key
     * and its 4 coordinates(bbox) as a values.
     *
     * @param input {@link java.io.File}
     * @return Map<Integer, List<TextInfo>>
     */
    public final Map<Integer, List<TextInfo>> readDataFromInput(
            final File input) {
        Map<String, Map<Integer, List<TextInfo>>> result =
                processInputFiles(input, OutputFormat.HOCR);
        if (result != null && result.size() > 0) {
            List<String> keys = new ArrayList<String>(result.keySet());
            return result.get(keys.get(0));
        } else {
            return new LinkedHashMap<Integer, List<TextInfo>>();
        }
    }

    /**
     * Reads data from the provided input image file.
     *
     * @param input {@link java.io.File}
     * @param outputFormat {@link OutputFormat}
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

        try {
            // image needs to be paginated only if it's tiff
            // or preprocessing isn't required
            int realNumOfPages = !ImageUtil.isTiffImage(input)
                    ? 1 : ImageUtil.getNumberOfPageTiff(input);
            int numOfPages = isPreprocessingImages() ? realNumOfPages : 1;
            int numOfFiles = isPreprocessingImages() ? 1 : realNumOfPages;

            for (int page = 1; page <= numOfPages; page++) {
                List<File> tempFiles = new ArrayList<File>();
                String extension = outputFormat.equals(OutputFormat.HOCR)
                        ? ".hocr" : ".txt";
                for (int i = 0; i < numOfFiles; i++) {
                    tempFiles.add(createTempFile(extension));
                }

                doTesseractOcr(input, tempFiles, outputFormat, page);
                if (outputFormat.equals(OutputFormat.HOCR)) {
                    Map<Integer, List<TextInfo>> pageData = UtilService
                            .parseHocrFile(tempFiles, getTextPositioning());

                    if (isPreprocessingImages()) {
                        imageData.put(page, pageData.get(1));
                    } else {
                        imageData = pageData;
                    }
                } else {
                    for (File tmpFile : tempFiles) {
                        if (Files.exists(
                                java.nio.file.Paths
                                        .get(tmpFile.getAbsolutePath()))) {
                            data.append(UtilService.readTxtFile(tmpFile));
                        }
                    }
                }

                for (File file : tempFiles) {
                    UtilService.deleteFile(file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil.format(
                            LogMessageConstant.CANNOT_OCR_INPUT_FILE,
                            e.getMessage()));
        }

        Map<String, Map<Integer, List<TextInfo>>> result =
                new LinkedHashMap<String, Map<Integer, List<TextInfo>>>();
        result.put(data.toString(), imageData);
        return result;
    }

    /**
     * Using provided list of words there will be created
     * temporary file containing words (one per line) which
     * ends with a new line character. Train data for provided language
     * should exist in specified tess data directory.
     *
     * @param language {@link java.lang.String}
     * @param userWords {@link java.util.List}
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
                LoggerFactory.getLogger(getClass())
                        .warn(MessageFormatUtil.format(
                                LogMessageConstant.CANNOT_USE_USER_WORDS,
                                e.getMessage()));
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
     * @param language {@link java.lang.String}
     * @param inputStream {@link java.io.InputStream}
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
            LoggerFactory.getLogger(getClass())
                    .warn(MessageFormatUtil.format(
                            LogMessageConstant.CANNOT_USE_USER_WORDS,
                            e.getMessage()));
        }
    }

    /**
     * Return path to the user words file if exists, otherwise null.
     *
     * @return {@link java.lang.String}
     */
    public final String getUserWordsFilePath() {
        return userWordsFile;
    }

    /**
     * Get path to provided tess data directory or return default one.
     *
     * @return {@link java.lang.String}
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
     * @return {@link java.lang.String}
     */
    public String identifyOSType() {
        String os = System.getProperty("os.name") == null
                ? System.getProperty("OS") : System.getProperty("os.name");
        return os.toLowerCase();
    }

    /**
     * Validate provided languages and
     * check if they exist in provided tess data directory.
     * @param languagesList {@link java.util.List}
     */
    public void validateLanguages(final List<String> languagesList) {
        String suffix = ".traineddata";
        if (languagesList.size() == 0) {
            if (!new File(getTessData()
                    + java.io.File.separatorChar + "eng" + suffix).exists()) {
                throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                        .setMessageParams("eng" + suffix, getTessData());
            }
        } else {
            for (String lang : languagesList) {
                if (!new File(getTessData()
                        + java.io.File.separatorChar + lang + suffix)
                        .exists()) {
                    throw new OCRException(OCRException.INCORRECT_LANGUAGE)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }

    /**
     * Create temporary file with given extension.
     *
     * @param extension {@link java.lang.String}
     * @return {@link java.io.File}
     */
    private File createTempFile(final String extension) {
        String tmpFileName = TesseractUtil.getTempDir()
                + UUID.randomUUID().toString() + extension;
        return new File(tmpFileName);
    }
}
