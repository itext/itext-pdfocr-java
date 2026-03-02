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
package com.itextpdf.pdfocr.onnx.exceptions;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;

/**
 * Exception class for exceptions during PaddleOCR initialization.
 */
public class PaddleOcrInitException extends PdfOcrException {

    /**
     * Creates new {@link PaddleOcrInitException} instance.
     *
     * @param message exception message
     */
    protected PaddleOcrInitException(String message) {
        super(message);
    }

    /**
     * Creates an exception for cases, when the detection model does not
     * return quads.
     *
     * @return the created exception
     */
    public static PaddleOcrInitException boxTypeIsNotSupported() {
        return new PaddleOcrInitException(
                PdfOcrOnnxTrExceptionMessageConstant.BOX_TYPE_IS_NOT_SUPPORTED
        );
    }

    /**
     * Creates an exception to assert {@code channel_first} is set to
     * {@code false}.
     *
     * @return the created exception
     */
    public static PaddleOcrInitException channelFirstIsNotSupported() {
        return new PaddleOcrInitException(
                PdfOcrOnnxTrExceptionMessageConstant.CHANNEL_FIRST_IS_NOT_SUPPORTED
        );
    }

    /**
     * Creates an exception for cases, when the detection model uses an
     * unsupported method for resizing input images.
     *
     * @return the created exception
     */
    public static PaddleOcrInitException imageShapeIsNotSupported() {
        return new PaddleOcrInitException(
                PdfOcrOnnxTrExceptionMessageConstant.IMAGE_SHAPE_IS_NOT_SUPPORTED
        );
    }

    /**
     * Creates an exception for cases, when an expected pre-processing
     * operation is missing in the configuration file.
     *
     * @param name name of the missing pre-processing operation
     *
     * @return the created exception
     */
    public static PaddleOcrInitException preProcessorOperationMissing(String name) {
        return new PaddleOcrInitException(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.PRE_PROCESSOR_OPERATION_MISSING, name
        ));
    }

    /**
     * Creates an exception for cases, when the detection model uses an
     * unsupported score calculation method.
     *
     * @return the created exception
     */
    public static PaddleOcrInitException scoreModeIsNotSupported() {
        return new PaddleOcrInitException(
                PdfOcrOnnxTrExceptionMessageConstant.SCORE_MODE_IS_NOT_SUPPORTED
        );
    }

    /**
     * Creates an exception for cases, when the size of the array of means for
     * normalization has an unexpected size.
     *
     * @param expectedCount expected size of the array
     *
     * @return the created exception
     */
    public static PaddleOcrInitException unexpectedMeanChannelCount(int expectedCount) {
        return new PaddleOcrInitException(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MEAN_CHANNEL_COUNT, expectedCount
        ));
    }


    /**
     * Creates an exception for cases, when an unexpected post-processor is
     * specified in the configuration file.
     *
     * @param name name of the post-processor that was found
     *
     * @return the created exception
     */
    public static PaddleOcrInitException unexpectedPostProcessorType(String name) {
        return new PaddleOcrInitException(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_POST_PROCESSOR_TYPE, name
        ));
    }

    /**
     * Creates an exception for cases, when the size of the array of standard
     * deviations for normalization has an unexpected size.
     *
     * @param expectedCount expected size of the array
     *
     * @return the created exception
     */
    public static PaddleOcrInitException unexpectedStdChannelCount(int expectedCount) {
        return new PaddleOcrInitException(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_STD_CHANNEL_COUNT, expectedCount
        ));
    }

    /**
     * Creates an exception for cases, when the detection model uses an
     * unsupported pre-processing step for input images.
     *
     * @return the created exception
     */
    public static PaddleOcrInitException useDilationIsNotSupported() {
        return new PaddleOcrInitException(
                PdfOcrOnnxTrExceptionMessageConstant.USE_DILATION_IS_NOT_SUPPORTED
        );
    }
}
