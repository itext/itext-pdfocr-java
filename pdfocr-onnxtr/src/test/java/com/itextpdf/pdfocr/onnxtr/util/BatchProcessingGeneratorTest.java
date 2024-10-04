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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BatchProcessingGeneratorTest {
    @Test
    void initWithInvalidArgs() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new BatchProcessingGenerator<>(null, null)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new BatchProcessingGenerator<>(Collections.emptyIterator(), null)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new BatchProcessingGenerator<>(null, batch ->
                        Collections.nCopies(batch.size(), 1)
                )
        );
    }

    @Test
    void processorReturnsNull() {
        final BatchProcessingGenerator<Integer, Object> generator = new BatchProcessingGenerator<>(
                Collections.singletonList(Collections.singletonList(1)).iterator(),
                batch -> null
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                generator::next
        );
    }

    @Test
    void processorReturnsIncorrectSize() {
        final BatchProcessingGenerator<Integer, Object> generator = new BatchProcessingGenerator<>(
                Collections.singletonList(Collections.singletonList(1)).iterator(),
                batch -> Collections.nCopies(batch.size() + 1, batch.get(0))
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                generator::next
        );
    }

    @Test
    void valid() {
        final int[] processorCallCount = {0};
        final BatchProcessingGenerator<Integer, String> generator = new BatchProcessingGenerator<>(
                Arrays.asList(Collections.singletonList(1), Arrays.asList(2, 3)).iterator(),
                (List<Integer> batch) -> {
                    ++processorCallCount[0];
                    return batch.stream()
                            .map(x -> Integer.toString(x * 2))
                            .collect(Collectors.toList());
                }
        );
        Assertions.assertEquals("2", generator.next());
        Assertions.assertEquals("4", generator.next());
        Assertions.assertEquals("6", generator.next());
        Assertions.assertThrows(NoSuchElementException.class, generator::next);
        Assertions.assertEquals(2, processorCallCount[0]);
    }
}
