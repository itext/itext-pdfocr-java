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
package com.itextpdf.pdfocr.onnxtr.util;

/**
 * Util class that uses static methods instead of final fields to make model paths auto portable to .NET.
 *
 * <p>
 * Paths to models should be initialized after correct NUnit.Framework.TestContext.CurrentContext.TestDirectory
 * will be available, but it's not available outside nunit in the static context for {@link OcrEngineType} and
 * {@link com.itextpdf.pdfocr.onnxtr.text.OcrEngineTypeWithTextPositioning}.
 */
public class ModelPaths {
    private static String paddleDetectionModel;
    private static String paddleRecognitionModel;
    private static String easyDetectionModel;
    private static String easyRecognitionModel;
    private static String docTrDetectionModel;
    private static String docTrRecognitionModel;

    public static String getPaddleOcrDetectionModel() {
        if (paddleDetectionModel == null) {
            paddleDetectionModel = "./src/test/resources/com/itextpdf/pdfocr/models/paddleocr/PP-OCRv5_mobile_det_infer/";
        }
        return paddleDetectionModel;
    }

    public static String getPaddleOcrRecognitionModel() {
        if (paddleRecognitionModel == null) {
            paddleRecognitionModel = "./src/test/resources/com/itextpdf/pdfocr/models/paddleocr/PP-OCRv5_mobile_rec_infer/";
        }
        return paddleRecognitionModel;
    }

    public static String getEasyOcrDetectionModel() {
        if (easyDetectionModel == null) {
            easyDetectionModel = "./src/test/resources/com/itextpdf/pdfocr/models/easyocr/craft_mlt_25k.onnx";
        }
        return easyDetectionModel;
    }

    public static String getEasyOcrRecognitionModel() {
        if (easyRecognitionModel == null) {
            easyRecognitionModel = "./src/test/resources/com/itextpdf/pdfocr/models/easyocr/latin_g2.onnx";
        }
        return easyRecognitionModel;
    }

    public static String getDocTrDetectionModel() {
        if (docTrDetectionModel == null) {
            docTrDetectionModel = "./src/test/resources/com/itextpdf/pdfocr/models/rep_fast_tiny-28867779.onnx";
        }
        return docTrDetectionModel;
    }

    public static String getDocTrRecognitionModel() {
        if (docTrRecognitionModel == null) {
            docTrRecognitionModel = "./src/test/resources/com/itextpdf/pdfocr/models/crnn_vgg16_bn-662979cc.onnx";
        }
        return docTrRecognitionModel;
    }
}
