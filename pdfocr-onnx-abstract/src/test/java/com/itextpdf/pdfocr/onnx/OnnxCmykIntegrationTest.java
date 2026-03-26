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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
import com.itextpdf.pdfocr.onnx.util.OcrEngineType;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Tag("IntegrationTest")
public class OnnxCmykIntegrationTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OnnxCmykIntegrationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxTRCmykIntegrationTest/";
    private static OnnxOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
        OCR_ENGINE = OcrEngineType.DOCTR.get();
    }

    @Test
    public void rainbowInvertedCmykTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rainbow_inverted_cmyk.jpg";
        String dest = TARGET_DIRECTORY + "rainbowInvertedCmykTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rainbowInvertedCmykTest.txt";

        try {
            OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
            OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.05);
        } catch (PdfOcrInputException e) {
            // CMYK bug https://bugs.openjdk.org/browse/JDK-8274735 in openJDK:
            // fixed for jdk8 from 351 onwards, for jdk11 from 16 onwards and for jdk17 starting from 4.
            // Amazon corretto jdk started support CMYK for JPEG from 11 version.
            // Temurin 8 does not support CMYK for JPEG either.
            Assertions.assertEquals(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE, e.getMessage());
        }
    }

    @Test
    public void rainbowAdobeCmykTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rainbow_adobe_cmyk.jpg";
        String dest = TARGET_DIRECTORY + "rainbowAdobeCmykTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rainbowAdobeCmykTest.txt";

        try {
            OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
            OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.05);
        } catch (PdfOcrInputException e) {
            // CMYK bug https://bugs.openjdk.org/browse/JDK-8274735 in openJDK:
            // fixed for jdk8 from 351 onwards, for jdk11 from 16 onwards and for jdk17 starting from 4.
            // Amazon corretto jdk started support CMYK for JPEG from 11 version.
            // Temurin 8 does not support CMYK for JPEG either.
            Assertions.assertEquals(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE, e.getMessage());
        }
    }

    @Test
    public void rainbowCmykNoProfileTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rainbow_cmyk_inverted_no_profile.jpg";
        String dest = TARGET_DIRECTORY + "rainbowCmykNoProfileTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rainbowCmykNoProfileTest.txt";

        try {
            OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
            OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.05);
        } catch (PdfOcrInputException e) {
            // CMYK bug https://bugs.openjdk.org/browse/JDK-8274735 in openJDK:
            // fixed for jdk8 from 351 onwards, for jdk11 from 16 onwards and for jdk17 starting from 4.
            // Amazon corretto jdk started support CMYK for JPEG from 11 version.
            // Temurin 8 does not support CMYK for JPEG either.
            Assertions.assertEquals(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE, e.getMessage());
        }
    }
}
