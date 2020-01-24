package com.itextpdf.ocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TextInfo class.
 * <p>
 * This class describes item of text info retrieved
 * from HOCR file after parsing
 */
class TextInfo {

    /**
     * Contains word or line.
     */
    private String text;

    /**
     * Contains 4 coordinates: bbox parameters.
     */
    private List<Float> coordinates;

    /**
     * Number of page for given text.
     */
    private Integer pageNumber;

    /**
     * TextInfo Constructor.
     *
     * @param newText        String
     * @param newPageNumber  Integer
     * @param newCoordinates List<Integer>
     */
    TextInfo(final String newText, final Integer newPageNumber,
            final List<Float> newCoordinates) {
        text = newText;
        pageNumber = newPageNumber;
        coordinates = Collections.unmodifiableList(newCoordinates);
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
     * Page of the word/text.
     *
     * @return Integer
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * Page of the word/text.
     *
     * @param newPage Integer
     */
    public void setPageNumber(final Integer newPage) {
        pageNumber = newPage;
    }

    /**
     * Bbox coordinates.
     *
     * @return List<Float>
     */
    public List<Float> getCoordinates() {
        return new ArrayList<>(coordinates);
    }

    /**
     * Bbox coordinates.
     *
     * @param newCoordinates List<Float>
     */
    public void setCoordinates(final List<Float> newCoordinates) {
        coordinates = Collections.unmodifiableList(newCoordinates);
    }
}
