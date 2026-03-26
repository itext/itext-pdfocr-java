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

import com.itextpdf.pdfocr.onnx.conf.paddle.model.BoxType;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DbPostProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ScoreMode;
import com.itextpdf.pdfocr.onnx.util.YamlUtil;

import java.util.Map;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link DbPostProcess} POJO.
 */
final class DbPostProcessParser {
    private static final String NAME_KEY = "name";
    private static final String THRESH_KEY = "thresh";
    private static final String BOX_THRESH_KEY = "box_thresh";
    private static final String UNCLIP_RATIO_KEY = "unclip_ratio";
    private static final String MAX_CANDIDATES_KEY = "max_candidates";
    private static final String USE_DILATION_KEY = "use_dilation";
    private static final String SCORE_MODE_KEY = "score_mode";
    private static final String BOX_TYPE_KEY = "box_type";

    private static final float DEFAULT_THRESH = 0.3F;
    private static final float DEFAULT_BOX_THRESH = 0.6F;
    private static final float DEFAULT_UNCLIP_RATIO = 1.5F;
    private static final int DEFAULT_MAX_CANDIDATES = 1000;
    private static final boolean DEFAULT_USE_DILATION = false;
    private static final ScoreMode DEFAULT_SCORE_MODE = ScoreMode.FAST;
    private static final BoxType DEFAULT_BOX_TYPE = BoxType.QUAD;

    private DbPostProcessParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link DbPostProcess} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static DbPostProcess parse(Map<Object, Object> yamlObj, String keyCtx) {
        assert DbPostProcess.NAME.equals(YamlUtil.objToString(yamlObj.get(NAME_KEY)));

        return new DbPostProcess(
                ParserUtil.getOrDefault(yamlObj, keyCtx, THRESH_KEY, DEFAULT_THRESH),
                ParserUtil.getOrDefault(yamlObj, keyCtx, BOX_THRESH_KEY, DEFAULT_BOX_THRESH),
                ParserUtil.getOrDefault(yamlObj, keyCtx, UNCLIP_RATIO_KEY, DEFAULT_UNCLIP_RATIO),
                ParserUtil.getOrDefault(yamlObj, keyCtx, MAX_CANDIDATES_KEY, DEFAULT_MAX_CANDIDATES),
                ParserUtil.getOrDefault(yamlObj, keyCtx, USE_DILATION_KEY, DEFAULT_USE_DILATION),
                ParserUtil.getOrDefault(yamlObj, keyCtx, SCORE_MODE_KEY, DEFAULT_SCORE_MODE),
                ParserUtil.getOrDefault(yamlObj, keyCtx, BOX_TYPE_KEY, DEFAULT_BOX_TYPE)
        );
    }
}
