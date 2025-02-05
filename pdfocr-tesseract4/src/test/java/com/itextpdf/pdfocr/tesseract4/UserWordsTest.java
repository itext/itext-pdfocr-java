/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UserWordsTest extends IntegrationTestHelper {

    AbstractTesseract4OcrEngine tesseractReader;
    String testFileTypeName;
    private boolean isExecutableReaderType;

    public UserWordsTest(ReaderType type) {
        isExecutableReaderType = type.equals(ReaderType.EXECUTABLE);
        if (isExecutableReaderType) {
            testFileTypeName = "executable";
        } else {
            testFileTypeName = "lib";
        }
        tesseractReader = getTesseractReader(type);
    }

    @BeforeEach
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void testCustomUserWords() {
        String imgPath = TEST_IMAGES_DIRECTORY + "wierdwords.png";
        List<String> userWords = Arrays.<String>asList("he23llo", "qwetyrtyqpwe-rty");

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setLanguages(Arrays.asList("fra"));
        properties.setUserWords("fra", userWords);
        tesseractReader.setTesseract4OcrEngineProperties(properties);
        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
        Assertions.assertTrue(result.contains(userWords.get(0))
                || result.contains(userWords.get(1)));

        Assertions.assertTrue(tesseractReader.getTesseract4OcrEngineProperties()
                .getPathToUserWordsFile().endsWith(".user-words"));
    }

    @Test
    public void testCustomUserWordsWithListOfLanguages() {
        String imgPath = TEST_IMAGES_DIRECTORY + "bogusText.jpg";
        String expectedOutput = "B1adeb1ab1a";

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setLanguages(Arrays.asList("fra", "eng"));
        properties.setUserWords("eng", Arrays.<String>asList("b1adeb1ab1a"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
        result = result.replace("\n", "").replace("\f", "");
        result = result.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\u007E]", "");
        Assertions.assertTrue(result.startsWith(expectedOutput));

        Assertions.assertTrue(tesseractReader.getTesseract4OcrEngineProperties()
                .getPathToUserWordsFile().endsWith(".user-words"));
    }

    @Test
    public void testUserWordsWithLanguageNotInList() throws FileNotFoundException {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            String userWords = TEST_DOCUMENTS_DIRECTORY + "userwords.txt";
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setUserWords("spa", new FileInputStream(userWords));
            properties.setLanguages(new ArrayList<String>());
        });

        Assertions.assertEquals(MessageFormatUtil
                        .format(PdfOcrTesseract4ExceptionMessageConstant.LANGUAGE_IS_NOT_IN_THE_LIST, "spa"),
                exception.getMessage());
    }

    @Test
    public void testIncorrectLanguageForUserWordsAsList() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setUserWords("eng1", Arrays.<String>asList("word1", "word2"));
            properties.setLanguages(new ArrayList<String>());
        });

        Assertions.assertEquals(MessageFormatUtil
                        .format(PdfOcrTesseract4ExceptionMessageConstant.LANGUAGE_IS_NOT_IN_THE_LIST, "eng1"),
                exception.getMessage());
    }

    @Test
    public void testUserWordsWithDefaultLanguageNotInList()
            throws FileNotFoundException {
        String userWords = TEST_DOCUMENTS_DIRECTORY + "userwords.txt";
        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setUserWords("eng", new FileInputStream(userWords));
        properties.setLanguages(new ArrayList<String>());
        tesseractReader.setTesseract4OcrEngineProperties(properties);
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String expectedOutput = "619121";
        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
        Assertions.assertTrue(result.startsWith(expectedOutput));
    }

    @Test
    public void testUserWordsFileNotDeleted() {
        String userWords = TEST_DOCUMENTS_DIRECTORY + "userwords.txt";
        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setPathToUserWordsFile(userWords);
        properties.setLanguages(Arrays.<String>asList("eng"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        tesseractReader.doImageOcr(new File(imgPath));
        Assertions.assertTrue(new File(userWords).exists());
    }
}
