/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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
package com.itextpdf.pdfocr.events;

import com.itextpdf.kernel.counter.EventCounter;
import com.itextpdf.kernel.counter.EventCounterHandler;
import com.itextpdf.kernel.counter.IEventCounterFactory;
import com.itextpdf.kernel.counter.SimpleEventCounterFactory;
import com.itextpdf.kernel.counter.event.IEvent;
import com.itextpdf.kernel.counter.event.IMetaInfo;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.metainfo.TestMetaInfo;
import com.itextpdf.pdfocr.IntegrationTestHelper;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.pdfocr.tesseract4.events.PdfOcrTesseract4Event;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class EventCountingTest extends IntegrationTestHelper {

    protected static final String PROFILE_FOLDER = "./src/test/resources/com/itextpdf/pdfocr/events/";

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    AbstractTesseract4OcrEngine tesseractReader;
    String testFileTypeName;
    private boolean isExecutableReaderType;

    public EventCountingTest(ReaderType type) {
        isExecutableReaderType = type.equals(ReaderType.EXECUTABLE);
        if (isExecutableReaderType) {
            testFileTypeName = "executable";
        } else {
            testFileTypeName = "lib";
        }
        tesseractReader = getTesseractReader(type);
    }

    @Before
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void testEventCountingPdfEvent() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            doImageToPdfOcr(tesseractReader, Arrays.asList(file));

            Assert.assertEquals(1, eventCounter.getEvents().size());
            Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF, eventCounter.getEvents().get(0));
            Assert.assertNull(eventCounter.getMetaInfos().get(0));
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
        }
    }

    @Test
    public void testEventCountingSeveralImagesOneImageToPdfEvent() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            doImageToPdfOcr(tesseractReader, Arrays.asList(file, file));

            Assert.assertEquals(1, eventCounter.getEvents().size());
            Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF, eventCounter.getEvents().get(0));
            Assert.assertNull(eventCounter.getMetaInfos().get(0));
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
        }
    }

    @Test
    public void testEventCountingPdfAEvent() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            doImageToPdfAOcr(tesseractReader, Arrays.asList(file));

            Assert.assertEquals(1, eventCounter.getEvents().size());
            Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDFA, eventCounter.getEvents().get(0));
            Assert.assertNull(eventCounter.getMetaInfos().get(0));
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
        }
    }

    @Test
    public void testEventCountingTwoPdfEvents() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            doImageToPdfOcr(tesseractReader, Arrays.asList(file));
            doImageToPdfOcr(tesseractReader, Arrays.asList(file));

            Assert.assertEquals(2, eventCounter.getEvents().size());
            for (int i = 0; i < eventCounter.getEvents().size(); i++) {
                Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF, eventCounter.getEvents().get(i));
                Assert.assertNull(eventCounter.getMetaInfos().get(i));
            }
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
        }
    }

    @Test
    public void testEventCountingImageEvent() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            doImageOcr(tesseractReader, file);

            Assert.assertEquals(1, eventCounter.getEvents().size());
            Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_OCR, eventCounter.getEvents().get(0));
            Assert.assertNull(eventCounter.getMetaInfos().get(0));
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
        }
    }

    @Test
    public void testEventCountingImageEventCustomMetaInfo() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            tesseractReader.setThreadLocalMetaInfo(new TestMetaInfo());
            doImageOcr(tesseractReader, file);

            Assert.assertEquals(1, eventCounter.getEvents().size());
            Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_OCR, eventCounter.getEvents().get(0));
            Assert.assertTrue(eventCounter.getMetaInfos().get(0) instanceof TestMetaInfo);
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
            tesseractReader.setThreadLocalMetaInfo(null);
        }
    }

    @Test
    public void testEventCountingPdfEventCustomMetaInfo() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_01.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            tesseractReader.setThreadLocalMetaInfo(new TestMetaInfo());
            doImageToPdfOcr(tesseractReader, Arrays.asList(file));

            Assert.assertEquals(1, eventCounter.getEvents().size());
            Assert.assertSame(PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF, eventCounter.getEvents().get(0));
            Assert.assertTrue(eventCounter.getMetaInfos().get(0) instanceof TestMetaInfo);
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
            tesseractReader.setThreadLocalMetaInfo(null);
        }
    }

    @Test
    public void testEventCountingWithImprovedHocrParsing() {
        String imgPath = TEST_IMAGES_DIRECTORY + "thai_03.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);

        Tesseract4OcrEngineProperties properties =
                tesseractReader.getTesseract4OcrEngineProperties();
        properties.setTextPositioning(TextPositioning.BY_WORDS_AND_LINES);
        properties.setUseTxtToImproveHocrParsing(true);
        properties.setPathToTessData(new File(LANG_TESS_DATA_DIRECTORY));
        tesseractReader.setTesseract4OcrEngineProperties(properties);

        tesseractReader.doImageOcr(file);

        Assert.assertEquals(1, eventCounter.getEvents().size());
        Assert.assertEquals(PdfOcrTesseract4Event.TESSERACT4_IMAGE_OCR.getEventType(),
                eventCounter.getEvents().get(0).getEventType());

        EventCounterHandler.getInstance().unregister(factory);
    }


    public void testEventCountingCustomMetaInfoError() {
        String imgPath = TEST_IMAGES_DIRECTORY + "numbers_101.jpg";
        File file = new File(imgPath);

        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);

        IMetaInfo metaInfo = new TestMetaInfo();
        try {
            tesseractReader.setThreadLocalMetaInfo(metaInfo);
            doImageToPdfOcr(tesseractReader, Arrays.asList(file));
        } finally {
            Assert.assertEquals(metaInfo, tesseractReader.getThreadLocalMetaInfo());
            EventCounterHandler.getInstance().unregister(factory);
            tesseractReader.setThreadLocalMetaInfo(null);
        }
    }



    private static void doImageOcr(AbstractTesseract4OcrEngine tesseractReader, File imageFile) {
        tesseractReader.doImageOcr(imageFile);
    }

    private static void doImageToPdfOcr(AbstractTesseract4OcrEngine tesseractReader, List<File> imageFiles) {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader);
        ocrPdfCreator.createPdf(imageFiles, new PdfWriter(new ByteArrayOutputStream()));
    }

    private static void doImageToPdfAOcr(AbstractTesseract4OcrEngine tesseractReader, List<File> imageFiles) {
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader,
                new OcrPdfCreatorProperties().setPdfLang("en-US"));
        InputStream is = null;
        try {
            is = new FileInputStream(PROFILE_FOLDER + "sRGB_CS_profile.icm");
        } catch (FileNotFoundException e) {
            // No expected
        }
        PdfOutputIntent outputIntent = new PdfOutputIntent("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1",
                is);

        ocrPdfCreator.createPdfA(imageFiles, new PdfWriter(new ByteArrayOutputStream()), outputIntent);
    }

    private static class TestEventCounter extends EventCounter {
        private List<IEvent> events = new ArrayList<>();
        private List<IMetaInfo> metaInfos = new ArrayList<>();

        public List<IEvent> getEvents() {
            return events;
        }

        public List<IMetaInfo> getMetaInfos() {
            return metaInfos;
        }

        @Override
        protected void onEvent(IEvent event, IMetaInfo metaInfo) {
            this.events.add(event);
            this.metaInfos.add(metaInfo);
        }
    }
}
