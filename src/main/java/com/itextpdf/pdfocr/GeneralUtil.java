package com.itextpdf.pdfocr;

import org.apache.commons.imaging.ImageFormats;

class GeneralUtil {
    /**
     * Path to the file with the default font.
     */
    static final String FONT_RESOURCE_PATH = "com/itextpdf/ocr/fonts/";

    /**
     * Utility method to get png image format as needed.
     *
     * @return {@link org.apache.commons.imaging.ImageFormats#PNG} as
     * {@link java.lang.String}
     */
    static String getPngImageFormat() {
        return ImageFormats.PNG.getName();
    }
}
