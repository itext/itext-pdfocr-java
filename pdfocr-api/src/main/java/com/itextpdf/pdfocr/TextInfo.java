/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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
     * Contains 4 float coordinates: bbox parameters.
     */
    private List<Float> bbox;

    /**
     * Creates a new {@link TextInfo} instance.
     */
    public TextInfo() {
        text = null;
        bbox = Collections.<Float>emptyList();
    }

    /**
     * Creates a new {@link TextInfo} instance.
     *
     * @param text any text
     * @param bbox {@link java.util.List} of bbox parameters
     */
    public TextInfo(final String text, final List<Float> bbox) {
        this.text = text;
        this.bbox = Collections.<Float>unmodifiableList(bbox);
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
     * @return {@link java.util.List} of bbox parameters
     */
    public List<Float> getBbox() {
        return new ArrayList<Float>(bbox);
    }

    /**
     * Sets bbox coordinates.
     *
     * @param bbox {@link java.util.List} of bbox parameters
     */
    public void setBbox(final List<Float> bbox) {
        this.bbox = Collections.<Float>unmodifiableList(bbox);
    }
}
