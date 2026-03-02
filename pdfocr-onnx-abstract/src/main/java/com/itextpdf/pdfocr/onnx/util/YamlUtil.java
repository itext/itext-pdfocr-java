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
package com.itextpdf.pdfocr.onnx.util;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.CoreScalarResolver;
import org.snakeyaml.engine.v2.resolver.FailsafeScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.schema.CoreSchema;

/**
 * Functions for working with YAML documents.
 */
public final class YamlUtil {
    private YamlUtil() {
        // Static class
    }

    /**
     * Deserializes a content stream, which contains a single YAML document.
     *
     * <p>
     * This method returns a regular object. To get access to concrete types
     * in the document, use the {@code objTo*} family of functions from this
     * class.
     *
     * @param content YAML document content stream to parse
     *
     * @return the parsed object
     */
    public static Object deserializeFromStream(InputStream content) {
        /*
         * We will be using our custom schema here. This means, that scalar
         * values, which do not have an explicit tag attached, will either be
         * nulls or strings. By default, it is using a JSON schema, which
         * guesses types in implicit cases. This is not what we really want,
         * and we will waste performance on all the guessing, so, instead, we
         * will just rely on objTo* methods. This will also make porting a bit
         * more predictable.
         */
        final LoadSettings settings = LoadSettings.builder()
                .setSchema(new CustomSchema())
                .build();
        final Load loader = new Load(settings);
        return loader.loadFromInputStream(content);
    }

    /**
     * Casts a parsed YAML object to a map/dictionary, if it is a mapping.
     * Otherwise, returns {@code null}.
     *
     * @param obj parsed YAML object to cast
     *
     * @return mapping or {@code null}
     */
    public static Map<Object, Object> objToMapping(Object obj) {
        if (obj instanceof Map) {
            return (Map<Object, Object>) obj;
        }
        return null;
    }

    /**
     * Casts a parsed YAML object to a collection, if it is a sequence.
     * Otherwise, returns {@code null}.
     *
     * @param obj parsed YAML object to cast
     *
     * @return sequence or {@code null}
     */
    public static Collection<Object> objToSequence(Object obj) {
        if (obj instanceof Collection) {
            return (Collection<Object>) obj;
        }
        return null;
    }

    /**
     * Casts a parsed YAML object to a string, if it is a string.
     * Otherwise, returns {@code null}.
     *
     * <p>
     * If the object in the YAML document had an explicit type tag, which is
     * not str, then this method won't do any conversions and will just return
     * {@code null}.
     *
     * @param obj parsed YAML object to cast
     *
     * @return string or {@code null}
     */
    public static String objToString(Object obj) {
        // With our schema, this should be enough. In case it is explicitly
        // not a string, then we will do no conversions
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    /**
     * Casts a parsed YAML object to a boolean, if it is a bool.
     * Otherwise, returns {@code null}.
     *
     * <p>
     * If the object in the YAML document had an explicit type tag, which is
     * not bool, then this method won't do any conversions and will just
     * return {@code null}.
     *
     * @param obj parsed YAML object to cast
     *
     * @return boolean or {@code null}
     */
    public static Boolean objToBool(Object obj) {
        // In case there was an explicit tag, it will already be Boolean
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        // In the implicit case we will try to parse the string
        if ("false".equals(obj)) {
            return Boolean.FALSE;
        }
        if ("true".equals(obj)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Casts a parsed YAML object to an int32, if it is an int.
     * Otherwise, returns {@code null}.
     *
     * <p>
     * If the object in the YAML document had an explicit type tag, which is
     * not int, then this method won't do any conversions and will just return
     * {@code null}.
     *
     * <p>
     * If the value does not fit into int32, then this method will also return
     * {@code null}.
     *
     * @param obj parsed YAML object to cast
     *
     * @return int32 or {@code null}
     */
    public static Integer objToInt(Object obj) {
        // We will only work with int32 here, if it only fit into int64 or
        // bigint, then we will assume "failure"
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        // In the implicit case we will try to parse the string
        if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (RuntimeException ignored) {
                // Empty
            }
        }
        return null;
    }

    /**
     * Casts a parsed YAML object to a double, if it is a float.
     * Otherwise, returns {@code null}.
     *
     * <p>
     * If the object in the YAML document had an explicit type tag, which is
     * not float, then this method won't do any conversions and will just
     * return {@code null}.
     *
     * <p>
     * If the value does not fit into int32, then this method will also return
     * {@code null}.
     *
     * @param obj parsed YAML object to cast
     *
     * @return double or {@code null}
     */
    public static Double objToFloat(Object obj) {
        // Explicit tag case
        if (obj instanceof Double) {
            return (Double) obj;
        }
        // In the implicit case we will try to parse the string
        if (obj instanceof String) {
            final String s = (String) obj;
            switch (s) {
                case "-.inf":
                    return Double.NEGATIVE_INFINITY;
                case ".inf":
                    return Double.POSITIVE_INFINITY;
                case ".nan":
                    return Double.NaN;
                default:
                    try {
                        return Double.valueOf(s);
                    } catch (RuntimeException ignored) {
                        // Empty
                    }
            }
        }
        return null;
    }

    /**
     * This is a custom YAML schema, which acts the same as {@link CoreSchema}
     * for values with explicit tags, but if there are no tags present, it
     * implicitly guesses only {@code null} and handles everything else as a
     * string.
     *
     * <p>
     * This makes the behavior in Java to be much closer to that in YamlDotNet.
     * This will also allow us to do proper type checking and not lose
     * information, if there is a real number in the document, that would get
     * implicitly parsed to Double, which will lose its string representation.
     */
    private static final class CustomSchema extends CoreSchema {
        private final ScalarResolver scalarResolver = new CustomResolver();

        /**
         * Creates new {@link CustomSchema} instance.
         */
        public CustomSchema() {
            // Empty constructor.
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ScalarResolver getScalarResolver() {
            return scalarResolver;
        }
    }

    /**
     * Custom implicit type resolver, which only handles {@code null}.
     */
    private static final class CustomResolver extends FailsafeScalarResolver {
        /**
         * Creates new {@link CustomResolver} instance.
         */
        public CustomResolver() {
            // Empty constructor.
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void addImplicitResolvers() {
            super.addImplicitResolvers();
            addImplicitResolver(Tag.NULL, CoreScalarResolver.NULL, "~nN\0");
        }
    }
}
