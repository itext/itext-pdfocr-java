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
package com.itextpdf.pdfocr.onnx.detection;

import com.itextpdf.pdfocr.onnx.AbstractOnnxPredictorProperties;
import com.itextpdf.pdfocr.onnx.IOrtSessionOptionsCreator;
import com.itextpdf.pdfocr.onnx.ImageChannelConfiguration;
import com.itextpdf.pdfocr.onnx.ImageResizeOptions;
import com.itextpdf.pdfocr.onnx.OnnxInputProperties;
import com.itextpdf.pdfocr.onnx.PaddingStrategy;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.BoxType;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DbPostProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DecodeImage;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.DetResizeForTest;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ImgMode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.InferenceConfig;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.NormalizeImage;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.PostProcess;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.ScoreMode;
import com.itextpdf.pdfocr.onnx.conf.paddle.model.TransformOp;
import com.itextpdf.pdfocr.onnx.conf.paddle.parser.InferenceConfigParser;
import com.itextpdf.pdfocr.onnx.exceptions.PaddleOcrInitException;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Properties for configuring text detection ONNX models.
 *
 * <p>
 * It contains a path to the model, model input properties and a model
 * output post-processor.
 */
public class OnnxDetectionPredictorProperties extends AbstractOnnxPredictorProperties {
    private static final OnnxInputProperties DEFAULT_INPUT_PROPERTIES = new OnnxInputProperties(
            new ImageResizeOptions(
                    ImageChannelConfiguration.RGB,
                    1024, 1024,
                    PaddingStrategy.SYMMETRIC_BLACK
            ),
            new float[]{0.798F, 0.785F, 0.772F},
            new float[]{0.264F, 0.2749F, 0.287F}
    );
    private static final IDetectionPostProcessor DEFAULT_POST_PROCESSOR =
            new OnnxDetectionPostProcessor();
    /*
     * By default, DBNet has different thresholds for binarization and for
     * discarding results.
     */
    private static final IDetectionPostProcessor DB_NET_POST_PROCESSOR =
            new OnnxDetectionPostProcessor(0.3F, 0.1F);

    private static final int PADDLE_LIMIT_SIDE_LEN = 64;
    private static final int PADDLE_MAX_SIDE_LIMIT = 4000;
    private static final int PADDLE_SIDE_MULTIPLE = 32;
    private static final int PADDLE_BATCH_SIZE = 1;

    private static final OnnxInputProperties EASY_OCR_INPUT_PROPERTIES = new OnnxInputProperties(
            /*
             * This will work a bit differently to what is done in EasyOCR.
             * They first scale the image and then put it on top of a 32-multiple
             * black background. So in their case there will be padding on both bottom
             * and right, where the image is padded to 32 chunks.
             *
             * In our case the image is scaled to the "multiple" canvas, so there
             * will be padding only on one side.
             *
             * Shouldn't, really, matter that much.
             */
            new ImageResizeOptions(
                    ImageChannelConfiguration.RGB,
                    32, 32,
                    2560, 2560,
                    32, 32,
                    PaddingStrategy.BOTTOM_RIGHT_BLACK
            ),
            new float[] {0.485F, 0.456F, 0.406F},
            new float[] {0.229F, 0.224F, 0.225F}
    );
    private static final EasyOcrDetectionPostProcessor EASY_OCR_POST_PROCESSOR =
            new EasyOcrDetectionPostProcessor();

    /**
     * Post-processor of the outputs of the ONNX model. Converts the mask-like
     * output of the model to rotated text rectangles.
     */
    private final IDetectionPostProcessor postProcessor;

    /**
     * Creates new text detection predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     */
    public OnnxDetectionPredictorProperties(
            String modelPath,
            OnnxInputProperties inputProperties,
            IDetectionPostProcessor postProcessor
    ) {
        this(modelPath, inputProperties, postProcessor, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates new text detection predictor properties.
     *
     * @param modelPath path to the ONNX model to load
     * @param inputProperties ONNX model input properties
     * @param postProcessor ONNX model output post-processor
     * @param ortSessionOptionsCreator ONNX runtime session options creator
     */
    public OnnxDetectionPredictorProperties(
            String modelPath,
            OnnxInputProperties inputProperties,
            IDetectionPostProcessor postProcessor,
            IOrtSessionOptionsCreator ortSessionOptionsCreator
    ) {
        super(modelPath, inputProperties, ortSessionOptionsCreator);
        this.postProcessor = Objects.requireNonNull(postProcessor);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * DBNet models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet50-69ba0015.onnx">
     *             db_resnet50
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet50_static_8_bit-09a6104f.onnx">
     *             db_resnet50 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet34-b4873198.onnx">
     *             db_resnet34
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet34_static_8_bit-027e2c7f.onnx">
     *             db_resnet34 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large-4987e7bd.onnx">
     *             db_mobilenet_v3_large
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large_static_8_bit-535a6f25.onnx">
     *             db_mobilenet_v3_large (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text detection properties object for a DBNet model
     */
    public static OnnxDetectionPredictorProperties dbNet(String modelPath) {
        return dbNet(modelPath, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * DBNet models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet50-69ba0015.onnx">
     *             db_resnet50
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet50_static_8_bit-09a6104f.onnx">
     *             db_resnet50 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/db_resnet34-b4873198.onnx">
     *             db_resnet34
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/db_resnet34_static_8_bit-027e2c7f.onnx">
     *             db_resnet34 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large-4987e7bd.onnx">
     *             db_mobilenet_v3_large
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.2.0/db_mobilenet_v3_large_static_8_bit-535a6f25.onnx">
     *             db_mobilenet_v3_large (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text detection properties object for a DBNet model
     */
    public static OnnxDetectionPredictorProperties dbNet(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                DB_NET_POST_PROCESSOR,
                ortSessionOptionsCreator
        );
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * FAST models, stored on disk. This is the default text detection model in
     * OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_base-1b89ebf9.onnx">
     *             fast_base
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_small-10428b70.onnx">
     *             fast_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_tiny-28867779.onnx">
     *             fast_tiny
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text detection properties object for a FAST model
     */
    public static OnnxDetectionPredictorProperties fast(String modelPath) {
        return fast(modelPath, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * FAST models, stored on disk. This is the default text detection model in
     * OnnxTR.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_base-1b89ebf9.onnx">
     *             fast_base
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_small-10428b70.onnx">
     *             fast_small
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/rep_fast_tiny-28867779.onnx">
     *             fast_tiny
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text detection properties object for a FAST model
     */
    public static OnnxDetectionPredictorProperties fast(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                DEFAULT_POST_PROCESSOR,
                ortSessionOptionsCreator
        );
    }

    /**
     * Creates a new text detection properties object for existing pre-trained LinkNet models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet50-15d8c4ec.onnx">
     *             linknet_resnet50
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet50_static_8_bit-65d6b0b8.onnx">
     *             linknet_resnet50 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet34-93e39a39.onnx">
     *             linknet_resnet34
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet34_static_8_bit-2824329d.onnx">
     *             linknet_resnet34 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet18-e0e0b9dc.onnx">
     *             linknet_resnet18
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet18_static_8_bit-3b3a37dd.onnx">
     *             linknet_resnet18 (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of words.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text detection properties object for a LinkNet model
     */
    public static OnnxDetectionPredictorProperties linkNet(String modelPath) {
        return linkNet(modelPath, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained LinkNet models, stored on disk.
     *
     * <p>
     * This can be used to load the following models from OnnxTR:
     * <ul>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet50-15d8c4ec.onnx">
     *             linknet_resnet50
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet50_static_8_bit-65d6b0b8.onnx">
     *             linknet_resnet50 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet34-93e39a39.onnx">
     *             linknet_resnet34
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet34_static_8_bit-2824329d.onnx">
     *             linknet_resnet34 (8-bit quantized)
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.0.1/linknet_resnet18-e0e0b9dc.onnx">
     *             linknet_resnet18
     *         </a>
     *     <li>
     *         <a href="https://github.com/felixdittrich92/OnnxTR/releases/download/v0.1.2/linknet_resnet18_static_8_bit-3b3a37dd.onnx">
     *             linknet_resnet18 (8-bit quantized)
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of words.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text detection properties object for a LinkNet model
     */
    public static OnnxDetectionPredictorProperties linkNet(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                DEFAULT_INPUT_PROPERTIES,
                DEFAULT_POST_PROCESSOR,
                ortSessionOptionsCreator
        );
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * PaddleOCR models, stored on disk.
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_det_infer">
     *             PP-OCRv5_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_det_infer">
     *             PP-OCRv5_mobile_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_det_infer">
     *             PP-OCRv4_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_det_infer">
     *             PP-OCRv4_mobile_det
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of text lines. Make sure you choose a
     * recognition model that can handle spaces.
     *
     * @param modelDirPath path to the directory with the model and its
     *                     configuration file
     *
     * @return a new text detection properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxDetectionPredictorProperties paddleOcr(String modelDirPath) throws IOException {
        return paddleOcr(modelDirPath, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * PaddleOCR models, stored on disk.
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_det_infer">
     *             PP-OCRv5_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_det_infer">
     *             PP-OCRv5_mobile_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_det_infer">
     *             PP-OCRv4_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_det_infer">
     *             PP-OCRv4_mobile_det
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of text lines. Make sure you choose a
     * recognition model that can handle spaces.
     *
     * @param modelDirPath path to the directory with the model and its
     *                     configuration file
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text detection properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxDetectionPredictorProperties paddleOcr(String modelDirPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) throws IOException {
        return paddleOcr(modelDirPath + "/inference.onnx", modelDirPath + "/inference.yml", ortSessionOptionsCreator);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * PaddleOCR models, stored on disk.
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_det_infer">
     *             PP-OCRv5_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_det_infer">
     *             PP-OCRv5_mobile_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_det_infer">
     *             PP-OCRv4_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_det_infer">
     *             PP-OCRv4_mobile_det
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of text lines. Make sure you choose a
     * recognition model that can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     *
     * @return a new text detection properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxDetectionPredictorProperties paddleOcr(String modelPath, String configPath) throws IOException {
        return paddleOcr(modelPath, configPath, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text detection properties object for existing pre-trained
     * PaddleOCR models, stored on disk.
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
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_server_det_infer">
     *             PP-OCRv5_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv5_mobile_det_infer">
     *             PP-OCRv5_mobile_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_server_det_infer">
     *             PP-OCRv4_server_det
     *         </a>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-PP-OCRv4_mobile_det_infer">
     *             PP-OCRv4_mobile_det
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of text lines. Make sure you choose a
     * recognition model that can handle spaces.
     *
     * @param modelPath path to the pre-trained model in the ONNX format
     * @param configPath path to the configuration file for the model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text detection properties object for a PaddleOCR model
     *
     * @throws IOException if any I/O error occurs while loading configuration file
     */
    public static OnnxDetectionPredictorProperties paddleOcr(String modelPath, String configPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) throws IOException {
        final InferenceConfig config;
        try (final InputStream is = Files.newInputStream(Paths.get(configPath))) {
            config = InferenceConfigParser.parse(is);
        }
        final OnnxInputProperties inputProperties = createPaddleInputProperties(config);
        final PaddleOcrDetectionPostProcessor postProcessor = createPaddlePostProcessor(config);
        return new OnnxDetectionPredictorProperties(modelPath, inputProperties, postProcessor, ortSessionOptionsCreator);
    }

    /**
     * Creates a new text detection properties object for an existing
     * pre-trained EasyOCR CRAFT model, stored on disk.
     *
     * <p>
     * Only models in the ONNX format are supported. Since, by default,
     * EasyOCR does not provide models in the ONNX format, you might need to
     * do a model conversion yourself.
     *
     * <p>
     * This can be used to load the following models from EasyOCR:
     * <ul>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-craft_mlt_25k">
     *             CRAFT
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of text lines. Make sure you choose a
     * recognition model that can handle spaces.
     *
     * @param modelPath path to the pre-trained model
     *
     * @return a new text detection properties object for an EasyOCR CRAFT model
     */
    public static OnnxDetectionPredictorProperties easyOcr(String modelPath) {
        return easyOcr(modelPath, DEFAULT_ORT_SESSION_CREATOR);
    }

    /**
     * Creates a new text detection properties object for an existing
     * pre-trained EasyOCR CRAFT model, stored on disk.
     *
     * <p>
     * Only models in the ONNX format are supported. Since, by default,
     * EasyOCR does not provide models in the ONNX format, you might need to
     * do a model conversion yourself.
     *
     * <p>
     * This can be used to load the following models from EasyOCR:
     * <ul>
     *     <li>
     *         <a href="https://huggingface.co/itextresearch/itext-EasyOCR-craft_mlt_25k">
     *             CRAFT
     *         </a>
     * </ul>
     *
     * <p>
     * These models output boxes of text lines. Make sure you choose a
     * recognition model that can handle spaces.
     *
     * @param modelPath path to the pre-trained model
     * @param ortSessionOptionsCreator the ONNX runtime session options creator
     *
     * @return a new text detection properties object for an EasyOCR CRAFT model
     */
    public static OnnxDetectionPredictorProperties easyOcr(String modelPath,
            IOrtSessionOptionsCreator ortSessionOptionsCreator) {
        return new OnnxDetectionPredictorProperties(
                modelPath,
                EASY_OCR_INPUT_PROPERTIES,
                EASY_OCR_POST_PROCESSOR,
                ortSessionOptionsCreator
        );
    }

    /**
     * Returns the ONNX model output post-processor.
     *
     * @return the ONNX model output post-processor
     */
    public IDetectionPostProcessor getPostProcessor() {
        return postProcessor;
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
        final OnnxDetectionPredictorProperties that = (OnnxDetectionPredictorProperties) o;
        return Objects.equals(modelPath, that.modelPath) &&
                Objects.equals(inputProperties, that.inputProperties) &&
                Objects.equals(postProcessor, that.postProcessor) &&
                Objects.equals(ortSessionOptionsCreator, that.ortSessionOptionsCreator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object) modelPath, inputProperties, postProcessor, ortSessionOptionsCreator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OnnxDetectionPredictorProperties{" +
                "modelPath='" + modelPath + '\'' +
                ", inputProperties=" + inputProperties +
                ", postProcessor=" + postProcessor +
                '}';
    }

    private static OnnxInputProperties createPaddleInputProperties(InferenceConfig config) {
        final TransformOp[] ops = config.getPreProcess().getTransformOps();

        final DecodeImage decode = getPaddleOp(ops, DecodeImage.class, DecodeImage.WRAPPING_KEY);
        if (decode.getChannelFirst()) {
            throw PaddleOcrInitException.channelFirstIsNotSupported();
        }
        final ImageChannelConfiguration channelConfig = mapImgMode(decode.getImgMode());

        final NormalizeImage normalize = getPaddleOp(ops, NormalizeImage.class, NormalizeImage.WRAPPING_KEY);
        final float[] mean = normalize.getMean();
        if (mean.length != channelConfig.getChannelCount()) {
            throw PaddleOcrInitException.unexpectedMeanChannelCount(mean.length);
        }
        final float[] std = normalize.getStd();
        if (std.length != channelConfig.getChannelCount()) {
            throw PaddleOcrInitException.unexpectedStdChannelCount(std.length);
        }

        final DetResizeForTest resize = getPaddleOp(ops, DetResizeForTest.class, DetResizeForTest.WRAPPING_KEY);
        if (resize.getImageShape() != null) {
            throw PaddleOcrInitException.imageShapeIsNotSupported();
        }

        /*
         * From looking at the logic within PaddleOCR, it seems like there are
         * very few ways for the configuration file to, actually, affect the
         * resizing operation. The majority of the parameters come from a
         * global OCR config file, which is static. So the only things you are
         * getting from the model config file here is the channel
         * configuration. It can also be affected, if an `image_shape` key is
         * present, but we didn't add support for that anyway.
         */
        final ImageResizeOptions resizeOpts = new ImageResizeOptions(
                channelConfig,
                PADDLE_LIMIT_SIDE_LEN, PADDLE_LIMIT_SIDE_LEN,
                PADDLE_MAX_SIDE_LIMIT, PADDLE_MAX_SIDE_LIMIT,
                PADDLE_SIDE_MULTIPLE, PADDLE_SIDE_MULTIPLE,
                PaddingStrategy.BOTTOM_RIGHT_BLACK
        );
        return new OnnxInputProperties(resizeOpts, mean, std, PADDLE_BATCH_SIZE);
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

    private static PaddleOcrDetectionPostProcessor createPaddlePostProcessor(InferenceConfig config) {
        final PostProcess postProcess = config.getPostProcess();
        if (!(postProcess instanceof DbPostProcess)) {
            throw PaddleOcrInitException.unexpectedPostProcessorType(postProcess.getName());
        }
        final DbPostProcess db = (DbPostProcess) postProcess;
        if (db.getUseDilation()) {
            throw PaddleOcrInitException.useDilationIsNotSupported();
        }
        if (db.getScoreMode() != ScoreMode.FAST) {
            throw PaddleOcrInitException.scoreModeIsNotSupported();
        }
        if (db.getBoxType() != BoxType.QUAD) {
            throw PaddleOcrInitException.boxTypeIsNotSupported();
        }
        return new PaddleOcrDetectionPostProcessor(
                db.getThresh(),
                db.getBoxThresh(),
                db.getUnclipRatio(),
                db.getMaxCandidates()
        );
    }
}
