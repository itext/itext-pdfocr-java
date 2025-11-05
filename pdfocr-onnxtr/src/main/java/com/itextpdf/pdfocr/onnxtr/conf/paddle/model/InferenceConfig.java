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
package com.itextpdf.pdfocr.onnxtr.conf.paddle.model;

import java.util.Objects;

/**
 * POJO for the root object in a config file.
 */
public class InferenceConfig {
    private final PreProcess preProcess;
    private final PostProcess postProcess;

    /**
     * Creates a new POJO for the config file object.
     *
     * @param preProcess value under the {@code PreProcess} key
     * @param postProcess value under the {@code PostProcess} key
     */
    public InferenceConfig(PreProcess preProcess, PostProcess postProcess) {
        this.preProcess = Objects.requireNonNull(preProcess);
        this.postProcess = Objects.requireNonNull(postProcess);
    }

    /**
     * Returns the value under the {@code PreProcess} key.
     *
     * @return the value under the {@code PreProcess} key
     */
    public PreProcess getPreProcess() {
        return preProcess;
    }

    /**
     * Returns the value under the {@code PostProcess} key.
     *
     * @return the value under the {@code PostProcess} key
     */
    public PostProcess getPostProcess() {
        return postProcess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InferenceConfig that = (InferenceConfig) o;
        return Objects.equals(preProcess, that.preProcess)
                && Objects.equals(postProcess, that.postProcess);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object)preProcess, postProcess);
    }
}
