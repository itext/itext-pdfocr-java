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
package com.itextpdf.pdfocr.util;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.TextInfo;
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
    private static final double EPS = 1e-4;

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
    public void buildTextDistancedTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("Third", new Rectangle(200, 0, 100, 100)));
        textInfos.add(new TextInfo("Fourth", new Rectangle(610, 0, 100, 100)));
        textInfos.add(new TextInfo("Second", new Rectangle(100, 100, 120, 65)));
        textInfos.add(new TextInfo("First", new Rectangle(0, 200, 100, 30)));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "First\nSecond\nThird\nFourth\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void generifyLineTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("Third", new Rectangle(200, 0, 100, 25)));
        textInfos.add(new TextInfo("Fourth", new Rectangle(310, 0, 100, 50)));
        textInfos.add(new TextInfo("Second", new Rectangle(100, 0, 120, 35)));
        textInfos.add(new TextInfo("First", new Rectangle(0, 0, 100, 30)));
        textInfoMap.put(1, textInfos);
        PdfOcrTextBuilder.generifyWordBBoxesByLine(textInfoMap);
        textInfos = textInfoMap.get(1);
        Assertions.assertTrue(new Rectangle(0, 0, 100, 50).equalsWithEpsilon(textInfos.get(0).getBBoxRect()));
        Assertions.assertTrue(new Rectangle(100, 0, 120, 50).equalsWithEpsilon(textInfos.get(1).getBBoxRect()));
        Assertions.assertTrue(new Rectangle(200, 0, 100, 50).equalsWithEpsilon(textInfos.get(2).getBBoxRect()));
        Assertions.assertTrue(new Rectangle(310, 0, 100, 50).equalsWithEpsilon(textInfos.get(3).getBBoxRect()));
    }

    @Test
    public void generifyThinLineTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("-", new Point[]{new Point(525, 50), new Point(525, 26), new Point(525, 28),  new Point(525, 52)}));
        textInfoMap.put(1, textInfos);
        PdfOcrTextBuilder.generifyWordBBoxesByLine(textInfoMap);
        final Point[] points = textInfos.get(0).getTextPoints();
        Assertions.assertEquals(4, points.length);
        Assertions.assertNotNull(points[0]);
        Assertions.assertNotNull(points[1]);
        Assertions.assertNotNull(points[2]);
        Assertions.assertNotNull(points[3]);
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
        Point a = new Point(0, 0);
        Point b = new Point(0, 200);
        Point c = new Point(200, 200);
        Point d = new Point(200, 0);
        Point e = new Point(300, 0);
        Point f = new Point(300, 200);
        Point g = new Point(500, 200);
        Point h = new Point(500, 0);
        Point i = new Point(300, -400);
        Point j = new Point(300, -200);
        Point k = new Point(500, -200);
        Point l = new Point(500, -400);

        textInfos.add(new TextInfo("Third", new Point[]{g, h, e, f}));
        textInfos.add(new TextInfo("First 2", new Point[]{i, j, k, l}));
        textInfos.add(new TextInfo("Fourth", new Point[]{f, g, h, e}));
        textInfos.add(new TextInfo(" Second 1", new Point[]{h, e, f, g}));
        textInfos.add(new TextInfo("Fourth 1", new Point[]{j, k, l, i}));
        textInfos.add(new TextInfo("First ", new Point[]{a, b, c, d}));
        textInfos.add(new TextInfo("First 1", new Point[]{e, f, g, h}));
        textInfos.add(new TextInfo("Third 1", new Point[]{c, d, a, b}));
        textInfos.add(new TextInfo("Second", new Point[]{l, i, j, k}));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "First    First 1\n" +
                "First 2\n" +
                "Second        Second 1\n" +
                "Third  Third 1\n" +
                "Fourth      Fourth 1\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void generifyLineOrientationsTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        Point a = new Point(0, 400);
        Point b = new Point(0, 600);
        Point c = new Point(200, 600);
        Point d = new Point(200, 400);
        Point e = new Point(300, 400);
        Point f = new Point(300, 580);
        Point g = new Point(480, 580);
        Point h = new Point(480, 400);
        Point i = new Point(300, 0);
        Point j = new Point(300, 200);
        Point k = new Point(500, 200);
        Point l = new Point(500, 0);

        textInfos.add(new TextInfo("Third", new Point[]{g, h, e, f}));
        textInfos.add(new TextInfo("First 2", new Point[]{i, j, k, l}));
        textInfos.add(new TextInfo("Fourth", new Point[]{f, g, h, e}));
        textInfos.add(new TextInfo("Second 1", new Point[]{h, e, f, g}));
        textInfos.add(new TextInfo("Fourth 1", new Point[]{j, k, l, i}));
        textInfos.add(new TextInfo("First", new Point[]{a, b, c, d}));
        textInfos.add(new TextInfo("First 1", new Point[]{e, f, g, h}));
        textInfos.add(new TextInfo("Third 1", new Point[]{c, d, a, b}));
        textInfos.add(new TextInfo("Second", new Point[]{l, i, j, k}));
        textInfoMap.put(1, textInfos);
        PdfOcrTextBuilder.generifyWordBBoxesByLine(textInfoMap);
        for (TextInfo textInfo : textInfos) {
            Point point1 = textInfo.getTextPoints()[1];
            Point point2 = textInfo.getTextPoints()[0];
            double dx = point1.getX() - point2.getX();
            double dy = point1.getY() - point2.getY();
            Assertions.assertEquals(200, Math.sqrt(dx * dx + dy * dy), EPS);
        }
    }

    @Test
    public void intersectionsTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        Point a = new Point(0, 200);
        Point b = new Point(0, 400);
        Point c = new Point(200, 400);
        Point d = new Point(200, 200);
        Point e = new Point(190, 200);
        Point f = new Point(190, 400);
        Point g = new Point(500, 400);
        Point h = new Point(500, 200);
        Point i = new Point(300, 0);
        Point j = new Point(300, 210);
        Point k = new Point(500, 210);
        Point l = new Point(500, 0);

        textInfos.add(new TextInfo("Third", new Point[]{g, h, e, f}));
        textInfos.add(new TextInfo("Fourth", new Point[]{f, g, h, e}));
        textInfos.add(new TextInfo("Second 1", new Point[]{h, e, f, g}));
        textInfos.add(new TextInfo("Fourth 1", new Point[]{j, k, l, i}));
        textInfos.add(new TextInfo("First", new Point[]{a, b, c, d}));
        textInfos.add(new TextInfo("First 1", new Point[]{e, f, g, h}));
        textInfos.add(new TextInfo("Third 1", new Point[]{c, d, a, b}));
        textInfos.add(new TextInfo("Second", new Point[]{l, i, j, k}));
        textInfoMap.put(1, textInfos);
        String actualResult = PdfOcrTextBuilder.buildText(textInfoMap);
        String expectedResult = "FirstFirst 1\nSecondSecond 1\nThirdThird 1\nFourthFourth 1\n";
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void distPerpendicularDiffTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        Point a = new Point(0, 0);
        Point b = new Point(0, 200);
        Point c = new Point(200, 200);
        Point d = new Point(200, 0);

        Point[] bbox0 = {a, b, c, d};
        Point[] bbox90 = {d, a, b, c};
        Point[] bbox180 = {c, d, a, b};
        Point[] bbox270 = {b, c, d, a};

        textInfos.add(new TextInfo("First,", bbox0));
        textInfos.add(new TextInfo("First down", shiftPoints(bbox0, 0, -200)));
        textInfos.add(new TextInfo("First up,", shiftPoints(bbox0, 0, 200)));

        textInfos.add(new TextInfo("Second,", bbox90));
        textInfos.add(new TextInfo("Second down", shiftPoints(bbox90, 200, 0)));
        textInfos.add(new TextInfo("Second up,", shiftPoints(bbox90, -200, 0)));

        textInfos.add(new TextInfo("Third,", bbox180));
        textInfos.add(new TextInfo("Third down", shiftPoints(bbox180, 0, 200)));
        textInfos.add(new TextInfo("Third up,", shiftPoints(bbox180, 0, -200)));

        textInfos.add(new TextInfo("Fourth,", bbox270));
        textInfos.add(new TextInfo("Fourth down", shiftPoints(bbox270, -200, 0)));
        textInfos.add(new TextInfo("Fourth up,", shiftPoints(bbox270, 200, 0)));

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
        Point a = new Point(0, 200);
        Point b = new Point(0, 0);
        Point c = new Point(200, 0);
        Point d = new Point(200, 200);

        Point[] bbox0 = {a, b, c, d};
        Point[] bbox90 = {d, a, b, c};
        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox0),
                new TextInfo("Two", bbox90)));
    }

    @Test
    public void isInTheSameLinePositiveTest() {
        Point a = new Point(0, 200);
        Point b = new Point(0, 0);
        Point c = new Point(200, 0);
        Point d = new Point(200, 200);

        Point[] bbox0 = {a, b, c, d};
        Point[] bbox90 = {d, a, b, c};
        Point[] bbox180 = {c, d, a, b};
        Point[] bbox270 = {b, c, d, a};

        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox0),
                new TextInfo("Two", shiftPoints(bbox0, 250, 0))));

        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox90),
                new TextInfo("Two", shiftPoints(bbox90, 0, 250))));

        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox180),
                new TextInfo("Two", shiftPoints(bbox180, -250, 0))));

        Assertions.assertTrue(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox270),
                new TextInfo("Two", shiftPoints(bbox270, 0, -250))));
    }

    @Test
    public void isInTheSameLineNegativeTest() {
        Point a = new Point(0, 200);
        Point b = new Point(0, 0);
        Point c = new Point(200, 0);
        Point d = new Point(200, 200);

        Point[] bbox0 = {a, b, c, d};
        Point[] bbox90 = {d, a, b, c};
        Point[] bbox180 = {c, d, a, b};
        Point[] bbox270 = {b, c, d, a};

        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox0),
                new TextInfo("Two", shiftPoints(bbox0, 0, 250))));

        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox90),
                new TextInfo("Two", shiftPoints(bbox90, 250, 0))));

        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox180),
                new TextInfo("Two", shiftPoints(bbox180, 0, -250))));

        Assertions.assertFalse(PdfOcrTextBuilder.isInTheSameLine(
                new TextInfo("One", bbox270),
                new TextInfo("Two", shiftPoints(bbox270, -250, 0))));
    }

    @Test
    public void collectWordsIntoLinesTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("Third", getPointsFromRect(new Rectangle(240, 100, 100, 25))));
        textInfos.add(new TextInfo("Fourth", getPointsFromRect(new Rectangle(350, 100, 100, 50))));
        textInfos.add(new TextInfo("Second", getPointsFromRect(new Rectangle(110, 100, 120, 35))));
        textInfos.add(new TextInfo("First", getPointsFromRect(new Rectangle(0, 100, 100, 30))));
        textInfos.add(new TextInfo("New line", getPointsFromRect(new Rectangle(0, 0, 100, 30))));
        textInfoMap.put(1, textInfos);
        PdfOcrTextBuilder.collectWordsIntoLines(textInfoMap);
        List<TextInfo> mergedTextInfos = textInfoMap.get(1);
        Assertions.assertEquals(2, mergedTextInfos.size());
        Assertions.assertEquals("First Second Third Fourth", mergedTextInfos.get(0).getText());
        Assertions.assertEquals(50, mergedTextInfos.get(0).getBBoxRect().getHeight());
        Assertions.assertEquals("New line", mergedTextInfos.get(1).getText());
    }

    @Test
    public void correctRotationAngleTest() {
        Point a = new Point(0, 200);
        Point b = new Point(0, 400);
        Point c = new Point(200, 400);
        Point d = new Point(200, 200);

        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        List<TextInfo> textInfos = new ArrayList<>();
        textInfos.add(new TextInfo("First", new Point[]{shiftPoint(a, 5, 5), b, c, d}));
        textInfos.add(new TextInfo("Second", new Point[]{shiftPoint(d, -5, 5), a, b, c}));
        textInfos.add(new TextInfo("Third", new Point[]{shiftPoint(c, -5, -5), d, a, b}));
        textInfos.add(new TextInfo("Fourth", new Point[]{shiftPoint(b, 5, -5), c, d, a}));
        textInfoMap.put(1, textInfos);

        List<Point[]> expected = new ArrayList<>();
        expected.add(new Point[]{a, b, c, d});
        expected.add(new Point[]{d, a, b, c});
        expected.add(new Point[]{c, d, a, b});
        expected.add(new Point[]{b, c, d, a});

        PdfOcrTextBuilder.correctRotationAngle(textInfoMap);

        for (int i = 0; i < expected.size(); i++) {
            Point[] expectedPoints = expected.get(i);
            Point[] actualPoints = textInfos.get(i).getTextPoints();
            for (int j = 0; j < expectedPoints.length; j++) {
                Assertions.assertEquals(expectedPoints[j], actualPoints[j]);
            }
        }
    }

    @Test
    public void emptyResultTest() {
        Map<Integer, List<TextInfo>> textInfoMap = new HashMap<>();
        textInfoMap.put(1, new ArrayList<>());
        PdfOcrTextBuilder.generifyWordBBoxesByLine(textInfoMap);
        Assertions.assertTrue(textInfoMap.get(1).isEmpty());
    }

    private Point[] getPointsFromRect(Rectangle rectangle) {
        Point[] rectPoints = rectangle.toPointsArray();
        return new Point[]{rectPoints[0], rectPoints[3], rectPoints[2], rectPoints[1]};
    }

    private static Point[] shiftPoints(Point[] points, int dx, int dy) {
        Point[] shiftedPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            shiftedPoints[i] = shiftPoint(points[i], dx, dy);
        }
        return shiftedPoints;
    }

    private static Point shiftPoint(Point point, int dx, int dy) {
        return new Point(point.getX() + dx, point.getY() + dy);
    }
}
