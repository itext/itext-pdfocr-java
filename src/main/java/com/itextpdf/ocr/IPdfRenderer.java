package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.util.List;


/**
 * Interface for PDF Renderer classes.
 *
 * The IPdfRenderer provides possibilities to set list of input images
 * to be used for OCR, to set scaling mode for images, color of text in output
 * PDF document,  set fixed size of the PDF document
 * and to perform OCR using given images and return PDFDocument as result
 *
 */
public interface IPdfRenderer {

    /**
     *  Enum describing possible scale modes for images.
     *
     *
     * <li>{@link #keepOriginalSize}</li>
     * <li>{@link #scaleWidth}</li>
     * <li>{@link #scaleHeight}</li>
     * <li>{@link #scaleToFit}</li>
     */
    enum ScaleMode {
        /**
         * keepOriginalSize (default value).
         *
         * the size of every page of
         * the output PDF document will match the size of the
         * corresponding input image
         */
        keepOriginalSize,
        /**
         * scaleWidth.
         *
         * Only width of the image will be proportionally scaled
         */
        scaleWidth,
        /**
         * scaleHeight.
         *
         * Only height of the image will be proportionally scaled
         */
        scaleHeight,
        /**
         * scaleToFit.
         *
         * the size of every page of the output PDF document
         * will match the values set using "setPdfSize()" method
         */
        scaleToFit
    }

    /**
     * Supported image formats.
     *
     */
    enum ImageFormat {
        /**
         * BMP file format.
         */
        bmp,
        /**
         * PNG file format.
         */
        png,
        /**
         * PNM file format.
         */
        pnm,
        /**
         * TIFF file format.
         */
        tiff,
        /**
         * TIF file format.
         */
        tif,
        /**
         * JPEG file format.
         */
        jpeg,
        /**
         * JPG file format.
         */
        jpg,
        /**
         * JPE file format.
         */
        jpe,
        /**
         * JFIF file format.
         */
        jfif
    }

    /**
     * Set list of input images for OCR.
     *
     * @param images a {@link java.util.List} object.
     */
    void setInputImages(List<File> images);

    /**
     * Get list of provided input images for OCR.
     *
     * @return a {@link java.util.List} object.
     */
    List<File> getInputImages();

    /**
     * Set text color (should be CMYK) in output PDF document.
     *
     * @param newColor a {@link com.itextpdf.kernel.colors.Color} object.
     */
    void setColor(Color newColor);

    /**
     * Get text color in output PDF document.
     *
     * @return a {@link com.itextpdf.kernel.colors.Color} object.
     */
    Color getColor();

    /**
     * Set scale mode for input images using available options
     * from ScaleMode enum.
     *
     * @param mode a {@link com.itextpdf.ocr.IPdfRenderer.ScaleMode} object.
     */
    void setScaleMode(ScaleMode mode);

    /**
     * Get scale mode for input images.
     *
     * @return a {@link com.itextpdf.ocr.IPdfRenderer.ScaleMode} object.
     */
    ScaleMode getScaleMode();

    /**
     * Set fixed size for output PDF document.
     * (this parameter is used only is ScaleMode is set as "fitToSize")
     *
     * @param pdfSize a {@link com.itextpdf.kernel.geom.Rectangle} object.
     */
    void setPageSize(Rectangle pdfSize);

    /**
     * Get size for output document.
     *
     * @return a {@link com.itextpdf.kernel.geom.Rectangle} object.
     */
    Rectangle getPageSize();

    /**
     * Perform OCR using provided parameters.
     *
     * @return a {@link com.itextpdf.kernel.pdf.PdfDocument} object.
     */
    PdfDocument doPdfOcr();

    /**
     * Perform OCR using provided pdfWriter.
     *
     * @return a {@link com.itextpdf.kernel.pdf.PdfDocument} object.
     */
    PdfDocument doPdfOcr(PdfWriter pdfWriter);
}

