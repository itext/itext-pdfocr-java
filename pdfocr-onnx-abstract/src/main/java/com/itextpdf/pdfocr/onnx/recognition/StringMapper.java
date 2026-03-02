/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx.recognition;

import com.itextpdf.pdfocr.onnx.IOutputLabelMapper;
import java.util.Arrays;

import java.util.Objects;

/**
 * Look-up table for mapping text recognition model results to strings.
 *
 * <p>
 * If you only need to map indices to single UTF-16 code units, then consider
 * using {@link Vocabulary} instead, as it is much more memory efficient.
 */
public class StringMapper implements IOutputLabelMapper<String> {
    private final String[] lookUpTable;

    /**
     * Creates a new string mapper based on a look-up string. Each code point
     * is mapped to an index.
     *
     * @param lookUpString look-up string, that will be used to build a look-up table
     */
    public StringMapper(String lookUpString) {
        Objects.requireNonNull(lookUpString);

        this.lookUpTable = lookUpString
                .codePoints()
                .mapToObj(cp -> new String(new int[] {cp}, 0, 1))
                .toArray(String[]::new);
    }

    /**
     * Creates a new string mapper based on a look-up table.
     *
     * @param lookUpTable look-up table to be used in the string mapper
     */
    public StringMapper(String[] lookUpTable) {
        Objects.requireNonNull(lookUpTable);

        this.lookUpTable = (String[]) lookUpTable.clone();
    }

    /**
     * Returns the size of the string mapper.
     *
     * @return the size of the string mapper
     */
    public int size() {
        return lookUpTable.length;
    }

    /**
     * Returns character, which is mapped to the specified index in the lookup
     * string.
     *
     * @param index index to map
     *
     * @return mapped character
     */
    public String map(int index) {
        return lookUpTable[index];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(lookUpTable);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StringMapper that = (StringMapper) o;
        return Objects.deepEquals(lookUpTable, that.lookUpTable);
    }

    @Override
    public String toString() {
        return Arrays.toString(lookUpTable);
    }
}
