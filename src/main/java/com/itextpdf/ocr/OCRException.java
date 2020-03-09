package com.itextpdf.ocr;

/**
 * Exception class for custom exceptions.
 */
public class OCRException extends com.itextpdf.io.IOException {

    public static final String INCORRECT_INPUT_IMAGE_FORMAT = "{0} format is not "
            + "supported.";
    public static final String INCORRECT_LANGUAGE = "{0} does not exist in {1}";
    public static final String LANGUAGE_IS_NOT_IN_THE_LIST = "Provided list of languages "
            + "doesn't contain {0} language";
    public static final String CANNOT_READ_INPUT_IMAGE = "Cannot read "
            + "input image";
    public static final String CANNOT_READ_FONT = "Cannot read font";
    public static final String TESSERACT_FAILED = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String TESSERACT_FAILED_WITH_REASON = "Tesseract "
            + "failed. {0}";
    public static final String CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE =
            "Cannot find path to tesseract executable.";
    public static final String CANNOT_FIND_PATH_TO_TESSDATA = "Cannot find path to tess "
            + "data directory";

    public OCRException(String msg) {
        super(msg);
    }
}
