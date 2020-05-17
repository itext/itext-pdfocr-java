package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.OcrException;

public class Tesseract4OcrException extends OcrException {
    public static final String IncorrectInputImageFormat =
            "{0} format is not supported.";
    public static final String IncorrectLanguage =
            "{0} does not exist in {1}";
    public static final String LanguageIsNotInTheList =
            "Provided list of languages doesn't contain {0} language";
    public static final String CannotReadProvidedImage =
            "Cannot read input image {0}";
    public static final String TesseractFailed = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String CannotFindPathToTesseractExecutable =
            "Cannot find path to tesseract executable.";
    public static final String CannotFindPathToTessDataDirectory =
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
