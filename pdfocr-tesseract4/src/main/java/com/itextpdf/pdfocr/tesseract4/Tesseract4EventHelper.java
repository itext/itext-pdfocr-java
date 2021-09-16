package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.commons.actions.AbstractContextBasedITextEvent;
import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;

/**
 * Helper class for working with events.
 */
class Tesseract4EventHelper extends AbstractPdfOcrEventHelper {

    @Override
    public void onEvent(AbstractProductITextEvent event) {
        if (event instanceof AbstractContextBasedITextEvent) {
            ((AbstractContextBasedITextEvent) event).setMetaInfo(new Tesseract4MetaInfo());
        }
        EventManager.getInstance().onEvent(event);
    }

    @Override
    public SequenceId getSequenceId() {
        return new SequenceId();
    }

    @Override
    public EventConfirmationType getConfirmationType() {
        return EventConfirmationType.ON_DEMAND;
    }
}
