package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Category(IntegrationTest.class)
public class TessDataIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void compareMultiLangImage() throws IOException, InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = testDirectory + filename + ".pdf";
        String resultPdfPath = testDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testDirectory + filename + ".png", resultPdfPath,
                tessDataDirectory, Arrays.asList("eng", "deu", "spa"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareSpanishPNGUsingTessData() throws IOException, InterruptedException {
        String filename = "scanned_spa_01";
        String expectedPdfPath = testDirectory + filename + ".pdf";
        String resultPdfPath = testDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(testDirectory + filename + ".png", resultPdfPath,
                tessDataDirectory, Arrays.asList("spa", "spa_old"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testDirectory, "diff_");

        deleteFile(resultPdfPath);
    }


    @Test
    public void testSpanishWithTessData() {
        String imgPath = testDirectory + "spanish_01.jpg";
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
        String imgPath = testDirectory + "german_01.jpg";
        File file = new File(imgPath);
        String expectedGerman = "Das Geheimnis\ndes Könnens\nim\nWollen.\nliegt";

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
    public void testFrenchWithTessData() {
        String imgPath = testDirectory + "french01.jpg";
        File file = new File(imgPath);
        String expectedFr = "LA EN\nPHRASE NÉGATIVE\nFRANÇAIS\nFormation de la\nnégation,\nexemples\net exercises.";

        // correct result with specified spanish language
        Assert.assertEquals(expectedFr, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("fra")));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expectedFr, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("eng")));
        Assert.assertNotEquals(expectedFr, getTextFromPdf(file, tessDataDirectory,
                Collections.singletonList("spa")));
        Assert.assertNotEquals(expectedFr, getTextFromPdf(file, tessDataDirectory, new ArrayList<>()));
    }
}
