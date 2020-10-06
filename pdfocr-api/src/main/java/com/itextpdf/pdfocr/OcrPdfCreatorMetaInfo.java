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
package com.itextpdf.pdfocr;

import com.itextpdf.kernel.counter.event.IMetaInfo;

import java.util.UUID;

/**
 * The meta info that is used internally by pdfOcr to pass a wrapped custom meta data
 */
public class OcrPdfCreatorMetaInfo implements IMetaInfo, IMetaInfoWrapper {

    private IMetaInfo wrappedMetaInfo;
    private UUID uuid;
    private PdfDocumentType pdfDocumentType;

    /**
     * Creates an inner meta info wrapper
     *
     * @param wrappedMetaInfo the meta info to be wrapped
     * @param uuid a unique String which corresponds to the ocr event for which this meta info is passed
     * @param pdfDocumentType a type of the document which is created during the corresponding ocr event
     */
    public OcrPdfCreatorMetaInfo(IMetaInfo wrappedMetaInfo, UUID uuid, PdfDocumentType pdfDocumentType) {
        this.wrappedMetaInfo = wrappedMetaInfo;
        this.uuid = uuid;
        this.pdfDocumentType = pdfDocumentType;
    }

    /**
     * Gets the unique String which corresponds to the ocr event for which this meta info is passed
     * @return the unique String which corresponds to the ocr event for which this meta info is passed
     */
    public UUID getDocumentId() {
        return uuid;
    }

    /**
     * Gets the type of the document which is created during the corresponding ocr event
     * @return the type of the document which is created during the corresponding ocr event
     */
    public PdfDocumentType getPdfDocumentType() {
        return pdfDocumentType;
    }

    @Override
    /**
     * Gets the wrapped meta info
     * @return the wrapped meta info
     */
    public IMetaInfo getWrappedMetaInfo() {
        return wrappedMetaInfo;
    }

    /**
     * The enum which represents types of documents, for which pdfOcr sends different events
     */
    public enum PdfDocumentType {
        PDF, PDFA;
    }
}
