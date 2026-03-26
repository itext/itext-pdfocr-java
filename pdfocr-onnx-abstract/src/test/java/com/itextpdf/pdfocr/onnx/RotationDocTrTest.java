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

import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.onnx.util.OcrEngineTypeWithOrientation;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Tag("IntegrationTest")
public class RotationDocTrTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/RotationTest/";
    private static final String TEST_IMAGE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/RotationDocTrTest/";

    private static final IOcrEngine OCR_ENGINE = OcrEngineTypeWithOrientation.DOCTR.get();
    private static final String OCR_NAME = "DocTR";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @Test
    public void rotatedTextBasicTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedTextBasic.png";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotatedTextBasicTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotatedTextBasicTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rotatedTextBasicTest.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        OnnxTestUtils.comparePdfs(dest, cmp, TARGET_DIRECTORY);
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.22);
    }

    @Test
    public void rotated90Test() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "90_degrees_rotated.jpg";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotated90Test.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotated90Test.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotated90Test.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.55);
    }

    @Test
    public void rotated180Test() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "180_degrees_rotated.jpg";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotated180Test.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotated180Test.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rotated180Test.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.05);
    }

    @Test
    public void rotated270Test() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "270_degrees_rotated.jpg";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotated270Test.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rotated270Test.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.58);
    }

    @Test
    public void rotatedColorsMixTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedColorsMix.png";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotatedColorsMixTest.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotatedColorsMixTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rotatedColorsMixTest.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        Assertions.assertNull(new CompareTool().compareByContent(dest, cmp, TARGET_DIRECTORY, "diff_"));
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.2);
    }

    @Test
    public void rotatedColorsMix2Test() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedColorsMix2.png";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotatedColorsMix2Test.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rotatedColorsMix2Test.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.6);
    }

    @Test
    public void rotatedCapsLCTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedCapsLC.png";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotatedCapsLCTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotatedCapsLCTest.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.46);
    }

    @Test
    public void rotatedBy90DegreesTest() throws IOException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedBy90Degrees.png";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotatedBy90DegreesTest.pdf";
        String cmpTxt = TEST_DIRECTORY + "cmp_rotatedBy90DegreesTest.txt";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        OnnxTestUtils.extractTextAndCompare(dest, cmpTxt, "Text1", 0.16);
    }

    @Test
    public void rotatedLinesTest() throws IOException, InterruptedException {
        String src = TEST_IMAGE_DIRECTORY + "rotatedLines.jpeg";
        String dest = TARGET_DIRECTORY + OCR_NAME + "_rotatedLines.pdf";
        String cmp = TEST_DIRECTORY + "cmp_" + OCR_NAME + "_rotatedLines.pdf";

        OnnxTestUtils.doOcrAndCreatePdf(src, dest, OCR_ENGINE);
        OnnxTestUtils.comparePdfs(dest, cmp, TARGET_DIRECTORY);
    }
}
