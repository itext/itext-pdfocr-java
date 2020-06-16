/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.general;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.AbstractIntegrationTest;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.OutputFormat;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrException;
import com.itextpdf.pdfocr.tesseract4.TesseractHelper;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class BasicTesseractIntegrationTest extends AbstractIntegrationTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    AbstractTesseract4OcrEngine tesseractReader;

    public BasicTesseractIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Before
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void testFontColorInMultiPagePdf() throws IOException {
        String testName = "testFontColorInMultiPagePdf";
        String path = TEST_IMAGES_DIRECTORY + "multipage.tiff";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.MAGENTA;
        ocrPdfCreatorProperties.setTextColor(color);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                ocrPdfCreatorProperties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

        Assert.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(1));

        com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
        Assert.assertEquals(fillColor, color);

        pdfDocument.close();
    }

    @Test
    public void testNoisyImage() {
        String path = TEST_IMAGES_DIRECTORY + "noisy_01.png";
        String expectedOutput1 = "Noisyimage to test Tesseract OCR";
        String expectedOutput2 = "Noisy image to test Tesseract OCR";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(path));
        Assert.assertTrue(realOutputHocr.equals(expectedOutput1) ||
                realOutputHocr.equals(expectedOutput2));
    }

    @Test
    public void testPantoneImage() {
        String filePath = TEST_IMAGES_DIRECTORY + "pantone_blue.jpg";
        String expected = "";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(filePath));
        Assert.assertEquals(expected, realOutputHocr);
    }

    @Test
    public void testDifferentTextStyles() {
        String path = TEST_IMAGES_DIRECTORY + "example_04.png";
        String expectedOutput = "How about a bigger font?";

        testImageOcrText(tesseractReader, path, expectedOutput);
    }

    @Test
    public void testImageWithoutText() throws IOException {
        String testName = "testImageWithoutText";
        String filePath = TEST_IMAGES_DIRECTORY + "pantone_blue.jpg";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        File file = new File(filePath);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);

        ocrPdfCreator.createPdf(Collections.<File>singletonList(file),
                        new PdfWriter(pdfPath)).close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getFirstPage());
        pdfDocument.close();

        Assert.assertEquals("", strategy.getResultantText());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 1)
    })
    @Test
    public void testInputInvalidImage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.INCORRECT_INPUT_IMAGE_FORMAT,
                        "txt"));

        File file1 = new File(TEST_IMAGES_DIRECTORY + "example.txt");
        File file2 = new File(TEST_IMAGES_DIRECTORY
                + "example_05_corrupted.bmp");
        File file3 = new File(TEST_IMAGES_DIRECTORY
                + "numbers_02.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(getTessDataDirectory()));
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);

        ocrPdfCreator.createPdf(Arrays.<File>asList(file3, file1, file2, file3), getPdfWriter());
    }

    @Test
    public void testNullPathToTessData() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.PATH_TO_TESS_DATA_DIRECTORY_IS_INVALID);
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(null));
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
    }

    @Test
    public void testPathToTessDataWithoutData() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(Tesseract4OcrException.PATH_TO_TESS_DATA_DIRECTORY_IS_INVALID);

        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File("test/")));
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4OcrException.INCORRECT_LANGUAGE)
    })
    @Test
    public void testEmptyPathToTessData() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.INCORRECT_LANGUAGE,
                        "eng.traineddata",
                        new File(".").getAbsolutePath()));

        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Tesseract4OcrEngineProperties properties = tesseractReader.getTesseract4OcrEngineProperties()
                .setPathToTessData(new File("."));
        tesseractReader.setTesseract4OcrEngineProperties(properties);
        getTextFromPdf(tesseractReader, file);

        Assert.assertEquals(new File("").getAbsolutePath(),
                tesseractReader.getTesseract4OcrEngineProperties()
                        .getPathToTessData().getAbsolutePath());
    }

    @Test
    public void testTxtStringOutput() {
        File file = new File(TEST_IMAGES_DIRECTORY + "multipage.tiff");
        List<String> expectedOutput = Arrays.<String>asList(
                "Multipage\nTIFF\nExample\nPage 1",
                "Multipage\nTIFF\nExample\nPage 2",
                "Multipage\nTIFF\nExample\nPage 4",
                "Multipage\nTIFF\nExample\nPage 5",
                "Multipage\nTIFF\nExample\nPage 6",
                "Multipage\nTIFF\nExample\nPage /",
                "Multipage\nTIFF\nExample\nPage 8",
                "Multipage\nTIFF\nExample\nPage 9"
        );

        String result = tesseractReader.doImageOcr(file, OutputFormat.TXT);
        for (String line : expectedOutput) {
            Assert.assertTrue(result.replaceAll("\r", "").contains(line));
        }
    }

    @Test
    public void testHocrStringOutput() {
        File file = new File(TEST_IMAGES_DIRECTORY + "multipage.tiff");
        List<String> expectedOutput = Arrays.<String>asList(
                "Multipage\nTIFF\nExample\nPage 1",
                "Multipage\nTIFF\nExample\nPage 2",
                "Multipage\nTIFF\nExample\nPage 4",
                "Multipage\nTIFF\nExample\nPage 5",
                "Multipage\nTIFF\nExample\nPage 6",
                "Multipage\nTIFF\nExample\nPage /",
                "Multipage\nTIFF\nExample\nPage 8",
                "Multipage\nTIFF\nExample\nPage 9"
        );

        String result = tesseractReader.doImageOcr(file, OutputFormat.HOCR);
        for (String line : expectedOutput) {
            Assert.assertTrue(result.replaceAll("\r", "").contains(line));
        }
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.INCORRECT_LANGUAGE,
                    count = 1)
    })
    @Test
    public void testIncorrectLanguage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.INCORRECT_LANGUAGE,
                        "spa_new.traineddata",
                        new File(LANG_TESS_DATA_DIRECTORY).getAbsolutePath()));
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("spa_new"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.INCORRECT_LANGUAGE,
                    count = 1)
    })
    @Test
    public void testListOfLanguagesWithOneIncorrectLanguage() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.INCORRECT_LANGUAGE,
                        "spa_new.traineddata",
                        new File(LANG_TESS_DATA_DIRECTORY).getAbsolutePath()));
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        getTextFromPdf(tesseractReader, file, Arrays.<String>asList("spa", "spa_new", "spa_old"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.INCORRECT_LANGUAGE,
                    count = 1)
    })
    @Test
    public void testIncorrectScriptsName() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.INCORRECT_LANGUAGE,
                        "English.traineddata",
                        new File(SCRIPT_TESS_DATA_DIRECTORY).getAbsolutePath()));

        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
        getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("English"));
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = Tesseract4OcrException.INCORRECT_LANGUAGE, count
                    = 1)
    })
    @Test
    public void testListOfScriptsWithOneIncorrect() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        junitExpectedException.expectMessage(MessageFormatUtil
                .format(Tesseract4OcrException.INCORRECT_LANGUAGE,
                        "English.traineddata",
                        new File(SCRIPT_TESS_DATA_DIRECTORY).getAbsolutePath()));

        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
        getTextFromPdf(tesseractReader, file,
                Arrays.<String>asList("Georgian", "Japanese", "English"));
    }

    @Test
    public void testTesseract4OcrForOnePageWithHocrFormat()
            throws IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String expected = "619121";
        File imgFile = new File(path);
        File outputFile = new File(getTargetDirectory()
                + "testTesseract4OcrForOnePage.hocr");

        tesseractReader.doTesseractOcr(imgFile, outputFile, OutputFormat.HOCR);
        Map<Integer, List<TextInfo>> pageData = TesseractHelper
                .parseHocrFile(Collections.<File>singletonList(outputFile),
                        tesseractReader
                                .getTesseract4OcrEngineProperties()
                                .getTextPositioning()
                );

        String result = getTextFromPage(pageData.get(1));
        Assert.assertEquals(expected, result.trim());
    }

    @Test
    public void testTesseract4OcrForOnePageWithTxtFormat() {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String expected = "619121";
        File imgFile = new File(path);
        File outputFile = new File(getTargetDirectory()
                + "testTesseract4OcrForOnePage.txt");

        tesseractReader.doTesseractOcr(imgFile, outputFile, OutputFormat.TXT);

        String result = getTextFromTextFile(outputFile);
        Assert.assertTrue(result.contains(expected));
    }

    /**
     * Parse text from image and compare with expected.
     */
    private void testImageOcrText(AbstractTesseract4OcrEngine tesseractReader, String path,
                                  String expectedOutput) {
        File ex1 = new File(path);

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                ex1);
        Assert.assertTrue(realOutputHocr.contains(expectedOutput));
    }

    /**
     * Parse text from given image using tesseract.
     */
    private String getTextUsingTesseractFromImage(IOcrEngine tesseractReader,
                                                  File file) {
        int page = 1;
        Map<Integer, List<TextInfo>> data = tesseractReader.doImageOcr(file);
        List<TextInfo> pageText = data.get(page);

        if (pageText == null || pageText.size() == 0) {
            pageText = new ArrayList<TextInfo>();
            TextInfo textInfo = new TextInfo();
            textInfo.setBbox(Arrays.<Float>asList(0f, 0f, 0f, 0f));
            textInfo.setText("");
            pageText.add(textInfo);
        }

        return getTextFromPage(pageText);
    }

    /**
     * Concatenates provided text items to one string.
     */
    private String getTextFromPage(List<TextInfo> pageText) {
        Assert.assertEquals(4,
                pageText.get(0).getBbox().size());

        StringBuilder stringBuilder = new StringBuilder();
        for (TextInfo text : pageText) {
            stringBuilder.append(text.getText());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().trim();
    }

    /**
     * Create pdfWriter.
     */
    private PdfWriter getPdfWriter() {
        return new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().addUAXmpMetadata());
    }
}
