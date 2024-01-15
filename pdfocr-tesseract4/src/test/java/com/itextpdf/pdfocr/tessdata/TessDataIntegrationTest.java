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

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TessDataIntegrationTest extends IntegrationTestHelper {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TessDataIntegrationTest.class);

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    AbstractTesseract4OcrEngine tesseractReader;
    String testFileTypeName;
    private boolean isExecutableReaderType;

    public TessDataIntegrationTest(ReaderType type) {
        isExecutableReaderType = type.equals(ReaderType.EXECUTABLE);
        if (isExecutableReaderType) {
            testFileTypeName = "executable";
        } else {
            testFileTypeName = "lib";
        }
        tesseractReader = getTesseractReader(type);
    }

    @Before
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void textGreekText() {
        String imgPath = TEST_IMAGES_DIRECTORY + "greek_01.jpg";
        File file = new File(imgPath);
        String expected = "ΟΜΟΛΟΓΙΑ";

        if (isExecutableReaderType) {
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPreprocessingImages(false));
        }
        String real = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("ell"), NOTO_SANS_FONT_PATH);
        // correct result with specified greek language
        Assert.assertTrue(real.contains(expected));
    }

    @Test
    public void textJapaneseText() {
        String imgPath = TEST_IMAGES_DIRECTORY + "japanese_01.png";
        File file = new File(imgPath);
        String expected = "日 本 語\n文法";

        // correct result with specified japanese language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("jpn"), KOSUGI_FONT_PATH));
    }

    @Test
    public void testFrench() {
        String imgPath = TEST_IMAGES_DIRECTORY + "french_01.png";
        File file = new File(imgPath);
        String expectedFr = "RESTEZ\nCALME\nPARLEZ EN\nFRANÇAIS";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("fra")).endsWith(expectedFr));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader,file,
                Collections.<String>singletonList("eng")).endsWith(expectedFr));
        Assert.assertNotEquals(expectedFr,
                getTextFromPdf(tesseractReader,file, Collections.<String>singletonList("spa")));
        Assert.assertNotEquals(expectedFr,
                getTextFromPdf(tesseractReader,file, new ArrayList<String>()));
    }

    @Test
    public void testSpanishPNG() throws IOException {
        String testName = "compareSpanishPNG";
        String filename = "scanned_spa_01";
        String expectedText1 = "¿Y SI ENSAYARA COMO ACTUAR?";
        String expectedText2 = "¿Y SI ENSAYARA ACTUAR?";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName
                + "_" + testFileTypeName + ".pdf";

        List<String> languages = Arrays.<String>asList("spa", "spa_old");
        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        if (isExecutableReaderType) {
            properties.setPreprocessingImages(false);
        }

        // locate text by words
        properties.setTextPositioning(TextPositioning.BY_WORDS);
        properties.setLanguages(languages);
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextColor(DeviceCmyk.BLACK);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader, ocrPdfCreatorProperties);
        try (PdfWriter pdfWriter = getPdfWriter(resultPdfPath)) {
            ocrPdfCreator.createPdf(Collections.<File>singletonList(
                    new File(TEST_IMAGES_DIRECTORY + filename + ".png")),
                    pdfWriter)
                    .close();
        }

        try {
            String result = getTextFromPdfLayer(resultPdfPath, null, 1)
                    .replace("\n", " ");
            Assert.assertTrue(result.contains(expectedText1)
                    || result.contains(expectedText2));
        } finally {
            Assert.assertEquals(TextPositioning.BY_WORDS,
                    tesseractReader.getTesseract4OcrEngineProperties().getTextPositioning());
        }
    }

    @Test
    public void textGreekOutputFromTxtFile() {
        String imgPath = TEST_IMAGES_DIRECTORY + "greek_01.jpg";
        String expected = "ΟΜΟΛΟΓΙΑ";

        if (isExecutableReaderType) {
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPreprocessingImages(false));
        }
        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("ell"));
        // correct result with specified greek language
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void textJapaneseOutputFromTxtFile() {
        String imgPath = TEST_IMAGES_DIRECTORY + "japanese_01.png";
        String expected = "日本語文法";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("jpn"));

        result = result.replaceAll("[\f\n]", "");
        // correct result with specified japanese language
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void testFrenchOutputFromTxtFile() {
        String imgPath = TEST_IMAGES_DIRECTORY + "french_01.png";
        String expectedFr = "RESTEZ\nCALME\nPARLEZ EN\nFRANÇAIS";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("fra"));
        result = result.replaceAll("(?:\\n\\f)+", "").trim();
        result = result.replaceAll("\\n\\n", "\n").trim();
        // correct result with specified spanish language
        Assert.assertTrue(result.endsWith(expectedFr));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(
                getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("eng")).endsWith(expectedFr));
        Assert.assertNotEquals(expectedFr,
                getRecognizedTextFromTextFile(tesseractReader, imgPath,
                        Collections.<String>singletonList("spa")));
        Assert.assertNotEquals(expectedFr,
                getRecognizedTextFromTextFile(tesseractReader, imgPath,
                        new ArrayList<String>()));
    }

    @Test
    public void testArabicOutputFromTxtFile() {
        String imgPath = TEST_IMAGES_DIRECTORY + "arabic_02.png";
        // First sentence
        String expected = "اللغة العربية";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("ara"));
        // correct result with specified arabic language
        Assert.assertTrue(result.startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order

        String engResult = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("eng"));
        Assert.assertFalse(engResult.startsWith(expected));
        String spaResult = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("spa"));
        Assert.assertFalse(spaResult.startsWith(expected));
        String langNotSpecifiedResult = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                new ArrayList<String>());
        Assert.assertFalse(langNotSpecifiedResult.startsWith(expected));
    }

    @Test
    public void testGermanAndCompareTxtFiles() {
        String imgPath = TEST_IMAGES_DIRECTORY + "german_01.jpg";
        String expectedTxt = TEST_DOCUMENTS_DIRECTORY + "german_01" + testFileTypeName + ".txt";

        boolean result = doOcrAndCompareTxtFiles(tesseractReader, imgPath, expectedTxt,
                Collections.<String>singletonList("deu"));
        Assert.assertTrue(result);
    }

    @Test
    public void testMultipageTiffAndCompareTxtFiles() {
        String imgPath = TEST_IMAGES_DIRECTORY + "multîpage.tiff";
        String expectedTxt = TEST_DOCUMENTS_DIRECTORY + "multipage_" + testFileTypeName + ".txt";

        boolean result = doOcrAndCompareTxtFiles(tesseractReader, imgPath, expectedTxt,
                Collections.<String>singletonList("eng"));
        Assert.assertTrue(result);
    }

    @Test
    public void testGermanWithTessData() {
        String imgPath = TEST_IMAGES_DIRECTORY + "german_01.jpg";
        File file = new File(imgPath);
        String expectedGerman = "Das Geheimnis\ndes Könnens\nliegt im Wollen.";

        String res = getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("deu"));
        // correct result with specified spanish language
        Assert.assertEquals(expectedGerman, res);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng")));
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("fra")));
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, new ArrayList<String>()));
    }

    @Test
    public void testArabicTextWithEng() {
        String imgPath = TEST_IMAGES_DIRECTORY + "arabic_01.jpg";
        File file = new File(imgPath);
        String expected = "الحية. والضحك؛ والحب\nlive, laugh, love";

        String result = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("ara", "eng"), CAIRO_FONT_PATH);
        // correct result with specified arabic+english languages
        Assert.assertEquals(expected, result.replaceAll("[?]", ""));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"), CAIRO_FONT_PATH));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, new ArrayList<String>(),
                        CAIRO_FONT_PATH));
    }

    @Test
    public void testArabicText() {
        String imgPath = TEST_IMAGES_DIRECTORY + "arabic_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "اللغة العربية";

        // correct result with specified arabic language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ara"), CAIRO_FONT_PATH));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"), CAIRO_FONT_PATH));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("spa"), CAIRO_FONT_PATH));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<String>(), CAIRO_FONT_PATH));
    }

    @Test
    public void compareMultiLangImage() throws InterruptedException, IOException {
        String testName = "compareMultiLangImage";
        String filename = "multilang";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + "_" + testFileTypeName + ".pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + "_" + testFileTypeName + ".pdf";

        try {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setTextPositioning(TextPositioning.BY_WORDS);
            properties.setPathToTessData(getTessDataDirectory());
            properties.setPageSegMode(3);
            tesseractReader.setTesseract4OcrEngineProperties(properties);
            doOcrAndSavePdfToPath(tesseractReader,
                    TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                    Arrays.<String>asList("eng", "deu", "spa"), DeviceCmyk.BLACK);

            Assert.assertNull(new CompareTool().compareByContent(resultPdfPath, expectedPdfPath,
                    getTargetDirectory(), "diff_"));
        } finally {
            Assert.assertEquals(TextPositioning.BY_WORDS,
                    tesseractReader.getTesseract4OcrEngineProperties().getTextPositioning());
            Assert.assertEquals(3, tesseractReader
                    .getTesseract4OcrEngineProperties().getPageSegMode().intValue());
        }
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 12)
    })
    @Test
    public void testHindiTextWithUrdu() throws IOException {
        String testName = "testHindiTextWithUrdu";
        String imgPath = TEST_IMAGES_DIRECTORY + "hindi_01.jpg";
        File file = new File(imgPath);
        String pdfPath = getTargetDirectory() + testName + ".pdf";

        String expectedHindi = "हिन्दुस्तानी";
        String expectedUrdu = "وتالی";

        doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                pdfPath, Arrays.asList("hin", "urd"),
                Collections.singletonList(CAIRO_FONT_PATH));

        String resultWithoutActualText = getTextFromPdfLayer(pdfPath, null, 1);
        // because of provided font only urdu will be displayed correctly
        Assert.assertTrue(resultWithoutActualText.contains(expectedUrdu));
        Assert.assertFalse(resultWithoutActualText.contains(expectedHindi));

        String resultWithActualText = getTextFromPdfLayerUsingActualText(pdfPath, null, 1);
        // actual text should contain all text
        Assert.assertTrue(resultWithActualText.contains(expectedUrdu));
        Assert.assertTrue(resultWithActualText.contains(expectedHindi));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER)
    }, ignore = true)
    @Test
    public void testHindiTextWithUrduActualTextWithIncorrectFont() throws IOException {
        String testName = "testHindiTextWithUrduActualTextWithIncorrectFont";
        String imgPath = TEST_IMAGES_DIRECTORY + "hindi_01.jpg";
        File file = new File(imgPath);
        String pdfPath = getTargetDirectory() + testName + ".pdf";

        String expectedHindi = "हिन्दुस्तानी";
        String expectedUrdu = "وتالی";

        doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                pdfPath, Arrays.asList("hin", "urd"), null, null);

        String resultWithoutActualText = getTextFromPdfLayer(pdfPath, null, 1);
        // because of provided font only urdu will be displayed correctly
        Assert.assertFalse(resultWithoutActualText.contains(expectedUrdu));
        Assert.assertFalse(resultWithoutActualText.contains(expectedHindi));

        String resultWithActualText = getTextFromPdfLayerUsingActualText(pdfPath, null, 1);
        // actual text should contain all text
        Assert.assertTrue(resultWithActualText.contains(expectedUrdu));
        Assert.assertTrue(resultWithActualText.contains(expectedHindi));
    }

    @Test
    public void testHindiTextWithEng() {
        String imgPath = TEST_IMAGES_DIRECTORY + "hindi_02.jpg";
        File file = new File(imgPath);

        String expected = "मानक हनिदी\nHindi";

        // correct result with specified arabic+english languages
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("hin", "eng"), NOTO_SANS_FONT_PATH));

        // incorrect result without specified english language
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("hin"), NOTO_SANS_FONT_PATH));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"), NOTO_SANS_FONT_PATH));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<String>(), NOTO_SANS_FONT_PATH));
    }

    @Test
    public void testGeorgianText() {
        String imgPath = TEST_IMAGES_DIRECTORY + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        String result = getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("kat"), FREE_SANS_FONT_PATH);
        // correct result with specified georgian+eng language
        Assert.assertEquals(expected, result);
        result = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("kat", "kat_old"), FREE_SANS_FONT_PATH);
        Assert.assertEquals(expected, result);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 6)
    })
    @Test
    public void testGeorgianActualTextWithDefaultFont() throws IOException {
        String testName = "testGeorgianActualTextWithDefaultFont";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        String imgPath = TEST_IMAGES_DIRECTORY + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                pdfPath, Collections.<String>singletonList("kat"), null, null);

        String resultWithoutActualText = getTextFromPdfLayer(pdfPath, null, 1);
        Assert.assertNotEquals(expected, resultWithoutActualText);

        String resultWithActualText = getTextFromPdfLayerUsingActualText(pdfPath, null, 1);
        Assert.assertEquals(expected, resultWithActualText);
    }

    @Test
    public void testBengali() {
        String imgPath = TEST_IMAGES_DIRECTORY + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে\nশখো";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_WORDS));
        // correct result with specified spanish language
        String result = getTextFromPdf(tesseractReader, file, 1,
                Collections.<String>singletonList("ben"),
                Arrays.<String>asList(FREE_SANS_FONT_PATH, KOSUGI_FONT_PATH));
        Assert.assertEquals(expected, result);

        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben"), FREE_SANS_FONT_PATH));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 8)
    })
    @Test
    public void testBengaliActualTextWithDefaultFont() throws IOException {
        String testName = "testBengaliActualTextWithDefaultFont";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        String imgPath = TEST_IMAGES_DIRECTORY + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে\nশখো";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_WORDS));

        doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                pdfPath, Collections.<String>singletonList("ben"), null, null);

        String resultWithoutActualText = getTextFromPdfLayer(pdfPath, null, 1);
        Assert.assertNotEquals(expected, resultWithoutActualText);

        String resultWithActualText = getTextFromPdfLayerUsingActualText(pdfPath, null, 1);
        Assert.assertEquals(expected, resultWithActualText);
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 6)
    })
    @Test
    public void testChinese() {
        String imgPath = TEST_IMAGES_DIRECTORY + "chinese_01.jpg";
        File file = new File(imgPath);
        String expected = "你 好\nni hao";

        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("chi_sim", "chi_tra"),
                NOTO_SANS_SC_FONT_PATH));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("chi_sim"),
                NOTO_SANS_SC_FONT_PATH));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("chi_tra"),
                NOTO_SANS_SC_FONT_PATH));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file,
                        Collections.<String>singletonList("chi_sim")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("chi_tra")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("chi_sim", "chi_tra")));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file, new ArrayList<String>())
                .contains(expected));
    }

    @Test
    public void testSpanishWithTessData() {
        String imgPath = TEST_IMAGES_DIRECTORY + "spanish_01.jpg";
        File file = new File(imgPath);
        String expectedSpanish = "Aquí\nhablamos\nespañol";

        // correct result with specified spanish language
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("spa")));
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("spa", "eng")));
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("eng", "spa")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng")));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, new ArrayList<String>()));
    }

    @Test
    public void testBengaliScript() {
        String imgPath = TEST_IMAGES_DIRECTORY + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file, 1,
                Collections.<String>singletonList("Bengali"),
                Arrays.<String>asList(FREE_SANS_FONT_PATH, KOSUGI_FONT_PATH))
                .startsWith(expected));
    }

    @Test
    public void testGeorgianTextWithScript() {
        String imgPath = TEST_IMAGES_DIRECTORY + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Georgian"),
                FREE_SANS_FONT_PATH)
                .startsWith(expected));
    }

    @Test
    public void testJapaneseScript() {
        String imgPath = TEST_IMAGES_DIRECTORY + "japanese_01.png";
        File file = new File(imgPath);
        String expected = "日 本 語\n文法";

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
        // correct result with specified japanese language
        String result = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("Japanese"), KOSUGI_FONT_PATH);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testTargetDirectoryWithNonAsciiPath() {
        String imgPath = TEST_IMAGES_DIRECTORY + "german_01.jpg";
        String expectedTxt = TEST_DOCUMENTS_DIRECTORY + "german_01" + testFileTypeName + ".txt";
        List<String> languages = Collections.<String>singletonList("deu");
        String resultTxtFile = getNonAsciiTargetDirectory() + getImageName(imgPath, languages) + ".txt";
        doOcrAndSaveToTextFile(tesseractReader, imgPath, resultTxtFile, languages);

        boolean result = compareTxtFiles(expectedTxt, resultTxtFile);
        Assert.assertTrue(result);
    }

    @Test
    public void testThai03ImageWithImprovedHocrParsing() {

        String[] expected = {"บ๊อบสตรอเบอรีออดิชั่นธัม โมเนิร์สเซอรี่",
                "ศากยบุตร เอเซีย",
                "หน่อมแน้ม เวอร์เบอร์เกอร์แชมป์"};

        String imgPath = TEST_IMAGES_DIRECTORY + "thai_03.jpg";
        File file = new File(imgPath);

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setTextPositioning(TextPositioning.BY_WORDS_AND_LINES);
        properties.setUseTxtToImproveHocrParsing(true);
        properties.setMinimalConfidenceLevel(80);
        properties.setPathToTessData(new File(LANG_TESS_DATA_DIRECTORY));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        String pdfText = getTextFromPdf(tesseractReader, file, 1,
                Arrays.<String>asList("tha"), Arrays.<String>asList(NOTO_SANS_THAI_FONT_PATH, NOTO_SANS_FONT_PATH));

        for (String e : expected) {
            Assert.assertTrue(pdfText.contains(e));
        }
    }

    /**
     * Do OCR and retrieve text from the first page of result PDF document
     * using tess data placed by path with non ASCII characters.
     * @return {@link java.lang.String}
     */
    protected String doOcrAndGetTextUsingTessDataByNonAsciiPath() {
        String imgPath = TEST_IMAGES_DIRECTORY + "georgian_01.jpg";
        File file = new File(imgPath);

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File(NON_ASCII_TESS_DATA_DIRECTORY)));

        return getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Georgian"),
                FREE_SANS_FONT_PATH);
    }

    /**
     * Do OCR for given image and compare result text file with expected one.
     */
    private boolean doOcrAndCompareTxtFiles(AbstractTesseract4OcrEngine tesseractReader,
            String imgPath, String expectedPath, List<String> languages) {
        String resultTxtFile = getTargetDirectory() + getImageName(imgPath, languages) + ".txt";
        doOcrAndSaveToTextFile(tesseractReader, imgPath, resultTxtFile, languages);
        return compareTxtFiles(expectedPath, resultTxtFile);
    }

    /**
     * Compare two arrays of text lines.
     */
    private boolean compareTxtLines(List<String> expected, List<String> result) {
        boolean areEqual = true;
        if (expected.size() != result.size()) {
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {
            String exp = expected.get(i)
                    .replace("\n", "")
                    .replace("\f", "");
            exp = exp.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\u007E]", "");
            String res = result.get(i)
                    .replace("\n", "")
                    .replace("\f", "");
            res = res.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\u007E]", "");
            if (expected.get(i) == null || result.get(i) == null) {
                areEqual = false;
                break;
            } else if (!exp.equals(res)) {
                areEqual = false;
                break;
            }
        }
        return areEqual;
    }

    /**
     * Compare two text files using provided paths.
     */
    private boolean compareTxtFiles(String expectedFilePath, String resultFilePath) {
        boolean areEqual = true;
        try {
            List<String> expected = Files.readAllLines(java.nio.file.Paths.get(expectedFilePath));
            List<String> result = Files.readAllLines(java.nio.file.Paths.get(resultFilePath));
            areEqual = compareTxtLines(expected, result);
        } catch (IOException e) {
            areEqual = false;
            LOGGER.error(e.getMessage());
        }
        return areEqual;
    }
}
