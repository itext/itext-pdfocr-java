/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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
