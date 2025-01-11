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

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.structuretree.ArtifactItem;
import com.itextpdf.pdfocr.structuretree.ParagraphTreeItem;
import com.itextpdf.pdfocr.structuretree.SpanTreeItem;
import com.itextpdf.pdfocr.structuretree.TableCellTreeItem;
import com.itextpdf.pdfocr.structuretree.TableRowTreeItem;
import com.itextpdf.pdfocr.structuretree.TableTreeItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestStructureDetectionOcrEngine implements IOcrEngine {

    public TestStructureDetectionOcrEngine() {
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        return null;
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
        TestProcessProperties processProperties = (TestProcessProperties) ocrProcessContext.getOcrProcessProperties();
        List<TextInfo> textItems = new ArrayList<>();
        TableTreeItem table = new TableTreeItem();
        final float cellWidth = processProperties.getCellWidth();
        final float cellHeight = processProperties.getCellHeight();
        final float startX = processProperties.getStartX();
        final float startY = processProperties.getStartY();
        float x = startX;
        float y = startY;
        for (int i = 0; i < processProperties.getRowCount(); ++i) {
            TableRowTreeItem row = null;
            if (i > 0) {
                row = new TableRowTreeItem();
                table.addRow(row);
            }
            for (int j = 0; j < processProperties.getColumnCount(); ++j) {
                TextInfo textInfo = new TextInfo(i + " " + j, new Rectangle(x, y, cellWidth, cellHeight));
                // Mark the 1st row item as artifacts
                if (i == 0) {
                    textInfo.setLogicalStructureTreeItem(ArtifactItem.getInstance());
                } else {
                    TableCellTreeItem cell = new TableCellTreeItem();
                    row.addCell(cell);
                    ParagraphTreeItem paragraph = new ParagraphTreeItem();
                    cell.addChild(paragraph);
                    SpanTreeItem span = new SpanTreeItem();
                    paragraph.addChild(span);

                    textInfo.setLogicalStructureTreeItem(span);
                }
                textItems.add(textInfo);
                x += cellWidth;
            }
            x = startX;
            y -= cellHeight;
        }

        Map<Integer, List<TextInfo>> result = new HashMap<Integer, List<TextInfo>>();
        result.put(1, textItems);
        return result;
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile) {
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext) {
    }

    @Override
    public boolean isTaggingSupported() {
        return true;
    }
}