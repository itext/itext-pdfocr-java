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
package com.itextpdf.pdfocr.pdfa3u;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.IntegrationTestHelper;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class PdfA3UIntegrationTest extends IntegrationTestHelper {

    // path to default cmyk color profile
    private static final String DEFAULT_CMYK_COLOR_PROFILE_PATH = TEST_DIRECTORY + "profiles/CoatedFOGRA27.icc";
    // path to default rgb color profile
    private static final String DEFAULT_RGB_COLOR_PROFILE_PATH = TEST_DIRECTORY + "profiles/sRGB_CS_profile.icm";

    AbstractTesseract4OcrEngine tesseractReader;

    public PdfA3UIntegrationTest(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Test
    public void comparePdfA3uCMYKColorSpaceJPG() throws IOException,
            InterruptedException {
        String testName = "comparePdfA3uCMYKColorSpaceJPG";
        String filename = "numbers_01";
        String expectedPdfPath = TEST_DOCUMENTS_DIRECTORY + filename + "_a3u.pdf";
        String resultPdfPath = getTargetDirectory() + filename + "_" + testName + "_a3u.pdf";

        try {
            OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
            ocrPdfCreatorProperties.setPdfLang("en-US");
            ocrPdfCreatorProperties.setTitle("");

            OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                    ocrPdfCreatorProperties);

            tesseractReader.setTesseract4OcrEngineProperties(
                    tesseractReader.getTesseract4OcrEngineProperties()
                            .setTextPositioning(TextPositioning.BY_WORDS));
            Assertions.assertEquals(tesseractReader, ocrPdfCreator.getOcrEngine());
            ocrPdfCreator.setOcrEngine(tesseractReader);
            PdfDocument doc =
                    ocrPdfCreator.createPdfA(
                            Collections.<File>singletonList(
                            new File(TEST_IMAGES_DIRECTORY
                                    + filename + ".jpg")),
                            getPdfWriter(resultPdfPath),
                            getCMYKPdfOutputIntent());
            Assertions.assertNotNull(doc);
            doc.close();

            Assertions.assertNull(new CompareTool()
                    .compareByContent(resultPdfPath, expectedPdfPath,
                            getTargetDirectory(), "diff_"));
        } finally {
            Assertions.assertEquals(TextPositioning.BY_WORDS,
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
        properties.setPathToTessData(getTessDataDirectory());
        properties.setLanguages(Collections.<String>singletonList("spa"));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        OcrPdfCreatorProperties ocrPdfCreatorProperties = new OcrPdfCreatorProperties();
        ocrPdfCreatorProperties.setPdfLang("en-US");
        ocrPdfCreatorProperties.setTitle("");
        ocrPdfCreatorProperties.setTextColor(DeviceRgb.BLACK);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                ocrPdfCreatorProperties);

        PdfDocument doc = ocrPdfCreator.createPdfA(
                Collections.<File>singletonList(
                        new File(TEST_IMAGES_DIRECTORY + filename
                                + ".jpg")), getPdfWriter(resultPdfPath),
                getRGBPdfOutputIntent());
        Assertions.assertNotNull(doc);
        doc.close();

        Assertions.assertNull(new CompareTool().compareByContent(resultPdfPath,
                expectedPdfPath, getTargetDirectory(), "diff_"));
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
