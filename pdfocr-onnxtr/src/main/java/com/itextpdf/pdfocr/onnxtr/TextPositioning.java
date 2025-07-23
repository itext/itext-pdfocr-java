package com.itextpdf.pdfocr.onnxtr;

/**
 * Enumeration of the possible types of text positioning.
 * It is used to combine the {@link OnnxTrOcrEngine} image OCR result text and group it by lines or by words.
 */
public enum TextPositioning {
    /**
     * Text will be grouped by lines.
     * (default value)
     */
    BY_LINES,
    /**
     * Text will be grouped by words.
     */
    BY_WORDS
}
