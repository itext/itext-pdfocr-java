package com.itextpdf.ocr;

/**
 * Exception class for custom exceptions.
 *
 */
public class Exception extends java.lang.RuntimeException {

    public static final String OUTPUT_INTENT_CANNOT_BE_NULL = "Output intent "
            + "can not be null for PDF/A-3u document";

    private final String message;

    public Exception(String msg) {
        message = msg;
    }

    public final String getMessage() {
        return message;
    }
}
