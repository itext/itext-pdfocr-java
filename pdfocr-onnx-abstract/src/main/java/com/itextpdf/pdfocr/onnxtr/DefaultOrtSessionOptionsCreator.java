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
package com.itextpdf.pdfocr.onnxtr;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.OrtSession.SessionOptions.ExecutionMode;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;

/**
 * Default implementation of {@link IOrtSessionOptionsCreator}.
 *
 * <p>
 * {@code CUDA} execution provider is added if available, otherwise default {@code CPU} execution provider is used.
 */
public class DefaultOrtSessionOptionsCreator implements IOrtSessionOptionsCreator {
    @Override
    public SessionOptions create() throws OrtException {
        final OrtSession.SessionOptions ortOptions = new OrtSession.SessionOptions();
        try {
            if (OrtEnvironment.getAvailableProviders().contains(OrtProvider.CUDA)) {
                ortOptions.addCUDA();
            } else {
                ortOptions.addCPU(true);
            }
            ortOptions.setExecutionMode(ExecutionMode.SEQUENTIAL);
            ortOptions.setOptimizationLevel(OptLevel.ALL_OPT);
            ortOptions.setIntraOpNumThreads(-1);
            ortOptions.setInterOpNumThreads(-1);
            return ortOptions;
        } catch (Exception e) {
            ortOptions.close();
            throw e;
        }
    }
}
