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
package com.itextpdf.pdfocr.onnxtr.conf.paddle.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * POJO for the CTCLabelDecode post-processor object under a
 * {@code PostProcess} key in a config file.
 */
public class CtcLabelDecode implements PostProcess {
    /**
     * Expected name for the CTCLabelDecode post-processor.
     */
    public static final String NAME = "CTCLabelDecode";

    private final String[] characterDict;

    /**
     * Creates a new POJO for the config file object.
     *
     * @param characterDict values under the {@code character_dict} key
     */
    public CtcLabelDecode(String[] characterDict) {
        this.characterDict = Objects.requireNonNull(characterDict);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the value under the {@code character_dict} key.
     *
     * @return the value under the {@code character_dict} key
     */
    public String[] getCharacterDict() {
        return characterDict;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CtcLabelDecode that = (CtcLabelDecode) o;
        return Arrays.equals(characterDict, that.characterDict);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(characterDict);
    }
}
