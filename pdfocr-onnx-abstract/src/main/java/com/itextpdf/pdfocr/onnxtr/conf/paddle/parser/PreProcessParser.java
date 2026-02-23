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

import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DetResizeForTest;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.NormalizeImage;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.PreProcess;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.RecResizeImg;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.TransformOp;
import com.itextpdf.pdfocr.onnxtr.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnxtr.util.YamlUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Static class with functions for parsing PaddleOCR YAML config objects into
 * a {@link PreProcess} POJO.
 */
final class PreProcessParser {
    private static final String TRANSFORM_OPS_KEY = "transform_ops";
    private static final Set<String> IGNORED_TRANSFORM_OPS = new HashSet<>(Arrays.asList(
            "DetLabelEncode",
            "KeepKeys",
            "MultiLabelEncode",
            "ToCHWImage"
    ));

    private PreProcessParser() {
        // Static class
    }

    /**
     * Parses a YAML mapping from a PaddleOCR config file into a
     * {@link PreProcess} POJO.
     *
     * @param yamlObj YAML mapping to parse
     * @param keyCtx config key under which the object is located
     *
     * @return the parsed object
     */
    static PreProcess parse(Object yamlObj, String keyCtx) {
        final Map<Object, Object> root = YamlUtil.objToMapping(yamlObj);
        if (root == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }

        final Set<Object> keys = new HashSet<>(root.keySet());
        keys.remove(TRANSFORM_OPS_KEY);
        if (!keys.isEmpty()) {
            final String key = keys.iterator().next().toString();
            throw ConfigParserException.unexpectedKey(keyCtx + "." + key);
        }

        final Collection<Object> transformOps = YamlUtil.objToSequence(root.get(TRANSFORM_OPS_KEY));
        if (transformOps == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + TRANSFORM_OPS_KEY);
        }

        final List<TransformOp> parsedOps = new ArrayList<>(transformOps.size());
        int idx = 0;
        for (final Iterator<Object> it = transformOps.iterator(); it.hasNext(); ++idx) {
            final String entryKeyCtx = keyCtx + "." + TRANSFORM_OPS_KEY + "." + idx;
            final Map<Object, Object> op = YamlUtil.objToMapping(it.next());
            if (op == null || op.size() != 1) {
                throw ConfigParserException.unexpectedValueForKey(entryKeyCtx);
            }

            final Entry<Object, Object> opEntry = op.entrySet().iterator().next();
            final String opKey = YamlUtil.objToString(opEntry.getKey());
            if (DecodeImage.WRAPPING_KEY.equals(opKey)) {
                parsedOps.add(DecodeImageParser.parse(
                        opEntry.getValue(), entryKeyCtx + "." + DecodeImage.WRAPPING_KEY
                ));
            } else if (DetResizeForTest.WRAPPING_KEY.equals(opKey)) {
                parsedOps.add(DetResizeForTestParser.parse(
                        opEntry.getValue(), entryKeyCtx + "." + DetResizeForTest.WRAPPING_KEY
                ));
            } else if (NormalizeImage.WRAPPING_KEY.equals(opKey)) {
                parsedOps.add(NormalizeImageParser.parse(
                        opEntry.getValue(), entryKeyCtx + "." + NormalizeImage.WRAPPING_KEY
                ));
            } else if (RecResizeImg.WRAPPING_KEY.equals(opKey)) {
                parsedOps.add(RecResizeImgParser.parse(
                        opEntry.getValue(), entryKeyCtx + "." + RecResizeImg.WRAPPING_KEY
                ));
            } else if (opKey == null || !IGNORED_TRANSFORM_OPS.contains(opKey)) {
                throw ConfigParserException.unexpectedValueForKey(entryKeyCtx);
            }
            // Else it is an ignored op and we do nothing
        }
        return new PreProcess(parsedOps.toArray(new TransformOp[0]));
    }
}
