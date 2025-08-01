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
package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;

import java.util.Objects;

/**
 * Options, that describe the way an image will be resized before being
 * converted to a tensor for an ML model input.
 *
 * <p>
 * At the moment only ratio-preserving resizing is supported.
 */
public class ImageResizeOptions {
    /**
     * Specifies the image channel configuration, that will be used when
     * passing the image to the ML model.
     *
     * <p>
     * While this is not directly relates to image resizing and padding, it is
     * important for us to know to which color model to use for the final
     * image.
     */
    private final ImageChannelConfiguration channelConfiguration;
    /**
     * Minimum width the image should be after resizing. Padding might be used
     * to get to this value.
     *
     * <p>
     * Should be a positive value.
     */
    private final int minWidth;
    /**
     * Minimum height the image should be after resizing. Padding might be used
     * to get to this value.
     *
     * <p>
     * Should be a positive value.
     */
    private final int minHeight;
    /**
     * Maximum width the image should be after resizing.
     *
     * <p>
     * Should be a positive value not less than {@code minWidth}. This value
     * should also be a multiple of {@code widthMultiple}.
     */
    private final int maxWidth;
    /**
     * Maximum height the image should be after resizing.
     *
     * <p>
     * Should be a positive value not less than {@code minHeight}. This value
     * should also be a multiple of {@code heightMultiple}.
     */
    private final int maxHeight;
    /**
     * After resizing, the width of the image should be a multiple of this
     * value.
     *
     * <p>
     * Very likely, that you don't need to worry about this parameter, and
     * should leave this at the default value (1). But some models (ex.
     * PaddleOCR detection ones) are picky about image size in this way.
     *
     * <p>
     * Should be a positive value.
     */
    private final int widthMultiple;
    /**
     * After resizing, the height of the image should be a multiple of this
     * value.
     *
     * <p>
     * Very likely, that you don't need to worry about this parameter, and
     * should leave this at the default value (1). But some models (ex.
     * PaddleOCR detection ones) are picky about image size in this way.
     *
     * <p>
     * Should be a positive value.
     */
    private final int heightMultiple;
    /**
     * Padding strategy to be used, when resizing an image to the target
     * size.
     *
     * <p>
     * We only do ratio-preserving resizing, so padding might be required to
     * get the image to the target size.
     */
    private final PaddingStrategy paddingStrategy;

    /**
     * Creates image resize options.
     *
     * @param channelConfiguration channel configuration, that will be used, when passing the image
     *                             to the ML model
     * @param minWidth             minimum width the image should be after resizing. Should be a
     *                             positive value
     * @param minHeight            minimum height the image should be after resizing. Should be a
     *                             positive value
     * @param maxWidth             maximum width the image should be after resizing. Should not be
     *                             less than {@code minWidth}. Should be a multiple of
     *                             {@code widthMultiple}
     * @param maxHeight            maximum height the image should be after resizing. Should not be
     *                             less than {@code minHeight}. Should be a multiple of
     *                             {@code heightMultiple}
     * @param widthMultiple        after resizing, the width of the image should be a multiple of
     *                             this value
     * @param heightMultiple       after resizing, the height of the image should be a multiple of
     *                             this value
     * @param paddingStrategy      padding strategy to be used
     */
    public ImageResizeOptions(
            ImageChannelConfiguration channelConfiguration,
            int minWidth, int minHeight,
            int maxWidth, int maxHeight,
            int widthMultiple, int heightMultiple,
            PaddingStrategy paddingStrategy
    ) {
        Objects.requireNonNull(channelConfiguration);
        this.channelConfiguration = channelConfiguration;

        if (minWidth < 1) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.MIN_WIDTH_SHOULD_BE_POSITIVE, minWidth
            ));
        }
        this.minWidth = minWidth;

        if (minHeight < 1) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.MIN_HEIGHT_SHOULD_BE_POSITIVE, minHeight
            ));
        }
        this.minHeight = minHeight;

        if (widthMultiple < 1) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.WIDTH_MULTIPLE_SHOULD_BE_POSITIVE, widthMultiple
            ));
        }
        this.widthMultiple = widthMultiple;

        if (heightMultiple < 1) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.HEIGHT_MULTIPLE_SHOULD_BE_POSITIVE, heightMultiple
            ));
        }
        this.heightMultiple = heightMultiple;

        if (maxWidth < minWidth) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.MAX_WIDTH_SHOULD_NOT_BE_LESS_THAN_MIN, maxWidth
            ));
        }
        if (maxWidth % widthMultiple != 0) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.MAX_WIDTH_SHOULD_BE_A_MULTIPLE, widthMultiple, maxWidth
            ));
        }
        this.maxWidth = maxWidth;

        if (maxHeight < minHeight) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.MAX_HEIGHT_SHOULD_NOT_BE_LESS_THAN_MIN, maxHeight
            ));
        }
        if (maxHeight % heightMultiple != 0) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.MAX_HEIGHT_SHOULD_BE_A_MULTIPLE, heightMultiple, maxHeight
            ));
        }
        this.maxHeight = maxHeight;

        Objects.requireNonNull(paddingStrategy);
        this.paddingStrategy = paddingStrategy;
    }

    /**
     * Creates image resize options.
     *
     * <p>
     * With this constructor variant output image dimensions are not bumped up to be a multiple of
     * some integer value.
     *
     * @param channelConfiguration channel configuration, that will be used, when passing the image
     *                             to the ML model
     * @param minWidth             minimum width the image should be after resizing. Should be a
     *                             positive value
     * @param minHeight            minimum height the image should be after resizing. Should be a
     *                             positive value
     * @param maxWidth             maximum width the image should be after resizing. Should not be
     *                             less than {@code minWidth}
     * @param maxHeight            maximum height the image should be after resizing. Should not be
     *                             less than {@code minHeight}
     * @param paddingStrategy      padding strategy to be used
     */
    public ImageResizeOptions(
            ImageChannelConfiguration channelConfiguration,
            int minWidth, int minHeight,
            int maxWidth, int maxHeight,
            PaddingStrategy paddingStrategy
    ) {
        this(channelConfiguration, minWidth, minHeight, maxWidth, maxHeight, 1, 1, paddingStrategy);
    }

    /**
     * Creates image resize options.
     *
     * <p>
     * With this constructor variant output image dimensions are not bumped up to be a multiple of
     * some integer value and a default black padding is added at the bottom-right of the image.
     *
     * @param channelConfiguration channel configuration, that will be used, when passing the image
     *                             to the ML model
     * @param minWidth             minimum width the image should be after resizing. Should be a
     *                             positive value
     * @param minHeight            minimum height the image should be after resizing. Should be a
     *                             positive value
     * @param maxWidth             maximum width the image should be after resizing. Should not be
     *                             less than {@code minWidth}
     * @param maxHeight            maximum height the image should be after resizing. Should not be
     *                             less than {@code minHeight}
     */
    public ImageResizeOptions(
            ImageChannelConfiguration channelConfiguration,
            int minWidth, int minHeight,
            int maxWidth, int maxHeight
    ) {
        this(channelConfiguration, minWidth, minHeight, maxWidth, maxHeight, PaddingStrategy.BOTTOM_RIGHT_BLACK);
    }

    /**
     * Creates image resize options.
     *
     * <p>
     * With this constructor variant output image dimensions are fixed to the provided values.
     *
     * @param channelConfiguration channel configuration, that will be used, when passing the image
     *                             to the ML model
     * @param targetWidth          width the image should be after resizing. Should be a positive
     *                             value
     * @param targetHeight         height the image should be after resizing. Should be a positive
     *                             value
     * @param paddingStrategy      padding strategy to be used
     */
    public ImageResizeOptions(
            ImageChannelConfiguration channelConfiguration,
            int targetWidth, int targetHeight,
            PaddingStrategy paddingStrategy
    ) {
        this(channelConfiguration, targetWidth, targetHeight, targetWidth, targetHeight, paddingStrategy);
    }

    /**
     * Creates image resize options.
     *
     * <p>
     * With this constructor variant output image dimensions are fixed to the provided values and
     * a default black padding is added at the bottom-right of the image.
     *
     * @param channelConfiguration channel configuration, that will be used, when passing the image
     *                             to the ML model
     * @param targetWidth          width the image should be after resizing. Should be a positive
     *                             value
     * @param targetHeight         height the image should be after resizing. Should be a positive
     *                             value
     */
    public ImageResizeOptions(
            ImageChannelConfiguration channelConfiguration,
            int targetWidth, int targetHeight
    ) {
        this(channelConfiguration, targetWidth, targetHeight, PaddingStrategy.BOTTOM_RIGHT_BLACK);
    }

    /**
     * Returns the image channel configuration, that will be used when passing
     * the image to the ML model.
     *
     * @return the image channel configuration
     */
    public ImageChannelConfiguration getChannelConfiguration() {
        return channelConfiguration;
    }

    /**
     * Returns the minimum width the image should be after resizing.
     *
     * @return the minimum width the image should be after resizing
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * Returns the minimum height the image should be after resizing.
     *
     * @return the minimum height the image should be after resizing
     */
    public int getMinHeight() {
        return minHeight;
    }

    /**
     * Returns the maximum width the image should be after resizing.
     *
     * @return the maximum width the image should be after resizing
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Returns the maximum height the image should be after resizing.
     *
     * @return the maximum height the image should be after resizing
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Returns the width multiple.
     *
     * <p>
     * After resizing, the width of the image should be a multiple of this
     * value.
     *
     * @return the width multiple
     */
    public int getWidthMultiple() {
        return widthMultiple;
    }

    /**
     * Returns the height multiple.
     *
     * <p>
     * After resizing, the height of the image should be a multiple of this
     * value.
     *
     * @return the height multiple
     */
    public int getHeightMultiple() {
        return heightMultiple;
    }

    /**
     * Returns the padding strategy.
     *
     * @return the padding strategy
     */
    public PaddingStrategy getPaddingStrategy() {
        return paddingStrategy;
    }

    /**
     * Returns whether the target size is fixed.
     *
     * @return whether the target size is fixed.
     */
    public boolean isFixedSize() {
        return (minWidth == maxWidth) && (minHeight == maxHeight);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImageResizeOptions that = (ImageResizeOptions) o;
        return minWidth == that.minWidth
                && minHeight == that.minHeight
                && maxWidth == that.maxWidth
                && maxHeight == that.maxHeight
                && widthMultiple == that.widthMultiple
                && heightMultiple == that.heightMultiple
                && channelConfiguration == that.channelConfiguration
                && paddingStrategy == that.paddingStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                (Object) channelConfiguration, minWidth, minHeight, maxWidth, maxHeight, widthMultiple,
                heightMultiple, paddingStrategy
        );
    }

    @Override
    public String toString() {
        return "ImageResizeOptions{" +
                "channelConfiguration=" + channelConfiguration +
                ", minWidth=" + minWidth +
                ", minHeight=" + minHeight +
                ", maxWidth=" + maxWidth +
                ", maxHeight=" + maxHeight +
                ", widthMultiple=" + widthMultiple +
                ", heightMultiple=" + heightMultiple +
                ", paddingStrategy=" + paddingStrategy +
                '}';
    }
}
