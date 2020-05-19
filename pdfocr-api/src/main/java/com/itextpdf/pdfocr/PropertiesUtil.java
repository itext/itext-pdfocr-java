package com.itextpdf.pdfocr;

import org.apache.commons.imaging.ImageFormats;

class PropertiesUtil {

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
