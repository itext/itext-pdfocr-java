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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class FloatBufferWrapperTest extends ExtendedITextTest {

    private FloatBufferWrapper floatBufferWrapper;
    private float[] array;

    @BeforeEach
    public void setUp() {
        array = new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        floatBufferWrapper = FloatBufferWrapper.wrap(array);
    }

    @Test
    public void wrapTest() {
        Assertions.assertArrayEquals(array, floatBufferWrapper.array());
    }

    @Test
    public void offsetTest() {
        Assertions.assertEquals(0, floatBufferWrapper.arrayOffset());
    }

    @Test
    public void getTest() {
        Assertions.assertEquals(1.0f, floatBufferWrapper.get());
        Assertions.assertEquals(2.0f, floatBufferWrapper.get());
        Assertions.assertEquals(3.0f, floatBufferWrapper.get());
    }

    @Test
    public void get2Test() {
        Assertions.assertEquals(3.0f, floatBufferWrapper.get(2));
    }

    @Test
    public void get3Test() {
        float[] actualArray = new float[5];
        floatBufferWrapper.get(actualArray);
        Assertions.assertArrayEquals(array, actualArray);
    }

    @Test
    public void rewindTest() {
        floatBufferWrapper.get();
        floatBufferWrapper.rewind();
        Assertions.assertEquals(1.0f, floatBufferWrapper.get());
    }

    @Test
    public void putTest() {
        floatBufferWrapper.put(6.0f);
        Assertions.assertEquals(6.0f, floatBufferWrapper.get(0));
    }

    @Test
    public void limitTest() {
        Assertions.assertEquals(5, floatBufferWrapper.limit());
    }

    @Test
    public void limit2Test() {
        floatBufferWrapper.limit(2);
        Assertions.assertEquals(2, floatBufferWrapper.limit());
    }

    @Test
    public void duplicateTest() {
        floatBufferWrapper.position(2);
        floatBufferWrapper.limit(4);
        FloatBufferWrapper duplicate = floatBufferWrapper.duplicate();
        Assertions.assertArrayEquals(floatBufferWrapper.array(), duplicate.array());
        Assertions.assertEquals(floatBufferWrapper.limit(), duplicate.limit());
        Assertions.assertEquals(floatBufferWrapper.get(), duplicate.get());
    }

    @Test
    public void remainingTest() {
        Assertions.assertEquals(5, floatBufferWrapper.remaining());
    }

    @Test
    public void positionTest() {
        floatBufferWrapper.position(2);
        Assertions.assertEquals(3.0f, floatBufferWrapper.get());
    }

    @Test
    public void sliceTest() {
        floatBufferWrapper.position(2);
        FloatBufferWrapper newBuffer = floatBufferWrapper.slice();
        Assertions.assertArrayEquals(array, newBuffer.array());
        Assertions.assertEquals(2, newBuffer.arrayOffset());
    }

    @Test
    public void allocateTest() {
        FloatBufferWrapper buffer = FloatBufferWrapper.allocate(3);
        Assertions.assertEquals(3, buffer.limit());
    }
}
