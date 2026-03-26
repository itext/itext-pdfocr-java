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

import com.itextpdf.pdfocr.onnx.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link DecodeImage} POJO.
 */
final class DecodeImageParser {
    private static final String CHANNEL_FIRST_KEY = "channel_first";
    private static final String IMG_MODE_KEY = "img_mode";

    private static final boolean DEFAULT_CHANNEL_FIRST = false;
    private static final ImgMode DEFAULT_IMG_MODE = ImgMode.BGR;

    private DecodeImageParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link DecodeImage} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static DecodeImage parse(Object yamlObj, String keyCtx) {
        // Since all fields we need are optional, we also accept null
        if (yamlObj == null) {
            return new DecodeImage(DEFAULT_CHANNEL_FIRST, DEFAULT_IMG_MODE);
        }
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        return new DecodeImage(
                ParserUtil.getOrDefault(root, keyCtx, CHANNEL_FIRST_KEY, DEFAULT_CHANNEL_FIRST),
                ParserUtil.getOrDefault(root, keyCtx, IMG_MODE_KEY, DEFAULT_IMG_MODE)
        );
    }
}
