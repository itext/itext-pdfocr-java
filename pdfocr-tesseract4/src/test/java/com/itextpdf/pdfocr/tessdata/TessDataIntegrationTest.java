package com.itextpdf.pdfocr.tessdata;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfocr.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public abstract class TessDataIntegrationTest extends AbstractIntegrationTest {

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
    public void compareSpanishPNG() throws IOException, InterruptedException {
        String testName = "compareSpanishPNG";
        String filename = "scanned_spa_01";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + testFileTypeName +
                ".pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";

        List<String> languages = Arrays.<String>asList("spa", "spa_old");
        if (isExecutableReaderType) {
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPreprocessingImages(false));
        }

        // locate text by words
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setTextPositioning(TextPositioning.BY_WORDS));
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".png", resultPdfPath,
                languages,  DeviceCmyk.BLACK);

        try {
            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    TEST_DOCUMENTS_DIRECTORY, "diff_");
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
        String imgPath = TEST_IMAGES_DIRECTORY + "multipage.tiff";
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
    public void compareMultiLangImage() throws InterruptedException, java.io.IOException {
        String testName = "compareMultiLangImage";
        String filename = "multilang";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + "_" + testFileTypeName + ".pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";

        try {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setTextPositioning(TextPositioning.BY_WORDS);
            properties.setPathToTessData(getTessDataDirectory());
            properties.setPageSegMode(3);
            tesseractReader.setTesseract4OcrEngineProperties(properties);
            doOcrAndSavePdfToPath(tesseractReader,
                    TEST_IMAGES_DIRECTORY + filename + ".png", resultPdfPath,
                    Arrays.<String>asList("eng", "deu", "spa"), DeviceCmyk.BLACK);

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    TEST_DOCUMENTS_DIRECTORY, "diff_");
        } finally {
            Assert.assertEquals(TextPositioning.BY_WORDS,
                    tesseractReader.getTesseract4OcrEngineProperties().getTextPositioning());
            Assert.assertEquals(3, tesseractReader
                    .getTesseract4OcrEngineProperties().getPageSegMode().intValue());
        }
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 1)
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
                pdfPath, Arrays.asList("hin", "urd"), CAIRO_FONT_PATH);

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
    public void testGeorgianText() throws IOException {
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
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 1)
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
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben"), FREE_SANS_FONT_PATH));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 2)
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
        @LogMessage(messageTemplate = PdfOcrLogMessageConstant.COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER, count = 3)
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
                        Collections.<String>singletonList("chi_sim")),
                NOTO_SANS_SC_FONT_PATH);
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("chi_tra")),
                NOTO_SANS_SC_FONT_PATH);
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("chi_sim", "chi_tra")),
                NOTO_SANS_SC_FONT_PATH);
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
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Bengali"),
                FREE_SANS_FONT_PATH)
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
    public void testCustomUserWords() {
        String imgPath = TEST_IMAGES_DIRECTORY + "wierdwords.png";
        List<String> userWords = Arrays.<String>asList("he23llo", "qwetyrtyqpwe-rty");

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setLanguages(Arrays.asList("fra"));
        properties.setUserWords("fra", userWords);
        tesseractReader.setTesseract4OcrEngineProperties(properties);
        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
        Assert.assertTrue(result.contains(userWords.get(0))
                || result.contains(userWords.get(1)));

        Assert.assertTrue(tesseractReader.getTesseract4OcrEngineProperties()
                        .getPathToUserWordsFile().endsWith("fra.user-words"));
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
        Assert.assertTrue(result.startsWith(expectedOutput));

        Assert.assertTrue(tesseractReader.getTesseract4OcrEngineProperties()
                .getPathToUserWordsFile().endsWith("eng.user-words"));
    }

    @Test
    public void testUserWordsWithLanguageNotInList() throws FileNotFoundException {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.LANGUAGE_IS_NOT_IN_THE_LIST,
                        "spa"));
        String userWords = TEST_DOCUMENTS_DIRECTORY + "userwords.txt";
        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setUserWords("spa", new FileInputStream(userWords));
        properties.setLanguages(new ArrayList<String>());
    }

    @Test
    public void testIncorrectLanguageForUserWordsAsList() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.LANGUAGE_IS_NOT_IN_THE_LIST,
                        "eng1"));
        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setUserWords("eng1", Arrays.<String>asList("word1", "word2"));
        properties.setLanguages(new ArrayList<String>());
    }

    @Test
    public void testUserWordsWithDefaultLanguageNotInList()
            throws FileNotFoundException {
        String userWords = TEST_DOCUMENTS_DIRECTORY + "userwords.txt";
        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setUserWords("eng", new FileInputStream(userWords));
        properties.setLanguages(new ArrayList<String>());
    }

    /**
     * Do OCR for given image and compare result etxt file with expected one.
     */
    private boolean doOcrAndCompareTxtFiles(AbstractTesseract4OcrEngine tesseractReader,
            String imgPath, String expectedPath, List<String> languages) {
        String resultTxtFile = getTargetDirectory() + getImageName(imgPath, languages) + ".txt";
        doOcrAndSaveToTextFile(tesseractReader, imgPath, resultTxtFile, languages);
        return compareTxtFiles(expectedPath, resultTxtFile);
    }

    /**
     * Compare two text files using provided paths.
     */
    private boolean compareTxtFiles(String expectedFilePath, String resultFilePath) {
        boolean areEqual = true;
        try {
            List<String> expected = Files.readAllLines(java.nio.file.Paths.get(expectedFilePath));
            List<String> result = Files.readAllLines(java.nio.file.Paths.get(resultFilePath));

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
        } catch (IOException e) {
            areEqual = false;
            LOGGER.error(e.getMessage());
        }

        return areEqual;
    }
}
