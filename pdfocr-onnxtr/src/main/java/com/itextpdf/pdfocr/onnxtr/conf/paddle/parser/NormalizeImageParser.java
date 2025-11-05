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

import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.NormalizeImage;
import com.itextpdf.pdfocr.onnxtr.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnxtr.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link NormalizeImage} POJO.
 */
final class NormalizeImageParser {
    private static final String MEAN_KEY = "mean";
    private static final String STD_KEY = "std";

    private static final float[] DEFAULT_MEAN = new float[]{0.485F, 0.456F, 0.406F};
    private static final float[] DEFAULT_STD = new float[]{0.229F, 0.224F, 0.225F};

    private NormalizeImageParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link NormalizeImage} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static NormalizeImage parse(Object yamlObj, String keyCtx) {
        // Since all fields we need are optional, we also accept null
        if (yamlObj == null) {
            return new NormalizeImage(DEFAULT_MEAN, DEFAULT_STD);
        }
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        return new NormalizeImage(
                ParserUtil.getOrDefault(root, keyCtx, MEAN_KEY, DEFAULT_MEAN),
                ParserUtil.getOrDefault(root, keyCtx, STD_KEY, DEFAULT_STD)
        );
    }
}
