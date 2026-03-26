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
package com.itextpdf.pdfocr.onnx.conf.paddle.parser;

import com.itextpdf.pdfocr.onnx.conf.paddle.model.DetResizeForTest;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link DetResizeForTest} POJO.
 */
final class DetResizeForTestParser {
    private static final String IMAGE_SHAPE_KEY = "image_shape";
    private static final String KEEP_RATIO_KEY = "keep_ratio";

    private static final int[] DEFAULT_IMAGE_SHAPE = null;
    private static final boolean DEFAULT_KEEP_RATIO = false;

    private DetResizeForTestParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link DetResizeForTest} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static DetResizeForTest parse(Object yamlObj, String keyCtx) {
        // Since all fields we need are optional, we also accept null
        if (yamlObj == null) {
            return new DetResizeForTest(DEFAULT_IMAGE_SHAPE, DEFAULT_KEEP_RATIO);
        }
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        /*
         * There can be more keys in the config object, but with how the code
         * works for OCR, we only care about these two. If image_shape is
         * present it will override the configuration, that comes from the
         * global OCR config. And we don't support that at the moment...
         */
        return new DetResizeForTest(
                ParserUtil.getOrDefault(root, keyCtx, IMAGE_SHAPE_KEY, DEFAULT_IMAGE_SHAPE),
                ParserUtil.getOrDefault(root, keyCtx, KEEP_RATIO_KEY, DEFAULT_KEEP_RATIO)
        );
    }
}
