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
package com.itextpdf.pdfocr.onnxtr.orientation;

import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.pdfocr.onnxtr.IOutputLabelMapper;

/**
 * Default implementation for mapping output of a crop orientation model to
 * {@link TextOrientation} values.
 * <p>
 * It expects the output to be an integer in the [0, 3] range, where the value
 * specifies the amount of 90 degree rotations. Usually the model output would
 * be a 4-element vector of probability-like values, which is then converted to
 * an integer index via an {@code argmax} function.
 * </p>
 */
public class DefaultOrientationMapper implements IOutputLabelMapper<TextOrientation> {
    public DefaultOrientationMapper() {
        // noop
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public TextOrientation map(int index) {
        switch (index) {
            case 0:
                return TextOrientation.HORIZONTAL;
            case 1:
                return TextOrientation.HORIZONTAL_ROTATED_90;
            case 2:
                return TextOrientation.HORIZONTAL_ROTATED_180;
            case 3:
                return TextOrientation.HORIZONTAL_ROTATED_270;
            default:
                throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
    }
}
