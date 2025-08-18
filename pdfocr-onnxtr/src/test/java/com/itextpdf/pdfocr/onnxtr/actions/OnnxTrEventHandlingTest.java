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
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TestMetaInfo;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.LogLevelConstants;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxTrEventHandlingTest extends IntegrationEventHandlingTestHelper {

    // Section with OcrPdfCreator#createPdfFile related tests

    @Test
    public void ocrPdfCreatorCreatePdfFileTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(OCR_ENGINE).createPdfFile(Collections.singletonList(imgFile), outPdfFile);

        // check ocr events
        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] { getPdfOcrEvent() });
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE, logLevel = LogLevelConstants.ERROR)
    })
    public void ocrPdfCreatorCreatePdfFileNoImageTest() throws IOException {
        File imgFile = new File("unknown");
        List<File> images = Collections.singletonList(imgFile);
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE);
        Assertions.assertThrows(PdfOcrException.class, () -> ocrPdfCreator.createPdfFile(images, outPdfFile));

        // check ocr events
        Assertions.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileNoOutputFileTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        List<File> images = Collections.singletonList(imgFile);
        File outPdfFile = new File("no/no_file");
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE);
        Assertions.assertThrows(IOException.class, () -> ocrPdfCreator.createPdfFile(images, outPdfFile));

        // check ocr events
        Assertions.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileNullOutputFileTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        List<File> images = Collections.singletonList(imgFile);
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE);
        Assertions.assertThrows(NullPointerException.class, () -> ocrPdfCreator.createPdfFile(images, null));

        // check ocr events
        Assertions.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileTwoImagesTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(OCR_ENGINE).createPdfFile(Arrays.asList(imgFile, imgFile), outPdfFile);

        // check ocr events
        Assertions.assertEquals(4, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent1 = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent1, EventConfirmationType.ON_CLOSE);
        IEvent ocrUsageEvent2 = eventsHandler.getEvents().get(1);
        validateUsageEvent(ocrUsageEvent2, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent1);
        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent2);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] { getPdfOcrEvent() });
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void ocrPdfCreatorCreatePdfFileTwoRunningsTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(OCR_ENGINE).createPdfFile(Collections.singletonList(imgFile), outPdfFile);
        new OcrPdfCreator(OCR_ENGINE).createPdfFile(Collections.singletonList(imgFile), outPdfFile);

        Assertions.assertEquals(4, eventsHandler.getEvents().size());

        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), ocrUsageEvent);

        // usage event
        ocrUsageEvent = eventsHandler.getEvents().get(2);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(3), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] { getPdfOcrEvent() });
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    // Section with OcrPdfCreator#createPdf related tests

    @Test
    public void ocrPdfCreatorCreatePdfTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        PdfWriter pdfWriter = new PdfWriter(outPdfFile);
        PdfDocument pdfDocument = new OcrPdfCreator(OCR_ENGINE).createPdf(Collections.singletonList(imgFile), pdfWriter);
        pdfDocument.close();

        Assertions.assertEquals(3, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateCoreConfirmEvent(eventsHandler.getEvents().get(1));
        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(), getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE, logLevel = LogLevelConstants.ERROR)
    })
    public void ocrPdfCreatorCreatePdfNoImageTest() throws IOException {
        List<File> images = Collections.singletonList(new File("no_image"));
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        PdfWriter pdfWriter = new PdfWriter(outPdfFile);

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE);
        Assertions.assertThrows(PdfOcrInputException.class, () -> ocrPdfCreator.createPdf(images, pdfWriter));

        pdfWriter.close();

        Assertions.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void ocrPdfCreatorCreatePdfNullWriterTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        List<File> images = Collections.singletonList(imgFile);
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OCR_ENGINE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ocrPdfCreator.createPdf(images, null));
        Assertions.assertEquals(1, eventsHandler.getEvents().size());
        validateUsageEvent(eventsHandler.getEvents().get(0), EventConfirmationType.ON_CLOSE);
    }

    // Section with OcrPdfCreator#createPdfAFile related tests

    @Test
    public void ocrPdfCreatorCreatePdfAFileTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties().setPdfLang("en-US");
        new OcrPdfCreator(OCR_ENGINE, props).createPdfAFile(Collections.singletonList(imgFile), outPdfFile,
                getRGBPdfOutputIntent());

        // check ocr events
        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] { getPdfOcrEvent() });
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    // Section with OcrPdfCreator#createPdfA related tests

    @Test
    public void ocrPdfCreatorCreatePdfATest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        PdfWriter pdfWriter = new PdfWriter(outPdfFile);
        OcrPdfCreatorProperties props = new OcrPdfCreatorProperties().setPdfLang("en-US");
        PdfDocument pdfDocument = new OcrPdfCreator(OCR_ENGINE, props).createPdfA(Collections.singletonList(imgFile),
                pdfWriter, getRGBPdfOutputIntent());
        pdfDocument.close();

        // check ocr events
        Assertions.assertEquals(3, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateCoreConfirmEvent(eventsHandler.getEvents().get(1));
        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(), getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    // Section with OnnxTrOcrEngine#doImageOcr related tests

    @Test
    public void doImageOcrTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        OCR_ENGINE.doImageOcr(imgFile);

        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), usageEvent);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE, logLevel = LogLevelConstants.ERROR)
    })
    public void doImageOcrNoImageTest() {
        File imgFile = new File("uncknown");
        Assertions.assertThrows(PdfOcrException.class, () -> OCR_ENGINE.doImageOcr(imgFile));
        Assertions.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void doImageOcrTwoRunningsTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");

        OCR_ENGINE.doImageOcr(imgFile);
        OCR_ENGINE.doImageOcr(imgFile);

        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), usageEvent);

        usageEvent = eventsHandler.getEvents().get(2);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent);
    }

    // Section with OnnxTrOcrEngine#createTxtFile related tests

    @Test
    public void createTxtFileTwoImagesTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        OCR_ENGINE.createTxtFile(Arrays.asList(imgFile, imgFile), FileUtil.createTempFile("test", ".txt"));

        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent1 = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent1, EventConfirmationType.ON_DEMAND);
        IEvent usageEvent2 = eventsHandler.getEvents().get(1);
        validateUsageEvent(usageEvent2, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(2), usageEvent1);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent2);
    }

    @Test
    public void createTxtFileNullEventHelperTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        OCR_ENGINE.createTxtFile(Arrays.asList(imgFile, imgFile), FileUtil.createTempFile("test", ".txt"),
                new OcrProcessContext(null));

        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent1 = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent1, EventConfirmationType.ON_DEMAND);
        IEvent usageEvent2 = eventsHandler.getEvents().get(1);
        validateUsageEvent(usageEvent2, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(2), usageEvent1);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent2);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE, logLevel = LogLevelConstants.ERROR)
    })
    public void createTxtFileNoImageTest() throws IOException {
        File imgFile = new File("no_image");
        List<File> images = Arrays.asList(imgFile, imgFile);
        File outPdfFile = FileUtil.createTempFile("test", ".txt");
        Assertions.assertThrows(PdfOcrException.class, () -> OCR_ENGINE.createTxtFile(images, outPdfFile));
        Assertions.assertEquals(0, eventsHandler.getEvents().size());
    }

    @Test
    public void createTxtFileNoFileTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        List<File> images = Arrays.asList(imgFile, imgFile);
        File outPdfFile = new File("nopath/nofile");
        Exception e = Assertions.assertThrows(PdfOcrException.class,
                () -> OCR_ENGINE.createTxtFile(images, outPdfFile));
        Assertions.assertTrue(e.getMessage().contains(
                PdfOcrExceptionMessageConstant.CANNOT_WRITE_TO_FILE.substring(0, 20)));
        Assertions.assertTrue(e.getMessage().contains("nopath"));
        Assertions.assertTrue(e.getMessage().contains("nofile"));

        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        // there is no confirm event

        usageEvent = eventsHandler.getEvents().get(1);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        // there is no confirm event
    }

    @Test
    public void createTxtFileNullOutFileTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        List<File> images = Arrays.asList(imgFile, imgFile);
        Assertions.assertThrows(NullPointerException.class, () -> OCR_ENGINE.createTxtFile(images, null));
        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        usageEvent = eventsHandler.getEvents().get(1);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
    }

    // Section with MetaInfo related tests

    @Test
    public void setEventCountingMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        createPdfAndSetEventCountingMetaInfo(OCR_ENGINE, outPdfFile, imgFile, new TestMetaInfo());

        // TestMetaInfo from com.itextpdf.pdfocr package which isn't
        // registered in ContextManager, it's why core events are passed
        Assertions.assertEquals(3, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateCoreConfirmEvent(eventsHandler.getEvents().get(1));
        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(), getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void setEventCountingOnnxTrMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        createPdfAndSetEventCountingMetaInfo(OCR_ENGINE, outPdfFile, imgFile, new TestOnnxTrMetaInfo());

        // TestOnnxTrMetaInfo from com.itextpdf.pdfocr.onnxtr package which
        // is registered in ContextManager, it's why core events are discarded
        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), ocrUsageEvent);

        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void createPdfFileTestMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        createPdfFileAndSetMetaInfoToProps(OCR_ENGINE, outPdfFile, imgFile, new TestMetaInfo());

        // TestMetaInfo from com.itextpdf.pdfocr package which isn't
        // registered in ContextManager, it's why core events are passed
        Assertions.assertEquals(3, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateCoreConfirmEvent(eventsHandler.getEvents().get(1));
        validateConfirmEvent(eventsHandler.getEvents().get(2), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getCoreEvent(), getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void createPdfFileTestOnnxTrMetaInfoTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        createPdfFileAndSetMetaInfoToProps(OCR_ENGINE, outPdfFile, imgFile, new TestOnnxTrMetaInfo());

        // TestOnnxTrMetaInfo from com.itextpdf.pdfocr.onnxtr package which
        // is registered in ContextManager, it's why core events are discarded
        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent ocrUsageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(ocrUsageEvent, EventConfirmationType.ON_CLOSE);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), ocrUsageEvent);

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] {getPdfOcrEvent()});
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    // Section with custom EventHelper related tests

    @Test
    public void doImageOcrCustomEventHelperTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        OCR_ENGINE.doImageOcr(imgFile, new OcrProcessContext(new CustomEventHelper()));

        Assertions.assertEquals(2, eventsHandler.getEvents().size());
        IEvent usageEvent = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(1), usageEvent);
    }

    @Test
    public void createTxtFileCustomEventHelperTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "numbers_01.jpg");
        OCR_ENGINE.createTxtFile(Arrays.asList(imgFile, imgFile),
                FileUtil.createTempFile("test", ".txt"),
                new OcrProcessContext(new CustomEventHelper()));

        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        IEvent usageEvent1 = eventsHandler.getEvents().get(0);
        validateUsageEvent(usageEvent1, EventConfirmationType.ON_DEMAND);
        IEvent usageEvent2 = eventsHandler.getEvents().get(1);
        validateUsageEvent(usageEvent2, EventConfirmationType.ON_DEMAND);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(2), usageEvent1);
        // there is no statistic event
        validateConfirmEvent(eventsHandler.getEvents().get(3), usageEvent2);
    }

    // Section with multipage TIFF image related tests

    @Test
    public void ocrPdfCreatorCreatePdfFileMultipageTiffTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "two_pages.tiff");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        new OcrPdfCreator(OCR_ENGINE).createPdfFile(Collections.singletonList(imgFile), outPdfFile);

        // check ocr events
        // 2 pages in TIFF image
        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        for (int i = 0; i < 2; i++) {
            IEvent usageEvent = eventsHandler.getEvents().get(i);
            validateUsageEvent(usageEvent, EventConfirmationType.ON_CLOSE);
            // there is no statistic event
            validateConfirmEvent(eventsHandler.getEvents().get(2 + i), usageEvent);
        }

        // check producer line in the output pdf
        String expectedProdLine = createExpectedProducerLine(new ConfirmedEventWrapper[] { getPdfOcrEvent() });
        validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
    }

    @Test
    public void createTxtFileMultipageTiffTest() throws IOException {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "two_pages.tiff");
        OCR_ENGINE.createTxtFile(Arrays.asList(imgFile), FileUtil.createTempFile("test", ".txt"));

        // 2 pages in TIFF image
        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        for (int i = 0; i < 2; i++) {
            IEvent usageEvent = eventsHandler.getEvents().get(i);
            validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
            // there is no statistic event
            validateConfirmEvent(eventsHandler.getEvents().get(2 + i), usageEvent);
        }

    }

    @Test
    public void doImageOcrMultipageTiffTest() {
        File imgFile = new File(TEST_IMAGE_DIRECTORY + "two_pages.tiff");
        OCR_ENGINE.doImageOcr(imgFile);

        // 2 pages in TIFF image
        Assertions.assertEquals(4, eventsHandler.getEvents().size());
        for (int i = 0; i < 2; i++) {
            IEvent usageEvent = eventsHandler.getEvents().get(i * 2);
            validateUsageEvent(usageEvent, EventConfirmationType.ON_DEMAND);
            // there is no statistic event
            validateConfirmEvent(eventsHandler.getEvents().get(i * 2 + 1), usageEvent);
        }
    }

    @Test
    public void ocrPdfCreatorMakeSearchableTest() throws IOException {
        File inPdfFile = new File(TEST_PDFS_DIRECTORY + "2pages.pdf");
        File outPdfFile = FileUtil.createTempFile("test", ".pdf");

        try {
            new OcrPdfCreator(OCR_ENGINE).makePdfSearchable(inPdfFile, outPdfFile);

            // Check ocr events. No stats events.
            // 3 images == 6 events + 1 confirm event for process_pdf event which is not caught by eventHandler
            Assertions.assertEquals(7, eventsHandler.getEvents().size());
            for (int i = 0; i < 3; i++) {
                IEvent usageEvent = eventsHandler.getEvents().get(i);
                validateUsageEvent(usageEvent, EventConfirmationType.ON_CLOSE);
                // There is no statistic event
                validateConfirmEvent(eventsHandler.getEvents().get(4 + i), usageEvent);
            }

            // Check producer line in the output pdf
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent(), getPdfOcrEvent()});
            validatePdfProducerLine(outPdfFile.getAbsolutePath(), expectedProdLine);
        } finally {
            outPdfFile.delete();
        }
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

    private static class TestOnnxTrMetaInfo implements IMetaInfo {
    }
}
