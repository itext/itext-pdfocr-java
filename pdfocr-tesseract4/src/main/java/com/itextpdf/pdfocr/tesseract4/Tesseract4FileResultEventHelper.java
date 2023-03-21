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

import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.tesseract4.actions.events.PdfOcrTesseract4ProductEvent;

/**
 * Helper class for working with events.
 */
class Tesseract4FileResultEventHelper extends AbstractPdfOcrEventHelper {

    private AbstractPdfOcrEventHelper wrappedEventHelper;

    Tesseract4FileResultEventHelper() {
        this(null);
    }

    Tesseract4FileResultEventHelper(AbstractPdfOcrEventHelper wrappedEventHelper) {
        this.wrappedEventHelper = wrappedEventHelper == null ? new Tesseract4EventHelper() : wrappedEventHelper;
    }

    @Override
    public void onEvent(AbstractProductITextEvent event) {
        if (!isProcessImageEvent(event)
                && !isConfirmForProcessImageEvent(event)) {
            wrappedEventHelper.onEvent(event);
        }
    }

    @Override
    public SequenceId getSequenceId() {
        return wrappedEventHelper.getSequenceId();
    }

    @Override
    public EventConfirmationType getConfirmationType() {
        return wrappedEventHelper.getConfirmationType();
    }

    private static boolean isProcessImageEvent(AbstractProductITextEvent event) {
        return event instanceof PdfOcrTesseract4ProductEvent
                && PdfOcrTesseract4ProductEvent.PROCESS_IMAGE.equals(
                ((PdfOcrTesseract4ProductEvent) event).getEventType());
    }

    private static boolean isConfirmForProcessImageEvent(AbstractProductITextEvent event) {
        return event instanceof ConfirmEvent
                && ((ConfirmEvent) event).getConfirmedEvent() instanceof PdfOcrTesseract4ProductEvent
                && PdfOcrTesseract4ProductEvent.PROCESS_IMAGE.equals(
                ((ConfirmEvent) event).getConfirmedEvent().getEventType());
    }
}
