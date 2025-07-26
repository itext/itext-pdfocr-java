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

import java.io.File;
import java.util.List;

/**
 * Properties that are used by the {@link OnnxTrOcrEngine}.
 */
public class OnnxTrEngineProperties {

    /**
     * Creates a new {@link OnnxTrEngineProperties} instance.
     */
    public OnnxTrEngineProperties() {

    }
    /**
     * Defines the way text is retrieved and grouped from onnxtr engine output.
     * It changes the way text is selected in the result pdf document.
     * Does not affect the result of {@link com.itextpdf.pdfocr.IOcrEngine#createTxtFile(List, File)}.
     */
    private TextPositioning textPositioning;

    /**
     * Defines the way text is retrieved from ocr engine output using
     * {@link TextPositioning}.
     *
     * @return the way text is retrieved
     */
    public TextPositioning getTextPositioning() {
        return textPositioning;
    }

    /**
     * Defines the way text is retrieved from ocr engine output
     * using {@link TextPositioning}.
     *
     * @param textPositioning the way text is retrieved
     * @return the {@link OnnxTrEngineProperties} instance
     */
    public OnnxTrEngineProperties setTextPositioning(TextPositioning textPositioning) {
        this.textPositioning = textPositioning;
        return this;
    }
}
