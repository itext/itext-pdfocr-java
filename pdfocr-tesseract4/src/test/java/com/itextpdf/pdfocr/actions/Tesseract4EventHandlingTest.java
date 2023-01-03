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
package com.itextpdf.pdfocr.actions;

import com.itextpdf.commons.actions.AbstractContextBasedITextEvent;
import com.itextpdf.commons.actions.AbstractProductITextEvent;
import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.confirmations.ConfirmedEventWrapper;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.IntegrationEventHandlingTestHelper;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public abstract class Tesseract4EventHandlingTest extends IntegrationEventHandlingTestHelper {

    public Tesseract4EventHandlingTest(ReaderType type) {
        super(type);
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(tesseractReader).createPdfFile(Collections.singletonList(imgFile), outPdfFile);

        // check ocr events
        Assert.assertEquals(3, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE))
    public void ocrPdfCreatorCreatePdfFileNoImageTest() throws IOException {
        File imgFile = new File("unknown");
        List<File> images = Collections.singletonList(imgFile);
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        Assert.assertThrows(PdfOcrException.class,
                () -> ocrPdfCreator.createPdfFile(images, outPdfFile));

        // check ocr events
        Assert.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileNoOutputFileTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        List<File> images = Collections.singletonList(imgFile);
        File outPdfFile = new File("no/no_file");
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        Assert.assertThrows(IOException.class, () -> ocrPdfCreator.createPdfFile(images, outPdfFile));
        Assert.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileNullOutputFileTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        List<File> images = Collections.singletonList(imgFile);
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        Assert.assertThrows(NullPointerException.class, () -> ocrPdfCreator.createPdfFile(images, null));
        Assert.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileTwoImagesTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(tesseractReader).createPdfFile(Arrays.asList(imgFile, imgFile), outPdfFile);

        // check ocr events
        Assert.assertEquals(5, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent1 = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent1, EventConfirmationType.ON_CLOSE);

        IEvent ocrUsageEvent2 = eventsHandler.getEvents().get(1);
        validateUsageEvent(ocrUsageEvent2, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(2), PdfOcrOutputType.PDF);

        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent1);

        validateConfirmEvent(eventsHandler.getEvents().get(4), ocrUsageEvent2);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileTwoRunningsTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(tesseractReader).createPdfFile(Collections.singletonList(imgFile), outPdfFile);
        new OcrPdfCreator(tesseractReader).createPdfFile(Collections.singletonList(imgFile), outPdfFile);

        Assert.assertEquals(6, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        // usage event
        ocrUsageEvent = eventsHandler.getEvents().get(3);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(4), PdfOcrOutputType.PDF);

        validateConfirmEvent(eventsHandler.getEvents().get(5), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void ocrPdfCreatorCreatePdfTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        PdfWriter pdfWriter = new PdfWriter(outPdfFile);
        PdfDocument pdfDocument =
                new OcrPdfCreator(tesseractReader).createPdf(Collections.singletonList(imgFile), pdfWriter);
        pdfDocument.close();

        Assert.assertEquals(4, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateCoreConfirmEvent(eventsHandler.getEvents().get(2));

        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent);

        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(),
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE))
    public void ocrPdfCreatorCreatePdfNoImageTest() throws IOException {
        List<File> images = Collections.singletonList(new File("no_image"));
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        PdfWriter pdfWriter = new PdfWriter(outPdfFile);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        Assert.assertThrows(PdfOcrTesseract4Exception.class, () -> ocrPdfCreator.createPdf(images, pdfWriter));

        pdfWriter.close();

        Assert.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfNullWriterTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        List<File> images = Collections.singletonList(imgFile);
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        Assert.assertThrows(IllegalArgumentException.class, () -> ocrPdfCreator.createPdf(images, null));
        Assert.assertEquals(1, eventsHandler.getEvents().size());
        validateUsageEvent(eventsHandler.getEvents().get(0), EventConfirmationType.ON_CLOSE);
    }

    @Test
    public void ocrPdfCreatorCreatePdfAFileTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setPdfLang("en-US");
        new OcrPdfCreator(tesseractReader, props).createPdfAFile(Collections.singletonList(imgFile),
                outPdfFile, getRGBPdfOutputIntent());

        // check ocr events
        Assert.assertEquals(3, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDFA);

        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void ocrPdfCreatorCreatePdfATest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        PdfWriter pdfWriter = new PdfWriter(outPdfFile);
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties()
                .setPdfLang("en-US");
        PdfDocument pdfDocument = new OcrPdfCreator(tesseractReader, props)
                .createPdfA(Collections.singletonList(imgFile), pdfWriter, getRGBPdfOutputIntent());
        pdfDocument.close();

        // check ocr events
        Assert.assertEquals(4, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDFA);

        validateCoreConfirmEvent(eventsHandler.getEvents().get(2));

        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(),
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void doImageOcrTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        tesseractReader.doImageOcr(imgFile);

        Assert.assertEquals(3, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(2), usageEvent);
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE))
    public void doImageOcrNoImageTest() {
        File imgFile = new File("uncknown");
        Assert.assertThrows(PdfOcrException.class, () -> tesseractReader.doImageOcr(imgFile));
        Assert.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void doImageOcrTwoRunningsTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");

        tesseractReader.doImageOcr(imgFile);
        tesseractReader.doImageOcr(imgFile);

        Assert.assertEquals(6, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(2), usageEvent);

        usageEvent = eventsHandler.getEvents().get(3);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(4), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(5), usageEvent);
    }

    @Test
    public void createTxtFileTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        tesseractReader.createTxtFile(Arrays.asList(imgFile, imgFile),
                FileUtil.createTempFile("test", ".txt"));

        Assert.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateStatisticEvent(eventsHandler.getEvents().get(2), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent);
    }

    @Test
    public void createTxtFileNullEventHelperTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        tesseractReader.createTxtFile(Arrays.asList(imgFile, imgFile),
                FileUtil.createTempFile("test", ".txt"),
                new OcrProcessContext(null));

        Assert.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateStatisticEvent(eventsHandler.getEvents().get(2), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent);
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE))
    public void createTxtFileNoImageTest() throws IOException {
        File imgFile = new File("no_image");
        List<File> images = Arrays.asList(imgFile, imgFile);
        File outPdfFile = FileUtil.createTempFile("test", ".txt");
        Assert.assertThrows(PdfOcrException.class, () -> tesseractReader.createTxtFile(images, outPdfFile));
        // only one usage event is expected and it is not confirmed (no confirm event
        Assert.assertEquals(1, eventsHandler.getEvents().size());
        validateUsageEvent(eventsHandler.getEvents().get(0), EventConfirmationType.ON_DEMAND);
    }

    @Test
    public void createTxtFileNoFileTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        List<File> images = Arrays.asList(imgFile, imgFile);
        File outPdfFile = new File("nopath/nofile");
        Exception e = Assert.assertThrows(PdfOcrTesseract4Exception.class,
                () -> tesseractReader.createTxtFile(images, outPdfFile));
        Assert.assertEquals(PdfOcrTesseract4ExceptionMessageConstant.CANNOT_WRITE_TO_FILE, e.getMessage());

        Assert.assertEquals(3, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateStatisticEvent(eventsHandler.getEvents().get(2), PdfOcrOutputType.DATA);
    }

    @Test
    public void createTxtFileNullOutFileTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        List<File> images = Arrays.asList(imgFile, imgFile);
        Assert.assertThrows(NullPointerException.class, () -> tesseractReader.createTxtFile(images, null));
        Assert.assertEquals(3, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateStatisticEvent(eventsHandler.getEvents().get(2), PdfOcrOutputType.DATA);
    }

    // set meta info tests
    @Test
    public void setEventCountingMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        createPdfAndSetEventCountingMetaInfo(tesseractReader, outPdfFile, imgFile, new TestMetaInfo());

        Assert.assertEquals(4, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateCoreConfirmEvent(eventsHandler.getEvents().get(2));

        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent);

        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(),
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void createPdfFileTestMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        createPdfFileAndSetMetaInfoToProps(tesseractReader, outPdfFile, imgFile, new TestMetaInfo());

        // check ocr events
        Assert.assertEquals(4, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);

        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.PDF);

        validateCoreConfirmEvent(eventsHandler.getEvents().get(2));

        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(),
                getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void doImageOcrCustomEventHelperTest() {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        tesseractReader.doImageOcr(imgFile, new OcrProcessContext(new CustomEventHelper()));

        Assert.assertEquals(3, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(2), usageEvent);
    }

    @Test
    public void createTxtFileCustomEventHelperTest() throws IOException {
        File imgFile = new File(TEST_IMAGES_DIRECTORY + "numbers_01.jpg");
        tesseractReader.createTxtFile(Arrays.asList(imgFile, imgFile),
                FileUtil.createTempFile("test", ".txt"),
                new OcrProcessContext(new CustomEventHelper()));

        Assert.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        validateStatisticEvent(eventsHandler.getEvents().get(1), PdfOcrOutputType.DATA);
        validateStatisticEvent(eventsHandler.getEvents().get(2), PdfOcrOutputType.DATA);
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent);
    }

    private static class CustomEventHelper extends AbstractPdfOcrEventHelper {
        @Override
        public void onEvent(AbstractProductITextEvent event) {
            if (event instanceof AbstractContextBasedITextEvent) {
                ((AbstractContextBasedITextEvent) event).setMetaInfo(new TestMetaInfo());
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

    private static class TestMetaInfo implements IMetaInfo {
    }
}
