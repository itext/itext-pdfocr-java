package com.itextpdf.ocr;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.util.List;

/**
 * {@link IPdfRenderer} interface is used for instantiating
 * new {@link PdfRenderer} objects.
 *
 * {@link IPdfRenderer} provides possibilities to set list of input images
 * to be used for OCR, to set scaling mode for images, to set color of text in
 * output PDF document, to set fixed size of the PDF document and
 * to perform OCR using given images and to return
 * {@link com.itextpdf.kernel.pdf.PdfDocument} as result.
 */
public interface IPdfRenderer {

    /**
     * Gets list of provided input images for OCR.
     *
     * @return {@link java.util.List} of given input images
     */
    List<File> getInputImages();

    /**
     * Sets list of input images for OCR.
     *
     * @param images {@link java.util.List} of input images
     */
    void setInputImages(List<File> images);

    /**
     * Gets text color in output PDF document.
     *
     * @return set text {@link com.itextpdf.kernel.colors.Color}
     */
    com.itextpdf.kernel.colors.Color getTextColor();

    /**
     * Sets text color in output PDF document.
     *
     * @param color required text {@link com.itextpdf.kernel.colors.Color}
     */
    void setTextColor(com.itextpdf.kernel.colors.Color color);

    /**
     * Gets scale mode for input images.
     *
     * @return selected {@link IPdfRenderer.ScaleMode}
     */
    ScaleMode getScaleMode();

    /**
     * Sets scale mode for input images using available options
     * from {@link com.itextpdf.ocr.IPdfRenderer.ScaleMode} enumeration.
     *
     * @param scaleMode selected {@link IPdfRenderer.ScaleMode}
     */
    void setScaleMode(ScaleMode scaleMode);

    /**
     * Gets required size for output PDF document. Real size of the page will
     * be calculated according to the selected {@link IPdfRenderer.ScaleMode}
     *
     * @return required page size as {@link com.itextpdf.kernel.geom.Rectangle}
     */
    com.itextpdf.kernel.geom.Rectangle getPageSize();

    /**
     * Sets required size for output PDF document. Real size of the page will
     * be calculated according to the selected {@link IPdfRenderer.ScaleMode}.
     *
     * @param pageSize required page
     *                size as {@link com.itextpdf.kernel.geom.Rectangle}
     */
    void setPageSize(com.itextpdf.kernel.geom.Rectangle pageSize);

    /**
     * Gets name of image layer.
     *
     * @return image layer's name as {@link java.lang.String} if it was
     * manually set, otherwise - the default name ("Image layer")
     */
    String getImageLayerName();

    /**
     * Sets name for the image layer.
     * (by default its name is "Image layer")
     *
     * @param layerName name of the image layer
     *                       as {@link java.lang.String}
     */
    void setImageLayerName(String layerName);

    /**
     * Gets name of text layer.
     *
     * @return text layer's name as {@link java.lang.String} if it was
     * manually set, otherwise - the default name ("Text layer")
     */
    String getTextLayerName();

    /**
     * Sets name for the text layer.
     * (by default it is "Text layer")
     *
     * @param layerName of the text layer as {@link java.lang.String}
     */
    void setTextLayerName(String layerName);

    /**
     * Gets pdf language.
     *
     * @return pdf document language as {@link java.lang.String}
     */
    String getPdfLang();

    /**
     * Specify pdf natural language, and optionally locale.
     * For the content usage dictionary use
     * {@link com.itextpdf.kernel.pdf.PdfName#Language}
     *
     * @param language pdf document language as {@link java.lang.String},
     *                 e.g. "en-US", etc.
     */
    void setPdfLang(String language);

    /**
     * Gets pdf document title.
     *
     * @return pdf title as {@link java.lang.String}
     */
    String getTitle();

    /**
     * Sets pdf document title.
     *
     * @param title pdf title as {@link java.lang.String}
     */
    void setTitle(String title);

    /**
     * Returns path to font to be used in pdf document.
     * @return path to the required font
     */
    String getFontPath();

    /**
     * Sets path to font to be used in pdf document.
     *
     * @param path path to the required font
     */
    void setFontPath(String path);

    /**
     * Performs OCR with set parameters and create pdf using provided
     * {@link com.itextpdf.kernel.pdf.PdfWriter} and
     * {@link com.itextpdf.kernel.pdf.PdfOutputIntent}.
     * PDF/A-3u document will be created if
     * provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final pdf document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     * @return result PDF/A-3u {@link com.itextpdf.kernel.pdf.PdfDocument}
     * object
     */
    PdfDocument doPdfOcr(PdfWriter pdfWriter,
            PdfOutputIntent pdfOutputIntent);

    /**
     * Performs OCR using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final pdf document to
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     * @throws OcrException if provided font is incorrect
     */
    PdfDocument doPdfOcr(PdfWriter pdfWriter) throws OcrException;

    /**
     * Performs OCR for the given list of input images and saves output to a
     * text file using provided path.
     *
     * @param path path as {@link java.lang.String} to file to be
     *                     created
     */
    void doPdfOcr(String path);

    /**
     * Enumeration of the possible scale modes for input images.
     */
    enum ScaleMode {
        /**
         * Size of every page of the output PDF document will match the size
         * of the corresponding input image.
         * (default value)
         */
        KEEP_ORIGINAL_SIZE,
        /**
         * Only width of the image will be proportionally scaled to fit
         * required size that is set using {@link #setPageSize(Rectangle)}
         * method.
         */
        SCALE_WIDTH,
        /**
         * Only height of the image will be proportionally scaled to fit
         * required size that is set using {@link #setPageSize(Rectangle)}
         * method.
         */
        SCALE_HEIGHT,
        /**
         * Size of every page of the output PDF document will match the
         * values set using {@link #setPageSize(Rectangle)} method.
         */
        SCALE_TO_FIT
    }
}
