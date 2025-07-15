/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
import com.itextpdf.kernel.pdf.PdfAConformance;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
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
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.pdfa.PdfADocument;
import com.itextpdf.pdfocr.ImageExtraction.PageImageData;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputType;
import com.itextpdf.pdfocr.statistics.PdfOcrOutputTypeStatisticsEvent;
import com.itextpdf.pdfocr.structuretree.ArtifactItem;
import com.itextpdf.pdfocr.structuretree.LogicalStructureTreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
 * <p>
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
    private static final Logger LOGGER = LoggerFactory.getLogger(OcrPdfCreator.class);

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
    public OcrPdfCreator(final IOcrEngine ocrEngine, final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
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
    public final void setOcrPdfCreatorProperties(final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        this.ocrPdfCreatorProperties = ocrPdfCreatorProperties;
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}, {@link DocumentProperties}
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
     * @throws PdfOcrException if it was not possible to read provided or default font
     */
    public final PdfDocument createPdfA(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final DocumentProperties documentProperties,
            final PdfOutputIntent pdfOutputIntent,
            final IOcrProcessProperties ocrProcessProperties)
            throws PdfOcrException {
        LOGGER.info(MessageFormatUtil.format(PdfOcrLogMessageConstant.START_OCR_FOR_IMAGES, inputImages.size()));

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
        Map<File, Map<Integer, List<TextInfo>>> imagesTextData = new LinkedHashMap<File, Map<Integer, List<TextInfo>>>(
                inputImages.size() * 2);

        for (File inputImage : inputImages) {
            imagesTextData.put(inputImage, ocrEngine.doImageOcr(inputImage, ocrProcessContext));
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
     * Gets used {@link IOcrEngine} reader object to perform OCR.
     *
     * @return selected {@link IOcrEngine} instance
     */
    public final IOcrEngine getOcrEngine() {
        return ocrEngine;
    }

    /**
     * Sets {@link IOcrEngine} reader object to perform OCR.
     *
     * @param reader selected {@link IOcrEngine} instance
     */
    public final void setOcrEngine(final IOcrEngine reader) {
        ocrEngine = reader;
    }

    /**
     * Performs OCR of all images in an input PDF file and generates searchable PDF.
     *
     * <p>
     * By default, it does not allow to OCR PDF/A documents and tagged documents. The reason is that the result document
     * might not comply with PDF/A specification and an added content might be not tagged depending on the
     * {@link IOcrEngine} implementation. To overrule this behavior one can override
     * {@link OcrPdfCreator#validateInputPdfDocument} with an empty implementation.
     *
     * <p>
     * Note that {@link OcrPdfCreatorProperties#setPageSize}, {@link OcrPdfCreatorProperties#setScaleMode(ScaleMode)}
     * and {@link OcrPdfCreatorProperties#setImageLayerName(String)} have no effect for this method.
     *
     * @param inputPdf PDF file to OCR
     * @param outputPdf searchable PDF with the recognized text on top of the images
     *
     * @throws com.itextpdf.io.exceptions.IOException if an image cannot be extracted from a PDF file
     * @throws PdfOcrException in case of any other OCR error
     */
    public void makePdfSearchable(File inputPdf, File outputPdf)
            throws com.itextpdf.io.exceptions.IOException, PdfOcrException {
        makePdfSearchable(inputPdf, outputPdf, null);
    }

    /**
     * Performs OCR of all images in an input PDF file and generates searchable PDF.
     *
     * <p>
     * By default, it does not allow to OCR PDF/A documents and tagged documents. The reason is that the result document
     * might not comply with PDF/A specification and an added content might be not tagged depending on the
     * {@link IOcrEngine} implementation. To overrule this behavior one can override
     * {@link OcrPdfCreator#validateInputPdfDocument} with an empty implementation.
     *
     * <p>
     * Note that {@link OcrPdfCreatorProperties#setPageSize}, {@link OcrPdfCreatorProperties#setScaleMode(ScaleMode)}
     * and {@link OcrPdfCreatorProperties#setImageLayerName(String)} have no effect for this method.
     *
     * @param inputPdf PDF file to OCR
     * @param outputPdf searchable PDF with the recognized text on top of the images
     * @param ocrProcessProperties extra OCR process properties passed to {@link OcrProcessContext}.
     *
     * @throws com.itextpdf.io.exceptions.IOException if an image cannot be extracted from a pdf
     * @throws PdfOcrException in case of any other OCR error
     */
    public void makePdfSearchable(File inputPdf, File outputPdf, IOcrProcessProperties ocrProcessProperties)
            throws com.itextpdf.io.exceptions.IOException, PdfOcrException {
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputPdf), new PdfWriter(outputPdf))) {
            makePdfSearchable(pdfDoc, ocrProcessProperties);
        } catch (IOException e) {
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.IO_EXCEPTION_OCCURRED, e);
        }
    }

    /**
     * Performs OCR of all images in an input PDF document and adds recognized text on top of the images.
     *
     * <p>
     * By default, it does not allow to OCR PDF/A documents and tagged documents. The reason is that the result document
     * might not comply with PDF/A specification and an added content might be not tagged depending on the
     * {@link IOcrEngine} implementation. To overrule this behavior one can override
     * {@link OcrPdfCreator#validateInputPdfDocument} with an empty implementation.
     *
     * <p>
     * Note that {@link OcrPdfCreatorProperties#setPageSize}, {@link OcrPdfCreatorProperties#setScaleMode(ScaleMode)}
     * and {@link OcrPdfCreatorProperties#setImageLayerName(String)} have no effect for this method.
     *
     * @param pdfDoc PDF document with images to OCR
     *
     * @throws com.itextpdf.io.exceptions.IOException if an image cannot be extracted from a pdf
     * @throws PdfOcrException in case of any other OCR error
     */
    public void makePdfSearchable(PdfDocument pdfDoc)
            throws com.itextpdf.io.exceptions.IOException, PdfOcrException {
        makePdfSearchable(pdfDoc, null);
    }

    /**
     * Performs OCR of all images in an input PDF document and adds recognized text on top of the images.
     *
     * <p>
     * By default, it does not allow to OCR PDF/A documents and tagged documents. The reason is that the result document
     * might not comply with PDF/A specification and an added content might be not tagged depending on the
     * {@link IOcrEngine} implementation. To overrule this behavior one can override
     * {@link OcrPdfCreator#validateInputPdfDocument} with an empty implementation.
     *
     * <p>
     * Note that {@link OcrPdfCreatorProperties#setPageSize}, {@link OcrPdfCreatorProperties#setScaleMode(ScaleMode)}
     * and {@link OcrPdfCreatorProperties#setImageLayerName(String)} have no effect for this method.
     *
     * @param pdfDoc PDF document with images to OCR
     * @param ocrProcessProperties extra OCR process properties passed to {@link OcrProcessContext}
     *
     * @throws com.itextpdf.io.exceptions.IOException if an image cannot be extracted from a pdf
     * @throws PdfOcrException in case of any other OCR error
     */
    public void makePdfSearchable(PdfDocument pdfDoc, IOcrProcessProperties ocrProcessProperties)
            throws com.itextpdf.io.exceptions.IOException, PdfOcrException {
        // Only PdfDocument in stamping mode is allowed
        if (pdfDoc.getReader() == null || pdfDoc.getWriter() == null) {
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE);
        }

        validateInputPdfDocument(pdfDoc);

        if (ocrPdfCreatorProperties.getPageSize() != null) {
            LOGGER.warn(PdfOcrLogMessageConstant.PAGE_SIZE_IS_NOT_APPLIED);
            ocrPdfCreatorProperties.setPageSize(null);
        }
        if (ocrPdfCreatorProperties.getImageLayerName() != null) {
            LOGGER.warn(PdfOcrLogMessageConstant.IMAGE_LAYER_NAME_IS_NOT_APPLIED);
            ocrPdfCreatorProperties.setImageLayerName(null);
        }

        // Let's respect language and title properties
        final boolean hasPdfLangProperty = ocrPdfCreatorProperties.getPdfLang() != null
                && !ocrPdfCreatorProperties.getPdfLang().isEmpty();
        if (hasPdfLangProperty) {
            pdfDoc.getCatalog().setLang(new PdfString(ocrPdfCreatorProperties.getPdfLang()));
        }

        // Set title
        if (ocrPdfCreatorProperties.getTitle() != null) {
            pdfDoc.getCatalog().setViewerPreferences(
                    new PdfViewerPreferences().setDisplayDocTitle(true));
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            info.setTitle(ocrPdfCreatorProperties.getTitle());
        }

        // Reset passed font provider
        ocrPdfCreatorProperties.getFontProvider().reset();

        // Create event helper
        OcrPdfCreatorEventHelper ocrEventHelper =
                new OcrPdfCreatorEventHelper(pdfDoc.getDocumentIdWrapper(), ocrPdfCreatorProperties.getMetaInfo());
        OcrProcessContext ocrProcessContext = new OcrProcessContext(ocrEventHelper);
        ocrProcessContext.setOcrProcessProperties(ocrProcessProperties);

        // Create layers if requested
        PdfLayer[] layers = createPdfLayers(ocrPdfCreatorProperties.getImageLayerName(),
                ocrPdfCreatorProperties.getTextLayerName(),
                pdfDoc);

        List<String> allImagePaths = new ArrayList<>();
        try {
            for (int pageNr = 1; pageNr <= pdfDoc.getNumberOfPages(); ++pageNr) {
                PdfPage pdfPage = pdfDoc.getPage(pageNr);
                // Extract images to temp files
                List<PageImageData> pageImageData = ImageExtraction.extractImagesFromPdfPage(pdfPage);
                // Image file - image position on the page + OCR result
                Map<PageImageData, Map<Integer, List<TextInfo>>> imagesTextData =
                        new LinkedHashMap<>(pageImageData.size());
                for (PageImageData image : pageImageData) {
                    allImagePaths.add(image.getPath().getAbsolutePath());
                    imagesTextData.put(image, ocrEngine.doImageOcr(image.getPath(), ocrProcessContext));
                }

                // Put the result into pdf
                addToPdfPage(pdfPage, imagesTextData, layers[1]);
            }
        } catch (IOException e) {
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.IO_EXCEPTION_OCCURRED, e);
        } finally {
            for (String imagePath : allImagePaths) {
                try {
                    Files.delete(Paths.get(imagePath));
                } catch (Exception e) {
                    // Some temp file might not be removed. Not a big deal.
                }
            }
        }
    }

    /**
     * Validates input PDF document.
     *
     * <p>
     * It checks that an input document is not tagged and not PDF/A. If you need to OCR tagged and/or PDF/A documents,
     * override this method with empty implementation. In that case it would be best to use
     * {@link OcrPdfCreator#makePdfSearchable(PdfDocument, IOcrProcessProperties)} overload because there you can pass
     * {@link PdfADocument} or PdfUADocument instance which will do the validation of the output document.
     *
     * @param pdfDoc a PDF document to check
     */
    protected void validateInputPdfDocument(PdfDocument pdfDoc) {
        if (pdfDoc.isTagged()) {
            // None of our engines supports tagging so far. Theoretically if tagging is supported, we could proceed
            // but then it opens another question. What to do with PDF UA? Still forbid or rely on our UA checks?
            // User probably can provide all the required info not to break the conformance but still.
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.TAGGED_PDF_IS_NOT_SUPPORTED);
        }

        if (pdfDoc.getConformance().isPdfA()) {
            // Even though we allow to create pdf/a documents from images,
            // it would still be safer to forbid pdfa input for now.
            // For example, input document may be without output intent. Then we have to request it from the user.
            // It complicates API and might still be not enough.
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.PDFA_IS_NOT_SUPPORTED);
        }
    }

    /**
     * Adds image (or its one page) and text that was found there to canvas.
     *
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param imageSizeOnPage size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param imageData input image if it is a single page or its one page if
     *                 this is a multi-page image
     * @param createPdfA3u true if PDF/A3u document is being created
     * @param layers an array with 2 elements representing PDF layers for image and text
     *
     * @throws PdfOcrException if PDF/A3u document is being created and provided
     * font contains notdef glyphs
     */
    private void addToCanvas(final PdfDocument pdfDocument,
            final Rectangle imageSizeOnPage,
            final List<TextInfo> pageText, final ImageData imageData,
            final boolean createPdfA3u,
            final PdfLayer[] layers) throws PdfOcrException {
        final Rectangle rectangleSize =
                ocrPdfCreatorProperties.getPageSize() == null
                        ? imageSizeOnPage : ocrPdfCreatorProperties.getPageSize();
        PageSize size = new PageSize(rectangleSize);
        PdfPage pdfPage = pdfDocument.addNewPage(size);
        PdfCanvas canvas = new NotDefCheckingPdfCanvas(pdfPage, createPdfA3u);

        if (layers[0] != null) {
            canvas.beginLayer(layers[0]);
        }
        addImageToCanvas(imageData, imageSizeOnPage, canvas);
        if (layers[0] != null && layers[0] != layers[1]) {
            canvas.endLayer();
        }

        if (layers[1] != null && layers[0] != layers[1]) {
            canvas.beginLayer(layers[1]);
        }
        collectTextAndAddToCanvas(pdfPage, canvas, pageText, imageSizeOnPage,
                new Rectangle(imageData.getWidth(), imageData.getHeight()));
        if (layers[1] != null) {
            canvas.endLayer();
        }
    }

    private void collectTextAndAddToCanvas(PdfPage pdfPage, PdfCanvas canvas, List<TextInfo> pageText,
            Rectangle imageBbox, Rectangle imageSize) {
        PdfDocument pdfDocument = pdfPage.getDocument();

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

            // How much the original image size changed
            final float widthMultiplier = imageBbox.getWidth() / PdfCreatorUtil.getPoints(imageSize.getWidth());
            final float heightMultiplier = imageBbox.getHeight() / PdfCreatorUtil.getPoints(imageSize.getHeight());

            addTextToCanvas(imageBbox, pageText, flatLogicalTree, canvas, widthMultiplier, heightMultiplier, pdfPage);
        } catch (PdfOcrException e) {
            LOGGER.error(MessageFormatUtil.format(
                    PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                    e.getMessage()));
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT)
                    .setMessageParams(e.getMessage());
        }
    }

    /**
     * @param imagesTextData a map where the key is {@link PageImageData} and the value is an OCR result
     */
    private void addToPdfPage(PdfPage pdfPage,
            Map<PageImageData, Map<Integer, List<TextInfo>>> imagesTextData, PdfLayer pdfLayer) {
        for (Map.Entry<PageImageData, Map<Integer, List<TextInfo>>> entry : imagesTextData.entrySet()) {
            // Key in OCR result is always 1 here
            List<TextInfo> textInfos = entry.getValue().get(1);
            PdfCanvas canvas = new PdfCanvas(pdfPage);
            Rectangle imageSize = new Rectangle(entry.getKey().getXObject().getWidth(),
                    entry.getKey().getXObject().getHeight());

            if (pdfLayer != null) {
                canvas.beginLayer(pdfLayer);
            }
            collectTextAndAddToCanvas(pdfPage, canvas, textInfos, entry.getKey().getPagePosition(), imageSize);
            if (pdfLayer != null) {
                canvas.endLayer();
            }
        }
    }

    private PdfDocument createPdfDocument(final PdfWriter pdfWriter, final PdfOutputIntent pdfOutputIntent,
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData,
            SequenceId pdfSequenceId, DocumentProperties documentProperties) {

        PdfDocument pdfDocument;
        boolean createPdfA3u = pdfOutputIntent != null;
        if (createPdfA3u) {
            pdfDocument = new PdfADocument(pdfWriter, PdfAConformance.PDF_A_3U, pdfOutputIntent, documentProperties);
        } else {
            pdfDocument = new PdfDocument(pdfWriter, documentProperties);
        }
        LinkDocumentIdEvent linkDocumentIdEvent = new LinkDocumentIdEvent(pdfDocument, pdfSequenceId);
        EventManager.getInstance().onEvent(linkDocumentIdEvent);

        // pdfLang should be set in PDF/A mode
        boolean hasPdfLangProperty = ocrPdfCreatorProperties.getPdfLang() != null
                && !ocrPdfCreatorProperties.getPdfLang().isEmpty();
        if (createPdfA3u && !hasPdfLangProperty) {
            LOGGER.error(MessageFormatUtil.format(PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT,
                    PdfOcrLogMessageConstant.PDF_LANGUAGE_PROPERTY_IS_NOT_SET));
            throw new PdfOcrException(PdfOcrExceptionMessageConstant.CANNOT_CREATE_PDF_DOCUMENT)
                    .setMessageParams(PdfOcrLogMessageConstant.PDF_LANGUAGE_PROPERTY_IS_NOT_SET);
        }

        // add metadata
        if (hasPdfLangProperty) {
            pdfDocument.getCatalog().setLang(new PdfString(ocrPdfCreatorProperties.getPdfLang()));
        }

        // set title if it is not empty
        if (ocrPdfCreatorProperties.getTitle() != null) {
            pdfDocument.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
            PdfDocumentInfo info = pdfDocument.getDocumentInfo();
            info.setTitle(ocrPdfCreatorProperties.getTitle());
        }

        // reset passed font provider
        ocrPdfCreatorProperties.getFontProvider().reset();

        addDataToPdfDocument(imagesTextData, pdfDocument, createPdfA3u);

        // statistics event about type of created pdf
        if (ocrEngine instanceof IProductAware && ((IProductAware) ocrEngine).getProductData() != null) {
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
     *
     * @throws PdfOcrException if input image cannot be read or provided font contains NOTDEF glyphs
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

            PdfLayer[] layers = createPdfLayers(ocrPdfCreatorProperties.getImageLayerName(),
                    ocrPdfCreatorProperties.getTextLayerName(),
                    pdfDocument);

            Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
            if (imageTextData.keySet().size() > 0) {
                for (int page = 0; page < imageDataList.size(); ++page) {
                    ImageData imageData = imageDataList.get(page);
                    final Rectangle imageSizeOnPage =
                            PdfCreatorUtil.calculateImageSize(imageData,
                                    ocrPdfCreatorProperties.getScaleMode(),
                                    ocrPdfCreatorProperties.getPageSize());

                    if (imageTextData.containsKey(page + 1)) {
                        addToCanvas(pdfDocument, imageSizeOnPage, imageTextData.get(page + 1), imageData, createPdfA3u,
                                layers);
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
                                (float) coordinates.getX(), (float) coordinates.getY(),
                                imageSize.getWidth(), imageSize.getHeight());
                pdfCanvas.addImageFittedIntoRectangle(imageData, rect, false);
            }

            if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.closeTag();
            }
        }
    }

    private static void buildLogicalTreeAndFlatten(
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
     * @param imageBbox size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param flatLogicalTree a map of TextInfo to a tag pointer
     * @param pdfCanvas canvas to place the text
     * @param widthMultiplier coefficient to adjust text width on canvas
     * @param heightMultiplier coefficient to adjust text height on canvas
     * @param page current page
     * @throws PdfOcrException if PDF/A3u document is being created and provided
     * font contains notdef glyphs
     */
    private void addTextToCanvas(
            final Rectangle imageBbox,
            final List<TextInfo> pageText,
            final Map<TextInfo, TagTreePointer> flatLogicalTree,
            final PdfCanvas pdfCanvas,
            final float widthMultiplier,
            final float heightMultiplier,
            final PdfPage page)
            throws PdfOcrException {
        if (pageText == null || pageText.isEmpty()) {
            return;
        }

        final Rectangle pageMediaBox = page.getMediaBox();

        final Point imageCoordinates =
                PdfCreatorUtil.calculateImageCoordinates(
                        ocrPdfCreatorProperties.getPageSize(), imageBbox);
        for (TextInfo item : pageText) {
            final float textWidthPt = getTextWidthPt(item, widthMultiplier);
            final float textHeightPt = getTextHeightPt(item, heightMultiplier);
            FontProvider fontProvider = getOcrPdfCreatorProperties()
                    .getFontProvider();
            String fontFamily = getOcrPdfCreatorProperties()
                    .getDefaultFontFamily();
            String line = item.getText();
            if (!lineNotEmpty(line, textHeightPt, textWidthPt)) {
                continue;
            }

            Document document = new Document(pdfCanvas.getDocument());
            document.setFontProvider(fontProvider);

            // Scale the text width to fit the OCR bbox
            final float fontSize = PdfCreatorUtil.calculateFontSize(
                    document, line, fontFamily,
                    textHeightPt, textWidthPt);

            final float lineWidth = PdfCreatorUtil.getRealLineWidth(document,
                    line, fontFamily, fontSize);

            final float xOffset = getXOffsetPt(item, widthMultiplier);
            final float yOffset = getYOffsetPt(item, heightMultiplier);

            TagTreePointer tagPointer = flatLogicalTree.get(item);
            if (tagPointer != null) {
                pdfCanvas.openTag(tagPointer.getTagReference());
            } else if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.openTag(new CanvasArtifact());
            }

            Canvas canvas = new Canvas(pdfCanvas, pageMediaBox);
            canvas.setFontProvider(fontProvider);

            Text text = new Text(line)
                    .setHorizontalScaling(textWidthPt / lineWidth);

            Paragraph paragraph = new Paragraph(text)
                    .setMargin(0)
                    .setFontFamily(fontFamily)
                    .setFontSize(fontSize)
                    .setWidth(textWidthPt * 1.5f);

            if (ocrPdfCreatorProperties.getTextColor() != null) {
                paragraph.setFontColor(ocrPdfCreatorProperties.getTextColor());
            } else {
                paragraph.setTextRenderingMode(TextRenderingMode.INVISIBLE);
            }

            canvas.showTextAligned(paragraph,
                    xOffset + (float) imageCoordinates.getX(),
                    yOffset + (float) imageCoordinates.getY(),
                    canvas.getPdfDocument().getPageNumber(page),
                    TextAlignment.LEFT,
                    VerticalAlignment.BOTTOM,
                    getRotationAngle(item.getOrientation()));

            if (ocrPdfCreatorProperties.isTagged()) {
                pdfCanvas.closeTag();
            }

            canvas.close();
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

    /**
     * Returns the text rotation angle in radian for the provided {@link TextOrientation}.
     *
     * @param orientation text orientation to get the angle for
     *
     * @return the text rotation angle in radian for the provided {@link TextOrientation}
     */
    private static float getRotationAngle(TextOrientation orientation) {
        switch (orientation) {
            case HORIZONTAL_ROTATED_90:
                return (float) (0.5 * Math.PI);
            case HORIZONTAL_ROTATED_180:
                return (float) Math.PI;
            case HORIZONTAL_ROTATED_270:
                return (float) (1.5 * Math.PI);
            case HORIZONTAL:
            default:
                return 0;
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
            return new PdfLayer[] {null, new PdfLayer(textLayerName, pdfDocument)};
        } else if (textLayerName == null) {
            return new PdfLayer[] {new PdfLayer(imageLayerName, pdfDocument), null};
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
    private static float getTextWidthPt(TextInfo textInfo, float multiplier) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_90:
            case HORIZONTAL_ROTATED_270:
                return getTop(textInfo, multiplier) - getBottom(textInfo, multiplier);
            case HORIZONTAL:
            case HORIZONTAL_ROTATED_180:
            default:
                return getRight(textInfo, multiplier) - getLeft(textInfo, multiplier);
        }
    }

    /**
     * Get height of text chunk in points.
     */
    private static float getTextHeightPt(TextInfo textInfo, float multiplier) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_90:
            case HORIZONTAL_ROTATED_270:
                return getRight(textInfo, multiplier) - getLeft(textInfo, multiplier);
            case HORIZONTAL:
            case HORIZONTAL_ROTATED_180:
            default:
                return getTop(textInfo, multiplier) - getBottom(textInfo, multiplier);
        }
    }

    /**
     * Get horizontal text offset in points.
     */
    private static float getXOffsetPt(TextInfo textInfo, float multiplier) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_90:
            case HORIZONTAL_ROTATED_180:
                return getRight(textInfo, multiplier);
            case HORIZONTAL:
            case HORIZONTAL_ROTATED_270:
            default:
                return getLeft(textInfo, multiplier);
        }
    }

    /**
     * Get vertical text offset in points.
     */
    private static float getYOffsetPt(TextInfo textInfo, float multiplier) {
        switch (textInfo.getOrientation()) {
            case HORIZONTAL_ROTATED_180:
            case HORIZONTAL_ROTATED_270:
                return getTop(textInfo, multiplier);
            case HORIZONTAL:
            case HORIZONTAL_ROTATED_90:
            default:
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
            for (int i = glyphLine.getStart(); i < glyphLine.getEnd(); i++) {
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
