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
