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
package com.itextpdf.pdfocr;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.structuretree.LogicalStructureTreeItem;

import java.util.Objects;

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
     * {@link TextOrientation} describing the orientation of the text (i.e. rotation). Text is
     * assumed to be horizontal without any rotation by default.
     */
    private TextOrientation orientation = TextOrientation.HORIZONTAL;

    /**
     * If LogicalStructureTreeItem is set, then {@link TextInfo}s are expected to be in logical order.
     */
    private LogicalStructureTreeItem logicalStructureTreeItem;

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
        this.orientation = textInfo.orientation;
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
     * Creates a new {@link TextInfo} instance.
     *
     * @param text any text
     * @param bbox {@link Rectangle} describing text bbox
     * @param orientation orientation of the text
     */
    public TextInfo(final String text, final Rectangle bbox, final TextOrientation orientation) {
        this.text = text;
        this.bboxRect = new Rectangle(bbox);
        this.orientation = Objects.requireNonNull(orientation);
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

    /**
     * Gets the text orientation.
     *
     * @return {@link TextOrientation} describing the orientation of the text (i.e. rotation)
     */
    public TextOrientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the text orientation.
     *
     * @param orientation {@link TextOrientation} describing the orientation of the text (i.e. rotation)
     */
    public void setOrientation(final TextOrientation orientation) {
        this.orientation = Objects.requireNonNull(orientation);
    }

    /**
     * Retrieves structure tree item for the text item.
     *
     * @return structure tree item.
     */
    public LogicalStructureTreeItem getLogicalStructureTreeItem() {
        return logicalStructureTreeItem;
    }

    /**
     * Sets logical structure tree parent item for the text info. It allows to organize text chunks
     * into logical hierarchy, e.g. specify document paragraphs, tables, etc.
     * <p>
     *
     * If LogicalStructureTreeItem is set, then the list of {@link TextInfo}s in {@link IOcrEngine#doImageOcr}
     * return value is expected to be in logical order.
     *
     * @param logicalStructureTreeItem structure tree item.
     */
    public void setLogicalStructureTreeItem(LogicalStructureTreeItem logicalStructureTreeItem) {
        this.logicalStructureTreeItem = logicalStructureTreeItem;
    }
}
