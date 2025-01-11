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
package com.itextpdf.pdfocr.helpers;

import com.itextpdf.pdfocr.IOcrProcessProperties;

public class TestProcessProperties implements IOcrProcessProperties {

    private float cellWidth;
    private float cellHeight;
    private float startX;
    private float startY;
    private int rowCount;
    private int columnCount;

    public TestProcessProperties(int rowCount, int columnCount, float cellWidth, float cellHeight, float startX,
            float startY) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.startX = startX;
        this.startY = startY;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public float getCellWidth() {
        return cellWidth;
    }

    public float getCellHeight() {
        return cellHeight;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }
}