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

import com.itextpdf.pdfocr.onnxtr.ImageChannelConfiguration;
import com.itextpdf.pdfocr.onnxtr.ImageResizeOptions;
import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;
import com.itextpdf.pdfocr.onnxtr.PaddingStrategy;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.CtcLabelDecode;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.InferenceConfig;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.PostProcess;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.RecResizeImg;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.model.TransformOp;
import com.itextpdf.pdfocr.onnxtr.conf.paddle.parser.InferenceConfigParser;
import com.itextpdf.pdfocr.onnxtr.exceptions.PaddleOcrInitException;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

/**
 * Properties for configuring text recognition ONNX models.
 *
 * <p>
 * It contains a path to the model, model input properties and a model output post-processor.
 */
public class OnnxRecognitionPredictorProperties {
    private static final OnnxInputProperties DEFAULT_INPUT_PROPERTIES = new OnnxInputProperties(
            new ImageResizeOptions(
                    ImageChannelConfiguration.RGB,
                    128, 32,
                    PaddingStrategy.BOTTOM_RIGHT_BLACK
            ),
            new float[]{0.694F, 0.695F, 0.693F},
            new float[]{0.299F, 0.296F, 0.301F},
            512
    );

    private static final int PADDLE_MAX_WIDTH = 3200;
    private static final float[] PADDLE_MEAN = new float[]{0.5F, 0.5F, 0.5F};
    private static final float[] PADDLE_STD = new float[]{0.5F, 0.5F, 0.5F};
    private static final int PADDLE_BATCH_SIZE = 6;

    /**
     * Path to the ONNX model to load.
     */
    private final String modelPath;

    /**
     * Properties of the inputs of the ONNX model. Used for validation (both
     * input and output, since output mask size is the same) and pre-processing.
     */
    private final OnnxInputProperties inputProperties;

    /**
     * Post-processor of the outputs of the ONNX model. Converts the  output of
     * the model to a text string.
     */
    private final IRecognitionPostProcessor postProcessor;

    /**
     * Defines, whether input images to the recognition model should be split
     * into smaller ones with better aspect ratios. Usually should be false
     * for models, which operates on lines, as merging of the text back could
     * cause errors.
     */
    private final boolean splitImages;

    /**
     * Creates new text recognition predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     * @param splitImages whether input images to the ML model should be split
     *                    into smaller ones with better aspect ratios
     */
    public OnnxRecognitionPredictorProperties(String modelPath, OnnxInputProperties inputProperties,
                                              IRecognitionPostProcessor postProcessor, boolean splitImages) {
        this.modelPath = Objects.requireNonNull(modelPath);
        this.inputProperties = Objects.requireNonNull(inputProperties);
        this.postProcessor = Objects.requireNonNull(postProcessor);
        this.splitImages = splitImages;
    }

    /**
     * Creates new text recognition predictor properties.
     *
     * <p>
     * Images will be split before passing them to the ML model.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     */
    public OnnxRecognitionPredictorProperties(String modelPath, OnnxInputProperties inputProperties,
                                              IRecognitionPostProcessor postProcessor) {
        this(modelPath, inputProperties, postProcessor, true);
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * CRNN models with a VGG-16 backbone, stored on disk. This is the default
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
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a CRNN model with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnVgg16(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new CrnnPostProcessor(Vocabulary.LEGACY_FRENCH)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * CRNN models with a MobileNet V3 backbone, stored on disk.
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
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a CRNN model with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnMobileNetV3(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new CrnnPostProcessor(Vocabulary.FRENCH)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained MASTER models, stored on disk.
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
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a MASTER model
     */
    public static OnnxRecognitionPredictorProperties master(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                // Additional "<sos>" and "<pad>" tokens
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 2)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * PARSeq models, stored on disk.
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
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath) {
        return OnnxRecognitionPredictorProperties.parSeq(modelPath, Vocabulary.LATIN_EXTENDED, 0);
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * PARSeq models, stored on disk.
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
     * @param modelPath path to the pre-trained model
     * @param vocabulary vocabulary used for the model output (without special tokens)
     * @param additionalTokens amount of additional tokens in the total vocabulary after the end-of-string token
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath, Vocabulary vocabulary,
                                                            int additionalTokens) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(vocabulary, additionalTokens)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * SAR models, stored on disk.
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
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a SAR model
     */
    public static OnnxRecognitionPredictorProperties sar(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0)
        );
    }

    /**
     * Creates a new text recognition properties object for existing pre-trained
     * ViTSTR models, stored on disk.
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
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a ViTSTR model
     */
    public static OnnxRecognitionPredictorProperties viTstr(String modelPath) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0)
        );
    }

    /**
     * Creates a new text recognition properties object for existing
     * pre-trained PaddleOCR models, stored on disk.
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
     *     <li>{@code inference.onnx} - the inference model in the ONNX format</li>
     *     <li>{@code inference.yml} - the configuration file for the model in YAML</li>
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
     * @param modelDirPath path to the directory with the model and its
     *                     configuration file
     *
     * @return a new text recognition properties object for a PaddleOCR model
     */
    public static OnnxRecognitionPredictorProperties paddleOcr(String modelDirPath) throws IOException {
        return paddleOcr(modelDirPath + "/inference.onnx", modelDirPath + "/inference.yml");
    }

    /**
     * Creates a new text recognition properties object for existing
     * pre-trained PaddleOCR models, stored on disk.
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
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     *
     * @return a new text recognition properties object for a PaddleOCR model
     */
    public static OnnxRecognitionPredictorProperties paddleOcr(String modelPath, String configPath) throws IOException {
        final InferenceConfig config;
        try (final InputStream is = Files.newInputStream(Paths.get(configPath))) {
            config = InferenceConfigParser.parse(is);
        }
        final OnnxInputProperties inputProperties = createPaddleInputProperties(config);
        final CtcLabelPostProcessor postProcessor = createPaddlePostProcessor(config);
        // Splitting the images makes the results worse, as the model is
        // designed to handle long line, also it seems like the split/merge
        // algorithm is not handling whitespaces properly
        return new OnnxRecognitionPredictorProperties(
                modelPath, inputProperties, postProcessor, false
        );
    }

    /**
     * Returns the path to the ONNX model.
     *
     * @return the path to the ONNX model
     */
    public String getModelPath() {
        return modelPath;
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
     * Returns the ONNX model output post-processor.
     *
     * @return the ONNX model output post-processor
     */
    public IRecognitionPostProcessor getPostProcessor() {
        return postProcessor;
    }

    /**
     * Returns whether input images should be split.
     *
     * @return whether input images should be split
     */
    public boolean shouldSplitImages() {
        return splitImages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final OnnxRecognitionPredictorProperties that = (OnnxRecognitionPredictorProperties) o;
        return splitImages == that.splitImages &&
                Objects.equals(modelPath, that.modelPath) &&
                Objects.equals(inputProperties, that.inputProperties) &&
                Objects.equals(postProcessor, that.postProcessor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object) modelPath, inputProperties, postProcessor, splitImages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OnnxRecognitionPredictorProperties{" +
                "modelPath='" + modelPath + '\'' +
                ", inputProperties=" + inputProperties +
                ", postProcessor=" + postProcessor +
                ", splitImages=" + splitImages +
                '}';
    }

    private static OnnxInputProperties createPaddleInputProperties(InferenceConfig config) {
        final TransformOp[] ops = config.getPreProcess().getTransformOps();

        final DecodeImage decode = getPaddleOp(ops, DecodeImage.class, DecodeImage.WRAPPING_KEY);
        if (decode.getChannelFirst()) {
            throw PaddleOcrInitException.channelFirstIsNotSupported();
        }
        final ImageChannelConfiguration channelConfig = mapImgMode(decode.getImgMode());

        final RecResizeImg resize = getPaddleOp(ops, RecResizeImg.class, RecResizeImg.WRAPPING_KEY);
        final int[] inputShape = resize.getImageShape();
        final int height = inputShape[1];
        final int minWidth = inputShape[2];
        final ImageResizeOptions resizeOpts = new ImageResizeOptions(
                channelConfig,
                minWidth, height,
                PADDLE_MAX_WIDTH, height,
                PaddingStrategy.BOTTOM_RIGHT_GRAY
        );
        return new OnnxInputProperties(resizeOpts, PADDLE_MEAN, PADDLE_STD, PADDLE_BATCH_SIZE);
    }

    private static <T> T getPaddleOp(TransformOp[] ops, Class<T> cls, String name) {
        for (int i = 0; i < ops.length; ++i) {
            final TransformOp op = ops[i];
            if (cls.isInstance(op)) {
                return (T) op;
            }
        }
        throw PaddleOcrInitException.preProcessorOperationMissing(name);
    }

    private static ImageChannelConfiguration mapImgMode(ImgMode im) {
        switch (im) {
            case GRAY:
                return ImageChannelConfiguration.GRAYSCALE;
            case RGB:
                return ImageChannelConfiguration.RGB;
            case BGR:
                return ImageChannelConfiguration.BGR;
        }
        // Should not get here
        throw new IllegalStateException(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION
        );
    }

    private static CtcLabelPostProcessor createPaddlePostProcessor(InferenceConfig config) {
        final PostProcess postProcess = config.getPostProcess();
        if (!(postProcess instanceof CtcLabelDecode)) {
            throw PaddleOcrInitException.unexpectedPostProcessorType(postProcess.getName());
        }
        final CtcLabelDecode ctc = (CtcLabelDecode) postProcess;
        /*
         * In PaddleOCR there is a space character mapping, but it is not
         * included in the config file. It is a parameter in the post
         * processor, which is always true. For simplicity, we will just
         * modify the vocab here.
         */
        final String[] lookUpTable = add(ctc.getCharacterDict(), " ");
        return new CtcLabelPostProcessor(new StringMapper(lookUpTable));
    }

    private static String[] add(String[] arr, String elem) {
        final String[] newArr = Arrays.copyOf(arr, arr.length + 1);
        newArr[arr.length] = elem;
        return newArr;
    }
}
