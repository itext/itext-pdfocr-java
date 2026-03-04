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

import com.itextpdf.pdfocr.onnx.AbstractOnnxPredictorProperties;
import com.itextpdf.pdfocr.onnx.IOrtSessionOptionsCreator;
import com.itextpdf.pdfocr.onnx.ImageChannelConfiguration;
import com.itextpdf.pdfocr.onnx.ImageResizeOptions;
import com.itextpdf.pdfocr.onnx.OnnxInputProperties;
import com.itextpdf.pdfocr.onnx.PaddingStrategy;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.CtcLabelDecode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.InferenceConfig;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.PostProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.RecResizeImg;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.TransformOp;
import com.itextpdf.pdfocr.onnx.conf.paddle.parser.InferenceConfigParser;
import com.itextpdf.pdfocr.onnx.exceptions.PaddleOcrInitException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;

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
public class OnnxRecognitionPredictorProperties extends AbstractOnnxPredictorProperties {
    private static final OnnxInputProperties DEFAULT_INPUT_PROPERTIES = new OnnxInputProperties(
            new ImageResizeOptions(
                    ImageChannelConfiguration.RGB,
                    128, 32,
                    PaddingStrategy.BOTTOM_RIGHT_BLACK
            ),
            new float[]{0.694F, 0.695F, 0.693F},
            new float[]{0.299F, 0.296F, 0.301F},
            64
    );

    private static final int PADDLE_MAX_WIDTH = 3200;
    private static final float[] PADDLE_MEAN = new float[]{0.5F, 0.5F, 0.5F};
    private static final float[] PADDLE_STD = new float[]{0.5F, 0.5F, 0.5F};
    private static final int PADDLE_BATCH_SIZE = 6;

    private static final OnnxInputProperties EASY_OCR_INPUT_PROPERTIES = new OnnxInputProperties(
            new ImageResizeOptions(
                    ImageChannelConfiguration.GRAYSCALE,
                    1, 64,
                    // There is, actually, no width limit here for EasyOCR, so just setting
                    // something big, but reasonable here...
                    4096, 64,
                    PaddingStrategy.BOTTOM_RIGHT_EDGE
            ),
            new float[]{0.5F},
            new float[]{0.5F},
            // In the CPU case just having 1 should be faster
            1
    );

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
        this(modelPath, inputProperties, postProcessor, splitImages, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates new text recognition predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     * @param splitImages whether input images to the ML model should be split
     *                    into smaller ones with better aspect ratios
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     */
    public OnnxRecognitionPredictorProperties(String modelPath, OnnxInputProperties inputProperties,
            IRecognitionPostProcessor postProcessor, boolean splitImages, IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        super(modelPath, inputProperties, ortSessionOptionsCreator);
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
     * Creates new text recognition predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     */
    public OnnxRecognitionPredictorProperties(String modelPath, OnnxInputProperties inputProperties,
            IRecognitionPostProcessor postProcessor, IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        this(modelPath, inputProperties, postProcessor, true, ortSessionOptionsCreator);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a CRNN model with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnVgg16(String modelPath) {
        return crnnVgg16(modelPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a CRNN model with a VGG-16 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnVgg16(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new CrnnPostProcessor(Vocabulary.LEGACY_FRENCH),
                ortSessionOptionsCreator
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a CRNN model with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnMobileNetV3(String modelPath) {
        return crnnMobileNetV3(modelPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a CRNN model with a MobileNet V3 backbone
     */
    public static OnnxRecognitionPredictorProperties crnnMobileNetV3(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new CrnnPostProcessor(Vocabulary.FRENCH),
                ortSessionOptionsCreator
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a MASTER model
     */
    public static OnnxRecognitionPredictorProperties master(String modelPath) {
        return master(modelPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a MASTER model
     */
    public static OnnxRecognitionPredictorProperties master(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                // Additional "<sos>" and "<pad>" tokens
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 2),
                ortSessionOptionsCreator
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath) {
        return parSeq(modelPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return OnnxRecognitionPredictorProperties.parSeq(modelPath, Vocabulary.LATIN_EXTENDED, 0,
                ortSessionOptionsCreator);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param vocabulary vocabulary used for the model output (without special tokens)
     * @param additionalTokens amount of additional tokens in the total vocabulary after the end-of-string token
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath, Vocabulary vocabulary,
                                                            int additionalTokens) {
        return parSeq(modelPath, vocabulary, additionalTokens, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param vocabulary vocabulary used for the model output (without special tokens)
     * @param additionalTokens amount of additional tokens in the total vocabulary after the end-of-string token
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a PARSeq model
     */
    public static OnnxRecognitionPredictorProperties parSeq(String modelPath, Vocabulary vocabulary,
            int additionalTokens, IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(vocabulary, additionalTokens),
                ortSessionOptionsCreator
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a SAR model
     */
    public static OnnxRecognitionPredictorProperties sar(String modelPath) {
        return sar(modelPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a SAR model
     */
    public static OnnxRecognitionPredictorProperties sar(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0),
                ortSessionOptionsCreator
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text recognition properties object for a ViTSTR model
     */
    public static OnnxRecognitionPredictorProperties viTstr(String modelPath) {
        return viTstr(modelPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models cannot handle spaces. Make sure you choose a detection
     * model that outputs words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a ViTSTR model
     */
    public static OnnxRecognitionPredictorProperties viTstr(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                new EndOfStringPostProcessor(Vocabulary.FRENCH, 0),
                ortSessionOptionsCreator
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
     * @return a new text recognition properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictorProperties paddleOcr(String modelDirPath) throws IOException {
        return paddleOcr(modelDirPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictorProperties paddleOcr(String modelDirPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) throws IOException {
        return paddleOcr(modelDirPath + "/inference.onnx", modelDirPath + "/inference.yml", ortSessionOptionsCreator);
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
     * <p>
     * These models can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     *
     * @return a new text recognition properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictorProperties paddleOcr(String modelPath, String configPath) throws IOException {
        return paddleOcr(modelPath, configPath, DEFAULT_ORT_SESSION_CREATOR);
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
     * <p>
     * These models can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxRecognitionPredictorProperties paddleOcr(String modelPath, String configPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) throws IOException {
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
                modelPath, inputProperties, postProcessor, false, ortSessionOptionsCreator
        );
    }

    /**
     * Creates a new text recognition properties object for existing
     * pre-trained EasyOCR models, stored on disk.
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
     * @return a new text recognition properties object for a EasyOCR model
     */
    public static OnnxRecognitionPredictorProperties easyOcr(String modelPath, EasyOcrMapper labelMapper) {
        return easyOcr(modelPath, labelMapper, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text recognition properties object for existing
     * pre-trained EasyOCR models, stored on disk.
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
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     *
     * @return a new text recognition properties object for a EasyOCR model
     */
    public static OnnxRecognitionPredictorProperties easyOcr(String modelPath, EasyOcrMapper labelMapper,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxRecognitionPredictorProperties(
                modelPath,
                EASY_OCR_INPUT_PROPERTIES,
                new CtcLabelPostProcessor(labelMapper),
                false,
                ortSessionOptionsCreator
        );
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
                Objects.equals(postProcessor, that.postProcessor) &&
                Objects.equals(ortSessionOptionsCreator, that.ortSessionOptionsCreator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object) modelPath, inputProperties, postProcessor, splitImages, ortSessionOptionsCreator);
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
                PdfOcrOnnxExceptionMessageConstant.UNEXPECTED_CHANNEL_CONFIGURATION
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
