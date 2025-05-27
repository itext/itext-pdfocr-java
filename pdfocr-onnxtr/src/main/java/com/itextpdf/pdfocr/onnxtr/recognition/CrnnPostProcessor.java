package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * Implementation of a text recognition predictor post-processor, used for
 * OnnxTR CRNN model outputs.
 *
 * <p>
 * Notably it does not have end-of-string tokens. Only token, besides the
 * vocabulary one, is blank, which is just skipped or used as a char separator.
 * Multiple of the same label in a row is aggregated into one.
 */
public class CrnnPostProcessor implements IRecognitionPostProcessor {
    /**
     * Vocabulary used for the model output (without special tokens).
     */
    private final Vocabulary vocabulary;

    /**
     * Creates a new post-processor.
     *
     * @param vocabulary vocabulary used for the model output (without special tokens)
     */
    public CrnnPostProcessor(Vocabulary vocabulary) {
        this.vocabulary = Objects.requireNonNull(vocabulary);
    }

    /**
     * Creates a new post-processor with the default vocabulary.
     */
    public CrnnPostProcessor() {
        this.vocabulary = Vocabulary.FRENCH;
    }

    @Override
    public String process(FloatBufferMdArray output) {
        final int maxWordLength = output.getDimension(0);
        final StringBuilder wordBuilder = new StringBuilder(maxWordLength);
        final float[] values = new float[labelDimension()];
        final FloatBuffer outputBuffer = output.getData();
        int prevLetterIndex = -1;
        while (outputBuffer.hasRemaining()) {
            outputBuffer.get(values);
            final int letterIndex = MathUtil.argmax(values);
            // Last letter is <blank>
            if (prevLetterIndex != letterIndex && letterIndex < vocabulary.size()) {
                wordBuilder.append(vocabulary.map(letterIndex));
            }
            prevLetterIndex = letterIndex;
        }
        return wordBuilder.toString();
    }

    @Override
    public int labelDimension() {
        // +1 is "<blank>" token
        return vocabulary.size() + 1;
    }
}
