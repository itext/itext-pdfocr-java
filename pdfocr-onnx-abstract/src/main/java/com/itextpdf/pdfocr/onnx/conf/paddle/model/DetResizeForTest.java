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
package com.itextpdf.pdfocr.onnx.conf.paddle.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * POJO for the DetResizeForTest transform operation within a
 * {@code PreProcess} object in a config file.
 */
public class DetResizeForTest implements TransformOp {
    /**
     * Expected wrapping key for the DetResizeForTest operation.
     */
    public static final String WRAPPING_KEY = "DetResizeForTest";

    /*
     * With how this config is read within PaddleOCR in a regular OCR case,
     * only a small subset of configuration options is used. Everything else
     * is taken from a global "OCR" configuration.
     *
     * Only useful fields are set up here.
     */

    private final int[] imageShape;
    private final boolean keepRatio;

    /**
     * Creates a new POJO for the config file object.
     *
     * @param imageShape value under the {@code image_shape} key, optional
     * @param keepRatio value under the {@code keep_ratio} key
     */
    public DetResizeForTest(int[] imageShape, boolean keepRatio) {
        this.imageShape = imageShape;
        this.keepRatio = keepRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWrappingKey() {
        return WRAPPING_KEY;
    }

    /**
     * Returns the values under the {@code image_shape} key.
     *
     * @return the values under the {@code image_shape} key
     */
    public int[] getImageShape() {
        return imageShape;
    }

    /**
     * Returns the value under the {@code keep_ratio} key.
     *
     * @return the value under the {@code keep_ratio} key
     */
    public boolean getKeepRatio() {
        return keepRatio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DetResizeForTest that = (DetResizeForTest) o;
        return keepRatio == that.keepRatio
                && Arrays.equals(imageShape, that.imageShape);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object)Arrays.hashCode(imageShape), keepRatio);
    }
}
