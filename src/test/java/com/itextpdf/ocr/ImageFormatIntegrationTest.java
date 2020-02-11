package com.itextpdf.ocr;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
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
    public void testBMPText() {
        String path = testImagesDirectory + "example_01.BMP";
        String expectedOutput = "message for OCR Scanner Test";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.singletonList("eng"));
        realOutputHocr = realOutputHocr.replaceAll("[\n]", " ");
        realOutputHocr = realOutputHocr.replaceAll("[‘]", "");
        Assert.assertTrue(realOutputHocr.contains((expectedOutput)));
    }

    @Test
    public void testJFIFText() throws IOException, InterruptedException {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String filename = "example_02";
        String expectedPdfPath = testPdfDirectory + filename + parameter + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        if ("executable".equals(parameter)) {
            tesseractReader.setPreprocessingImages(false);
        }
        tesseractReader.setTextPositioning(TextPositioning.byWords);
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".JFIF", resultPdfPath, cairoFontPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
        tesseractReader.setPreprocessingImages(preprocess);
        tesseractReader.setTextPositioning(TextPositioning.byLines);
    }

    @Test
    public void testTextFromJPG() {
        String path = testImagesDirectory + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testTextFromTIF() {
        String path = testImagesDirectory + "numbers_01.tif";
        String expectedOutput = "619121";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testBigTiffWithoutPreprocessing() {
        String path = testImagesDirectory + "example_03_10MB.tiff";
        String expectedOutput = "File Format";

        String realOutputHocr = getTextFromPdf(tesseractReader, new File(path),
                Collections.singletonList("eng"));
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    @Test
    public void testInputMultipagesTIFF() {
        boolean preprocess = tesseractReader.isPreprocessingImages();
        String path = testImagesDirectory + "multipage.tiff";
        String expectedOutput = "Multipage\nTIFF\nExample\nPage 5";

        File file = new File(path);

        tesseractReader.setPreprocessingImages(false);
        String realOutputHocr = getTextFromPdf(tesseractReader, file, 5,
                Collections.singletonList("eng"));
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

        tesseractReader.setTextPositioning(TextPositioning.byWords);
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".jpg",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
        tesseractReader.setTextPositioning(TextPositioning.byLines);
    }
}
