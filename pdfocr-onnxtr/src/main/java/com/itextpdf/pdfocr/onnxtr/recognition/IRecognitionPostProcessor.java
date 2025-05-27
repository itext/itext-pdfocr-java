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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;

/**
 * Interface for post-processors, which convert raw output of an ML model and
 * returns recognized characters as a string.
 */
public interface IRecognitionPostProcessor {
    /**
     * Process ML model output and return recognized characters as string.
     *
     * @param output raw output of the ML model
     *
     * @return recognized characters as string
     */
    String process(FloatBufferMdArray output);

    /**
     * Returns the size of the output character label vector. I.e. how many
     * distinct tokens/characters the model recognizes.
     *
     * @return the size of the output character label vector
     */
    int labelDimension();
}
