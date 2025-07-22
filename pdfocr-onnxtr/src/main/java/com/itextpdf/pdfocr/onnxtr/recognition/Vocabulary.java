/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnxtr.recognition;

import java.util.Objects;

/**
 * A string-based LUT for mapping text recognition model results to characters.
 *
 * <p>
 * This class assumes, that each character is represented with a single UTF-16
 * code unit. So the string itself can be used as a LUT. If this is not the
 * case, results will be unpredictable.
 *
 * <p>
 * It pretty much implements {@link com.itextpdf.pdfocr.onnxtr.IOutputLabelMapper}
 * for {@link Character} but since it would involve unnecessary boxing, it is a
 * standalone thing instead.
 */
public class Vocabulary {
    public static final Vocabulary ASCII_LOWERCASE = new Vocabulary(
            "abcdefghijklmnopqrstuvwxyz"
    );
    public static final Vocabulary ASCII_UPPERCASE = new Vocabulary(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    );
    public static final Vocabulary ASCII_LETTERS = concat(
            ASCII_LOWERCASE, ASCII_UPPERCASE
    );
    public static final Vocabulary DIGITS = new Vocabulary(
            "0123456789"
    );
    public static final Vocabulary PUNCTUATION = new Vocabulary(
            "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
    );
    public static final Vocabulary CURRENCY = new Vocabulary(
            "£€¥¢฿"
    );
    public static final Vocabulary LATIN = concat(
            DIGITS, ASCII_LETTERS, PUNCTUATION
    );
    public static final Vocabulary ENGLISH = concat(
            LATIN, new Vocabulary("°"), CURRENCY
    );
    public static final Vocabulary LEGACY_FRENCH = concat(
            LATIN, new Vocabulary("°àâéèêëîïôùûçÀÂÉÈËÎÏÔÙÛÇ"), CURRENCY
    );
    public static final Vocabulary FRENCH = concat(
            ENGLISH, new Vocabulary("àâéèêëîïôùûüçÀÂÉÈÊËÎÏÔÙÛÜÇ")
    );

    private final String lookUpString;

    /**
     * Creates a new vocabulary based on a look-up string.
     *
     * @param lookUpString look-up string to be used as LUT for the vocabulary
     */
    public Vocabulary(String lookUpString) {
        Objects.requireNonNull(lookUpString);
        if (lookUpString.codePointCount(0, lookUpString.length()) != lookUpString.length()) {
            throw new IllegalArgumentException(
                    "Look-up string contains code points, which are encoded with 2 code units"
            );
        }

        this.lookUpString = lookUpString;
    }

    /**
     * Creates a new vocabulary by concatenating multiple ones.
     *
     * @param vocabularies vocabularies to concatenate
     *
     * @return the new aggregated vocabulary
     */
    public static Vocabulary concat(Vocabulary... vocabularies) {
        final StringBuilder lutString = new StringBuilder();
        for (final Vocabulary vocabulary : vocabularies) {
            lutString.append(vocabulary.lookUpString);
        }
        return new Vocabulary(lutString.toString());
    }

    /**
     * Returns the look-up string.
     *
     * @return the look-up string
     */
    public String getLookUpString() {
        return lookUpString;
    }

    /**
     * Returns the size of the vocabulary.
     *
     * @return the size of the vocabulary
     */
    public int size() {
        return lookUpString.length();
    }

    /**
     * Returns character, which is mapped to the specified index in the lookup
     * string.
     *
     * @param index index to map
     *
     * @return mapped character
     */
    public char map(int index) {
        return lookUpString.charAt(index);
    }

    @Override
    public int hashCode() {
        return lookUpString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Vocabulary that = (Vocabulary) o;
        return Objects.equals(lookUpString, that.lookUpString);
    }

    @Override
    public String toString() {
        return lookUpString;
    }
}
