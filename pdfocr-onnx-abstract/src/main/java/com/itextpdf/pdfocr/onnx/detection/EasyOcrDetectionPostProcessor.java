/*
    Copyright (c) 2020 JaidedAI Authors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.itextpdf.pdfocr.onnx.detection;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.pdfocr.onnx.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnx.FloatBufferWrapper;
import com.itextpdf.pdfocr.onnx.detection.score.IScoreCalculator;
import com.itextpdf.pdfocr.onnx.detection.score.MaxScoreCalculator;
import com.itextpdf.pdfocr.onnx.merging.EasyOcrTextBoxMerger;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Implementation of a text detection predictor post-processor, used for
 * EasyOCR model outputs.
 */
public class EasyOcrDetectionPostProcessor extends BasicDetectionPostProcessor {
    /**
     * Threshold for binarization of the text score array.
     */
    private static final float TEXT_BINARIZATION_THRESHOLD = 0.4F;
    /**
     * Threshold for binarization of the link score array.
     */
    private static final float LINK_BINARIZATION_THRESHOLD = 0.4F;

    /**
     * Creates a new post-processor.
     *
     * @param scoreThreshold score threshold for a detected box. If score is lower than this value,
     *                       the box gets discarded
     */
    public EasyOcrDetectionPostProcessor(float scoreThreshold) {
        super(1.0F, scoreThreshold, Integer.MAX_VALUE);
    }

    /**
     * Creates a new post-processor with the default parameters.
     */
    public EasyOcrDetectionPostProcessor() {
        this(0.7F);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point[]> process(BufferedImage input, FloatBufferMdArray output) {
        List<Point[]> result = super.process(input, output);
        return applyTextBoxMerger(result);
    }

    /**
     * The text detection model from EasyOCR, for the most part, returns
     * words or small groups of words. Since the EasyOCR text recognition
     * models expect lines as input, we need to merge the boxes.
     *
     * @param detectedTextBoxes list of rotated text boxes, provided by the
     * text detection routine
     *
     * @return a new list with merged text boxes
     */
    protected List<Point[]> applyTextBoxMerger(List<Point[]> detectedTextBoxes) {
        return new EasyOcrTextBoxMerger().process(detectedTextBoxes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FloatBufferMdArray getMaskSourceArray(FloatBufferMdArray output) {
        /*
         * Mask for finding contours is based on there being either text or
         * link data. So we are creating a new buffer, where they are
         * combined.
         */
        final FloatBufferWrapper textScoreBuffer = output.getSubArray(0).getData();
        final FloatBufferWrapper linkScoreBuffer = output.getSubArray(1).getData();
        final int height = output.getDimension(1);
        final int width = output.getDimension(2);
        final int size = height * width;
        final FloatBufferWrapper maskSourceBuffer = FloatBufferWrapper.allocate(height * width);
        for (int i = 0; i < size; ++i) {
            final float text = textScoreBuffer.get() >= TEXT_BINARIZATION_THRESHOLD ? 1.0F : 0.0F;
            final float link = linkScoreBuffer.get() >= LINK_BINARIZATION_THRESHOLD ? 1.0F : 0.0F;
            maskSourceBuffer.put(text + link);
        }
        maskSourceBuffer.rewind();
        return new FloatBufferMdArray(maskSourceBuffer, new long[]{height, width});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IScoreCalculator createScoreCalculator() {
        return new MaxScoreCalculator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double calcTextBoxEnlargement(double width, double height) {
        /*
         * In EasyOCR it is calculated as `2 * sqrt(area / max(w, h))`. But in
         * their case they use the area of the contour. Since we are not
         * calculating that, but are working on a box instead, it got
         * simplified further.
         */
        return 2.0 * Math.sqrt(Math.min(width, height));
    }
}
