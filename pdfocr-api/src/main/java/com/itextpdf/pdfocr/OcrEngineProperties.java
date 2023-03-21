/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains additional properties for ocr engine.
 */
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
