/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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

import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.AbstractProductProcessITextEvent;
import com.itextpdf.commons.actions.AbstractStatisticsEvent;
import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.IEventHandler;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.test.ExtendedITextTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("UnitTest")
public class OcrPdfCreatorEventHelperTest extends ExtendedITextTest {
    private static final ProductData DUMMY_PRODUCT_DATA =
            new ProductData("test-product", "inner_product", "1.0.0", 1900, 2100);
    private StoreEventsHandler storeEventsHandler;

    @BeforeEach
    public void before() {
        storeEventsHandler = new StoreEventsHandler();
        EventManager.getInstance().register(storeEventsHandler);
    }

    @AfterEach
    public void after() {
        EventManager.getInstance().unregister(storeEventsHandler);
        storeEventsHandler = null;
    }

    @Test
    public void productContextBasedEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        DummyITextEvent event = new DummyITextEvent();
        helper.onEvent(event);

        Assertions.assertEquals(1, storeEventsHandler.getEvents().size());
        Assertions.assertEquals(event, storeEventsHandler.getEvents().get(0));
    }

    @Test
    public void pdfOcrStatisticsEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        PdfOcrOutputTypeStatisticsEvent e = new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF,
                DUMMY_PRODUCT_DATA);
        helper.onEvent(e);

        Assertions.assertEquals(0, storeEventsHandler.getEvents().size());
    }

    @Test
    public void customProductEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        AbstractProductITextEvent event = new CustomProductITextEvent(DUMMY_PRODUCT_DATA);
        helper.onEvent(event);

        Assertions.assertEquals(1, storeEventsHandler.getEvents().size());
        Assertions.assertEquals(event, storeEventsHandler.getEvents().get(0));
    }

    @Test
    public void customStatisticsEventTest() {
        OcrPdfCreatorEventHelper helper = new OcrPdfCreatorEventHelper(new SequenceId(), new DummyMetaInfo());
        CustomStatisticsEvent event = new CustomStatisticsEvent(DUMMY_PRODUCT_DATA);
        helper.onEvent(event);

        Assertions.assertEquals(1, storeEventsHandler.getEvents().size());
        Assertions.assertEquals(event, storeEventsHandler.getEvents().get(0));
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

    private static class CustomStatisticsEvent extends AbstractStatisticsEvent {

        protected CustomStatisticsEvent(ProductData productData) {
            super(productData);
        }

        @Override
        public List<String> getStatisticsNames() {
            return Collections.singletonList("custom-statistics");
        }
    }

    private static class StoreEventsHandler implements IEventHandler {
        private List<IEvent> events = new ArrayList<>();

        public List<IEvent> getEvents() {
            return events;
        }

        @Override
        public void onEvent(IEvent event) {
            events.add(event);
        }
    }
}
