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
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ScoreMode;
import com.itextpdf.pdfocr.onnx.exceptions.ConfigParserException;
import com.itextpdf.pdfocr.onnx.util.YamlUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Internal static class with extracted functions related to PaddleOCR
 * configuration file parsing.
 */
final class ParserUtil {
    private ParserUtil() {
        // Static class
    }

    /**
     * Tries to cast a YAML sequence object to an array of strings. Throws
     * a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML sequence object to cast
     * @param keyCtx config key under which the object is located
     *
     * @return resulting array of strings
     */
    static String[] castToStringArray(Collection<Object> obj, String keyCtx) {
        if (obj == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        final String[] casted = new String[obj.size()];
        int idx = 0;
        for (final Iterator<Object> it = obj.iterator(); it.hasNext(); ++idx) {
            final String elem = YamlUtil.objToString(it.next());
            if (elem == null) {
                throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + idx);
            }
            casted[idx] = elem;
        }
        return casted;
    }

    /**
     * Tries to cast a YAML sequence object to an array of integers. Throws
     * a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML sequence object to cast
     * @param keyCtx config key under which the object is located
     *
     * @return resulting array of integers
     */
    static int[] castToIntArray(Collection<Object> obj, String keyCtx) {
        if (obj == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        final int[] casted = new int[obj.size()];
        int idx = 0;
        for (final Iterator<Object> it = obj.iterator(); it.hasNext(); ++idx) {
            final Integer elem = YamlUtil.objToInt(it.next());
            if (elem == null) {
                throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + idx);
            }
            casted[idx] = (int) elem;
        }
        return casted;
    }

    /**
     * Tries to cast a YAML sequence object to an array of floats. Throws
     * a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML sequence object to cast
     * @param keyCtx config key under which the object is located
     *
     * @return resulting array of floats
     */
    static float[] castToFloatArray(Collection<Object> obj, String keyCtx) {
        if (obj == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx);
        }
        final float[] casted = new float[obj.size()];
        int idx = 0;
        for (final Iterator<Object> it = obj.iterator(); it.hasNext(); ++idx) {
            final Double elem = YamlUtil.objToFloat(it.next());
            if (elem == null) {
                throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + idx);
            }
            casted[idx] = (float) elem.floatValue();
        }
        return casted;
    }

    /**
     * Tries to get a float value from a YAML mapping object under the
     * specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static float getOrDefault(Map<Object, Object> obj, String keyCtx, String key, float defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        final Double casted = YamlUtil.objToFloat(value);
        if (casted == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + key);
        }
        return (float) casted.floatValue();
    }

    /**
     * Tries to get a float array value from a YAML mapping object under the
     * specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static float[] getOrDefault(Map<Object, Object> obj, String keyCtx, String key, float[] defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        return castToFloatArray(YamlUtil.objToSequence(value), keyCtx + "." + key);
    }

    /**
     * Tries to get an integer value from a YAML mapping object under the
     * specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static int getOrDefault(Map<Object, Object> obj, String keyCtx, String key, int defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        final Integer casted = YamlUtil.objToInt(value);
        if (casted == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + key);
        }
        return (int) casted;
    }

    /**
     * Tries to get an integer array value from a YAML mapping object under
     * the specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static int[] getOrDefault(Map<Object, Object> obj, String keyCtx, String key, int[] defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        return castToIntArray(YamlUtil.objToSequence(value), keyCtx + "." + key);
    }

    /**
     * Tries to get a boolean value from a YAML mapping object under the
     * specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static boolean getOrDefault(Map<Object, Object> obj, String keyCtx, String key, boolean defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        final Boolean casted = YamlUtil.objToBool(value);
        if (casted == null) {
            throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + key);
        }
        return (boolean) casted;
    }

    /**
     * Tries to get a {@link ScoreMode} value from a YAML mapping object
     * under the specified key. If key is not present, returns
     * {@code defaultValue}. Throws a {@link ConfigParserException} exception
     * on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static ScoreMode getOrDefault(Map<Object, Object> obj, String keyCtx, String key, ScoreMode defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        final String casted = YamlUtil.objToString(value);
        if ("fast".equals(casted)) {
            return ScoreMode.FAST;
        }
        if ("slow".equals(casted)) {
            return ScoreMode.SLOW;
        }
        throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + key);
    }

    /**
     * Tries to get a {@link BoxType} value from a YAML mapping object under
     * the specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static BoxType getOrDefault(Map<Object, Object> obj, String keyCtx, String key, BoxType defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        final String casted = YamlUtil.objToString(value);
        if ("quad".equals(casted)) {
            return BoxType.QUAD;
        }
        if ("poly".equals(casted)) {
            return BoxType.POLY;
        }
        throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + key);
    }

    /**
     * Tries to get an {@link ImgMode} value from a YAML mapping object under
     * the specified key. If key is not present, returns {@code defaultValue}.
     * Throws a {@link ConfigParserException} exception on error.
     *
     * @param obj YAML mapping object to get the value from
     * @param keyCtx config key under which the mapping object is located
     * @param key key to use for value extraction
     * @param defaultValue value to return if value is missing
     *
     * @return value from the mapping object, if present, or {@code defaultValue}
     */
    static ImgMode getOrDefault(Map<Object, Object> obj, String keyCtx, String key, ImgMode defaultValue) {
        final Object value = obj.get(key);
        if (value == null && !obj.containsKey(key)) {
            return defaultValue;
        }
        final String casted = YamlUtil.objToString(value);
        if ("GRAY".equals(casted)) {
            return ImgMode.GRAY;
        }
        if ("RGB".equals(casted)) {
            return ImgMode.RGB;
        }
        if ("BGR".equals(casted)) {
            return ImgMode.BGR;
        }
        throw ConfigParserException.unexpectedValueForKey(keyCtx + "." + key);
    }
}
