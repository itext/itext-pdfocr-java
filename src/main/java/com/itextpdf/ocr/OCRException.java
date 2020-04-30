package com.itextpdf.ocr;

import com.itextpdf.io.util.MessageFormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception class for custom exceptions.
 */
public class OCRException extends RuntimeException {

    public static final String INCORRECT_INPUT_IMAGE_FORMAT =
            "{0} format is not supported.";
    public static final String INCORRECT_LANGUAGE =
            "{0} does not exist in {1}";
    public static final String LANGUAGE_IS_NOT_IN_THE_LIST =
            "Provided list of languages doesn't contain {0} language";
    public static final String CANNOT_READ_INPUT_IMAGE =
            "Cannot read input image";
    public static final String CANNOT_READ_PROVIDED_IMAGE =
            "Cannot read input image {0}";
    public static final String CANNOT_READ_FONT = "Cannot read font";
    public static final String TESSERACT_FAILED = "Tesseract failed. "
            + "Please check provided parameters";
    public static final String CANNOT_FIND_PATH_TO_TESSERACT_EXECUTABLE =
            "Cannot find path to tesseract executable.";
    public static final String CANNOT_FIND_PATH_TO_TESSDATA =
            "Cannot find path to tess data directory";
    private List<String> messageParams;

    /**
     * Creates a new OCRException.
     *
     * @param msg the detail message.
     * @param e   the cause
     *            (which is saved for later retrieval
     *            by {@link #getCause()} method).
     */
    public OCRException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * Creates a new OCRException.
     *
     * @param msg the detail message.
     */
    public OCRException(String msg) {
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
    public RuntimeException setMessageParams(String... messageParams) {
        this.messageParams = new ArrayList<String>();
        for (String obj : messageParams) {
            this.messageParams.add(obj);
        }
        return this;
    }
}
