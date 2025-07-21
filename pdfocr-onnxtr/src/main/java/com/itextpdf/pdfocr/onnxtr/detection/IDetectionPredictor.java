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
import com.itextpdf.pdfocr.onnxtr.IPredictor;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Interface for predictors, which take a full image and find text boxes on
 * them. Output boxes can be rotated, which is why output is 4 points.
 */
public interface IDetectionPredictor extends IPredictor<BufferedImage, List<Point[]>> {
}
