/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.IEventHandler;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.pdfocr.tesseract4.actions.data.PdfOcrTesseract4ProductData;
import com.itextpdf.pdfocr.tesseract4.actions.events.PdfOcrTesseract4ProductEvent;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class Tesseract4FileResultEventHelperTest extends ExtendedITextTest {

    @Test
    public void defaultProcessImageEventTest() {
        StoreEventsHandler eventsHandler = new StoreEventsHandler();
        EventManager.getInstance().register(eventsHandler);
        Tesseract4FileResultEventHelper helper = new Tesseract4FileResultEventHelper();
        helper.onEvent(PdfOcrTesseract4ProductEvent.createProcessImageEvent(new SequenceId(), null,
                EventConfirmationType.ON_CLOSE));
        Assert.assertEquals(0, eventsHandler.getEvents().size());
        EventManager.getInstance().unregister(eventsHandler);
    }

    @Test
    public void defaultStatisticsEventTest() {
        StoreEventsHandler eventsHandler = new StoreEventsHandler();
        EventManager.getInstance().register(eventsHandler);
        Tesseract4FileResultEventHelper helper = new Tesseract4FileResultEventHelper();
        helper.onEvent(new PdfOcrOutputTypeStatisticsEvent(PdfOcrOutputType.PDF,
                PdfOcrTesseract4ProductData.getInstance()));
        Assert.assertEquals(1, eventsHandler.getEvents().size());
        EventManager.getInstance().unregister(eventsHandler);
    }

    protected static class StoreEventsHandler implements IEventHandler {
        private final List<IEvent> events = new ArrayList<>();

        public List<IEvent> getEvents() {
            return events;
        }

        @Override
        public void onEvent(IEvent event) {
            if (event instanceof PdfOcrTesseract4ProductEvent
                    || event instanceof PdfOcrOutputTypeStatisticsEvent
                    || event instanceof ConfirmEvent) {
                events.add(event);
            }
        }
    }
}
