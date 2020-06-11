/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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
package com.itextpdf.pdfocr.events.multithreading;

import com.itextpdf.kernel.counter.EventCounter;
import com.itextpdf.kernel.counter.EventCounterHandler;
import com.itextpdf.kernel.counter.IEventCounterFactory;
import com.itextpdf.kernel.counter.SimpleEventCounterFactory;
import com.itextpdf.kernel.counter.event.IEvent;
import com.itextpdf.kernel.counter.event.IMetaInfo;
import com.itextpdf.metainfo.TestMetaInfo;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LibOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.events.PdfOcrTesseract4Event;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public abstract class MultiThreadingTest extends ExtendedITextTest {
    protected static final String destinationFolder = "./target/test/com/itextpdf/pdfocr/events/multithreading/";
    protected static final String sourceFolder = "./src/test/resources/com/itextpdf/pdfocr/events/multithreading/";

    private AbstractTesseract4OcrEngine tesseractReader;
    private String testFileTypeName;
    private boolean isExecutableReaderType;

    private static Tesseract4LibOcrEngine tesseractLibReader = new Tesseract4LibOcrEngine(
            new Tesseract4OcrEngineProperties());
    private static Tesseract4ExecutableOcrEngine tesseractExecutableReader = new Tesseract4ExecutableOcrEngine(
            new Tesseract4OcrEngineProperties());

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    public MultiThreadingTest(ReaderType type) {
        isExecutableReaderType = type.equals(ReaderType.EXECUTABLE);
        if (isExecutableReaderType) {
            testFileTypeName = "executable";
        } else {
            testFileTypeName = "lib";
        }
        tesseractReader = getTesseractReader(type);
    }

    @BeforeClass
    public static void beforeClass() {
        createDestinationFolder(destinationFolder);
    }

    protected static AbstractTesseract4OcrEngine getTesseractReader(ReaderType type) {
        if (type.equals(ReaderType.LIB)) {
            return tesseractLibReader;
        } else {
            return tesseractExecutableReader;
        }
    }

    @Before
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(new File(sourceFolder + "../../tessdata/"));
        tesseractReader.setTesseract4OcrEngineProperties(ocrEngineProperties);
    }

    @Test
    public void testEventCountingPdfEvent() throws InterruptedException {
        TestEventCounter eventCounter = new TestEventCounter();
        IEventCounterFactory factory = new SimpleEventCounterFactory(eventCounter);
        EventCounterHandler.getInstance().register(factory);
        try {
            int n = 16;
            IMetaInfo metainfo = new TestMetaInfo();
            Thread[] threads = new Thread[n];
            for (int i = 0; i < n; i++) {
                // We do not use Runnable as the variable's type because of porting issues
                DoImageOcrRunnable runnable = new DoImageOcrRunnable(
                        tesseractReader,
                        metainfo,
                        new File(sourceFolder + "numbers_01.jpg"),
                        new File(destinationFolder + "ocr-result-" + (i + 1) + ".txt"),
                        0 == i % 2);
                threads[i] = getThread(runnable);
            }
            for (int i = 0; i < n; i++) {
                threads[i].start();

                // The test will pass in sequential mode, i.e. if the following line is uncommented
                //threads[i].join();
            }
            for (int i = 0; i < n; i++) {
                threads[i].join();
            }

            Assert.assertEquals(n, eventCounter.getEvents().size());
            int expectedPdfEvents = n / 2;
            int expectedImageEvents = n - expectedPdfEvents;
            int foundPdfEvents = 0;
            int foundImageEvents = 0;
            for (int i = 0; i < n; i++) {
                if (PdfOcrTesseract4Event.TESSERACT4_IMAGE_TO_PDF == eventCounter.getEvents().get(i)) {
                    foundPdfEvents++;
                } else if (PdfOcrTesseract4Event.TESSERACT4_IMAGE_OCR == eventCounter.getEvents().get(i)) {
                    foundImageEvents++;
                }
                Assert.assertEquals(metainfo, eventCounter.getMetaInfos().get(i));
            }
            Assert.assertEquals(expectedImageEvents, foundImageEvents);
            Assert.assertEquals(expectedPdfEvents, foundPdfEvents);
        } finally {
            EventCounterHandler.getInstance().unregister(factory);
        }
    }

    private static Thread getThread(DoImageOcrRunnable runnable) {
        return new Thread(runnable);
    }

    public static class TestEventCounter extends EventCounter {
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

    public enum ReaderType {
        LIB,
        EXECUTABLE
    }
}
