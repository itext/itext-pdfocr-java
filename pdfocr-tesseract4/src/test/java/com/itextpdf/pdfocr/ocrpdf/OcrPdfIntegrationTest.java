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
package com.itextpdf.pdfocr.ocrpdf;

import com.itextpdf.commons.utils.StringNormalizer;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;

import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class OcrPdfIntegrationTest extends IntegrationTestHelper {

    private static final String TARGET_DIRECTORY = getTargetDirectory() + "OcrPdfIntegrationTest/";
    private static final String CMP_DIRECTORY = TEST_DOCUMENTS_DIRECTORY + "OcrPdfIntegrationTest/";
    private final AbstractTesseract4OcrEngine tesseractReader;
    private final String testType;

    public OcrPdfIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
        this.testType = StringNormalizer.toLowerCase(type.toString());
    }

    @BeforeAll
    public static void init() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @BeforeEach
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {
        makeSearchable("numbers");
    }

    @Test
    public void pageRotationTest() throws IOException, InterruptedException {
        makeSearchable("pageRotation");
    }

    @Test
    public void twoImagesTest() throws IOException, InterruptedException {
        makeSearchable("2images");
    }

    @Test
    public void twoPagesTest() throws IOException, InterruptedException {
        makeSearchable("2pages");
    }

    @Test
    public void rotatedTest() throws IOException, InterruptedException {
        // Tesseract doesn't return textangle, that is why the resulting text is not rotated here
        makeSearchable("rotated");
    }

    @Test
    public void mixedRotationTest() throws IOException, InterruptedException {
        makeSearchable("mixedRotation");
    }

    @Test
    public void notRecognizableTest() throws IOException, InterruptedException {
        makeSearchable("notRecognizable");
    }

    @Test
    public void imageIntersectionTest() throws IOException, InterruptedException {
        makeSearchable("imageIntersection");
    }

    @Test
    public void whiteTextTest() throws IOException, InterruptedException {
        // Not OCRed by tesseract
        makeSearchable("whiteText");
    }

    @Test
    public void changedImageProportionTest() throws IOException, InterruptedException {
        makeSearchable("changedImageProportion");
    }

    @Test
    public void textWithImagesTest() throws IOException, InterruptedException {
        makeSearchable("textWithImages");
    }

    @Test
    public void invisibleTextImageTest() throws IOException, InterruptedException {
        makeSearchable("invisibleTextImage");
    }

    @Test
    public void skewedRotated45Test() throws IOException, InterruptedException {
        makeSearchable("skewedRotated45");
    }

    private void makeSearchable(String fileName) throws InterruptedException, IOException {
        String path = TEST_PDFS_DIRECTORY + fileName + ".pdf";
        String expectedPdfPath = CMP_DIRECTORY + fileName + ".pdf";
        String resultPdfPath = TARGET_DIRECTORY + fileName + "_" + testType + ".pdf";

        doOcrAndSavePdfToPath(tesseractReader, path, resultPdfPath,
                Collections.<String>singletonList("eng"), null, DeviceCmyk.MAGENTA, false, false);

        Assertions.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
    }
}
