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

import com.itextpdf.commons.actions.data.ProductData;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.AbstractPdfOcrEventHelper;
import com.itextpdf.pdfocr.IOcrEngine;
import com.itextpdf.pdfocr.IProductAware;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.PdfOcrMetaInfoContainer;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.util.PdfOcrFileUtil;
import com.itextpdf.pdfocr.util.PdfOcrTextBuilder;
import com.itextpdf.pdfocr.util.TiffImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.slf4j.LoggerFactory;

/**
 * {@link IOcrEngine} implementation, based on OnnxTR/DocTR machine learning OCR projects.
 *
 * <p>
 * NOTE: {@link OnnxTrOcrEngine} instance shall be closed after all usages to avoid native allocations leak.
 */
public class OnnxTrOcrEngine implements IOcrEngine, AutoCloseable, IProductAware {
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
     * Set of properties.
     */
    private final OnnxTrEngineProperties properties;

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
        this(detectionPredictor, orientationPredictor, recognitionPredictor, new OnnxTrEngineProperties());
    }

    /**
     * Create a new OCR engine with the provided predictors.
     *
     * @param detectionPredictor   text detector. For an input image it outputs a list of text boxes
     * @param orientationPredictor text orientation predictor. For an input image, which is a tight  crop of text,
     *                             it outputs its orientation in 90 degrees steps. Can be null, in that case all text
     *                             is assumed to be upright
     * @param recognitionPredictor text recognizer. For an input image, which is a tight crop of text, it outputs the
     *                             displayed string
     * @param properties           set of properties
     */
    public OnnxTrOcrEngine(IDetectionPredictor detectionPredictor, IOrientationPredictor orientationPredictor,
                           IRecognitionPredictor recognitionPredictor, OnnxTrEngineProperties properties) {
        this.detectionPredictor = Objects.requireNonNull(detectionPredictor);
        this.orientationPredictor = orientationPredictor;
        this.recognitionPredictor = Objects.requireNonNull(recognitionPredictor);
        this.properties = properties;
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
        return doImageOcr(input, new OcrProcessContext(new OnnxTrEventHelper()));
    }

    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
        Map<Integer, List<TextInfo>> result = doOnnxTrOcr(input, ocrProcessContext);
        if (TextPositioning.BY_WORDS.equals(properties.getTextPositioning())) {
            PdfOcrTextBuilder.sortTextInfosByLines(result);
        } else {
            PdfOcrTextBuilder.generifyWordBBoxesByLine(result);
        }
        return result;
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile) {
        createTxtFile(inputImages, txtFile, new OcrProcessContext(new OnnxTrEventHelper()));
    }

    @Override
    public void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext) {
        LoggerFactory.getLogger(getClass()).info(
                MessageFormatUtil.format(PdfOcrLogMessageConstant.START_OCR_FOR_IMAGES, inputImages.size()));

        AbstractPdfOcrEventHelper storedEventHelper;
        if (ocrProcessContext.getOcrEventHelper() == null) {
            storedEventHelper = new OnnxTrEventHelper();
        } else {
            storedEventHelper = ocrProcessContext.getOcrEventHelper();
        }

        try {
            // save confirm events from doImageOcr, to send them only after successful writing to the file
            OnnxTrFileResultEventHelper fileResultEventHelper = new OnnxTrFileResultEventHelper(storedEventHelper);
            ocrProcessContext.setOcrEventHelper(fileResultEventHelper);

            StringBuilder content = new StringBuilder();
            for (File inputImage : inputImages) {
                Map<Integer, List<TextInfo>> outputMap = doOnnxTrOcr(inputImage, ocrProcessContext);
                content.append(PdfOcrTextBuilder.buildText(outputMap));
            }
            PdfOcrFileUtil.writeToTextFile(txtFile.getAbsolutePath(), content.toString());

            fileResultEventHelper.registerAllSavedEvents();
        } finally {
            ocrProcessContext.setOcrEventHelper(storedEventHelper);
        }
    }

    @Override
    public boolean isTaggingSupported() {
        return false;
    }

    @Override
    public PdfOcrMetaInfoContainer getMetaInfoContainer() {
        return new PdfOcrMetaInfoContainer(new OnnxTrMetaInfo());
    }

    @Override
    public ProductData getProductData() {
        return null;
    }

    /**
     * Reads raw data from the provided input image file and returns retrieved data
     * in the format described below.
     *
     * @param input input image {@link java.io.File}
     * @param ocrProcessContext ocr processing context
     *
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    private Map<Integer, List<TextInfo>> doOnnxTrOcr(File input, OcrProcessContext ocrProcessContext) {
        final List<BufferedImage> images = getImages(input);
        OnnxTrProcessor onnxTrProcessor = new OnnxTrProcessor(detectionPredictor, orientationPredictor,
                recognitionPredictor);
        return onnxTrProcessor.doOcr(images, ocrProcessContext);
    }

    private static List<BufferedImage> getImages(File input) {
        try {
            if (TiffImageUtil.isTiffImage(input)) {
                List<BufferedImage> images = TiffImageUtil.getAllImages(input);
                if (images.isEmpty()) {
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
