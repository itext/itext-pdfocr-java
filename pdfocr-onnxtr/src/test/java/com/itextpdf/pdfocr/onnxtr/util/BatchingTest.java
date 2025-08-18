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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class BatchingTest extends ExtendedITextTest {
    @Test
    public void wrapWithValidArgs() {
        final Iterator<List<Integer>> wrapped =
                Batching.wrap(Arrays.asList(1, 2, 3, 4, 5, 6, 7).iterator(), 2);
        Assertions.assertTrue(wrapped.hasNext());
        Assertions.assertEquals(Arrays.asList(1, 2), wrapped.next());
        Assertions.assertTrue(wrapped.hasNext());
        Assertions.assertEquals(Arrays.asList(3, 4), wrapped.next());
        Assertions.assertTrue(wrapped.hasNext());
        Assertions.assertEquals(Arrays.asList(5, 6), wrapped.next());
        Assertions.assertTrue(wrapped.hasNext());
        Assertions.assertEquals(Collections.singletonList(7), wrapped.next());
        Assertions.assertFalse(wrapped.hasNext());
    }

    @Test
    public void wrapWithInvalidArgs() {
        Exception nullPtrException = Assertions.assertThrows(NullPointerException.class,
                () -> Batching.wrap((Iterator<Object>) null, 2).hasNext()
        );
        Assertions.assertEquals(NullPointerException.class, nullPtrException.getClass());
        Exception illegalArgException = Assertions.assertThrows(IllegalArgumentException.class,
                () -> Batching.wrap(Collections.<Object>emptyIterator(), 0).hasNext()
        );
        Assertions.assertEquals(IllegalArgumentException.class, illegalArgException.getClass());
    }
}
