/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

/**
 * Abstract Implementation of a basic text recognition predictor post-processor. It contains logic, which is common
 * between OnnxTR, EasyOCR and PaddleOCR label mappers:
 * <ul>
 *     <li>It receives a two-dimensional array with a (maxStringLength, labelDimension) shape.</li>
 *     <li>Label with the highest value in the array is picked as the recognized one.</li>
 *     <li>There are labels, which should not be added to the string, and should just be treated as separators.</li>
 *     <li>If the same label appears multiple times in a row in a string, it is returned only once.</li>
 * </ul>
 */
public abstract class BasicLabelPostProcessor implements IRecognitionPostProcessor {
    /**
     * {@inheritDoc}
     */
    @Override
    public String process(FloatBufferMdArray output) {
        final int maxStringLength = output.getDimension(0);
        final int labelStride = output.getDimension(1);
        final StringBuilder stringBuilder = new StringBuilder(maxStringLength);
        final float[] values = new float[Math.min(labelDimension(), labelStride)];

        final float[] outputBuffer = output.getData().array();
        int prevLabelIndex = -1;
        int arrayOffset = output.getArrayOffset();
        for (int i = arrayOffset; i < arrayOffset + output.getArraySize(); i += labelStride) {
            System.arraycopy(outputBuffer, i, values, 0, values.length);
            final int labelIndex = MathUtil.argmax(values);
            if (prevLabelIndex != labelIndex) {
                appendLabel(stringBuilder, labelIndex);
            }
            prevLabelIndex = labelIndex;
        }
        return stringBuilder.toString();
    }

    /**
     * Adds label to the string output, based on the label's index. Can be a
     * noop, if label index should be ignored.
     *
     * @param output     string builder to append the label to
     * @param labelIndex index of the label to append, guaranteed to be in the
     *                   [0; labelDimension()) range.
     */
    protected abstract void appendLabel(StringBuilder output, int labelIndex);
}
