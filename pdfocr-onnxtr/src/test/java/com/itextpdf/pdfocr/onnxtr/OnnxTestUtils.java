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
package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class OnnxTestUtils {

    public static ExtractionStrategy extractTextFromLayer(PdfDocument pdfDocument, int pageNr, String layerName) {
        ExtractionStrategy strategy = new ExtractionStrategy(layerName);
        PdfCanvasProcessor processor = new PdfCanvasProcessor(strategy);

        processor.processPageContent(pdfDocument.getPage(pageNr));
        return strategy;
    }

    protected static String getTextFromImage(File imageFile, IOcrEngine ocrEngine) {
        Map<Integer, List<TextInfo>> integerListMap = ocrEngine.doImageOcr(imageFile);
        return getStringFromListMap(integerListMap);
    }

    private static String getStringFromListMap(Map<Integer, List<TextInfo>> listMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Entry<Integer, List<TextInfo>> entry : listMap.entrySet()) {
            for (TextInfo textInfo : entry.getValue()) {
                if (textInfo.getText() != null) {
                    stringBuilder.append(textInfo.getText()).append('\n');
                }
            }
        }
        return stringBuilder.toString();
    }
}
