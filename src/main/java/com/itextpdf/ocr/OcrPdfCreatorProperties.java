package com.itextpdf.ocr;

/**
 * Properties that will be used by the {@link com.itextpdf.ocr.PdfRenderer}.
 */
public class OcrPdfCreatorProperties {

    /**
     * Color of the text in the output PDF document.
     * Text will be transparent by default.
     */
    private com.itextpdf.kernel.colors.Color textColor = null;

    /**
     * Scale mode for input images.
     * {@link ScaleMode#SCALE_TO_FIT} by default. But this value will be used
     * only if {@link #pageSize} is not null;
     */
    private ScaleMode scaleMode = ScaleMode.SCALE_TO_FIT;

    /**
     * Size of the PDF document pages.
     * NUll by default.
     * If this parameter is null, size of the page will be equal to the
     * input image size. If this parameter is not null, input image will be
     * scaled according to the selected {@link ScaleMode}.
     */
    private com.itextpdf.kernel.geom.Rectangle pageSize = null;

    /**
     * Name of the image layer.
     * "Image Layer" by default.
     */
    private String imageLayerName = "Image Layer";

    /**
     * Name of the text layer.
     * "Text Layer" by default.
     */
    private String textLayerName = "Text Layer";

    /**
     * Pdf Language.
     * "en-US" by default.
     */
    private String pdfLang = "en-US";

    /**
     * Title of the created document.
     * It is empty by default.
     */
    private String title = "";

    /**
     * Path to the used font.
     * It should be set explicitly or the default font will be used.
     */
    private String fontPath;

    /**
     * Creates a new {@link OcrPdfCreatorProperties} instance.
     */
    public OcrPdfCreatorProperties() {
    }

    /**
     * Creates a new {@link OcrPdfCreatorProperties} instance
     * based on another {@link OcrPdfCreatorProperties} instance (copy
     * constructor).
     *
     * @param other the other {@link OcrPdfCreatorProperties} instance
     */
    public OcrPdfCreatorProperties(OcrPdfCreatorProperties other) {
        this.scaleMode = other.scaleMode;
        this.pageSize = other.pageSize;
        this.imageLayerName = other.imageLayerName;
        this.textLayerName = other.textLayerName;
        this.textColor = other.textColor;
        this.pdfLang = other.pdfLang;
        this.title = other.title;
        this.fontPath = other.fontPath;
    }

    /**
     * Gets text color in output PDF document.
     *
     * @return set text {@link com.itextpdf.kernel.colors.Color}
     */
    public final com.itextpdf.kernel.colors.Color getTextColor() {
        return textColor;
    }

    /**
     * Sets text color in output PDF document.
     *
     * @param textColor required text {@link com.itextpdf.kernel.colors.Color}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setTextColor(
            final com.itextpdf.kernel.colors.Color textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * Gets scale mode for input images using available options from
     * {@link com.itextpdf.ocr.ScaleMode} enumeration.
     *
     * @return selected {@link ScaleMode}
     */
    public final ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * Sets scale mode for input images using available options
     * from {@link com.itextpdf.ocr.ScaleMode} enumeration.
     *
     * @param scaleMode selected {@link ScaleMode}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setScaleMode(
            final ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        return this;
    }

    /**
     * Gets required size for output PDF document. Real size of the page will
     * be calculated according to the selected {@link ScaleMode}
     *
     * @return required page size as {@link com.itextpdf.kernel.geom.Rectangle}
     */
    public final com.itextpdf.kernel.geom.Rectangle getPageSize() {
        return pageSize;
    }

    /**
     * Sets required size for output PDF document. Real size of the page will
     * be calculated according to the selected {@link ScaleMode}.
     *
     * @param pageSize required page
     *                size as {@link com.itextpdf.kernel.geom.Rectangle}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setPageSize(
            final com.itextpdf.kernel.geom.Rectangle pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * Gets name of image layer.
     *
     * @return image layer's name as {@link java.lang.String} if it was
     * manually set, otherwise - the default name ("Image layer")
     */
    public final String getImageLayerName() {
        return imageLayerName;
    }

    /**
     * Sets name for the image layer.
     * "Image layer" by default.
     *
     * @param layerName name of the image layer
     *                       as {@link java.lang.String}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setImageLayerName(
            final String layerName) {
        imageLayerName = layerName;
        return this;
    }

    /**
     * Gets name of text layer.
     *
     * @return text layer's name as {@link java.lang.String} if it was
     * manually set, otherwise - the default name ("Text layer")
     */
    public final String getTextLayerName() {
        return textLayerName;
    }

    /**
     * Sets name for the text layer.
     * "Text layer" by default.
     *
     * @param layerName of the text layer as {@link java.lang.String}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setTextLayerName(
            final String layerName) {
        textLayerName = layerName;
        return this;
    }

    /**
     * Gets pdf language.
     *
     * @return pdf document language as {@link java.lang.String}
     */
    public final String getPdfLang() {
        return pdfLang;
    }

    /**
     * Specify pdf natural language, and optionally locale.
     * For the content usage dictionary use
     * {@link com.itextpdf.kernel.pdf.PdfName#Language}
     *
     * @param language pdf document language as {@link java.lang.String},
     *                 e.g. "en-US", etc.
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setPdfLang(
            final String language) {
        pdfLang = language;
        return this;
    }

    /**
     * Gets pdf document title.
     *
     * @return pdf title as {@link java.lang.String}
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets pdf document title.
     *
     * @param title pdf title as {@link java.lang.String}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setTitle(
            final String title) {
        this.title = title;
        return this;
    }

    /**
     * Returns path to the font to be used in pdf document.
     * @return path to the required font
     */
    public String getFontPath() {
        return fontPath;
    }

    /**
     * Sets path to the font to be used in pdf document.
     *
     * @param path path to the required font
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public OcrPdfCreatorProperties setFontPath(final String path) {
        fontPath = path;
        return this;
    }
}
