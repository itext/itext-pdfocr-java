/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;

import java.util.Objects;

/**
 * Properties for configuring text recognition ONNX models.
 *
 * <p>
 * It contains a path to the model, model input properties and a model
 * output post-processor.
 */
public class OnnxRecognitionPredictorProperties {
    private static final OnnxInputProperties DEFAULT_INPUT_PROPERTIES = new OnnxInputProperties(
            new float[] {0.694F, 0.695F, 0.693F},
            new float[] {0.299F, 0.296F, 0.301F},
            new long[] {512, 3, 32, 128},
            false
    );

    /**
     * Path to the ONNX model to load.
     */
    private final String modelPath;
    /**
     * Properties of the inputs of the ONNX model. Used for validation (both
     * input and output, since output mask size is the same) and pre-processing.
     */
    private final OnnxInputProperties inputProperties;
    /**
     * Post-processor of the outputs of the ONNX model. Converts the  output of
     * the model to a text string.
     */
    private final IRecognitionPostProcessor postProcessor;

    /**
     * Creates new text recognition predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     */
    public OnnxRecognitionPredictorProperties(
            String modelPath,
            OnnxInputProperties inputProperties,
            IRecognitionPostProcessor postProcessor
    ) {
        this.modelPath = Objects.requireNonNull(modelPath);
        this.inputProperties = Objects.requireNonNull(inputProperties);
        this.postProcessor = Objects.requireNonNull(postProcessor);
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * CRNN models with a VGG-16 backbone, stored on disk. This is the default
     * text recognition model in OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_vgg16_bn-662979cc.onnx">
     *             crnn_vgg16_bn
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_vgg16_bn_static_8_bit-bce050c7.onnx">
     *             crnn_vgg16_bn (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a CRNN model with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnVgg16(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new CrnnPostProcessor(Vocabulary.LEGACY_FRENCH)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * CRNN models with a MobileNet V3 backbone, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_mobilenet_v3_large-d42e8185.onnx">
     *             crnn_mobilenet_v3_large
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_mobilenet_v3_large_static_8_bit-459e856d.onnx">
     *             crnn_mobilenet_v3_large (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_mobilenet_v3_small-bded4d49.onnx">
     *             crnn_mobilenet_v3_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_mobilenet_v3_small_static_8_bit-4949006f.onnx">
     *             crnn_mobilenet_v3_small (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a CRNN model with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnMobileNetV3(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new CrnnPostProcessor(Vocabulary.FRENCH)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained MASTER models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/master-b1287fcd.onnx">
     *             MASTER
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/master_dynamic_8_bit-d8bd8206.onnx">
     *             MASTER (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a MASTER model
     */
    public static OnnxRecognitionPredictorProperties master(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                // Additional "<sos>" and "<pad>" tokens
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 2)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * PARSeq models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/parseq-00b40714.onnx">
     *             parseq
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/parseq_dynamic_8_bit-5b04d9f7.onnx">
     *             parseq (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * SAR models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/sar_resnet31-395f8005.onnx">
     *             sar_resnet31
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/sar_resnet31_static_8_bit-c07316bc.onnx">
     *             sar_resnet31 (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a SAR model
     */
    public static OnnxRecognitionPredictorProperties sar(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * ViTSTR models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/vitstr_base-ff62f5be.onnx">
     *             vitstr_base
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/vitstr_base_dynamic_8_bit-976c7cd6.onnx">
     *             vitstr_base (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/vitstr_small-3ff9c500.onnx">
     *             vitstr_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/vitstr_small_dynamic_8_bit-bec6c796.onnx">
     *             vitstr_small (8-bit quantized)
     *         </a>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a ViTSTR model
     */
    public static OnnxRecognitionPredictorProperties viTstr(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0)
        );
    }

    /**
     * Returns the path to the ONNX model.
     *
     * @return the path to the ONNX model
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * Returns the ONNX model input properties.
     *
     * @return the ONNX model input properties
     */
    public OnnxInputProperties getInputProperties() {
        return inputProperties;
    }

    /**
     * Returns the ONNX model output post-processor.
     *
     * @return the ONNX model output post-processor
     */
    public IRecognitionPostProcessor getPostProcessor() {
        return postProcessor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OnnxRecognitionPredictorProperties that = (OnnxRecognitionPredictorProperties) o;
        return Objects.equals(modelPath, that.modelPath) && Objects.equals(inputProperties,
                that.inputProperties) && Objects.equals(postProcessor, that.postProcessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)modelPath, inputProperties, postProcessor);
    }

    @Override
    public String toString() {
        return "OnnxRecognitionPredictorProperties{" +
                "modelPath='" + modelPath + '\'' +
                ", inputProperties=" + inputProperties +
                ", postProcessor=" + postProcessor +
                '}';
    }
}
