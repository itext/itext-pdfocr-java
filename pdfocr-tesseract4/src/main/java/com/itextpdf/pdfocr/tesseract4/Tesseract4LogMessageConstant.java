package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.LogMessageConstant;

public class Tesseract4LogMessageConstant extends LogMessageConstant {
    public static final String TesseractFailed =
            "Tesseract failed: {0}";
    public static final String CannotReadFile =
            "Cannot read file {0}: {1}";
    public static final String CannotOcrInputFile =
            "Cannot ocr input file: {1}";
    public static final String CannotUseUserWords =
            "Cannot use custom user words: {0}";
    public static final String CannotRetrievePagesFromImage =
            "Cannot get pages from image {0}: {1}";
    public static final String PageNumberIsIncorrect =
            "Provided number of page ({0}) is incorrect for {1}";
    public static final String CannotDeleteFile =
            "File {0} cannot be deleted: {1}";
    public static final String CannotProcessImage = "Cannot process "
            + "image: {0}";

    /*
    INFO messages
     */
    public static final String CreatedTemporaryFile =
            "Created temp file {0}";
    public static final String CannotConvertImageToGrayscale =
            "Cannot convert to gray image with depth {0}";
    public static final String CannotBinarizeImage =
            "Cannot binarize image with depth {0}";
    public static final String CannotConvertImageToPix =
            "Cannot convert image to pix: {0}";
    public static final String CannotCreateBufferedImage =
            "Cannot create a buffered image from the input image: {0}";
    public static final String CannotGetTemporaryDirectory = "Cannot get "
            + "temporary directory: {0}";
}
