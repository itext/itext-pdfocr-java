package com.itextpdf.pdfocr.pdfa3u;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class PdfA3UIntegrationTest extends AbstractIntegrationTest {

    // path to default cmyk color profile
    private static final String DEFAULT_CMYK_COLOR_PROFILE_PATH = TEST_DIRECTORY + "profiles/CoatedFOGRA27.icc";
    // path to default rgb color profile
    private static final String DEFAULT_RGB_COLOR_PROFILE_PATH = TEST_DIRECTORY + "profiles/sRGB_CS_profile.icm";

    AbstractTesseract4OcrEngine tesseractReader;

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    public PdfA3UIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void comparePdfA3uCMYKColorSpaceSpanishJPG() throws IOException,
            InterruptedException {
        String testName = "comparePdfA3uCMYKColorSpaceSpanishJPG";
        String filename = "numbers_01";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + "_a3u.pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + "_a3u.pdf";

        try {
            OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);

            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setTextPositioning(TextPositioning.BY_WORDS));
            Assert.assertEquals(tesseractReader, ocrPdfCreator.getOcrEngine());
            ocrPdfCreator.setOcrEngine(tesseractReader);
            PdfDocument doc =
                    ocrPdfCreator.createPdfA(
                            Collections.<File>singletonList(
                            new File(TEST_IMAGES_DIRECTORY
                                    + filename + ".jpg")),
                            getPdfWriter(resultPdfPath),
                            getCMYKPdfOutputIntent());
            Assert.assertNotNull(doc);
            doc.close();

            new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                    TEST_DOCUMENTS_DIRECTORY, "diff_");
        } finally {
            Assert.assertEquals(TextPositioning.BY_WORDS,
                    tesseractReader.getTesseract4OcrEngineProperties().getTextPositioning());
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setTextPositioning(TextPositioning.BY_LINES));
        }
    }

    @Test
    public void comparePdfA3uRGBSpanishJPG()
            throws IOException, InterruptedException {
        String testName = "comparePdfA3uRGBSpanishJPG";
        String filename = "spanish_01";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + "_a3u.pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + "_a3u.pdf";

        Tesseract4OcrEngineProperties properties =
                new Tesseract4OcrEngineProperties(tesseractReader.getTesseract4OcrEngineProperties());
        properties.setPathToTessData(LANG_TESS_DATA_DIRECTORY);
        properties.setLanguages(Collections.<String>singletonList("spa"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                new OcrPdfCreatorProperties().setTextColor(DeviceRgb.BLACK));

        PdfDocument doc = ocrPdfCreator.createPdfA(
                Collections.<File>singletonList(
                        new File(TEST_IMAGES_DIRECTORY + filename
                                + ".jpg")), getPdfWriter(resultPdfPath),
                getRGBPdfOutputIntent());
        Assert.assertNotNull(doc);
        doc.close();

        new CompareTool().compareByContent(expectedPdfPath, resultPdfPath,
                TEST_DOCUMENTS_DIRECTORY, "diff_");
    }

    /**
     * Creates PDF cmyk output intent for tests.
     */
    protected PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(DEFAULT_CMYK_COLOR_PROFILE_PATH);
        return new PdfOutputIntent("Custom",
                "","http://www.color.org",
                "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * Creates PDF rgb output intent for tests.
     */
    protected  PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(DEFAULT_RGB_COLOR_PROFILE_PATH);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }
}
