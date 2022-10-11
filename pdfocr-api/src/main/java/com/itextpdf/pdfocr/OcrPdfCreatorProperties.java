/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr;

import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.layout.font.FontProvider;

/**
 * Properties that will be used by the {@link OcrPdfCreator}.
 */
public class OcrPdfCreatorProperties {

    /**
     * Font provider.
     * By default it is {@link PdfOcrFontProvider} object with default font
     * family {@link PdfOcrFontProvider#getDefaultFontFamily()}.
     */
    private FontProvider fontProvider = null;

    /**
     * Default font family.
     * {@link PdfOcrFontProvider#getDefaultFontFamily()} by default.
     */
    private String defaultFontFamily = null;

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
     */
    private String pdfLang = "";

    /**
     * Title of the created document.
     * It is not set by default.
     */
    private String title = null;

    /**
     * Handles rotated images as described in {@link com.itextpdf.pdfocr.IImageRotationHandler}.
     */
    private IImageRotationHandler imageRotationHandler;

    private IMetaInfo metaInfo;

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
        this.fontProvider = other.fontProvider;
        this.defaultFontFamily = other.defaultFontFamily;
        this.imageRotationHandler = other.imageRotationHandler;
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
     * Text will be transparent by default.
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
     * Sets required size for output PDF document.
     *
     * @param pageSize requested page
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
     * Language identifier shall either be the empty text string, to indicate that the language is unknown,
     * or a Language-Tag as defined in BCP 47 (2009), Tags for the Identification of Languages.
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
     * Returns FontProvider that was set previously or if it is
     * <code>null</code> a new instance of {@link PdfOcrFontProvider} is
     * returned.
     * @return {@link com.itextpdf.layout.font.FontProvider} object
     */
    public FontProvider getFontProvider() {
        if (fontProvider == null) {
            fontProvider = new PdfOcrFontProvider();
        }
        return fontProvider;
    }

    /**
     * Sets font provider.
     * Please note that passed FontProvider is not to be used in multithreaded
     * environments or for any parallel processing.
     * There will be set the following default font family:
     * {@link PdfOcrFontProvider#getDefaultFontFamily()}
     * @param fontProvider selected
     * {@link com.itextpdf.layout.font.FontProvider} instance
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public OcrPdfCreatorProperties setFontProvider(FontProvider fontProvider) {
        this.fontProvider = fontProvider;
        return this;
    }

    /**
     * Sets font provider and default font family.
     * Please note that passed FontProvider is not to be used in multithreaded
     * environments or for any parallel processing.
     * @param fontProvider selected
     * {@link com.itextpdf.layout.font.FontProvider} instance
     * @param defaultFontFamily preferred font family to be used when selecting
     *                          font from
     *                          {@link com.itextpdf.layout.font.FontProvider}.
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public OcrPdfCreatorProperties setFontProvider(FontProvider fontProvider,
            String defaultFontFamily) {
        this.fontProvider = fontProvider;
        this.defaultFontFamily = defaultFontFamily;
        return this;
    }

    /**
     * Gets preferred font family to be used when selecting font from
     * {@link com.itextpdf.layout.font.FontProvider}.
     *
     * @return if default font family is not set or it is null or empty
     * {@link PdfOcrFontProvider#getDefaultFontFamily()} is returned
     */
    public String getDefaultFontFamily() {
        return defaultFontFamily == null || defaultFontFamily.length() == 0
                ? getFontProvider().getDefaultFontFamily() : defaultFontFamily;
    }

    /**
     * Gets image rotation handler instance.
     *
     * @return image rotation handler
     */
    public IImageRotationHandler getImageRotationHandler() {
        return this.imageRotationHandler;
    }

    /**
     * Sets image rotation handler instance.
     * If not set - image rotation handling is not applied.
     *
     * @param imageRotationDetector image rotation handler instance
     * @return the {@link OcrPdfCreatorProperties} instance
     */
    public OcrPdfCreatorProperties setImageRotationHandler(
            IImageRotationHandler imageRotationDetector) {
        this.imageRotationHandler = imageRotationDetector;
        return this;
    }

    /**
     * Set meta info for this {@link OcrPdfCreatorProperties}.
     *
     * @param metaInfo meta info
     *
     * @return the instance of the current {@link OcrPdfCreatorProperties}
     */
    public OcrPdfCreatorProperties setMetaInfo(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
        return this;
    }

    /**
     * Returns meta info
     *
     * @return meta info
     */
    IMetaInfo getMetaInfo() {
        return metaInfo;
    }
}
