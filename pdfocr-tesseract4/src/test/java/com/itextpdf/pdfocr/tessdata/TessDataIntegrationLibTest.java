/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.pdfocr.tessdata;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Timeout;

@Tag("IntegrationTest")
public class TessDataIntegrationLibTest extends TessDataIntegrationTest {
    public TessDataIntegrationLibTest() {
        super(ReaderType.LIB);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_DIRECTORY_CONTAINS_NON_ASCII_CHARACTERS)
    })
    @Test
    public void testTessDataWithNonAsciiPath() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class,
                // Throws exception for the tesseract lib test
                () -> doOcrAndGetTextUsingTessDataByNonAsciiPath());

        Assertions.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_DIRECTORY_CONTAINS_NON_ASCII_CHARACTERS,
                exception.getMessage());
    }

    @Timeout(value = 60000, unit = TimeUnit.MILLISECONDS)
    @Test
    public void textOutputFromHalftoneFile() {
        String imgPath = TEST_IMAGES_DIRECTORY + "halftone.jpg";
        String expected01 = "Silliness Enablers";
        String expected02 = "You dream it, we enable it";
        String expected03 = "QUANTITY";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("eng"));

        // correct result for a halftone input image
        Assertions.assertTrue(result.contains(expected01));
        Assertions.assertTrue(result.contains(expected02));
        Assertions.assertTrue(result.contains(expected03));
    }

    @Test
    public void compareInvoiceFrontThaiImage() throws InterruptedException, java.io.IOException {
        String testName = "compareInvoiceFrontThaiImage";
        String filename = "invoice_front_thai";

        //Tesseract for Java and Tesseract for .NET give different output
        //So we cannot use one reference pdf file for them
        String expectedPdfPathJava = TEST_DOCUMENTS_DIRECTORY + filename + "_" + testFileTypeName + "_java.pdf";
        String expectedPdfPathDotNet = TEST_DOCUMENTS_DIRECTORY + filename + "_" + testFileTypeName + "_dotnet.pdf";

        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + "_" + testFileTypeName + ".pdf";

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setTextPositioning(TextPositioning.BY_WORDS_AND_LINES);
        properties.setPathToTessData(getTessDataDirectory());
        properties.setLanguages(Arrays.asList("tha", "eng"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("tha", "eng"), Arrays.<String>asList(NOTO_SANS_THAI_FONT_PATH, NOTO_SANS_FONT_PATH), DeviceRgb.RED);
        boolean javaTest = new CompareTool().compareByContent(resultPdfPath, expectedPdfPathJava,
                getTargetDirectory(), "diff_") == null;
        boolean dotNetTest = new CompareTool().compareByContent(resultPdfPath, expectedPdfPathDotNet,
                getTargetDirectory(), "diff_") == null;

        Assertions.assertTrue(javaTest || dotNetTest);
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 2)
    })
    @Test
    public void compareThaiTextImage() throws InterruptedException, java.io.IOException {
        String testName = "compareThaiTextImage";
        String filename = "thai_01";

        //Tesseract for Java and Tesseract for .NET give different output
        //So we cannot use one reference pdf file for them
        String expectedPdfPathJava = TEST_DOCUMENTS_DIRECTORY + filename + "_" + testFileTypeName + "_java.pdf";
        String expectedPdfPathDotNet = TEST_DOCUMENTS_DIRECTORY + filename + "_" + testFileTypeName + "_dotnet.pdf";

        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + "_" + testFileTypeName + ".pdf";

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setTextPositioning(TextPositioning.BY_WORDS_AND_LINES);
        properties.setPathToTessData(getTessDataDirectory());
        properties.setLanguages(Arrays.asList("tha"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("tha"), Arrays.<String>asList(NOTO_SANS_THAI_FONT_PATH), DeviceRgb.RED);
        boolean javaTest = new CompareTool().compareByContent(resultPdfPath, expectedPdfPathJava,
                getTargetDirectory(), "diff_") == null;
        boolean dotNetTest = new CompareTool().compareByContent(resultPdfPath, expectedPdfPathDotNet,
                getTargetDirectory(), "diff_") == null;

        Assertions.assertTrue(javaTest || dotNetTest);
    }

}
