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
package com.itextpdf.pdfocr.helpers;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;

public class ExtractionStrategy extends LocationTextExtractionStrategy {
    private com.itextpdf.kernel.geom.Rectangle imageBBoxRectangle;
    private com.itextpdf.kernel.colors.Color fillColor;
    private String layerName;
    private PdfFont pdfFont;

    public ExtractionStrategy(String name) {
        super();
        layerName = name;
    }

    public com.itextpdf.kernel.colors.Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(com.itextpdf.kernel.colors.Color color) {
        fillColor = color;
    }

    public PdfFont getPdfFont() {
        return pdfFont;
    }

    public void setPdfFont(PdfFont font) {
        pdfFont = font;
    }

    public com.itextpdf.kernel.geom.Rectangle getImageBBoxRectangle() {
        return this.imageBBoxRectangle;
    }

    public void setImageBBoxRectangle(com.itextpdf.kernel.geom.Rectangle imageBBoxRectangle) {
        this.imageBBoxRectangle = imageBBoxRectangle;
    }

    @Override
    public void eventOccurred(IEventData data, EventType type) {
        if (type.equals(EventType.RENDER_TEXT) || type.equals(EventType.RENDER_IMAGE)) {
            String tagName = getTagName(data, type);
            if ((tagName == null && layerName == null) || (layerName != null && layerName.equals(tagName))) {
                if (type.equals(EventType.RENDER_TEXT)) {
                    TextRenderInfo renderInfo = (TextRenderInfo) data;
                    setFillColor(renderInfo.getGraphicsState()
                            .getFillColor());
                    setPdfFont(renderInfo.getGraphicsState().getFont());
                    super.eventOccurred(data, type);
                }
                else if (type.equals(EventType.RENDER_IMAGE)) {
                    ImageRenderInfo renderInfo = (ImageRenderInfo) data;
                    com.itextpdf.kernel.geom.Matrix ctm = renderInfo.getImageCtm();
                    setImageBBoxRectangle(new com.itextpdf.kernel.geom.Rectangle(ctm.get(6), ctm.get(7),
                            ctm.get(0), ctm.get(4)));
                }
            }
        }
    }

    @Override
    protected boolean isChunkAtWordBoundary(TextChunk chunk,
            TextChunk previousChunk) {
        ITextChunkLocation curLoc = chunk.getLocation();
        ITextChunkLocation prevLoc = previousChunk.getLocation();

        if (curLoc.getStartLocation().equals(curLoc.getEndLocation()) ||
                prevLoc.getEndLocation()
                        .equals(prevLoc.getStartLocation())) {
            return false;
        }

        return curLoc.distParallelEnd() - prevLoc.distParallelStart() >
                (curLoc.getCharSpaceWidth() + prevLoc.getCharSpaceWidth())
                        / 2.0f;
    }

    private String getTagName(IEventData data, EventType type) {
        java.util.List<CanvasTag> tagHierarchy = null;
        if (type.equals(EventType.RENDER_TEXT)) {
            TextRenderInfo textRenderInfo = (TextRenderInfo) data;
            tagHierarchy = textRenderInfo.getCanvasTagHierarchy();
        }
        else if (type.equals(EventType.RENDER_IMAGE)) {
            ImageRenderInfo imageRenderInfo = (ImageRenderInfo) data;
            tagHierarchy = imageRenderInfo.getCanvasTagHierarchy();
        }
        return (tagHierarchy == null || tagHierarchy.size() == 0
                || tagHierarchy.get(0).getProperties().get(PdfName.Name) == null)
                ? null
                : tagHierarchy.get(0).getProperties().get(PdfName.Name).toString();
    }

}
