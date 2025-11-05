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
package com.itextpdf.pdfocr.onnxtr.conf.paddle.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * POJO for the NormalizeImage transform operation within a {@code PreProcess}
 * object in a config file.
 */
public class NormalizeImage implements TransformOp {
    /**
     * Expected wrapping key for the NormalizeImage operation.
     */
    public static final String WRAPPING_KEY = "NormalizeImage";

    /*
     * Not including the "order" field here, as in our logic we are always
     * working with CHW anyway. Also skipping scale, as we do 1 / 255 anyway...
     */

    private final float[] mean;
    private final float[] std;

    /**
     * Creates a new POJO for the config file object.
     *
     * @param mean values under the {@code mean} key
     * @param std values under the {@code std} key
     */
    public NormalizeImage(float[] mean, float[] std) {
        this.mean = Objects.requireNonNull(mean);
        this.std = Objects.requireNonNull(std);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWrappingKey() {
        return WRAPPING_KEY;
    }

    /**
     * Returns the values under the {@code mean} key.
     *
     * @return the values under the {@code mean} key
     */
    public float[] getMean() {
        return mean;
    }

    /**
     * Returns the values under the {@code std} key.
     *
     * @return the values under the {@code std} key
     */
    public float[] getStd() {
        return std;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NormalizeImage that = (NormalizeImage) o;
        return Arrays.equals(mean, that.mean)
                && Arrays.equals(std, that.std);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(mean), Arrays.hashCode(std));
    }
}
