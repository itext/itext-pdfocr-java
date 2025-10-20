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
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

import java.awt.image.BufferedImage;
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
     * The expected output shape (BCHW).
     *
     * <p>
     * Batch size is dynamic, as usual, so -1 there.
     *
     * <p>
     * For channels, ideally, there is just one "monochrome" image, but some
     * models put multiple different metrics in one output (ex. EasyOCR
     * returns 2), so we will assume dynamic size here as well.
     *
     * <p>
     * As for height and width, while in OnnxTR the dimensions are static and
     * are equal to the input image dimensions, this is not the case
     * everywhere. For example, in EasyOCR output is quarter of the input
     * resolution, but still static. On the other hand, in PaddleOCR, input
     * and output resolutions are the same, but they are dynamic. So we cannot
     * statically check this here without knowing the exact dimensions of the
     * input.
     *
     * <p>
     * Overall, this means, that the dimension checks for the output of the
     * models are useless here, except for checking, that there are 4
     * dimensions...
     */
    private static final long[] EXPECTED_OUTPUT_SHAPE = new long[]{-1, -1, -1, -1};

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
        super(properties.getModelPath(), properties.getInputProperties(), EXPECTED_OUTPUT_SHAPE);
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
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet50_static_8_bit-09a6104f.onnx">
     *             db_resnet50 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet34-b4873198.onnx">
     *             db_resnet34
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet34_static_8_bit-027e2c7f.onnx">
     *             db_resnet34 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large-4987e7bd.onnx">
     *             db_mobilenet_v3_large
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large_static_8_bit-535a6f25.onnx">
     *             db_mobilenet_v3_large (8-bit quantized)
     *         </a>
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
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_small-10428b70.onnx">
     *             fast_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_tiny-28867779.onnx">
     *             fast_tiny
     *         </a>
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
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet50_static_8_bit-65d6b0b8.onnx">
     *             linknet_resnet50 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet34-93e39a39.onnx">
     *             linknet_resnet34
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet34_static_8_bit-2824329d.onnx">
     *             linknet_resnet34 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet18-e0e0b9dc.onnx">
     *             linknet_resnet18
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet18_static_8_bit-3b3a37dd.onnx">
     *             linknet_resnet18 (8-bit quantized)
     *         </a>
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected FloatBufferMdArray toInputBuffer(List<BufferedImage> batch) {
        // Just your regular BCHW input
        return BufferedImageUtil.toBchwInput(batch, properties.getInputProperties());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<List<Point[]>> fromOutputBuffer(List<BufferedImage> inputBatch, FloatBufferMdArray outputBatch) {
        final int batchWidth = outputBatch.getDimension(3);
        final int batchHeight = outputBatch.getDimension(2);
        final boolean usedSymmetricPadding = properties.getInputProperties().useSymmetricPad();
        final IDetectionPostProcessor postProcessor = properties.getPostProcessor();
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
            convertToAbsoluteInputBoxes(image, textBoxes, batchWidth, batchHeight, usedSymmetricPadding);
            batchTextBoxes.add(textBoxes);
        }
        return batchTextBoxes;
    }

    private static void convertToAbsoluteInputBoxes(
            BufferedImage image,
            List<Point[]> boxes,
            int batchWidth,
            int batchHeight,
            boolean usedSymmetricPadding
    ) {
        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();
        final double widthRatio = (double) batchWidth / sourceWidth;
        final double heightRatio = (double) batchHeight / sourceHeight;
        final double widthScale;
        final double heightScale;
        // We preserve ratio, when resizing input
        if (heightRatio > widthRatio) {
            heightScale = batchHeight / (double) Math.round(sourceHeight * widthRatio);
            widthScale = 1;
        } else {
            widthScale = batchWidth / (double) Math.round(sourceWidth * heightRatio);
            heightScale = 1;
        }
        final Consumer<Point> updater;
        if (usedSymmetricPadding) {
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
}
