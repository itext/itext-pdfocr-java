package com.itextpdf.pdfocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class describes the way text info retrieved from HOCR file
 * is structured.
 */
public class TextInfo {

    /**
     * Contains any text.
     */
    private String text;

    /**
     * Contains 4 float coordinates: bbox parameters.
     */
    private List<Float> coordinates;

    /**
     * Creates a new {@link TextInfo} instance.
     */
    public TextInfo() {
        text = null;
        coordinates = Collections.<Float>emptyList();
    }

    /**
     * Creates a new {@link TextInfo} instance.
     *
     * @param newText any text
     * @param newCoordinates {@link java.util.List} of bbox parameters
     */
    public TextInfo(final String newText, final List<Float> newCoordinates) {
        text = newText;
        coordinates = Collections.<Float>unmodifiableList(newCoordinates);
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
    public List<Float> getCoordinates() {
        return new ArrayList<Float>(coordinates);
    }

    /**
     * Sets bbox coordinates.
     *
     * @param newCoordinates {@link java.util.List} of bbox parameters
     */
    public void setCoordinates(final List<Float> newCoordinates) {
        coordinates = Collections.<Float>unmodifiableList(newCoordinates);
    }
}
