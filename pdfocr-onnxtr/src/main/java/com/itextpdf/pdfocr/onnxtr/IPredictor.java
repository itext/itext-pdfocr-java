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

import java.util.Iterator;

/**
 * Interface of a generic predictor. It takes a stream of inputs and returns a same-sizes stream of outputs.
 *
 * @param <T> input type
 * @param <R> output type
 */
public interface IPredictor<T, R> extends AutoCloseable {

    /**
     * Performs prediction on a sequence of input items.
     *
     * <p>
     * This method consumes the provided {@link Iterator} of inputs and produces an {@link Iterator}
     * of outputs, typically yielding one result per input item.
     *
     * @param inputs an {@link Iterator} over the input items to be processed
     *
     * @return an {@link Iterator} over the predicted output items
     */
    Iterator<R> predict(Iterator<T> inputs);

    /**
     * Performs prediction on a sequence of input items provided as an {@link Iterable}.
     *
     * @param inputs an {@link Iterable} over the input items to be processed
     *
     * @return an {@link Iterator} over the predicted output items
     */
    default Iterator<R> predict(Iterable<T> inputs) {
        return predict(inputs.iterator());
    }
}
