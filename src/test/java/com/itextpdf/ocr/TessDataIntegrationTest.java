package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class TessDataIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;
    String parameter;

    public TessDataIntegrationTest(TesseractReader reader, String param) {
        tesseractReader = reader;
        parameter = param;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.<Object[]>asList(
                new Object[][] { {
                        new TesseractExecutableReader(getTesseractDirectory(),
                                getTessDataDirectory()),
                        "executable"
                    }, {
                        new TesseractLibReader(getTessDataDirectory()),
                        "lib"
                    }
                });
    }

    @Test
    public void compareGreekPNG() throws IOException, InterruptedException {
        String filename = "greek_02";
        String expectedPdfPath = testDocumentsDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_created.pdf";

        tesseractReader.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTextPositioning(TextPositioning.byLines);
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                Arrays.<String>asList("ell", "eng"),
                notoSansFontPath, DeviceCmyk.BLACK);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void textGreekText() {
        String imgPath = testImagesDirectory + "greek_01.jpg";
        File file = new File(imgPath);
        String expected = "ΟΜΟΛΟΓΙΑ";

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
    public void testGeorgianTextWithEng() {
        String imgPath = testImagesDirectory + "georgian_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "გამარჯობა\n(gamarjoba)\nhello";

        // correct result with specified georgian+eng language
        Assert.assertEquals(expected,
                getTextFromPdf(tesseractReader, file,
                        Arrays.<String>asList("kat", "eng"), freeSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file,
                        Collections.<String>singletonList("kat")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file,
                        Collections.<String>singletonList("eng")));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file, new ArrayList<String>())
                .contains(expected));
    }

    @Test
    public void compareSpanishPNG() throws IOException, InterruptedException {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String filename = "scanned_spa_01";
        String expectedPdfPath = testDocumentsDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_created.pdf";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }

        // locate text by words
        tesseractReader.setTextPositioning(TextPositioning.byWords);
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                Arrays.<String>asList("spa", "spa_old"),  DeviceCmyk.BLACK);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");

        deleteFile(resultPdfPath);
        Assert.assertEquals(TextPositioning.byWords, tesseractReader.getTextPositioning());
        tesseractReader.setPreprocessingImages(preprocess);
        tesseractReader.setTextPositioning(TextPositioning.byLines);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String filename = "scanned_eng_01";
        String expectedPdfPath = testDocumentsDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_created.pdf";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".png",
                resultPdfPath,
                Arrays.<String>asList("eng"), DeviceCmyk.MAGENTA);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");

        deleteFile(resultPdfPath);
        tesseractReader.setPreprocessingImages(preprocess);
    }

    @Test
    public void textGreekOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "greek_01.jpg";
        String expected = "ΟΜΟΛΟΓΙΑ";

        String result = getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("ell"));
        // correct result with specified greek language
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void textJapaneseOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "japanese_01.png";
        String expected = "日 本 語文法";

        String result = getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("jpn"));

        result = result.replaceAll("[\f\n]", "");
        // correct result with specified japanese language
        Assert.assertTrue(result.contains(expected));
    }

    @Test
    public void testFrenchOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "french_01.png";
        String expectedFr = "RESTEZ\nCALME\nPARLEZ EN\nFRANÇAIS";

        String result = getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("fra"));
        result = result.replaceAll("(?:\\n\\f?\\n)+", "");
        // correct result with specified spanish language
        Assert.assertTrue(result.trim().endsWith(expectedFr));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("eng")).endsWith(expectedFr));
        Assert.assertNotEquals(expectedFr,
                getOCRedTextFromTextFile(tesseractReader, imgPath,
                        Collections.<String>singletonList("spa")));
        Assert.assertNotEquals(expectedFr,
                getOCRedTextFromTextFile(tesseractReader, imgPath,
                        new ArrayList<String>()));
    }

    @Test
    public void testGeorgianOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "georgian_02.png";
        // First sentence
        String expected = "გამარჯობა (gamarjoba) hello ";

        String result = getOCRedTextFromTextFile(tesseractReader, imgPath,
                Arrays.<String>asList("kat", "eng"));
        result = result.replaceAll("\f", "");
        result = result.replaceAll("\\n+", " ");
        // correct result with specified georgian+eng language
        Assert.assertEquals(expected, result);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getOCRedTextFromTextFile(tesseractReader, imgPath,
                        Collections.<String>singletonList("kat")));
        Assert.assertNotEquals(expected,
                getOCRedTextFromTextFile(tesseractReader, imgPath,
                        Collections.<String>singletonList("eng")));
        Assert.assertFalse(getOCRedTextFromTextFile(tesseractReader, imgPath,
                new ArrayList<String>()).contains(expected));
    }

    @Test
    public void testArabicOutputFromTxtFile() {
        String imgPath = testImagesDirectory + "arabic_02.png";
        // First sentence
        String expected = "اللغة العربية";

        String result = getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("ara"));
        result = result.replaceAll("[\n\f]", "");
        // correct result with specified arabic language
        Assert.assertEquals(expected, result);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("eng")));
        Assert.assertNotEquals(expected, getOCRedTextFromTextFile(tesseractReader, imgPath,
                Collections.<String>singletonList("spa")));
        Assert.assertNotEquals(expected, getOCRedTextFromTextFile(tesseractReader, imgPath,
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

        // correct result with specified spanish language
        Assert.assertEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("deu")));

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
    public void compareMultiLangImage() throws IOException,
            InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = testDocumentsDirectory + filename + ".pdf";
        String resultPdfPath = testDocumentsDirectory + filename + "_created.pdf";

        tesseractReader.setTextPositioning(TextPositioning.byWords);
        tesseractReader.setPathToTessData(getTessDataDirectory());
        doOcrAndSavePdfToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                Arrays.<String>asList("eng", "deu", "spa"), DeviceCmyk.BLACK);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDocumentsDirectory, "diff_");

        deleteFile(resultPdfPath);
        Assert.assertEquals(TextPositioning.byWords, tesseractReader.getTextPositioning());
        tesseractReader.setTextPositioning(TextPositioning.byLines);
    }

    @Test
    public void testHindiTextWithUrdu() {
        String imgPath = testImagesDirectory + "hindi_01.jpg";
        File file = new File(imgPath);

        String expectedHindi = "हिन्दुस्तानी";
        String expectedUrdu = "ہنروتالی";

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

        String expectedHindi = "हनिदी\nमानक";
        String expectedEng = "Hindi";
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

        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("kat"), freeSansFontPath)
                .startsWith(expected));
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("kat", "kat_old"), freeSansFontPath)
                .startsWith(expected));

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
        String expected = "শখো\nইংরজে";

        tesseractReader.setTextPositioning(TextPositioning.byWords);
        // correct result with specified spanish language
        Assert.assertEquals(getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben"), freeSansFontPath), expected);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben")));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.<String>singletonList("ben"), kosugiFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<String>()));
        tesseractReader.setTextPositioning(TextPositioning.byLines);
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

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng")));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, new ArrayList<String>()));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("eng", "spa")));
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
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("Japanese"), kosugiFontPath));

        tesseractReader.setPathToTessData(getTessDataDirectory());
    }

    @Test
    public void testCustomUserWords() {
        String imgPath = testImagesDirectory + "wierdwords.png";
        String expectedOutput = "he23llo qwetyrtyqpwe-rty";

        tesseractReader.setLanguages(Collections.<String>singletonList("eng"));
        tesseractReader.setUserWords("eng", Arrays.<String>asList("he23llo", "qwetyrtyqpwe-rty"));
        String result = getOCRedTextFromTextFile(tesseractReader, imgPath);
        Assert.assertTrue(result.trim().contains(expectedOutput));

        Assert.assertEquals(System.getProperty("java.io.tmpdir") + File.separator
                        + "eng.user-words",
                tesseractReader.getUserWordsFilePath());
        tesseractReader.setUserWords("eng", new ArrayList<String>());
        tesseractReader.setLanguages(new ArrayList<>());
    }

    @Test
    public void testCustomUserWordsWIthSeveralLanguages() {
        String imgPath = testImagesDirectory + "wierdwords.png";
        String expectedOutput = "he23llo qwetyrtyqpwe-rty";

        tesseractReader.setLanguages(Arrays.<String>asList("eng", "deu"));
        tesseractReader.setUserWords("deu", Arrays.<String>asList("he23llo", "qwetyrtyqpwe-rty"));
        String result = getOCRedTextFromTextFile(tesseractReader, imgPath);
        Assert.assertTrue(result.trim().contains(expectedOutput));

        Assert.assertEquals(System.getProperty("java.io.tmpdir") + File.separator
                        + "deu.user-words",
                tesseractReader.getUserWordsFilePath());
        tesseractReader.setUserWords("eng", new ArrayList<String>());
        tesseractReader.setLanguages(new ArrayList<>());
    }

    @Test
    public void testUserWordsSmallImage() throws FileNotFoundException {
        String imgPath = testImagesDirectory + "small.jpg";
        String userWords = testDocumentsDirectory + "userwords.txt";

        List<String> expectedOutput = Arrays.<String>asList("2", "Item(s)", "included", "36,000",
                "48,000", "4,000", "CHANGE");

        tesseractReader.setUserWords("eng", new FileInputStream(new File(userWords)));
        String result = getOCRedTextFromTextFile(tesseractReader, imgPath);

        for (String word : expectedOutput) {
            Assert.assertTrue(result.contains(word));
        }
        Assert.assertEquals(System.getProperty("java.io.tmpdir")
                        + File.separator + "eng.user-words",
                tesseractReader.getUserWordsFilePath());
        Assert.assertEquals(1, tesseractReader.getLanguages().size());
        Assert.assertEquals("eng", tesseractReader.getLanguages().get(0));
        tesseractReader.setUserWords("eng", new ArrayList<String>());
        tesseractReader.setLanguages(new ArrayList<>());
    }

    @Test
    public void testIncorrectLanguageForUserWords() {
        try {
            tesseractReader.setUserWords("eng1", Arrays.<String>asList("word1", "word2"));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "eng1.traineddata", tesseractReader.getTessData());
            Assert.assertEquals(expectedMsg, e.getMessage());
            tesseractReader.setLanguages(new ArrayList<>());
        }

        try {
            String userWords = testDocumentsDirectory + "userwords.txt";
            tesseractReader.setUserWords("test", new FileInputStream(new File(userWords)));
        } catch (OCRException | FileNotFoundException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_LANGUAGE,
                            "test.traineddata", tesseractReader.getTessData());
            Assert.assertEquals(expectedMsg, e.getMessage());
            tesseractReader.setLanguages(new ArrayList<>());
        }
    }
}
