package com.itextpdf.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TextInfo class.
 *
 * This class describes item of text info retrieved
 * from HOCR file after parsing
 */
public class TextInfo {

    /**
     * Contains word or line.
     */
    private String text;

    /**
     * Contains 4 coordinates: bbox parameters.
     */
    private List<Float> coordinates;

    /**
     * TextInfo Constructor.
     *
     * @param newText String
     * @param newCoordinates List<Integer>
     */
    public TextInfo(final String newText, final List<Float> newCoordinates) {
        text = newText;
        coordinates = Collections.<Float>unmodifiableList(newCoordinates);
    }

    /**
     * Text element.
     *
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Text element.
     *
     * @param newText String
     */
    public void setText(final String newText) {
        text = newText;
    }

    /**
     * Bbox coordinates.
     *
     * @return List<Float>
     */
    public List<Float> getCoordinates() {
        return new ArrayList<Float>(coordinates);
    }

    /**
     * Bbox coordinates.
     *
     * @param newCoordinates List<Float>
     */
    public void setCoordinates(final List<Float> newCoordinates) {
        coordinates = Collections.<Float>unmodifiableList(newCoordinates);
    }
}
