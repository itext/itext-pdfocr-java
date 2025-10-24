/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnxtr.recognition;

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
public class CrnnPostProcessor extends BasicLabelPostProcessor {
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void appendLabel(StringBuilder output, int labelIndex) {
        // Last letter is <blank>
        if (labelIndex < vocabulary.size()) {
            output.append(vocabulary.map(labelIndex));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int labelDimension() {
        // +1 is "<blank>" token at the end
        return vocabulary.size() + 1;
    }
}
