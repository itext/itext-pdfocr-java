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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Static utility class to help with batching.
 */
public final class Batching {
    private Batching() {
    }

    /**
     * Wraps an existing iterator into a new one, which output List-based batches,
     *
     * @param iterator Iterator to wrap.
     * @param batchSize Target batch size. Last batch might have smaller size.
     *
     * @return Batch iterator wrapper.
     *
     * @param <E> Input iterator element type.
     */
    public static <E> Iterator<List<E>> wrap(Iterator<E> iterator, int batchSize) {
        Objects.requireNonNull(iterator);
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize should be positive");
        }
        return new Iterator<List<E>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public List<E> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final List<E> batch = new ArrayList<>(batchSize);
                for (int i = 0; i < batchSize && iterator.hasNext(); ++i) {
                    batch.add(iterator.next());
                }
                return batch;
            }
        };
    }
}
