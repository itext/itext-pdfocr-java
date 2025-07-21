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

import com.itextpdf.test.ExtendedITextTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class BatchProcessingGeneratorTest extends ExtendedITextTest {
    @Test
    public void initWithInvalidArgs() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new BatchProcessingGenerator<>(null, null)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new BatchProcessingGenerator<>(Collections.<List<Object>>emptyIterator(), null)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new BatchProcessingGenerator<>(null, new IBatchProcessor<Object, Integer>() {
                    @Override
                    public List<Integer> processBatch(List<Object> batch) {
                        return Collections.nCopies(batch.size(), 1);
                    }
                }
                )
        );
    }

    @Test
    public void processorReturnsNull() {
        final BatchProcessingGenerator<Integer, Object> generator = new BatchProcessingGenerator<>(
                Collections.singletonList(Collections.singletonList(Integer.valueOf(1))).iterator(),
                new IBatchProcessor<Integer, Object>() {
                    @Override
                    public List<Object> processBatch(List<Integer> batch) {
                        return null;
                    }
                }
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> generator.next()
        );
    }

    @Test
    public void processorReturnsIncorrectSize() {
        final BatchProcessingGenerator<Integer, Object> generator = new BatchProcessingGenerator<>(
                Collections.singletonList(Collections.singletonList(1)).iterator(),
                new IBatchProcessor<Integer, Object>() {
                    @Override
                    public List<Object> processBatch(List<Integer> batch) {
                        return Collections.nCopies(batch.size() + 1, batch.get(0));
                    }
                }
        );
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> generator.next()
        );
    }

    @Test
    public void valid() {
        final int[] processorCallCount = {0};
        final BatchProcessingGenerator<Integer, String> generator = new BatchProcessingGenerator<>(
                Arrays.asList(Collections.singletonList(1), Arrays.asList(2, 3)).iterator(),
                new IBatchProcessor<Integer, String>() {
                    @Override
                    public List<String> processBatch(List<Integer> batch) {
                        ++processorCallCount[0];
                        return batch.stream()
                                .map(x -> Integer.toString(x * 2))
                                .collect(Collectors.toList());
                    }
                }
        );
        Assertions.assertTrue(generator.hasNext());
        Assertions.assertEquals("2", generator.next());
        Assertions.assertTrue(generator.hasNext());
        Assertions.assertEquals("4", generator.next());
        Assertions.assertTrue(generator.hasNext());
        Assertions.assertEquals("6", generator.next());
        Assertions.assertFalse(generator.hasNext());
        Assertions.assertEquals(2, processorCallCount[0]);
    }
}
