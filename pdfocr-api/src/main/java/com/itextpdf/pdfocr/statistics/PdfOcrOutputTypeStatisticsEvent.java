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
package com.itextpdf.pdfocr.statistics;

import com.itextpdf.commons.actions.AbstractStatisticsAggregator;
import com.itextpdf.commons.actions.AbstractStatisticsEvent;
import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.pdfocr.exceptions.OcrException;

import java.util.Collections;
import java.util.List;

/**
 * Class which represents an event for specifying type of an ocr processing.
 * For internal usage only.
 */
public class PdfOcrOutputTypeStatisticsEvent extends AbstractStatisticsEvent {

    private static final String OCR_OUTPUT_TYPE = "ocrOutput";

    private final PdfOcrOutputType type;

    /**
     * Creates instance of pdfOcr statistics event.
     *
     * @param type pdfCcr output type
     * @param productData product data
     */
    public PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType type, ProductData productData) {
        super(productData);
        if (type == null) {
            throw new OcrException(OcrException.STATISTICS_EVENT_TYPE_CANT_BE_NULL);
        }
        if (null == PdfOcrOutputTypeStatisticsAggregator.getKeyForType(type)) {
            throw new OcrException(OcrException.STATISTICS_EVENT_TYPE_IS_NOT_DETECTED);
        }
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractStatisticsAggregator createStatisticsAggregatorFromName(String statisticsName) {
        if (OCR_OUTPUT_TYPE.equals(statisticsName)) {
            return new PdfOcrOutputTypeStatisticsAggregator();
        }
        return super.createStatisticsAggregatorFromName(statisticsName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getStatisticsNames() {
        return Collections.singletonList(OCR_OUTPUT_TYPE);
    }

    /**
     * Gets the type of statistic event.
     *
     * @return the statistics event type
     */
    public PdfOcrOutputType getPdfOcrStatisticsEventType() {
        return type;
    }
}
