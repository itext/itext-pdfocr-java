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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class StringMapperTest extends ExtendedITextTest {
    @Test
    public void initWithInvalidArgs() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new StringMapper((String) null)
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new StringMapper((String[]) null)
        );
    }

    @Test
    public void mapperFromString() {
        //                                             U+1FAE0
        final StringMapper mapper = new StringMapper("A\uD83E\uDEE0B");
        Assertions.assertEquals(3, mapper.size());
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> mapper.map(-1)
        );
        Assertions.assertEquals("A", mapper.map(0));
        Assertions.assertEquals("\uD83E\uDEE0", mapper.map(1));
        Assertions.assertEquals("B", mapper.map(2));
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> mapper.map(3)
        );
        Assertions.assertEquals(new StringMapper("A\uD83E\uDEE0B"), mapper);
        Assertions.assertNotEquals(new StringMapper("AB"), mapper);
    }

    @Test
    public void mapperFromArray() {
        //                                                 DE flag: U+1F1E9 U+1F1EA
        final String[] backingArray = new String[] {"AB", "\uD83C\uDDE9\uD83C\uDDEA", "CD"};
        final StringMapper mapper = new StringMapper(backingArray);
        Assertions.assertEquals(3, mapper.size());
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> mapper.map(-1)
        );
        Assertions.assertEquals("AB", mapper.map(0));
        Assertions.assertEquals("\uD83C\uDDE9\uD83C\uDDEA", mapper.map(1));
        Assertions.assertEquals("CD", mapper.map(2));
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> mapper.map(3)
        );
        Assertions.assertEquals(new StringMapper(backingArray).hashCode(), mapper.hashCode());
        Assertions.assertEquals(new StringMapper(backingArray), mapper);
        Assertions.assertNotEquals(new StringMapper("ABCD"), mapper);
    }
}
