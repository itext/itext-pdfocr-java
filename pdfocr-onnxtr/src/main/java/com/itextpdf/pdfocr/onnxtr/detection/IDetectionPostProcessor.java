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
package com.itextpdf.pdfocr.onnxtr.detection;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Interface for post-processors, which convert normalized, but still raw output
 * of an ML model and returns rotated boxes with the detected objects.
 *
 * <p>
 * Output point arrays should represent a rectangle and contain 4 points. Order
 * of the points should be the following: Bottom-Left, Top-Left, Top-Right,
 * Bottom-Right. This is with "bottom" and "top" referring to how it would look
 * on an image, not Y coordinate.
 *
 * <p>
 * Its box points are return in a different order, it will cause issues with
 * the following steps (like orientation prediction and text recognition).
 */
public interface IDetectionPostProcessor {
    /**
     * Process ML model output for a specified image and return a list of
     * detected objects.
     *
     * @param input input image, which was used to produce the inputs to the ML model
     * @param output normalized output of the ML model
     *
     * @return a list of detected objects. See interface documentation for more information
     */
    List<Point[]> process(BufferedImage input, FloatBufferMdArray output);
}
