/*
    Copyright (c) 2020 JaidedAI Authors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.itextpdf.pdfocr.onnxtr.merging;

import com.itextpdf.kernel.geom.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Text box merger, based on the algorithm used in EasyOCR.
 */
public class EasyOcrTextBoxMerger implements ITextBoxMerger {
    /**
     * Pre-calculated {@code sqrt(2)}.
     */
    private static final double SQRT_2 = Math.sqrt(2);
    /**
     * Threshold for the tangent of the box slope to consider it for merging.
     */
    private static final double SLOPE_THRESHOLD = 0.1;
    /**
     * Threshold for the dimension ratio of the box slope to straighten it.
     */
    private static final double RATIO_THRESHOLD = 1.1;
    /**
     * Threshold for the vertical distance between text boxes during merging.
     */
    private static final double Y_THRESHOLD = 0.5;
    /**
     * Threshold for the height differences between text boxes during merging.
     */
    private static final double HEIGHT_THRESHOLD = 0.5;
    /**
     * Threshold for the horizontal distance between text boxes during merging.
     */
    private static final double WIDTH_THRESHOLD = 0.5;
    /**
     * Multiplier for calculating the added margin.
     */
    private static final double MARGIN_MUL = 0.1;

    /**
     * Creates new {@link EasyOcrTextBoxMerger} instance.
     */
    public EasyOcrTextBoxMerger() {
        // Empty constructor in order for default one to not be removed if another one is added.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point[]> process(List<Point[]> detectedTextBoxes) {
        // Separating aligned and sloped text boxes
        final ArrayList<Point[]> alignedBoxes = new ArrayList<>();
        final ArrayList<Point[]> slopedBoxes = new ArrayList<>();
        for (int i = 0; i < detectedTextBoxes.size(); ++i) {
            final Point[] box = detectedTextBoxes.get(i);
            // If the box is square enough, we will ignore the slope
            if (calcSlope(box) < SLOPE_THRESHOLD || calcRatio(box) < RATIO_THRESHOLD) {
                alignedBoxes.add(toBoundingBox(box));
            } else {
                slopedBoxes.add(addMarginSloped(box));
            }
        }

        // If no aligned boxes, then there is nothing to merge
        // Early exit
        if (alignedBoxes.isEmpty()) {
            return slopedBoxes;
        }

        // Sort by middle Y before merging
        alignedBoxes.sort(Comparator.comparingDouble(EasyOcrTextBoxMerger::calcYAligned));

        final ArrayList<Point[]> finalBoxes = new ArrayList<>(slopedBoxes);
        // Grouping and merging
        final ArrayList<Point[]> groupBoxes = new ArrayList<>();
        groupBoxes.add(alignedBoxes.get(0));
        double groupHeightSum = calcHeightAligned(alignedBoxes.get(0));
        double groupYSum = calcYAligned(alignedBoxes.get(0));
        for (int i = 1; i < alignedBoxes.size(); ++i) {
            final Point[] box = alignedBoxes.get(i);
            final double height = calcHeightAligned(box);
            final double y = calcYAligned(box);
            final double avgGroupY = groupYSum / groupBoxes.size();
            final double avgGroupHeight = groupHeightSum / groupBoxes.size();
            if (Math.abs(y - avgGroupY) < Y_THRESHOLD * avgGroupHeight) {
                // Adding box to group, if comparable
                groupBoxes.add(box);
                groupHeightSum += height;
                groupYSum += y;
            } else {
                // Otherwise process current vertical group and start a new one
                processVerticalGroup(groupBoxes, finalBoxes);
                groupBoxes.clear();
                groupBoxes.add(box);
                groupHeightSum = height;
                groupYSum = y;
            }
        }
        if (!groupBoxes.isEmpty()) {
            processVerticalGroup(groupBoxes, finalBoxes);
        }
        return finalBoxes;
    }

    /**
     * Handles text box processing within a vertical group of text boxes.
     *
     * @param verticalGroup vertical group of text boxes to process
     * @param out output list to store merged text boxes in
     */
    private static void processVerticalGroup(List<Point[]> verticalGroup, List<Point[]> out) {
        assert !verticalGroup.isEmpty();

        // If only one box in verticalGroup, just pass it padded to out
        if (verticalGroup.size() == 1) {
            out.add(addMarginAligned(verticalGroup.get(0)));
            return;
        }

        verticalGroup.sort(Comparator.comparingDouble(EasyOcrTextBoxMerger::calcXMinAligned));

        final ArrayList<Point[]> groupBoxes = new ArrayList<>();
        groupBoxes.add(verticalGroup.get(0));
        double groupHeightSum = calcHeightAligned(verticalGroup.get(0));
        double groupXMax = calcXMaxAligned(verticalGroup.get(0));
        for (int i = 1; i < verticalGroup.size(); ++i) {
            final Point[] box = verticalGroup.get(i);
            final double width = calcWidthAligned(box);
            final double height = calcHeightAligned(box);
            final double xMin = calcXMinAligned(box);
            final double avgGroupHeight = groupHeightSum / groupBoxes.size();
            if ((Math.abs(height - avgGroupHeight) < HEIGHT_THRESHOLD * avgGroupHeight)
                    && (xMin - groupXMax < WIDTH_THRESHOLD * width)) {
                // Adding box to group, if comparable
                groupBoxes.add(box);
                groupHeightSum += height;
            } else {
                // Otherwise process current horizontal group and start a new one
                out.add(addMarginAligned(toBoundingBox(groupBoxes)));
                groupBoxes.clear();
                groupBoxes.add(box);
                groupHeightSum = height;
            }
            groupXMax = calcXMaxAligned(box);
        }
        if (!groupBoxes.isEmpty()) {
            out.add(addMarginAligned(toBoundingBox(groupBoxes)));
        }
    }

    /**
     * Builds a bounding box for a list of text boxes.
     *
     * @param boxes list of text boxes to build a bounding box for
     *
     * @return built bounding box
     */
    private static Point[] toBoundingBox(List<Point[]> boxes) {
        assert !boxes.isEmpty();

        double minX = boxes.get(0)[0].getX();
        double maxX = minX;
        double minY = boxes.get(0)[0].getY();
        double maxY = minY;
        for (int boxIdx = 0; boxIdx < boxes.size(); ++boxIdx) {
            final Point[] points = boxes.get(boxIdx);
            for (int pointIdx = 0; pointIdx < points.length; ++pointIdx) {
                final Point p = points[pointIdx];
                final double x = p.getX();
                if (x < minX) {
                    minX = x;
                } else if (x > maxX) {
                    maxX = x;
                }
                final double y = p.getY();
                if (y < minY) {
                    minY = y;
                } else if (y > maxY) {
                    maxY = y;
                }
            }
        }
        return new Point[] {
                new Point(minX, maxY),
                new Point(minX, minY),
                new Point(maxX, minY),
                new Point(maxX, maxY)
        };
    }

    /**
     * Builds a bounding box for an array of points.
     *
     * @param points array of points to build a bounding box for
     *
     * @return built bounding box
     */
    private static Point[] toBoundingBox(Point[] points) {
        return toBoundingBox(Collections.singletonList(points));
    }

    /**
     * Creates a new box, which adds a margin to an existing sloped one.
     *
     * @param box sloped text box to add margin to
     *
     * @return box with the added margin
     */
    private static Point[] addMarginSloped(Point[] box) {
        /*
         * The algorithm just extends the rotated rectangle by extending its
         * diagonals on both sides. And then it calculates new points from
         * that.
         */
        final Point p0 = box[0];    // Bottom-left  -X +Y
        final Point p1 = box[1];    // Top-left     -X -Y
        final Point p2 = box[2];    // Top-right    +X -Y
        final Point p3 = box[3];    // Bottom-right +X +Y

        final double height = p1.distance(p0);
        final double width = p1.distance(p2);
        final double diagonalMargin = SQRT_2 * MARGIN_MUL * Math.min(height, width);

        final double theta02 = Math.abs(Math.atan(
                (p0.getY() - p2.getY()) / (p0.getX() - p2.getX())
        ));
        final double theta13 = Math.abs(Math.atan(
                (p1.getY() - p3.getY()) / (p1.getX() - p3.getX())
        ));

        final double dx02 = Math.cos(theta02) * diagonalMargin;
        final double dy02 = Math.sin(theta02) * diagonalMargin;
        final double dx13 = Math.cos(theta13) * diagonalMargin;
        final double dy13 = Math.sin(theta13) * diagonalMargin;

        return new Point[] {
                new Point(p0.getX() - dx02, p0.getY() + dy02),
                new Point(p1.getX() - dx13, p1.getY() - dy13),
                new Point(p2.getX() + dx02, p2.getY() - dy02),
                new Point(p3.getX() + dx13, p3.getY() + dy13)
        };
    }

    /**
     * Creates a new box, which adds a margin to an existing aligned one.
     *
     * @param box aligned text box to add margin to
     *
     * @return box with the added margin
     */
    private static Point[] addMarginAligned(Point[] box) {
        final Point p0 = box[0];    // Bottom-left  -X +Y
        final Point p1 = box[1];    // Top-left     -X -Y
        final Point p2 = box[2];    // Top-right    +X -Y
        final Point p3 = box[3];    // Bottom-right +X +Y

        final double height = calcHeightAligned(box);
        final double width = calcWidthAligned(box);
        final double margin = MARGIN_MUL * Math.min(height, width);
        return new Point[] {
                new Point(p0.getX() - margin, p0.getY() + margin),
                new Point(p1.getX() - margin, p1.getY() - margin),
                new Point(p2.getX() + margin, p2.getY() - margin),
                new Point(p3.getX() + margin, p3.getY() + margin),
        };
    }

    /**
     * Calculates slope for an arbitrary text box.
     *
     * @param box text box to calculate slope for
     *
     * @return calculated slope
     */
    private static double calcSlope(Point[] box) {
        return Math.abs((box[0].getY() - box[3].getY()) / (box[0].getX() - box[3].getX()));
    }

    /**
     * Calculates ratio of the text box from biggest dimension to the smallest
     * one.
     *
     * @param box text box to calculate ratio for
     *
     * @return calculated ratio
     */
    private static double calcRatio(Point[] box) {
        final double height = box[1].distance(box[0]);
        final double width = box[1].distance(box[2]);
        if (height > width) {
            return height / width;
        }
        return width / height;
    }

    /**
     * Calculates width for an aligned text box.
     *
     * @param box aligned text box
     *
     * @return width
     */
    private static double calcWidthAligned(Point[] box) {
        return box[2].getX() - box[1].getX();
    }

    /**
     * Calculates height for an aligned text box.
     *
     * @param box aligned text box
     *
     * @return height
     */
    private static double calcHeightAligned(Point[] box) {
        return box[0].getY() - box[1].getY();
    }

    /**
     * Calculates central y for an aligned text box.
     *
     * @param box aligned text box
     *
     * @return central y
     */
    private static double calcYAligned(Point[] box) {
        return (box[0].getY() + box[1].getY()) / 2;
    }

    /**
     * Calculates minimum x for an aligned text box.
     *
     * @param box aligned text box
     *
     * @return minimum x
     */
    private static double calcXMinAligned(Point[] box) {
        return box[0].getX();
    }

    /**
     * Calculates maximum x for an aligned text box.
     *
     * @param box aligned text box
     *
     * @return maximum x
     */
    private static double calcXMaxAligned(Point[] box) {
        return box[2].getX();
    }
}
