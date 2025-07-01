package com.itextpdf.pdfocr.onnxtr.orientation;

import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.onnxtr.IOutputLabelMapper;
import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;

import java.util.Objects;

/**
 * Properties for configuring crop orientation ONNX models.
 *
 * <p>
 * It contains a path to the model, model input properties and a model
 * output mapper.
 */
public class OnnxOrientationPredictorProperties {
    private static final OnnxInputProperties DEFAULT_INPUT_PROPERTIES = new OnnxInputProperties(
            new float[] {0.694F, 0.695F, 0.693F},
            new float[] {0.299F, 0.296F, 0.301F},
            new long[] {512, 3, 256, 256},
            true
    );
    private static final DefaultOrientationMapper DEFAULT_OUTPUT_MAPPER = new DefaultOrientationMapper();

    /**
     * Path to the ONNX model to load.
     */
    private final String modelPath;
    /**
     * Properties of the inputs of the ONNX model. Used for validation and
     * pre-processing.
     */
    private final OnnxInputProperties inputProperties;
    /**
     * Properties of the outputs of the ONNX model. Used for validation and
     * post-processing.
     */
    private final IOutputLabelMapper<TextOrientation> outputMapper;

    /**
     * Creates new crop orientation predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param outputMapper ONNX model output mapper
     */
    public OnnxOrientationPredictorProperties(
            String modelPath,
            OnnxInputProperties inputProperties,
            IOutputLabelMapper<TextOrientation> outputMapper) {
        this.modelPath = Objects.requireNonNull(modelPath);
        this.inputProperties = Objects.requireNonNull(inputProperties);
        this.outputMapper = Objects.requireNonNull(outputMapper);
    }

    /**
     * Creates a new crop orientation properties object for existing pre-trained
     * MobileNetV3 models, stored on disk. This is the only crop orientation
     * model architecture available in OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/mobilenet_v3_small_crop_orientation-5620cf7e.onnx">
     *             mobilenet_v3_small_crop_orientation
     *         </a>
     *     </li>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/mobilenet_v3_small_crop_orientation_static_8_bit-4cfaa621.onnx">
     *             mobilenet_v3_small_crop_orientation (8-bit quantized)
     *         </a>
     *     </li>
     * </ul>
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new crop orientation properties object for a MobileNetV3 model
     */
    public static OnnxOrientationPredictorProperties mobileNetV3(String modelPath) {
        return new OnnxOrientationPredictorProperties(modelPath, DEFAULT_INPUT_PROPERTIES, DEFAULT_OUTPUT_MAPPER);
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
     * Returns the ONNX model output mapper.
     *
     * @return the ONNX model output mapper
     */
    public IOutputLabelMapper<TextOrientation> getOutputMapper() {
        return outputMapper;
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) modelPath, inputProperties, outputMapper);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OnnxOrientationPredictorProperties that = (OnnxOrientationPredictorProperties) o;
        return Objects.equals(modelPath, that.modelPath) && Objects.equals(inputProperties,
                that.inputProperties) && Objects.equals(outputMapper, that.outputMapper);
    }

    @Override
    public String toString() {
        return "OnnxOrientationPredictorProperties{" +
                "modelPath='" + modelPath + '\'' +
                ", inputProperties=" + inputProperties +
                ", outputMapper=" + outputMapper +
                '}';
    }
}
