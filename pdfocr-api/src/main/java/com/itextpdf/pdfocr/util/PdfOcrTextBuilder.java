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
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.TextInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to build text output from the provided image OCR result and write it to the TXT file.
 */
public final class PdfOcrTextBuilder {
    private static final float DEFAULT_INTERSECTION_THRESHOLD = 0.55F;
    private static final double DEFAULT_ANGLE_THRESHOLD = Math.toRadians(10);
    private static final double EPS = 1e-6;

    private PdfOcrTextBuilder() {
        // Private constructor will prevent the instantiation of this class directly.
    }

    /**
     * Constructs string output from the provided {@link IOcrEngine#doImageOcr} result.
     *
     * @param textInfos {@link java.util.Map} where key is {@link java.lang.Integer} representing the number of the page
     * and value is {@link java.util.List} of {@link TextInfo} elements where each {@link TextInfo}
     * element contains a word or a line and its 4 coordinates (bbox)
     *
     * @return string output of the OCR result
     */
    public static String buildText(Map<Integer, List<TextInfo>> textInfos) {
        StringBuilder outputText = new StringBuilder();
        PdfOcrTextBuilder.collectWordsIntoLines(textInfos);
        List<Integer> pages = textInfos.keySet().stream().sorted().collect(Collectors.toList());
        for (int page : pages) {
            for (TextInfo chunk : textInfos.get(page)) {
                outputText.append(chunk.getText()).append('\n');
            }
        }
        return outputText.toString();
    }

    /**
     * Sorts the provided {@link IOcrEngine#doImageOcr} result by lines and updates line bboxes to match the largest words.
     *
     * @param textInfos {@link java.util.Map} where key is {@link java.lang.Integer} representing the number of the page
     * and value is {@link java.util.List} of {@link TextInfo} elements where each {@link TextInfo}
     * element contains a word or a line and its 4 coordinates (bbox)
     */
    public static void generifyWordBBoxesByLine(Map<Integer, List<TextInfo>> textInfos) {
        PdfOcrTextBuilder.sortTextInfosByLines(textInfos);
        List<Integer> pages = textInfos.keySet().stream().sorted().collect(Collectors.toList());
        for (int page : pages) {
            List<TextInfo> line = new ArrayList<>();
            TextInfo prevChunk = null;
            for (TextInfo chunk : textInfos.get(page)) {
                if (prevChunk == null) {
                    line.add(chunk);
                } else {
                    if (isInTheSameLine(chunk, prevChunk)) {
                        line.add(chunk);
                    } else {
                        updateBBoxes(line);
                        line.clear();
                        line.add(chunk);
                    }
                }
                prevChunk = chunk;
            }
            updateBBoxes(line);
            line.clear();
        }
    }

    /**
     * Merges the provided {@link IOcrEngine#doImageOcr} result into lines and
     * updates line bounding boxes to match the largest words.
     *
     * @param textInfos {@link java.util.Map} where key is {@link java.lang.Integer} representing the number of the page
     * and value is {@link java.util.List} of {@link TextInfo} elements where each {@link TextInfo}
     * element contains a word or a line and its 4 coordinates (bbox)
     */
    public static void collectWordsIntoLines(Map<Integer, List<TextInfo>> textInfos) {
        PdfOcrTextBuilder.sortTextInfosByLines(textInfos);
        List<Integer> pages = textInfos.keySet().stream().sorted().collect(Collectors.toList());
        for (int page : pages) {
            List<TextInfo> pageLines = new ArrayList<>();
            List<TextInfo> line = new ArrayList<>();
            TextInfo prevChunk = null;
            for (TextInfo chunk : textInfos.get(page)) {
                if (prevChunk == null) {
                    line.add(chunk);
                } else {
                    if (isInTheSameLine(chunk, prevChunk)) {
                        line.add(chunk);
                    } else {
                        // Merge into one text chunk.
                        TextInfo newLine = mergeTextChunksIntoLine(line);
                        if (newLine != null) {
                            pageLines.add(newLine);
                        }
                        line.clear();
                        line.add(chunk);
                    }
                }
                prevChunk = chunk;
            }
            // Merge into one text chunk.
            TextInfo newLine = mergeTextChunksIntoLine(line);
            if (newLine != null) {
                pageLines.add(newLine);
            }
            line.clear();
            // Replace text chunks by lines.
            textInfos.put(page, pageLines);
        }
    }

    /**
     * Sorts the provided {@link IOcrEngine#doImageOcr} result by lines.
     *
     * @param textInfos {@link java.util.Map} where key is {@link java.lang.Integer} representing the number of the page
     * and value is {@link java.util.List} of {@link TextInfo} elements where each {@link TextInfo}
     * element contains a word or a line and its 4 coordinates (bbox)
     */
    public static void sortTextInfosByLines(Map<Integer, List<TextInfo>> textInfos) {
        for (Map.Entry<Integer, List<TextInfo>> entry : textInfos.entrySet()) {
            Collections.sort(entry.getValue(), new Comparator<TextInfo>() {
                @Override
                public int compare(TextInfo first, TextInfo second) {
                    // Not really needed, but just in case.
                    if (first == second) {
                        return 0;
                    }

                    double angleDiff = getAngleDiff(first, second);
                    if (Math.abs(angleDiff) > DEFAULT_ANGLE_THRESHOLD) {
                        double firstRoundAngle = roundAngle(first.getRotationAngle(), DEFAULT_ANGLE_THRESHOLD);
                        double secondRoundAngle = roundAngle(second.getRotationAngle(), DEFAULT_ANGLE_THRESHOLD);
                        return Double.compare(firstRoundAngle, secondRoundAngle);
                    }

                    BoundingBox[] boxes = BoundingBox.getNormalizedBBoxes(first, second);
                    BoundingBox box1 = boxes[0];
                    BoundingBox box2 = boxes[1];

                    if (!areIntersect(box1, box2)) {
                        double middleDistPerpendicularDiff =
                                (box2.minY + box2.getHeight() / 2) - (box1.minY + box1.getHeight() / 2);
                        return middleDistPerpendicularDiff > 0 ? 1 : -1;
                    }

                    return Double.compare(box1.minX, box2.minX) > 0 ? 1 : -1;
                }
            });
        }
    }

    /**
     * Processes all text infos to round the rotation angle to either 0, 90, 180 or 270 degrees.
     * Text bounding rectangle will be used for updated text bounding points.
     *
     * @param result OCR result to process
     *
     * @return same result, but corrected
     */
    public static Map<Integer, List<TextInfo>> correctRotationAngle(Map<Integer, List<TextInfo>> result) {
        List<Integer> pages = result.keySet().stream().sorted().collect(Collectors.toList());
        for (int page : pages) {
            List<TextInfo> textInfos = result.get(page);
            for (TextInfo textInfo : textInfos) {
                Point[] textPoints = textInfo.getBBoxRect().toPointsArray();
                double angle = roundAngle(textInfo.getRotationAngle(), Math.PI / 2);
                if (Math.abs(angle) < EPS) {
                    textInfo.setTextPoints(new Point[]{textPoints[0], textPoints[3], textPoints[2], textPoints[1]});
                } else if (Math.abs(Math.PI / 2 - angle) < EPS) {
                    textInfo.setTextPoints(new Point[]{textPoints[1], textPoints[0], textPoints[3], textPoints[2]});
                } else if (Math.abs(Math.PI - angle) < EPS) {
                    textInfo.setTextPoints(new Point[]{textPoints[2], textPoints[1], textPoints[0], textPoints[3]});
                } else if (Math.abs(3 * Math.PI / 2 - angle) < EPS) {
                    textInfo.setTextPoints(new Point[]{textPoints[3], textPoints[2], textPoints[1], textPoints[0]});
                }
            }
        }
        return result;
    }

    /**
     * Checks whether text chunks are in the same line.
     *
     * <p>
     * We consider text chunks to be in the same line if they oriented in a same way and if their intersection
     * is more than {@link #DEFAULT_INTERSECTION_THRESHOLD} of at least one of the text chunks,
     * e.g. for `one eight` intersection percentage will be 100% for `one` and less than 50% for `eight`.
     *
     * @param currentTextInfo current {@link TextInfo}
     * @param previousTextInfo previous {@link TextInfo}
     *
     * @return {@code true} if both text chunks are in the same line, {@code false} otherwise
     */
    static boolean isInTheSameLine(TextInfo currentTextInfo, TextInfo previousTextInfo) {
        double angleDiff = getAngleDiff(currentTextInfo, previousTextInfo);
        if (Math.abs(angleDiff) > DEFAULT_ANGLE_THRESHOLD) {
            return false;
        }

        BoundingBox[] boxes = BoundingBox.getNormalizedBBoxes(currentTextInfo, previousTextInfo);
        return areIntersect(boxes[0], boxes[1]);
    }

    /**
     * Updates line bounding boxes to match the largest words, so all text infos will have the same height
     * and bottom line (taking into account rotation angle).
     *
     * <p>
     * Steps:
     * 1) find average rotation angle for all text infos to update and use it as line rotation angle;
     * 2) find {@link BoundingBox} for all text infos to update. It stores new coordinates after projection
     * onto the rotated axes, so we could work with this bounding box as if text has no rotation (0 degrees);
     * 3) find the height, top and bottom coordinates for the whole line in the rotated coordinate system;
     * 4) translate lineBottomPoint back to initial (not rotated) coordinate system;
     * 5) find bottom {@link Line} vector for the bottom line of the text infos to update;
     * 6) find parallel top line by shifting bottom line to line height;
     * 7) for each text info find its bounding points via intersection of its left and rights sides
     * with found bottom and top lines.
     *
     * @param line list of text infos to update
     */
    private static void updateBBoxes(List<TextInfo> line) {
        if (line.isEmpty()) {
            return;
        }

        double avgAngle = correctAngle(getAvgAngle(line));

        List<BoundingBox> boxes = new ArrayList<>();
        for (TextInfo textInfo : line) {
            boxes.add(BoundingBox.projectToLine(textInfo.getTextPoints(), avgAngle));
        }

        double lineTop = boxes.stream().reduce((lhs, rhs) -> Double.compare(lhs.maxY, rhs.maxY) < 0 ? rhs : lhs)
                .orElseThrow(UnsupportedOperationException::new).maxY;
        double lineBottom = boxes.stream().reduce((lhs, rhs) -> Double.compare(lhs.minY, rhs.minY) > 0 ? rhs : lhs)
                .orElseThrow(UnsupportedOperationException::new).minY;
        double lineHeight = boxes.stream()
                .reduce((lhs, rhs) -> Double.compare(lhs.getHeight(), rhs.getHeight()) < 0 ? rhs : lhs)
                .orElseThrow(UnsupportedOperationException::new).getHeight();
        double delta = (lineTop - lineBottom - lineHeight) / 2;

        Point lineBottomPoint = new Point(boxes.get(0).minX, lineBottom + delta);
        double x = lineBottomPoint.getX();
        double y = lineBottomPoint.getY();
        double xp = x * Math.cos(-avgAngle) + y * Math.sin(-avgAngle);
        double yp = -x * Math.sin(-avgAngle) + y * Math.cos(-avgAngle);

        double ux = Math.cos(avgAngle);
        double uy = Math.sin(avgAngle);
        Line bottom = new Line(xp, yp, ux, uy);
        Line top = bottom.getParallelLine(lineHeight);

        for (TextInfo word : line) {
            Point[] wordP = word.getTextPoints();
            Line left = new Line(wordP[0], wordP[1]);
            Line right = new Line(wordP[3], wordP[2]);
            word.setTextPoints(new Point[]{
                    left.intersection(bottom),
                    left.intersection(top),
                    right.intersection(top),
                    right.intersection(bottom)}
            );
        }
    }

    /**
     * Checks whether 2 text chunks are in the same line by their bounding boxes. The horizontal intersection
     * determined by the projection onto the y-axis must be more than {@link #DEFAULT_INTERSECTION_THRESHOLD}
     * for at least one of the text chunks.
     *
     * @param box1 bounding box of the first text chunk
     * @param box2 bounding box of the second text chunk
     *
     * @return {@code true} if chunks intersect horizontally, {@code false} otherwise
     */
    private static boolean areIntersect(BoundingBox box1, BoundingBox box2) {
        double intersection = Math.min(box1.maxY, box2.maxY) - Math.max(box1.minY, box2.minY);
        double firstIntersectPercentage = intersection / box1.getHeight();
        double secondIntersectPercentage = intersection / box2.getHeight();

        return Math.max(firstIntersectPercentage, secondIntersectPercentage) > DEFAULT_INTERSECTION_THRESHOLD;
    }

    /**
     * Merges list of text infos into single line.
     *
     * @param line list of text infos to merge
     *
     * @return {@link TextInfo} for the whole line
     */
    private static TextInfo mergeTextChunksIntoLine(List<TextInfo> line) {
        if (line.isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        TextInfo prevChunk = null;
        for (TextInfo chunk : line) {
            if (prevChunk == null) {
                text.append(chunk.getText());
            } else {
                BoundingBox[] boxes = BoundingBox.getNormalizedBBoxes(chunk, prevChunk);
                BoundingBox box1 = boxes[0];
                BoundingBox box2 = boxes[1];
                double dist = getDistance(box1, box2);
                double space = (box1.getWidth() / chunk.getText().length() +
                        box2.getWidth() / prevChunk.getText().length()) / 2;
                if (dist > space) {
                    for (int i = 0; i < (int) (dist / space); ++i) {
                        text.append(' ');
                    }
                } else if (dist > 0 && !chunk.getText().startsWith(" ") && !prevChunk.getText().endsWith(" ")) {
                    // We only insert a blank space if the trailing character of the previous string wasn't a space,
                    // and the leading character of the current string isn't a space.
                    text.append(' ');
                }
                text.append(chunk.getText());
            }
            prevChunk = chunk;
        }

        updateBBoxes(line);

        return mergeTextChunksIntoLine(line, text);
    }

    /**
     * Merges list of text infos into single line with provided text.
     *
     * @param line list of text infos to merge into line
     * @param text text for the whole line
     *
     * @return {@link TextInfo} for the whole line with provided text
     */
    private static TextInfo mergeTextChunksIntoLine(List<TextInfo> line, StringBuilder text) {
        Point lineLeftBottomPoint = line.get(0).getTextPoints()[0];
        Point lineLeftTopPoint = line.get(0).getTextPoints()[1];
        Point lineRightTopPoint = line.get(line.size() - 1).getTextPoints()[2];
        Point lineRightBottomPoint = line.get(line.size() - 1).getTextPoints()[3];

        // Preserve orientation and image getHeight for line.
        TextInfo textInfo = new TextInfo(line.get(0));
        textInfo.setText(text.toString());
        textInfo.setTextPoints(new Point[]{lineLeftBottomPoint, lineLeftTopPoint,
                lineRightTopPoint, lineRightBottomPoint});
        return textInfo;
    }

    /**
     * Returns horizontal distance between provided current and previous text chunks.
     *
     * @param current bounding box of the current text chunk
     * @param previous bounding box of the previous text chunk
     *
     * @return distance between 2 text chunks
     */
    private static double getDistance(BoundingBox current, BoundingBox previous) {
        return current.minX - previous.maxX;
    }

    /**
     * Returns normalized angle in radians in range from 0 to 2*pi.
     *
     * @param angle angle in radians to normalize
     *
     * @return normalized angle in range [0, 2*pi)
     */
    private static double normalizeAngle(double angle) {
        angle %= 2 * Math.PI;
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    /**
     * Rounds the angle to a certain value using specified step value.
     *
     * <p>
     * For example, if step is pi/2 (90 degrees), returned value will be either 0, 90, 180 or 270 degrees (in radians).
     * And 0 will be returned for [-45, 45) degrees angles.
     *
     * @param angle angle in radians to round
     * @param step step in radians to find values to round to
     *
     * @return rounded angle
     */
    private static double roundAngle(double angle, double step) {
        double normalizedAngle = normalizeAngle(angle);
        double lowBound = -step / 2;
        if (normalizedAngle >= 2 * Math.PI + lowBound) {
            return 0;
        }
        for (double upBound = step / 2; upBound < 2 * Math.PI + step / 2; upBound += step) {
            if (normalizedAngle >= lowBound && normalizedAngle < upBound) {
                return (lowBound + upBound) / 2;
            }
            lowBound = upBound;
        }

        return angle;
    }

    /**
     * Calculates the minimal difference between rotation angles of the provided text chunks from 0 to pi.
     *
     * @param currentTextInfo the first text chunk
     * @param previousTextInfo the second text chunk
     *
     * @return difference between rotation angles of the provided text chunks
     */
    private static double getAngleDiff(TextInfo currentTextInfo, TextInfo previousTextInfo) {
        // Compare rotation angles in the range of -pi to pi.
        double firstRotation = currentTextInfo.getRotationAngle();
        double secondRotation = previousTextInfo.getRotationAngle();
        double diff = firstRotation - secondRotation;
        // Diff can be from -2pi to 2pi.
        return Math.min(Math.abs(diff), Math.min(2 * Math.PI - diff, 2 * Math.PI + diff));
    }

    /**
     * Returns average rotation angle for all text infos from the provided list.
     *
     * <p>
     * Uses vector sum to correctly process cyclic angles, e.g. cases like -179 + 179 (avg is 180), 1 + 359 (avg 0).
     * Note, that for mutually exclusive vectors this method will return 0 (e.g. 0 + 180 or 0 + 120 + 240).
     *
     * @param textInfos list of text infos to find average rotation angle for
     *
     * @return average rotation angle for all text infos
     */
    private static double getAvgAngle(List<TextInfo> textInfos) {
        double sumSin = 0;
        double sumCos = 0;
        for (TextInfo textInfo : textInfos) {
            double angle = textInfo.getRotationAngle();
            sumSin += Math.sin(angle);
            sumCos += Math.cos(angle);
        }
        return (sumSin == 0 && sumCos == 0) ? 0 : Math.atan2(sumSin, sumCos);
    }

    /**
     * This method is needed to correct the rotation angle if it is really close to 0, 90, 180 or 270 degrees.
     *
     * @param angle angle to correct
     *
     * @return corrected angle
     */
    private static double correctAngle(double angle) {
        int absAngleDegrees = Math.abs((int) toDegrees(angle));
        if (absAngleDegrees <= 1) {
            return Math.toRadians(0);
        }
        if (Math.abs(90 - absAngleDegrees) <= 1) {
            return Math.toRadians(angle > 0 ? 90 : -90);
        }
        if (Math.abs(180 - absAngleDegrees) <= 1) {
            return Math.toRadians(180);
        }
        return angle;
    }

    /**
     * Converts an angle measured in radians to an approximately equivalent angle measured in degrees.
     * The conversion from radians to degrees is generally inexact; users should not expect cos(toRadians(90.0))
     * to exactly equal 0.0.
     *
     * @param radians an angle, in radians
     *
     * @return the measurement of the angle in degrees
     */
    private static double toDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

    /**
     * Class representing parametric representation of a line:
     * point {@code (x, y)} and unit direction vector {@code (ux, uy)}.
     */
    private static class Line {
        private final double x;
        private final double y;
        private final double ux;
        private final double uy;

        /**
         * Creates new {@link Line} instance based on two lines.
         *
         * @param first first {@link Point} on a line
         * @param second second {@link Point} on a line
         */
        public Line(Point first, Point second) {
            this.x = first.getX();
            this.y = first.getY();

            double dx = second.getX() - first.getX();
            double dy = second.getY() - first.getY();
            double length = Math.sqrt(dx * dx + dy * dy);

            this.ux = dx / length;
            this.uy = dy / length;
        }

        /**
         * Creates new {@link Line} instance based on one point {@code (x, y)}
         * and unit direction vector {@code (ux, uy)}.
         *
         * @param x {@code x} coordinate of the point
         * @param y {@code y} coordinate of the point
         * @param ux {@code x} coordinate to specify the unit direction vector
         * @param uy {@code y} coordinate to specify the unit direction vector
         */
        public Line(double x, double y, double ux, double uy) {
            this.x = x;
            this.y = y;
            this.ux = ux;
            this.uy = uy;
        }

        /**
         * Finds intersection {@link Point} with provided line.
         *
         * @param other other {@link Line} to find intersection with
         *
         * @return intersection {@link Point} or {@code null} in case lines are parallel
         */
        public Point intersection(Line other) {
            double det = this.ux * other.uy - other.ux * this.uy;
            if (Math.abs(det) < 1e-10) {
                return null;
            }
            double dx = other.x - this.x;
            double dy = other.y - this.y;
            double t = (dx * other.uy - dy * other.ux) / det;
            double intersectX = this.x + t * this.ux;
            double intersectY = this.y + t * this.uy;
            return new Point(intersectX, intersectY);
        }

        /**
         * Gets {@link Line} parallel to the current one on provided distance.
         *
         * @param distance distance to shift the original point by (along the normal vector)
         *
         * @return parallel {@link Line} on provided distance
         */
        public Line getParallelLine(double distance) {
            // Unit normal vector. It is perpendicular to the current line, the line will be shifted along it.
            double nx = -uy;
            double ny = ux;

            double shiftedX = x + distance * nx;
            double shiftedY = y + distance * ny;
            return new Line(shiftedX, shiftedY, ux, uy);
        }
    }

    /**
     * Helper class to store the bounding box of the text chunk after projection onto the new rotated axes.
     */
    private static class BoundingBox {
        final double minX;
        final double maxX;
        final double minY;
        final double maxY;

        /**
         * Creates new {@link BoundingBox} instance.
         *
         * @param minX minimum value of the bounding rectangle along the x-axis
         * @param maxX maximum value of the bounding rectangle along the x-axis
         * @param minY minimum value of the bounding rectangle along the y-axis
         * @param maxY maximum value of the bounding rectangle along the y-axis
         */
        public BoundingBox(double minX, double maxX, double minY, double maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        /**
         * Returns the width of the bounding box.
         *
         * @return the width of the bounding box
         */
        public double getWidth() {
            return maxX - minX;
        }

        /**
         * Returns the height of the bounding box.
         *
         * @return the height of the bounding box
         */
        public double getHeight() {
            return maxY - minY;
        }

        /**
         * Projects all points onto the new rotated (x, y) axes and returns the min/max values.
         *
         * @param points {@link Point} array to process
         * @param angle the angle defining the directions of the new coordinate system
         *
         * @return {@link BoundingBox} instance containing the min/max values in the new coordinate system
         */
        public static BoundingBox projectToLine(Point[] points, double angle) {
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            double minX = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;

            for (Point p : points) {
                double x = p.getX();
                double y = p.getY();
                double xp = x * cosA + y * sinA;
                double yp = -x * sinA + y * cosA;

                minX = Math.min(minX, xp);
                maxX = Math.max(maxX, xp);
                minY = Math.min(minY, yp);
                maxY = Math.max(maxY, yp);
            }

            return new BoundingBox(minX, maxX, minY, maxY);
        }

        /**
         * Calculates average angle for several text chunks and projects them onto the new axes rotated by that angle.
         * After that bounding boxes are calculated in that new coordinate system.
         *
         * @param first first {@link TextInfo} to calculate normalized bounding box for
         * @param second second {@link TextInfo} to calculate normalized bounding box for
         *
         * @return array of 2 {@link BoundingBox}es in the new coordinate system for the 1st and the 2nd text chunks
         */
        public static BoundingBox[] getNormalizedBBoxes(TextInfo first, TextInfo second) {
            // The average angle to construct a common coordinate system.
            double avgAngle = getAvgAngle(Arrays.asList(first, second));

            // Transform the points of each text chunk into new line coordinates (new x along the line, new y across).
            BoundingBox box1 = BoundingBox.projectToLine(first.getTextPoints(), avgAngle);
            BoundingBox box2 = BoundingBox.projectToLine(second.getTextPoints(), avgAngle);
            return new BoundingBox[]{box1, box2};
        }
    }
}
