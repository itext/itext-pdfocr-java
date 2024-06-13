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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.TextInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class TesseractHelperTest extends IntegrationTestHelper {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractHelperTest.class);

    AbstractTesseract4OcrEngine tesseractReader;
    String testFileTypeName;
    private boolean isExecutableReaderType;

    public TesseractHelperTest(ReaderType type) {
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
    public void testTesseract4OcrForOnePageWithHocrFormat()
            throws IOException {
        String path = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        String expected = "619121";
        File imgFile = new File(path);
        File outputFile = new File(getTargetDirectory()
                + "testTesseract4OcrForOnePage.hocr");

        tesseractReader.doTesseractOcr(imgFile, outputFile, OutputFormat.HOCR);
        Map<Integer, List<TextInfo>> pageData = TesseractHelper
                .parseHocrFile(Collections.<File>singletonList(outputFile), null,
                        tesseractReader
                                .getTesseract4OcrEngineProperties()
                );

        String result = getTextFromPage(pageData.get(1));
        Assertions.assertEquals(expected, result.trim());
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

}
