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

import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.tesseract4.events.PdfOcrTesseract4Event;
import com.itextpdf.test.annotations.type.UnitTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class PdfOcrTesseract4EventTest extends IntegrationTestHelper {

    private static final String PDF_OCR_TESSERACT4_ORIGIN_ID = "com.itextpdf.pdfocr.tesseract4";

    @Test
    public void testEventTypes() {
        String[] expectedTypes = {"pdfOcr-tesseract4-image-ocr", "pdfOcr-tesseract4-image-to-pdf", "pdfOcr-tesseract4-image-to-pdfa"};
        PdfOcrTesseract4Event[] testedEvents = {PdfOcrTesseract4Event.TESSERACT4_IMAGE_OCR,
                PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF, PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDFA};

        for (int i = 0; i < testedEvents.length; i++) {
            Assert.assertEquals(expectedTypes[i], testedEvents[i].getEventType());
        }
    }

    @Test
    public void testOriginId() {
        String expected = PDF_OCR_TESSERACT4_ORIGIN_ID;
        PdfOcrTesseract4Event[] testedEvents = {PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF,
                PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF, PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDFA};

        for (PdfOcrTesseract4Event event : testedEvents) {
            Assert.assertEquals(expected, event.getOriginId());
        }
    }
}
