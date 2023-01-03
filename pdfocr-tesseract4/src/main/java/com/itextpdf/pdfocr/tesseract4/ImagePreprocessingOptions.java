/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

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
package com.itextpdf.pdfocr.tesseract4;

/**
 * Additional options applied on image preprocessing step.
 */
public class ImagePreprocessingOptions {

    /**
     * Adaptive threshold tile width as described here: http://www.leptonica.org/binarization.html.
     * Default value of 0 is considered as full image width which means no tiling.
     */
    private int tileWidth;


    /**
     * Adaptive threshold tile height as described here: http://www.leptonica.org/binarization.html.
     * Default value of 0 is considered as full image height which means no tiling.
     */
    private int tileHeight;

    /**
     * Adaptive threshold smoothing as described here: http://www.leptonica.org/binarization.html.
     */
    private boolean smoothTiling = true;

    /**
     * Creates {@link ImagePreprocessingOptions} instance.
     */
    public ImagePreprocessingOptions() {
    }

    /**
     * Creates a new {@link ImagePreprocessingOptions} instance
     * based on another {@link ImagePreprocessingOptions} instance (copy
     * constructor).
     *
     * @param imagePreprocessingOptions the other {@link ImagePreprocessingOptions} instance
     */
    public ImagePreprocessingOptions(ImagePreprocessingOptions imagePreprocessingOptions) {
        this.tileWidth = imagePreprocessingOptions.tileWidth;
        this.tileHeight = imagePreprocessingOptions.tileHeight;
        this.smoothTiling = imagePreprocessingOptions.smoothTiling;
    }

    /**
     * Gets {@link #tileWidth}.
     * @return tile width
     */
    final public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Sets {@link #tileWidth}.
     * @param tileWidth tile width
     * @return {@link ImagePreprocessingOptions}
     */
    final public ImagePreprocessingOptions setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
        return this;
    }

    /**
     * Gets {@link #tileHeight}.
     * @return tile height
     */
    final public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Sets {@link #tileHeight}.
     * @param tileHeight tile height
     * @return {@link ImagePreprocessingOptions}
     */
    final public ImagePreprocessingOptions setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
        return this;
    }

    /**
     * Gets {@link #smoothTiling}.
     * @return smooth tiling flag
     */
    final public boolean isSmoothTiling() {
        return smoothTiling;
    }

    /**
     * Sets {@link #smoothTiling}.
     * @param smoothTiling smooth tiling flag
     * @return {@link ImagePreprocessingOptions}
     */
    final public ImagePreprocessingOptions setSmoothTiling(boolean smoothTiling) {
        this.smoothTiling = smoothTiling;
        return this;
    }
}
