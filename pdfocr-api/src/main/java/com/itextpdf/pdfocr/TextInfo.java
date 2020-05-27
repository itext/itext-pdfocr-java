package com.itextpdf.pdfocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * Contains 4 float coordinates: bbox parameters.
     */
    private List<Float> bbox;

    /**
     * Creates a new {@link TextInfo} instance.
     */
    public TextInfo() {
        text = null;
        bbox = Collections.<Float>emptyList();
    }

    /**
     * Creates a new {@link TextInfo} instance.
     *
     * @param text any text
     * @param bbox {@link java.util.List} of bbox parameters
     */
    public TextInfo(final String text, final List<Float> bbox) {
        this.text = text;
        this.bbox = Collections.<Float>unmodifiableList(bbox);
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
     * @return {@link java.util.List} of bbox parameters
     */
    public List<Float> getBbox() {
        return new ArrayList<Float>(bbox);
    }

    /**
     * Sets bbox coordinates.
     *
     * @param bbox {@link java.util.List} of bbox parameters
     */
    public void setBbox(final List<Float> bbox) {
        this.bbox = Collections.<Float>unmodifiableList(bbox);
    }
}
