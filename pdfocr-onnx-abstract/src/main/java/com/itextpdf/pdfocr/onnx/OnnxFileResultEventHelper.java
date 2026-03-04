/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.onnx.actions.events.PdfOcrOnnxProductEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for working with events.
 */
final class OnnxFileResultEventHelper extends AbstractPdfOcrEventHelper {
    private final AbstractPdfOcrEventHelper wrappedEventHelper;
    private final List<ConfirmEvent> events;

    OnnxFileResultEventHelper(AbstractPdfOcrEventHelper wrappedEventHelper) {
        this.wrappedEventHelper = wrappedEventHelper == null ? new OnnxEventHelper() : wrappedEventHelper;
        this.events = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEvent(AbstractProductITextEvent event) {
        if (isConfirmForProcessImageOnnxEvent(event)) {
            events.add((ConfirmEvent) event);
        } else {
            wrappedEventHelper.onEvent(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SequenceId getSequenceId() {
        return wrappedEventHelper.getSequenceId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventConfirmationType getConfirmationType() {
        return wrappedEventHelper.getConfirmationType();
    }

    /**
     * Register all previously saved events to wrapped {@link AbstractPdfOcrEventHelper}.
     */
    public void registerAllSavedEvents() {
        for (AbstractProductITextEvent event : events) {
            wrappedEventHelper.onEvent(event);
        }
    }

    private static boolean isConfirmForProcessImageOnnxEvent(AbstractProductITextEvent event) {
        return event instanceof ConfirmEvent &&
                ((ConfirmEvent) event).getConfirmedEvent() instanceof PdfOcrOnnxProductEvent &&
                PdfOcrOnnxProductEvent.PROCESS_IMAGE_ONNX.equals(((ConfirmEvent) event).getConfirmedEvent().getEventType());
    }
}
