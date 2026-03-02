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
package com.itextpdf.pdfocr.onnx.util;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.onnx.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnx.ImageChannelConfiguration;
import com.itextpdf.pdfocr.onnx.ImageResizeOptions;
import com.itextpdf.pdfocr.onnx.OnnxInputProperties;
import com.itextpdf.pdfocr.onnx.PaddingStrategy;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.opencv.core.CvType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Additional algorithms for working with {@link BufferedImage}.
 */
public final class BufferedImageUtil {
    /**
     * Band index to retrieve a gray channel sample from a Raster.
     */
    private static final int BAND_GRAY = 0;
    /**
     * Band index to retrieve a red channel sample from a Raster. Band order
     * does not depend on the image type, so it is the same for all RGB
     * variants: RGB, ARGB, BGR, ABGR.
     */
    private static final int BAND_RED = 0;
    /**
     * Band index to retrieve a green channel sample from a Raster. Band order
     * does not depend on the image type, so it is the same for all RGB
     * variants: RGB, ARGB, BGR, ABGR.
     */
    private static final int BAND_GREEN = 1;
    /**
     * Band index to retrieve a blue channel sample from a Raster. Band order
     * does not depend on the image type, so it is the same for all RGB
     * variants: RGB, ARGB, BGR, ABGR.
     */
    private static final int BAND_BLUE = 2;

    private BufferedImageUtil() {
    }

    /**
     * Converts a collection of images to a batched ML model input in a BCHW format with 1 or 3
     * channels. This does aspect-preserving image resizing to fit the input shape.
     *
     * @param images collection of images to convert to model input
     * @param properties model input properties
     *
     * @return batched BCHW model input MD-array
     */
    public static FloatBufferMdArray toBchwInput(Collection<BufferedImage> images, OnnxInputProperties properties) {
        if (images.isEmpty()) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.SHOULD_BE_AT_LEAST_ONE_IMAGE);
        }
        if (images.size() > properties.getBatchSize()) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.TOO_MANY_IMAGES, images.size(), properties.getBatchSize()));
        }

        final ImageResizeOptions resizeOptions = properties.getImageResizeOptions();
        final Dimensions2D batchDimensions = calcOutputDimensions(images, resizeOptions);
        final long[] inputShape = new long[] {
                images.size(),
                resizeOptions.getChannelConfiguration().getChannelCount(),
                batchDimensions.getHeight(),
                batchDimensions.getWidth()
        };
        final FloatBuffer inputData = allocFloatBuffer(inputShape);
        for (final BufferedImage image : images) {
            final BufferedImage resizedImage = resize(
                    image,
                    batchDimensions.getWidth(),
                    batchDimensions.getHeight(),
                    resizeOptions.getPaddingStrategy(),
                    toImageType(resizeOptions.getChannelConfiguration())
            );
            putImageWithNormalization(inputData, resizedImage, properties);
        }
        inputData.rewind();
        return new FloatBufferMdArray(inputData, inputShape);
    }

    /**
     * Rotates image based on text orientation. If no rotation necessary, same image is returned.
     *
     * @param image image to rotate
     * @param orientation text orientation used to rotate the image
     *
     * @return new rotated image, or same image, if no rotation is required
     */
    public static BufferedImage rotate(BufferedImage image, TextOrientation orientation) {
        if (orientation == TextOrientation.HORIZONTAL) {
            return image;
        }

        final int oldW = image.getWidth();
        final int oldH = image.getHeight();
        final int newW;
        final int newH;
        final double angle;
        if (orientation == TextOrientation.HORIZONTAL_ROTATED_180) {
            newW = oldW;
            newH = oldH;
            angle = Math.PI;
        } else {
            newW = oldH;
            newH = oldW;
            if (orientation == TextOrientation.HORIZONTAL_ROTATED_90) {
                angle = 0.5 * Math.PI;
            } else {
                angle = 1.5 * Math.PI;
            }
        }
        final BufferedImage rotated = new BufferedImage(newW, newH, image.getType());
        final Graphics2D graphics = rotated.createGraphics();
        graphics.translate((newW - oldW) / 2.0, (newH - oldH) / 2.0);
        graphics.rotate(angle, image.getWidth() / 2.0, image.getHeight() / 2.0);
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return rotated;
    }

    /**
     * Extracts sub-images from an image, based on provided rotated 4-point boxes. Sub-images are
     * transformed to fit the whole image without (in our use cases it is just rotation).
     *
     * @param image original image to be used for extraction
     * @param boxes list of 4-point boxes. Points should be in the following order: BL, TL, TR, BR
     *
     * @return list of extracted image boxes
     */
    public static List<BufferedImage> extractBoxes(BufferedImage image, Collection<Point[]> boxes) {
        final List<BufferedImage> boxesImages = new ArrayList<>(boxes.size());
        try (final Mat imageMat = BufferedImageUtil.toRgbMat(image)) {
            for (final Point[] box : boxes) {
                final float boxWidth = (float) box[1].distance(box[2]);
                final float boxHeight = (float) box[1].distance(box[0]);
                try (final Mat transformationMat = calculateBoxTransformationMat(box, boxWidth, boxHeight);
                     final Mat boxImageMat = new Mat((int) boxHeight, (int) boxWidth, CvType.CV_8UC3);
                     final Size size = new Size((int) boxWidth, (int) boxHeight)) {
                    opencv_imgproc.warpAffine(imageMat, boxImageMat, transformationMat, size);
                    boxesImages.add(BufferedImageUtil.fromRgbMat(boxImageMat));
                }
            }
        }
        return boxesImages;
    }

    /**
     * Based on the provided ImageResizeOptions, calculates the dimensions to
     * which a batch of images should be scaled and padded.
     *
     * @param images        batch of images to scale/pad
     * @param resizeOptions resize options to take into consideration for
     *                      scaling/padding
     *
     * @return the calculated dimensions
     */
    public static Dimensions2D calcOutputDimensions(
            Collection<BufferedImage> images,
            ImageResizeOptions resizeOptions
    ) {
        /*
         * Calculating target dimensions for each image, We need to know them
         * all before creating a batch buffers, as width and height should be the same
         * for each image in the batch. So we need to know the maximum sizes
         * before we create the buffers. And we don't really want to bloat
         * peak memory usage by creating an array of resized images...
         */
        final ArrayList<Dimensions2D> targetDimensions = new ArrayList<>(images.size());
        for (final BufferedImage image : images) {
            targetDimensions.add(calcOutputDimensions(image, resizeOptions));
        }
        final int maxWidth = Collections.max(
                targetDimensions, Comparator.comparingInt(Dimensions2D::getWidth)
        ).getWidth();
        final int maxHeight = Collections.max(
                targetDimensions, Comparator.comparingInt(Dimensions2D::getHeight)
        ).getHeight();
        return new Dimensions2D(maxWidth, maxHeight);
    }

    /**
     * Based on the provided ImageResizeOptions, calculates the dimensions of
     * the output image, to where there original image should be scaled and
     * placed with padding. The returned dimensions will always satisfy the
     * minimum constraints. Maximum constraints will also be satisfied, if the
     * dimension multiple is 1, but if it is greater, it may round up to be
     * higher than maximum.
     *
     * @param image         image, that will be scaled/padded
     * @param resizeOptions resize options to take into consideration for
     *                      scaling/padding
     *
     * @return the calculated dimensions
     */
    public static Dimensions2D calcOutputDimensions(BufferedImage image, ImageResizeOptions resizeOptions) {
        int targetWidth = image.getWidth();
        int targetHeight = image.getHeight();

        /*
         * If the image is smaller in one of the dimensions, we will try to
         * resize it in a way that both dimensions at least match "min". In the
         * case, when one of the dimensions gets too big and goes over "max",
         * then we will shrink it to fit max again in the next block
         */
        final double widthToMinMul = (double) resizeOptions.getMinWidth() / targetWidth;
        final double heightToMinMul = (double) resizeOptions.getMinHeight() / targetHeight;
        if (widthToMinMul > 1. || heightToMinMul > 1.) {
            if (widthToMinMul >= heightToMinMul) {
                targetWidth = resizeOptions.getMinWidth();
                targetHeight = Math.max(resizeOptions.getMinHeight(), (int) Math.round(widthToMinMul * targetHeight));
            } else {
                targetWidth = Math.max(resizeOptions.getMinWidth(), (int) Math.round(heightToMinMul * targetWidth));
                targetHeight = resizeOptions.getMinHeight();
            }
        }

        /*
         * If the image is bigger in one of the dimensions, we will shrink it
         * in a way to satisfy the "max" constraints. In case one of the
         * dimensions will fall below its "min" constraint afterward, we will
         * pad it back.
         */
        final double widthToMaxMul = (double) resizeOptions.getMaxWidth() / targetWidth;
        final double heightToMaxMul = (double) resizeOptions.getMaxHeight() / targetHeight;
        if (widthToMaxMul < 1. || heightToMaxMul < 1.) {
            if (widthToMaxMul <= heightToMaxMul) {
                targetWidth = resizeOptions.getMaxWidth();
                targetHeight = (int) MathUtil.clamp(
                        widthToMaxMul * targetHeight, resizeOptions.getMinHeight(), resizeOptions.getMaxHeight()
                );
            } else {
                targetWidth = (int) MathUtil.clamp(
                        heightToMaxMul * targetWidth, resizeOptions.getMinWidth(), resizeOptions.getMaxWidth()
                );
                targetHeight = resizeOptions.getMaxHeight();
            }
        }

        // Rounding-up to multiple here
        final int widthMultiple = resizeOptions.getWidthMultiple();
        final int heightMultiple = resizeOptions.getHeightMultiple();
        return new Dimensions2D(
                (targetWidth + (widthMultiple - 1)) / widthMultiple * widthMultiple,
                (targetHeight + (heightMultiple - 1)) / heightMultiple * heightMultiple
        );
    }

    /**
     * Truncates the input image, so that neither width/height, nor
     * height/width ratios exceed the limit.
     *
     * <p>
     * If width/height ratio exceeds the limit, the image will be truncated
     * on left and right equally.
     *
     * <p>
     * If height/width ratio exceeds the limit, the image will be truncated
     * on top and bottom equally.
     *
     * @param image      input image to truncate
     * @param ratioLimit target ratio limit
     *
     * @return the truncated image
     */
    public static BufferedImage truncateToRatio(BufferedImage image, double ratioLimit) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        // If w/h ratio is too big, truncating by width
        final double imageRatio = (double) width / height;
        if (imageRatio > ratioLimit) {
            final int newWidth = Math.max(1, (int) (ratioLimit * height));
            final int newX = (width - newWidth) / 2;
            return image.getSubimage(newX, 0, newWidth, height);
        }

        // If h/w ratio is too big, truncating by height
        final double imageRatioInv = 1. / imageRatio;
        if (imageRatioInv > ratioLimit) {
            final int newHeight = Math.max(1, (int) (ratioLimit * width));
            final int newY = (height - newHeight) / 2;
            return image.getSubimage(0, newY, width, newHeight);
        }

        // Otherwise leaving as-is
        return image;
    }

    /**
     * Creates a new image with an aspect ratio preserving resize.
     *
     * @param image image to resize
     * @param width target width
     * @param height target height
     * @param paddingStrategy padding strategy to use
     * @param targetType type of the created image
     *
     * @return new resized image
     */
    static BufferedImage resize(BufferedImage image, int width, int height,
                                PaddingStrategy paddingStrategy, int targetType) {
        // It is pretty unlikely, that the image is already the correct size, so no need for an exception
        final BufferedImage result = new BufferedImage(width, height, targetType);
        final Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();
        final double widthRatio = (double) width / sourceWidth;
        final double heightRatio = (double) height / sourceHeight;
        if (heightRatio > widthRatio) {
            final int scaledHeight = Math.min(height, (int) Math.round(sourceHeight * widthRatio));
            drawResizedImage(graphics, width, height, image, width, scaledHeight, paddingStrategy);
        } else {
            final int scaledWidth = Math.min(width, (int) Math.round(sourceWidth * heightRatio));
            drawResizedImage(graphics, width, height, image, scaledWidth, height, paddingStrategy);
        }
        graphics.dispose();
        return result;
    }

    private static void putImageWithNormalization(
            FloatBuffer outputBuffer,
            BufferedImage image,
            OnnxInputProperties props
    ) {
        switch (props.getImageResizeOptions().getChannelConfiguration()) {
            case GRAYSCALE:
                putGrayscaleImageWithNormalization(outputBuffer, image, props);
                return;
            case RGB:
                putRgbImageWithNormalization(outputBuffer, image, props);
                return;
            case BGR:
                putBgrImageWithNormalization(outputBuffer, image, props);
                return;
        }
        throw new IllegalStateException(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION);
    }

    private static void putGrayscaleImageWithNormalization(
            FloatBuffer outputBuffer,
            BufferedImage image,
            OnnxInputProperties props
    ) {
        assert image.getType() == BufferedImage.TYPE_BYTE_GRAY;

        putImageBandWithNormalization(outputBuffer, image, BAND_GRAY, props.getGrayMean(), props.getGrayStd());
    }

    private static void putRgbImageWithNormalization(
            FloatBuffer outputBuffer,
            BufferedImage image,
            OnnxInputProperties props
    ) {
        assert image.getType() == BufferedImage.TYPE_3BYTE_BGR;

        putImageBandWithNormalization(outputBuffer, image, BAND_RED, props.getRedMean(), props.getRedStd());
        putImageBandWithNormalization(outputBuffer, image, BAND_GREEN, props.getGreenMean(), props.getGreenStd());
        putImageBandWithNormalization(outputBuffer, image, BAND_BLUE, props.getBlueMean(), props.getBlueStd());
    }

    private static void putBgrImageWithNormalization(
            FloatBuffer outputBuffer,
            BufferedImage image,
            OnnxInputProperties props
    ) {
        assert image.getType() == BufferedImage.TYPE_3BYTE_BGR;

        putImageBandWithNormalization(outputBuffer, image, BAND_BLUE, props.getBlueMean(), props.getBlueStd());
        putImageBandWithNormalization(outputBuffer, image, BAND_GREEN, props.getGreenMean(), props.getGreenStd());
        putImageBandWithNormalization(outputBuffer, image, BAND_RED, props.getRedMean(), props.getRedStd());
    }

    private static void putImageBandWithNormalization(
            FloatBuffer outputBuffer,
            BufferedImage image,
            int band,
            double mean,
            double std
    ) {
        final Raster raster = image.getRaster();
        for (int y = 0; y < raster.getHeight(); ++y) {
            for (int x = 0; x < raster.getWidth(); ++x) {
                final double v = raster.getSample(x, y, band) / 255.0;
                outputBuffer.put((float) ((v - mean) / std));
            }
        }
    }

    /**
     * Converts an image to an RGB Mat for use in OpenCV.
     *
     * @param image image to convert
     *
     * @return RGB 8UC3 OpenCV Mat with the image
     */
    private static Mat toRgbMat(BufferedImage image) {
        final Mat resultMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        try (final UByteIndexer resultMatIndexer = resultMat.createIndexer()) {
            for (int y = 0; y < image.getHeight(); ++y) {
                for (int x = 0; x < image.getWidth(); ++x) {
                    final int rgb = image.getRGB(x, y);
                    final int r = (rgb >> 16) & 0xFF;
                    final int g = (rgb >> 8) & 0xFF;
                    final int b = rgb & 0xFF;
                    resultMatIndexer.put(y, (long) x, r, g, b);
                }
            }
        }
        return resultMat;
    }

    /**
     * Converts an RGB 8UC3 OpenCV Mat to a buffered image.
     *
     * @param rgb RGB 8UC3 OpenCV Mat to convert
     *
     * @return buffered image based on Mat
     */
    private static BufferedImage fromRgbMat(Mat rgb) {
        if (rgb.type() != CvType.CV_8UC3) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MAT_TYPE, CvType.typeToString(rgb.type())));
        }

        final BufferedImage image = new BufferedImage(rgb.cols(), rgb.rows(), BufferedImage.TYPE_3BYTE_BGR);
        final int[] rgbBuffer = new int[3];
        try (final UByteIndexer rgbIndexer = rgb.createIndexer()) {
            for (int y = 0; y < image.getHeight(); ++y) {
                for (int x = 0; x < image.getWidth(); ++x) {
                    rgbIndexer.get(y, x, rgbBuffer);
                    final int rgbValue = 0xFF000000 | (rgbBuffer[0] << 16) | (rgbBuffer[1] << 8) | rgbBuffer[2];
                    image.setRGB(x, y, rgbValue);
                }
            }
        }
        return image;
    }

    private static void drawResizedImage(Graphics2D output, int outputWidth, int outputHeight, BufferedImage image,
                                         int targetWidth, int targetHeight, PaddingStrategy paddingStrategy) {
        // Figuring where to put the image
        int xPos = 0;
        int yPos = 0;
        if (paddingStrategy.usesSymmetricPadding()) {
            xPos += (outputWidth - targetWidth) / 2;
            yPos += (outputHeight - targetHeight) / 2;
        } else if (!paddingStrategy.usesBottomRightPadding()) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_PADDING_STRATEGY, paddingStrategy
            ));
        }
        // Drawing all the paddings first
        if (paddingStrategy.usesSolidColor()) {
            // Might as well just fill the whole output, images are small
            // anyway, so they might just be in cache whole
            output.setColor(paddingStrategy.getSolidColor());
            output.fillRect(0, 0, outputWidth, outputHeight);
        } else {
            final int sourceWidth = image.getWidth();
            final int sourceHeight = image.getHeight();
            // Top padding
            if (yPos > 0) {
                output.drawImage(
                        image,
                        0, 0, outputWidth, yPos,
                        0, 0, sourceWidth, 1,
                        Color.WHITE, null
                );
            }
            // Right padding
            final int rightPaddingX = xPos + targetWidth;
            if (rightPaddingX < outputWidth) {
                output.drawImage(
                        image,
                        rightPaddingX, 0, outputWidth, outputHeight,
                        sourceWidth - 1, 0, sourceWidth, sourceHeight,
                        Color.WHITE, null
                );
            }
            // Bottom padding
            final int bottomPaddingY = yPos + targetHeight;
            if (bottomPaddingY < outputHeight) {
                output.drawImage(
                        image,
                        0, bottomPaddingY, outputWidth, outputHeight,
                        0, sourceHeight - 1, sourceWidth, sourceHeight,
                        Color.WHITE, null
                );
            }
            // Left padding
            if (xPos > 0) {
                output.drawImage(
                        image,
                        0, 0, xPos, outputHeight,
                        0, 0, 1, sourceHeight,
                        Color.WHITE, null
                );
            }
        }
        // Drawing the image itself
        output.drawImage(image, xPos, yPos, targetWidth, targetHeight, Color.WHITE, null);
    }

    private static Mat calculateBoxTransformationMat(Point[] box, float boxWidth, float boxHeight) {
        try (final Mat srcPoints = new Mat(3, 2, CvType.CV_32F);
             final Mat dstPoints = new Mat(3, 2, CvType.CV_32F);
             final FloatIndexer srcPointsIndexer = srcPoints.createIndexer();
             final FloatIndexer dstPointsIndexer = dstPoints.createIndexer()) {
            for (int i = 0; i < 3; ++i) {
                srcPointsIndexer.put(i, (float) box[i].getX(), (float) box[i].getY());
            }
            dstPointsIndexer.put(0, 0F, boxHeight - 1);
            dstPointsIndexer.put(1, 0F, 0F);
            dstPointsIndexer.put(2, boxWidth - 1, 0F);
            return opencv_imgproc.getAffineTransform(srcPoints, dstPoints);
        }
    }

    private static int toImageType(ImageChannelConfiguration channelConfiguration) {
        switch (channelConfiguration) {
            case GRAYSCALE:
                return BufferedImage.TYPE_BYTE_GRAY;
            case RGB:
            case BGR:
                return BufferedImage.TYPE_3BYTE_BGR;
        }
        throw new IllegalStateException(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION);
    }

    /**
     * Returns the byte capacity required for a float32 buffer of the specified shape.
     *
     * @param shape shape of the MD-array
     *
     * @return the byte capacity required for a float32 buffer of the specified shape
     */
    private static int calculateBufferCapacity(long[] shape) {
        int capacity = Float.BYTES;
        for (final long dim : shape) {
            capacity *= (int) dim;
        }
        return capacity;
    }

    /**
     * Allocates a direct float buffer to accommodate the provided shape.
     *
     * @param shape shape of the MD-array
     *
     * @return the allocated direct float buffer
     */
    private static FloatBuffer allocFloatBuffer(long[] shape) {
        /*
         * It is important to do it via ByteBuffer with allocateDirect. If the
         * buffer is non-direct, it will allocate a direct buffer within the
         * ONNX runtime and copy the buffer there instead. So we will waste
         * twice the memory for no reason.
         *
         * For some reason there doesn't seem to be a way to allocate a direct
         * buffer via FloatBuffer itself...
         */
        return ByteBuffer
                .allocateDirect(calculateBufferCapacity(shape))
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }
}
