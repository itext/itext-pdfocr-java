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
package com.itextpdf.pdfocr.statistics;

import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.commons.logs.CommonsLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("UnitTest")
public class PdfOcrOutputTypeStatisticsEventTest extends ExtendedITextTest {
    private static final ProductData DUMMY_PRODUCT_DATA =
            new ProductData("test-product", "inner_product", "1.0.0", 1900, 2100);

    @Test
    public void defaultEventTest() {
        PdfOcrOutputTypeStatisticsEvent event = new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA);

        Assertions.assertEquals(PdfOcrOutputType.PDF, event.getPdfOcrStatisticsEventType());
        Assertions.assertEquals(Collections.singletonList("ocrOutput"), event.getStatisticsNames());
        Assertions.assertEquals(
                PdfOcrOutputTypeStatisticsAggregator.class, event.createStatisticsAggregatorFromName("ocrOutput").getClass());
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CommonsLogMessageConstant.INVALID_STATISTICS_NAME))
    public void invalidAggregatorNameTest() {
        Assertions.assertNull(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA)
                .createStatisticsAggregatorFromName("dummy name"));
    }
}
