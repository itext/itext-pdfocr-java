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
package com.itextpdf.pdfocr.onnxtr.util;

import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;

import java.util.Objects;

/**
 * Additional math functions.
 */
public final class MathUtil {
    private MathUtil() {
    }

    /**
     * Returns the index of the maximum value in the given array.
     *
     * @param values the array of float values (must not be null or empty)
     *
     * @return the index of the maximum value in the array
     *
     * @throws NullPointerException     if {@code values} is {@code null}
     * @throws IllegalArgumentException if {@code values} is empty
     */
    public static int argmax(float[] values) {
        Objects.requireNonNull(values);
        if (values.length == 0) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.VALUES_SHOULD_BE_A_NON_EMPTY_ARRAY);
        }
        float resultValue = Float.NEGATIVE_INFINITY;
        int resultIndex = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > resultValue) {
                resultValue = values[i];
                resultIndex = i;
            }
        }
        return resultIndex;
    }

    /**
     * Calculates the Levenshtein distance between two input strings.
     *
     * @param source the original string to be transformed
     * @param target the target string to transform into
     *
     * @return the minimum number of single-character edits required
     * to convert the source string into the target string
     */
    public static int calculateLevenshteinDistance(String source, String target) {
        if (source == null || source.isEmpty()) {
            return target == null || target.isEmpty() ? 0 : target.length();
        }
        if (target == null || target.isEmpty()) {
            return source.length();
        }

        char[] sourceChars = source.toCharArray();
        char[] targetChars = target.toCharArray();

        int[] previousRow = new int[targetChars.length + 1];

        for (int i = 0; i <= targetChars.length; ++i) {
            previousRow[i] = i;
        }

        for (int i = 1; i <= sourceChars.length; ++i) {
            int[] currentRow = new int[targetChars.length + 1];
            currentRow[0] = i;

            for (int j = 1; j <= targetChars.length; ++j) {
                int costDelete = previousRow[j] + 1;
                int costInsert = currentRow[j - 1] + 1;
                int costReplace = previousRow[j - 1] + (sourceChars[i - 1] == targetChars[j - 1] ? 0 : 1);

                currentRow[j] = Math.min(Math.min(costDelete, costInsert), costReplace);
            }

            previousRow = currentRow;
        }

        return previousRow[targetChars.length];
    }

    /**
     * Computes the sigmoid function, also known as the logistic function, for the given input.
     *
     * @param x the input value
     *
     * @return the sigmoid of the input value
     */
    public static float expit(float x) {
        return (float) (1 / (1 + Math.exp(-x)));
    }

    /**
     * Computes the logit function, which is the inverse of expit, for the given input.
     *
     * @param x the input value
     *
     * @return the logit of the input value
     */
    public static double logit(double x) {
        if (0 < x && x < 1) {
            return Math.log(x / (1.0 - x));
        }
        if (x == 0F) {
            return Float.NEGATIVE_INFINITY;
        }
        if (x == 1F) {
            return Float.POSITIVE_INFINITY;
        }
        throw new IllegalArgumentException(
                PdfOcrOnnxTrExceptionMessageConstant.X_SHOULD_BE_IN_0_1_RANGE
        );
    }

    /**
     * Computes the logit function, which is the inverse of expit, for the given input.
     *
     * @param x the input value
     *
     * @return the logit of the input value
     */
    public static float logit(float x) {
        return (float) logit((double) x);
    }

    /**
     * Computes the Euclidean modulo (non-negative remainder) of {@code x} modulo {@code y}.
     *
     * @param x the dividend
     * @param y the divisor (must not be zero)
     *
     * @return the non-negative remainder of {@code x} modulo {@code y}
     */
    public static float euclideanModulo(float x, float y) {
        final float remainder = x % y;
        if (remainder < 0) {
            return remainder + Math.abs(y);
        }
        return remainder;
    }

    /**
     * Clamps a value between a specified minimum and maximum range.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     *
     * @return {@code value} if it falls within the range; otherwise, the nearest bound (min or max)
     *
     * @throws IllegalArgumentException if {@code max} is less than {@code min}
     */
    public static double clamp(double value, double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException(PdfOcrOnnxTrExceptionMessageConstant.MAX_SHOULD_NOT_BE_LESS_THAN_MIN);
        }
        return Math.min(max, Math.max(value, min));
    }
}
