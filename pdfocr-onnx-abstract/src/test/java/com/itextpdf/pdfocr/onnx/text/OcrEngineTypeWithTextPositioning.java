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
package com.itextpdf.pdfocr.onnx.text;

import com.itextpdf.pdfocr.onnx.OnnxTrEngineProperties;
import com.itextpdf.pdfocr.onnx.OnnxTrOcrEngine;
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.EasyOcrMapper;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.recognition.OnnxRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.util.ModelPaths;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * This enum is created for {@link TextPositioningModeTest} and should be used in it only
 * since all engines (and so predictors) will be closed after these tests, and it won't be possible to reuse them.
 */
public enum OcrEngineTypeWithTextPositioning {
    PADDLE_LINES("PaddleOCR_BY_LINES", () -> createPaddleOcrEngine(TextPositioning.BY_LINES)),
    EASY_LINES("EasyOCR_BY_LINES", () -> createEasyOcrEngine(TextPositioning.BY_LINES)),
    DOCTR_LINES("DocTR_BY_LINES", () -> createDocTrEngine(TextPositioning.BY_LINES)),
    PADDLE_WORDS("PaddleOCR_BY_WORDS", () -> createPaddleOcrEngine(TextPositioning.BY_WORDS)),
    EASY_WORDS("EasyOCR_BY_WORDS", () -> createEasyOcrEngine(TextPositioning.BY_WORDS)),
    DOCTR_WORDS("DocTR_BY_WORDS", () -> createDocTrEngine(TextPositioning.BY_WORDS)),
    PADDLE_WORDS_AND_LINES("PaddleOCR_BY_WORDS_AND_LINES", () -> createPaddleOcrEngine(TextPositioning.BY_WORDS_AND_LINES)),
    EASY_WORDS_AND_LINES("EasyOCR_BY_WORDS_AND_LINES", () -> createEasyOcrEngine(TextPositioning.BY_WORDS_AND_LINES)),
    DOCTR_WORDS_AND_LINES("DocTR_BY_WORDS_AND_LINES", () -> createDocTrEngine(TextPositioning.BY_WORDS_AND_LINES));

    public volatile OnnxTrOcrEngine instance;
    private final String displayName;
    private final Supplier<OnnxTrOcrEngine> supplier;

    OcrEngineTypeWithTextPositioning(String displayName, Supplier<OnnxTrOcrEngine> supplier) {
        this.displayName = displayName;
        this.supplier = supplier;
    }

    public OnnxTrOcrEngine get() {
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

    public static OcrEngineTypeWithTextPositioning[] all() {
        return new OcrEngineTypeWithTextPositioning[]{PADDLE_LINES, EASY_LINES, DOCTR_LINES,
                PADDLE_WORDS, EASY_WORDS, DOCTR_WORDS,
                PADDLE_WORDS_AND_LINES, EASY_WORDS_AND_LINES, DOCTR_WORDS_AND_LINES};
    }

    private static IDetectionPredictor paddleDetectionPredictor;
    private static IRecognitionPredictor paddleRecognitionPredictor;
    private static IDetectionPredictor easyDetectionPredictor;
    private static IRecognitionPredictor easyRecognitionPredictor;
    private static IDetectionPredictor docTrDetectionPredictor;
    private static IRecognitionPredictor docTrRecognitionPredictor;

    private static OnnxTrOcrEngine createPaddleOcrEngine(TextPositioning textPositioning) {
        try {
            if (paddleDetectionPredictor == null) {
                paddleDetectionPredictor = OnnxDetectionPredictor.paddleOcr(ModelPaths.getPaddleOcrDetectionModel());
            }
            if (paddleRecognitionPredictor == null) {
                paddleRecognitionPredictor = OnnxRecognitionPredictor.paddleOcr(ModelPaths.getPaddleOcrRecognitionModel());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new OnnxTrOcrEngine(paddleDetectionPredictor, null, paddleRecognitionPredictor,
                new OnnxTrEngineProperties().setTextPositioning(textPositioning));
    }

    private static OnnxTrOcrEngine createEasyOcrEngine(TextPositioning textPositioning) {
        if (easyDetectionPredictor == null) {
            easyDetectionPredictor = OnnxDetectionPredictor.easyOcr(ModelPaths.getEasyOcrDetectionModel());
        }
        if (easyRecognitionPredictor == null) {
            easyRecognitionPredictor = OnnxRecognitionPredictor.easyOcr(ModelPaths.getEasyOcrRecognitionModel(), EasyOcrMapper.LATIN_G2);
        }
        return new OnnxTrOcrEngine(easyDetectionPredictor, null, easyRecognitionPredictor,
                new OnnxTrEngineProperties().setTextPositioning(textPositioning));
    }

    private static OnnxTrOcrEngine createDocTrEngine(TextPositioning textPositioning) {
        if (docTrDetectionPredictor == null) {
            docTrDetectionPredictor = OnnxDetectionPredictor.fast(ModelPaths.getDocTrDetectionModel());
        }
        if (docTrRecognitionPredictor == null) {
            docTrRecognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(ModelPaths.getDocTrRecognitionModel());
        }
        return new OnnxTrOcrEngine(docTrDetectionPredictor, null, docTrRecognitionPredictor,
                new OnnxTrEngineProperties().setTextPositioning(textPositioning));
    }
}
