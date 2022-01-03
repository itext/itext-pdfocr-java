/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
package com.itextpdf.pdfocr;

import com.itextpdf.kernel.geom.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class describes how recognized text is positioned on the image
 * providing bbox for each text item (could be a line or a word).
 */
public class TextInfo {

    /**
     * Contains any text.
     */
    private String text;

    /**
     * {@link Rectangle} describing text bbox (lower-left based) expressed in points.
     */
    private Rectangle bboxRect;

    /**
     * Creates a new {@link TextInfo} instance.
     */
    public TextInfo() {
    }

    /**
     * Creates a new {@link TextInfo} instance from existing one.
     *
     * @param textInfo to create from
     */
    public TextInfo(final TextInfo textInfo) {
        this.text = textInfo.text;
        this.bboxRect = new Rectangle(textInfo.bboxRect);
    }

    /**
     * Creates a new {@link TextInfo} instance.
     *
     * @param text any text
     * @param bbox {@link Rectangle} describing text bbox
     */
    public TextInfo(final String text, final Rectangle bbox) {
        this.text = text;
        this.bboxRect = new Rectangle(bbox);
    }

    /**
     * Gets text element.
     *
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text element.
     *
     * @param newText retrieved text
     */
    public void setText(final String newText) {
        text = newText;
    }

    /**
     * Gets bbox coordinates.
     *
     * @return {@link Rectangle} describing text bbox
     */
    public Rectangle getBboxRect() {
        return bboxRect;
    }

    /**
     * Sets text bbox.
     *
     * @param bbox {@link Rectangle} describing text bbox
     */
    public void setBboxRect(final Rectangle bbox) {
        this.bboxRect = new Rectangle(bbox);
    }
}
