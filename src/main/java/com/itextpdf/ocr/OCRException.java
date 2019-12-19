package com.itextpdf.ocr;

/**
 * Exception class for custom exceptions.
 *
 */
public class OCRException extends com.itextpdf.io.IOException {

    public static final String OUTPUT_INTENT_CANNOT_BE_NULL = "Output intent can not be null for PDF/A-3u document";
    public static final String INCORRECT_INPUT_IMAGE_FORMAT = "{0} format is not supported.";
    public static final String CANNOT_READ_INPUT_IMAGE = "Cannot read input image";
    public static final String TESSERACT_FAILED = "Tesseract failed. Please check provided paramters";
    public static final String CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE = "Cannot find path to tesseract executable.";

    public OCRException(String msg) {
        super(msg);
    }
}
