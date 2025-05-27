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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.AbstractOnnxPredictor;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A text recognition predictor implementation, which is using ONNX Runtime and
 * its ML models to recognize text characters on an image.
 */
public class OnnxRecognitionPredictor
        extends AbstractOnnxPredictor<BufferedImage, String>
        implements IRecognitionPredictor {
    /**
     * Configuration properties of the predictor.
     */
    private final OnnxRecognitionPredictorProperties properties;

    /**
     * Creates a text recognition predictor with the specified properties.
     *
     * @param properties properties of the predictor
     */
    public OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties properties) {
        super(properties.getModelPath(), properties.getInputProperties(), getExpectedOutputShape(properties));
        this.properties = properties;
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * CRNN model with a VGG-16 backbone, stored on disk. This is the default
     * text recognition model in OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_vgg16_bn-662979cc.onnx">
     *             crnn_vgg16_bn
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_vgg16_bn_static_8_bit-bce050c7.onnx">
     *             crnn_vgg16_bn (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the CRNN model loaded with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictor crnnVgg16(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.crnnVgg16(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * CRNN model with a MobileNet V3 backbone, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_mobilenet_v3_large-d42e8185.onnx">
     *             crnn_mobilenet_v3_large
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_mobilenet_v3_large_static_8_bit-459e856d.onnx">
     *             crnn_mobilenet_v3_large (8-bit quantized)
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_mobilenet_v3_small-bded4d49.onnx">
     *             crnn_mobilenet_v3_small
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_mobilenet_v3_small_static_8_bit-4949006f.onnx">
     *             crnn_mobilenet_v3_small (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the CRNN model loaded with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictor crnnMobileNetV3(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.crnnMobileNetV3(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * MASTER model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/master-b1287fcd.onnx">
     *             MASTER
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/master_dynamic_8_bit-d8bd8206.onnx">
     *             MASTER (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     * </p>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the MASTER model loaded
     */
    public static OnnxRecognitionPredictor master(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.master(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * PARSeq model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/parseq-00b40714.onnx">
     *             parseq
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/parseq_dynamic_8_bit-5b04d9f7.onnx">
     *             parseq (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the PARSeq model loaded
     */
    public static OnnxRecognitionPredictor parSeq(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.parSeq(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * SAR model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/sar_resnet31-395f8005.onnx">
     *             sar_resnet31
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/sar_resnet31_static_8_bit-c07316bc.onnx">
     *             sar_resnet31 (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the SAR model loaded
     */
    public static OnnxRecognitionPredictor sar(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.sar(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained ViTSTR model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/vitstr_base-ff62f5be.onnx">
     *             vitstr_base
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/vitstr_base_dynamic_8_bit-976c7cd6.onnx">
     *             vitstr_base (8-bit quantized)
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/vitstr_small-3ff9c500.onnx">
     *             vitstr_small
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/vitstr_small_dynamic_8_bit-bec6c796.onnx">
     *             vitstr_small (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the ViTSTR model loaded
     */
    public static OnnxRecognitionPredictor viTstr(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.viTstr(modelPath));
    }

    /**
     * Returns the text recognition predictor properties.
     *
     * @return the text recognition predictor properties
     */
    public OnnxRecognitionPredictorProperties getProperties() {
        return properties;
    }

    @Override
    protected FloatBufferMdArray toInputBuffer(List<BufferedImage> batch) {
        // Just your regular BCHW input
        return BufferedImageUtil.toBchwInput(batch, properties.getInputProperties());
    }

    @Override
    protected List<String> fromOutputBuffer(List<BufferedImage> inputBatch, FloatBufferMdArray outputBatch) {
        final int batchSize = outputBatch.getDimension(0);
        final List<String> words = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; ++i) {
            words.add(properties.getPostProcessor().process(outputBatch.getSubArray(i)));
        }
        return words;
    }

    private static long[] getExpectedOutputShape(OnnxRecognitionPredictorProperties properties) {
        // Dynamic batch size
        final long BATCH_SIZE = -1;
        // Token count is, usually, not dynamic in the model, but we don't
        // really care about it, as it is just a loop boundary in the algorithm
        final long TOKEN_COUNT = -1;
        final long classCount = properties.getPostProcessor().labelDimension();
        return new long[] {BATCH_SIZE, TOKEN_COUNT, classCount};
    }
}
