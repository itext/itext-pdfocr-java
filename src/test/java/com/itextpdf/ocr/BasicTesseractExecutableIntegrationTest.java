package com.itextpdf.ocr;

import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Category(IntegrationTest.class)
public class BasicTesseractExecutableIntegrationTest
        extends AbstractIntegrationTest {

    @Test
    public void testFrench() {
        String imgPath = testImagesDirectory + "french_01.png";
        File file = new File(imgPath);
        String expectedFr = "RESTEZ\nCALME\nEN\nPARLEZ\nFRANÇAIS";

        TesseractExecutableReader tesseractReader = new TesseractExecutableReader();

        tesseractReader.setPathToExecutable(getTesseractDirectory());
        Assert.assertEquals(getTesseractDirectory(),
                tesseractReader.getPathToExecutable());

        // correct result with specified spanish language
        Assert.assertTrue(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory,
                Collections.singletonList("fra")).endsWith(expectedFr));

        tesseractReader.setPathToExecutable(getTesseractDirectory());

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertFalse(getTextFromPdf(tesseractReader,file,
                langTessDataDirectory,
                Collections.singletonList("eng")).endsWith(expectedFr));
        Assert.assertNotEquals(expectedFr,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                        Collections.singletonList("spa")));
        Assert.assertNotEquals(expectedFr,
                getTextFromPdf(tesseractReader,file, langTessDataDirectory,
                        new ArrayList<>()));
        Assert.assertEquals(getTesseractDirectory(),
                tesseractReader.getPathToExecutable());
    }

    @Test
    public void testGeorgianTextWithEng() {
        String imgPath = testImagesDirectory + "georgian_02.png";
        File file = new File(imgPath);
        // First sentence
        String expected = "გამარჯობა\n(gamarjoba)\nhello";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        // correct result with specified georgian+eng language
        Assert.assertEquals(expected,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                        Arrays.asList("kat", "eng"), freeSansFontPath));

        // incorrect result when languages are not specified
        // or languages were specified in the wrong order
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                        Collections.singletonList("kat")));
        Assert.assertNotEquals(expected,
                getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                        Collections.singletonList("eng")));
        Assert.assertFalse(getTextFromPdf(tesseractReader, file,
                langTessDataDirectory, new ArrayList<>())
                .contains(expected));
    }

    @Test
    public void testIncorrectLanguages() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                Collections.singletonList("spa"));
        try {
            getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                    Arrays.asList("spa", "spa_new", "spa_old"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, langTessDataDirectory,
                    Collections.singletonList("spa_new"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                Collections.singletonList("Georgian"));
        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    Collections.singletonList("English"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    Arrays.asList("Georgian", "Japanese", "English"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file, scriptTessDataDirectory,
                    new ArrayList<>());
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }
    }

    @Test
    public void testIncorrectPathToTessData() {
        File file = new File(testImagesDirectory + "spanish_01.jpg");
        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        try {
            getTextFromPdf(tesseractReader, file, "test/",
                    Collections.singletonList("eng"));
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }

        try {
            getTextFromPdf(tesseractReader, file);
        } catch (OCRException e) {
            Assert.assertEquals(OCRException.TESSERACT_FAILED, e.getMessage());
        }
    }

    // TODO CMYK
    @Test
    public void testTextFromJPG() {
        String path = testImagesDirectory + "numbers_02.jpg";
        String expectedOutput = "0123456789";

        File file = new File(path);

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        String realOutputHocr = getTextFromPdf(tesseractReader, file);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    // TODO CMYK
    @Test
    public void compareJFIF() throws IOException, InterruptedException {
        String filename = "example_02";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".JFIF",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    // TODO
    @Test
    public void compareEngTextPNG() throws IOException, InterruptedException {
        String filename = "scanned_eng_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    // TODO
    @Test
    public void compareBigTiff() throws IOException, InterruptedException {
        String filename = "example_03_10MB";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".tiff",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    // TODO
    @Test
    public void compareBMP() throws IOException, InterruptedException {
        String filename = "example_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".BMP",
                resultPdfPath);

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void testInputInvalidImage() throws IOException {
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";

        File file1 = new File(testImagesDirectory + "example.txt");
        File file2 = new File(testImagesDirectory
                + "example_05_corrupted.bmp");
        File file3 = new File(testImagesDirectory
                + "numbers_02.jpg");
        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        try {
            IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                    Arrays.asList(file3, file1, file2, file3));

            pdfRenderer.doPdfOcr(createPdfWriter(pdfPath));
        } catch (OCRException e) {
            String expectedMsg = MessageFormat
                    .format(OCRException.INCORRECT_INPUT_IMAGE_FORMAT,
                            "txt");
            Assert.assertEquals(expectedMsg, e.getMessage());
        } finally {
            deleteFile(pdfPath);
        }
    }

    @Test
    public void compareSpanishPNGUsingTessData() throws IOException, InterruptedException {
        String filename = "scanned_spa_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                langTessDataDirectory, Arrays.asList("spa", "spa_old"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareJapanesePdf() throws IOException, InterruptedException {
        String filename = "japanese_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                langTessDataDirectory, Arrays.asList("jpn"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareGreekPdf() throws IOException, InterruptedException {
        String filename = "greek_01";
        String expectedPdfPath = testPdfDirectory + filename + ".pdf";
        String resultPdfPath = testPdfDirectory + filename + "_created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".jpg", resultPdfPath,
                langTessDataDirectory, Arrays.asList("ell"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    @Test
    public void compareJapaneseScript() throws IOException, InterruptedException {
        String filename = "japanese_01";
        String expectedPdfPath = testPdfDirectory + filename + "_script.pdf";
        String resultPdfPath = testPdfDirectory + filename + "script__created.pdf";

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        doOcrAndSaveToPath(tesseractReader,
                testImagesDirectory + filename + ".png", resultPdfPath,
                scriptTessDataDirectory, Collections.singletonList("Japanese"));

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                testPdfDirectory, "diff_");

        deleteFile(resultPdfPath);
    }

    // TODO
    @Test
    public void testNotPdfA3uWithIntent() throws IOException {
        String path = testImagesDirectory + "numbers_02.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        // PdfA3u should not be created as 'createdPdfA3u' flag is false
        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath),
                false, getCMYKPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertNotEquals(PdfAConformanceLevel.PDF_A_3U,
                pdfDocument.getReader().getPdfAConformanceLevel());

        pdfDocument.close();
        deleteFile(pdfPath);
    }

    // TODO
    @Test
    public void testPdfCustomMetadata() throws IOException {
        String path = testImagesDirectory + "numbers_02.jpg";
        String pdfPath = testImagesDirectory + UUID.randomUUID().toString()
                + ".pdf";
        File file = new File(path);

        TesseractReader tesseractReader = new TesseractExecutableReader(
                getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file));

        String locale = "nl-BE";
        pdfRenderer.setPdfLang(locale);
        String title = "Title";
        pdfRenderer.setTitle(title);

        PdfDocument doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath),
                true, getCMYKPdfOutputIntent());

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));
        Assert.assertEquals(locale,
                pdfDocument.getCatalog().getLang().toString());
        Assert.assertEquals(title, pdfDocument.getDocumentInfo().getTitle());
        Assert.assertEquals(PdfAConformanceLevel.PDF_A_3U,
                pdfDocument.getReader().getPdfAConformanceLevel());

        deleteFile(pdfPath);
    }
}
