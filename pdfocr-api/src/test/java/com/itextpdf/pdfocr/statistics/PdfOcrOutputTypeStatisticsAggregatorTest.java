/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class PdfOcrOutputTypeStatisticsAggregatorTest extends ExtendedITextTest {
    private static final ProductData DUMMY_PRODUCT_DATA =
            new ProductData("test-product", "inner_product", "1.0.0", 1900, 2100);

    @Test
    public void aggregateEventTest() {
        PdfOcrOutputTypeStatisticsAggregator aggregator = new PdfOcrOutputTypeStatisticsAggregator();
        aggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA));
        aggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDFA, DUMMY_PRODUCT_DATA));
        aggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.DATA, DUMMY_PRODUCT_DATA));
        aggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDFA, DUMMY_PRODUCT_DATA));
        aggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA));

        Map<String, Long> aggregation = (Map<String, Long>) aggregator.retrieveAggregation();

        Assert.assertEquals(3, aggregation.size());

        Long numberOfOcrProcessesWithGivenOutput = aggregation.get("data");
        Assert.assertEquals(new Long(1L), numberOfOcrProcessesWithGivenOutput);

        numberOfOcrProcessesWithGivenOutput = aggregation.get("pdf");
        Assert.assertEquals(new Long(2L), numberOfOcrProcessesWithGivenOutput);

        numberOfOcrProcessesWithGivenOutput = aggregation.get("pdfa");
        Assert.assertEquals(new Long(2L), numberOfOcrProcessesWithGivenOutput);
    }

    @Test
    public void mergeTest() {
        PdfOcrOutputTypeStatisticsAggregator firstAggregator = new PdfOcrOutputTypeStatisticsAggregator();
        PdfOcrOutputTypeStatisticsAggregator secondAggregator = new PdfOcrOutputTypeStatisticsAggregator();

        firstAggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA));
        firstAggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDFA, DUMMY_PRODUCT_DATA));
        secondAggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.DATA, DUMMY_PRODUCT_DATA));
        secondAggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDFA, DUMMY_PRODUCT_DATA));
        secondAggregator.aggregate(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA));

        firstAggregator.merge(secondAggregator);

        Map<String, Long> aggregation = (Map<String, Long>) firstAggregator.retrieveAggregation();

        Assert.assertEquals(3, aggregation.size());

        Long numberOfOcrProcessesWithGivenOutput = aggregation.get("data");
        Assert.assertEquals(new Long(1L), numberOfOcrProcessesWithGivenOutput);

        numberOfOcrProcessesWithGivenOutput = aggregation.get("pdf");
        Assert.assertEquals(new Long(2L), numberOfOcrProcessesWithGivenOutput);

        numberOfOcrProcessesWithGivenOutput = aggregation.get("pdfa");
        Assert.assertEquals(new Long(2L), numberOfOcrProcessesWithGivenOutput);
    }

    @Test
    public void aggregateInvalidEventTest() {
        PdfOcrOutputTypeStatisticsAggregator aggregator = new PdfOcrOutputTypeStatisticsAggregator();
        aggregator.aggregate(new DummyAbstractStatisticsEvent(DUMMY_PRODUCT_DATA));
        Assert.assertTrue(((Map<String, Long>) aggregator.retrieveAggregation()).isEmpty());

    }

    @Test
    public void mergeInvalidAggregatorTest() {
        PdfOcrOutputTypeStatisticsAggregator aggregator = new PdfOcrOutputTypeStatisticsAggregator();
        aggregator.merge(new DummyAbstractStatisticsAggregator());
        Assert.assertTrue(((Map<String, Long>) aggregator.retrieveAggregation()).isEmpty());
    }

    private static class DummyAbstractStatisticsEvent extends AbstractStatisticsEvent {
        protected DummyAbstractStatisticsEvent(ProductData productData) {
            super(productData);
        }

        @Override
        public List<String> getStatisticsNames() {
            return null;
        }
    }

    private static class DummyAbstractStatisticsAggregator extends AbstractStatisticsAggregator {
        @Override
        public void aggregate(AbstractStatisticsEvent event) {

        }

        @Override
        public Object retrieveAggregation() {
            return null;
        }

        @Override
        public void merge(AbstractStatisticsAggregator aggregator) {

        }
    }

}
