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
package com.itextpdf.pdfocr.onnx;

import java.awt.Color;

/**
 * Enumeration of implemented padding strategies for padding images. These are
 * used, when we need to adapt the image to fit an input of a ML model.
 */
public enum PaddingStrategy {
    /**
     * Image will be put into the top-left corner. Remaining pixels are filled
     * with {@code #000000}.
     */
    BOTTOM_RIGHT_BLACK,
    /**
     * Image will be put into the middle. Remaining pixels are filled with
     * {@code #000000}.
     */
    SYMMETRIC_BLACK,
    /**
     * Image will be put into the top-left corner. Remaining pixels are filled
     * with {@code #808080}.
     */
    BOTTOM_RIGHT_GRAY,
    /**
     * Image will be put into the middle. Remaining pixels are filled with
     * {@code #808080}.
     */
    SYMMETRIC_GRAY,
    /**
     * Image will be put into the top-left corner. Remaining pixels are filled
     * with {@code #FFFFFF}.
     */
    BOTTOM_RIGHT_WHITE,
    /**
     * Image will be put into the middle. Remaining pixels are filled with
     * {@code #FFFFFF}.
     */
    SYMMETRIC_WHITE,
    /**
     * Image will be put into the top-left corner. Pixels to the right are
     * repeats of the right-most pixel column of the image. Pixels to the top
     * are repeats of the top-most pixel row of the image.
     */
    BOTTOM_RIGHT_EDGE,
    /**
     * Image will be put into the middle. Pixels to the left and to the right
     * are repeats of the left-most and the right-most pixel columns of the
     * image respectfully. Pixels to the bottom and to the top are repeats of
     * the bottom-most and the top-most pixel rows of the image.
     */
    SYMMETRIC_EDGE;

    /**
     * Returns the solid color used for padding. If the strategy doesn't use a
     * solid color, returns {@code null}.
     *
     * @return the solid color used for padding, or {@code null}
     */
    public Color getSolidColor() {
        if (PaddingStrategy.BOTTOM_RIGHT_BLACK == this || PaddingStrategy.SYMMETRIC_BLACK == this) {
            return Color.BLACK;
        } else if (PaddingStrategy.BOTTOM_RIGHT_GRAY == this || PaddingStrategy.SYMMETRIC_GRAY == this) {
            return Color.GRAY;
        } else if (PaddingStrategy.BOTTOM_RIGHT_WHITE == this || PaddingStrategy.SYMMETRIC_WHITE == this) {
            return Color.WHITE;
        } else if (PaddingStrategy.SYMMETRIC_EDGE == this || PaddingStrategy.BOTTOM_RIGHT_EDGE == this) {
            return null;
        }
        return null;
    }

    /**
     * Returns whether the strategy uses a solid color for padding.
     *
     * @return whether the strategy uses a solid color for padding
     */
    public boolean usesSolidColor() {
        if (PaddingStrategy.BOTTOM_RIGHT_BLACK == this || PaddingStrategy.BOTTOM_RIGHT_GRAY == this ||
                PaddingStrategy.BOTTOM_RIGHT_WHITE == this || PaddingStrategy.SYMMETRIC_BLACK == this ||
                PaddingStrategy.SYMMETRIC_GRAY == this || PaddingStrategy.SYMMETRIC_WHITE == this) {
            return true;
        } else if (PaddingStrategy.SYMMETRIC_EDGE == this || PaddingStrategy.BOTTOM_RIGHT_EDGE == this) {
            return false;
        }
        return false;
    }

    /**
     * Returns whether the strategy uses the edge of the image for padding.
     *
     * @return whether the strategy uses the edge of the image for padding
     */
    public boolean usesImageEdge() {
        if (PaddingStrategy.SYMMETRIC_EDGE == this || PaddingStrategy.BOTTOM_RIGHT_EDGE == this) {
            return true;
        } else if (PaddingStrategy.BOTTOM_RIGHT_BLACK == this || PaddingStrategy.BOTTOM_RIGHT_GRAY == this ||
                PaddingStrategy.BOTTOM_RIGHT_WHITE == this || PaddingStrategy.SYMMETRIC_BLACK == this ||
                PaddingStrategy.SYMMETRIC_GRAY == this || PaddingStrategy.SYMMETRIC_WHITE == this) {
            return false;
        }
        return false;
    }

    /**
     * Returns whether the strategy uses symmetric padding.
     *
     * @return whether the strategy uses symmetric padding
     */
    public boolean usesSymmetricPadding() {
        if (PaddingStrategy.SYMMETRIC_BLACK == this || PaddingStrategy.SYMMETRIC_GRAY == this ||
                PaddingStrategy.SYMMETRIC_WHITE == this || PaddingStrategy.SYMMETRIC_EDGE == this) {
            return true;
        } else if (PaddingStrategy.BOTTOM_RIGHT_BLACK == this || PaddingStrategy.BOTTOM_RIGHT_GRAY == this ||
                PaddingStrategy.BOTTOM_RIGHT_WHITE == this || PaddingStrategy.BOTTOM_RIGHT_EDGE == this) {
            return false;
        }
        return false;
    }

    /**
     * Returns whether the strategy uses bottom-right padding.
     *
     * @return whether the strategy uses bottom-right padding
     */
    public boolean usesBottomRightPadding() {
        if (PaddingStrategy.BOTTOM_RIGHT_BLACK == this || PaddingStrategy.BOTTOM_RIGHT_GRAY == this ||
                PaddingStrategy.BOTTOM_RIGHT_WHITE == this || PaddingStrategy.BOTTOM_RIGHT_EDGE == this) {
            return true;
        } else if (PaddingStrategy.SYMMETRIC_BLACK == this || PaddingStrategy.SYMMETRIC_GRAY == this ||
                PaddingStrategy.SYMMETRIC_WHITE == this || PaddingStrategy.SYMMETRIC_EDGE == this) {
            return false;
        }
        return false;
    }
}
