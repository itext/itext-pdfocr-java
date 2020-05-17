package com.itextpdf.pdfocr;

import org.apache.commons.imaging.ImageFormats;

class PropertiesUtil {
    /**
     * Path to the file with the default font.
     */
    static final String FONT_RESOURCE_PATH = "com/itextpdf/pdfocr/fonts/";

    /**
     * Path to default font file.
     * "LiberationSans-Regular" by default.
     */
    private static final String DEFAULT_FONT_NAME =
            "LiberationSans-Regular.ttf";

    /**
     * Gets path to the default font.
     *
     * @return {@link java.lang.String} path to default font
     */
    static String getDefaultFontName() {
        return FONT_RESOURCE_PATH + DEFAULT_FONT_NAME;
    }

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
