package com.itextpdf.ocr;

public class Exception extends java.lang.RuntimeException {
    public static final String OutputIntentCannotBeNull = "Output intent can not be null for PDF/A-3u document";
    private String message;

    public Exception(String msg) {
        message = msg;
    }

    public String getMessage() {
        return message;
    }
}
