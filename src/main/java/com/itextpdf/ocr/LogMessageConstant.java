package com.itextpdf.ocr;

public class LogMessageConstant {
    public static final String CannotReadInputImage =
            "Cannot read input image {0}";
    public static final String TesseractFailed =
            "Tesseract failed: {0}";
    public static final String CannotReadProvidedFont =
            "Cannot read given font or it was not provided: {0}";
    public static final String CannotReadDefaultFont =
            "Cannot default read font: {0}";
    public static final String CannotWriteToFile =
            "Cannot write to file {0}: {1}";
    public static final String CannotReadFile =
            "Cannot read file {0}: {1}";
    public static final String CannotOcrInputFile =
            "Cannot ocr input file: {1}";
    public static final String CannotAddDataToPdfDocument =
            "Cannot add data to pdf document: {1}";
    public static final String CannotUseUserWords =
            "Cannot use custom user words: {0}";
    public static final String CannotRetrievePagesFromImage =
            "Cannot get pages from image {0}: {1}";
    public static final String PageNumberIsIncorrect =
            "Provided number of page ({0}) is incorrect for {1}";
    public static final String CannotDeleteFile =
            "File {0} cannot be deleted: {1}";

    /*
    INFO messages
     */
    public static final String StartOcrForImages =
            "Starting ocr for {0} image(s)";
    public static final String NumberOfPagesInImage =
            "Image {0} contains {1} page(s)";
    public static final String AttemptToConvertToPng =
            "Trying to convert {0} to png: {1}";
    public static final String ReadingImageAsPix =
            "Trying to read image {0} as pix: {1}";
    public static final String CreatedTemporaryFile =
            "Created temp file {0}";
    public static final String CannotConvertImageToGrayscale =
            "Cannot convert to gray image with depth {0}";
    public static final String CannotBinarizeImage =
            "Cannot binarize image with depth {0}";
}
