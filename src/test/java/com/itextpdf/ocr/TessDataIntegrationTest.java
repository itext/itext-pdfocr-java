package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
        return Arrays.asList(
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
        String expectedPdfPath = testPdfDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        tesseractReader.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTextPositioning(TextPositioning.byLines);
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                Arrays.asList("ell", "eng"),
                notoSansFontPath, DeviceCmyk.BLACK);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void textGreekText() {
        String imgPath = testImagesDirectory + "greek_01.jpg";
        File file = new File(imgPath);
        String expected = "ΟΜΟΛΟΓΙΑ";

        String real = getTextFromPdf(tesseractReader, file,
                Arrays.asList("ell"), notoSansFontPath);
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
                Arrays.asList("jpn"), kosugiFontPath));
    }

    @Test
    public void testFrench() {
        String imgPath = testImagesDirectory + "french_01.png";
        File file = new File(imgPath);
        String expectedFr = "RESTEZ\nCALME\nPARLEZ EN\nFRANÇAIS";

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("fra")).endsWith(expectedFr));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader,file,
                Collections.singletonList("eng")).endsWith(expectedFr));
        Assert.assertNotEquals(expectedFr,
                getTextFromPdf(tesseractReader,file, Collections.singletonList("spa")));
        Assert.assertNotEquals(expectedFr,
                getTextFromPdf(tesseractReader,file, new ArrayList<>()));
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
                        Arrays.asList("kat", "eng"), freeSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file,
                        Collections.singletonList("kat")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file,
                        Collections.singletonList("eng")));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file, new ArrayList<>())
                .contains(expected));
    }

    @Test
    public void compareSpanishPNG() throws IOException, InterruptedException {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String filename = "scanned_spa_01";
        String expectedPdfPath = testPdfDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }

        // locate text by words
        tesseractReader.setTextPositioning(TextPositioning.byWords);
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                Arrays.asList("spa", "spa_old"),  DeviceCmyk.BLACK);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
        Assert.assertEquals(TextPositioning.byWords, tesseractReader.getTextPositioning());
        tesseractReader.setPreprocessingImages(preprocess);
        tesseractReader.setTextPositioning(TextPositioning.byLines);
    }

    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String filename = "scanned_eng_01";
        String expectedPdfPath = testPdfDirectory + filename + parameter +
                ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png",
                resultPdfPath,
                Arrays.asList("eng"), DeviceCmyk.MAGENTA);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
        tesseractReader.setPreprocessingImages(preprocess);
    }

    @Test
    public void testGermanWithTessData() {
        String imgPath = testImagesDirectory + "german_01.jpg";
        File file = new File(imgPath);
        String expectedGerman = "Das Geheimnis\ndes Könnens\nliegt im Wollen.";

        // correct result with specified spanish language
        Assert.assertEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("deu")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("fra")));
        Assert.assertNotEquals(expectedGerman,
                getTextFromPdf(tesseractReader, file, new ArrayList<>()));
    }

    @Test
    public void testArabicTextWithEng() {
        String imgPath = testImagesDirectory + "arabic_01.jpg";
        File file = new File(imgPath);
        String expected = "الحية. والضحك؛ والحب\nlive, laugh, love";

        String result = getTextFromPdf(tesseractReader, file,
                Arrays.asList("ara", "eng"), cairoFontPath);
        // correct result with specified arabic+english languages
        Assert.assertEquals(expected, result.replaceAll("[?]", ""));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("eng"), cairoFontPath));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, new ArrayList<>(), cairoFontPath));
    }

    @Test
    public void testArabicText() {
        String imgPath = testImagesDirectory + "arabic_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "اللغة العربية";

        // correct result with specified arabic language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("ara"), cairoFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("eng"), cairoFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("spa"), cairoFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<>(), cairoFontPath));
    }

    @Test
    public void compareMultiLangImage() throws IOException,
            InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        tesseractReader.setTextPositioning(TextPositioning.byWords);
        tesseractReader.setPathToTessData(getTessDataDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                Arrays.asList("eng", "deu", "spa"), DeviceCmyk.BLACK);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
        Assert.assertEquals(TextPositioning.byWords, tesseractReader.getTextPositioning());
        tesseractReader.setTextPositioning(TextPositioning.byLines);
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
                Collections.singletonList("hin"), notoSansFontPath)
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("hin"))
                .contains(expectedHindi));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
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
        String expected = "मानक हनिदी\nHindi";

        // correct result with specified arabic+english languages
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.asList("hin", "eng"), notoSansFontPath));

        // incorrect result without specified english language
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("hin"), notoSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("eng"), notoSansFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<>(), notoSansFontPath));
    }

    @Test
    public void testGeorgianText() {
        String imgPath = testImagesDirectory + "georgian_01.jpg";
        File file = new File(imgPath);
        // First sentence
        String expected = "ღმერთი";

        // correct result with specified georgian+eng language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("kat"), freeSansFontPath)
                .startsWith(expected));
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Arrays.asList("kat", "kat_old"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("kat")).contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("eng"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                new ArrayList<>())
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
                Collections.singletonList("ben"), freeSansFontPath), expected);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("ben")));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("ben"), kosugiFontPath));
        Assert.assertNotEquals(expected, getTextFromPdf(tesseractReader, file,
                new ArrayList<>()));
        tesseractReader.setTextPositioning(TextPositioning.byLines);
    }

    @Test
    public void testChinese() {
        String imgPath = testImagesDirectory + "chinese_01.jpg";
        File file = new File(imgPath);
        String expected = "你 好\nni hao";

        // correct result with specified spanish language
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Arrays.asList("chi_sim", "chi_tra"), notoSansSCFontPath));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("chi_sim"), notoSansSCFontPath));
        Assert.assertEquals(expected, getTextFromPdf(tesseractReader, file,
                Collections.singletonList("chi_tra"), notoSansSCFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("chi_sim")),
                notoSansSCFontPath);
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("chi_tra")),
                notoSansSCFontPath);
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, Arrays.asList("chi_sim", "chi_tra")),
                notoSansSCFontPath);
        Assert.assertFalse(getTextFromPdf(tesseractReader, file, new ArrayList<>())
                .contains(expected));
    }

    @Test
    public void testSpanishWithTessData() {
        String imgPath = testImagesDirectory + "spanish_01.jpg";
        File file = new File(imgPath);
        String expectedSpanish = "Aquí\nhablamos\nespañol";

        // correct result with specified spanish language
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("spa")));
        Assert.assertEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Arrays.asList("spa", "eng")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, new ArrayList<>()));
        Assert.assertNotEquals(expectedSpanish,
                getTextFromPdf(tesseractReader, file, Arrays.asList("eng", "spa")));
    }

    @Test
    public void testBengaliScript() {
        String imgPath = testImagesDirectory + "bengali_01.jpeg";
        File file = new File(imgPath);
        String expected = "ইংরজে";

        tesseractReader.setPathToTessData(scriptTessDataDirectory);
        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("Bengali"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("Bengali"))
                .startsWith(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("Bengali"), kosugiFontPath)
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
                Collections.singletonList("Georgian"), freeSansFontPath)
                .startsWith(expected));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("Georgian"))
                .contains(expected));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                Collections.singletonList("Japanese"))
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
                Arrays.asList("Japanese"), kosugiFontPath));

        tesseractReader.setPathToTessData(getTessDataDirectory());
    }
}
