/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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
package com.itextpdf.pdfocr.events;

import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.metainfo.TestMetaInfo;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class EventCountingTest extends ExtendedITextTest {

    protected static final String PROFILE_FOLDER = "./src/test/resources/com/itextpdf/pdfocr/profiles/";
    protected static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/pdfocr/events/";

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    private IOcrEngine tesseractReader;

    public EventCountingTest() {
        tesseractReader = new CustomOcrEngine();
    }

    @Test
    public void testEventCountingPdfEvent() {
        ((CustomOcrEngine) tesseractReader).setThreadLocalMetaInfo(new TestMetaInfo());

        doImageToPdfOcr(tesseractReader, getTestImageFile());

        Assert.assertTrue(((CustomOcrEngine) tesseractReader).getThreadLocalMetaInfo() instanceof TestMetaInfo);
    }

    @Test
    public void testEventCountingPdfAEvent() {
        ((CustomOcrEngine) tesseractReader).setThreadLocalMetaInfo(new TestMetaInfo());

        doImageToPdfAOcr(tesseractReader, getTestImageFile());

        Assert.assertTrue(((CustomOcrEngine) tesseractReader).getThreadLocalMetaInfo() instanceof TestMetaInfo);
    }

    @Test
    public void testEventCountingImageEvent() {
        ((CustomOcrEngine) tesseractReader).setThreadLocalMetaInfo(new TestMetaInfo());

        doImageOcr(tesseractReader, getTestImageFile());

        Assert.assertTrue(((CustomOcrEngine) tesseractReader).getThreadLocalMetaInfo() instanceof TestMetaInfo);
    }

    private static void doImageOcr(IOcrEngine tesseractReader, File imageFile) {
        tesseractReader.doImageOcr(imageFile);
    }

    private static void doImageToPdfOcr(IOcrEngine tesseractReader, File imageFile) {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        ocrPdfCreator.createPdf(Arrays.asList(imageFile), new PdfWriter(new ByteArrayOutputStream()));
    }

    private static void doImageToPdfAOcr(IOcrEngine tesseractReader, File imageFile) {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                new OcrPdfCreatorProperties().setPdfLang("en-US"));
        InputStream is = null;
        try {
            is = new FileInputStream(PROFILE_FOLDER + "sRGB_CS_profile.icm");
        } catch (FileNotFoundException e) {
            // No expected
        }
        PdfOutputIntent outputIntent = new PdfOutputIntent("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1",
                is);

        ocrPdfCreator.createPdfA(Arrays.asList(imageFile), new PdfWriter(new ByteArrayOutputStream()), outputIntent);
    }

    private static File getTestImageFile() {
        String imgPath = SOURCE_FOLDER + "numbers_01.jpg";
        return new File(imgPath);
    }
}
