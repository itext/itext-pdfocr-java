package com.itextpdf.pdfocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OcrEngineProperties {

    /**
     * List of languages required for ocr for provided images.
     */
    private List<String> languages = Collections.<String>emptyList();

    /**
     * Creates a new {@link OcrEngineProperties} instance.
     */
    public OcrEngineProperties() {
    }

    /**
     * Creates a new {@link OcrEngineProperties} instance
     * based on another {@link OcrEngineProperties} instance (copy
     * constructor).
     *
     * @param other the other {@link OcrEngineProperties} instance
     */
    public OcrEngineProperties(OcrEngineProperties other) {
        this.languages = other.languages;
    }

    /**
     * Gets list of languages required for provided images.
     *
     * @return {@link List} of languages
     */
    public final List<String> getLanguages() {
        return new ArrayList<String>(languages);
    }

    /**
     * Sets list of languages to be recognized in provided images.
     * Consult with documentation of specific engine implementations
     * to check on which format to give the language in.
     *
     * @param requiredLanguages {@link List} of languages in string
     *                                               format
     * @return the {@link OcrEngineProperties} instance
     */
    public final OcrEngineProperties setLanguages(
            final List<String> requiredLanguages) {
        languages = Collections.<String>unmodifiableList(requiredLanguages);
        return this;
    }
}
