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

import ai.onnxruntime.OrtUtil;
import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * Multidimensional array with a {@link FloatBuffer} backing storage.
 */
public class FloatBufferMdArray {
    private final FloatBuffer data;
    private final long[] shape;

    public FloatBufferMdArray(FloatBuffer data, long[] shape) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(shape);
        if (!OrtUtil.validateShape(shape)) {
            throw new IllegalArgumentException("Shape is not valid");
        }
        if (data.remaining() != OrtUtil.elementCount(shape)) {
            throw new IllegalArgumentException("Data element count does not match shape");
        }
        this.data = data.duplicate();
        this.shape = shape.clone();
    }

    public FloatBuffer getData() {
        return data.duplicate();
    }

    public long[] getShape() {
        return shape.clone();
    }

    public int getDimensionCount() {
        return shape.length;
    }

    public int getDimension(int index) {
        if (index < 0 || index >= shape.length) {
            throw new IndexOutOfBoundsException();
        }
        return (int) shape[index];
    }

    public FloatBufferMdArray getSubArray(int index) {
        if (shape.length == 0) {
            throw new IllegalStateException();
        }

        if (index < 0 || index >= shape[0]) {
            throw new IndexOutOfBoundsException();
        }
        final long[] newShape = new long[shape.length - 1];
        System.arraycopy(shape, 1, newShape, 0, newShape.length);
        final int subArraySize = (data.remaining() / (int) shape[0]);
        FloatBuffer newData = data.duplicate();
        newData.position(index * subArraySize);
        newData = newData.slice();
        newData.limit(subArraySize);
        return new FloatBufferMdArray(newData, newShape);
    }

    public float getScalar(int index) {
        if (shape.length != 0 && (OrtUtil.elementCount(shape) != shape[0])) {
            throw new IllegalStateException();
        }
        return data.get(index);
    }
}
