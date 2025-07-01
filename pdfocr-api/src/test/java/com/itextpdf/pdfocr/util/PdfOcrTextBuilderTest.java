/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
package com.itextpdf.pdfocr.util;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.test.ExtendedITextTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PdfOcrTextBuilderTest extends ExtendedITextTest {
    private static final String DESTINATION_DIRECTORY =
            "./target/test/resources/com/itextpdf/pdfocr/util/PdfOcrTextBuilderTest/";

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(DESTINATION_DIRECTORY);
    }

    @Test
    public void buildTextTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("Third", new Rectangle(200, 0, 100, 100)));
        textInfos.add(new TextInfo("Fourth", new Rectangle(310, 0, 100, 100)));
        textInfos.add(new TextInfo("Second", new Rectangle(100, 100, 120, 65)));
        textInfos.add(new TextInfo("First", new Rectangle(0, 200, 100, 30)));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "First\nSecond\nThird Fourth\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void pagesOrderTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        textInfoMap.put(3, Arrays.asList(new TextInfo("Third", new Rectangle(200, 0, 100, 100))));
        textInfoMap.put(2, Arrays.asList(new TextInfo("Second", new Rectangle(100, 100, 120, 65))));
        textInfoMap.put(1, Arrays.asList(new TextInfo("First", new Rectangle(0, 200, 100, 30))));
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "First\nSecond\nThird\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void orientationsTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("Third", new Rectangle(200, 0, 100, 50), TextOrientation.HORIZONTAL_ROTATED_180));
        textInfos.add(new TextInfo("Fourth", new Rectangle(300, 180, 40, 120), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfos.add(new TextInfo(" Second 1", new Rectangle(100, 140, 60, 160), TextOrientation.HORIZONTAL_ROTATED_90));
        textInfos.add(new TextInfo("Fourth 1", new Rectangle(300, 10, 40, 160), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfos.add(new TextInfo("First ", new Rectangle(0, 200, 100, 30), TextOrientation.HORIZONTAL));
        textInfos.add(new TextInfo("First 1", new Rectangle(110, 200, 140, 30), TextOrientation.HORIZONTAL));
        textInfos.add(new TextInfo("Third 1", new Rectangle(50, 0, 140, 50), TextOrientation.HORIZONTAL_ROTATED_180));
        textInfos.add(new TextInfo("Second", new Rectangle(100, 10, 60, 120), TextOrientation.HORIZONTAL_ROTATED_90));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "First First 1\nSecond Second 1\nThird Third 1\nFourth Fourth 1\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void intersectionsTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("First", new Rectangle(0, 200, 100, 30), TextOrientation.HORIZONTAL));
        textInfos.add(new TextInfo("First 1", new Rectangle(70, 200, 140, 30), TextOrientation.HORIZONTAL));
        textInfos.add(new TextInfo("Second", new Rectangle(100, 10, 60, 120), TextOrientation.HORIZONTAL_ROTATED_90));
        textInfos.add(new TextInfo("Second 1", new Rectangle(100, 100, 60, 160), TextOrientation.HORIZONTAL_ROTATED_90));
        textInfos.add(new TextInfo("Third", new Rectangle(200, 0, 100, 50), TextOrientation.HORIZONTAL_ROTATED_180));
        textInfos.add(new TextInfo("Third 1", new Rectangle(80, 0, 140, 50), TextOrientation.HORIZONTAL_ROTATED_180));
        textInfos.add(new TextInfo("Fourth", new Rectangle(300, 180, 40, 120), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfos.add(new TextInfo("Fourth 1", new Rectangle(300, 50, 40, 160), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "FirstFirst 1\nSecondSecond 1\nThirdThird 1\nFourthFourth 1\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void distPerpendicularDiffTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("First,", new Rectangle(0, 200, 100, 30), TextOrientation.HORIZONTAL));
        textInfos.add(new TextInfo("First down", new Rectangle(110, 180, 140, 30), TextOrientation.HORIZONTAL));
        textInfos.add(new TextInfo("First up,", new Rectangle(110, 220, 140, 30), TextOrientation.HORIZONTAL));

        textInfos.add(new TextInfo("Second,", new Rectangle(100, 10, 60, 120), TextOrientation.HORIZONTAL_ROTATED_90));
        textInfos.add(new TextInfo("Second down", new Rectangle(140, 140, 60, 160), TextOrientation.HORIZONTAL_ROTATED_90));
        textInfos.add(new TextInfo("Second up,", new Rectangle(60, 140, 60, 160), TextOrientation.HORIZONTAL_ROTATED_90));

        textInfos.add(new TextInfo("Third,", new Rectangle(200, 0, 100, 50), TextOrientation.HORIZONTAL_ROTATED_180));
        textInfos.add(new TextInfo("Third down", new Rectangle(50, 30, 140, 50), TextOrientation.HORIZONTAL_ROTATED_180));
        textInfos.add(new TextInfo("Third up,", new Rectangle(50, -30, 140, 50), TextOrientation.HORIZONTAL_ROTATED_180));

        textInfos.add(new TextInfo("Fourth,", new Rectangle(300, 180, 40, 120), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfos.add(new TextInfo("Fourth down", new Rectangle(270, 10, 40, 160), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfos.add(new TextInfo("Fourth up,", new Rectangle(330, 10, 40, 160), TextOrientation.HORIZONTAL_ROTATED_270));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "First up,\nFirst,\nFirst down\n" +
                "Second up,\nSecond,\nSecond down\n" +
                "Third up,\nThird,\nThird down\n" +
                "Fourth up,\nFourth,\nFourth down\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void isInTheSameLineDifferentOrientationsTest() {
        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(0, 200, 100, 30), TextOrientation.HORIZONTAL),
                new TextInfo("Two", new Rectangle(0, 200, 100, 30), TextOrientation.HORIZONTAL_ROTATED_90)));
    }

    @Test
    public void isInTheSameLinePositiveTest() {
        // Compare y
        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(0, 186, 100, 60)),
                new TextInfo("Two", new Rectangle(110, 200, 100, 30))));

        // Compare x
        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(300, 110, 20, 100), TextOrientation.HORIZONTAL_ROTATED_270),
                new TextInfo("Two", new Rectangle(291, 0, 40, 100), TextOrientation.HORIZONTAL_ROTATED_270)));

        // Compare y + h
        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(110, 0, 100, 70), TextOrientation.HORIZONTAL_ROTATED_180),
                new TextInfo("Two", new Rectangle(0, 30, 100, 50), TextOrientation.HORIZONTAL_ROTATED_180)));

        // Compare x + w
        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(100, 0, 30, 100), TextOrientation.HORIZONTAL_ROTATED_90),
                new TextInfo("Two", new Rectangle(111, 110, 25, 100), TextOrientation.HORIZONTAL_ROTATED_90)));
    }

    @Test
    public void isInTheSameLineNegativeTest() {
        // Compare y
        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(0, 180, 100, 30)),
                new TextInfo("Two", new Rectangle(110, 200, 100, 60))));

        // Compare x
        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(300, 110, 40, 100), TextOrientation.HORIZONTAL_ROTATED_270),
                new TextInfo("Two", new Rectangle(285, 0, 20, 100), TextOrientation.HORIZONTAL_ROTATED_270)));

        // Compare y + h
        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(110, 15, 100, 70), TextOrientation.HORIZONTAL_ROTATED_180),
                new TextInfo("Two", new Rectangle(0, 0, 100, 50), TextOrientation.HORIZONTAL_ROTATED_180)));

        // Compare x + w
        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", new Rectangle(110, 0, 30, 100), TextOrientation.HORIZONTAL_ROTATED_90),
                new TextInfo("Two", new Rectangle(100, 110, 25, 100), TextOrientation.HORIZONTAL_ROTATED_90)));
    }
}
