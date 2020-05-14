package com.itextpdf.ocr.tessdata;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.ocr.AbstractIntegrationTest;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
import com.itextpdf.ocr.OcrException;
import com.itextpdf.ocr.TesseractExecutableReader;
import com.itextpdf.ocr.TesseractLibReader;
import com.itextpdf.ocr.TesseractReader;
import com.itextpdf.ocr.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class TessDataIntegrationTest extends AbstractIntegrationTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    TesseractReader tesseractReader;
    String parameter;

    @Before
    public void initTessDataPath() {
        tesseractReader.setPreprocessingImages(true);
        tesseractReader.setPathToTessData(getTessDataDirectory());
        tesseractReader.setLanguages(new ArrayList<String>());
        tesseractReader.setUserWords("eng", new ArrayList<String>());
    }

    public TessDataIntegrationTest(String type) {
        parameter = type;
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void textGreekText() {
        String imgPath = testImagesDirectory + "greek_01.jpg";
        File file = new File(imgPath);
        String expected = "ΟΜΟΛΟΓΙΑ";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }
        String real = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("ell"), notoSansFontPath);
        // correct result with specified greek language
        Assert.assertTrue(real.contains(expected));
    }

    @Test
    public void textJapaneseText() {
        String imgPath = testImagesDirectory + "japanese_01.png";
        File file = new File(imgPath);
        String expected = "日 本 語\n文法";

        // correct result with specified japanese language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("jpn"), kosugiFontPath));
    }

    @Test
    public void testFrench() {
        String imgPath = testImagesDirectory + "french_01.png";
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
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String filename = "scanned_spa_01";
        String expectedPdfPath = testDocumentsDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_created.pdf";

        List<String> languages = Arrays.<String>asList("spa", "spa_old");
        if ("executable".equals(parameter)) {
            tesseractReader =
                    new TesseractExecutableReader(getTesseractDirectory(),
                            getTessDataDirectory(), languages);
            tesseractReader.setPreprocessingImages(false);
        } else {
            tesseractReader =
                    new TesseractLibReader(getTessDataDirectory(), languages);
        }

        // locate text by words
        tesseractReader.setTextPositioning(TextPositioning.BY_WORDS);
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                languages,  DeviceCmyk.BLACK);

        try {
            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    testDocumentsDirectory, "diff_");
        } finally {
            deleteFile(resultPdfPath);
            Assert.assertEquals(TextPositioning.BY_WORDS, tesseractReader.getTextPositioning());
            tesseractReader.setPreprocessingImages(preprocess);
            tesseractReader.setTextPositioning(TextPositioning.BY_LINES);
        }
    }

    @Test
    public void textGreekOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "greek_01.jpg";
        String expected = "ΟΜΟΛΟΓΙΑ";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }
        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("ell"));
        // correct result with specified greek language
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void textJapaneseOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "japanese_01.png";
        String expected = "日本語文法";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("jpn"));

        result = result.replaceAll("[\f\n]", "");
        // correct result with specified japanese language
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void testFrenchOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "french_01.png";
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
        String imgPath = testImagesDirectory + "arabic_02.png";
        // First sentence
        String expected = "اللغة العربية";

        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("ara"));
        // correct result with specified arabic language
        Assert.assertTrue(result.startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("eng")));
        Assert.assertNotEquals(expected, getRecognizedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("spa")));
        Assert.assertNotEquals(expected, getRecognizedTextFromTextFile(tesseractReader, imgPath,
                new ArrayList<String>()));
    }

    @Test
    public void testGermanAndCompareTxtFiles() {
        String imgPath = testImagesDirectory + "german_01.jpg";
        String expectedTxt = testDocumentsDirectory + "german_01" + parameter + ".txt";

        boolean result = doOcrAndCompareTxtFiles(tesseractReader, imgPath, expectedTxt,
                Collections.<String>singletonList("deu"));
        Assert.assertTrue(result);
    }

    @Test
    public void testMultipageTiffAndCompareTxtFiles() {
        String imgPath = testImagesDirectory + "multipage.tiff";
        String expectedTxt = testDocumentsDirectory + "multipage_" + parameter + ".txt";

        boolean result = doOcrAndCompareTxtFiles(tesseractReader, imgPath, expectedTxt,
                Collections.<String>singletonList("eng"));
        Assert.assertTrue(result);
    }

    @Test
    public void testGermanWithTessData() {
        String imgPath = testImagesDirectory + "german_01.jpg";
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
        String imgPath = testImagesDirectory + "arabic_01.jpg";
        File file = new File(imgPath);
        String expected = "الحية. والضحك؛ والحب\nlive, laugh, love";

        String result = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("ara", "eng"), cairoFontPath);
        // correct result with specified arabic+english languages
        Assert.assertEquals(expected, result.replaceAll("[?]", ""));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"), cairoFontPath));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, new ArrayList<String>(), cairoFontPath));
    }

    @Test
    public void testArabicText() {
        String imgPath = testImagesDirectory + "arabic_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "اللغة العربية";

        // correct result with specified arabic language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ara"), cairoFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"), cairoFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("spa"), cairoFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<String>(), cairoFontPath));
    }

    @Test
    public void compareMultiLangImage() throws InterruptedException, java.io.IOException {
        String filename = "multilang";
        String expectedPdfPath = testDocumentsDirectory + filename + "_" + parameter + ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_created.pdf";

        try {
            tesseractReader.setTextPositioning(TextPositioning.BY_WORDS);
            tesseractReader.setPathToTessData(getTessDataDirectory());
            doOcrAndSavePdfToPath(tesseractReader,
                    testImagesDirectory + filename + ".png", resultPdfPath,
                    Arrays.<String>asList("eng", "deu", "spa"), DeviceCmyk.BLACK);

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    testDocumentsDirectory, "diff_");
        } finally {
            deleteFile(resultPdfPath);
            Assert.assertEquals(TextPositioning.BY_WORDS, tesseractReader.getTextPositioning());
            tesseractReader.setTextPositioning(TextPositioning.BY_LINES);
        }
    }

    @Test
    public void testHindiTextWithUrdu() {
        String imgPath = testImagesDirectory + "hindi_01.jpg";
        File file = new File(imgPath);

        String expectedHindi = "हिन्दुस्तानी";
        String expectedUrdu = "وتالی";

        // correct result with specified arabic+urdu languages
        // but because of specified font only hindi will be displayed
        String resultHindiFont = getTextFromPdf(tesseractReader, file,
                Arrays.asList("hin", "urd"), freeSansFontPath);

        Assert.assertTrue(resultHindiFont.startsWith(expectedHindi));
        Assert.assertTrue(resultHindiFont.contains(expectedHindi));
        Assert.assertFalse(resultHindiFont.contains(expectedUrdu));

        String resultArabic = getTextFromPdf(tesseractReader, file,
                Arrays.asList("hin", "urd"), cairoFontPath);

        // correct result with specified arabic+urdu languages
        // but because of default font only urdu will be displayed
        Assert.assertTrue(resultArabic.contains(expectedUrdu));
        Assert.assertFalse(resultArabic.contains(expectedHindi));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        // with different fonts
        Assert.assertTrue(getTextFromPdf(tesseractReader ,file,
                Collections.<String>singletonList("hin"), notoSansFontPath)
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("hin"))
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"))
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file)
                .contains(expectedHindi));
    }

    @Test
    public void testHindiTextWithEng() {
        String imgPath = testImagesDirectory + "hindi_02.jpg";
        File file = new File(imgPath);

        String expected = "मानक हनिदी\nHindi";

        // correct result with specified arabic+english languages
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("hin", "eng"), notoSansFontPath));

        // incorrect result without specified english language
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("hin"), notoSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"), notoSansFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<String>(), notoSansFontPath));
    }

    @Test
    public void testGeorgianText() {
        String imgPath = testImagesDirectory + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        String result = getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("kat"), freeSansFontPath);
        // correct result with specified georgian+eng language
        Assert.assertEquals(expected, result);
        result = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("kat", "kat_old"), freeSansFontPath);
        Assert.assertEquals(expected, result);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("kat")).contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("eng"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                new ArrayList<String>())
                .contains(expected));
    }

    @Test
    public void testBengali() {
        String imgPath = testImagesDirectory + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে\nশখো";

        tesseractReader.setTextPositioning(TextPositioning.BY_WORDS);
        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben"), freeSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben")));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben"), kosugiFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<String>()));
        tesseractReader.setTextPositioning(TextPositioning.BY_LINES);
    }

    @Test
    public void testChinese() {
        String imgPath = testImagesDirectory + "chinese_01.jpg";
        File file = new File(imgPath);
        String expected = "你 好\nni hao";

        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("chi_sim", "chi_tra"), notoSansSCFontPath));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("chi_sim"), notoSansSCFontPath));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("chi_tra"), notoSansSCFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("chi_sim")),
                notoSansSCFontPath);
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("chi_tra")),
                notoSansSCFontPath);
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("chi_sim", "chi_tra")),
                notoSansSCFontPath);
        Assert.assertFalse(getTextFromPdf(tesseractReader, file, new ArrayList<String>())
                .contains(expected));
    }

    @Test
    public void testSpanishWithTessData() {
        String imgPath = testImagesDirectory + "spanish_01.jpg";
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
        String imgPath = testImagesDirectory + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে";

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Bengali"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Bengali"))
                .startsWith(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Bengali"), kosugiFontPath)
                .startsWith(expected));

        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @Test
    public void testGeorgianTextWithScript() {
        String imgPath = testImagesDirectory + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Georgian"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Georgian"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("Japanese"))
                .contains(expected));

        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @Test
    public void testJapaneseScript() {
        String imgPath = testImagesDirectory + "japanese_01.png";
        File file = new File(imgPath);
        String expected = "日 本 語\n文法";

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        // correct result with specified japanese language
        String result = getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("Japanese"), kosugiFontPath);
        Assert.assertEquals(expected, result);

        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @Test
    public void testCustomUserWords() {
        String imgPath = testImagesDirectory + "wierdwords.png";
        List<String> userWords = Arrays.<String>asList("he23llo", "qwetyrtyqpwe-rty");

        tesseractReader.setLanguages(Arrays.asList("fra"));
        tesseractReader.setUserWords("fra", userWords);
        String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
        Assert.assertTrue(result.contains(userWords.get(0))
                || result.contains(userWords.get(1)));

        Assert.assertEquals(TestUtils.getTempDir()
                        + java.io.File.separatorChar
                        + "fra.user-words",
                tesseractReader.getUserWordsFilePath());
        tesseractReader.setUserWords("eng", new ArrayList<String>());
        tesseractReader.setLanguages(new ArrayList<String>());
    }

    @Test
    public void testCustomUserWordsWithListOfLanguages() {
        String imgPath = testImagesDirectory + "bogusText.jpg";
        String expectedOutput = "B1adeb1ab1a";

        try {
            tesseractReader.setLanguages(Arrays.asList("fra", "eng"));
            tesseractReader.setUserWords("eng", Arrays.<String>asList("b1adeb1ab1a"));
            String result = getRecognizedTextFromTextFile(tesseractReader, imgPath);
            result = result.replace("\n", "").replace("\f", "");
            result = result.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\u007E]", "");
            Assert.assertTrue(result.startsWith(expectedOutput));

            Assert.assertEquals(TestUtils.getTempDir()
                            + java.io.File.separatorChar
                            + "eng.user-words",
                    tesseractReader.getUserWordsFilePath());
        } finally {
            tesseractReader.setUserWords("eng", new ArrayList<String>());
            tesseractReader.setLanguages(new ArrayList<String>());
        }
    }

    @Test
    public void testUserWordsWithLanguageNotInList() throws FileNotFoundException {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(OcrException.LanguageIsNotInTheList,
                        "spa"));
        String userWords = testDocumentsDirectory + "userwords.txt";
        tesseractReader.setUserWords("spa", new FileInputStream(userWords));
        tesseractReader.setLanguages(new ArrayList<String>());
    }

    @Test
    public void testIncorrectLanguageForUserWordsAsList() {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(OcrException.LanguageIsNotInTheList,
                        "eng1"));
        tesseractReader.setUserWords("eng1", Arrays.<String>asList("word1", "word2"));
        tesseractReader.setLanguages(new ArrayList<String>());
    }

    @Test
    public void testIncorrectLanguageForUserWordsAsInputStream()
            throws FileNotFoundException {
        junitExpectedException.expect(OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(OcrException.LanguageIsNotInTheList,
                        "test"));
        String userWords = testDocumentsDirectory + "userwords.txt";
        tesseractReader.setUserWords("test", new FileInputStream(userWords));
        tesseractReader.setLanguages(new ArrayList<String>());
    }
}
