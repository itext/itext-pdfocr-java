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

import com.itextpdf.pdfocr.onnx.conf.paddle.model.InferenceConfig;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.util.YamlUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config files into
 * a {@link InferenceConfig} POJO.
 */
public final class InferenceConfigParser {
    private static final String PRE_PROCESS_KEY = "PreProcess";
    private static final String POST_PROCESS_KEY = "PostProcess";

    private InferenceConfigParser() {
        // Static class
    }

    /**
     * Parses a PaddleOCR config file into a {@link InferenceConfig} POJO.
     *
     * @param content stream with the config file contents
     *
     * @return the parsed object
     */
    public static InferenceConfig parse(InputStream content) {
        return parse(YamlUtil.deserializeFromStream(content));
    }

    /**
     * Parses a YAML object from a PaddleOCR config file into a
     * {@link InferenceConfig} POJO.
     *
     * @param yamlObj YAML object to parse
     *
     * @return the parsed object
     */
    private static InferenceConfig parse(Object yamlObj) {
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey("<root>");
        }
        return new InferenceConfig(
                PreProcessParser.parse(root.get(PRE_PROCESS_KEY), PRE_PROCESS_KEY),
                PostProcessParser.parse(root.get(POST_PROCESS_KEY), POST_PROCESS_KEY)
        );
    }
}
