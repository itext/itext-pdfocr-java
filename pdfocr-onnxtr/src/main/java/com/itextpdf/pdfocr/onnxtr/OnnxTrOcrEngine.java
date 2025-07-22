/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.util.PdfOcrTextBuilder;
import com.itextpdf.pdfocr.util.TiffImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 * {@link IOcrEngine} implementation, based on OnnxTR/DocTR machine learning OCR projects.
 *
 * <p>
 * NOTE: {@link OnnxTrOcrEngine} instance shall be closed after all usages to avoid native allocations leak.
 */
public class OnnxTrOcrEngine implements IOcrEngine, AutoCloseable {
    /**
     * Text detector. For an input image it outputs a list of text boxes.
     */
    private final IDetectionPredictor detectionPredictor;

    /**
     * Text orientation predictor. For an input image, which is a tight crop of text, it outputs its orientation
     * in 90 degrees steps. Can be null.
     */
    private final IOrientationPredictor orientationPredictor;

    /**
     * Text recognizer. For an input image, which is a tight crop of text, it outputs the displayed string.
     */
    private final IRecognitionPredictor recognitionPredictor;

    /**
     * Create a new OCR engine with the provided predictors.
     *
     * @param detectionPredictor   text detector. For an input image it outputs a list of text boxes
     * @param orientationPredictor text orientation predictor. For an input image, which is a tight  crop of text,
     *                             it outputs its orientation in 90 degrees steps. Can be null, in that case all text
     *                             is assumed to be upright
     * @param recognitionPredictor text recognizer. For an input image, which is a tight crop of text, it outputs the
     *                             displayed string
     */
    public OnnxTrOcrEngine(IDetectionPredictor detectionPredictor, IOrientationPredictor orientationPredictor,
                           IRecognitionPredictor recognitionPredictor) {
        this.detectionPredictor = Objects.requireNonNull(detectionPredictor);
        this.orientationPredictor = orientationPredictor;
        this.recognitionPredictor = Objects.requireNonNull(recognitionPredictor);
    }

    /**
     * Create a new OCR engine with the provided predictors, without text orientation prediction.
     *
     * @param detectionPredictor text detector. For an input image it outputs a list of text boxes
     * @param recognitionPredictor text recognizer. For an input image, which is a tight crop of text,
     *                             it outputs the displayed string
     */
    public OnnxTrOcrEngine(IDetectionPredictor detectionPredictor, IRecognitionPredictor recognitionPredictor) {
        this(detectionPredictor, null, recognitionPredictor);
    }

    @Override
    public void close() throws Exception {
        detectionPredictor.close();
        if (orientationPredictor != null) {
            orientationPredictor.close();
        }
        recognitionPredictor.close();
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        return doImageOcr(input, null);
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
        final List<BufferedImage> images = getImages(input);
        return new OnnxTrProcessor(detectionPredictor, orientationPredictor, recognitionPredictor).doOcr(images);
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile) {
        createTxtFile(inputImages, txtFile, null);
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext) {
        StringBuilder content = new StringBuilder();
        for (File inputImage : inputImages) {
            Map<Integer, List<TextInfo>> outputMap = doImageOcr(inputImage, ocrProcessContext);
            content.append(PdfOcrTextBuilder.buildText(outputMap));
        }
        writeToTextFile(txtFile.getAbsolutePath(), content.toString());
    }

    @Override
    public boolean isTaggingSupported() {
        return false;
    }

    /**
     * Writes provided {@link java.lang.String} to text file using provided path.
     *
     * @param path path as {@link java.lang.String} to file to be created
     * @param data text data in required format as {@link java.lang.String}
     */
    private static void writeToTextFile(final String path, final String data) {
        try (Writer writer = new OutputStreamWriter(FileUtil.getFileOutputStream(path), StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            throw new PdfOcrException(MessageFormatUtil.format(PdfOcrExceptionMessageConstant.CANNOT_WRITE_TO_FILE,
                    path, e.getMessage()), e);
        }
    }

    private static List<BufferedImage> getImages(File input) {
        try {
            if (TiffImageUtil.isTiffImage(input)) {
                List<BufferedImage> images = TiffImageUtil.getAllImages(input);
                if (images.size() == 0) {
                    throw new PdfOcrInputException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE);
                }

                return images;
            } else {
                BufferedImage image = ImageIO.read(input);
                if (image == null) {
                    throw new PdfOcrInputException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE);
                }

                return Collections.singletonList(image);
            }
        } catch (Exception e) {
            throw new PdfOcrInputException(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_READ_IMAGE, e);
        }
    }
}
