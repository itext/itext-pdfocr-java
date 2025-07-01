package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.MathUtil;

import java.util.Objects;

/**
 * Implementation of a text recognition predictor post-processor, used for
 * OnnxTR non-CRNN model outputs.
 *
 * <p>
 * This assumes there is an end-of-string token just after the vocabulary. You
 * can specify additional tokens afterwards, but they are not used in the
 * processing. No same character aggregation is done. Output is read till an
 * end-of-string token in encountered.
 */
public class EndOfStringPostProcessor implements IRecognitionPostProcessor {
    /**
     * Vocabulary used for the model output (without special tokens).
     */
    private final Vocabulary vocabulary;
    /**
     * Amount of additional tokens in the total vocabulary after the
     * end-of-string token.
     */
    private final int additionalTokens;

    /**
     * Creates a new post-processor.
     *
     * @param vocabulary vocabulary used for the model output (without special tokens)
     * @param additionalTokens amount of additional tokens in the total vocabulary after the end-of-string token
     */
    public EndOfStringPostProcessor(Vocabulary vocabulary, int additionalTokens) {
        this.vocabulary = Objects.requireNonNull(vocabulary);
        this.additionalTokens = additionalTokens;
    }

    /**
     * Creates a new post-processor without any additional tokens.
     *
     * @param vocabulary vocabulary used for the model output (without special tokens)
     */
    public EndOfStringPostProcessor(Vocabulary vocabulary) {
        this(vocabulary, 0);
    }

    /**
     * Creates a new post-processor with the default vocabulary.
     */
    public EndOfStringPostProcessor() {
        this(Vocabulary.FRENCH, 0);
    }

    @Override
    public String process(FloatBufferMdArray output) {
        final int maxWordLength = output.getDimension(0);
        final StringBuilder wordBuilder = new StringBuilder(maxWordLength);
        final float[] values = new float[labelDimension()];

        final float[] outputBuffer = output.getData().array();
        int arrayOffset = output.getArrayOffset();
        for (int i = arrayOffset; i < arrayOffset + output.getArraySize(); i += values.length) {
            System.arraycopy(outputBuffer, i, values, 0, values.length);
            final int letterIndex = MathUtil.argmax(values);
            if (letterIndex < vocabulary.size()) {
                wordBuilder.append(vocabulary.map(letterIndex));
            } else if (letterIndex == vocabulary.size()) {
                // If found end-of-sentence "<eos>" token
                break;
            }
        }

        return wordBuilder.toString();
    }

    @Override
    public int labelDimension() {
        // +1 is for "<eos>" token itself
        return vocabulary.size() + 1 + additionalTokens;
    }
}
