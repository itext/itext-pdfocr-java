package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class TessDataIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;

    public TessDataIntegrationTest(TesseractReader reader) {
        tesseractReader = reader;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] { {
                        new TesseractExecutableReader(getTesseractDirectory())
                    }, {
                        new TesseractLibReader()
                    }
                });
    }

    @Test
    public void compareMultiLangImage() throws IOException, InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                langTessDataDirectory, Arrays.asList("eng", "deu", "spa"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void testSpanishWithTessData() {
        String imgPath = testImagesDirectory + "spanish_01.jpg";
        File file = new File(imgPath);
        String expectedSpanish = "Aquí\nhablamos\nespañol";

        tesseractReader.setPageSegMode(3);

        // correct result with specified spanish language
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("spa")));
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                Arrays.asList("spa", "eng")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                        new ArrayList<>()));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                Arrays.asList("eng", "spa")));

        Assert.assertEquals(3, tesseractReader.getPageSegMode().intValue());
    }

    @Test
    public void testGermanWithTessData() {
        String imgPath = testImagesDirectory + "german_01.jpg";
        File file = new File(imgPath);
        String expectedGerman = "Das\nGeheimnis\ndes\nKönnens\nim Wollen.\nliegt";

        // correct result with specified spanish language
        Assert.assertEquals(expectedGerman,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                Collections.singletonList("deu")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                Collections.singletonList("fra")));
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                        new ArrayList<>()));
    }

    @Test
    public void testArabicTextWithEng() {
        String imgPath = testImagesDirectory + "arabic_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expectedArabic = "الحية.\nوالضحك؛\nوالحب";
        List<String> engWords = Arrays.asList("live", "laugh", "love");

        // correct result with specified arabic+english languages
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Arrays.asList("ara", "eng")).contains(expectedArabic));
        engWords
                .forEach(word -> Assert.assertTrue(
                        getTextFromPdf(tesseractReader, file,
                                langTessDataDirectory,
                                Arrays.asList("ara", "eng")).contains(word)));

        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("ara")).contains(expectedArabic));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("eng")).contains(expectedArabic));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file)
                .contains(expectedArabic));
    }

    @Test
    public void testArabicText() {
        String imgPath = testImagesDirectory + "arabic_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "العربية\nاللغة";

        // correct result with specified arabic language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("ara")).startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("eng"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("spa"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                new ArrayList<>())
                .contains(expected));
    }

    @Test
    public void testHindiTextWithUrdu() {
        String imgPath = testImagesDirectory + "hindi_01.jpg";
        File file = new File(imgPath);

        String expectedHindi = "हिन्दुस्तानी";
        String expectedUrdu = "ہروتالی";

        // correct result with specified arabic+urdu languages
        // but because of specified font only hindi will be displayed
        String resultHindiFont = getTextFromPdf(tesseractReader, file,
                langTessDataDirectory, Arrays.asList("hin", "urd"),
                notoSansFontPath);

        Assert.assertTrue(resultHindiFont.startsWith(expectedHindi));
        Assert.assertTrue(resultHindiFont.contains(expectedHindi));
        Assert.assertFalse(resultHindiFont.contains(expectedUrdu));

        String resultArabicFont = getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Arrays.asList("hin", "urd"));

        // correct result with specified arabic+urdu languages
        // but because of default font only urdu will be displayed
        Assert.assertTrue(resultArabicFont.endsWith(expectedUrdu));
        Assert.assertTrue(resultArabicFont.contains(expectedUrdu));
        Assert.assertFalse(resultArabicFont.contains(expectedHindi));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        // with different fonts
        Assert.assertTrue(getTextFromPdf(tesseractReader ,file,
                langTessDataDirectory,
                Collections.singletonList("hin"), notoSansFontPath)
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("hin"))
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("eng"))
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

        String resultWithEng = getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Arrays.asList("hin", "eng"), notoSansFontPath);

        // correct result with specified arabic+english languages
        Assert.assertTrue(resultWithEng.contains(expectedHindi));
        Assert.assertTrue(resultWithEng.contains(expectedEng));

        String resultWithoutEng = getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("hin"), notoSansFontPath);

        // correct result with specified arabic+english languages
        Assert.assertTrue(resultWithoutEng.contains(expectedHindi));
        Assert.assertFalse(resultWithoutEng.contains(expectedEng));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("eng"))
                .contains(expectedEng));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("eng"))
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file)
                .contains(expectedHindi));
    }

    @Test
    public void testGeorgianText() {
        String imgPath = testImagesDirectory + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("kat"), freeSansFontPath)
                .startsWith(expected));
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Arrays.asList("kat", "kat_old"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("kat")).contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("eng"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory, new ArrayList<>())
                .contains(expected));
    }

    @Test
    public void testBengali() {
        String imgPath = testImagesDirectory + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("ben"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("ben"))
                .startsWith(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("ben"), notoSansJPFontPath)
                .startsWith(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory, new ArrayList<>())
                .startsWith(expected));
    }

    @Test
    public void testChinese() {
        String imgPath = testImagesDirectory + "chinese_01.jpg";
        File file = new File(imgPath);
        String expected = "好\n你\nni\nhao";

        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Arrays.asList("chi_sim", "chi_tra"), notoSansJPFontPath));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("chi_sim"), notoSansJPFontPath));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("chi_tra"), notoSansJPFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("chi_sim")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("chi_tra")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Arrays.asList("chi_sim", "chi_tra")));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory, new ArrayList<>())
                .contains(expected));
    }

    @Test
    public void testBengaliScript() {
        String imgPath = testImagesDirectory + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                scriptTessDataDirectory,
                Collections.singletonList("Bengali"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                scriptTessDataDirectory,
                Collections.singletonList("Bengali"))
                .startsWith(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                scriptTessDataDirectory,
                Collections.singletonList("Bengali"), notoSansJPFontPath)
                .startsWith(expected));
    }

    @Test
    public void testGeorgianTextWithScript() {
        String imgPath = testImagesDirectory + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                scriptTessDataDirectory,
                Collections.singletonList("Georgian"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                scriptTessDataDirectory,
                Collections.singletonList("Georgian"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                scriptTessDataDirectory,
                Collections.singletonList("Japanese"))
                .contains(expected));
    }
}
