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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;

import java.util.Arrays;
import java.util.Objects;

/**
 * Properties of the input of an ONNX model, which expects an image.
 *
 * <p>
 * It contains the input shape (batchSize, channel, height, width), mean and standard
 * deviation values for normalization, what type of padding should be used.
 */
public class OnnxInputProperties {
    /**
     * Expected channel count. We expect RGB format.
     *
     * @deprecated Grayscale and BGR are now supported as well. Check the
     *             documentation for more information.
     */
    @Deprecated
    public static final int EXPECTED_CHANNEL_COUNT = 3;

    /**
     * Expected shape size. We expect the standard BCHW format (batch, channel, height, width).
     */
    public static final int EXPECTED_SHAPE_SIZE = 4;

    /**
     * Per-channel mean, used for normalization. Expected length
     * of the array is based on the specified channel configuration in the
     * image resize options.
     */
    private final float[] mean;

    /**
     * Per-channel standard deviation, used for normalization. Expected length
     * of the array is based on the specified channel configuration in the
     * image resize options.
     */
    private final float[] std;

    /**
     * Options, that control the way the input images for the models will be
     * converted, resized and padded for ML model input.
     */
    private final ImageResizeOptions imageResizeOptions;

    /**
     * Batch size used for the ML model input.
     *
     * <p>
     * Default value is 1. If a GPU is used for calculations, it is worthwhile
     * to bump this value as high as your VRAM allows you to.
     */
    private final int batchSize;

    /**
     * Creates model input properties.
     *
     * @param mean per-channel mean, used for normalization. Should be EXPECTED_CHANNEL_COUNT length
     * @param std per-channel standard deviation, used for normalization. Should be EXPECTED_CHANNEL_COUNT length
     * @param shape target input shape. Should be EXPECTED_SHAPE_SIZE length
     * @param symmetricPad whether padding should be symmetrical during input resizing
     *
     * @deprecated This is the original constructor, which only supported RGB inputs with a static
     *             width/height and black pixel values padding. Use constructors with an
     *             ImageResizeOptions parameter instead.
     */
    @Deprecated
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
        this.imageResizeOptions = new ImageResizeOptions(
                ImageChannelConfiguration.RGB,
                (int) shape[3], (int) shape[2],
                (symmetricPad ? PaddingStrategy.SYMMETRIC_BLACK : PaddingStrategy.BOTTOM_RIGHT_BLACK)
        );
        this.batchSize = (int) shape[0];
    }

    /**
     * Creates model input properties.
     *
     * @param imageResizeOptions options, that control the way the input images for the models will
     *                           be converted, resized and padded for ML model input
     * @param mean               per-channel mean, used for normalization. Length of the array
     *                           should match the channel count in the image resize options
     * @param std                per-channel standard deviation, used for normalization. Length of
     *                           the array should match the channel count in the image resize
     *                           options
     * @param batchSize          size of the batch used for the ML model. Should be a positive
     *                           number
     */
    public OnnxInputProperties(
            ImageResizeOptions imageResizeOptions,
            float[] mean,
            float[] std,
            int batchSize
    ) {
        Objects.requireNonNull(imageResizeOptions);
        this.imageResizeOptions = imageResizeOptions;

        final int channelCount = imageResizeOptions.getChannelConfiguration().getChannelCount();

        Objects.requireNonNull(mean);
        if (mean.length != channelCount) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MEAN_CHANNEL_COUNT, channelCount));
        }
        this.mean = new float[mean.length];
        System.arraycopy(mean, 0, this.mean, 0, mean.length);

        Objects.requireNonNull(std);
        if (std.length != channelCount) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_STD_CHANNEL_COUNT, channelCount));
        }
        this.std = new float[std.length];
        System.arraycopy(std, 0, this.std, 0, std.length);

        if (batchSize < 1) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.BATCH_SIZE_SHOULD_BE_POSITIVE);
        }
        this.batchSize = batchSize;
    }

    /**
     * Creates model input properties.
     *
     * <p>
     * With this constructor variant batching is disabled (i.e. batch size is set to 1).
     *
     * @param imageResizeOptions options, that control the way the input images for the models will
     *                           be converted, resized and padded for ML model input
     * @param mean               per-channel mean, used for normalization. Length of the array
     *                           should match the channel count in the image resize options
     * @param std                per-channel standard deviation, used for normalization. Length of
     *                           the array should match the channel count in the image resize
     *                           options
     */
    public OnnxInputProperties(ImageResizeOptions imageResizeOptions, float[] mean, float[] std) {
        this(imageResizeOptions, mean, std, 1);
    }

    /**
     * Creates model input properties.
     *
     * <p>
     * With this constructor variant no input normalization is done, only mapping to [0; 1].
     *
     * @param imageResizeOptions options, that control the way the input images for the models will
     *                           be converted, resized and padded for ML model input
     * @param batchSize          size of the batch used for the ML model. Should be a positive
     *                           number
     */
    public OnnxInputProperties(ImageResizeOptions imageResizeOptions, int batchSize) {
        this(imageResizeOptions, newNoopMean(imageResizeOptions), newNoopStd(imageResizeOptions), batchSize);
    }


    /**
     * Creates model input properties.
     *
     * <p>
     * With this constructor variant no input normalization is done, only mapping to [0; 1], and
     * batching is disabled (i.e. batch size is set to 1).
     *
     * @param imageResizeOptions options, that control the way the input images for the models will
     *                           be converted, resized and padded for ML model input
     */
    public OnnxInputProperties(ImageResizeOptions imageResizeOptions) {
        this(imageResizeOptions, 1);
    }

    /**
     * Returns image resize options for the input.
     *
     * @return image resize options for the input.
     */
    public ImageResizeOptions getImageResizeOptions() {
        return imageResizeOptions;
    }

    /**
     * Returns per-channel mean, used for normalization.
     *
     * @return per-channel mean, used for normalization
     */
    public float[] getMean() {
        float[] copy = new float[mean.length];
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
     * Returns gray channel mean, used for normalization.
     *
     * @return gray channel mean, used for normalization
     */
    public float getGrayMean() {
        return getMean(0);
    }

    /**
     * Returns red channel mean, used for normalization.
     *
     * @return red channel mean, used for normalization
     */
    public float getRedMean() {
        return getMean(imageResizeOptions.getChannelConfiguration().getRedChannelIndex());
    }

    /**
     * Returns green channel mean, used for normalization.
     *
     * @return green channel mean, used for normalization
     */
    public float getGreenMean() {
        return getMean(imageResizeOptions.getChannelConfiguration().getGreenChannelIndex());
    }

    /**
     * Returns blue channel mean, used for normalization.
     *
     * @return blue channel mean, used for normalization
     */
    public float getBlueMean() {
        return getMean(imageResizeOptions.getChannelConfiguration().getBlueChannelIndex());
    }

    /**
     * Returns per-channel standard deviation, used for normalization.
     *
     * @return per-channel standard deviation, used for normalization
     */
    public float[] getStd() {
        float[] copy = new float[std.length];
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
     * Returns gray channel standard deviation, used for normalization.
     *
     * @return gray channel standard deviation, used for normalization
     */
    public float getGrayStd() {
        return getStd(0);
    }

    /**
     * Returns red channel standard deviation, used for normalization.
     *
     * @return red channel standard deviation, used for normalization
     */
    public float getRedStd() {
        return getStd(imageResizeOptions.getChannelConfiguration().getRedChannelIndex());
    }

    /**
     * Returns green channel standard deviation, used for normalization.
     *
     * @return green channel standard deviation, used for normalization
     */
    public float getGreenStd() {
        return getStd(imageResizeOptions.getChannelConfiguration().getGreenChannelIndex());
    }

    /**
     * Returns blue channel standard deviation, used for normalization.
     *
     * @return blue channel standard deviation, used for normalization
     */
    public float getBlueStd() {
        return getStd(imageResizeOptions.getChannelConfiguration().getBlueChannelIndex());
    }

    /**
     * Returns target input shape. Minimum height and width are used.
     *
     * @return target input shape
     */
    public long[] getShape() {
        return new long[]{
                getBatchSize(),
                getChannelCount(),
                getHeight(),
                getWidth()
        };
    }

    /**
     * Returns target input dimension value.
     *
     * @param index index of the dimension
     *
     * @return target input dimension value
     */
    public int getShape(int index) {
        switch (index) {
            case 0:
                return getBatchSize();
            case 1:
                return getChannelCount();
            case 2:
                return getHeight();
            case 3:
                return getWidth();
            default:
                // Fallthrough
        }
        throw new ArrayIndexOutOfBoundsException(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.INDEX_OUT_OF_BOUNDS, index));
    }

    /**
     * Returns input batch size.
     *
     * @return input batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Returns input channel count.
     *
     * @return input channel count
     */
    public int getChannelCount() {
        return imageResizeOptions.getChannelConfiguration().getChannelCount();
    }

    /**
     * Returns input minimum height.
     *
     * @return input minimum height
     */
    public int getHeight() {
        return imageResizeOptions.getMinHeight();
    }

    /**
     * Returns input minimum width.
     *
     * @return input minimum width
     */
    public int getWidth() {
        return imageResizeOptions.getMinWidth();
    }

    /**
     * Returns whether padding should be symmetrical during input resizing.
     *
     * @return whether padding should be symmetrical during input resizing
     */
    public boolean useSymmetricPad() {
        return imageResizeOptions.getPaddingStrategy().usesSymmetricPadding();
    }

    /**
     * Returns the padding strategy for image inputs.
     *
     * @return the padding strategy for image inputs
     */
    public PaddingStrategy getPaddingStrategy() {
        return imageResizeOptions.getPaddingStrategy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                (Object) Arrays.hashCode(mean), Arrays.hashCode(std), imageResizeOptions, batchSize
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OnnxInputProperties that = (OnnxInputProperties) o;
        return batchSize == that.batchSize
                && Arrays.equals(mean, that.mean)
                && Arrays.equals(std, that.std)
                && Objects.equals(imageResizeOptions, that.imageResizeOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OnnxInputProperties{" +
                "mean=" + Arrays.toString(mean) +
                ", std=" + Arrays.toString(std) +
                ", imageResizeOptions=" + imageResizeOptions +
                ", batchSize=" + batchSize +
                '}';
    }

    private static float[] newNoopMean(ImageResizeOptions imageResizeOptions) {
        final int channelCount = imageResizeOptions.getChannelConfiguration().getChannelCount();
        final float[] mean = new float[channelCount];
        Arrays.fill(mean, 0.0F);
        return mean;
    }

    private static float[] newNoopStd(ImageResizeOptions imageResizeOptions) {
        final int channelCount = imageResizeOptions.getChannelConfiguration().getChannelCount();
        final float[] std = new float[channelCount];
        Arrays.fill(std, 1.0F);
        return std;
    }
}
