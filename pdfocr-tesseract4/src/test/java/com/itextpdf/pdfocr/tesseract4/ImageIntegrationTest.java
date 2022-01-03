/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.TextInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ImageIntegrationTest extends IntegrationTestHelper {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ImageIntegrationTest.class);

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    AbstractTesseract4OcrEngine tesseractReader;
    String testFileTypeName;
    private boolean isExecutableReaderType;

    public ImageIntegrationTest(ReaderType type) {
        isExecutableReaderType = type.equals(ReaderType.EXECUTABLE);
        if (isExecutableReaderType) {
            testFileTypeName = "executable";
        } else {
            testFileTypeName = "lib";
        }
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
    public void testHocrRotatedImage() throws IOException {
        String path = TEST_IMAGES_DIRECTORY + "90_degrees_rotated.jpg";

        File imgFile = new File(path);
        File outputFile = new File(getTargetDirectory()
                + "90_degrees_rotated.hocr");

        tesseractReader.doTesseractOcr(imgFile, outputFile, OutputFormat.HOCR);
        Map<Integer, List<TextInfo>> pageData = TesseractHelper
                .parseHocrFile(Collections.<File>singletonList(outputFile), null,
                        new Tesseract4OcrEngineProperties().setTextPositioning(TextPositioning.BY_WORDS)
                );

        Assert.assertEquals("90", pageData.get(1).get(0).getText());
        Assert.assertEquals("degrees", pageData.get(1).get(1).getText());
        Assert.assertEquals("rotated", pageData.get(1).get(2).getText());
        Assert.assertEquals("image", pageData.get(1).get(3).getText());
        Assert.assertTrue(pageData.get(1).get(1).getBboxRect().getWidth() > 100);
        Assert.assertTrue(pageData.get(1).get(1).getBboxRect().getHeight() < 100);
    }

    @Test
    public void compareRotatedImage() throws InterruptedException, IOException {
        String testName = "compareRotatedImage";
        String filename = "90_degrees_rotated";

        //Tesseract for Java and Tesseract for .NET give different output
        //So we cannot use one reference pdf file for them
        String expectedPdfPathJava = TEST_DOCUMENTS_DIRECTORY + filename + "_java.pdf";
        String expectedPdfPathDotNet = TEST_DOCUMENTS_DIRECTORY + filename + "_dotnet.pdf";

        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setTextPositioning(TextPositioning.BY_WORDS);
        properties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(properties);
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("eng"), Arrays.<String>asList(NOTO_SANS_FONT_PATH),
                null, true);

        // Because of difference of tesseract 5 and tesseract 4 there're some differences in text recognition.
        // So the goal of this test is to make text invisible and check if image is rotated.
        // Proper text recognition is compared in testHocrRotatedImage test by checking HOCR file.
        boolean javaTest = new CompareTool().compareVisually(resultPdfPath, expectedPdfPathJava,
                    TEST_DOCUMENTS_DIRECTORY, "diff_") == null;
        boolean dotNetTest = new CompareTool().compareVisually(resultPdfPath, expectedPdfPathDotNet,
                    TEST_DOCUMENTS_DIRECTORY, "diff_") == null;
        Assert.assertTrue(javaTest || dotNetTest);

        filename = "180_degrees_rotated";
        expectedPdfPathJava = TEST_DOCUMENTS_DIRECTORY + filename + "_java.pdf";
        expectedPdfPathDotNet = TEST_DOCUMENTS_DIRECTORY + filename + "_dotnet.pdf";
        resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("eng"), Arrays.<String>asList(NOTO_SANS_FONT_PATH),
                null, true);


        javaTest = new CompareTool().compareVisually(resultPdfPath, expectedPdfPathJava,
                    TEST_DOCUMENTS_DIRECTORY, "diff_") == null;
        dotNetTest = new CompareTool().compareVisually(resultPdfPath, expectedPdfPathDotNet,
                    TEST_DOCUMENTS_DIRECTORY, "diff_") == null;

        Assert.assertTrue(javaTest || dotNetTest);

        filename = "270_degrees_rotated";
        expectedPdfPathJava = TEST_DOCUMENTS_DIRECTORY + filename + "_java.pdf";
        expectedPdfPathDotNet = TEST_DOCUMENTS_DIRECTORY + filename + "_dotnet.pdf";
        resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("eng"), Arrays.<String>asList(NOTO_SANS_FONT_PATH),
                null, true);


        javaTest = new CompareTool().compareVisually(resultPdfPath, expectedPdfPathJava,
                    TEST_DOCUMENTS_DIRECTORY, "diff_") == null;
        dotNetTest = new CompareTool().compareVisually(resultPdfPath, expectedPdfPathDotNet,
                    TEST_DOCUMENTS_DIRECTORY, "diff_") == null;

        Assert.assertTrue(javaTest || dotNetTest);
    }

}
