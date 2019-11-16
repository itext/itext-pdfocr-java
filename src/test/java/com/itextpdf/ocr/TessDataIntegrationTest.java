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

    /*@Test
    public void compareMultiLangImage() throws IOException, InterruptedException {
        String filename = "multilang";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcrAndSaveToPath(directory + filename + ".png", resultPdfPath,
                tessDataDirectory, Arrays.asList("eng", "deu", "spa"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareSpanishPNGUsingTessData() throws IOException, InterruptedException {
        String filename = "scanned_spa_01";
        String expectedPdfPath = directory + filename + ".pdf";
        String resultPdfPath = directory + filename + "_created.pdf";

        doOcrAndSaveToPath(directory + filename + ".png", resultPdfPath,
                tessDataDirectory, Arrays.asList("spa", "spa_old"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                directory, "diff_");

        deleteFile(resultPdfPath);
    }


    @Test
    public void testSpanishWithTessData() {
        String imgPath = directory + "spanish_01.jpg";
        File file = new File(imgPath);
        String expectedSpanish = "Aquí\nhablamos\nespañol";

        // correct result with specified spanish language
        Assert.assertEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("spa")), expectedSpanish);
        Assert.assertEquals(getTextFromPdfFile(file, tessDataDirectory,
                Arrays.asList("spa", "eng")), expectedSpanish);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("eng")), expectedSpanish);
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory, new ArrayList<>()), expectedSpanish);
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory,
                Arrays.asList("eng", "spa")), expectedSpanish);
    }

    @Test
    public void testGermanWithTessData() {
        String imgPath = directory + "german_01.jpg";
        File file = new File(imgPath);
        String expectedGerman = "Das Geheimnis\ndes Könnens\nliegt\nim Wollen.";

        // correct result with specified spanish language
        Assert.assertEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("deu")), expectedGerman);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("eng")), expectedGerman);
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("fra")), expectedGerman);
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory, new ArrayList<>()), expectedGerman);
    }

    @Test
    public void testFrenchWithTessData() {
        String imgPath = directory + "french01.jpg";
        File file = new File(imgPath);
        String expectedFr = "LA PHRASE NÉGATIVE EN\nFRANÇAIS\nFormation de la\nnégation, exemples\net exercises.";

        // correct result with specified spanish language
        Assert.assertEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("fra")), expectedFr);

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("eng")), expectedFr);
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory,
                Collections.singletonList("spa")), expectedFr);
        Assert.assertNotEquals(getTextFromPdfFile(file, tessDataDirectory, new ArrayList<>()), expectedFr);
    }*/
}
