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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BatchingTest {
    @Test
    void wrapWithValidArgs() {
        final Iterator<List<Integer>> wrapped =
                Batching.wrap(Arrays.asList(1, 2, 3, 4, 5, 6, 7).iterator(), 2);
        Assertions.assertEquals(Arrays.asList(1, 2), wrapped.next());
        Assertions.assertEquals(Arrays.asList(3, 4), wrapped.next());
        Assertions.assertEquals(Arrays.asList(5, 6), wrapped.next());
        Assertions.assertEquals(Collections.singletonList(7), wrapped.next());
        Assertions.assertThrows(NoSuchElementException.class, wrapped::next);
    }

    @Test
    void wrapWithInvalidArgs() {
        Assertions.assertThrowsExactly(
                NullPointerException.class,
                () -> Batching.wrap(null, 2)
        );
        Assertions.assertThrowsExactly(
                IllegalArgumentException.class,
                () -> Batching.wrap(Collections.emptyIterator(), 0)
        );
    }
}
