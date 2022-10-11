/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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

import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.IEventHandler;
import com.itextpdf.commons.actions.ProductNameConstant;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.ConfirmedEventWrapper;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.processors.DefaultITextProductEventProcessor;
import com.itextpdf.commons.actions.producer.ProducerBuilder;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.kernel.actions.events.ITextCoreProductEvent;
import com.itextpdf.kernel.pdf.DocumentProperties;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.actions.data.PdfOcrTesseract4ProductData;
import com.itextpdf.pdfocr.tesseract4.actions.events.PdfOcrTesseract4ProductEvent;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public abstract class IntegrationEventHandlingTestHelper extends IntegrationTestHelper {
    protected final AbstractTesseract4OcrEngine tesseractReader;
    protected StoreEventsHandler eventsHandler;

    public IntegrationEventHandlingTestHelper(ReaderType type) {
        tesseractReader = getTesseractReader(type);
    }

    @Before
    public void before() {
        // init ocr engine
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);

        // register event handler
        eventsHandler = new StoreEventsHandler();
        EventManager.getInstance().register(eventsHandler);
    }

    @After
    public void after() {
        EventManager.getInstance().unregister(eventsHandler);
        eventsHandler = null;
    }

    protected static void validateUsageEvent(IEvent event, EventConfirmationType expectedConfirmationType) {
        Assert.assertTrue(event instanceof PdfOcrTesseract4ProductEvent);
        Assert.assertEquals("process-image", ((PdfOcrTesseract4ProductEvent) event).getEventType());
        Assert.assertEquals(expectedConfirmationType, ((PdfOcrTesseract4ProductEvent) event).getConfirmationType());
        Assert.assertEquals(PdfOcrTesseract4ProductData.getInstance(),
                ((PdfOcrTesseract4ProductEvent) event).getProductData());
    }

    protected static void validateStatisticEvent(IEvent event, PdfOcrOutputType outputType) {
        Assert.assertTrue(event instanceof PdfOcrOutputTypeStatisticsEvent);
        Assert.assertEquals(outputType, ((PdfOcrOutputTypeStatisticsEvent) event).getPdfOcrStatisticsEventType());
        Assert.assertEquals(PdfOcrTesseract4ProductData.getInstance(),
                ((PdfOcrOutputTypeStatisticsEvent) event).getProductData());
    }

    protected static void validateConfirmEvent(IEvent event, IEvent expectedConfirmedEvent) {
        Assert.assertTrue(event instanceof ConfirmEvent);
        Assert.assertSame(expectedConfirmedEvent, ((ConfirmEvent) event).getConfirmedEvent());
    }

    // we expect core events in case of API methods returning PdfDocument
    protected static void validateCoreConfirmEvent(IEvent event) {
        Assert.assertTrue(event instanceof ConfirmEvent);
        Assert.assertEquals(getCoreEvent().getEvent().getEventType(),
                ((ConfirmEvent) event).getConfirmedEvent().getEventType());
        Assert.assertEquals(getCoreEvent().getEvent().getConfirmationType(),
                ((ConfirmEvent) event).getConfirmedEvent().getConfirmationType());
    }

    protected void validatePdfProducerLine(String filePath, String expected) throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(filePath))) {
            Assert.assertEquals(expected, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    protected static String createExpectedProducerLine(ConfirmedEventWrapper[] expectedEvents) {
        List<ConfirmedEventWrapper> listEvents = Arrays.asList(expectedEvents);
        return ProducerBuilder.modifyProducer(listEvents, null);
    }

    protected static ConfirmedEventWrapper getPdfOcrEvent() {
        DefaultITextProductEventProcessor processor = new DefaultITextProductEventProcessor(
                ProductNameConstant.PDF_HTML);
        return new ConfirmedEventWrapper(
                PdfOcrTesseract4ProductEvent.createProcessImageEvent(new SequenceId(), null,
                        EventConfirmationType.ON_CLOSE),
                processor.getUsageType(),
                processor.getProducer());
    }

    protected static ConfirmedEventWrapper getCoreEvent() {
        DefaultITextProductEventProcessor processor = new DefaultITextProductEventProcessor(
                ProductNameConstant.ITEXT_CORE);
        return new ConfirmedEventWrapper(
                ITextCoreProductEvent.createProcessPdfEvent(new SequenceId(), null, EventConfirmationType.ON_CLOSE),
                processor.getUsageType(),
                processor.getProducer());
    }

    protected static PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        String defaultRGBColorProfilePath = TEST_DIRECTORY + "profiles"
                + "/sRGB_CS_profile.icm";
        InputStream is = new FileInputStream(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * Creates PDF document with {@link OcrPdfCreator#createPdf} and set event counting meta info.
     *
     * @param engine     engine to set in the {@link OcrPdfCreator}
     * @param outPdfFile out pdf file
     * @param imgFile    image file
     * @param metaInfo   meta info
     *
     * @throws IOException signals that an I/O exception of some sort has occurred
     */
    protected void createPdfAndSetEventCountingMetaInfo(IOcrEngine engine, File outPdfFile, File imgFile,
            IMetaInfo metaInfo)
            throws IOException {
        try (PdfWriter pdfWriter = new PdfWriter(outPdfFile)) {
            PdfDocument pdfDocument =
                    new OcrPdfCreator(engine).createPdf(Collections.singletonList(imgFile), pdfWriter,
                            new DocumentProperties().setEventCountingMetaInfo(metaInfo));
            pdfDocument.close();
        }
    }

    /**
     * Creates PDF document with {@link OcrPdfCreator#createPdf} and set meta info to
     * {@link OcrPdfCreatorProperties}.
     *
     * @param engine     engine to set in the {@link OcrPdfCreator}
     * @param outPdfFile out pdf file
     * @param imgFile    image file
     * @param metaInfo   meta info
     *
     * @throws IOException signals that an I/O exception of some sort has occurred
     */
    protected void createPdfFileAndSetMetaInfoToProps(IOcrEngine engine, File outPdfFile, File imgFile,
            IMetaInfo metaInfo)
            throws IOException {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties().setMetaInfo(metaInfo);
        new OcrPdfCreator(engine, properties).createPdfFile(
                Collections.singletonList(imgFile), outPdfFile);
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
