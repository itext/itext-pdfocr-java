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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.confirmations.ConfirmedEventWrapper;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.pdfocr.IntegrationEventHandlingTestHelper;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class Tesseract4MetaInfoEventHandlingTest extends IntegrationEventHandlingTestHelper {

    protected String destinationFolder;

    public Tesseract4MetaInfoEventHandlingTest(ReaderType type, String destinationFolder) {
        super(type);
        this.destinationFolder = destinationFolder;
    }

    // set meta info tests
    @Test
    public void setEventCountingMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = new File(destinationFolder + "setEventCountingMetaInfo,pdf");

        createPdfAndSetEventCountingMetaInfo(tesseractReader, outPdfFile, imgFile, new TestMetaInfo());

        Assertions.assertEquals(3, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void createPdfFileTestMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = new File(destinationFolder + "createPdfFileTestMetaInfo.pdf");

        createPdfFileAndSetMetaInfoToProps(tesseractReader, outPdfFile, imgFile, new TestMetaInfo());

        // check ocr events
        Assertions.assertEquals(3, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    private static class TestMetaInfo implements IMetaInfo {
    }
}
