/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx.detection.score;

/**
 * Score calculator, which calculates the mean values over the observed
 * samples. If no samples are observed, 0 is returned.
 */
public class MeanScoreCalculator implements IScoreCalculator {
    /**
     * Value to return, when no samples are observed.
     */
    private static final float EMPTY_VALUE = 0.0F;

    /**
     * Sum of the observed samples.
     */
    private double sum;

    /**
     * Counter of observed samples.
     */
    private long n;

    /**
     * Creates a new score calculator.
     */
    public MeanScoreCalculator() {
        this.sum = 0.0;
        this.n = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void observe(float sample) {
        sum += sample;
        ++n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float calculate() {
        if (n == 0) {
            return EMPTY_VALUE;
        }
        return (float) (sum / n);
    }
}
