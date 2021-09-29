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
package com.itextpdf.pdfocr.helpers;

import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.IProductAware;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.OcrEngineProperties;
import com.itextpdf.pdfocr.PdfOcrMetaInfoContainer;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOcrEngine implements IOcrEngine {

    private OcrEngineProperties ocrEngineProperties;

    public CustomOcrEngine() {
    }

    public CustomOcrEngine(OcrEngineProperties ocrEngineProperties) {
        this.ocrEngineProperties = new OcrEngineProperties(ocrEngineProperties);
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        Map<Integer, List<TextInfo>> result =
                new HashMap<Integer, List<TextInfo>>();
        String text = PdfHelper.DEFAULT_TEXT;
        if (input.getAbsolutePath().contains(PdfHelper.THAI_IMAGE_NAME)) {
            text = PdfHelper.THAI_TEXT;
        }
        TextInfo textInfo = new TextInfo(text, new Rectangle(204.0f, 158.0f, 538.0f, 136.0f));
        result.put(1, Collections.<TextInfo>singletonList(textInfo));
        return result;
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
        return doImageOcr(input);
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile) {
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext) {

    }


    public OcrEngineProperties getOcrEngineProperties() {
        return ocrEngineProperties;
    }
}
