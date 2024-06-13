/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

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
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.OutputFormat;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BasicTesseractIntegrationTest extends IntegrationTestHelper {

    AbstractTesseract4OcrEngine tesseractReader;

    public BasicTesseractIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @BeforeEach
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void testFontColorInMultiPagePdf() throws IOException {
        String testName = "testFontColorInMultiPagePdf";
        String path = TEST_IMAGES_DIRECTORY + "multîpage.tiff";
        String pdfPath = getTargetDirectory() + testName + ".pdf";
        File file = new File(path);

        tesseractReader.setTesseract4OcrEngineProperties(
                tesseractReader.getTesseract4OcrEngineProperties()
                        .setPreprocessingImages(false));
        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setTextLayerName("Text1");
        com.itextpdf.kernel.colors.Color color = DeviceCmyk.MAGENTA;
        ocrPdfCreatorProperties.setTextColor(color);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                ocrPdfCreatorProperties);
        PdfDocument doc =
                ocrPdfCreator.createPdf(Collections.<File>singletonList(file), getPdfWriter(pdfPath));

        Assertions.assertNotNull(doc);
        doc.close();

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy strategy = new ExtractionStrategy("Text1");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(1));

        com.itextpdf.kernel.colors.Color fillColor = strategy.getFillColor();
        Assertions.assertEquals(fillColor, color);

        pdfDocument.close();
    }

    @Test
    public void testNoisyImage() {
        String path = TEST_IMAGES_DIRECTORY + "tèst/noisy_01.png";
        String expectedOutput1 = "Noisyimage to test Tesseract OCR";
        String expectedOutput2 = "Noisy image to test Tesseract OCR";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(path));
        Assertions.assertTrue(realOutputHocr.equals(expectedOutput1) ||
                realOutputHocr.equals(expectedOutput2));
    }

    @Test
    public void testPantoneImage() {
        String filePath = TEST_IMAGES_DIRECTORY + "pantone_blue.jpg";
        String expected = "";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(filePath));
        Assertions.assertEquals(expected, realOutputHocr);
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

        Assertions.assertEquals("", strategy.getResultantText());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE, count = 1)
    })
    @Test
    public void testInputInvalidImage() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
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
        });

        Assertions.assertEquals(MessageFormatUtil
                .format(PdfOcrTesseract4ExceptionMessageConstant.CANNOT_READ_PROVIDED_IMAGE,
                        new File(TEST_IMAGES_DIRECTORY + "example.txt")
                                .getAbsolutePath()), exception.getMessage());
    }

    @Test
    public void testNonAsciiImagePath() {
        String path = TEST_IMAGES_DIRECTORY + "tèst/noisy_01.png";
        String expectedOutput1 = "Noisyimage to test Tesseract OCR";
        String expectedOutput2 = "Noisy image to test Tesseract OCR";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(path));
        Assertions.assertTrue(realOutputHocr.equals(expectedOutput1) ||
                realOutputHocr.equals(expectedOutput2));
    }

    @Test
    public void testNonAsciiImageName() {
        String path = TEST_IMAGES_DIRECTORY + "nümbérs.jpg";
        String expectedOutput = "619121";

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                new File(path));
        Assertions.assertTrue(realOutputHocr.equals(expectedOutput));
    }

    @Test
    public void testNullPathToTessData() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPathToTessData(null));
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
        });

        Assertions.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_DIRECTORY_IS_INVALID,
                exception.getMessage());
    }

    @Test
    public void testPathToTessDataWithoutData() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPathToTessData(new File("test/")));
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("eng"));
        });

        Assertions.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.PATH_TO_TESS_DATA_DIRECTORY_IS_INVALID,
                exception.getMessage());
    }

    @LogMessages(messages = {
        @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE)
    })
    @Test
    public void testEmptyPathToTessData() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
            Tesseract4OcrEngineProperties properties = tesseractReader.getTesseract4OcrEngineProperties()
                    .setPathToTessData(new File("."));
            tesseractReader.setTesseract4OcrEngineProperties(properties);
            getTextFromPdf(tesseractReader, file);

            Assertions.assertEquals(new File("").getAbsolutePath(),
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .getPathToTessData().getAbsolutePath());
        });

        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                "eng.traineddata",
                new File(".").getAbsolutePath()), exception.getMessage());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                    count = 1)
    })
    @Test
    public void testIncorrectLanguage() {
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class,
                () -> getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("spa_new")));
        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                "spa_new.traineddata",
                new File(LANG_TESS_DATA_DIRECTORY).getAbsolutePath()), exception.getMessage());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                    count = 1)
    })
    @Test
    public void testListOfLanguagesWithOneIncorrectLanguage() {
        File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () ->
                getTextFromPdf(tesseractReader, file, Arrays.<String>asList("spa", "spa_new", "spa_old")));
        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                "spa_new.traineddata",
                new File(LANG_TESS_DATA_DIRECTORY).getAbsolutePath()), exception.getMessage());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                    count = 1)
    })
    @Test
    public void testIncorrectScriptsName() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
            getTextFromPdf(tesseractReader, file, Collections.<String>singletonList("English"));
        });

        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                "English.traineddata",
                new File(SCRIPT_TESS_DATA_DIRECTORY).getAbsolutePath()), exception.getMessage());
    }

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE, count
                    = 1)
    })
    @Test
    public void testListOfScriptsWithOneIncorrect() {
        Exception exception = Assertions.assertThrows(PdfOcrTesseract4Exception.class, () -> {
            File file = new File(TEST_IMAGES_DIRECTORY + "spanish_01.jpg");
            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setPathToTessData(new File(SCRIPT_TESS_DATA_DIRECTORY)));
            getTextFromPdf(tesseractReader, file,
                    Arrays.<String>asList("Georgian", "Japanese", "English"));
        });

        Assertions.assertEquals(MessageFormatUtil.format(PdfOcrTesseract4ExceptionMessageConstant.INCORRECT_LANGUAGE,
                "English.traineddata", new File(SCRIPT_TESS_DATA_DIRECTORY).getAbsolutePath()),
                exception.getMessage());
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
        Assertions.assertTrue(result.contains(expected));
    }

    @Test
    public void testSimpleTextOutput() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String expectedOutput = "619121";

        Assertions.assertTrue(
                getRecognizedTextFromTextFile(tesseractReader, imgPath)
                        .contains(expectedOutput));
    }

    @Test
    public void testTxtStringOutput() {
        File file = new File(TEST_IMAGES_DIRECTORY + "multîpage.tiff");
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
            Assertions.assertTrue(result.replaceAll("\r", "").contains(line));
        }
    }

    @Test
    public void testHocrStringOutput() {
        File file = new File(TEST_IMAGES_DIRECTORY + "multîpage.tiff");
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
            Assertions.assertTrue(result.replaceAll("\r", "").contains(line));
        }
    }

    /**
     * Parse text from image and compare with expected.
     */
    private void testImageOcrText(AbstractTesseract4OcrEngine tesseractReader, String path,
                                  String expectedOutput) {
        File ex1 = new File(path);

        String realOutputHocr = getTextUsingTesseractFromImage(tesseractReader,
                ex1);
        Assertions.assertTrue(realOutputHocr.contains(expectedOutput));
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
            textInfo.setBboxRect(new Rectangle(0, 0, 0,0));
            textInfo.setText("");
            pageText.add(textInfo);
        }

        return getTextFromPage(pageText);
    }

    /**
     * Concatenates provided text items to one string.
     */
    private String getTextFromPage(List<TextInfo> pageText) {
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
