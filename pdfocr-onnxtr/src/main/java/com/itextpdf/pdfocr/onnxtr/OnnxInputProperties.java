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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;

import java.util.Arrays;
import java.util.Objects;

/**
 * Properties of the input of an ONNX model, which expects an RGB image.
 *
 * <p>
 * It contains the input shape, as a [batchSize, channel, height, width] array, mean and standard
 * deviation values for normalization, whether padding should be symmetrical or not.
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
     * Per-channel mean, used for normalization. Should be EXPECTED_CHANNEL_COUNT length.
     */
    private final float[] mean;

    /**
     * Per-channel standard deviation, used for normalization. Should be EXPECTED_CHANNEL_COUNT length.
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
     * @param mean per-channel mean, used for normalization. Should be EXPECTED_CHANNEL_COUNT length
     * @param std per-channel standard deviation, used for normalization. Should be EXPECTED_CHANNEL_COUNT length
     * @param shape target input shape. Should be EXPECTED_SHAPE_SIZE length
     * @param symmetricPad whether padding should be symmetrical during input resizing
     */
    public OnnxInputProperties(float[] mean, float[] std, long[] shape, boolean symmetricPad) {
        Objects.requireNonNull(mean);
        if (mean.length != EXPECTED_CHANNEL_COUNT) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MEAN_CHANNEL_COUNT, EXPECTED_CHANNEL_COUNT));
        }
        Objects.requireNonNull(std);
        if (std.length != EXPECTED_CHANNEL_COUNT) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_STD_CHANNEL_COUNT, EXPECTED_CHANNEL_COUNT));
        }
        Objects.requireNonNull(shape);
        if (shape.length != EXPECTED_SHAPE_SIZE) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_SHAPE_SIZE, EXPECTED_SHAPE_SIZE));
        }
        if (shape[1] != EXPECTED_CHANNEL_COUNT) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.MODEL_ONLY_SUPPORTS_RGB);
        }
        for (final long dim : shape) {
            if (dim <= 0 || ((int) dim) != dim) {
                throw new IllegalArgumentException(MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_DIMENSION_VALUE, dim));
            }
        }

        this.mean = new float[mean.length];
        System.arraycopy(mean, 0, this.mean, 0, mean.length);
        this.std = new float[std.length];
        System.arraycopy(std, 0, this.std, 0, std.length);
        this.shape = new long[shape.length];
        System.arraycopy(shape, 0, this.shape, 0, shape.length);
        this.symmetricPad = symmetricPad;
    }

    /**
     * Returns per-channel mean, used for normalization.
     *
     * @return per-channel mean, used for normalization
     */
    public float[] getMean() {
        float[] copy = new float[shape.length];
        System.arraycopy(mean, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Returns channel-specific mean, used for normalization.
     *
     * @param index index of the channel
     *
     * @return channel-specific mean, used for normalization
     */
    public float getMean(int index) {
        return mean[index];
    }

    /**
     * Returns red channel mean, used for normalization.
     *
     * @return red channel mean, used for normalization
     */
    public float getRedMean() {
        return getMean(0);
    }

    /**
     * Returns green channel mean, used for normalization.
     *
     * @return green channel mean, used for normalization
     */
    public float getGreenMean() {
        return getMean(1);
    }

    /**
     * Returns blue channel mean, used for normalization.
     *
     * @return blue channel mean, used for normalization
     */
    public float getBlueMean() {
        return getMean(2);
    }

    /**
     * Returns per-channel standard deviation, used for normalization.
     *
     * @return per-channel standard deviation, used for normalization
     */
    public float[] getStd() {
        float[] copy = new float[shape.length];
        System.arraycopy(std, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Returns channel-specific standard deviation, used for normalization.
     *
     * @param index index of the channel
     *
     * @return channel-specific standard deviation, used for normalization
     */
    public float getStd(int index) {
        return std[index];
    }

    /**
     * Returns red channel standard deviation, used for normalization.
     *
     * @return red channel standard deviation, used for normalization
     */
    public float getRedStd() {
        return getStd(0);
    }

    /**
     * Returns green channel standard deviation, used for normalization.
     *
     * @return green channel standard deviation, used for normalization
     */
    public float getGreenStd() {
        return getStd(1);
    }

    /**
     * Returns blue channel standard deviation, used for normalization.
     *
     * @return blue channel standard deviation, used for normalization
     */
    public float getBlueStd() {
        return getStd(2);
    }

    /**
     * Returns target input shape.
     *
     * @return target input shape
     */
    public long[] getShape() {
        long[] copy = new long[shape.length];
        System.arraycopy(shape, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Returns target input dimension value.
     *
     * @param index index of the dimension
     *
     * @return target input dimension value
     */
    public int getShape(int index) {
        return (int) shape[index];
    }

    /**
     * Returns input batch size.
     *
     * @return input batch size
     */
    public int getBatchSize() {
        return getShape(0);
    }

    /**
     * Returns input channel count.
     *
     * @return input channel count
     */
    public int getChannelCount() {
        return getShape(1);
    }

    /**
     * Returns input height.
     *
     * @return input height
     */
    public int getHeight() {
        return getShape(2);
    }

    /**
     * Returns input width.
     *
     * @return input width
     */
    public int getWidth() {
        return getShape(3);
    }

    /**
     * Returns whether padding should be symmetrical during input resizing.
     *
     * @return whether padding should be symmetrical during input resizing
     */
    public boolean useSymmetricPad() {
        return symmetricPad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object) Arrays.hashCode(mean), Arrays.hashCode(std), Arrays.hashCode(shape), symmetricPad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OnnxInputProperties that = (OnnxInputProperties) o;
        return symmetricPad == that.symmetricPad && Arrays.equals(mean, that.mean)
                && Arrays.equals(std, that.std) && Arrays.equals(shape, that.shape);
    }

    /**
     * {@inheritDoc}
     */
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
