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

import java.util.Objects;

/**
 * Additional math functions.
 */
public final class MathUtil {
    private MathUtil() {
    }

    public static int argmax(float[] values) {
        Objects.requireNonNull(values);
        if (values.length == 0) {
            throw new IllegalArgumentException("values should be a non-empty array");
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

        for (int i = 0; i <= targetChars.length; i++) {
            previousRow[i] = i;
        }

        for (int i = 1; i <= sourceChars.length; i++) {
            int[] currentRow = new int[targetChars.length + 1];
            currentRow[0] = i;

            for (int j = 1; j <= targetChars.length; j++) {
                int costDelete = previousRow[j] + 1;
                int costInsert = currentRow[j - 1] + 1;
                int costReplace = previousRow[j - 1] + (sourceChars[i - 1] == targetChars[j - 1] ? 0 : 1);

                currentRow[j] = Math.min(Math.min(costDelete, costInsert), costReplace);
            }

            previousRow = currentRow;
        }

        return previousRow[targetChars.length];
    }

    public static float expit(float x) {
        return (float) (1 / (1 + Math.exp(-x)));
    }

    public static float euclideanModulo(float x, float y) {
        final float remainder = x % y;
        if (remainder < 0) {
            return remainder + Math.abs(y);
        }
        return remainder;
    }

    public static double clamp(double value, double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException("max should not be less than min");
        }
        return Math.min(max, Math.max(value, min));
    }
}
