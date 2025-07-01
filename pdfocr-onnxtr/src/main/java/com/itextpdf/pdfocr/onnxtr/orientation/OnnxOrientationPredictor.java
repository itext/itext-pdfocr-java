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
package com.itextpdf.pdfocr.onnxtr.orientation;

import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.onnxtr.AbstractOnnxPredictor;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A crop orientation predictor implementation, which is using ONNX Runtime and
 * its ML models to figure out, how text is oriented in a cropped image of text.
 */
public class OnnxOrientationPredictor
        extends AbstractOnnxPredictor<BufferedImage, TextOrientation>
        implements IOrientationPredictor {
    /**
     * Configuration properties of the predictor.
     */
    private final OnnxOrientationPredictorProperties properties;

    /**
     * Creates a crop orientation predictor with the specified properties.
     *
     * @param properties properties of the predictor
     */
    public OnnxOrientationPredictor(OnnxOrientationPredictorProperties properties) {
        super(properties.getModelPath(), properties.getInputProperties(), getExpectedOutputShape(properties));
        this.properties = properties;
    }

    /**
     * Creates a new crop orientation predictor using an existing pre-trained
     * MobileNetV3 model, stored on disk. This is the only crop orientation
     * model architecture available in OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/mobilenet_v3_small_crop_orientation-5620cf7e.onnx">
     *             mobilenet_v3_small_crop_orientation
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/mobilenet_v3_small_crop_orientation_static_8_bit-4cfaa621.onnx">
     *             mobilenet_v3_small_crop_orientation (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor with the MobileNetV3 model loaded
     */
    public static OnnxOrientationPredictor mobileNetV3(String modelPath) {
        return new OnnxOrientationPredictor(OnnxOrientationPredictorProperties.mobileNetV3(modelPath));
    }

    /**
     * Returns the crop orientation predictor properties.
     *
     * @return the crop orientation predictor properties
     */
    public OnnxOrientationPredictorProperties getProperties() {
        return properties;
    }

    @Override
    protected FloatBufferMdArray toInputBuffer(List<BufferedImage> batch) {
        // Just your regular BCHW input
        return BufferedImageUtil.toBchwInput(batch, properties.getInputProperties());
    }

    @Override
    protected List<TextOrientation> fromOutputBuffer(List<BufferedImage> inputBatch, FloatBufferMdArray outputBatch) {
        // Just extracting the highest scoring "orientation class" for each image via argmax
        final List<TextOrientation> orientations = new ArrayList<>(outputBatch.getDimension(0));
        final float[] values = new float[outputBatch.getDimension(1)];
        final float[] outputBuffer = outputBatch.getData().array();
        int offset = outputBatch.getArrayOffset();
        for (int i = offset; i < offset + outputBatch.getArraySize(); i += values.length) {
            System.arraycopy(outputBuffer, i, values, 0, values.length);
            final int label = MathUtil.argmax(values);
            orientations.add(properties.getOutputMapper().map(label));
        }
        return orientations;
    }

    private static long[] getExpectedOutputShape(OnnxOrientationPredictorProperties properties) {
        // Dynamic batch size
        final long BATCH_SIZE = -1;
        final long classCount = properties.getOutputMapper().size();
        return new long[] {BATCH_SIZE, classCount};
    }
}
