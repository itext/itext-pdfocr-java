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
package com.itextpdf.pdfocr;

/**
 * Class for storing ocr processing context.
 */
public class OcrProcessContext {
    private AbstractPdfOcrEventHelper ocrEventHelper;

    private IOcrProcessProperties ocrProcessProperties;

    /**
     * Creates an instance of ocr process context
     *
     * @param eventHelper helper class for working with events
     */
    public OcrProcessContext(AbstractPdfOcrEventHelper eventHelper) {
        this.ocrEventHelper = eventHelper;
    }

    /**
     * Returns helper for working with events.
     *
     * @return an instance of {@link AbstractPdfOcrEventHelper}
     */
    public AbstractPdfOcrEventHelper getOcrEventHelper() {
        return ocrEventHelper;
    }

    /**
     * Sets ocr event helper.
     *
     * @param eventHelper event helper
     */
    public void setOcrEventHelper(AbstractPdfOcrEventHelper eventHelper) {
        this.ocrEventHelper = eventHelper;
    }

    /**
     * Set extra OCR process properties.
     *
     * @param ocrProcessProperties extra OCR process properties.
     */
    void setOcrProcessProperties(IOcrProcessProperties ocrProcessProperties) {
        this.ocrProcessProperties = ocrProcessProperties;
    }

    /**
     * Get extra OCR process properties.
     *
     * @return extra OCR process properties.
     */
    public IOcrProcessProperties getOcrProcessProperties() {
        return ocrProcessProperties;
    }
}
