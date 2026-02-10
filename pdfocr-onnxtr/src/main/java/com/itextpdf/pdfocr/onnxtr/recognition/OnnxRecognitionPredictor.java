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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.AbstractOnnxPredictor;
import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.util.BufferedImageUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A text recognition predictor implementation, which is using ONNX Runtime and
 * its ML models to recognize text characters on an image.
 */
public class OnnxRecognitionPredictor extends AbstractOnnxPredictor<BufferedImage, String>
        implements IRecognitionPredictor {
    /**
     * Configuration properties of the predictor.
     */
    private final OnnxRecognitionPredictorProperties properties;

    /**
     * Creates a text recognition predictor with the specified properties.
     *
     * @param properties properties of the predictor
     */
    public OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties properties) {
        super(properties.getModelPath(), properties.getInputProperties(), getExpectedOutputShape(properties));
        this.properties = properties;
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * CRNN model with a VGG-16 backbone, stored on disk. This is the default
     * text recognition model in OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_vgg16_bn-662979cc.onnx">
     *             crnn_vgg16_bn
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_vgg16_bn_static_8_bit-bce050c7.onnx">
     *             crnn_vgg16_bn (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the CRNN model loaded with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictor crnnVgg16(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.crnnVgg16(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * CRNN model with a MobileNet V3 backbone, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_mobilenet_v3_large-d42e8185.onnx">
     *             crnn_mobilenet_v3_large
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_mobilenet_v3_large_static_8_bit-459e856d.onnx">
     *             crnn_mobilenet_v3_large (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/crnn_mobilenet_v3_small-bded4d49.onnx">
     *             crnn_mobilenet_v3_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/crnn_mobilenet_v3_small_static_8_bit-4949006f.onnx">
     *             crnn_mobilenet_v3_small (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the CRNN model loaded with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictor crnnMobileNetV3(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.crnnMobileNetV3(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * MASTER model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/master-b1287fcd.onnx">
     *             MASTER
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/master_dynamic_8_bit-d8bd8206.onnx">
     *             MASTER (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the MASTER model loaded
     */
    public static OnnxRecognitionPredictor master(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.master(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * PARSeq model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/parseq-00b40714.onnx">
     *             parseq
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/parseq_dynamic_8_bit-5b04d9f7.onnx">
     *             parseq (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the PARSeq model loaded
     */
    public static OnnxRecognitionPredictor parSeq(String modelPath) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.parSeq(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * PARSeq model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/parseq-00b40714.onnx">
     *             parseq
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/parseq_dynamic_8_bit-5b04d9f7.onnx">
     *             parseq (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param vocabulary vocabulary used for the model output (without special tokens)
     * @param additionalTokens amount of additional tokens in the total vocabulary after the end-of-string token
     *
     * @return a new predictor object with the PARSeq model loaded
     */
    public static OnnxRecognitionPredictor parSeq(String modelPath, Vocabulary vocabulary, int additionalTokens) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.parSeq(modelPath, vocabulary, additionalTokens));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * SAR model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/sar_resnet31-395f8005.onnx">
     *             sar_resnet31
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/sar_resnet31_static_8_bit-c07316bc.onnx">
     *             sar_resnet31 (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the SAR model loaded
     */
    public static OnnxRecognitionPredictor sar(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.sar(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained ViTSTR model, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/vitstr_base-ff62f5be.onnx">
     *             vitstr_base
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/vitstr_base_dynamic_8_bit-976c7cd6.onnx">
     *             vitstr_base (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/vitstr_small-3ff9c500.onnx">
     *             vitstr_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/vitstr_small_dynamic_8_bit-bec6c796.onnx">
     *             vitstr_small (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new predictor object with the ViTSTR model loaded
     */
    public static OnnxRecognitionPredictor viTstr(String modelPath) {
        return new OnnxRecognitionPredictor(OnnxRecognitionPredictorProperties.viTstr(modelPath));
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * PaddleOCR model, stored on disk.
     *
     * <p>
     * Only models in the ONNX format are supported. Since, by default,
     * PaddleOCR does not provide models in the ONNX format, you might need to
     * do a model conversion yourself. Check out
     * <a href="https://www.paddleocr.ai/latest/en/version3.x/deployment/obtaining_onnx_models.html">this page</a>
     * for information on how to do that.
     *
     * <p>
     * This method expects the directory to contain two files:
     * <ul>
     *     <li>{@code inference.onnx} - the inference model in the ONNX format
     *     <li>{@code inference.yml} - the configuration file for the model in YAML
     * </ul>
     *
     * <p>
     * This method can be used to load the following PaddleOCR models:
     * <ul>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv5_server_rec_infer.tar">
     *             PP-OCRv5_server_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv5_mobile_rec_infer.tar">
     *             PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv4_server_rec_doc_infer.tar">
     *             PP-OCRv4_server_rec_doc
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv4_mobile_rec_infer.tar">
     *             PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv4_server_rec_infer.tar">
     *             PP-OCRv4_server_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv3_mobile_rec_infer.tar">
     *             PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ch_SVTRv2_rec_infer.tar">
     *             ch_SVTRv2_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ch_RepSVTR_rec_infer.tar">
     *             ch_RepSVTR_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/en_PP-OCRv5_mobile_rec_infer.tar">
     *             en_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/en_PP-OCRv4_mobile_rec_infer.tar">
     *             en_PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/en_PP-OCRv3_mobile_rec_infer.tar">
     *             en_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/korean_PP-OCRv5_mobile_rec_infer.tar">
     *             korean_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/latin_PP-OCRv5_mobile_rec_infer.tar">
     *             latin_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/eslav_PP-OCRv5_mobile_rec_infer.tar">
     *             eslav_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/th_PP-OCRv5_mobile_rec_infer.tar">
     *             th_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/el_PP-OCRv5_mobile_rec_infer.tar">
     *             el_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/arabic_PP-OCRv5_mobile_rec_infer.tar">
     *             arabic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/cyrillic_PP-OCRv5_mobile_rec_infer.tar">
     *             cyrillic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/devanagari_PP-OCRv5_mobile_rec_infer.tar">
     *             devanagari_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/te_PP-OCRv5_mobile_rec_infer.tar">
     *             te_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ta_PP-OCRv5_mobile_rec_infer.tar">
     *             ta_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/korean_PP-OCRv3_mobile_rec_infer.tar">
     *             korean_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/japan_PP-OCRv3_mobile_rec_infer.tar">
     *             japan_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/chinese_cht_PP-OCRv3_mobile_rec_infer.tar">
     *             chinese_cht_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/te_PP-OCRv3_mobile_rec_infer.tar">
     *             te_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ka_PP-OCRv3_mobile_rec_infer.tar">
     *             ka_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ta_PP-OCRv3_mobile_rec_infer.tar">
     *             ta_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/latin_PP-OCRv3_mobile_rec_infer.tar">
     *             latin_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/arabic_PP-OCRv3_mobile_rec_infer.tar">
     *             arabic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/cyrillic_PP-OCRv3_mobile_rec_infer.tar">
     *             cyrillic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/devanagari_PP-OCRv3_mobile_rec_infer.tar">
     *             devanagari_PP-OCRv3_mobile_rec
     *         </a>
     * </ul>
     *
     * <p>
     * These models can handle spaces.
     *
     * @param modelDirPath path to the directory with the model and its
     *                     configuration file
     *
     * @return a new predictor object with the PaddleOCR model loaded
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictor paddleOcr(String modelDirPath) throws IOException {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.paddleOcr(modelDirPath)
        );
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * PaddleOCR model, stored on disk.
     *
     * <p>
     * Only models in the ONNX format are supported. Since, by default,
     * PaddleOCR does not provide models in the ONNX format, you might need to
     * do a model conversion yourself. Check out
     * <a href="https://www.paddleocr.ai/latest/en/version3.x/deployment/obtaining_onnx_models.html">this page</a>
     * for information on how to do that.
     *
     * <p>
     * This method can be used to load the following PaddleOCR models:
     * <ul>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv5_server_rec_infer.tar">
     *             PP-OCRv5_server_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv5_mobile_rec_infer.tar">
     *             PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv4_server_rec_doc_infer.tar">
     *             PP-OCRv4_server_rec_doc
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv4_mobile_rec_infer.tar">
     *             PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv4_server_rec_infer.tar">
     *             PP-OCRv4_server_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/PP-OCRv3_mobile_rec_infer.tar">
     *             PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ch_SVTRv2_rec_infer.tar">
     *             ch_SVTRv2_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ch_RepSVTR_rec_infer.tar">
     *             ch_RepSVTR_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/en_PP-OCRv5_mobile_rec_infer.tar">
     *             en_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/en_PP-OCRv4_mobile_rec_infer.tar">
     *             en_PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/en_PP-OCRv3_mobile_rec_infer.tar">
     *             en_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/korean_PP-OCRv5_mobile_rec_infer.tar">
     *             korean_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/latin_PP-OCRv5_mobile_rec_infer.tar">
     *             latin_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/eslav_PP-OCRv5_mobile_rec_infer.tar">
     *             eslav_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/th_PP-OCRv5_mobile_rec_infer.tar">
     *             th_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/el_PP-OCRv5_mobile_rec_infer.tar">
     *             el_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/arabic_PP-OCRv5_mobile_rec_infer.tar">
     *             arabic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/cyrillic_PP-OCRv5_mobile_rec_infer.tar">
     *             cyrillic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/devanagari_PP-OCRv5_mobile_rec_infer.tar">
     *             devanagari_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/te_PP-OCRv5_mobile_rec_infer.tar">
     *             te_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ta_PP-OCRv5_mobile_rec_infer.tar">
     *             ta_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/korean_PP-OCRv3_mobile_rec_infer.tar">
     *             korean_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/japan_PP-OCRv3_mobile_rec_infer.tar">
     *             japan_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/chinese_cht_PP-OCRv3_mobile_rec_infer.tar">
     *             chinese_cht_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/te_PP-OCRv3_mobile_rec_infer.tar">
     *             te_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ka_PP-OCRv3_mobile_rec_infer.tar">
     *             ka_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/ta_PP-OCRv3_mobile_rec_infer.tar">
     *             ta_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/latin_PP-OCRv3_mobile_rec_infer.tar">
     *             latin_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/arabic_PP-OCRv3_mobile_rec_infer.tar">
     *             arabic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/cyrillic_PP-OCRv3_mobile_rec_infer.tar">
     *             cyrillic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://paddle-model-ecology.bj.bcebos.com/paddlex/official_inference_model/paddle3.0.0/devanagari_PP-OCRv3_mobile_rec_infer.tar">
     *             devanagari_PP-OCRv3_mobile_rec
     *         </a>
     * </ul>
     *
     * <p>
     * These models can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     *
     * @return a new predictor object with the PaddleOCR model loaded
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictor paddleOcr(String modelPath, String configPath) throws IOException {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.paddleOcr(modelPath, configPath)
        );
    }

    /**
     * Creates a new text recognition predictor using an existing pre-trained
     * EasyOCR model, stored on disk.
     *
     * <p>
     * Only models in the ONNX format are supported. Since, by default,
     * EasyOCR does not provide models in the ONNX format, you might need to
     * do a model conversion yourself.
     *
     * <p>
     * TODO: Host models ourselves? Conversion is not exactly straight-forward...
     *
     * <p>
     * This method can be used to load the following EasyOCR models:
     * <ul>
     *     <li>
     *         <a href=https://github.com/JaidedAI/EasyOCR/releases/download/v1.3/english_g2.zip">
     *             english_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.3/latin_g2.zip">
     *             latin_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.3/zh_sim_g2.zip">
     *             zh_sim_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.3/japanese_g2.zip">
     *             japanese_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.3/korean_g2.zip">
     *             korean_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.2/telugu.zip">
     *             telugu_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.2/kannada.zip">
     *             kannada_g2
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/latin.zip">
     *             latin_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/chinese_sim.zip">
     *             zh_sim_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/chinese.zip">
     *             zh_tra_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/japanese.zip">
     *             japanese_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/korean.zip">
     *             korean_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/thai.zip">
     *             thai_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/devanagari.zip">
     *             devanagari_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/cyrillic.zip">
     *             cyrillic_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/pre-v1.1.6/arabic.zip">
     *             arabic_g1
     *         </a>
     *     <li>
     *         <a href="https://github.com/JaidedAI/EasyOCR/releases/download/v1.1.8/bengali.zip">
     *             bengali_g1
     *         </a>
     * </ul>
     *
     * <p>
     * These models can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param labelMapper label mapper to use for the model
     *
     * @return a new predictor object with the EasyOCR model loaded
     */
    public static OnnxRecognitionPredictor easyOcr(String modelPath, EasyOcrMapper labelMapper) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.easyOcr(modelPath, labelMapper)
        );
    }

    /**
     * Returns the text recognition predictor properties.
     *
     * @return the text recognition predictor properties
     */
    public OnnxRecognitionPredictorProperties getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> predict(Iterator<BufferedImage> inputs) {
        if (!properties.shouldSplitImages()) {
            return super.predict(inputs);
        }

        // Additional pre- and post-processing, if we are splitting images
        final TextBoxSplitter textBoxSplitter = new TextBoxSplitter();
        final Iterator<BufferedImage> splitInputs = textBoxSplitter.mapInputs(inputs);
        final Iterator<String> outputs = super.predict(splitInputs);
        return textBoxSplitter.mapOutputs(outputs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FloatBufferMdArray toInputBuffer(List<BufferedImage> batch) {
        // Just your regular BCHW input
        return BufferedImageUtil.toBchwInput(batch, properties.getInputProperties());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> fromOutputBuffer(List<BufferedImage> inputBatch, FloatBufferMdArray outputBatch) {
        final int batchSize = outputBatch.getDimension(0);
        final List<String> words = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; ++i) {
            words.add(properties.getPostProcessor().process(outputBatch.getSubArray(i)));
        }
        return words;
    }

    private static long[] getExpectedOutputShape(OnnxRecognitionPredictorProperties properties) {
        // Dynamic batch size
        final long BATCH_SIZE = -1;
        // Token count is, usually, not dynamic in the model, but we don't
        // really care about it, as it is just a loop boundary in the algorithm
        final long TOKEN_COUNT = -1;
        final long classCount = properties.getPostProcessor().labelDimension();
        return new long[]{BATCH_SIZE, TOKEN_COUNT, classCount};
    }
}
