/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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

import com.itextpdf.io.util.FileUtil;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrEngineProperties;

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
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Properties that will be used by the {@link IOcrEngine}.
 */
public class Tesseract4OcrEngineProperties extends OcrEngineProperties {

    /**
     * Default suffix for user-word file.
     * (e.g. name: 'eng.user-words')
     */
    static final String DEFAULT_USER_WORDS_SUFFIX = "user-words";

    /**
     * Default language for OCR.
     */
    private static final String DEFAULT_LANGUAGE = "eng";

    /**
     * Path to directory with tess data.
     */
    private File tessDataDir;

    /**
     * Page Segmentation Mode.
     */
    private Integer pageSegMode;

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
    private String pathToUserWordsFile = null;

    /**
     * Creates a new {@link Tesseract4OcrEngineProperties} instance.
     */
    public Tesseract4OcrEngineProperties() {
    }

    /**
     * Creates a new {@link Tesseract4OcrEngineProperties} instance
     * based on another {@link Tesseract4OcrEngineProperties} instance (copy
     * constructor).
     *
     * @param other the other {@link Tesseract4OcrEngineProperties} instance
     */
    public Tesseract4OcrEngineProperties(Tesseract4OcrEngineProperties other) {
        super(other);
        this.tessDataDir = other.tessDataDir;
        this.pageSegMode = other.pageSegMode;
        this.preprocessingImages = other.preprocessingImages;
        this.textPositioning = other.textPositioning;
        this.pathToUserWordsFile = other.pathToUserWordsFile;
    }

    /**
     * Gets default language for ocr.
     *
     * @return default language - "eng"
     */
    public final String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    /**
     * Gets default user words suffix.
     *
     * @return default suffix for user words files
     */
    public final String getDefaultUserWordsSuffix() {
        return DEFAULT_USER_WORDS_SUFFIX;
    }

    /**
     * Gets path to directory with tess data.
     *
     * @return path to directory with tess data
     */
    public final File getPathToTessData() {
        return tessDataDir;
    }

    /**
     * Sets path to directory with tess data.
     *
     * @param tessData path to train directory as {@link java.io.File}
     * @return the {@link Tesseract4OcrEngineProperties} instance
     * @throws Tesseract4OcrException if path to tess data directory is
     * null or empty, or provided directory does not exist? or it is not
     * a directory
     */
    public final Tesseract4OcrEngineProperties setPathToTessData(
            final File tessData) {
        if (tessData == null
                || !FileUtil.directoryExists(tessData.getAbsolutePath())) {
            throw new Tesseract4OcrException(
                    Tesseract4OcrException
                            .PATH_TO_TESS_DATA_DIRECTORY_IS_INVALID);
        }
        this.tessDataDir = tessData;

        return this;
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
     * @return the {@link Tesseract4OcrEngineProperties} instance
     */
    public final Tesseract4OcrEngineProperties setPageSegMode(
            final Integer mode) {
        pageSegMode = mode;
        return this;
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
     * @return the {@link Tesseract4OcrEngineProperties} instance
     */
    public final Tesseract4OcrEngineProperties setPreprocessingImages(
            final boolean preprocess) {
        preprocessingImages = preprocess;
        return this;
    }

    /**
     * Defines the way text is retrieved from tesseract output using
     * {@link TextPositioning}.
     *
     * @return the way text is retrieved
     */
    public final TextPositioning getTextPositioning() {
        return textPositioning;
    }

    /**
     * Defines the way text is retrieved from tesseract output
     * using {@link TextPositioning}.
     *
     * @param positioning the way text is retrieved
     * @return the {@link Tesseract4OcrEngineProperties} instance
     */
    public final Tesseract4OcrEngineProperties setTextPositioning(
            final TextPositioning positioning) {
        textPositioning = positioning;
        return this;
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
     * @return the {@link Tesseract4OcrEngineProperties} instance
     * @throws Tesseract4OcrException if one of given languages wasn't specified in the
     * list of required languages for OCR using
     */
    public Tesseract4OcrEngineProperties setUserWords(final String language,
            final List<String> userWords)
            throws Tesseract4OcrException {
        setPathToUserWordsFile(null);
        if (userWords != null && userWords.size() > 0) {
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
                                Tesseract4LogMessageConstant.CANNOT_USE_USER_WORDS,
                                e.getMessage()));
            }
        }
        return this;
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
     * @throws Tesseract4OcrException if one of given languages wasn't specified
     * in the list of required languages for OCR using
     * {@link Tesseract4OcrEngineProperties#setLanguages(List)} method
     * @return the {@link Tesseract4OcrEngineProperties} instance
     */
    public Tesseract4OcrEngineProperties setUserWords(final String language,
            final InputStream inputStream) throws Tesseract4OcrException {
        setPathToUserWordsFile(null);
        String userWordsFileName = TesseractOcrUtil.getTempDir()
                + java.io.File.separatorChar
                + language + "." + DEFAULT_USER_WORDS_SUFFIX;
        if (!getLanguages().contains(language)) {
            if (DEFAULT_LANGUAGE.equals(language.toLowerCase())) {
                List<String> languagesList = getLanguages();
                languagesList.add(language);
                setLanguages(languagesList);
            } else {
                throw new Tesseract4OcrException(
                        Tesseract4OcrException.LANGUAGE_IS_NOT_IN_THE_LIST)
                        .setMessageParams(language);
            }
        }

        try (OutputStreamWriter writer =
                new FileWriter(userWordsFileName)) {
            Reader reader = new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8);
            int data;
            while ((data = reader.read()) != -1) {
                writer.write(data);
            }
            writer.write(System.lineSeparator());
            setPathToUserWordsFile(userWordsFileName);
        } catch (IOException e) {
            setPathToUserWordsFile(null);
            LoggerFactory.getLogger(getClass())
                    .warn(MessageFormatUtil.format(
                            Tesseract4LogMessageConstant.CANNOT_USE_USER_WORDS,
                            e.getMessage()));
        }
        return this;
    }

    /**
     * Returns path to the user words file.
     *
     * @return path to user words file as {@link java.lang.String} if it
     * exists, otherwise - null
     */
    public final String getPathToUserWordsFile() {
        return pathToUserWordsFile;
    }

    /**
     * Sets path to the user words file.
     *
     * @param pathToUserWordsFile path to user words file
     *                        as {@link java.lang.String}
     * @return the {@link Tesseract4OcrEngineProperties} instance
     */
    public final Tesseract4OcrEngineProperties setPathToUserWordsFile(
            String pathToUserWordsFile) {
        this.pathToUserWordsFile = pathToUserWordsFile;
        return this;
    }
}
