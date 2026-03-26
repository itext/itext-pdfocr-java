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

import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;

/**
 * Enumeration of supported image channel configuration for buffers. These are
 * used, when we need to provide an input to an ML model from an image.
 */
public enum ImageChannelConfiguration {
    /**
     * Image is represented with a single channel, which contains the
     * grayscale version of the image.
     */
    GRAYSCALE,
    /**
     * Image is represented with three channels: red, green, blue.
     */
    RGB,
    /**
     * Image is represented with three channels: blue, green, red.
     */
    BGR;

    /**
     * Returns the amount of channels used to store the image.
     *
     * @return the amount of channels used to store the image
     */
    public int getChannelCount() {
        if (ImageChannelConfiguration.GRAYSCALE == this) {
            return 1;
        } else if (ImageChannelConfiguration.RGB == this || ImageChannelConfiguration.BGR == this) {
            return 3;
        }
        // Should not get here
        throw new IllegalStateException(PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION);
    }

    /**
     * Returns the index of the red channel in the resulting ML input buffer.
     *
     * @return the index of the red channel in the resulting ML input buffer
     */
    public int getRedChannelIndex() {
        if (ImageChannelConfiguration.GRAYSCALE == this || ImageChannelConfiguration.RGB == this) {
            return 0;
        } else if (ImageChannelConfiguration.BGR == this) {
            return 2;
        }
        // Should not get here
        throw new IllegalStateException(PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION);
    }

    /**
     * Returns the index of the green channel in the resulting ML input buffer.
     *
     * @return the index of the green channel in the resulting ML input buffer
     */
    public int getGreenChannelIndex() {
        if (ImageChannelConfiguration.GRAYSCALE == this) {
            return 0;
        } else if (ImageChannelConfiguration.RGB == this || ImageChannelConfiguration.BGR == this) {
            return 1;
        }
        // Should not get here
        throw new IllegalStateException(PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION);
    }

    /**
     * Returns the index of the blue channel in the resulting ML input buffer.
     *
     * @return the index of the blue channel in the resulting ML input buffer
     */
    public int getBlueChannelIndex() {
        if (ImageChannelConfiguration.GRAYSCALE == this || ImageChannelConfiguration.BGR == this) {
            return 0;
        } else if (ImageChannelConfiguration.RGB == this) {
            return 2;
        }
        // Should not get here
        throw new IllegalStateException(PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION);
    }
}
