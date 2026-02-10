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
package com.itextpdf.pdfocr.onnxtr.util;

import com.itextpdf.test.ExtendedITextTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class YamlUtilTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY =
            "./src/test/resources/com/itextpdf/pdfocr/onnxtr/util/YamlUtilTest/";
    private static final String TYPES_TEST_FILE = TEST_DIRECTORY + "types.yml";

    @Test
    public void deserializeFromStreamTest() throws IOException {
        final Object yaml;
        try (final InputStream is = Files.newInputStream(Paths.get(TYPES_TEST_FILE))) {
            yaml = YamlUtil.deserializeFromStream(is);
        }
        Assertions.assertInstanceOf(Map.class, yaml);
        final Map<Object, Object> root = (Map<Object, Object>) yaml;

        // String
        Assertions.assertEquals("Hello, YAML", root.get("string_plain"));
        Assertions.assertEquals("Hello, YAML with quotes", root.get("string_quoted"));
        Assertions.assertEquals(
                "This is a multi-line\n" +
                "literal block scalar.\n" +
                "Preserves line breaks.\n",
                root.get("string_multiline")
        );
        Assertions.assertEquals(
                "This is a folded block scalar that folds newlines into spaces.\n",
                root.get("string_folded")
        );

        // Null
        Assertions.assertTrue(root.containsKey("null_implicit"));
        Assertions.assertNull(root.get("null_implicit"));
        Assertions.assertTrue(root.containsKey("null_shortcut"));
        Assertions.assertNull(root.get("null_shortcut"));

        // Boolean, should remain a string, if no explicit tag
        Assertions.assertEquals("true", root.get("boolean_true"));
        Assertions.assertEquals("false", root.get("boolean_false"));
        Assertions.assertEquals(Boolean.TRUE, root.get("boolean_explicit_true"));
        Assertions.assertEquals(Boolean.FALSE, root.get("boolean_explicit_false"));

        // Int, should remain a string, if no explicit tag
        Assertions.assertEquals("42", root.get("int_decimal"));
        Assertions.assertEquals("0o52", root.get("int_octal"));
        Assertions.assertEquals("0x2A", root.get("int_hexadecimal"));
        Assertions.assertEquals(42, root.get("int_explicit"));

        // Float, should remain a string, if no explicit tag
        Assertions.assertEquals("3.14159", root.get("float_plain"));
        Assertions.assertEquals("1.2e+3", root.get("float_exponent"));
        Assertions.assertEquals("-.inf", root.get("float_negative_inf"));
        Assertions.assertEquals(".inf", root.get("float_positive_inf"));
        Assertions.assertEquals(".nan", root.get("float_nan"));
        Assertions.assertEquals(3.0, root.get("float_explicit"));

        // Sequence
        assertObjectCollectionEquals(
                Arrays.asList((Object)"red", "green", "blue"),
                root.get("sequence_inline")
        );
        assertObjectCollectionEquals(
                Arrays.asList((Object)"apple", "banana", "cherry"),
                root.get("sequence_block")
        );

        // Mapping (inline)
        {
            final Object obj = root.get("mapping_inline");
            Assertions.assertInstanceOf(Map.class, obj);
            final Map<Object, Object> mapping = (Map<Object, Object>) obj;
            Assertions.assertEquals(2, mapping.size());
            Assertions.assertEquals("Alice", mapping.get("name"));
            Assertions.assertEquals("30", mapping.get("age"));
        }

        // Mapping (block)
        {
            final Object obj = root.get("mapping_block");
            Assertions.assertInstanceOf(Map.class, obj);
            final Map<Object, Object> mapping = (Map<Object, Object>) obj;
            Assertions.assertEquals(3, mapping.size());
            Assertions.assertEquals("Bob", mapping.get("name"));
            Assertions.assertEquals("25", mapping.get("age"));
            Assertions.assertEquals("true", mapping.get("active"));
        }
    }

    @Test
    public void objToMappingTest() {
        Assertions.assertNull(YamlUtil.objToMapping("not map"));
        Assertions.assertNull(YamlUtil.objToMapping(null));
        Assertions.assertNull(YamlUtil.objToMapping(Boolean.TRUE));
        Assertions.assertNull(YamlUtil.objToMapping(3));
        Assertions.assertNull(YamlUtil.objToMapping(3.14));
        final Map<Object, Object> map = new HashMap<Object, Object>();
        Assertions.assertEquals(map, YamlUtil.objToMapping(map));
        Assertions.assertNull(YamlUtil.objToMapping(new ArrayList<Object>()));
    }

    @Test
    public void mappingElementsTest() {
        final Map<Integer, ArrayList<String>> map = new HashMap<>();
        ArrayList<String> array = new ArrayList<>(Arrays.asList("one", "two", "three"));
        map.put(1, array);
        map.put(2, new ArrayList<>());
        Map<Object, Object> newMap = YamlUtil.objToMapping(map);
        Assertions.assertEquals(2, newMap.size());
        Assertions.assertTrue(map.keySet().contains(1));
        Assertions.assertEquals(array, newMap.get(1));
        Assertions.assertTrue(map.keySet().contains(2));
        Assertions.assertEquals(new ArrayList<>(), newMap.get(2));
    }

    @Test
    public void objToSequenceTest() {
        Assertions.assertNull(YamlUtil.objToSequence("not seq"));
        Assertions.assertNull(YamlUtil.objToSequence(null));
        Assertions.assertNull(YamlUtil.objToSequence(Boolean.TRUE));
        Assertions.assertNull(YamlUtil.objToSequence(3));
        Assertions.assertNull(YamlUtil.objToSequence(3.14));
        Assertions.assertNull(YamlUtil.objToSequence(new HashMap<Object, Object>()));
        final List<Object> seq = new ArrayList<Object>();
        Assertions.assertEquals(seq, YamlUtil.objToSequence(seq));
    }

    @Test
    public void sequenceElementsTest() {
        final List<Integer> seq = new ArrayList<>(Arrays.asList(1, 2, 3));
        Collection<Object> newSeq = YamlUtil.objToSequence(seq);
        Assertions.assertTrue(newSeq.contains(1));
        Assertions.assertTrue(newSeq.contains(2));
        Assertions.assertTrue(newSeq.contains(3));
    }

    @Test
    public void objToStringTest() {
        final String str = "3.14";
        Assertions.assertEquals(str, YamlUtil.objToString(str));
        Assertions.assertNull(YamlUtil.objToString(null));
        Assertions.assertNull(YamlUtil.objToString(Boolean.TRUE));
        Assertions.assertNull(YamlUtil.objToString(3));
        Assertions.assertNull(YamlUtil.objToString(3.14));
        Assertions.assertNull(YamlUtil.objToString(new HashMap<Object, Object>()));
        Assertions.assertNull(YamlUtil.objToString(new ArrayList<Object>()));
    }

    @Test
    public void objToBoolTest() {
        Assertions.assertNull(YamlUtil.objToBool("not bool"));
        Assertions.assertNull(YamlUtil.objToBool(null));
        Assertions.assertEquals(Boolean.TRUE, YamlUtil.objToBool(Boolean.TRUE));
        Assertions.assertNull(YamlUtil.objToBool(3));
        Assertions.assertNull(YamlUtil.objToBool(3.14));
        Assertions.assertNull(YamlUtil.objToBool(new HashMap<Object, Object>()));
        Assertions.assertNull(YamlUtil.objToBool(new ArrayList<Object>()));

        // Implicit case
        Assertions.assertEquals(Boolean.TRUE, YamlUtil.objToBool("true"));
        Assertions.assertEquals(Boolean.FALSE, YamlUtil.objToBool("false"));
    }

    @Test
    public void objToIntTest() {
        Assertions.assertNull(YamlUtil.objToInt("not int"));
        Assertions.assertNull(YamlUtil.objToInt(null));
        Assertions.assertNull(YamlUtil.objToInt(Boolean.TRUE));
        final Integer i = 3;
        Assertions.assertEquals(i, YamlUtil.objToInt(i));
        Assertions.assertNull(YamlUtil.objToInt(3.14));
        Assertions.assertNull(YamlUtil.objToInt(new HashMap<Object, Object>()));
        Assertions.assertNull(YamlUtil.objToInt(new ArrayList<Object>()));

        // Implicit case
        Assertions.assertEquals(-42, YamlUtil.objToInt("-42"));
        Assertions.assertEquals(42, YamlUtil.objToInt("42"));
    }

    @Test
    public void objToFloatTest() {
        Assertions.assertNull(YamlUtil.objToFloat("not float"));
        Assertions.assertNull(YamlUtil.objToFloat(null));
        Assertions.assertNull(YamlUtil.objToFloat(Boolean.TRUE));
        Assertions.assertNull(YamlUtil.objToFloat(3));
        final Double f = 3.14;
        Assertions.assertEquals(f, YamlUtil.objToFloat(f));
        Assertions.assertNull(YamlUtil.objToFloat(new HashMap<Object, Object>()));
        Assertions.assertNull(YamlUtil.objToFloat(new ArrayList<Object>()));

        // Implicit case
        Assertions.assertEquals(-3.14, YamlUtil.objToFloat("-3.14"));
        Assertions.assertEquals(3.14, YamlUtil.objToFloat("3.14"));
        Assertions.assertEquals(-1200., YamlUtil.objToFloat("-1.2e+3"));
        Assertions.assertEquals(1200., YamlUtil.objToFloat("1.2e+3"));
        Assertions.assertEquals(-42., YamlUtil.objToFloat("-42"));
        Assertions.assertEquals(42., YamlUtil.objToFloat("42"));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, YamlUtil.objToFloat("-.inf"));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, YamlUtil.objToFloat(".inf"));
        Assertions.assertTrue(Double.isNaN((double)YamlUtil.objToFloat(".nan")));
    }

    private static void assertObjectCollectionEquals(Collection<Object> expected, Object actual) {
        Assertions.assertInstanceOf(Collection.class, actual);
        Assertions.assertArrayEquals(expected.toArray(), ((Collection<Object>) actual).toArray());
    }
}