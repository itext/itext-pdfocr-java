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
package com.itextpdf.pdfocr.events.multithreading;

import com.itextpdf.kernel.counter.event.IMetaInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.OutputFormat;

import java.io.File;
import java.util.Arrays;

public class DoImageOcrRunnable implements Runnable {
    private AbstractTesseract4OcrEngine tesseractReader;
    private File imgFile;
    private File outputFile;
    private boolean createPdf;
    private IMetaInfo metaInfo;

    DoImageOcrRunnable(AbstractTesseract4OcrEngine tesseractReader, IMetaInfo metaInfo, File imgFile, File outputFile, boolean createPdf) {
        this.tesseractReader = tesseractReader;
        this.metaInfo = metaInfo;
        this.imgFile = imgFile;
        this.outputFile = outputFile;
        this.createPdf = createPdf;
    }

    public void run() {
        try {
            tesseractReader.setThreadLocalMetaInfo(metaInfo);
            if (createPdf) {
                new OcrPdfCreator(tesseractReader).createPdf(Arrays.asList(imgFile), new PdfWriter(outputFile));
            } else {
                tesseractReader.doTesseractOcr(imgFile, outputFile, OutputFormat.TXT);
            }
            // for test purposes
            System.out.println(imgFile.getName());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
