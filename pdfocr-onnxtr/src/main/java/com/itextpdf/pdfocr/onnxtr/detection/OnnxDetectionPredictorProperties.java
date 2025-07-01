package com.itextpdf.pdfocr.onnxtr.detection;

import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;

import java.util.Objects;

/**
 * Properties for configuring text detection ONNX models.
 *
 * <p>
 * It contains a path to the model, model input properties and a model
 * output post-processor.
 */
public class OnnxDetectionPredictorProperties {
    private static final OnnxInputProperties DEFAULT_INPUT_PROPERTIES = new OnnxInputProperties(
            new float[] {0.798F, 0.785F, 0.772F},
            new float[] {0.264F, 0.2749F, 0.287F},
            new long[] {2, 3, 1024, 1024},
            true
    );
    private static final IDetectionPostProcessor DEFAULT_POST_PROCESSOR =
            new OnnxDetectionPostProcessor();
    /*
     * By default, DBNet has different thresholds for binarization and for
     * discarding results.
     */
    private static final IDetectionPostProcessor DB_NET_POST_PROCESSOR =
            new OnnxDetectionPostProcessor(0.3F, 0.1F);

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
     * Post-processor of the outputs of the ONNX model. Converts the mask-like
     * output of the model to rotated text rectangles.
     */
    private final IDetectionPostProcessor postProcessor;

    /**
     * Creates new text detection predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     */
    public OnnxDetectionPredictorProperties(
            String modelPath,
            OnnxInputProperties inputProperties,
            IDetectionPostProcessor postProcessor
    ) {
        this.modelPath = Objects.requireNonNull(modelPath);
        this.inputProperties = Objects.requireNonNull(inputProperties);
        this.postProcessor = Objects.requireNonNull(postProcessor);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * DBNet models, stored on disk.
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
     * @return a new text detection properties object for a DBNet model
     */
    public static OnnxDetectionPredictorProperties dbNet(String modelPath) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                DB_NET_POST_PROCESSOR
        );
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * FAST models, stored on disk. This is the default text detection model in
     * OnnxTR.
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
     * @return a new text detection properties object for a FAST model
     */
    public static OnnxDetectionPredictorProperties fast(String modelPath) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                DEFAULT_POST_PROCESSOR
        );
    }

    /**
     * Creates a new text detection properties object for existing pre-trained LinkNet models, stored on disk.
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
     * @return a new text detection properties object for a LinkNet model
     */
    public static OnnxDetectionPredictorProperties linkNet(String modelPath) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                DEFAULT_POST_PROCESSOR
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
    public IDetectionPostProcessor getPostProcessor() {
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
        final OnnxDetectionPredictorProperties that = (OnnxDetectionPredictorProperties) o;
        return Objects.equals(modelPath, that.modelPath)
                && Objects.equals(inputProperties, that.inputProperties)
                && Objects.equals(postProcessor, that.postProcessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)modelPath, inputProperties, postProcessor);
    }

    @Override
    public String toString() {
        return "OnnxDetectionPredictorProperties{" +
                "modelPath='" + modelPath + '\'' +
                ", inputProperties=" + inputProperties +
                ", postProcessor=" + postProcessor +
                '}';
    }
}
