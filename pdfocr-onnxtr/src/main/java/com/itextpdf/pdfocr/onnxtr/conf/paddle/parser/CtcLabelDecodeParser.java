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
package com.itextpdf.pdfocr.onnxtr.conf.paddle.parser;

import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.CtcLabelDecode;
import com.itextpdf.pdfocr.onnxtr.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link CtcLabelDecode} POJO.
 */
final class CtcLabelDecodeParser {
    private static final String NAME_KEY = "name";
    private static final String CHARACTER_DICT_KEY = "character_dict";

    private CtcLabelDecodeParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link CtcLabelDecode} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static CtcLabelDecode parse(Map<Object, Object> yamlObj, String keyCtx) {
        assert CtcLabelDecode.NAME.equals(YamlUtil.objToString(yamlObj.get(NAME_KEY)));

        return new CtcLabelDecode(
                ParserUtil.castToStringArray(
                        YamlUtil.objToSequence(yamlObj.get(CHARACTER_DICT_KEY)),
                        keyCtx + "." + CHARACTER_DICT_KEY
                )
        );
    }
}
