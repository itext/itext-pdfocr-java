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

    public com.itextpdf.kernel.geom.Rectangle getImageBBoxRectangle() { return this.imageBBoxRectangle; }

    @Override
    public void eventOccurred(IEventData data, EventType type) {
        java.util.List<CanvasTag> tagHierarchy = null;
        if (type.equals(EventType.RENDER_TEXT)) {
            TextRenderInfo textRenderInfo = (TextRenderInfo) data;
            tagHierarchy = textRenderInfo.getCanvasTagHierarchy();
        }
        else if (type.equals(EventType.RENDER_IMAGE)) {
            ImageRenderInfo imageRenderInfo = (ImageRenderInfo) data;
            tagHierarchy = imageRenderInfo.getCanvasTagHierarchy();
        }

        if (tagHierarchy != null) {
            for (CanvasTag tag : tagHierarchy) {
                PdfDictionary dict = tag.getProperties();
                String name = dict.get(PdfName.Name).toString();
                if (name.equals(layerName)) {
                    if (type.equals(EventType.RENDER_TEXT)) {
                        TextRenderInfo renderInfo = (TextRenderInfo) data;
                        setFillColor(renderInfo.getGraphicsState()
                                .getFillColor());
                        setPdfFont(renderInfo.getGraphicsState().getFont());
                        super.eventOccurred(data, type);
                        break;
                    }
                    else if (type.equals(EventType.RENDER_IMAGE)) {
                        ImageRenderInfo renderInfo = (ImageRenderInfo) data;
                        com.itextpdf.kernel.geom.Matrix ctm = renderInfo.getImageCtm();
                        this.imageBBoxRectangle = new com.itextpdf.kernel.geom.Rectangle(ctm.get(6), ctm.get(7),
                                ctm.get(0), ctm.get(4));
                        break;
                    }
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
}
