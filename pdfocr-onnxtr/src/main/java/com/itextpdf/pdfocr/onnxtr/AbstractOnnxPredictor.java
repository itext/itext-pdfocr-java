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
package com.itextpdf.pdfocr.onnxtr;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxJavaType;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.SessionOptions.ExecutionMode;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;
import ai.onnxruntime.TensorInfo;
import ai.onnxruntime.ValueInfo;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnxtr.util.BatchProcessingGenerator;
import com.itextpdf.pdfocr.onnxtr.util.Batching;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Abstract predictor, based on models running over ONNX runtime.
 *
 * @param <T> predictor input type
 * @param <R> predictor output type
 */
public abstract class AbstractOnnxPredictor<T, R> implements IPredictor<T, R> {
    /**
     * Model input properties.
     */
    private final OnnxInputProperties inputProperties;

    /**
     * ONNX runtime session options. {@link OrtSession} does not take ownership of the options, when you pass them.
     * It uses the options during initialization but does not manage their lifetime afterward. So storing to dispose
     * it after session disposal.
     */
    private final OrtSession.SessionOptions sessionOptions;

    /**
     * ONNX runtime session. Contains the machine learning model.
     */
    private final OrtSession session;

    /**
     * Key for the singular input of a model.
     */
    private final String inputName;

    /**
     * Close status of the predictor.
     */
    private boolean closed = false;

    static {
        try {
            // OnnxRuntime.init() is used under the hood.
            new OrtSession.SessionOptions().close();
        } catch (RuntimeException | UnsatisfiedLinkError e) {
            DependencyLoadChecker.processException(e);
            throw e;
        }
    }

    /**
     * Creates a new abstract predictor.
     *
     * <p>
     * If the specified model does not match input and output properties, it will throw an exception.
     *
     * @param modelPath path to the ONNX runtime model to load
     * @param inputProperties expected input properties of a model
     * @param outputShape expected shape of the output. -1 entries mean that the dimension can be
     *                    of any size (ex. batch size)
     */
    protected AbstractOnnxPredictor(String modelPath, OnnxInputProperties inputProperties, long[] outputShape) {
        this.inputProperties = Objects.requireNonNull(inputProperties);

        try {
            this.sessionOptions = createDefaultSessionOptions();
        } catch (OrtException e) {
            throw new PdfOcrException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_INIT_SESSION_OPTIONS, e);
        }

        try {
            this.session = OrtEnvironment.getEnvironment().createSession(modelPath, sessionOptions);
        } catch (Exception e) {
            this.sessionOptions.close();
            throw new PdfOcrException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_INIT_ONNX_RUNTIME_SESSION, e);
        }

        try {
            this.inputName = validateModel(this.session, inputProperties, outputShape);
        } catch (Exception e) {
            final PdfOcrException userException = new PdfOcrException(
                    PdfOcrOnnxTrExceptionMessageConstant.MODEL_DID_NOT_PASS_VALIDATION, e);
            try {
                this.session.close();
            } catch (OrtException closeException) {
                userException.addSuppressed(closeException);
            }
            this.sessionOptions.close();
            throw userException;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<R> predict(Iterator<T> inputs) {
        return new BatchProcessingGenerator<>(
                Batching.wrap(inputs, inputProperties.getBatchSize()),
                (List<T> batch) -> {
                    try (final OnnxTensor inputTensor = createTensor(toInputBuffer(batch));
                         final Result outputTensor = session.run(Collections.singletonMap(inputName, inputTensor))) {
                        return fromOutputBuffer(batch, parseModelOutput(outputTensor));
                    } catch (OrtException e) {
                        throw new PdfOcrException(
                                PdfOcrOnnxTrExceptionMessageConstant.ONNX_RUNTIME_OPERATION_FAILED, e);
                    }
                }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        try {
            session.close();
            sessionOptions.close();
        } catch (OrtException e) {
            throw new PdfOcrException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_CLOSE_ONNX_RUNTIME_SESSION, e);
        }
        closed = true;
    }

    /**
     * Converts predictor inputs to an ONNX runtime model batched input MD-array buffer.
     *
     * @param batch batch of raw predictor inputs
     *
     * @return batched model input MD-array buffer
     */
    protected abstract FloatBufferMdArray toInputBuffer(List<T> batch);

    /**
     * Converts ONNX runtime model batched output MD-array buffer to a list of predictor outputs.
     *
     * @param inputBatch list of raw predictor inputs, matching the output
     * @param outputBatch batched model output MD-array buffer
     *
     * @return a list of predictor output
     */
    protected abstract List<R> fromOutputBuffer(List<T> inputBatch, FloatBufferMdArray outputBatch);

    private static OrtSession.SessionOptions createDefaultSessionOptions() throws OrtException {
        final OrtSession.SessionOptions ortOptions = new OrtSession.SessionOptions();
        try {
            ortOptions.addCPU(true);
            if (OrtEnvironment.getAvailableProviders().contains(OrtProvider.CUDA)) {
                ortOptions.addCUDA();
            }
            ortOptions.setExecutionMode(ExecutionMode.SEQUENTIAL);
            ortOptions.setOptimizationLevel(OptLevel.ALL_OPT);
            ortOptions.setIntraOpNumThreads(-1);
            ortOptions.setInterOpNumThreads(-1);
            return ortOptions;
        } catch (Exception e) {
            ortOptions.close();
            throw e;
        }
    }

    private static OnnxTensor createTensor(FloatBufferMdArray batch) throws OrtException {
        return OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), batch.getData(), batch.getShape());
    }

    /**
     * Validates model, loaded in session, against expected inputs and outputs.
     * If model is invalid, then an exception is thrown.
     *
     * @param session current {@link OrtSession} with the loaded ONNX runtime model
     * @param properties {@link OnnxInputProperties} properties of the input of an ONNX model which expects an RGB image
     * @param outputShape expected shape of the output. -1 entries mean that the dimension can be of any size
     *
     * @return input info
     *
     * @throws OrtException in case model is invalid
     */
    private static String validateModel(OrtSession session, OnnxInputProperties properties, long[] outputShape)
            throws OrtException {
        final String inputName = validateModelInput(session, properties);
        validateModelOutput(session, outputShape);
        return inputName;
    }

    private static String validateModelInput(OrtSession session, OnnxInputProperties properties) throws OrtException {
        final Collection<NodeInfo> inputInfo = session.getInputInfo().values();
        if (inputInfo.size() != 1) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_INPUT_SIZE, inputInfo.size()));
        }
        final NodeInfo inputNodeInfo = inputInfo.iterator().next();
        final ValueInfo inputNodeValueInfo = inputNodeInfo.getInfo();
        if (!(inputNodeValueInfo instanceof TensorInfo)) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_INPUT_TYPE);
        }
        final TensorInfo inputTensorInfo = (TensorInfo) inputNodeValueInfo;
        if (inputTensorInfo.type != OnnxJavaType.FLOAT) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_INPUT_TYPE);
        }
        final long[] inputShape = inputTensorInfo.getShape();
        if (isShapeIncompatible(properties.getShape(), inputShape)) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_INPUT_SHAPE, Arrays.toString(properties.getShape()),
                    Arrays.toString(inputShape)));
        }
        return inputNodeInfo.getName();
    }

    private static void validateModelOutput(OrtSession session, long[] expectedOutputShape) throws OrtException {
        final Collection<NodeInfo> outputInfo = session.getOutputInfo().values();
        if (outputInfo.size() != 1) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_OUTPUT_SIZE, outputInfo.size()));
        }
        final NodeInfo outputNodeInfo = outputInfo.iterator().next();
        final ValueInfo outputNodeValueInfo = outputNodeInfo.getInfo();
        if (!(outputNodeValueInfo instanceof TensorInfo)) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_OUTPUT_TYPE);
        }
        final TensorInfo outputTensorInfo = (TensorInfo) outputNodeValueInfo;
        if (outputTensorInfo.type != OnnxJavaType.FLOAT) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_OUTPUT_TYPE);
        }
        final long[] actualOutputShape = outputTensorInfo.getShape();
        if (isShapeIncompatible(expectedOutputShape, actualOutputShape)) {
            throw new IllegalArgumentException(MessageFormatUtil.format(
                    PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_OUTPUT_SHAPE, Arrays.toString(expectedOutputShape),
                    Arrays.toString(actualOutputShape)));
        }
    }

    /**
     * Wraps a model output into an MD-array.
     *
     * @param result model output
     *
     * @return MD-array wrapper
     */
    private static FloatBufferMdArray parseModelOutput(OrtSession.Result result) {
        final OnnxValue output = result.get(0);
        final TensorInfo outputInfo = (TensorInfo) output.getInfo();
        final long[] outputShape = outputInfo.getShape();
        final FloatBuffer outputBuffer = ((OnnxTensor) output).getFloatBuffer();
        return new FloatBufferMdArray(outputBuffer, outputShape);
    }

    /**
     * Returns whether two shapes are compatible. I.e. have the same size and dimensions (except for -1 wildcards).
     *
     * @param expectedShape expected shape
     * @param actualShape actual model shape
     *
     * @return whether shapes are compatible
     */
    private static boolean isShapeIncompatible(long[] expectedShape, long[] actualShape) {
        if (actualShape.length != expectedShape.length) {
            return true;
        }
        for (int i = 0; i < actualShape.length; ++i) {
            // -1 is flexible, so can be skipped
            if (actualShape[i] != expectedShape[i]
                    && actualShape[i] != -1
                    && expectedShape[i] != -1) {
                return true;
            }
        }
        return false;
    }
}
