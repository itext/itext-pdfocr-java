package com.itextpdf.pdfocr.helpers;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;

public class ExtractionStrategy extends LocationTextExtractionStrategy {
    private com.itextpdf.kernel.colors.Color fillColor;
    private String layerName;
    private PdfFont pdfFont;

    public ExtractionStrategy(String name) {
        super();
        layerName = name;
    }

    public void setFillColor(com.itextpdf.kernel.colors.Color color) {
        fillColor = color;
    }

    public com.itextpdf.kernel.colors.Color getFillColor() {
        return fillColor;
    }

    public void setPdfFont(PdfFont font) {
        pdfFont = font;
    }

    public PdfFont getPdfFont() {
        return pdfFont;
    }

    @Override
    protected boolean isChunkAtWordBoundary(TextChunk chunk,
            TextChunk previousChunk) {
        String cur = chunk.getText();
        String prev = previousChunk.getText();
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

    @Override
    public void eventOccurred(IEventData data, EventType type) {
        if (EventType.RENDER_TEXT.equals(type)) {
            TextRenderInfo renderInfo = (TextRenderInfo) data;
            java.util.List<CanvasTag> tagHierarchy = renderInfo
                    .getCanvasTagHierarchy();
            for (CanvasTag tag : tagHierarchy) {
                PdfDictionary dict = tag.getProperties();
                String name = dict.get(PdfName.Name).toString();
                if (layerName.equals(name)) {
                    setFillColor(renderInfo.getGraphicsState()
                            .getFillColor());
                    setPdfFont(renderInfo.getGraphicsState().getFont());
                    super.eventOccurred(data, type);
                    break;
                }
            }
        }
    }
}
