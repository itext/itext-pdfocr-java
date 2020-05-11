package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Exception class for custom exceptions.
 */
public class OcrException extends RuntimeException {

    public static final String IncorrectInputImageFormat =
            "{0} format is not supported.";
    public static final String IncorrectLanguage =
            "{0} does not exist in {1}";
    public static final String LanguageIsNotInTheList =
            "Provided list of languages doesn't contain {0} language";
    public static final String CannotReadInputImage =
            "Cannot read input image";
    public static final String CannotReadProvidedImage =
            "Cannot read input image {0}";
    public static final String CannotReadFont = "Cannot read font";
    public static final String TesseractFailed = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String CannotFindPathToTesseractExecutable =
            "Cannot find path to tesseract executable.";
    public static final String CannotFindPathToTessDataDirectory =
            "Cannot find path to tess data directory";
    private List<String> messageParams;

    /**
     * Creates a new OcrException.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public OcrException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new OcrException.
     *
     * @param msg the detail message.
     */
    public OcrException(String msg) {
        super(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return this.messageParams != null && this.messageParams.size() != 0
                ? MessageFormatUtil
                        .format(super.getMessage(), this.getMessageParams())
                : super.getMessage();
    }

    /**
     * Gets additional params for Exception message.
     */
    protected Object[] getMessageParams() {
        Object[] parameters = new Object[this.messageParams.size()];

        for(int i = 0; i < this.messageParams.size(); ++i) {
            parameters[i] = this.messageParams.get(i);
        }

        return parameters;
    }

    /**
     * Sets additional params for Exception message.
     *
     * @param messageParams additional params.
     * @return object itself.
     */
    public OcrException setMessageParams(String... messageParams) {
        this.messageParams = Arrays.<String>asList(messageParams);
        return this;
    }
}
