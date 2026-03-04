/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.TextInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ImageIntegrationTest extends IntegrationTestHelper {

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

    @BeforeEach
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

        Assertions.assertEquals("90", pageData.get(1).get(0).getText());
        Assertions.assertEquals("degrees", pageData.get(1).get(1).getText());
        Assertions.assertEquals("rotated", pageData.get(1).get(2).getText());
        Assertions.assertEquals("image", pageData.get(1).get(3).getText());
        Assertions.assertTrue(pageData.get(1).get(1).getBBoxRect().getWidth() > 100);
        Assertions.assertTrue(pageData.get(1).get(1).getBBoxRect().getHeight() < 100);
    }

    @Test
    public void compareRotatedImage() throws InterruptedException, IOException {
        String testName = "compareRotatedImage";
        String filename = "90_degrees_rotated";

        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + ".pdf";

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

        // So the goal of this test is to make text invisible and check if image is rotated.
        // Proper text recognition is compared in testHocrRotatedImage test by checking HOCR file.
        Assertions.assertNull(new CompareTool().compareVisually(resultPdfPath, expectedPdfPath,
                getTargetDirectory(), 16.5));

        filename = "180_degrees_rotated";
        expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + ".pdf";
        resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("eng"), Arrays.<String>asList(NOTO_SANS_FONT_PATH),
                null, true);

        Assertions.assertNull(new CompareTool().compareVisually(resultPdfPath, expectedPdfPath,
                getTargetDirectory(), 18.5));

        filename = "270_degrees_rotated";
        expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + ".pdf";
        resultPdfPath = getTargetDirectory() + filename + "_" + testName + ".pdf";
        doOcrAndSavePdfToPath(tesseractReader,
                TEST_IMAGES_DIRECTORY + filename + ".jpg", resultPdfPath,
                Arrays.<String>asList("eng"), Arrays.<String>asList(NOTO_SANS_FONT_PATH),
                null, true);

        Assertions.assertNull(new CompareTool().compareVisually(resultPdfPath, expectedPdfPath,
                getTargetDirectory(), 18.5));
    }

}
