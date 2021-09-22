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

import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.AbstractProductProcessITextEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class OcrPdfCreatorEventHelperTest extends ExtendedITextTest {
    private static final ProductData DUMMY_PRODUCT_DATA =
            new ProductData("test-product", "inner_product", "1.0.0", 1900, 2100);

    @Test
    public void productContextBasedEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        DummyITextEvent event = new DummyITextEvent();
        helper.onEvent(event);

        // TODO DEVSIX-5887 assert event reached EventManager
    }

    @Test
    public void statisticsEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        PdfOcrOutputTypeStatisticsEvent e = new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF, DUMMY_PRODUCT_DATA);
        helper.onEvent(e);

        // TODO DEVSIX-5887 assert event didn't reach EventManager
    }

    @Test
    public void customProductEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        AbstractProductITextEvent event = new CustomProductITextEvent(DUMMY_PRODUCT_DATA);
        helper.onEvent(event);

        // TODO DEVSIX-5887 assert event reached reach EventManager
    }

    private static class DummyMetaInfo implements IMetaInfo {
    }

    private static class DummyITextEvent extends AbstractProductProcessITextEvent {
        protected DummyITextEvent() {
            super(ITextCoreProductData.getInstance(), null, EventConfirmationType.ON_DEMAND);
        }

        @Override
        public String getEventType() {
            return "test-event";
        }

    }

    private static class CustomProductITextEvent extends AbstractProductITextEvent {
        protected CustomProductITextEvent(ProductData productData) {
            super(productData);
        }
    }
}
