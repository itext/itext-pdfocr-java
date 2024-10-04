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
package com.itextpdf.pdfocr.onnxtr.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Generator with batch processing. It goes over and processes input values in batches and results
 * are cached. This is useful for use in ML-models, where you want to process stuff in batches
 * instead of one-by-ine.
 *
 * @param <T> Input type.
 * @param <R> Output type.
 */
public class BatchProcessingGenerator<T, R> implements Iterator<R> {
    private final Iterator<List<T>> batchIterator;
    private final IBatchProcessor<T, R> batchProcessor;
    /**
     * Processing result cache.
     */
    private List<R> batchResult = null;
    /**
     * Current position in the processing result cache.
     */
    private int batchResultIndex;

    /**
     * Creates a new generator with the provided batch iterator and processor.
     *
     * @param batchIterator  Input batch iterator.
     * @param batchProcessor Batch processor.
     */
    public BatchProcessingGenerator(Iterator<List<T>> batchIterator, IBatchProcessor<T, R> batchProcessor) {
        this.batchIterator = Objects.requireNonNull(batchIterator);
        this.batchProcessor = Objects.requireNonNull(batchProcessor);
    }

    @Override
    public boolean hasNext() {
        return batchIterator.hasNext() || batchResult != null;
    }

    @Override
    public R next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (batchResult == null) {
            final List<T> batch = batchIterator.next();
            batchResult = batchProcessor.processBatch(batch);
            if (batchResult == null || batchResult.size() != batch.size()) {
                throw new IllegalStateException("Batch processing failed: invalid number of outputs");
            }
            batchResultIndex = 0;
        }

        final R result = batchResult.get(batchResultIndex);
        ++batchResultIndex;
        if (batchResultIndex >= batchResult.size()) {
            batchResult = null;
        }
        return result;
    }
}
