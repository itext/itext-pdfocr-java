/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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


import com.itextpdf.pdfocr.TextInfo;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Timeout;

@Tag("IntegrationTest")
public class TesseractHelperLibTest extends TesseractHelperTest {
    public TesseractHelperLibTest() {
        super(ReaderType.LIB);
    }


    @Timeout(value = 60000, unit = TimeUnit.MILLISECONDS)
    @Test
    public void hocrOutputFromHalftoneFile() throws java.io.IOException {
        String path = TEST_IMAGES_DIRECTORY + "halftone.jpg";
        String expected01 = "Silliness";
        String expected02 = "Enablers";
        String expected03 = "You";
        String expected04 = "Middle";
        String expected05 = "André";
        String expected06 = "QUANTITY";
        String expected07 = "DESCRIPTION";
        String expected08 = "Silliness Enablers";
        String expected09 = "QUANTITY DESCRIPTION UNIT PRICE TOTAL";

        File imgFile = new File(path);
        File outputFile = new File(getTargetDirectory()
                + "hocrOutputFromHalftoneFile.hocr");

        tesseractReader.doTesseractOcr(imgFile, outputFile, OutputFormat.HOCR);
        Map<Integer, List<TextInfo>> pageData = TesseractHelper
                .parseHocrFile(Collections.<File>singletonList(outputFile), null,
                        new Tesseract4OcrEngineProperties().setTextPositioning(TextPositioning.BY_WORDS)
                );
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected01));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected02));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected03));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected04));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected05));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected06));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected07));

        pageData = TesseractHelper
                .parseHocrFile(Collections.<File>singletonList(outputFile), null,
                        new Tesseract4OcrEngineProperties().setTextPositioning(TextPositioning.BY_LINES)
                );
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected08));
        Assertions.assertTrue(findTextInPageData(pageData, 1, expected09));
    }

    /**
     * Searches for certain text in page data.
     */
    private boolean findTextInPageData(Map<Integer, List<TextInfo>> pageData, int page, String textToSearchFor) {
        for (TextInfo textInfo : pageData.get(page)) {
            if (textToSearchFor.equals(textInfo.getText())) {
                return true;
            }
        }
        return false;
    }

}
