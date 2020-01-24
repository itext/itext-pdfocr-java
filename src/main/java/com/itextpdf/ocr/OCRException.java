package com.itextpdf.ocr;

/**
 * Exception class for custom exceptions.
 *
 */
public class OCRException extends com.itextpdf.io.IOException {

    public static final String OUTPUT_INTENT_CANNOT_BE_NULL = "Output intent "
            + "can not be null for PDF/A-3u document";
    public static final String INCORRECT_INPUT_IMAGE_FORMAT = "{0} format is "
            + "supported.";
    public static final String INCORRECT_LANGUAGE = "{0} does not exist in {1}";
    public static final String CANNOT_READ_INPUT_IMAGE = "Cannot read "
            + "input image";
    public static final String TESSERACT_FAILED = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String TESSERACT_FAILED_WITH_REASON = "Tesseract "
            + "failed. {0}";
    public static final String CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE =
            "Cannot find path to tesseract executable.";

    public OCRException(String msg) {
        super(msg);
    }
}
