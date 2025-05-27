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
package com.itextpdf.pdfocr.onnxtr.detection;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.onnxtr.AbstractOnnxPredictor;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A text detection predictor implementation, which is using ONNX Runtime and
 * its ML models to find, where text is located on an image.
 */
public class OnnxDetectionPredictor extends AbstractOnnxPredictor<BufferedImage, List<Point[]>>
        implements IDetectionPredictor {
    /**
     * Configuration properties of the predictor.
     */
    private final OnnxDetectionPredictorProperties properties;

    /**
     * Creates a text detection predictor with the specified properties.
     *
     * @param properties properties of the predictor
     */
    public OnnxDetectionPredictor(OnnxDetectionPredictorProperties properties) {
        super(properties.getModelPath(), properties.getInputProperties(), getExpectedOutputShape(properties));
        this.properties = properties;
    }

    /**
     * Creates a new text detection predictor using an existing pre-trained DBNet model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet50-69ba0015.onnx">
     *             db_resnet50
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet50_static_8_bit-09a6104f.onnx">
     *             db_resnet50 (8-bit quantized)
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet34-b4873198.onnx">
     *             db_resnet34
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet34_static_8_bit-027e2c7f.onnx">
     *             db_resnet34 (8-bit quantized)
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large-4987e7bd.onnx">
     *             db_mobilenet_v3_large
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large_static_8_bit-535a6f25.onnx">
     *             db_mobilenet_v3_large (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor with the DBNet model loaded
     */
    public static OnnxDetectionPredictor dbNet(String modelPath) {
        return new OnnxDetectionPredictor(OnnxDetectionPredictorProperties.dbNet(modelPath));
    }

    /**
     * Creates a new text detection predictor using an existing pre-trained FAST model, stored on disk.
     * This is the default text detection model in OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_base-1b89ebf9.onnx">
     *             fast_base
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_small-10428b70.onnx">
     *             fast_small
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_tiny-28867779.onnx">
     *             fast_tiny
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor with the FAST model loaded
     */
    public static OnnxDetectionPredictor fast(String modelPath) {
        return new OnnxDetectionPredictor(OnnxDetectionPredictorProperties.fast(modelPath));
    }

    /**
     * Creates a new text detection predictor using an existing pre-trained LinkNet model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet50-15d8c4ec.onnx">
     *             linknet_resnet50
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet50_static_8_bit-65d6b0b8.onnx">
     *             linknet_resnet50 (8-bit quantized)
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet34-93e39a39.onnx">
     *             linknet_resnet34
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet34_static_8_bit-2824329d.onnx">
     *             linknet_resnet34 (8-bit quantized)
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet18-e0e0b9dc.onnx">
     *             linknet_resnet18
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet18_static_8_bit-3b3a37dd.onnx">
     *             linknet_resnet18 (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor with the LinkNet model loaded
     */
    public static OnnxDetectionPredictor linkNet(String modelPath) {
        return new OnnxDetectionPredictor(OnnxDetectionPredictorProperties.linkNet(modelPath));
    }

    /**
     * Returns the text detection predictor properties.
     *
     * @return the text detection predictor properties
     */
    public OnnxDetectionPredictorProperties getProperties() {
        return properties;
    }

    @Override
    protected FloatBufferMdArray toInputBuffer(List<BufferedImage> batch) {
        // Just your regular BCHW input
        return BufferedImageUtil.toBchwInput(batch, properties.getInputProperties());
    }

    @Override
    protected List<List<Point[]>> fromOutputBuffer(List<BufferedImage> inputBatch, FloatBufferMdArray outputBatch) {
        final IDetectionPostProcessor postProcessor = properties.getPostProcessor();
        // Normalizing pixel values via a sigmoid expit function
        final FloatBuffer outputBuffer = outputBatch.getData();
        for (int i = 0; i < outputBuffer.limit(); ++i) {
            outputBuffer.put(i, MathUtil.expit(outputBuffer.get(i)));
        }
        final List<List<Point[]>> batchTextBoxes = new ArrayList<>(inputBatch.size());
        for (int i = 0; i < inputBatch.size(); ++i) {
            final BufferedImage image = inputBatch.get(i);
            final List<Point[]> textBoxes = postProcessor.process(image, outputBatch.getSubArray(i));
            /*
             * Post-processor returns points with relative floating-point
             * coordinates in the [0, 1] range. We need to convert these to
             * absolute coordinates in the input image. This means, that we need
             * to revert resizing/padding changes as well.
             */
            convertToAbsoluteInputBoxes(image, textBoxes, properties.getInputProperties());
            batchTextBoxes.add(textBoxes);
        }
        return batchTextBoxes;
    }

    private static void convertToAbsoluteInputBoxes(
            BufferedImage image,
            List<Point[]> boxes,
            OnnxInputProperties properties
    ) {
        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();
        final float targetWidth = properties.getWidth();
        final float targetHeight = properties.getHeight();
        final float widthRatio = targetWidth / sourceWidth;
        final float heightRatio = targetHeight / sourceHeight;
        final float widthScale;
        final float heightScale;
        // We preserve ratio, when resizing input
        if (heightRatio > widthRatio) {
            heightScale = targetHeight / Math.round(sourceHeight * widthRatio);
            widthScale = 1;
        } else {
            widthScale = targetWidth / Math.round(sourceWidth * heightRatio);
            heightScale = 1;
        }
        final Consumer<Point> updater;
        if (properties.useSymmetricPad()) {
            updater = p -> p.setLocation(
                    MathUtil.clamp(sourceWidth * (0.5 + (p.getX() - 0.5) * widthScale), 0, sourceWidth),
                    MathUtil.clamp(sourceHeight * (0.5 + (p.getY() - 0.5) * heightScale), 0, sourceHeight)
            );
        } else {
            updater = p -> p.setLocation(
                    MathUtil.clamp(sourceWidth * (p.getX() * widthScale), 0, sourceWidth),
                    MathUtil.clamp(sourceHeight * (p.getY() * heightScale), 0, sourceHeight)
            );
        }
        for (final Point[] box : boxes) {
            for (final Point p : box) {
                updater.accept(p);
            }
        }
    }

    private static long[] getExpectedOutputShape(OnnxDetectionPredictorProperties properties) {
        final OnnxInputProperties inputProperties = properties.getInputProperties();
        // Dynamic batch size
        final long BATCH_SIZE = -1;
        // Output is "monochrome"
        final long CHANNEL_COUNT = 1;
        // Output retains the "image" dimension from the input
        final long height = inputProperties.getHeight();
        final long width = inputProperties.getWidth();
        return new long[] {BATCH_SIZE, CHANNEL_COUNT, height, width};
    }
}
