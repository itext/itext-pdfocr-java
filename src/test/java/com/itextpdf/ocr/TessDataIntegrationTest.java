package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Category(IntegrationTest.class)
public class TessDataIntegrationTest extends AbstractIntegrationTest {

    @BeforeClass
    public static void setup() {
        initializeLicense();
    }

    @Test
    public void compareMultiLangImage() throws IOException, InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".png", resultPdfPath,
                tessDataDirectory, Arrays.asList("eng", "deu", "spa"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareSpanishPNGUsingTessData() throws IOException, InterruptedException {
        String filename = "scanned_spa_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testImagesDirectory + filename + ".png", resultPdfPath,
                tessDataDirectory, Arrays.asList("spa", "spa_old"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void testSpanishWithTessData() {
        String imgPath = testImagesDirectory + "spanish_01.jpg";
        File file = new File(imgPath);
        String expectedSpanish = "Aquí\nhablamos\nespañol";

        // correct result with specified spanish language
        Assert.assertEquals(expectedSpanish, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("spa")));
        Assert.assertEquals(expectedSpanish, getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("spa", "eng")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedSpanish, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedSpanish, getTextFromPdf(file, tessDataDirectory, new ArrayList<>()));
        Assert.assertNotEquals(expectedSpanish, getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("eng", "spa")));
    }

    @Test
    public void testGermanWithTessData() {
        String imgPath = testImagesDirectory + "german_01.jpg";
        File file = new File(imgPath);
        String expectedGerman = "Das\nGeheimnis\ndes\nKönnens\nim Wollen.\nliegt";

        // correct result with specified spanish language
        Assert.assertEquals(expectedGerman, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("deu")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedGerman, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedGerman, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("fra")));
        Assert.assertNotEquals(expectedGerman, getTextFromPdf(file, tessDataDirectory, new ArrayList<>()));
    }

    @Test
    public void testFrench() {
        String imgPath = testImagesDirectory + "french_01.png";
        File file = new File(imgPath);
        String expectedFr = "RESTEZ\nCALME\nEN\nPARLEZ\nFRANÇAIS";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("fra")).endsWith(expectedFr));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).endsWith(expectedFr));
        Assert.assertNotEquals(expectedFr, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("spa")));
        Assert.assertNotEquals(expectedFr, getTextFromPdf(file, tessDataDirectory, new ArrayList<>()));
    }

    @Test
    public void testArabicTextWithEng() {
        String imgPath = testImagesDirectory + "arabic_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expectedArabic = "الحية.\nوالضحك؛\nوالحب";
        List<String> engWords = Arrays.asList("live", "laugh", "love");

        // correct result with specified arabic+english languages
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("ara", "eng")).startsWith(expectedArabic));
        engWords.forEach(word -> Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("ara", "eng")).contains(word)));

        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ara")).contains(expectedArabic));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).contains(expectedArabic));
        Assert.assertFalse(getTextFromPdf(file).contains(expectedArabic));
    }

    @Test
    public void testArabicText() {
        String imgPath = testImagesDirectory + "arabic_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "العربية\nاللغة";

        // correct result with specified arabic language
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ara")).startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("spa")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory, new ArrayList<>()).contains(expected));
    }

    @Test
    public void testHindiTextWithUrdu() {
        String imgPath = testImagesDirectory + "hindi_01.jpg";
        File file = new File(imgPath);

        String expectedHindi = "हिन्दुस्तानी";
        String expectedUrdu = "ہروتالی";

        // correct result with specified arabic+urdu languages
        // but because of specified font only hindi will be displayed
        String resultHindiFont = getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("hin", "urd"), notoSansFontPath);

        Assert.assertTrue(resultHindiFont.startsWith(expectedHindi));
        Assert.assertTrue(resultHindiFont.contains(expectedHindi));
        Assert.assertFalse(resultHindiFont.contains(expectedUrdu));

        String resultArabicFont = getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("hin", "urd"));

        // correct result with specified arabic+urdu languages
        // but because of default font only urdu will be displayed
        Assert.assertTrue(resultArabicFont.endsWith(expectedUrdu));
        Assert.assertTrue(resultArabicFont.contains(expectedUrdu));
        Assert.assertFalse(resultArabicFont.contains(expectedHindi));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        // with different fonts
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("hin"), notoSansFontPath).contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("hin")).contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(file).contains(expectedHindi));
    }

    @Test
    public void testHindiTextWithEng() {
        String imgPath = testImagesDirectory + "hindi_02.jpg";
        File file = new File(imgPath);

        String expectedHindi = "हनिदी\nमानक";
        String expectedEng = "Hindi";

        String resultWithEng = getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("hin", "eng"), notoSansFontPath);

        // correct result with specified arabic+english languages
        Assert.assertTrue(resultWithEng.contains(expectedHindi));
        Assert.assertTrue(resultWithEng.contains(expectedEng));
        Assert.assertTrue(resultWithEng.startsWith(expectedHindi));
        Assert.assertTrue(resultWithEng.endsWith(expectedEng));

        String resultWithoutEng = getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("hin"), notoSansFontPath);

        // correct result with specified arabic+english languages
        Assert.assertTrue(resultWithoutEng.contains(expectedHindi));
        Assert.assertFalse(resultWithoutEng.contains(expectedEng));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).contains(expectedEng));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(file).contains(expectedHindi));
    }

    @Test
    public void testGeorgianText() {
        String imgPath = testImagesDirectory + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("kat"), freeSansFontPath).startsWith(expected));
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("kat", "kat_old"), freeSansFontPath).startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("kat")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory, new ArrayList<>()).contains(expected));
    }

    @Test
    public void testGeorgianTextWithEng() {
        String imgPath = testImagesDirectory + "georgian_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "გამარჯობა\n(gamarjoba)\nhello";

        // correct result with specified georgian+eng language
        Assert.assertEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("kat", "eng"), freeSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("kat")));
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory, new ArrayList<>()).contains(expected));
    }

    @Test
    public void testJapanese() {
        String imgPath = testImagesDirectory + "japanese_01.png";
        File file = new File(imgPath);
        String expected = "語\n⽇\n本\n⽂法";

        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("jpn"), notoSansJPFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("jpn")));
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng"), notoSansJPFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory, new ArrayList<>()));
    }

    @Test
    public void testBengali() {
        String imgPath = testImagesDirectory + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ben"), freeSansFontPath).startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ben")).startsWith(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ben"), notoSansJPFontPath).startsWith(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory, new ArrayList<>()).startsWith(expected));
    }

    @Test
    public void testChinese() {
        String imgPath = testImagesDirectory + "chinese_01.jpg";
        File file = new File(imgPath);
        String expected = "好\n你\nni\nhao";

        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("chi_sim", "chi_tra"), notoSansJPFontPath));
        Assert.assertEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("chi_sim"), notoSansJPFontPath));
        Assert.assertEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("chi_tra"), notoSansJPFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("chi_sim")));
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("chi_tra")));
        Assert.assertNotEquals(expected, getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("chi_sim", "chi_tra")));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory, new ArrayList<>()).contains(expected));
    }

    @Test
    public void testGreek() {
        String imgPath = testImagesDirectory + "greek_01.jpg";
        File file = new File(imgPath);
        String expected = "ὈΡΘΟΔΟΞΟΣ\nΟΜΟΛΟΓΙΑ";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("ell", "grc"), notoSansFontPath).contains(expected));
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("ell", "grc"), notoSansFontPath).startsWith(expected));
        Assert.assertTrue(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ell"), notoSansFontPath).startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("ell")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("grc")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("grc"), notoSansFontPath).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory,
                Arrays.asList("ell", "grc")).contains(expected));
        Assert.assertFalse(getTextFromPdf(file, tessDataDirectory, new ArrayList<>()).contains(expected));
    }
}
