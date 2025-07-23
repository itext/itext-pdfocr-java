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
package com.itextpdf.pdfocr.onnxtr.actions;

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
import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.kernel.actions.events.ITextCoreProductEvent;
import com.itextpdf.kernel.pdf.DocumentProperties;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.onnxtr.OnnxTrOcrEngine;
import com.itextpdf.pdfocr.onnxtr.actions.data.PdfOcrOnnxTrProductData;
import com.itextpdf.pdfocr.onnxtr.actions.events.PdfOcrOnnxTrProductEvent;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public abstract class IntegrationEventHandlingTestHelper extends ExtendedITextTest {
    protected static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    protected static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";

    protected static OnnxTrOcrEngine OCR_ENGINE;
    protected StoreEventsHandler eventsHandler;

    @BeforeAll
    public static void beforeClass() {
        // init ocr engine
        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE.close();
    }

    @BeforeEach
    public void before() {
        // register event handler
        eventsHandler = new StoreEventsHandler();
        EventManager.getInstance().register(eventsHandler);
    }

    @AfterEach
    public void after() {
        EventManager.getInstance().unregister(eventsHandler);
        eventsHandler = null;
    }

    protected static void validateUsageEvent(IEvent event, EventConfirmationType expectedConfirmationType) {
        Assertions.assertTrue(event instanceof PdfOcrOnnxTrProductEvent);
        Assertions.assertEquals("process-image-onnxtr", ((PdfOcrOnnxTrProductEvent) event).getEventType());
        Assertions.assertEquals(expectedConfirmationType, ((PdfOcrOnnxTrProductEvent) event).getConfirmationType());
        Assertions.assertEquals(PdfOcrOnnxTrProductData.getInstance(),
                ((PdfOcrOnnxTrProductEvent) event).getProductData());
    }

    protected static void validateConfirmEvent(IEvent event, IEvent expectedConfirmedEvent) {
        Assertions.assertTrue(event instanceof ConfirmEvent);
        Assertions.assertSame(expectedConfirmedEvent, ((ConfirmEvent) event).getConfirmedEvent());
    }

    // we expect core events in case of API methods returning PdfDocument
    protected static void validateCoreConfirmEvent(IEvent event) {
        Assertions.assertTrue(event instanceof ConfirmEvent);
        Assertions.assertEquals(getCoreEvent().getEvent().getEventType(),
                ((ConfirmEvent) event).getConfirmedEvent().getEventType());
        Assertions.assertEquals(getCoreEvent().getEvent().getConfirmationType(),
                ((ConfirmEvent) event).getConfirmedEvent().getConfirmationType());
    }

    protected void validatePdfProducerLine(String filePath, String expected) throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(filePath))) {
            Assertions.assertEquals(expected, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    protected static String createExpectedProducerLine(ConfirmedEventWrapper[] expectedEvents) {
        List<ConfirmedEventWrapper> listEvents = Arrays.asList(expectedEvents);
        return ProducerBuilder.modifyProducer(listEvents, null);
    }

    protected static ConfirmedEventWrapper getPdfOcrEvent() {
        PdfOcrOnnxTrProductEvent event = PdfOcrOnnxTrProductEvent.createProcessImageOnnxTrEvent(new SequenceId(), null,
                EventConfirmationType.ON_CLOSE);
        DefaultITextProductEventProcessor processor = new DefaultITextProductEventProcessor(ProductNameConstant.PDF_OCR_ONNXTR);
        return new ConfirmedEventWrapper(event, processor.getUsageType(), processor.getProducer());
    }

    protected static ConfirmedEventWrapper getCoreEvent() {
        DefaultITextProductEventProcessor processor = new DefaultITextProductEventProcessor(ProductNameConstant.ITEXT_CORE);
        ITextCoreProductEvent event = ITextCoreProductEvent.createProcessPdfEvent(new SequenceId(), null,
                EventConfirmationType.ON_CLOSE);
        return new ConfirmedEventWrapper(event, processor.getUsageType(), processor.getProducer());
    }

    protected static PdfOutputIntent getRGBPdfOutputIntent() throws IOException {
        String defaultRGBColorProfilePath = TEST_DIRECTORY + "profiles/sRGB_CS_profile.icm";
        InputStream is = FileUtil.getInputStreamForFile(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "", "", "sRGB IEC61966-2.1", is);
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
            IMetaInfo metaInfo) throws IOException {

        try (PdfWriter pdfWriter = new PdfWriter(outPdfFile)) {
            PdfDocument pdfDocument = new OcrPdfCreator(engine).createPdf(Collections.singletonList(imgFile), pdfWriter,
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
            IMetaInfo metaInfo) throws IOException {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties().setMetaInfo(metaInfo);
        new OcrPdfCreator(engine, properties).createPdfFile(Collections.singletonList(imgFile), outPdfFile);
    }

    protected static class StoreEventsHandler implements IEventHandler {
        private final List<IEvent> events = new ArrayList<>();

        public List<IEvent> getEvents() {
            return events;
        }

        @Override
        public void onEvent(IEvent event) {
            if (event instanceof PdfOcrOnnxTrProductEvent
                    || event instanceof PdfOcrOutputTypeStatisticsEvent
                    || event instanceof ConfirmEvent) {
                events.add(event);
            }
        }
    }
}
