/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

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

import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.font.otf.ActualTextIterator;
import com.itextpdf.io.font.otf.Glyph;
import com.itextpdf.io.font.otf.GlyphLine;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.actions.events.LinkDocumentIdEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfTrueTypeFont;
import com.itextpdf.kernel.font.PdfType0Font;
import com.itextpdf.kernel.font.PdfType1Font;
import com.itextpdf.kernel.font.PdfType3Font;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.DocumentProperties;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.CanvasArtifact;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.TextRenderingMode;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.kernel.pdf.tagutils.AccessibilityProperties;
import com.itextpdf.kernel.pdf.tagutils.DefaultAccessibilityProperties;
import com.itextpdf.kernel.pdf.tagutils.TagTreePointer;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.pdfa.PdfADocument;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.pdfocr.structuretree.ArtifactItem;
import com.itextpdf.pdfocr.structuretree.LogicalStructureTreeItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OcrPdfCreator} is the class that creates PDF documents containing input
 * images and text that was recognized using provided {@link IOcrEngine}.
 *
 * {@link OcrPdfCreator} provides possibilities to set list of input images to
 * be used for OCR, to set scaling mode for images, to set color of text in
 * output PDF document, to set fixed size of the PDF document's page and to
 * perform OCR using given images and to return
 * {@link com.itextpdf.kernel.pdf.PdfDocument} as result.
 * OCR is based on the provided {@link IOcrEngine}
 * (e.g. tesseract reader). This parameter is obligatory and it should be
 * provided in constructor
 * or using setter.
 */
public class OcrPdfCreator {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(OcrPdfCreator.class);

    /**
     * Selected {@link IOcrEngine}.
     */
    private IOcrEngine ocrEngine;

    /**
     * Set of properties.
     */
    private OcrPdfCreatorProperties ocrPdfCreatorProperties;

    /**
     * Creates a new {@link OcrPdfCreator} instance.
     *
     * @param ocrEngine {@link IOcrEngine} selected OCR Reader
     */
    public OcrPdfCreator(final IOcrEngine ocrEngine) {
        this(ocrEngine, new OcrPdfCreatorProperties());
    }

    /**
     * Creates a new {@link OcrPdfCreator} instance.
     *
     * @param ocrEngine selected OCR Reader {@link IOcrEngine}
     * @param ocrPdfCreatorProperties set of properties for {@link OcrPdfCreator}
     */
    public OcrPdfCreator(final IOcrEngine ocrEngine,
            final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        if (ocrPdfCreatorProperties.isTagged() && !ocrEngine.isTaggingSupported()) {
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.TAGGING_IS_NOT_SUPPORTED);
        }
        setOcrEngine(ocrEngine);
        setOcrPdfCreatorProperties(ocrPdfCreatorProperties);
    }

    /**
     * Gets properties for {@link OcrPdfCreator}.
     *
     * @return set properties {@link OcrPdfCreatorProperties}
     */
    public final OcrPdfCreatorProperties getOcrPdfCreatorProperties() {
        return ocrPdfCreatorProperties;
    }

    /**
     * Sets properties for {@link OcrPdfCreator}.
     *
     * @param ocrPdfCreatorProperties set of properties
     * {@link OcrPdfCreatorProperties} for {@link OcrPdfCreator}
     */
    public final void setOcrPdfCreatorProperties(
            final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        this.ocrPdfCreatorProperties = ocrPdfCreatorProperties;
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}, {@link DocumentProperties }
     * and {@link com.itextpdf.kernel.pdf.PdfOutputIntent}. PDF/A-3u document will be created if
     * provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * <p>
     * NOTE that after executing this method you will have a product event from
     * the both itextcore and pdfOcr. Therefore, use this method only if you need to work
     * with the generated {@link PdfDocument}. If you don't need this, use the
     * {@link OcrPdfCreator#createPdfAFile} method. In this case, only the pdfOcr event will be dispatched.
     *
     * @param inputImages        {@link java.util.List} of images to be OCRed
     * @param pdfWriter          the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                           to write final PDF document to
     * @param documentProperties document properties
     * @param pdfOutputIntent    {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                           for PDF/A-3u document
     * @param ocrProcessProperties extra OCR process properties passed to {@link OcrProcessContext}
     *
     * @return result PDF/A-3u {@link com.itextpdf.kernel.pdf.PdfDocument}
     * object
     *
     * @throws PdfOcrException if it was not possible to read provided or
     *                      default font
     */
    public final PdfDocument createPdfA(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final DocumentProperties documentProperties,
            final PdfOutputIntent pdfOutputIntent,
            final IOcrProcessProperties ocrProcessProperties)
            throws PdfOcrException {
        LOGGER.info(MessageFormatUtil.format(
                PdfOcrLogMessageConstant.START_OCR_FOR_IMAGES,
                inputImages.size()));

        // create event helper
        SequenceId pdfSequenceId = new SequenceId();
        OcrPdfCreatorEventHelper ocrEventHelper =
                new OcrPdfCreatorEventHelper(pdfSequenceId, ocrPdfCreatorProperties.getMetaInfo());
        OcrProcessContext ocrProcessContext = new OcrProcessContext(ocrEventHelper);
        ocrProcessContext.setOcrProcessProperties(ocrProcessProperties);

        // map contains:
        // keys: image files
        // values:
        // map pageNumber -> retrieved text data(text and its coordinates)
        Map<File, Map<Integer, List<TextInfo>>> imagesTextData =
                new LinkedHashMap<File, Map<Integer, List<TextInfo>>>();

        for (File inputImage : inputImages) {
            imagesTextData.put(inputImage,
                    ocrEngine.doImageOcr(inputImage, ocrProcessContext));
        }

        // create PdfDocument
        return createPdfDocument(pdfWriter, pdfOutputIntent, imagesTextData, pdfSequenceId, documentProperties);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter} and
     * {@link com.itextpdf.kernel.pdf.PdfOutputIntent}. PDF/A-3u document will be created if
     * provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * <p>
     * NOTE that after executing this method you will have a product event from
     * the both itextcore and pdfOcr. Therefore, use this method only if you need to work
     * with the generated {@link PdfDocument}. If you don't need this, use the
     * {@link OcrPdfCreator#createPdfAFile} method. In this case, only the pdfOcr event will be dispatched.
     *
     * @param inputImages     {@link java.util.List} of images to be OCRed
     * @param pdfWriter       the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                        to write final PDF document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     *
     * @return result PDF/A-3u {@link com.itextpdf.kernel.pdf.PdfDocument}
     * object
     *
     * @throws PdfOcrException if it was not possible to read provided or
     *                      default font
     */
    public final PdfDocument createPdfA(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent)
            throws PdfOcrException {
        return createPdfA(inputImages, pdfWriter, new DocumentProperties(), pdfOutputIntent);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}, {@link DocumentProperties }
     * and {@link com.itextpdf.kernel.pdf.PdfOutputIntent}. PDF/A-3u document will be created if
     * provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * <p>
     * NOTE that after executing this method you will have a product event from
     * the both itextcore and pdfOcr. Therefore, use this method only if you need to work
     * with the generated {@link PdfDocument}. If you don't need this, use the
     * {@link OcrPdfCreator#createPdfAFile} method. In this case, only the pdfOcr event will be dispatched.
     *
     * @param inputImages        {@link java.util.List} of images to be OCRed
     * @param pdfWriter          the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                           to write final PDF document to
     * @param documentProperties document properties
     * @param pdfOutputIntent    {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                           for PDF/A-3u document
     *
     * @return result PDF/A-3u {@link com.itextpdf.kernel.pdf.PdfDocument}
     * object
     *
     * @throws PdfOcrException if it was not possible to read provided or
     *                      default font
     */
    public final PdfDocument createPdfA(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final DocumentProperties documentProperties,
            final PdfOutputIntent pdfOutputIntent)
            throws PdfOcrException {
        return createPdfA(inputImages, pdfWriter, documentProperties, pdfOutputIntent, null);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * <p>
     * NOTE that after executing this method you will have a product event from
     * the both itextcore and pdfOcr. Therefore, use this method only if you need to work
     * with the generated {@link PdfDocument}. If you don't need this, use the
     * {@link OcrPdfCreator#createPdfFile} method. In this case, only the pdfOcr event will be dispatched.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter   the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                    to write final PDF document to
     * @param documentProperties document properties
     * @param ocrProcessProperties extra OCR process properties passed to OcrProcessContext
     *
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     *
     * @throws PdfOcrException if provided font is incorrect
     */
    public final PdfDocument createPdf(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final DocumentProperties documentProperties,
            final IOcrProcessProperties ocrProcessProperties)
            throws PdfOcrException {
        return createPdfA(inputImages, pdfWriter, documentProperties, null, ocrProcessProperties);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * <p>
     * NOTE that after executing this method you will have a product event from
     * the both itextcore and pdfOcr. Therefore, use this method only if you need to work
     * with the generated {@link PdfDocument}. If you don't need this, use the
     * {@link OcrPdfCreator#createPdfFile} method. In this case, only the pdfOcr event will be dispatched.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter   the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                    to write final PDF document to
     * @param documentProperties document properties
     *
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     *
     * @throws PdfOcrException if provided font is incorrect
     */
    public final PdfDocument createPdf(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final DocumentProperties documentProperties)
            throws PdfOcrException {
        return createPdfA(inputImages, pdfWriter, documentProperties, null, null);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * <p>
     * NOTE that after executing this method you will have a product event from
     * the both itextcore and pdfOcr. Therefore, use this method only if you need to work
     * with the generated {@link PdfDocument}. If you don't need this, use the
     * {@link OcrPdfCreator#createPdfFile} method. In this case, only the pdfOcr event will be dispatched.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter   the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                    to write final PDF document to
     *
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     *
     * @throws PdfOcrException if provided font is incorrect
     */
    public final PdfDocument createPdf(final List<File> inputImages,
            final PdfWriter pdfWriter)
            throws PdfOcrException {
        return createPdfA(inputImages, pdfWriter, new DocumentProperties(), null, null);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link java.io.File}.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param outPdfFile  the {@link java.io.File} object to write final PDF document to
     *
     * @throws IOException  signals that an I/O exception of some sort has occurred.
     * @throws PdfOcrException if it was not possible to read provided or
     *                      default font
     */
    public void createPdfFile(final List<File> inputImages,
            final File outPdfFile)
            throws PdfOcrException, IOException {
        createPdfAFile(inputImages, outPdfFile, null);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link java.io.File} and {@link com.itextpdf.kernel.pdf.PdfOutputIntent}.
     * PDF/A-3u document will be created if provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * @param inputImages     {@link java.util.List} of images to be OCRed
     * @param outPdfFile      the {@link java.io.File} object to write final PDF document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     *
     * @throws IOException  signals that an I/O exception of some sort has occurred
     * @throws PdfOcrException if it was not possible to read provided or
     *                      default font
     */
    public void createPdfAFile(final List<File> inputImages,
            final File outPdfFile,
            final PdfOutputIntent pdfOutputIntent)
            throws PdfOcrException, IOException {
        DocumentProperties documentProperties = new DocumentProperties();
        if (ocrPdfCreatorProperties.getMetaInfo() != null) {
            documentProperties.setEventCountingMetaInfo(ocrPdfCreatorProperties.getMetaInfo());
        } else if (ocrEngine instanceof IProductAware) {
            documentProperties.setEventCountingMetaInfo(
                    ((IProductAware) ocrEngine).getMetaInfoContainer().getMetaInfo());
        }
        try (PdfWriter pdfWriter = new PdfWriter(outPdfFile.getAbsolutePath())) {
            PdfDocument pdfDocument = createPdfA(inputImages, pdfWriter, documentProperties, pdfOutputIntent);
            pdfDocument.close();
        }
    }

    /**
     * Gets used {@link IOcrEngine}.
     *
     * Returns {@link IOcrEngine} reader object to perform OCR.
     * @return selected {@link IOcrEngine} instance
     */
    public final IOcrEngine getOcrEngine() {
        return ocrEngine;
    }

    /**
     * Sets {@link IOcrEngine} reader object to perform OCR.
     * @param reader selected {@link IOcrEngine} instance
     */
    public final void setOcrEngine(final IOcrEngine reader) {
        ocrEngine = reader;
    }

    /**
     * Adds image (or its one page) and text that was found there to canvas.
     *
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param imageData input image if it is a single page or its one page if
     *                 this is a multi-page image
     * @param createPdfA3u true if PDF/A3u document is being created
     * @throws PdfOcrException if PDF/A3u document is being created and provided
     * font contains notdef glyphs
     */
    private void addToCanvas(final PdfDocument pdfDocument,
            final Rectangle imageSize,
            final List<TextInfo> pageText, final ImageData imageData,
            final boolean createPdfA3u) throws PdfOcrException {
        final Rectangle rectangleSize =
                ocrPdfCreatorProperties.getPageSize() == null
                        ? imageSize : ocrPdfCreatorProperties.getPageSize();
        PageSize size = new PageSize(rectangleSize);
        PdfPage pdfPage = pdfDocument.addNewPage(size);
        PdfCanvas canvas = new NotDefCheckingPdfCanvas(pdfPage, createPdfA3u);

        PdfLayer[] layers = createPdfLayers(ocrPdfCreatorProperties.getImageLayerName(),
                ocrPdfCreatorProperties.getTextLayerName(),
                pdfDocument);

        if (layers[0] != null) {
            canvas.beginLayer(layers[0]);
        }
        addImageToCanvas(imageData, imageSize, canvas);
        if (layers[0] != null && layers[0] != layers[1]) {
            canvas.endLayer();
        }

        // how much the original image size changed
        float multiplier = imageData == null
                ? 1 : imageSize.getWidth()
                / PdfCreatorUtil.getPoints(imageData.getWidth());
        if (layers[1] != null && layers[0] != layers[1]) {
            canvas.beginLayer(layers[1]);
        }

        try {
            // A map of TextInfo to a tag pointer, always empty if tagging is not supported
            Map<TextInfo, TagTreePointer> flatLogicalTree = new HashMap<>();
            if (ocrPdfCreatorProperties.isTagged()) {
                // Logical tree, a list of top items, children can be retrieved out of them
                List<LogicalStructureTreeItem> logicalTree = new ArrayList<>();
                // A map of leaf LogicalStructureTreeItem's to TextInfo's attached to these leaves
                Map<LogicalStructureTreeItem, List<TextInfo>> leavesTextInfos = getLogicalTree(pageText, logicalTree);
                pdfDocument.setTagged();

                // Create a map of TextInfo to tag pointers meanwhile creating the required tags.
                // Tag pointers are later used to put all the required info into canvas (content stream)
                buildLogicalTreeAndFlatten(logicalTree, leavesTextInfos,
                        new TagTreePointer(pdfDocument).setPageForTagging(pdfPage), flatLogicalTree);
            }
            addTextToCanvas(imageSize, pageText, flatLogicalTree, canvas, multiplier, pdfPage);
        } catch (PdfOcrException e) {
            LOGGER.error(MessageFormatUtil.format(
                    PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                    e.getMessage()));
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT)
                    .setMessageParams(e.getMessage());
        }
        if (layers[1] != null) {
            canvas.endLayer();
        }
    }

    private PdfDocument createPdfDocument(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent,
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData,
            SequenceId pdfSequenceId, DocumentProperties documentProperties) {
        PdfDocument pdfDocument;
        boolean createPdfA3u = pdfOutputIntent != null;
        if (createPdfA3u) {
            pdfDocument = new PdfADocument(pdfWriter,
                    PdfAConformanceLevel.PDF_A_3U, pdfOutputIntent,
                    documentProperties);
        } else {
            pdfDocument = new PdfDocument(pdfWriter,
                    documentProperties);
        }
        LinkDocumentIdEvent linkDocumentIdEvent = new LinkDocumentIdEvent(pdfDocument, pdfSequenceId);
        EventManager.getInstance().onEvent(linkDocumentIdEvent);

        // pdfLang should be set in PDF/A mode
        boolean hasPdfLangProperty = ocrPdfCreatorProperties.getPdfLang() != null
                && !ocrPdfCreatorProperties.getPdfLang().equals("");
        if (createPdfA3u && !hasPdfLangProperty) {
            LOGGER.error(MessageFormatUtil.format(
                    PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                    PdfOcrLogMessageConstant.PDF_LANGUAGE_PROPERTY_IS_NOT_SET));
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT)
                    .setMessageParams(PdfOcrLogMessageConstant.PDF_LANGUAGE_PROPERTY_IS_NOT_SET);
        }

        // add metadata
        if (hasPdfLangProperty) {
            pdfDocument.getCatalog()
                    .setLang(new PdfString(ocrPdfCreatorProperties.getPdfLang()));
        }

        // set title if it is not empty
        if (ocrPdfCreatorProperties.getTitle() != null) {
            pdfDocument.getCatalog().setViewerPreferences(
                    new PdfViewerPreferences().setDisplayDocTitle(true));
            PdfDocumentInfo info = pdfDocument.getDocumentInfo();
            info.setTitle(ocrPdfCreatorProperties.getTitle());
        }

        // reset passed font provider
        ocrPdfCreatorProperties.getFontProvider().reset();

        addDataToPdfDocument(imagesTextData, pdfDocument, createPdfA3u);

        // statisctics event about type of created pdf
        if (ocrEngine instanceof IProductAware
                && ((IProductAware) ocrEngine).getProductData() != null) {
            PdfOcrOutputType eventType = createPdfA3u ? PdfOcrOutputType.PDFA : PdfOcrOutputType.PDF;
            PdfOcrOutputTypeStatisticsEvent docTypeStatisticsEvent =
                    new PdfOcrOutputTypeStatisticsEvent(eventType, ((IProductAware) ocrEngine).getProductData());
            EventManager.getInstance().onEvent(docTypeStatisticsEvent);
        }

        return pdfDocument;
    }

    /**
     * Places provided images and recognized text to the result PDF document.
     *
     * @param imagesTextData map that contains input image
     *                       files as keys, and as value:
     *                       map pageNumber -> text for the page
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param createPdfA3u true if PDF/A3u document is being created
     * @throws PdfOcrException if input image cannot be read or provided font
     * contains NOTDEF glyphs
     */
    private void addDataToPdfDocument(
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData,
            final PdfDocument pdfDocument,
            final boolean createPdfA3u) throws PdfOcrException {
        for (Map.Entry<File, Map<Integer, List<TextInfo>>> entry
                : imagesTextData.entrySet()) {
            File inputImage = entry.getKey();
            List<ImageData> imageDataList =
                    PdfCreatorUtil.getImageData(inputImage,
                            ocrPdfCreatorProperties.getImageRotationHandler());
            LOGGER.info(MessageFormatUtil.format(
                    PdfOcrLogMessageConstant.NUMBER_OF_PAGES_IN_IMAGE,
                    inputImage.toString(), imageDataList.size()));

            Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
            if (imageTextData.keySet().size() > 0) {
                for (int page = 0; page < imageDataList.size(); ++page) {
                    ImageData imageData = imageDataList.get(page);
                    final Rectangle imageSize =
                            PdfCreatorUtil.calculateImageSize(
                                    imageData,
                                    ocrPdfCreatorProperties.getScaleMode(),
                                    ocrPdfCreatorProperties.getPageSize());

                    if (imageTextData.containsKey(page + 1)) {
                        addToCanvas(pdfDocument, imageSize,
                                imageTextData.get(page + 1),
                                imageData, createPdfA3u);
                    }
                }
            }
        }
    }

    /**
     * Places given image to canvas to background to a separate layer.
     *
     * @param imageData input image as {@link java.io.File}
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pdfCanvas canvas to place the image
     */
    private void addImageToCanvas(final ImageData imageData,
            final Rectangle imageSize,
            final PdfCanvas pdfCanvas) {
        if (imageData != null) {
            if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.openTag(new CanvasArtifact());
            }
            if (ocrPdfCreatorProperties.getPageSize() == null) {
                pdfCanvas.addImageFittedIntoRectangle(imageData, imageSize, false);
            } else {
                final Point coordinates =
                        PdfCreatorUtil.calculateImageCoordinates(
                        ocrPdfCreatorProperties.getPageSize(), imageSize);
                final Rectangle rect =
                        new Rectangle(
                                (float)coordinates.getX(), (float)coordinates.getY(),
                                imageSize.getWidth(), imageSize.getHeight());
                pdfCanvas.addImageFittedIntoRectangle(imageData, rect, false);
            }

            if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.closeTag();
            }
        }
    }

    private static Map<LogicalStructureTreeItem, List<TextInfo>> getLogicalTree(
            List<TextInfo> textInfos, List<LogicalStructureTreeItem> logicalStructureTreeItems) {

        Map<LogicalStructureTreeItem, List<TextInfo>> leavesTextInfos = new HashMap<>();
        if (textInfos == null) {
            return leavesTextInfos;
        }

        for (TextInfo textInfo : textInfos) {
            LogicalStructureTreeItem structTreeItem = textInfo.getLogicalStructureTreeItem();
            LogicalStructureTreeItem topParent;
            if (structTreeItem instanceof ArtifactItem) {
                continue;
            } else if (structTreeItem != null) {
                topParent = getTopParent(structTreeItem);
            } else {
                structTreeItem = new LogicalStructureTreeItem();
                textInfo.setLogicalStructureTreeItem(structTreeItem);
                topParent = structTreeItem;
            }
            List<TextInfo> textInfosPerStructItem = leavesTextInfos.get(structTreeItem);
            if (textInfosPerStructItem == null) {
                textInfosPerStructItem = new ArrayList<>();
                textInfosPerStructItem.add(textInfo);
                leavesTextInfos.put(structTreeItem, textInfosPerStructItem);
            } else {
                textInfosPerStructItem.add(textInfo);
            }
            if (!logicalStructureTreeItems.contains(topParent)) {
                logicalStructureTreeItems.add(topParent);
            }
        }

        return leavesTextInfos;
    }

    private static LogicalStructureTreeItem getTopParent(LogicalStructureTreeItem structInfo) {
        if (structInfo.getParent() != null) {
            return getTopParent(structInfo.getParent());
        } else {
            return structInfo;
        }
    }

    private void buildLogicalTreeAndFlatten(
            List<LogicalStructureTreeItem> logicalStructureTreeItems,
            Map<LogicalStructureTreeItem, List<TextInfo>> leavesTextInfos,
            TagTreePointer tagPointer, Map<TextInfo, TagTreePointer> flatLogicalTree) {
        for (LogicalStructureTreeItem structTreeItem : logicalStructureTreeItems) {
            AccessibilityProperties accessibilityProperties = structTreeItem.getAccessibilityProperties();
            if (accessibilityProperties == null) {
                accessibilityProperties = new DefaultAccessibilityProperties(PdfName.Span.getValue());
            }

            tagPointer.addTag(accessibilityProperties);

            List<TextInfo> textItems = leavesTextInfos.get(structTreeItem);
            if (textItems != null) {
                for (TextInfo item : textItems) {
                    flatLogicalTree.put(item, new TagTreePointer(tagPointer));
                }
            }

            buildLogicalTreeAndFlatten(structTreeItem.getChildren(), leavesTextInfos, tagPointer, flatLogicalTree);
            tagPointer.moveToParent();
        }
    }

    /**
     * Places retrieved text to canvas to a separate layer.
     *
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param flatLogicalTree a map of TextInfo to a tag pointer
     * @param pdfCanvas canvas to place the text
     * @param multiplier coefficient to adjust text placing on canvas
     * @param page current page
     * @throws PdfOcrException if PDF/A3u document is being created and provided
     * font contains notdef glyphs
     */
    private void addTextToCanvas(
            final Rectangle imageSize,
            final List<TextInfo> pageText,
            final Map<TextInfo, TagTreePointer> flatLogicalTree,
            final PdfCanvas pdfCanvas,
            final float multiplier,
            final PdfPage page)
            throws PdfOcrException {
        if (pageText == null || pageText.size() == 0) {
            return;
        }

        final Rectangle pageMediaBox = page.getMediaBox();

        final Point imageCoordinates =
                PdfCreatorUtil.calculateImageCoordinates(
                ocrPdfCreatorProperties.getPageSize(), imageSize);
        for (TextInfo item : pageText) {
            final float bboxWidthPt = getWidthPt(item, multiplier);
            final float bboxHeightPt = getHeightPt(item, multiplier);
            FontProvider fontProvider = getOcrPdfCreatorProperties()
                    .getFontProvider();
            String fontFamily = getOcrPdfCreatorProperties()
                    .getDefaultFontFamily();
            String line = item.getText();
            if (!lineNotEmpty(line, bboxHeightPt, bboxWidthPt)) {
                continue;
            }

            Document document = new Document(pdfCanvas.getDocument());
            document.setFontProvider(fontProvider);

            // Scale the text width to fit the OCR bbox
            final float fontSize = PdfCreatorUtil.calculateFontSize(
                    document, line, fontFamily,
                    bboxHeightPt, bboxWidthPt);

            final float lineWidth = PdfCreatorUtil.getRealLineWidth(document,
                    line, fontFamily, fontSize);

            final float xOffset = getXOffsetPt(item, multiplier);
            final float yOffset = getYOffsetPt(item, multiplier, imageSize);

            TagTreePointer tagPointer = flatLogicalTree.get(item);
            if (tagPointer != null) {
                pdfCanvas.openTag(tagPointer.getTagReference());
            } else if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.openTag(new CanvasArtifact());
            }

            Canvas canvas = new Canvas(pdfCanvas, pageMediaBox);
            canvas.setFontProvider(fontProvider);

            Text text = new Text(line)
                    .setHorizontalScaling(bboxWidthPt / lineWidth);

            Paragraph paragraph = new Paragraph(text)
                    .setMargin(0);
            paragraph.setFontFamily(fontFamily)
                    .setFontSize(fontSize);
            paragraph.setWidth(bboxWidthPt * 1.5f);

            if (ocrPdfCreatorProperties.getTextColor() != null) {
                paragraph.setFontColor(ocrPdfCreatorProperties.getTextColor());
            } else {
                paragraph.setTextRenderingMode(TextRenderingMode.INVISIBLE);
            }

            canvas.showTextAligned(paragraph,
                    xOffset + (float) imageCoordinates.getX(),
                    yOffset + (float) imageCoordinates.getY(),
                    TextAlignment.LEFT);

            if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.closeTag();
            }

            canvas.close();
        }
    }

    /**
     * Creates layers for image and text according rules set in {@link OcrPdfCreatorProperties}.
     *
     * @param imageLayerName name of the image layer
     * @param textLayerName name of the text layer
     * @param pdfDocument document to add layers to
     *
     * @return array of two layers: first layer is for image, second layer is for text.
     * Elements may be null meaning that layer creation is not requested
     */
    private static PdfLayer[] createPdfLayers(
            String imageLayerName,
            String textLayerName,
            PdfDocument pdfDocument) {
        if (imageLayerName == null && textLayerName == null) {
            return new PdfLayer[] {null, null};
        } else if (imageLayerName == null) {
            return new PdfLayer[]{null, new PdfLayer(textLayerName, pdfDocument)};
        } else if (textLayerName == null) {
            return new PdfLayer[]{new PdfLayer(imageLayerName, pdfDocument), null};
        } else if (imageLayerName.equals(textLayerName)) {
            PdfLayer pdfLayer = new PdfLayer(imageLayerName, pdfDocument);
            return new PdfLayer[] {pdfLayer, pdfLayer};
        } else {
            return new PdfLayer[] {new PdfLayer(imageLayerName, pdfDocument), new PdfLayer(textLayerName, pdfDocument)};
        }
    }

    /**
     * Get left bound of text chunk.
     */
    private static float getLeft(TextInfo textInfo, float multiplier) {
        return textInfo.getBboxRect().getLeft() * multiplier;
    }

    /**
     * Get right bound of text chunk.
     */
    private static float getRight(TextInfo textInfo, float multiplier) {
        return (textInfo.getBboxRect().getRight() + 1) * multiplier - 1;
    }

    /**
     * Get top bound of text chunk.
     */
    private static float getTop(TextInfo textInfo, float multiplier) {
        return textInfo.getBboxRect().getTop() * multiplier;
    }

    /**
     * Get bottom bound of text chunk.
     */
    private static float getBottom(TextInfo textInfo, float multiplier) {
        return (textInfo.getBboxRect().getBottom() + 1) * multiplier - 1;
    }

    /**
     * Check if line is not empty.
     */
    private static boolean lineNotEmpty(String line, float bboxHeightPt, float bboxWidthPt) {
        return !line.isEmpty() && bboxHeightPt > 0 && bboxWidthPt > 0;
    }

    /**
     * Get width of text chunk in points.
     */
    private static float getWidthPt(TextInfo textInfo, float multiplier) {
        if (textInfo.getBboxRect() == null) {
            return PdfCreatorUtil.getPoints(
                    getRight(textInfo, multiplier) - getLeft(textInfo, multiplier));
        } else {
            return getRight(textInfo, multiplier) - getLeft(textInfo, multiplier);
        }
    }

    /**
     * Get height of text chunk in points.
     */
    private static float getHeightPt(TextInfo textInfo, float multiplier) {
        if (textInfo.getBboxRect() == null) {
            return PdfCreatorUtil.getPoints(
                    getBottom(textInfo, multiplier) - getTop(textInfo, multiplier));
        } else {
            return getTop(textInfo, multiplier) - getBottom(textInfo, multiplier);
        }
    }

    /**
     * Get horizontal text offset in points.
     */
    private static float getXOffsetPt(TextInfo textInfo, float multiplier) {
        if (textInfo.getBboxRect() == null) {
            return PdfCreatorUtil.getPoints(getLeft(textInfo, multiplier));
        } else {
            return getLeft(textInfo, multiplier);
        }
    }

    /**
     * Get vertical text offset in points.
     */
    private static float getYOffsetPt(TextInfo textInfo, float multiplier, Rectangle imageSize) {
        if (textInfo.getBboxRect() == null) {
            return imageSize.getHeight() - PdfCreatorUtil.getPoints(getBottom(textInfo, multiplier));
        } else {
            return getBottom(textInfo, multiplier);
        }
    }

    /**
     * A handler for PDF canvas that validates existing glyphs.
     */
    private static class NotDefCheckingPdfCanvas extends PdfCanvas {
        private final boolean createPdfA3u;
        public NotDefCheckingPdfCanvas(PdfPage page, boolean createPdfA3u) {
            super(page);
            this.createPdfA3u = createPdfA3u;
        }

        @Override
        public PdfCanvas showText(GlyphLine text) {
            ActualTextCheckingGlyphLine glyphLine =
                    new ActualTextCheckingGlyphLine(text);
            PdfFont currentFont = getGraphicsState().getFont();
            boolean notDefGlyphsExists = false;
            // default value for error message, it'll be updated with the
            // unicode of the not found glyph
            String message = PdfOcrLogMessageConstant
                    .COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER;
            for (int i = glyphLine.start; i < glyphLine.end; i++) {
                if (isNotDefGlyph(currentFont, glyphLine.get(i))) {
                    notDefGlyphsExists = true;
                    message = MessageFormatUtil.format(PdfOcrLogMessageConstant
                                    .COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER,
                            glyphLine.get(i).getUnicode());
                    if (this.createPdfA3u) {
                        // exception is thrown only if PDF/A document is
                        // being created
                        throw new PdfOcrException(message);
                    }
                    // setting actual text to NotDef glyph
                    glyphLine.setActualTextToGlyph(i,
                            glyphLine.toUnicodeString(i, i + 1));
                    // setting a fake unicode deliberately to pass further
                    // checks for actual text necessity during iterating over
                    // glyphline chunks with ActualTextIterator
                    Glyph glyph = new Glyph(glyphLine.get(i));
                    glyph.setUnicode(-1);
                    glyphLine.set(i, glyph);
                }
            }
            // Warning is logged if not PDF/A document is being created
            if (notDefGlyphsExists) {
                LOGGER.warn(message);
            }
            return this.showText(glyphLine, new ActualTextIterator(glyphLine));
        }

        private static boolean isNotDefGlyph(PdfFont font, Glyph glyph) {
            if (font instanceof PdfType0Font
                    || font instanceof PdfTrueTypeFont) {
                return glyph.getCode() == 0;
            } else if (font instanceof PdfType1Font
                    || font instanceof PdfType3Font) {
                return glyph.getCode() == -1;
            }
            return false;
        }
    }

    /**
     * A handler for GlyphLine that checks existing actual text not to
     * overwrite it.
     */
    private static class ActualTextCheckingGlyphLine extends GlyphLine {

        public ActualTextCheckingGlyphLine(GlyphLine other) {
            super(other);
        }

        public void setActualTextToGlyph(int i, String text) {
            // set actual text if it doesn't exist for i-th glyph
            if ((this.actualText == null || this.actualText.size() <= i
                    || this.actualText.get(i) == null)) {
                super.setActualText(i, i + 1, text);
            }
        }
    }
}
