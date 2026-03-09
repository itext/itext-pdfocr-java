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
package com.itextpdf.pdfocr.onnx.recognition;

import com.itextpdf.pdfocr.onnx.AbstractOnnxPredictor;
import com.itextpdf.pdfocr.onnx.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnx.IOrtSessionOptionsCreator;
import com.itextpdf.pdfocr.onnx.util.BufferedImageUtil;

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
        super(properties, getExpectedOutputShape(properties));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the CRNN model loaded with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictor crnnVgg16(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.crnnVgg16(modelPath, ortSessionOptionsCreator));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the CRNN model loaded with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictor crnnMobileNetV3(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.crnnMobileNetV3(modelPath, ortSessionOptionsCreator));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the MASTER model loaded
     */
    public static OnnxRecognitionPredictor master(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.master(modelPath, ortSessionOptionsCreator));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the PARSeq model loaded
     */
    public static OnnxRecognitionPredictor parSeq(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.parSeq(modelPath, ortSessionOptionsCreator));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the PARSeq model loaded
     */
    public static OnnxRecognitionPredictor parSeq(String modelPath, Vocabulary vocabulary, int additionalTokens,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.parSeq(modelPath, vocabulary, additionalTokens,
                        ortSessionOptionsCreator));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the SAR model loaded
     */
    public static OnnxRecognitionPredictor sar(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.sar(modelPath, ortSessionOptionsCreator));
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
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the ViTSTR model loaded
     */
    public static OnnxRecognitionPredictor viTstr(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.viTstr(modelPath, ortSessionOptionsCreator));
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_rec_infer">
     *             PP-OCRv5_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_rec_infer">
     *             PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_doc_infer">
     *             PP-OCRv4_server_rec_doc
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_rec_infer">
     *             PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_infer">
     *             PP-OCRv4_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv3_mobile_rec_infer">
     *             PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_SVTRv2_rec_infer">
     *             ch_SVTRv2_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_RepSVTR_rec_infer">
     *             ch_RepSVTR_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv5_mobile_rec_infer">
     *             en_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv4_mobile_rec_infer">
     *             en_PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv3_mobile_rec_infer">
     *             en_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv5_mobile_rec_infer">
     *             korean_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv5_mobile_rec_infer">
     *             latin_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-eslav_PP-OCRv5_mobile_rec_infer">
     *             eslav_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-th_PP-OCRv5_mobile_rec_infer">
     *             th_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-el_PP-OCRv5_mobile_rec_infer">
     *             el_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv5_mobile_rec_infer">
     *             arabic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv5_mobile_rec_infer">
     *             cyrillic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv5_mobile_rec_infer">
     *             devanagari_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv5_mobile_rec_infer">
     *             te_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv5_mobile_rec_infer">
     *             ta_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv3_mobile_rec_infer">
     *             korean_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-japan_PP-OCRv3_mobile_rec_infer">
     *             japan_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-chinese_cht_PP-OCRv3_mobile_rec_infer">
     *             chinese_cht_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv3_mobile_rec_infer">
     *             te_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ka_PP-OCRv3_mobile_rec_infer">
     *             ka_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv3_mobile_rec_infer">
     *             ta_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv3_mobile_rec_infer">
     *             latin_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv3_mobile_rec_infer">
     *             arabic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv3_mobile_rec_infer">
     *             cyrillic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv3_mobile_rec_infer">
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_rec_infer">
     *             PP-OCRv5_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_rec_infer">
     *             PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_doc_infer.tar">
     *             PP-OCRv4_server_rec_doc
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_rec_infer">
     *             PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_infer">
     *             PP-OCRv4_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv3_mobile_rec_infer">
     *             PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_SVTRv2_rec_infer">
     *             ch_SVTRv2_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_RepSVTR_rec_infer">
     *             ch_RepSVTR_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv5_mobile_rec_infer">
     *             en_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv4_mobile_rec_infer">
     *             en_PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv3_mobile_rec_infer">
     *             en_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv5_mobile_rec_infer">
     *             korean_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv5_mobile_rec_infer">
     *             latin_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-eslav_PP-OCRv5_mobile_rec_infer">
     *             eslav_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-th_PP-OCRv5_mobile_rec_infer">
     *             th_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-el_PP-OCRv5_mobile_rec_infer">
     *             el_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv5_mobile_rec_infer">
     *             arabic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv5_mobile_rec_infer">
     *             cyrillic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv5_mobile_rec_infer">
     *             devanagari_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv5_mobile_rec_infer">
     *             te_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv5_mobile_rec_infer">
     *             ta_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv3_mobile_rec_infer">
     *             korean_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-japan_PP-OCRv3_mobile_rec_infer">
     *             japan_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-chinese_cht_PP-OCRv3_mobile_rec_infer">
     *             chinese_cht_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv3_mobile_rec_infer">
     *             te_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ka_PP-OCRv3_mobile_rec_infer">
     *             ka_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv3_mobile_rec_infer">
     *             ta_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv3_mobile_rec_infer">
     *             latin_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv3_mobile_rec_infer">
     *             arabic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv3_mobile_rec_infer">
     *             cyrillic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv3_mobile_rec_infer">
     *             devanagari_PP-OCRv3_mobile_rec
     *         </a>
     * </ul>
     *
     * <p>
     * These models can handle spaces.
     *
     * @param modelDirPath path to the directory with the model and its
     *                     configuration file
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the PaddleOCR model loaded
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictor paddleOcr(String modelDirPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) throws IOException {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.paddleOcr(modelDirPath, ortSessionOptionsCreator)
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_rec_infer">
     *             PP-OCRv5_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_rec_infer">
     *             PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_doc_infer.tar">
     *             PP-OCRv4_server_rec_doc
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_rec_infer">
     *             PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_infer">
     *             PP-OCRv4_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv3_mobile_rec_infer">
     *             PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_SVTRv2_rec_infer">
     *             ch_SVTRv2_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_RepSVTR_rec_infer">
     *             ch_RepSVTR_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv5_mobile_rec_infer">
     *             en_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv4_mobile_rec_infer">
     *             en_PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv3_mobile_rec_infer">
     *             en_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv5_mobile_rec_infer">
     *             korean_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv5_mobile_rec_infer">
     *             latin_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-eslav_PP-OCRv5_mobile_rec_infer">
     *             eslav_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-th_PP-OCRv5_mobile_rec_infer">
     *             th_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-el_PP-OCRv5_mobile_rec_infer">
     *             el_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv5_mobile_rec_infer">
     *             arabic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv5_mobile_rec_infer">
     *             cyrillic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv5_mobile_rec_infer">
     *             devanagari_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv5_mobile_rec_infer">
     *             te_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv5_mobile_rec_infer">
     *             ta_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv3_mobile_rec_infer">
     *             korean_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-japan_PP-OCRv3_mobile_rec_infer">
     *             japan_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-chinese_cht_PP-OCRv3_mobile_rec_infer">
     *             chinese_cht_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv3_mobile_rec_infer">
     *             te_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ka_PP-OCRv3_mobile_rec_infer">
     *             ka_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv3_mobile_rec_infer">
     *             ta_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv3_mobile_rec_infer">
     *             latin_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv3_mobile_rec_infer">
     *             arabic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv3_mobile_rec_infer">
     *             cyrillic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv3_mobile_rec_infer">
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_rec_infer">
     *             PP-OCRv5_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_rec_infer">
     *             PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_doc_infer.tar">
     *             PP-OCRv4_server_rec_doc
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_rec_infer">
     *             PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_rec_infer">
     *             PP-OCRv4_server_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv3_mobile_rec_infer">
     *             PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_SVTRv2_rec_infer">
     *             ch_SVTRv2_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ch_RepSVTR_rec_infer">
     *             ch_RepSVTR_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv5_mobile_rec_infer">
     *             en_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv4_mobile_rec_infer">
     *             en_PP-OCRv4_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-en_PP-OCRv3_mobile_rec_infer">
     *             en_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv5_mobile_rec_infer">
     *             korean_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv5_mobile_rec_infer">
     *             latin_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-eslav_PP-OCRv5_mobile_rec_infer">
     *             eslav_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-th_PP-OCRv5_mobile_rec_infer">
     *             th_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-el_PP-OCRv5_mobile_rec_infer">
     *             el_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv5_mobile_rec_infer">
     *             arabic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv5_mobile_rec_infer">
     *             cyrillic_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv5_mobile_rec_infer">
     *             devanagari_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv5_mobile_rec_infer">
     *             te_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv5_mobile_rec_infer">
     *             ta_PP-OCRv5_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-korean_PP-OCRv3_mobile_rec_infer">
     *             korean_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-japan_PP-OCRv3_mobile_rec_infer">
     *             japan_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-chinese_cht_PP-OCRv3_mobile_rec_infer">
     *             chinese_cht_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-te_PP-OCRv3_mobile_rec_infer">
     *             te_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ka_PP-OCRv3_mobile_rec_infer">
     *             ka_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-ta_PP-OCRv3_mobile_rec_infer">
     *             ta_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-latin_PP-OCRv3_mobile_rec_infer">
     *             latin_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-arabic_PP-OCRv3_mobile_rec_infer">
     *             arabic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-cyrillic_PP-OCRv3_mobile_rec_infer">
     *             cyrillic_PP-OCRv3_mobile_rec
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-devanagari_PP-OCRv3_mobile_rec_infer">
     *             devanagari_PP-OCRv3_mobile_rec
     *         </a>
     * </ul>
     *
     * <p>
     * These models can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the PaddleOCR model loaded
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictor paddleOcr(String modelPath, String configPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) throws IOException {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.paddleOcr(modelPath, configPath, ortSessionOptionsCreator)
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
     * This method can be used to load the following EasyOCR models:
     * <ul>
     *     <li>
     *         <a href=https://huggingface.co/itextresearch/itext-EasyOCR-english_g2">
     *             english_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-latin_g2">
     *             latin_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-zh_sim_g2">
     *             zh_sim_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-japanese_g2">
     *             japanese_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-korean_g2">
     *             korean_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-telugu">
     *             telugu_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-kannada">
     *             kannada_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-latin">
     *             latin_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-chinese_sim">
     *             zh_sim_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-chinese">
     *             zh_tra_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-japanese">
     *             japanese_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-korean">
     *             korean_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-thai">
     *             thai_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-devanagari">
     *             devanagari_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-cyrillic">
     *             cyrillic_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-arabic">
     *             arabic_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-bengali">
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
     * Creates a new text recognition predictor using an existing pre-trained
     * EasyOCR model, stored on disk.
     *
     * <p>
     * Only models in the ONNX format are supported. Since, by default,
     * EasyOCR does not provide models in the ONNX format, you might need to
     * do a model conversion yourself.
     *
     * <p>
     * This method can be used to load the following EasyOCR models:
     * <ul>
     *     <li>
     *         <a href=https://huggingface.co/itextresearch/itext-EasyOCR-english_g2">
     *             english_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-latin_g2">
     *             latin_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-zh_sim_g2">
     *             zh_sim_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-japanese_g2">
     *             japanese_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-korean_g2">
     *             korean_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-telugu">
     *             telugu_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-kannada">
     *             kannada_g2
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-latin">
     *             latin_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-chinese_sim">
     *             zh_sim_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-chinese">
     *             zh_tra_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-japanese">
     *             japanese_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-korean">
     *             korean_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-thai">
     *             thai_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-devanagari">
     *             devanagari_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-cyrillic">
     *             cyrillic_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-arabic">
     *             arabic_g1
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-bengali">
     *             bengali_g1
     *         </a>
     * </ul>
     *
     * <p>
     * These models can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param labelMapper label mapper to use for the model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new predictor object with the EasyOCR model loaded
     */
    public static OnnxRecognitionPredictor easyOcr(String modelPath, EasyOcrMapper labelMapper,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictor(
                OnnxRecognitionPredictorProperties.easyOcr(modelPath, labelMapper, ortSessionOptionsCreator)
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
