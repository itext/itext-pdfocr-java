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

    Tesseract4EventHelper() {
        // do nothing
    }

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
