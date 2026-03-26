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

import com.itextpdf.pdfocr.onnx.conf.paddle.model.RecResizeImg;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link RecResizeImg} POJO.
 */
final class RecResizeImgParser {
    private static final String IMAGE_SHAPE_KEY = "image_shape";

    private static final int[] DEFAULT_IMAGE_SHAPE = new int[]{3, 48, 320};

    private static final int EXPECTED_IMAGE_SHAPE_LENGTH = 3;

    private RecResizeImgParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link RecResizeImg} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static RecResizeImg parse(Object yamlObj, String keyCtx) {
        // Since all fields we need are optional, we also accept null
        if (yamlObj == null) {
            return new RecResizeImg(DEFAULT_IMAGE_SHAPE);
        }
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }

        final int[] imageShape = ParserUtil.getOrDefault(root, keyCtx, IMAGE_SHAPE_KEY, DEFAULT_IMAGE_SHAPE);
        if (imageShape.length != EXPECTED_IMAGE_SHAPE_LENGTH) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + IMAGE_SHAPE_KEY);
        }

        return new RecResizeImg(imageShape);
    }
}
