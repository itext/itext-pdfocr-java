package com.itextpdf.ocr;

public class LogMessageConstant {
    public static final String CANNOT_READ_INPUT_IMAGE =
            "Cannot read input image {0}";
    public static final String TESSERACT_FAILED =
            "Tesseract failed: {0}";
    public static final String CANNOT_READ_PROVIDED_FONT =
            "Cannot read given font or it was not provided: {0}";
    public static final String CANNOT_READ_DEFAULT_FONT =
            "Cannot default read font: {0}";
    public static final String CANNOT_WRITE_TO_FILE =
            "Cannot write to file {0}: {1}";
    public static final String CANNOT_READ_FILE =
            "Cannot read file {0}: {1}";
    public static final String CANNOT_OCR_INPUT_FILE =
            "Cannot ocr input file: {1}";
    public static final String CANNOT_ADD_DATA_TO_PDF_DOCUMENT =
            "Cannot add data to pdf document: {1}";
    public static final String CANNOT_USE_USER_WORDS =
            "Cannot use custom user words: {0}";
    public static final String CANNOT_RETRIEVE_PAGES_FROM_IMAGE =
            "Cannot get pages from image {0}: {1}";
    public static final String PAGE_NUMBER_IS_INCORRECT =
            "Provided number of page ({0}) is incorrect for {1}";
    public static final String CANNOT_DELETE_FILE =
            "File {0} cannot be deleted: {1}";

    /*
    INFO messages
     */
    public static final String START_OCR_FOR_IMAGES =
            "Starting ocr for {0} image(s)";
    public static final String NUMBER_OF_PAGES_IN_IMAGE =
            "Image {0} contains {1} page(s)";
    public static final String ATTEMPT_TO_CONVERT_TO_PNG =
            "Trying to convert {0} to png: {1}";
    public static final String READING_IMAGE_AS_PIX =
            "Trying to read image {0} as pix: {1}";
    public static final String CREATED_TEMPORARY_FILE =
            "Created temp file {0}";
    public static final String CANNOT_CONVERT_IMAGE_TO_GRAYSCALE =
            "Cannot convert to gray image with depth {0}";
    public static final String CANNOT_BINARIZE_IMAGE =
            "Cannot binarize image with depth {0}";
}
