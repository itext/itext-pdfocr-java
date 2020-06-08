package com.itextpdf.pdfocr;

import com.itextpdf.io.util.MessageFormatUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Exception class for custom exceptions.
 */
public class OcrException extends RuntimeException {

    public static final String CANNOT_READ_INPUT_IMAGE =
            "Cannot read input image";
    public static final String CANNOT_RESOLVE_PROVIDED_FONTS = "Cannot resolve "
            + "any of provided fonts. Please check provided FontProvider.";
    public static final String CANNOT_CREATE_PDF_DOCUMENT = "Cannot create "
            + "PDF document: {0}";
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
