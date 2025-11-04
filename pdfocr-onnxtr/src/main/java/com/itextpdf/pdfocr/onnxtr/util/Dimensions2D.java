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
 * A basic 2-element tuple with a width and a height.
 */
public class Dimensions2D {
    private final int width;
    private final int height;

    /**
     * Creates new {@link Dimensions2D} instance.
     *
     * @param width width dimension
     * @param height height dimension
     */
    public Dimensions2D(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Gets width of the {@link Dimensions2D} instance.
     *
     * @return width of the {@link Dimensions2D} instance
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets height of the {@link Dimensions2D} instance.
     *
     * @return height of the {@link Dimensions2D} instance
     */
    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Dimensions2D that = (Dimensions2D) o;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
