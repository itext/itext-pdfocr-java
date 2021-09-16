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
package com.itextpdf.pdfocr.statisctics;

import com.itextpdf.commons.actions.AbstractStatisticsAggregator;
import com.itextpdf.commons.actions.AbstractStatisticsEvent;
import com.itextpdf.commons.utils.MapUtil;
import com.itextpdf.pdfocr.OcrException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Statistics aggregator which aggregates types of ocr processing.
 */
public class PdfOcrOutputTypeStatisticsAggregator extends AbstractStatisticsAggregator {

    private static final String STRING_FOR_DATA = "data";
    private static final String STRING_FOR_PDF = "pdf";
    private static final String STRING_FOR_PDFA = "pdfa";

    private static final Map<PdfOcrOutputType, String> OCR_OUTPUT_TYPES;

    static {
        Map<PdfOcrOutputType, String> temp = new HashMap<>();
        temp.put(PdfOcrOutputType.DATA, STRING_FOR_DATA);
        temp.put(PdfOcrOutputType.PDF, STRING_FOR_PDF);
        temp.put(PdfOcrOutputType.PDFA, STRING_FOR_PDFA);
        OCR_OUTPUT_TYPES = Collections.unmodifiableMap(temp);
    }

    private final Object lock = new Object();

    private final Map<String, Long> numberOfUsagesPerType = new LinkedHashMap<>();

    /**
     * Aggregates pdfOcr event type.
     *
     * @param event {@link PdfOcrOutputTypeStatisticsEvent} instance
     */
    @Override
    public void aggregate(AbstractStatisticsEvent event) {
        if (!(event instanceof PdfOcrOutputTypeStatisticsEvent)) {
            return;
        }
        // the event's properties are required to be not null
        PdfOcrOutputType type = ((PdfOcrOutputTypeStatisticsEvent) event).getPdfOcrStatisticsEventType();
        String fileTypeKey = getKeyForType(type);
        if (null == fileTypeKey) {
            // this line is not expected to be reached, since an exception should have been thrown on event creation
            throw new OcrException(OcrException.STATISTICS_EVENT_TYPE_IS_NOT_DETECTED);
        }
        synchronized (lock) {
            Long documentsOfThisRange = numberOfUsagesPerType.get(fileTypeKey);
            Long currentValue = documentsOfThisRange == null ? 1L : (documentsOfThisRange + 1L);
            numberOfUsagesPerType.put(fileTypeKey, currentValue);
        }
    }

    /**
     * Retrieves Map where keys are pdfOcr event types and values are the amounts of such events.
     *
     * @return aggregated {@link Map}
     */
    @Override
    public Object retrieveAggregation() {
        return Collections.unmodifiableMap(numberOfUsagesPerType);
    }

    /**
     * Merges data about amounts of pdfOcr event types from the provided aggregator into this aggregator.
     *
     * @param aggregator {@link PdfOcrOutputTypeStatisticsAggregator} from which data will be taken.
     */
    @Override
    public void merge(AbstractStatisticsAggregator aggregator) {
        if (!(aggregator instanceof PdfOcrOutputTypeStatisticsAggregator)) {
            return;
        }

        Map<String, Long> otherNumberOfFiles = ((PdfOcrOutputTypeStatisticsAggregator) aggregator).numberOfUsagesPerType;
        synchronized (lock) {
            MapUtil.merge(this.numberOfUsagesPerType, otherNumberOfFiles, (el1, el2) -> {
                if (el2 == null) {
                    return el1;
                } else {
                    return el1 + el2;
                }
            });
        }
    }

    static String getKeyForType(PdfOcrOutputType type) {
        return OCR_OUTPUT_TYPES.get(type);
    }
}
