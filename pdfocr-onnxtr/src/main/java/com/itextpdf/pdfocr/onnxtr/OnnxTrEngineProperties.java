package com.itextpdf.pdfocr.onnxtr;

import java.io.File;
import java.util.List;

/**
 * Properties that are used by the {@link OnnxTrOcrEngine}.
 */
public class OnnxTrEngineProperties {

    /**
     * Creates a new {@link OnnxTrEngineProperties} instance.
     */
    public OnnxTrEngineProperties() {

    }
    /**
     * Defines the way text is retrieved and grouped from onnxtr engine output.
     * It changes the way text is selected in the result pdf document.
     * Does not affect the result of {@link com.itextpdf.pdfocr.IOcrEngine#createTxtFile(List, File)}.
     */
    private TextPositioning textPositioning;

    /**
     * Defines the way text is retrieved from ocr engine output using
     * {@link TextPositioning}.
     *
     * @return the way text is retrieved
     */
    public TextPositioning getTextPositioning() {
        return textPositioning;
    }

    /**
     * Defines the way text is retrieved from ocr engine output
     * using {@link TextPositioning}.
     *
     * @param textPositioning the way text is retrieved
     * @return the {@link OnnxTrEngineProperties} instance
     */
    public OnnxTrEngineProperties setTextPositioning(TextPositioning textPositioning) {
        this.textPositioning = textPositioning;
        return this;
    }
}
