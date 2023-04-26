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
package com.itextpdf.pdfocr.actions.events;

import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfocr.tesseract4.actions.data.PdfOcrTesseract4ProductData;
import com.itextpdf.pdfocr.tesseract4.actions.events.PdfOcrTesseract4ProductEvent;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class PdfOcrTesseract4ProductEventTest extends ExtendedITextTest {
    @Test
    public void eventTypeTest() {
        PdfOcrTesseract4ProductEvent e = PdfOcrTesseract4ProductEvent
                .createProcessImageEvent(new SequenceId(), null, EventConfirmationType.ON_DEMAND);
        Assert.assertEquals(PdfOcrTesseract4ProductEvent.PROCESS_IMAGE, e.getEventType());
    }

    @Test
    public void productDataNameTest() {
        Assert.assertEquals("pdfOcr-tesseract4", PdfOcrTesseract4ProductData.getInstance().getProductName());
    }
}
