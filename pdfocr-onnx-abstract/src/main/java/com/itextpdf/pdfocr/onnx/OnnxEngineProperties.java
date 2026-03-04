/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.pdfocr.onnx.text.TextPositioning;

import java.io.File;
import java.util.List;

/**
 * Properties that are used by the {@link OnnxOcrEngine}.
 */
public class OnnxEngineProperties {

    /**
     * Creates a new {@link OnnxEngineProperties} instance.
     */
    public OnnxEngineProperties() {

    }

    /**
     * Defines the way text is retrieved and grouped from onnx engine output.
     * It changes the way text is selected in the result pdf document.
     * Does not affect the result of {@link com.itextpdf.pdfocr.IOcrEngine#createTxtFile(List, File)}.
     */
    private TextPositioning textPositioning =
            TextPositioning.BY_WORDS_AND_LINES;

    /**
     * Gets the way text is retrieved from ocr engine output
     * using {@link TextPositioning}.
     *
     * @return the way text is retrieved
     */
    public TextPositioning getTextPositioningMode() {
        return textPositioning;
    }

    /**
     * Defines the way text is retrieved from ocr engine output
     * using {@link TextPositioning}.
     *
     * @param textPositioning the way text is retrieved
     *
     * @return the {@link OnnxEngineProperties} instance
     */
    public OnnxEngineProperties setTextPositioning(TextPositioning textPositioning) {
        this.textPositioning = textPositioning;
        return this;
    }
}
