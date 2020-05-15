package com.itextpdf.pdfocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.pdfocr.tesseract4.ScaleMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PdfUtil {

    /**
     * The Constant to convert pixels to points.
     */
    static final float PX_TO_PT = 3f / 4f;

    /**
     * The Constant for points per inch.
     */
    private static final float POINTS_PER_INCH = 72.0f;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfUtil.class);

    /**
     * Calculates the size of the PDF document page according to the provided
     * {@link ScaleMode}.
     *
     * @param imageData input image or its one page as
     *                  {@link com.itextpdf.io.image.ImageData}
     * @param scaleMode required {@link ScaleMode} that could be
     *                  set using {@link OcrPdfCreatorProperties#setScaleMode}
     *                  method
     * @param requiredSize size of the page that could be using
     *                     {@link OcrPdfCreatorProperties#setPageSize} method
     * @return {@link com.itextpdf.kernel.geom.Rectangle}
     */
    static com.itextpdf.kernel.geom.Rectangle calculateImageSize(
            final ImageData imageData,
            final ScaleMode scaleMode,
            final com.itextpdf.kernel.geom.Rectangle requiredSize) {
        // Adjust image size and dpi
        // The resolution of a PDF file is 72pt per inch
        float dotsPerPointX = 1.0f;
        float dotsPerPointY = 1.0f;
        if (imageData != null && imageData.getDpiX() > 0
                && imageData.getDpiY() > 0) {
            dotsPerPointX = imageData.getDpiX() / POINTS_PER_INCH;
            dotsPerPointY = imageData.getDpiY() / POINTS_PER_INCH;
        }

        if (imageData != null) {
            float imgWidthPt = getPoints(imageData.getWidth());
            float imgHeightPt = getPoints(imageData.getHeight());
            // page size will be equal to the image size if page size or
            // scale mode are not set
            if (requiredSize == null || scaleMode == null) {
                return new com.itextpdf.kernel.geom.Rectangle(imgWidthPt,
                        imgHeightPt);
            } else {
                com.itextpdf.kernel.geom.Rectangle size =
                        new com.itextpdf.kernel.geom.Rectangle(
                                requiredSize.getWidth(),
                                requiredSize.getHeight());
                // scale image according to the page size and scale mode
                if (scaleMode == ScaleMode.SCALE_HEIGHT) {
                    float newHeight = imgHeightPt
                            * requiredSize.getWidth() / imgWidthPt;
                    size.setHeight(newHeight);
                } else if (scaleMode == ScaleMode.SCALE_WIDTH) {
                    float newWidth = imgWidthPt
                            * requiredSize.getHeight() / imgHeightPt;
                    size.setWidth(newWidth);
                } else if (scaleMode == ScaleMode.SCALE_TO_FIT) {
                    float ratio = Math.min(
                            requiredSize.getWidth() / imgWidthPt,
                            requiredSize.getHeight() / imgHeightPt);
                    size.setWidth(imgWidthPt * ratio);
                    size.setHeight(imgHeightPt * ratio);
                }
                return size;
            }
        } else {
            return requiredSize;
        }
    }

    /**
     * Converts value from pixels to points.
     *
     * @param pixels input value in pixels
     * @return result value in points
     */
    static float getPoints(final float pixels) {
        return pixels * PX_TO_PT;
    }
}
