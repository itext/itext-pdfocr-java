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
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DbPostProcess;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.PostProcess;
import com.itextpdf.pdfocr.onnxtr.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnxtr.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link PostProcess} POJO.
 */
final class PostProcessParser {
    private static final String NAME_KEY = "name";

    private PostProcessParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link PostProcess} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static PostProcess parse(Object yamlObj, String keyCtx) {
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        final String name = YamlUtil.objToString(root.get(NAME_KEY));
        if (DbPostProcess.NAME.equals(name)) {
            return DbPostProcessParser.parse(root, keyCtx);
        }
        if (CtcLabelDecode.NAME.equals(name)) {
            return CtcLabelDecodeParser.parse(root, keyCtx);
        }
        throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + NAME_KEY);
    }
}
