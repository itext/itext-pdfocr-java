package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.OcrException;

public class Tesseract4OcrException extends OcrException {
    public static final String INCORRECT_INPUT_IMAGE_FORMAT =
            "{0} format is not supported.";
    public static final String INCORRECT_LANGUAGE =
            "{0} does not exist in {1}";
    public static final String LANGUAGE_IS_NOT_IN_THE_LIST =
            "Provided list of languages doesn't contain {0} language";
    public static final String CANNOT_READ_PROVIDED_IMAGE =
            "Cannot read input image {0}";
    public static final String TESSERACT_FAILED = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String TESSERACT_NOT_FOUND = "Tesseract failed. "
            + "Please check that tesseract is installed and provided path to "
            + "tesseract executable directory is correct";
    public static final String CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE =
            "Cannot find path to tesseract executable.";
    public static final String CANNOT_FIND_PATH_TO_TESS_DATA_DIRECTORY =
            "Cannot find path to tess data directory";

    /**
     * Creates a new TesseractException.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public Tesseract4OcrException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new TesseractException.
     *
     * @param msg the detail message.
     */
    public Tesseract4OcrException(String msg) {
        super(msg);
    }
}
