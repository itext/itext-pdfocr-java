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

import java.util.Arrays;
import java.util.Objects;

/**
 * Properties of the input of an ONNX model, which expects an RGB image.
 * <p>
 * It contains the input shape, as a [batchSize, channel, height, width] array, mean and standard
 * deviation values for normalization, whether padding should be symmetrical or not.
 * </p>
 */
public class OnnxInputProperties {
    /**
     * Expected channel count. We expect RGB format.
     */
    public static final int EXPECTED_CHANNEL_COUNT = 3;
    /**
     * Expected shape size. We inspect the standard BCHW format (batch, channel, height, width).
     */
    public static final int EXPECTED_SHAPE_SIZE = 4;

    /**
     * Per-channel mean, used for normalization. Should be EXPECTED_SHAPE_SIZE length.
     */
    private final float[] mean;
    /**
     * Per-channel standard deviation, used for normalization. Should be EXPECTED_SHAPE_SIZE length.
     */
    private final float[] std;
    /**
     * Target input shape. Should be EXPECTED_SHAPE_SIZE length.
     */
    private final long[] shape;
    /**
     * Whether padding should be symmetrical during input resizing.
     */
    private final boolean symmetricPad;

    /**
     * Creates model input properties.
     *
     * @param mean         Per-channel mean, used for normalization.
     *                     Should be EXPECTED_SHAPE_SIZE length.
     * @param std          Per-channel standard deviation, used for normalization.
     *                     Should be EXPECTED_SHAPE_SIZE length.
     * @param shape        Target input shape.
     *                     Should be EXPECTED_SHAPE_SIZE length.
     * @param symmetricPad Whether padding should be symmetrical during input resizing.
     */
    public OnnxInputProperties(float[] mean, float[] std, long[] shape, boolean symmetricPad) {
        Objects.requireNonNull(mean);
        if (mean.length != EXPECTED_CHANNEL_COUNT) {
            throw new IllegalArgumentException("mean should be a " + EXPECTED_CHANNEL_COUNT + "-element array");
        }
        Objects.requireNonNull(std);
        if (std.length != EXPECTED_CHANNEL_COUNT) {
            throw new IllegalArgumentException("std should be a " + EXPECTED_CHANNEL_COUNT + "-element array");
        }
        Objects.requireNonNull(shape);
        if (shape.length != EXPECTED_SHAPE_SIZE) {
            throw new IllegalArgumentException("shape should be a " + EXPECTED_SHAPE_SIZE + "-element array (BCHW)");
        }
        if (shape[1] != EXPECTED_CHANNEL_COUNT) {
            throw new IllegalArgumentException("Model only supports RGB images with a BCHW input format");
        }
        for (final long dim : shape) {
            if (dim <= 0 || ((int) dim) != dim) {
                throw new IllegalArgumentException("Unexpected dimension value: " + dim);
            }
        }

        this.mean = mean.clone();
        this.std = std.clone();
        this.shape = shape.clone();
        this.symmetricPad = symmetricPad;
    }

    /**
     * Returns per-channel mean, used for normalization.
     *
     * @return Per-channel mean, used for normalization.
     */
    public float[] getMean() {
        return mean.clone();
    }

    /**
     * Returns channel-specific mean, used for normalization.
     *
     * @param index Index of the channel.
     *
     * @return Channel-specific mean, used for normalization.
     */
    public float getMean(int index) {
        return mean[index];
    }

    /**
     * Returns red channel mean, used for normalization.
     *
     * @return Red channel mean, used for normalization.
     */
    public float getRedMean() {
        return getMean(0);
    }

    /**
     * Returns green channel mean, used for normalization.
     *
     * @return Green channel mean, used for normalization.
     */
    public float getGreenMean() {
        return getMean(1);
    }

    /**
     * Returns blue channel mean, used for normalization.
     *
     * @return Blue channel mean, used for normalization.
     */
    public float getBlueMean() {
        return getMean(2);
    }

    /**
     * Returns per-channel standard deviation, used for normalization.
     *
     * @return Per-channel standard deviation, used for normalization.
     */
    public float[] getStd() {
        return std.clone();
    }

    /**
     * Returns channel-specific standard deviation, used for normalization.
     *
     * @param index Index of the channel.
     *
     * @return Channel-specific standard deviation, used for normalization.
     */
    public float getStd(int index) {
        return std[index];
    }

    /**
     * Returns red channel standard deviation, used for normalization.
     *
     * @return Red channel standard deviation, used for normalization.
     */
    public float getRedStd() {
        return getStd(0);
    }

    /**
     * Returns green channel standard deviation, used for normalization.
     *
     * @return Green channel standard deviation, used for normalization.
     */
    public float getGreenStd() {
        return getStd(1);
    }

    /**
     * Returns blue channel standard deviation, used for normalization.
     *
     * @return Blue channel standard deviation, used for normalization.
     */
    public float getBlueStd() {
        return getStd(2);
    }

    /**
     * Returns target input shape.
     *
     * @return Target input shape.
     */
    public long[] getShape() {
        return shape.clone();
    }

    /**
     * Returns target input dimension value.
     *
     * @param index Index of the dimension.
     *
     * @return Target input dimension value.
     */
    public int getShape(int index) {
        return (int) shape[index];
    }

    /**
     * Returns input batch size.
     *
     * @return Input batch size.
     */
    public int getBatchSize() {
        return getShape(0);
    }

    /**
     * Returns input channel count.
     *
     * @return Input channel count.
     */
    public int getChannelCount() {
        return getShape(1);
    }

    /**
     * Returns input height.
     *
     * @return Input height.
     */
    public int getHeight() {
        return getShape(2);
    }

    /**
     * Returns input width.
     *
     * @return Input width.
     */
    public int getWidth() {
        return getShape(3);
    }

    /**
     * Returns whether padding should be symmetrical during input resizing.
     *
     * @return Whether padding should be symmetrical during input resizing.
     */
    public boolean useSymmetricPad() {
        return symmetricPad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(mean), Arrays.hashCode(std), Arrays.hashCode(shape), symmetricPad);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OnnxInputProperties that = (OnnxInputProperties) o;
        return symmetricPad == that.symmetricPad && Objects.deepEquals(mean, that.mean)
                && Objects.deepEquals(std, that.std) && Objects.deepEquals(shape, that.shape);
    }

    @Override
    public String toString() {
        return "OnnxInputProperties{" +
                "mean=" + Arrays.toString(mean) +
                ", std=" + Arrays.toString(std) +
                ", shape=" + Arrays.toString(shape) +
                ", symmetricPad=" + symmetricPad +
                '}';
    }
}
