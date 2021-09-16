package com.itextpdf.pdfocr;

/**
 * Class for storing ocr processing context.
 */
public class OcrProcessContext {
    private final AbstractPdfOcrEventHelper ocrEventHelper;

    /**
     * Creates an instance of ocr process context
     *
     * @param eventHelper helper class for working with events
     */
    public OcrProcessContext(AbstractPdfOcrEventHelper eventHelper) {
        this.ocrEventHelper = eventHelper;
    }

    /**
     * Returns helper for working with events.
     *
     * @return an instance of {@link AbstractPdfOcrEventHelper}
     */
    public AbstractPdfOcrEventHelper getOcrEventHelper() {
        return ocrEventHelper;
    }
}
