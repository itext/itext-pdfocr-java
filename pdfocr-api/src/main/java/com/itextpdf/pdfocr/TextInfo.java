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
package com.itextpdf.pdfocr;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.structuretree.LogicalStructureTreeItem;

/**
 * This class describes how recognized text is positioned on the image
 * providing bbox for each text item (could be a line or a word).
 */
public class TextInfo {
    /**
     * Image pixel to PDF point ratio.
     */
    private static final float PX_TO_PT = 0.75F;

    /**
     * Contains any text.
     */
    private String text;

    /**
     * Array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in PDF points.
     */
    private Point[] textPoints;

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
        this.textPoints = (Point[]) textInfo.textPoints.clone();
    }

    /**
     * Creates new {@link TextInfo} instance.
     *
     * @param text text string
     * @param bbox array of 4 {@link Point}s describing text bbox (lower-left based relative to text)
     * expressed in points (0 - lower-left, 1 - upper-left, 2 - upper-right, 3 - lower-right point)
     */
    public TextInfo(final String text, final Point[] bbox) {
        this.text = text;
        this.textPoints = bbox;
    }

    /**
     * Creates new {@link TextInfo} instance. Could be used for not rotated text chunks.
     *
     * @param text text string
     * @param bbox {@link Rectangle} describing text bounding box expressed in PDF points
     */
    public TextInfo(final String text, final Rectangle bbox) {
        this.text = text;
        this.textPoints = new Point[]{
                new Point(bbox.getLeft(), bbox.getBottom()),
                new Point(bbox.getLeft(), bbox.getTop()),
                new Point(bbox.getRight(), bbox.getTop()),
                new Point(bbox.getRight(), bbox.getBottom())
        };
    }

    /**
     * Gets text element.
     *
     * @return text string
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text element.
     *
     * @param newText retrieved text
     *
     * @return this instance
     */
    public TextInfo setText(final String newText) {
        text = newText;
        return this;
    }

    /**
     * Gets array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in points.
     *
     * <p>
     * Point array stores text polygon in the following order relative to text:
     * 0 - lower-left, 1 - upper-left, 2 - upper-right, 3 - lower-right point.
     *
     * <p>
     * The following coordinate system is used for points coordinate:
     * the origin is located in left bottom corner of the page,
     * vertical (y) coordinates increase from the bottom of the page to the top,
     * horizontal (x) coordinates increase from the left side of the page to the right,
     * axe unit is user space unit which we call PDF point (1 PDF point = 1/72 inch = 4/3 pixel).
     *
     * @return array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in points
     */
    public Point[] getTextPoints() {
        return textPoints;
    }

    /**
     * Sets array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in points.
     *
     * <p>
     * Point array should store text polygon in the following order relative to text:
     * 0 - lower-left, 1 - upper-left, 2 - upper-right, 3 - lower-right point.
     *
     * <p>
     * The following coordinate system is used for points coordinate:
     * the origin is located in left bottom corner of the page,
     * vertical (y) coordinates increase from the bottom of the page to the top,
     * horizontal (x) coordinates increase from the left side of the page to the right,
     * axe unit is user space unit which we call PDF point (1 PDF point = 1/72 inch = 4/3 pixel).
     *
     * @param textPoints array of 4 {@link Point}s describing text bbox (lower-left based relative to text)
     * expressed in points
     *
     * @return this instance
     */
    public TextInfo setTextPoints(Point[] textPoints) {
        this.textPoints = textPoints;
        return this;
    }

    /**
     * Gets array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in pixels.
     *
     * <p>
     * Point array stores text polygon in the following order relative to text:
     * 0 - lower-left, 1 - upper-left, 2 - upper-right, 3 - lower-right point.
     *
     * <p>
     * The following coordinate system is used for text points coordinate:
     * the origin is located in left top corner of the page (image),
     * vertical (y) coordinates increase from the top of the page to the bottom,
     * horizontal (x) coordinates increase from the left side of the page to the right,
     * axe unit is pixel (1 pixel = 1/96 inch = 0.75 PDF point).
     *
     * @param imageHeight height of the image to convert the text PDF points to image pixels coordinates.
     * Used to change the {@code y} origin
     *
     * @return array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in pixels
     */
    public Point[] getPixelTextPoints(int imageHeight) {
        Point[] result = new Point[this.textPoints.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new Point(this.textPoints[i].getX() / PX_TO_PT,
                    imageHeight - this.textPoints[i].getY() / PX_TO_PT);
        }
        return result;
    }

    /**
     * Sets an array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in pixels.
     *
     * <p>
     * Point array should store text polygon in the following order relative to text:
     * 0 - lower-left, 1 - upper-left, 2 - upper-right, 3 - lower-right point.
     *
     * <p>
     * The following coordinate system is used for text points coordinate:
     * the origin is located in left top corner of the page,
     * vertical (y) coordinates increase from the top of the page to the bottom,
     * horizontal (x) coordinates increase from the left side of the page to the right,
     * axe unit is pixel (1 pixel = 1/96 inch = 0.75 PDF point).
     *
     * @param textPoints array of 4 {@link Point}s describing text bbox (0 - lower-left, 1 - upper-left,
     * 2 - upper-right, 3 - lower-right relative to text) expressed in pixels
     * @param imageHeight height of the image to convert the text PDF points to image pixels coordinates.
     * Used to change the {@code y} origin
     *
     * @return array of 4 {@link Point}s describing text bbox (lower-left based relative to text) expressed in pixels
     */
    public TextInfo setPixelTextPoints(Point[] textPoints, int imageHeight) {
        Point[] result = new Point[textPoints.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new Point(PX_TO_PT * textPoints[i].getX(),
                    PX_TO_PT * (imageHeight - textPoints[i].getY()));
        }
        this.textPoints = result;
        return this;
    }

    /**
     * Converts a text polygon to a bounding box.
     *
     * @return {@link Rectangle} representing text bounding box
     */
    public Rectangle getBBoxRect() {
        float minX = (float) this.textPoints[0].getX();
        float maxX = minX;
        float minY = (float) this.textPoints[0].getY();
        float maxY = minY;
        for (int i = 1; i < this.textPoints.length; ++i) {
            final float x = (float) this.textPoints[i].getX();
            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
            final float y = (float) this.textPoints[i].getY();
            if (y < minY) {
                minY = y;
            } else if (y > maxY) {
                maxY = y;
            }
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Returns the text rotation angle in radian for this {@link TextInfo} in the range of -pi to pi.
     *
     * @return the text rotation angle in radian for the current {@link TextInfo}
     */
    public float getRotationAngle() {
        double dx = textPoints[3].getX() - textPoints[0].getX();
        double dy = textPoints[3].getY() - textPoints[0].getY();
        return (float) Math.atan2(dy, dx);
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
     *
     * <p>
     * If LogicalStructureTreeItem is set, then the list of {@link TextInfo}s in {@link IOcrEngine#doImageOcr}
     * return value is expected to be in logical order.
     *
     * @param logicalStructureTreeItem structure tree item
     */
    public void setLogicalStructureTreeItem(LogicalStructureTreeItem logicalStructureTreeItem) {
        this.logicalStructureTreeItem = logicalStructureTreeItem;
    }
}
