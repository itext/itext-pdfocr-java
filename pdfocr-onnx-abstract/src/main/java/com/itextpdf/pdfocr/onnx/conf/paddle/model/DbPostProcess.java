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
package com.itextpdf.pdfocr.onnx.conf.paddle.model;

import java.util.Objects;

/**
 * POJO for the DBPostProcess post-processor object under a
 * {@code PostProcess} key in a config file.
 */
public class DbPostProcess implements PostProcess {
    /**
     * Expected name for the CTCLabelDecode post-processor.
     */
    public static final String NAME = "DBPostProcess";

    private final float thresh;
    private final float boxThresh;
    private final float unclipRatio;
    private final int maxCandidates;
    private final boolean useDilation;
    private final ScoreMode scoreMode;
    private final BoxType boxType;

    /**
     * Creates a new POJO for the config file object.
     *
     * @param thresh value under the {@code thresh} key
     * @param boxThresh value under the {@code box_thresh} key
     * @param unclipRatio value under the {@code unclip_ratio} key
     * @param maxCandidates value under the {@code max_candidates} key
     * @param useDilation value under the {@code use_dilation} key
     * @param scoreMode value under the {@code score_mode} key
     * @param boxType value under the {@code box_type} key
     */
    public DbPostProcess(
            float thresh,
            float boxThresh,
            float unclipRatio,
            int maxCandidates,
            boolean useDilation,
            ScoreMode scoreMode,
            BoxType boxType
    ) {
        this.thresh = thresh;
        this.boxThresh = boxThresh;
        this.unclipRatio = unclipRatio;
        this.maxCandidates = maxCandidates;
        this.useDilation = useDilation;
        this.scoreMode = Objects.requireNonNull(scoreMode);
        this.boxType = Objects.requireNonNull(boxType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the value under the {@code thresh} key.
     *
     * @return the value under the {@code thresh} key
     */
    public float getThresh() {
        return thresh;
    }

    /**
     * Returns the value under the {@code box_thresh} key.
     *
     * @return the value under the {@code box_thresh} key
     */
    public float getBoxThresh() {
        return boxThresh;
    }

    /**
     * Returns the value under the {@code unclip_ratio} key.
     *
     * @return the value under the {@code unclip_ratio} key
     */
    public float getUnclipRatio() {
        return unclipRatio;
    }

    /**
     * Returns the value under the {@code max_candidates} key.
     *
     * @return the value under the {@code max_candidates} key
     */
    public int getMaxCandidates() {
        return maxCandidates;
    }

    /**
     * Returns the value under the {@code use_dilation} key.
     *
     * @return the value under the {@code use_dilation} key
     */
    public boolean getUseDilation() {
        return useDilation;
    }

    /**
     * Returns the value under the {@code score_mode} key.
     *
     * @return the value under the {@code score_mode} key
     */
    public ScoreMode getScoreMode() {
        return scoreMode;
    }

    /**
     * Returns the value under the {@code box_type} key.
     *
     * @return the value under the {@code box_type} key
     */
    public BoxType getBoxType() {
        return boxType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DbPostProcess that = (DbPostProcess) o;
        return Float.compare(thresh, that.thresh) == 0
                && Float.compare(boxThresh, that.boxThresh) == 0
                && Float.compare(unclipRatio, that.unclipRatio) == 0
                && maxCandidates == that.maxCandidates
                && useDilation == that.useDilation
                && scoreMode == that.scoreMode
                && boxType == that.boxType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                (Object)thresh, boxThresh, unclipRatio, maxCandidates, useDilation, scoreMode, boxType
        );
    }
}
