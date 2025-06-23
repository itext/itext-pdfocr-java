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

import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.TextOrientation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to build text output from the provided image OCR result and write it to the TXT file.
 */
public final class PdfOcrTextBuilder {
    private static final float DEFAULT_INTERSECTION_THRESHOLD = 0.7f;
    private static final float DEFAULT_GAP_THRESHOLD = 0.1f;

    private PdfOcrTextBuilder() {
        // Private constructor will prevent the instantiation of this class directly.
    }

    /**
     * Constructs string output from the provided {@link IOcrEngine#doImageOcr} result.
     *
     * @param textInfos {@link java.util.Map} where key is {@link java.lang.Integer} representing the number of the page
     *                  and value is {@link java.util.List} of {@link TextInfo} elements where each {@link TextInfo}
     *                  element contains a word or a line and its 4 coordinates (bbox)
     *
     * @return string output of the OCR result
     */
    public static String buildText(Map<Integer, List<TextInfo>> textInfos) {
        StringBuilder outputText = new StringBuilder();
        for (int page : textInfos.keySet().stream().sorted().collect(Collectors.toList())) {
            List<TextInfo> textChunks = textInfos.get(page);
            Collections.sort(textChunks, new Comparator<TextInfo>() {
                @Override
                public int compare(TextInfo first, TextInfo second) {
                    // Not really needed, but just in case.
                    if (first == second) {
                        return 0;
                    }

                    int result = Integer.compare(getOrientation(first.getOrientation()),
                            getOrientation(second.getOrientation()));
                    if (result != 0) {
                        return result;
                    }

                    if (!areIntersect(first, second)) {
                        float middleDistPerpendicularDiff = getDistPerpendicularBottom(second) + getHeight(second) / 2
                                - (getDistPerpendicularBottom(first) + getHeight(first) / 2);
                        return middleDistPerpendicularDiff > 0 ? 1 : -1;
                    }

                    return Float.compare(getDistParallelStart(first), getDistParallelStart(second)) > 0 ? 1 : -1;
                }
            });

            StringBuilder sb = new StringBuilder();
            TextInfo lastChunk = null;
            for (TextInfo chunk : textChunks) {
                if (lastChunk == null) {
                    sb.append(chunk.getText());
                } else {
                    if (isInTheSameLine(chunk, lastChunk)) {
                        // We only insert a blank space if the trailing character of the previous string wasn't a space,
                        // and the leading character of the current string isn't a space.
                        if (isChunkAtWordBoundary(chunk, lastChunk) &&
                                !chunk.getText().startsWith(" ") && !lastChunk.getText().endsWith(" ")) {
                            sb.append(' ');
                        }
                        sb.append(chunk.getText());
                    } else {
                        sb.append('\n');
                        sb.append(chunk.getText());
                    }
                }
                lastChunk = chunk;
            }
            outputText.append(sb).append('\n');
        }
        return outputText.toString();
    }

    /**
     * Checks whether text chunks are in the same line.
     *
     * <p>
     * We consider text chunks to be in the same line if they oriented in a same way and
     * if their intersection is more than 70% of at least one of the text chunks, e.g. for `one eight`
     * intersection percentage will be 100% for `one` and less than 50% for `eight`.
     *
     * @param currentTextInfo current {@link TextInfo}
     * @param previousTextInfo previous {@link TextInfo}
     *
     * @return {@code true} if both text chunks are in the same line, {@code false} otherwise
     */
    static boolean isInTheSameLine(TextInfo currentTextInfo, TextInfo previousTextInfo) {
        if (currentTextInfo.getOrientation() != previousTextInfo.getOrientation()) {
            return false;
        }

        return areIntersect(currentTextInfo, previousTextInfo);
    }

    private static boolean areIntersect(TextInfo first, TextInfo second) {
        float intersection = Math.min(getDistPerpendicularTop(first), getDistPerpendicularTop(second)) -
                Math.max(getDistPerpendicularBottom(first), getDistPerpendicularBottom(second));

        float firstIntersectPercentage = intersection / getHeight(first);
        float secondIntersectPercentage = intersection / getHeight(second);

        return Math.max(firstIntersectPercentage, secondIntersectPercentage) > DEFAULT_INTERSECTION_THRESHOLD;
    }

    private static boolean isChunkAtWordBoundary(TextInfo currentTextInfo, TextInfo previousTextInfo) {
        float dist = getDistParallelStart(currentTextInfo) - getDistParallelEnd(previousTextInfo);

        if (dist < 0) {
            dist = getDistParallelStart(previousTextInfo) - getDistParallelEnd(currentTextInfo);

            // The situation when the chunks intersect. We don't need to add space in this case.
            if (dist < 0) {
                return false;
            }
        }
        // We consider that space should be added in case the difference is
        // more than 10% of the minimal of the two average widths per char.
        return dist > DEFAULT_GAP_THRESHOLD * Math.min(
                getWidth(currentTextInfo) / currentTextInfo.getText().length(),
                getWidth(previousTextInfo) / previousTextInfo.getText().length());
    }

    private static int getOrientation(TextOrientation orientation) {
        switch (orientation) {
            case HORIZONTAL_ROTATED_90:
                return 90;
            case HORIZONTAL_ROTATED_180:
                return 180;
            case HORIZONTAL_ROTATED_270:
                return 270;
            case HORIZONTAL:
            default:
                return 0;
        }
    }

    /**
     * Distance of the start of the chunk parallel to the orientation unit vector (i.e. the left (X) position
     * in the not rotated coordinate system) with a minus sign for 180 and 270 degrees, since we need the coordinates
     * of the text to the left to be less than those of the text to the right.
     *
     * @param textInfo {@link TextInfo} to get distance parallel start
     *
     * @return distance parallel start
     */
    private static float getDistParallelStart(TextInfo textInfo) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_270:
                return -textInfo.getBboxRect().getTop();
            case HORIZONTAL_ROTATED_180:
                return -textInfo.getBboxRect().getRight();
            case HORIZONTAL_ROTATED_90:
                return textInfo.getBboxRect().getBottom();
            case HORIZONTAL:
            default:
                return textInfo.getBboxRect().getLeft();
        }
    }

    /**
     * Distance of the end of the chunk parallel to the orientation unit vector
     * (i.e. the right (X + width) position in the not rotated coordinate system)
     * with a minus sign for 180 and 270 degrees, since we need the coordinates
     * of the text to the left to be less than those of the text to the right.
     *
     * @param textInfo {@link TextInfo} to get distance parallel end
     *
     * @return distance parallel end
     */
    private static float getDistParallelEnd(TextInfo textInfo) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_270:
                return -textInfo.getBboxRect().getBottom();
            case HORIZONTAL_ROTATED_180:
                return -textInfo.getBboxRect().getLeft();
            case HORIZONTAL_ROTATED_90:
                return textInfo.getBboxRect().getTop();
            case HORIZONTAL:
            default:
                return textInfo.getBboxRect().getRight();
        }
    }

    /**
     * Gets perpendicular distance to the orientation unit vector
     * (i.e. the bottom (Y) position in the not rotated coordinate system)
     * with a minus sign for 90 and 180 degrees, since we need the coordinates
     * of the text to the bottom to be less than those of the text to the top.
     *
     * @param textInfo {@link TextInfo} to get distance perpendicular
     *
     * @return distance perpendicular
     */
    private static float getDistPerpendicularBottom(TextInfo textInfo) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_270:
                return textInfo.getBboxRect().getLeft();
            case HORIZONTAL_ROTATED_180:
                return -textInfo.getBboxRect().getTop();
            case HORIZONTAL_ROTATED_90:
                return -textInfo.getBboxRect().getRight();
            case HORIZONTAL:
            default:
                return textInfo.getBboxRect().getBottom();
        }
    }

    /**
     * Gets perpendicular distance to the orientation unit vector from the top point
     * (i.e. the top (Y + height) position in the not rotated coordinate system)
     * with a minus sign for 90 and 180 degrees, since we need the coordinates
     * of the text to the bottom to be less than those of the text to the top.
     *
     * @param textInfo {@link TextInfo} to get distance perpendicular
     *
     * @return distance perpendicular
     */
    private static float getDistPerpendicularTop(TextInfo textInfo) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_270:
                return textInfo.getBboxRect().getRight();
            case HORIZONTAL_ROTATED_180:
                return -textInfo.getBboxRect().getBottom();
            case HORIZONTAL_ROTATED_90:
                return -textInfo.getBboxRect().getLeft();
            case HORIZONTAL:
            default:
                return textInfo.getBboxRect().getTop();
        }
    }

    private static float getWidth(TextInfo textInfo) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_90:
            case HORIZONTAL_ROTATED_270:
                return textInfo.getBboxRect().getHeight();
            case HORIZONTAL_ROTATED_180:
            case HORIZONTAL:
            default:
                return textInfo.getBboxRect().getWidth();
        }
    }

    private static float getHeight(TextInfo textInfo) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_90:
            case HORIZONTAL_ROTATED_270:
                return textInfo.getBboxRect().getWidth();
            case HORIZONTAL_ROTATED_180:
            case HORIZONTAL:
            default:
                return textInfo.getBboxRect().getHeight();
        }
    }
}
