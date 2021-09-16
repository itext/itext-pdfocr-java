package com.itextpdf.pdfocr;

import com.itextpdf.commons.actions.AbstractITextEvent;
import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.sequence.SequenceId;

/**
 * Helper class for working with events. This class is for internal usage.
 */
public abstract class AbstractPdfOcrEventHelper extends AbstractITextEvent {

    /**
     * Handles the event.
     *
     * @param event event
     */
    public abstract void onEvent(AbstractProductITextEvent event);

    /**
     * Returns the sequence id
     *
     * @return sequence id
     */
    public abstract SequenceId getSequenceId();

    /**
     * Returns the confirmation type of event.
     *
     * @return event confirmation type
     */
    public abstract EventConfirmationType getConfirmationType();
}
