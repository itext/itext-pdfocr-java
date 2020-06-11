/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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
package com.itextpdf.pdfocr.tesseract4.events;

import com.itextpdf.kernel.counter.event.IGenericEvent;

/**
 * Class for ocr events
 */
public class PdfOcrTesseract4Event implements IGenericEvent {

    public static final PdfOcrTesseract4Event TESSERACT4_IMAGE_OCR = new PdfOcrTesseract4Event("tesseract4-image-ocr");
    public static final PdfOcrTesseract4Event TESSERACT4_IMAGE_TO_PDF = new PdfOcrTesseract4Event("tesseract4-image-to-pdf");
    public static final PdfOcrTesseract4Event TESSERACT4_IMAGE_TO_PDFA = new PdfOcrTesseract4Event("tesseract4-image-to-pdfa");

    private static final String PDF_OCR_TESSERACT4_ORIGIN_ID = "com.itextpdf.pdfocr.tesseract4";

    private final String subtype;

    private PdfOcrTesseract4Event(String subtype) {
        this.subtype = subtype;
    }

    @Override
    /**
     * Gets the type of the event
     * @return the event type
     */
    public String getEventType() {
        return "pdfOcr-" + subtype;
    }

    @Override
    /**
     * Gets the origin id of the event
     * @return the origin id
     */
    public String getOriginId() {
        return PDF_OCR_TESSERACT4_ORIGIN_ID;
    }
}
