/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.pdfocr;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;

/**
 * Enumeration of the possible scale modes for input images.
 */
public enum ScaleMode {
    /**
     * Only width of the image will be proportionally scaled to fit
     * required size that is set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Height will be equal to the page height that was set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method and
     * width will be proportionally scaled to keep the original aspect ratio.
     */
    SCALE_WIDTH,
    /**
     * Only height of the image will be proportionally scaled to fit
     * required size that is set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Width will be equal to the page width that was set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method and
     * height will be proportionally scaled to keep the original aspect ratio.
     */
    SCALE_HEIGHT,
    /**
     * The image will be scaled to fit within the page width and height dimensions that are set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Original aspect ratio of the image stays unchanged.
     */
    SCALE_TO_FIT
}
