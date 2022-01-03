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
package com.itextpdf.pdfocr.helpers;

import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.IProductAware;
import com.itextpdf.pdfocr.OcrEngineProperties;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.PdfOcrMetaInfoContainer;
import com.itextpdf.pdfocr.TextInfo;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomProductAwareOcrEngine implements IOcrEngine, IProductAware {

    private boolean getMetaInfoContainerTriggered = false;

    public CustomProductAwareOcrEngine() {
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        return Collections.<Integer, List<TextInfo>>emptyMap();
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
        return null;
    }

    @Override
    public PdfOcrMetaInfoContainer getMetaInfoContainer() {
        getMetaInfoContainerTriggered = true;
        return new PdfOcrMetaInfoContainer(new DummyMetaInfo());
    }

    @Override
    public ProductData getProductData() {
        return null;
    }

    public boolean isGetMetaInfoContainerTriggered() {
        return getMetaInfoContainerTriggered;
    }

    private static class DummyMetaInfo implements IMetaInfo {
    }
}
