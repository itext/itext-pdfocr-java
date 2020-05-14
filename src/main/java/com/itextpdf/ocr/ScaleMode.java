package com.itextpdf.ocr;

import com.itextpdf.kernel.geom.Rectangle;

/**
 * Enumeration of the possible scale modes for input images.
 */
public enum ScaleMode {
    /**
     * Only width of the image will be proportionally scaled to fit
     * required size that is set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Height will be equal to the page width that was set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method and
     * width will be proportionally scaled to keep the original aspect ratio.
     */
    SCALE_WIDTH,
    /**
     * Only height of the image will be proportionally scaled to fit
     * required size that is set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Width will be equal to the page width that was set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method and
     * height will be proportionally scaled to keep the original aspect ratio.
     */
    SCALE_HEIGHT,
    /**
     * Size of every page of the output PDF document will match the
     * values set using {@link OcrPdfCreatorProperties#setPageSize(Rectangle)}
     * method.
     */
    SCALE_TO_FIT
}
