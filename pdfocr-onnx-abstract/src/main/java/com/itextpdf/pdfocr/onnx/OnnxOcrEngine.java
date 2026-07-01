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
package com.itextpdf.pdfocr.onnx;

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
import com.itextpdf.pdfocr.onnx.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
import com.itextpdf.pdfocr.onnx.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnx.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnx.text.TextPositioning;
import com.itextpdf.pdfocr.util.ByteArrayStreamUtil;
import com.itextpdf.pdfocr.util.PdfOcrFileUtil;
import com.itextpdf.pdfocr.util.PdfOcrTextBuilder;
import com.itextpdf.pdfocr.util.TiffImageUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IOcrEngine} implementation, based on OnnxTR/DocTR machine learning OCR projects.
 *
 * <p>
 * NOTE: {@link OnnxOcrEngine} instance shall be closed after all usages to avoid native allocations leak.
 */
public class OnnxOcrEngine implements IOcrEngine, AutoCloseable, IProductAware {
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
    private final OnnxEngineProperties properties;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OnnxOcrEngine.class);

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
    public OnnxOcrEngine(IDetectionPredictor detectionPredictor, IOrientationPredictor orientationPredictor,
                           IRecognitionPredictor recognitionPredictor) {
        this(detectionPredictor, orientationPredictor, recognitionPredictor, new OnnxEngineProperties());
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
    public OnnxOcrEngine(IDetectionPredictor detectionPredictor, IOrientationPredictor orientationPredictor,
                           IRecognitionPredictor recognitionPredictor, OnnxEngineProperties properties) {
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
    public OnnxOcrEngine(IDetectionPredictor detectionPredictor, IRecognitionPredictor recognitionPredictor) {
        this(detectionPredictor, null, recognitionPredictor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        detectionPredictor.close();
        if (orientationPredictor != null) {
            orientationPredictor.close();
        }
        recognitionPredictor.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input) {
        return doImageOcr(input, new OcrProcessContext(new OnnxEventHelper()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(File input, OcrProcessContext ocrProcessContext) {
        return doImageOcr(Collections.singletonList(input), ocrProcessContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(List<File> inputs) {
        return doImageOcr(inputs, new OcrProcessContext(new OnnxEventHelper()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<TextInfo>> doImageOcr(List<File> inputs, OcrProcessContext ocrProcessContext) {
        return doImageOcrInternal(PdfOcrFileUtil.convertToInputStreams(inputs), ocrProcessContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTxtFile(List<File> inputImages, File txtFile) {
        createTxtFile(inputImages, txtFile, new OcrProcessContext(new OnnxEventHelper()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTxtFile(List<File> inputImages, File txtFile, OcrProcessContext ocrProcessContext) {
        createTxtFileInternal(PdfOcrFileUtil.convertToInputStreams(inputImages), PdfOcrFileUtil.convertToOutputStream(txtFile),
                ocrProcessContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTaggingSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PdfOcrMetaInfoContainer getMetaInfoContainer() {
        return new PdfOcrMetaInfoContainer(new OnnxMetaInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductData getProductData() {
        return null;
    }

    /**
     * Reads data from the provided input image stream and returns retrieved data
     * in the format described below.
     *
     * @param input input stream {@link java.io.InputStream}
     *
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    public Map<Integer, List<TextInfo>> doImageOcr(InputStream input) {
        return doImageOcr(input, new OcrProcessContext(new OnnxEventHelper()));
    }

    /**
     * Reads data from the provided input image stream and returns retrieved data
     * in the format described below.
     *
     * @param input input image {@link java.io.InputStream}
     * @param ocrProcessContext ocr processing context
     *
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    public Map<Integer, List<TextInfo>> doImageOcr(InputStream input, OcrProcessContext ocrProcessContext) {
        return doImageOcrInternal(Collections.singletonList(input), ocrProcessContext);
    }

    /**
     * Performs OCR using provided {@link IOcrEngine} for the given
     * input image and saves result into provided {@link FileOutputStream} with UTF-8 encoding.
     * Note that a human reading order is not guaranteed
     * due to possible specifics of input images (multi column layout, tables etc)
     *
     * @param inputImage image {@link java.io.InputStream}
     * @param outputStream output stream {@link FileOutputStream}
     */
    public void createTxtFile(InputStream inputImage, FileOutputStream outputStream) {
        createTxtFileInternal(Collections.singletonList(inputImage), outputStream, new OcrProcessContext(new OnnxEventHelper()));
    }

    private Map<Integer, List<TextInfo>> doImageOcrInternal(List<InputStream> inputs,
            OcrProcessContext ocrProcessContext) {

        Map<Integer, List<TextInfo>> result = doOnnxOcr(inputs, ocrProcessContext);
        if (TextPositioning.BY_WORDS.equals(properties.getTextPositioning())) {
            PdfOcrTextBuilder.sortTextInfosByLines(result);
        } else if (TextPositioning.BY_LINES.equals(properties.getTextPositioning())) {
            PdfOcrTextBuilder.collectWordsIntoLines(result);
        } else {
            // Use TextPositioning.BY_WORDS_AND_LINES by default.
            PdfOcrTextBuilder.generifyWordBBoxesByLine(result);
        }
        return result;
    }

    private void createTxtFileInternal(List<InputStream> inputImages, OutputStream outputStream,
            OcrProcessContext ocrProcessContext) {
        LOGGER.info(MessageFormatUtil.format(PdfOcrLogMessageConstant.START_OCR_FOR_IMAGES, inputImages.size()));

        AbstractPdfOcrEventHelper storedEventHelper;
        if (ocrProcessContext.getOcrEventHelper() == null) {
            storedEventHelper = new OnnxEventHelper();
        } else {
            storedEventHelper = ocrProcessContext.getOcrEventHelper();
        }

        try {
            // save confirm events from doImageOcr, to send them only after successful writing to the file
            OnnxFileResultEventHelper fileResultEventHelper = new OnnxFileResultEventHelper(storedEventHelper);
            ocrProcessContext.setOcrEventHelper(fileResultEventHelper);

            Map<Integer, List<TextInfo>> outputMap = doOnnxOcr(inputImages, ocrProcessContext);
            String content = PdfOcrTextBuilder.buildText(outputMap);
            PdfOcrFileUtil.writeToStream(outputStream, content);

            fileResultEventHelper.registerAllSavedEvents();
        } finally {
            ocrProcessContext.setOcrEventHelper(storedEventHelper);
        }
    }

    static List<BufferedImage> getImages(ByteArrayInputStream input) {
        try {
            if (TiffImageUtil.isTiffImage(input)) {
                List<BufferedImage> images = TiffImageUtil.getAllImages(input);
                if (images.isEmpty()) {
                    throw new PdfOcrInputException(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE);
                }
                return images;
            } else {
                BufferedImage image = ImageIO.read(input);
                if (image == null) {
                    throw new PdfOcrInputException(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE);
                }
                return Collections.singletonList(image);
            }
        } catch (Exception e) {
            throw new PdfOcrInputException(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE, e);
        }
    }

    /**
     * Reads raw data from the provided input image files and returns retrieved data
     * in the format described below.
     *
     * @param inputStreams {@link java.util.List} of input image files
     * @param ocrProcessContext ocr processing context
     *
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     */
    private Map<Integer, List<TextInfo>> doOnnxOcr(List<InputStream> inputStreams, OcrProcessContext ocrProcessContext) {
        final List<BufferedImage> images = new ArrayList<>();
        for (InputStream stream : inputStreams) {
            images.addAll(getImages(ByteArrayStreamUtil.createByteArrayInputStream(stream)));
        }
        OnnxProcessor onnxProcessor = new OnnxProcessor(detectionPredictor, orientationPredictor, recognitionPredictor);
        return onnxProcessor.doOcr(images, ocrProcessContext);
    }
}
