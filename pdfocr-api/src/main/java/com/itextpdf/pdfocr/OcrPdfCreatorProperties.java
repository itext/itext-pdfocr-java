package com.itextpdf.pdfocr;

/**
 * Properties that will be used by the {@link OcrPdfCreator}.
 */
public class OcrPdfCreatorProperties {

    /**
     * Path to the default font.
     */
    private static final String DEFAULT_FONT_PATH = "com/itextpdf/pdfocr/fonts/LiberationSans-Regular.ttf";

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
     * <code>null</code> by default.
     * If this parameter is null, size of the page will be equal to the
     * input image size. If this parameter is not null, input image will be
     * scaled according to the selected {@link ScaleMode}.
     */
    private com.itextpdf.kernel.geom.Rectangle pageSize = null;

    /**
     * Name of the image layer.
     * <code>null</code> by default.
     * If this parameter is null then image is placed directly in canvas instead of layer.
     * If value of imageLayerName is equal to value of textLayerName then image and text placed in one layer.
     */
    private String imageLayerName = null;

    /**
     * Name of the text layer.
     * <code>null</code> by default.
     * If this parameter is null then text is placed directly in canvas instead of layer.
     * If value of textLayerName is equal to value of imageLayerName then text and image placed in one layer.
     */
    private String textLayerName = null;

    /**
     * PDF Language.
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
     * {@link ScaleMode} enumeration.
     *
     * @return selected {@link ScaleMode}
     */
    public final ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * Sets scale mode for input images using available options
     * from {@link ScaleMode} enumeration.
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
     * manually set, otherwise - <code>null</code>
     */
    public final String getImageLayerName() {
        return imageLayerName;
    }

    /**
     * Sets name for the image layer.
     * <code>null</code> by default.
     * If null then image is placed directly in canvas instead of layer.
     * If image layer name is equal to text layer name then text and image placed in one layer.
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
     * manually set, otherwise - <code>null</code>
     */
    public final String getTextLayerName() {
        return textLayerName;
    }

    /**
     * Sets name for the text layer.
     * <code>null</code> by default.
     * If null then text is placed directly in canvas instead of layer.
     * If text layer name is equal to image layer name then text and image placed in one layer.
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
     * Gets PDF language.
     *
     * @return PDF document language as {@link java.lang.String}
     */
    public final String getPdfLang() {
        return pdfLang;
    }

    /**
     * Specify PDF natural language, and optionally locale.
     * For the content usage dictionary use
     * {@link com.itextpdf.kernel.pdf.PdfName#Language}
     *
     * @param language PDF document language as {@link java.lang.String},
     *                 e.g. "en-US", etc.
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setPdfLang(
            final String language) {
        pdfLang = language;
        return this;
    }

    /**
     * Gets PDF document title.
     *
     * @return PDF title as {@link java.lang.String}
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets PDF document title.
     *
     * @param title PDF title as {@link java.lang.String}
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public final OcrPdfCreatorProperties setTitle(
            final String title) {
        this.title = title;
        return this;
    }

    /**
     * Returns path to the font to be used in PDF document.
     * @return path to the required font
     */
    public String getFontPath() {
        return fontPath;
    }

    /**
     * Sets path to the font to be used in PDF document.
     *
     * @param path path to the required font
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public OcrPdfCreatorProperties setFontPath(final String path) {
        fontPath = path;
        return this;
    }

    /**
     * Gets path to the default font.
     *
     * @return {@link java.lang.String} path to default font
     */
    public String getDefaultFontName() {
        return com.itextpdf.pdfocr.OcrPdfCreatorProperties.DEFAULT_FONT_PATH;
    }
}
