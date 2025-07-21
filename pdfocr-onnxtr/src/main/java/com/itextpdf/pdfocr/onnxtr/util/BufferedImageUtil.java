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
package com.itextpdf.pdfocr.onnxtr.util;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.opencv.core.CvType;

/**
 * Additional algorithms for working with {@link BufferedImage}.
 */
public final class BufferedImageUtil {
    private BufferedImageUtil() {
    }

    /**
     * Converts a collection of images to a batched ML model input in a BCHW format with 3 channels.
     * This does aspect-preserving image resizing to fit the input shape.
     *
     * @param images     Collection of images to convert to model input.
     * @param properties Model input properties.
     *
     * @return Batched BCHW model input MD-array.
     */
    public static FloatBufferMdArray toBchwInput(
            Collection<BufferedImage> images,
            OnnxInputProperties properties
    ) {
        // Currently properties guarantee RGB, this is just in case this changes later
        if (properties.getChannelCount() != 3) {
            throw new IllegalArgumentException("toBchwInput only support RGB images");
        }

        if (images.size() > properties.getBatchSize()) {
            throw new IllegalArgumentException(
                    "Too many images (" + images.size() + ") "
                            + "for the provided batch size (" + properties.getBatchSize() + ")"
            );
        }
        final long[] inputShape = new long[] {
                images.size(),
                properties.getChannelCount(),
                properties.getHeight(),
                properties.getWidth()
        };
        /*
         * It is important to do it via ByteBuffer with allocateDirect. If the
         * buffer is non-direct, it will allocate a direct buffer within the
         * ONNX runtime and copy the buffer there instead. So we will waste
         * twice the memory for no reason.
         *
         * For some reason there doesn't seem to be a way to allocate a direct
         * buffer via FloatBuffer itself...
         */
        final FloatBuffer inputData = ByteBuffer
                .allocateDirect(calculateBufferCapacity(inputShape))
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        for (final BufferedImage image : images) {
            final BufferedImage resizedImage = resize(
                    image,
                    properties.getWidth(),
                    properties.getHeight(),
                    properties.useSymmetricPad()
            );
            assert resizedImage.getType() == BufferedImage.TYPE_3BYTE_BGR;
            // Doing normalization at the same time as we fill the buffer
            final WritableRaster raster = resizedImage.getRaster();
            for (int y = 0; y < resizedImage.getHeight(); ++y) {
                for (int x = 0; x < resizedImage.getWidth(); ++x) {
                    final float r = raster.getSample(x, y, 2) / 255F;
                    inputData.put((r - properties.getRedMean()) / properties.getRedStd());
                }
            }
            for (int y = 0; y < resizedImage.getHeight(); ++y) {
                for (int x = 0; x < resizedImage.getWidth(); ++x) {
                    final float g = raster.getSample(x, y, 1) / 255F;
                    inputData.put((g - properties.getGreenMean()) / properties.getGreenStd());
                }
            }
            for (int y = 0; y < resizedImage.getHeight(); ++y) {
                for (int x = 0; x < resizedImage.getWidth(); ++x) {
                    final float b = raster.getSample(x, y, 0) / 255F;
                    inputData.put((b - properties.getBlueMean()) / properties.getBlueStd());
                }
            }
        }
        inputData.rewind();
        return new FloatBufferMdArray(inputData, inputShape);
    }

    /**
     * Converts an image to an RGB Mat for use in OpenCV.
     *
     * @param image Image to convert.
     *
     * @return RGB 8UC3 OpenCV Mat with the image.
     */
    public static Mat toRgbMat(BufferedImage image) {
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
     * @param rgb RGB 8UC3 OpenCV Mat to convert.
     *
     * @return Buffered image based on Mat.
     */
    public static BufferedImage fromRgbMat(Mat rgb) {
        if (rgb.type() != CvType.CV_8UC3) {
            throw new IllegalArgumentException("Unexpected Mat type");
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

    /**
     * Rotates image based on text orientation. If no rotation necessary, same image is returned.
     *
     * @param image       Image to rotate.
     * @param orientation Text orientation used to rotate the image.
     *
     * @return New rotated image, or same image, if no rotation is required.
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
     * Creates a new image with an aspect ratio preserving resize. New blank pixel will have black
     * color.
     *
     * @param image        Image to resize.
     * @param width        Target width.
     * @param height       Target height.
     * @param symmetricPad Whether padding should be symmetric or should it be bottom-right.
     *
     * @return New resized image.
     */
    public static BufferedImage resize(BufferedImage image, int width, int height, boolean symmetricPad) {
        // It is pretty unlikely, that the image is already the correct size, so no need for an exception
        final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics = result.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();
        final double widthRatio = (double) width / sourceWidth;
        final double heightRatio = (double) height / sourceHeight;
        if (heightRatio > widthRatio) {
            final int scaledHeight = (int) Math.round(sourceHeight * widthRatio);
            final int yPos;
            if (symmetricPad) {
                yPos = (height - scaledHeight) / 2;
                graphics.fillRect(0, 0, width, yPos);
            } else {
                yPos = 0;
            }
            graphics.fillRect(0, yPos + scaledHeight, width, height - scaledHeight - yPos);
            graphics.drawImage(image, 0, yPos, width, scaledHeight, Color.WHITE, null);
        } else {
            final int scaledWidth = (int) Math.round(sourceWidth * heightRatio);
            final int xPos;
            if (symmetricPad) {
                xPos = (width - scaledWidth) / 2;
                graphics.fillRect(0, 0, xPos, height);
            } else {
                xPos = 0;
            }
            graphics.fillRect(xPos + scaledWidth, 0, width - scaledWidth - xPos, height);
            graphics.drawImage(image, xPos, 0, scaledWidth, height, Color.WHITE, null);
        }
        graphics.dispose();
        return result;
    }

    /**
     * Extracts sub-images from an image, based on provided rotated 4-point boxes. Sub-images are
     * transformed to fit the whole image without (in our use cases it is just rotation).
     *
     * @param image Original image to be used for extraction.
     * @param boxes List of 4-point boxes. Points should be in the following order: BL, TL, TR, BR.
     *
     * @return List of extracted image boxes.
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

    /**
     * Returns the byte capacity required for a float32 buffer of the specified shape.
     *
     * @param shape Shape of the MD-array.
     *
     * @return The byte capacity required for a float32 buffer of the specified shape.
     */
    private static int calculateBufferCapacity(long[] shape) {
        int capacity = Float.BYTES;
        for (final long dim : shape) {
            capacity *= (int) dim;
        }
        return capacity;
    }
}
