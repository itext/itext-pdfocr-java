package com.itextpdf.pdfocr;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;

/**
 * Enumeration of the possible scale modes for input images.
 */
public enum ScaleMode {
    /**
     * Only width of the image will be proportionally scaled to fit
     * required size that is set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Height will be equal to the page height that was set using
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
     * The image will be scaled to fit within the page width and height dimensions that are set using
     * {@link OcrPdfCreatorProperties#setPageSize(Rectangle)} method.
     * Original aspect ratio of the image stays unchanged.
     */
    SCALE_TO_FIT
}
