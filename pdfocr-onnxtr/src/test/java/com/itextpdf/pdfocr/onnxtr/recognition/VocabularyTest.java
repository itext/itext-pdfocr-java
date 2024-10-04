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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VocabularyTest {
    @Test
    void initWithInvalidArgs() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new Vocabulary(null)
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                //                            U+1FAE0
                () -> new Vocabulary("ABC" + "\uD83E\uDEE0" + "DEF")
        );
    }

    @Test
    void valid() {
        final Vocabulary vocabulary = new Vocabulary("ABC");
        Assertions.assertEquals("ABC", vocabulary.getLookUpString());
        Assertions.assertEquals("ABC", vocabulary.toString());
        Assertions.assertEquals(3, vocabulary.size());
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> vocabulary.map(-1)
        );
        Assertions.assertEquals('A', vocabulary.map(0));
        Assertions.assertEquals('B', vocabulary.map(1));
        Assertions.assertEquals('C', vocabulary.map(2));
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> vocabulary.map(3)
        );
        Assertions.assertEquals(new Vocabulary("ABC").hashCode(), vocabulary.hashCode());
        Assertions.assertEquals(new Vocabulary("ABC"), vocabulary);
        Assertions.assertNotEquals(new Vocabulary("ABCD"), vocabulary);
    }
}
