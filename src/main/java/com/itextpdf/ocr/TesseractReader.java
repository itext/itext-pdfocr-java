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
 * The implementation of {@link IOcrReader}.
 *
 * This class provides possibilities to perform OCR, to read data from input
 * files and to return contained text in the required format.
 * Also there are possibilities to use features of "tesseract"
 * (optical character recognition engine for various operating systems).
 */
public abstract class TesseractReader implements IOcrReader {

    /**
     * Default language for OCR.
     */
    public static final String DEFAULT_LANGUAGE = "eng";

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
     * True by default.
     */
    private boolean preprocessingImages = true;

    /**
     * Defines the way text is retrieved from tesseract output.
     * Default text positioning is by lines.
     */
    private TextPositioning textPositioning = TextPositioning.BY_LINES;

    /**
     * Path to the file containing user words.
     * Each word should be on a new line,
     * file should end with a newline character.
     */
    private String userWordsFile = null;

    /**
     * Performs tesseract OCR using command line tool
     * or a wrapper for Tesseract OCR API.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFiles {@link java.util.List} of output files
     *                                          (one per each page)
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     * @param pageNumber number of page to be processed
     */
    public abstract void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat,
            int pageNumber);

    /**
     * Performs tesseract OCR for the first (or for the only) image page.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFiles {@link java.util.List} of output files
     *                                          (one per each page)
     * @param outputFormat selected {@link IOcrReader.OutputFormat} for
     *                     tesseract
     */
    public void doTesseractOcr(File inputImage,
            List<File> outputFiles, OutputFormat outputFormat) {
        doTesseractOcr(inputImage, outputFiles, outputFormat, 1);
    }

    /**
     * Sets list of languages to be recognized in provided images.
     *
     * @param requiredLanguages {@link java.util.List} of languages in string
     *                                               format
     */
    public final void setLanguages(final List<String> requiredLanguages) {
        languages = Collections.<String>unmodifiableList(requiredLanguages);
    }

    /**
     * Gets list of languages required for provided images.
     *
     * @return {@link java.util.List} of languages
     */
    public final List<String> getLanguagesAsList() {
        return new ArrayList<String>(languages);
    }

    /**
     * Gets list of languages concatenated with "+" symbol to a string
     * in format required by tesseract.
     * @return {@link java.lang.String} of concatenated languages
     */
    public final String getLanguagesAsString() {
        if (getLanguagesAsList().size() > 0) {
            return String.join("+", getLanguagesAsList());
        } else {
            return DEFAULT_LANGUAGE;
        }
    }

    /**
     * Gets path to directory with tess data.
     *
     * @return path to directory with tess data
     */
    public final String getPathToTessData() {
        return tessDataDir;
    }

    /**
     * Sets path to directory with tess data.
     *
     * @param tessData path to train directory as {@link java.lang.String}
     */
    public final void setPathToTessData(final String tessData) {
        tessDataDir = tessData;
    }

    /**
     * Gets Page Segmentation Mode.
     *
     * @return psm mode as {@link java.lang.Integer}
     */
    public final Integer getPageSegMode() {
        return pageSegMode;
    }

    /**
     * Sets Page Segmentation Mode.
     * More detailed explanation about psm modes could be found
     * here https://github.com/tesseract-ocr/tesseract/blob/master/doc/tesseract.1.asc#options
     *
     * @param mode psm mode as {@link java.lang.Integer}
     */
    public final void setPageSegMode(final Integer mode) {
        pageSegMode = mode;
    }

    /**
     * Get type of current OS.
     *
     * @return os type as {@link java.lang.String}
     */
    public final String getOsType() {
        return osType;
    }

    /**
     * Sets type of current OS.
     *
     * @param os os type as {@link java.lang.String}
     */
    public final void setOsType(final String os) {
        osType = os;
    }

    /**
     * Checks whether image preprocessing is needed.
     *
     * @return true if images need to be preprocessed, otherwise - false
     */
    public final boolean isPreprocessingImages() {
        return preprocessingImages;
    }

    /**
     * Sets true if image preprocessing is needed.
     *
     * @param preprocess true if images need to be preprocessed,
     *                   otherwise - false
     */
    public final void setPreprocessingImages(final boolean preprocess) {
        preprocessingImages = preprocess;
    }

    /**
     * Defines the way text is retrieved from tesseract output using
     * {@link IOcrReader.TextPositioning}.
     *
     * @return the way text is retrieved
     */
    public final TextPositioning getTextPositioning() {
        return textPositioning;
    }

    /**
     * Defines the way text is retrieved from tesseract output
     * using {@link IOcrReader.TextPositioning}.
     *
     * @param positioning the way text is retrieved
     */
    public final void setTextPositioning(final TextPositioning positioning) {
        textPositioning = positioning;
    }

    /**
     * Reads data from the provided input image file and returns retrieved data
     * in the format described below.
     *
     * @param input input image {@link java.io.File}
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
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
     * Reads data from the provided input image file and returns retrieved data
     * as string.
     *
     * @param input input image {@link java.io.File}
     * @param outputFormat {@link IOcrReader.OutputFormat} for the result
     *                     returned by {@link IOcrReader}
     * @return OCR result as a {@link java.lang.String} that is
     * returned after processing the given image
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
     * Using provided list of words there will be created
     * temporary file containing words (one per line) which
     * ends with a new line character. Train data for provided language
     * should exist in specified tess data directory.
     *
     * @param language language as {@link java.lang.String}, tessdata for
     *                 this languages has to exist in tess data directory
     * @param userWords {@link java.util.List} of custom words
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
                                LogMessageConstant.CannotUseUserWords,
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
     * @param language language as {@link java.lang.String}, tessdata for
     *                 this languages has to exist in tess data directory
     * @param inputStream custom user words as {@link java.io.InputStream}
     * @throws OcrException if one of given languages wasn't specified in the
     * list of required languages for OCR using
     * {@link TesseractReader#setLanguages(List)} method
     */
    public void setUserWords(final String language,
            final InputStream inputStream) throws OcrException {
        String userWordsFileName = TesseractUtil.getTempDir()
                + java.io.File.separatorChar
                + language + "." + DEFAULT_USER_WORDS_SUFFIX;
        if (!getLanguagesAsList().contains(language)) {
            if (DEFAULT_LANGUAGE.equals(language.toLowerCase())) {
                List<String> languagesList = getLanguagesAsList();
                languagesList.add(language);
                setLanguages(languagesList);
            } else {
                throw new OcrException(
                        OcrException.LanguageIsNotInTheList)
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
                            LogMessageConstant.CannotUseUserWords,
                            e.getMessage()));
        }
    }

    /**
     * Returns path to the user words file.
     *
     * @return path to user words file as {@link java.lang.String} if it
     * exists, otherwise - null
     */
    public final String getUserWordsFilePath() {
        return userWordsFile;
    }

    /**
     * Checks current os type.
     *
     * @return boolean true is current os is windows, otherwise - false
     */
    public boolean isWindows() {
        return getOsType().toLowerCase().contains("win");
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
     * @throws OcrException if tess data wasn't found for one of the
     * languages from the provided list
     */
    public void validateLanguages(final List<String> languagesList)
            throws OcrException {
        String suffix = ".traineddata";
        if (languagesList.size() == 0) {
            if (!new File(getTessData()
                    + java.io.File.separatorChar
                    + DEFAULT_LANGUAGE + suffix).exists()) {
                throw new OcrException(OcrException.IncorrectLanguage)
                        .setMessageParams(DEFAULT_LANGUAGE + suffix,
                                getTessData());
            }
        } else {
            for (String lang : languagesList) {
                if (!new File(getTessData()
                        + java.io.File.separatorChar + lang + suffix)
                        .exists()) {
                    throw new OcrException(OcrException.IncorrectLanguage)
                            .setMessageParams(lang + suffix, getTessData());
                }
            }
        }
    }

    /**
     * Reads data from the provided input image file.
     *
     * @param input input image {@link java.io.File}
     * @param outputFormat {@link OutputFormat} for the result returned
     *                                         by {@link IOcrReader}
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
                            LogMessageConstant.CannotOcrInputFile,
                            e.getMessage()));
        }

        Map<String, Map<Integer, List<TextInfo>>> result =
                new LinkedHashMap<String, Map<Integer, List<TextInfo>>>();
        result.put(data.toString(), imageData);
        return result;
    }

    /**
     * Gets path to provided tess data directory.
     *
     * @return path to provided tess data directory as
     * {@link java.lang.String}, otherwise - the default one
     * @throws OcrException if path to tess data directory is empty or null
     */
    String getTessData() throws OcrException {
        if (getPathToTessData() != null && !getPathToTessData().isEmpty()) {
            return getPathToTessData();
        } else {
            throw new OcrException(OcrException.CannotFindPathToTessDataDirectory);
        }
    }

    /**
     * Creates a temporary file with given extension.
     *
     * @param extension file extesion for a new file {@link java.lang.String}
     * @return a new created {@link java.io.File} instance
     */
    private File createTempFile(final String extension) {
        String tmpFileName = TesseractUtil.getTempDir()
                + UUID.randomUUID().toString() + extension;
        return new File(tmpFileName);
    }
}
