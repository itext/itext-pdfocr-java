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

import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Wrapper class around {@link java.nio.FloatBuffer}.
 */
public class FloatBufferWrapper {

    private final FloatBuffer floatBuffer;

    /**
     * Constructs {@link FloatBufferWrapper} on top of {@link FloatBuffer}.
     *
     * @param floatBuffer buffer on top of which {@link FloatBufferWrapper} will be built
     */
    public FloatBufferWrapper(FloatBuffer floatBuffer) {
        this.floatBuffer = floatBuffer;
    }

    /**
     * Returns {@link FloatBuffer} that backs this buffer.
     *
     * <p>
     * Modifications to this buffer's content will cause the returned
     * buffer's content to be modified, and vice versa.
     *
     * @return the array that backs this buffer
     */
    public FloatBuffer getFloatBuffer() {
        return floatBuffer;
    }

    /**
     * Returns the float array that backs this buffer.
     *
     * <p>
     * Modifications to this buffer's content will cause the returned
     * array's content to be modified, and vice versa.
     *
     * @return the array that backs this buffer
     */
    public float[] array() {
        return floatBuffer.array();
    }

    /**
     * Returns the offset within this buffer's backing array of the first
     * element of the buffer.
     *
     * <p>
     * If this buffer is backed by an array then buffer position
     * corresponds to array index position.
     *
     * @return the offset within this buffer's array
     * of the first element of the buffer
     */
    public int arrayOffset() {
        return floatBuffer.arrayOffset();
    }

    /**
     * Relative get method.  Reads the float at this buffer's
     * current position, and then increments the position.
     *
     * @return the float at the buffer's current position
     */
    public float get() {
        return floatBuffer.get();
    }

    /**
     * Absolute get method. Reads the float at the given index.
     *
     * @param index the index from which the float will be read
     *
     * @return the float at the given index
     */
    public float get(int index) {
        return floatBuffer.get(index);
    }

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p>
     * This method transfers floats from this buffer into the given
     * destination array.
     *
     * @param dst the destination array
     *
     * @return this buffer
     */
    public FloatBufferWrapper get(float[] dst) {
        floatBuffer.get(dst);
        return this;
    }

    /**
     * Rewinds this buffer.  The position is set to zero.
     *
     * <p>
     * Invoke this method before a sequence of channel-write or get
     * operations, assuming that the limit has already been set
     * appropriately.
     *
     * @return this buffer
     */
    public FloatBufferWrapper rewind() {
        floatBuffer.rewind();
        return this;
    }

    /**
     * Relative put method.
     *
     * <p>
     * Writes the given float into this buffer at the current
     * position, and then increments the position.
     *
     * @param value the float to be written
     *
     * @return this buffer
     */
    public FloatBufferWrapper put(float value) {
        floatBuffer.put(value);
        return this;
    }

    /**
     * Relative bulk put method.
     *
     * <p>
     * This method transfers floats into this buffer from the given source array.
     *
     * @param src the array from which floats are to be read
     * @param offset the offset within the array of the first float to be read;
     * must be non-negative and no larger than {@code array.length}
     * @param length the number of floats to be read from the given array;
     * must be non-negative and no larger than {@code array.length - offset}
     *
     * @return this buffer
     */
    public FloatBufferWrapper put(float[] src, int offset, int length) {
        floatBuffer.put(src, offset, length);
        return this;
    }

    /**
     * Returns this buffer's limit.
     *
     * @return the limit of this buffer
     */
    public int limit() {
        return floatBuffer.limit();
    }

    /**
     * Sets this buffer's limit. If the position is larger than the new limit
     * then it is set to the new limit.
     *
     * @param newLimit the new limit value; must be non-negative and no larger than this buffer's capacity
     *
     * @return this buffer
     */
    public FloatBufferWrapper limit(int newLimit) {
        floatBuffer.limit(newLimit);
        return this;
    }

    /**
     * Creates a new float buffer that shares this buffer's content.
     *
     * <p>
     * The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer, and vice
     * versa; the two buffers' position and limit will be
     * independent.
     *
     * <p>
     * The new buffer's capacity, limit, position and byte order will be identical to those of this buffer.
     *
     * @return the new float buffer
     */
    public FloatBufferWrapper duplicate() {
        return new FloatBufferWrapper(floatBuffer.duplicate());
    }

    /**
     * Returns the number of elements between the current position and the limit.
     *
     * @return the number of elements remaining in this buffer
     */
    public int remaining() {
        return floatBuffer.remaining();
    }

    /**
     * Sets this buffer's position.
     *
     * @param newPosition the new position value; must be non-negative and no larger than the current limit
     *
     * @return this buffer
     */
    public FloatBufferWrapper position(int newPosition) {
        floatBuffer.position(newPosition);
        return this;
    }


    /**
     * Creates a new float buffer whose content is a shared subsequence of
     * this buffer's content.
     *
     * <p>
     * The content of the new buffer will start at this buffer's current
     * position.  Changes to this buffer's content will be visible in the new
     * buffer, and vice versa; the two buffers' position and limit values will be independent.
     *
     * <p>
     * The new buffer's position will be zero, its capacity and its limit
     * will be the number of floats remaining in this buffer and its byte order
     * will be identical to that of this buffer.
     *
     * @return the new float buffer
     */
    public FloatBufferWrapper slice() {
        return new FloatBufferWrapper(floatBuffer.slice());
    }

    /**
     * Wraps a float array into a buffer.
     *
     * <p>
     * The new buffer will be backed by the given float array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's capacity and limit will be
     * {@code array.length}, its position will be zero and its byte order
     * will be the {@link ByteOrder#nativeOrder native order} of the underlying hardware.
     * Its {@link #array backing array} will be the given array, and its {@link #arrayOffset array offset} will be zero.
     *
     * @param array the array that will back this buffer
     *
     * @return the new float buffer
     */
    public static FloatBufferWrapper wrap(float[] array) {
        return new FloatBufferWrapper(FloatBuffer.wrap(array));
    }

    /**
     * Allocates a new float buffer.
     *
     * <p>
     * The new buffer's position will be zero, its limit will be its
     * capacity, its mark will be undefined, each of its elements will be
     * initialized to zero, and its byte order will be
     * the {@link ByteOrder#nativeOrder native order} of the underlying
     * hardware.
     * It will have a {@link #array backing array}, and its
     * {@link #arrayOffset array offset} will be zero.
     *
     * @param capacity the new buffer's capacity, in floats
     *
     * @return the new float buffer
     */
    public static FloatBufferWrapper allocate(int capacity) {
        return new FloatBufferWrapper(FloatBuffer.allocate(capacity));
    }
}
