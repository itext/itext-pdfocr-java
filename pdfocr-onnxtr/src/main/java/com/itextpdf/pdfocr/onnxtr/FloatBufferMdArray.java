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

    /**
     * Constructs a new {@code FloatBufferMdArray} with the specified data buffer and shape.
     *
     * @param data  the {@link FloatBuffer} containing the data for this array
     * @param shape the shape of the multidimensional array, where each entry specifies the size of a dimension
     *
     * @throws NullPointerException     if {@code data} or {@code shape} is {@code null}
     * @throws IllegalArgumentException if {@code shape} is invalid or the number of elements in {@code data}
     *                                   does not match the element count derived from {@code shape}
     */
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

    /**
     * Returns a duplicate of the backing {@link FloatBuffer}.
     *
     * @return a duplicate of the backing {@link FloatBuffer}
     */
    public FloatBuffer getData() {
        return data.duplicate();
    }

    /**
     * Returns a copy of the shape array that defines the dimensions of this multidimensional array.
     *
     * @return a copy of the shape array
     */
    public long[] getShape() {
        return shape.clone();
    }

    /**
     * Returns the number of dimensions of this multidimensional array.
     *
     * @return the number of dimensions
     */
    public int getDimensionCount() {
        return shape.length;
    }

    /**
     * Returns the size of the specified dimension.
     *
     * @param index the zero-based index of the dimension to query
     *
     * @return the size of the dimension at the specified index
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or greater than or equal to the number of dimensions
     */
    public int getDimension(int index) {
        if (index < 0 || index >= shape.length) {
            throw new IndexOutOfBoundsException();
        }
        return (int) shape[index];
    }

    /**
     * Returns a sub-array representing the slice at the specified index of the first dimension.
     *
     * @param index the index along the first dimension to retrieve
     *
     * @return a {@code FloatBufferMdArray} representing the specified sub-array
     *
     * @throws IllegalStateException     if this array has no dimensions
     * @throws IndexOutOfBoundsException if {@code index} is negative or exceeds the bounds of the first dimension
     */
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

    /**
     * Returns the scalar value at the specified index.
     *
     * <p>
     * This method only works on one-dimensional arrays, where the total element count matches the size of the first dimension
     *
     * @param index the index of the scalar to retrieve
     *
     * @return the scalar float value at the specified index
     *
     * @throws IllegalStateException if this array is not properly shaped as a one-dimensional array
     */
    public float getScalar(int index) {
        if (shape.length != 0 && (OrtUtil.elementCount(shape) != shape[0])) {
            throw new IllegalStateException();
        }
        return data.get(index);
    }

    /**
     * Gets internal offset of the provided float buffer array.
     *
     * @return internal offset
     */
    public int getArrayOffset() {
        return data.arrayOffset();
    }

    /**
     * Gets number of available bytes for read from provided float buffer array.
     *
     * @return number of available bytes for read
     */
    public int getArraySize() {
        return data.limit();
    }
}
