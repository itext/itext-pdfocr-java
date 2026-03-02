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
package com.itextpdf.pdfocr.onnx;

import java.util.Objects;

/**
 * Properties for configuring ONNX models.
 *
 * <p>
 * It contains a path to the model, model input properties and an ONNX runtime session options creator.
 */
public abstract class AbstractOnnxPredictorProperties {
    /**
     * Default ONNX runtime session options creator.
     */
    protected static final IOrtSessionOptionsCreator DEFAULT_ORT_SESSION_CREATOR = new DefaultOrtSessionOptionsCreator();

    /**
     * Path to the ONNX model to load.
     */
    protected final String modelPath;
    /**
     * Properties of the inputs of the ONNX model. Used for validation (both
     * input and output, since output mask size is the same) and pre-processing.
     */
    protected final OnnxInputProperties inputProperties;
    /**
     * ONNX runtime session options creator.
     */
    protected final IOrtSessionOptionsCreator ortSessionOptionsCreator;


    /**
     * Creates new predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     */
    public AbstractOnnxPredictorProperties(String modelPath, OnnxInputProperties inputProperties,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        this.modelPath = Objects.requireNonNull(modelPath);
        this.inputProperties = Objects.requireNonNull(inputProperties);
        this.ortSessionOptionsCreator = Objects.requireNonNull(ortSessionOptionsCreator);
    }

    /**
     * Returns the ONNX runtime session options creator.
     *
     * @return the ONNX runtime session options creator
     */
    public IOrtSessionOptionsCreator getOrtSessionOptionsCreator() {
        return ortSessionOptionsCreator;
    }

    /**
     * Returns the ONNX model input properties.
     *
     * @return the ONNX model input properties
     */
    public OnnxInputProperties getInputProperties() {
        return inputProperties;
    }

    /**
     * Returns the path to the ONNX model.
     *
     * @return the path to the ONNX model
     */
    public String getModelPath() {
        return modelPath;
    }
}
