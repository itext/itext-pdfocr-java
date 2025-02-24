/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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

import com.itextpdf.commons.actions.AbstractContextBasedITextEvent;
import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;

class OcrPdfCreatorEventHelper extends AbstractPdfOcrEventHelper {
    private final SequenceId sequenceId;
    private final IMetaInfo metaInfo;

    OcrPdfCreatorEventHelper(SequenceId sequenceId, IMetaInfo metaInfo) {
        this.sequenceId = sequenceId;
        this.metaInfo = metaInfo;
    }

    @Override
    public void onEvent(AbstractProductITextEvent event) {
        if (event instanceof AbstractContextBasedITextEvent) {
            ((AbstractContextBasedITextEvent) event).setMetaInfo(this.metaInfo);
        } else if (event instanceof PdfOcrOutputTypeStatisticsEvent) {
            // do nothing as we would
            return;
        }
        EventManager.getInstance().onEvent(event);
    }

    @Override
    public SequenceId getSequenceId() {
        return sequenceId;
    }

    @Override
    public EventConfirmationType getConfirmationType() {
        return EventConfirmationType.ON_CLOSE;
    }
}
