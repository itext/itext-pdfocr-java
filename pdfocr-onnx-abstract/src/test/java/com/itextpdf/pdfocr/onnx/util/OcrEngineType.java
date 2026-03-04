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
package com.itextpdf.pdfocr.onnx.util;

import com.itextpdf.pdfocr.onnx.OnnxOcrEngine;
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.EasyOcrMapper;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;

import java.io.IOException;
import java.util.function.Supplier;

public enum OcrEngineType {
    PADDLE("PaddleOCR", () -> createPaddleOcrEngine()),
    EASY("EasyOCR", () -> createEasyOcrEngine()),
    DOCTR("DocTR", () -> createDocTrEngine());

    public volatile OnnxOcrEngine instance;
    private final String displayName;
    private final Supplier<OnnxOcrEngine> supplier;

    OcrEngineType(String displayName, Supplier<OnnxOcrEngine> supplier) {
        this.displayName = displayName;
        this.supplier = supplier;
    }

    public OnnxOcrEngine get() {
        if (this.instance == null) {
            synchronized (this) {
                if (this.instance == null) {
                    this.instance = this.supplier.get();
                }
            }
        }
        return this.instance;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static OcrEngineType[] all() {
        return new OcrEngineType[]{PADDLE, EASY, DOCTR};
    }

    private static OnnxOcrEngine createPaddleOcrEngine() {
        try {
            IDetectionPredictor paddleDetectionPredictor = OnnxDetectionPredictor.paddleOcr(ModelPaths.getPaddleOcrDetectionModel());
            IRecognitionPredictor paddleRecognitionPredictor = OnnxRecognitionPredictor.paddleOcr(ModelPaths.getPaddleOcrRecognitionModel());
            return new OnnxOcrEngine(paddleDetectionPredictor, paddleRecognitionPredictor);
        } catch (IOException e) {
            // Shouldn't reach there.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static OnnxOcrEngine createEasyOcrEngine() {
        IDetectionPredictor easyDetectionPredictor = OnnxDetectionPredictor.easyOcr(ModelPaths.getEasyOcrDetectionModel());
        IRecognitionPredictor easyRecognitionPredictor = OnnxRecognitionPredictor.easyOcr(ModelPaths.getEasyOcrRecognitionModel(),
                EasyOcrMapper.LATIN_G2);
        return new OnnxOcrEngine(easyDetectionPredictor, easyRecognitionPredictor);
    }

    private static OnnxOcrEngine createDocTrEngine() {
        IDetectionPredictor docTrDetectionPredictor = OnnxDetectionPredictor.fast(ModelPaths.getDocTrDetectionModel());
        IRecognitionPredictor docTrRecognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(ModelPaths.getDocTrRecognitionModel());
        return new OnnxOcrEngine(docTrDetectionPredictor, docTrRecognitionPredictor);
    }
}
