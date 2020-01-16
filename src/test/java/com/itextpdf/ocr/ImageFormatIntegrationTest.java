package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
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
public class ImageFormatIntegrationTest extends AbstractIntegrationTest {

    TesseractReader tesseractReader;
    String parameter;

    public ImageFormatIntegrationTest(TesseractReader reader, String param) {
        tesseractReader = reader;
        parameter = param;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] { {
                        new TesseractExecutableReader(getTesseractDirectory()), "executable"
                    }, {
                        new TesseractLibReader(), "lib"
                    }
                });
    }

    @Test
    public void testBMPText() {
        String path = testImagesDirectory + "example_01.BMP";
        String expectedOutput = "for message Scanner OCR Test";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                langTessDataDirectory, Collections.singletonList("eng"));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        Assert.assertTrue(realOutputHocr.replaceAll("[‘]", "").contains((expectedOutput)));
    }

    @Test
    public void testJFIFText() {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String path = testImagesDirectory + "example_02.JFIF";
        String expectedOutput = "This is test a for message Scanner OCR Test";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        Assert.assertEquals(expectedOutput, realOutputHocr.replaceAll("[‘]", ""));
        tesseractReader.setPreprocessingImages(preprocess);
    }

    @Test
    public void testTextFromJPG() {
        String path = testImagesDirectory + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testBigTiffWithoutPreprocessing() {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String path = testImagesDirectory + "example_03_10MB.tiff";
        String expectedOutput = "Tagged File Format Image";

        tesseractReader.setPreprocessingImages(false);
        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                langTessDataDirectory, Collections.singletonList("eng"));
        realOutputHocr = realOutputHocr.replaceAll("\n", " ");
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
        tesseractReader.setPreprocessingImages(preprocess);
    }

    @Test
    public void testInputMultipagesTIFF() {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String path = testImagesDirectory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\n5\nPage";

        File file = new File(path);

        tesseractReader.setPreprocessingImages(false);
        String realOutputHocr = getTextFromPdf(tesseractReader, file, 5,
                langTessDataDirectory, Collections.singletonList("eng"));
        Assert.assertNotNull(realOutputHocr);
        Assert.assertEquals(expectedOutput, realOutputHocr);
        tesseractReader.setPreprocessingImages(preprocess);
    }

    @Test
    public void testInputWrongFormat() {
        try {
            File file = new File(testImagesDirectory + "example.txt");
            String realOutput = getTextFromPdf(tesseractReader, file);
            Assert.assertNotNull(realOutput);
            Assert.assertEquals("", realOutput);
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_INPUT_IMAGE_FORMAT,
                            "txt");
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    @Test
    public void compareNumbersJPG() throws IOException, InterruptedException {
        String filename = "numbers_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".jpg",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }
}
