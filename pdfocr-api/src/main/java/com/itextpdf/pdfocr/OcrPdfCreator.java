package com.itextpdf.pdfocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.io.util.ResourceUtil;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.TextRenderingMode;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.pdfa.PdfADocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OcrPdfCreator} is the class that creates Pdf documents containing input
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
     * creates pdf using provided {@link com.itextpdf.kernel.pdf.PdfWriter} and
     * {@link com.itextpdf.kernel.pdf.PdfOutputIntent}.
     * PDF/A-3u document will be created if
     * provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final pdf document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     * @return result PDF/A-3u {@link com.itextpdf.kernel.pdf.PdfDocument}
     * object
     * @throws OcrException if it was not possible to read provided or
     * default font
     */
    public final PdfDocument createPdfA(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent)
            throws OcrException {
        LOGGER.info(MessageFormatUtil.format(
                LogMessageConstant.StartOcrForImages,
                inputImages.size()));

        // map contains:
        // keys: image files
        // values:
        // map pageNumber -> retrieved text data(text and its coordinates)
        Map<File, Map<Integer, List<TextInfo>>> imagesTextData =
                new LinkedHashMap<File, Map<Integer, List<TextInfo>>>();
        for (File inputImage : inputImages) {
            imagesTextData.put(inputImage,
                    ocrEngine.doImageOcr(inputImage));
        }

        // create PdfDocument
        return createPdfDocument(pdfWriter, pdfOutputIntent, imagesTextData);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates pdf using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final pdf document to
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     * @throws OcrException if provided font is incorrect
     */
    public final PdfDocument createPdf(final List<File> inputImages,
            final PdfWriter pdfWriter)
            throws OcrException {
        return createPdfA(inputImages, pdfWriter, null);
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
     * Gets font as a byte array using provided fontp ath or the default one.
     *
     * @return selected font as byte[]
     */
    private byte[] getFont() {
        if (ocrPdfCreatorProperties.getFontPath() != null
                && !ocrPdfCreatorProperties.getFontPath().isEmpty()) {
            try {
                return Files.readAllBytes(java.nio.file.Paths
                        .get(ocrPdfCreatorProperties.getFontPath()));
            } catch (IOException | OutOfMemoryError e) {
                LOGGER.error(MessageFormatUtil.format(
                        LogMessageConstant.CannotReadProvidedFont,
                        e.getMessage()));
                return getDefaultFont();
            }
        } else {
            return getDefaultFont();
        }
    }

    /**
     * Gets default font as a byte array.
     *
     * @return default font as byte[]
     */
    private byte[] getDefaultFont() {
        try (InputStream stream = ResourceUtil
                .getResourceStream(getOcrPdfCreatorProperties()
                        .getDefaultFontName())) {
            return StreamUtil.inputStreamToArray(stream);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    LogMessageConstant.CannotReadDefaultFont,
                    e.getMessage()));
            return new byte[0];
        }
    }

    /**
     * Adds image (or its one page) and text that was found there to canvas.
     *
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param font font for the placed text (could be custom or default)
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param imageData input image if it is a single page or its one page if
     *                 this is a multi-page image
     */
    private void addToCanvas(final PdfDocument pdfDocument, final PdfFont font,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> pageText, final ImageData imageData) {
        com.itextpdf.kernel.geom.Rectangle rectangleSize =
                ocrPdfCreatorProperties.getPageSize() == null
                        ? imageSize : ocrPdfCreatorProperties.getPageSize();
        PageSize size = new PageSize(rectangleSize);
        PdfPage pdfPage = pdfDocument.addNewPage(size);
        PdfCanvas canvas = new PdfCanvas(pdfPage);

        PdfLayer imageLayer = new PdfLayer(
                ocrPdfCreatorProperties.getImageLayerName(), pdfDocument);
        PdfLayer textLayer = new PdfLayer(
                ocrPdfCreatorProperties.getTextLayerName(), pdfDocument);

        canvas.beginLayer(imageLayer);
        addImageToCanvas(imageData, imageSize, canvas);
        canvas.endLayer();

        // how much the original image size changed
        float multiplier = imageData == null
                ? 1 : imageSize.getWidth()
                / PdfCreatorUtil.getPoints(imageData.getWidth());
        canvas.beginLayer(textLayer);
        addTextToCanvas(imageSize, pageText, canvas, font,
                multiplier, pdfPage.getMediaBox());
        canvas.endLayer();
    }

    /**
     * Creates a new pdf document using provided properties, adds images with
     * recognized text.
     *
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final pdf document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     * @param imagesTextData Map<File, Map<Integer, List<TextInfo>>> -
     *                       map that contains input image files as keys,
     *                       and as value: map pageNumber -> text for the page
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     */
    private PdfDocument createPdfDocument(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent,
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData) {
        PdfDocument pdfDocument;
        if (pdfOutputIntent != null) {
            pdfDocument = new PdfADocument(pdfWriter,
                    PdfAConformanceLevel.PDF_A_3U, pdfOutputIntent);
        } else {
            pdfDocument = new PdfDocument(pdfWriter);
        }

        // add metadata
        pdfDocument.getCatalog()
                .setLang(new PdfString(ocrPdfCreatorProperties.getPdfLang()));
        pdfDocument.getCatalog().setViewerPreferences(
                new PdfViewerPreferences().setDisplayDocTitle(true));
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
        info.setTitle(ocrPdfCreatorProperties.getTitle());

        // create PdfFont
        PdfFont defaultFont = null;
        try {
            defaultFont = PdfFontFactory.createFont(getFont(),
                    PdfEncodings.IDENTITY_H, true);
        } catch (com.itextpdf.io.IOException | IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    LogMessageConstant.CannotReadProvidedFont,
                    e.getMessage()));
            try {
                defaultFont = PdfFontFactory.createFont(getDefaultFont(),
                        PdfEncodings.IDENTITY_H, true);
            } catch (com.itextpdf.io.IOException
                    | IOException | NullPointerException ex) {
                LOGGER.error(MessageFormatUtil.format(
                        LogMessageConstant.CannotReadDefaultFont,
                        ex.getMessage()));
                throw new OcrException(OcrException.CannotReadFont);
            }
        }
        addDataToPdfDocument(imagesTextData, pdfDocument, defaultFont);

        return pdfDocument;
    }

    /**
     * Places provided images and recognized text to the result PDF document.
     *
     * @param imagesTextData Map<File, Map<Integer, List<TextInfo>>> -
     *                       map that contains input image
     *                       files as keys, and as value:
     *                       map pageNumber -> text for the page
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param font font for the placed text (could be custom or default)
     * @throws OcrException if input image cannot be read
     */
    private void addDataToPdfDocument(
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData,
            final PdfDocument pdfDocument,
            final PdfFont font) throws OcrException {
        for (Map.Entry<File, Map<Integer, List<TextInfo>>> entry
                : imagesTextData.entrySet()) {
            try {
                File inputImage = entry.getKey();
                List<ImageData> imageDataList =
                        PdfCreatorUtil.getImageData(inputImage);
                LOGGER.info(MessageFormatUtil.format(
                        LogMessageConstant.NumberOfPagesInImage,
                        inputImage.toString(), imageDataList.size()));

                Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
                if (imageTextData.keySet().size() > 0) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        com.itextpdf.kernel.geom.Rectangle imageSize =
                                PdfCreatorUtil.calculateImageSize(
                                        imageData,
                                        ocrPdfCreatorProperties.getScaleMode(),
                                        ocrPdfCreatorProperties.getPageSize());

                        if (imageTextData.containsKey(page + 1)) {
                            addToCanvas(pdfDocument, font, imageSize,
                                    imageTextData.get(page + 1),
                                    imageData);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error(MessageFormatUtil.format(
                        LogMessageConstant.CannotAddDataToPdfDocument,
                        e.getMessage()));
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
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final PdfCanvas pdfCanvas) {
        if (imageData != null) {
            if (ocrPdfCreatorProperties.getPageSize() == null) {
                pdfCanvas.addImage(imageData, imageSize, false);
            } else {
                List<Float> coordinates =
                        PdfCreatorUtil.calculateImageCoordinates(
                        ocrPdfCreatorProperties.getPageSize(), imageSize);
                com.itextpdf.kernel.geom.Rectangle rect =
                        new com.itextpdf.kernel.geom.Rectangle(
                                coordinates.get(0), coordinates.get(1),
                                imageSize.getWidth(), imageSize.getHeight());
                pdfCanvas.addImage(imageData, rect, false);
            }
        }
    }

    /**
     * Places retrieved text to canvas to a separate layer.
     *
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param pdfCanvas canvas to place the text
     * @param font font for the placed text (could be custom or default)
     * @param multiplier coefficient to adjust text placing on canvas
     * @param pageMediaBox page parameters
     */
    private void addTextToCanvas(
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> pageText,
            final PdfCanvas pdfCanvas,
            final PdfFont font,
            final float multiplier,
            final com.itextpdf.kernel.geom.Rectangle pageMediaBox) {
        if (pageText == null || pageText.size() == 0) {
            pdfCanvas.beginText().setFontAndSize(font, 1);
        } else {
            List<Float> imageCoordinates =
                    PdfCreatorUtil.calculateImageCoordinates(
                    ocrPdfCreatorProperties.getPageSize(), imageSize);
            float x = imageCoordinates.get(0);
            float y = imageCoordinates.get(1);
            for (TextInfo item : pageText) {
                String line = item.getText();
                List<Float> coordinates = item.getBbox();
                final float left = coordinates.get(0) * multiplier;
                final float right = (coordinates.get(2) + 1) * multiplier - 1;
                final float top = coordinates.get(1) * multiplier;
                final float bottom = (coordinates.get(3) + 1) * multiplier - 1;

                float bboxWidthPt = PdfCreatorUtil.getPoints(right - left);
                float bboxHeightPt = PdfCreatorUtil.getPoints(bottom - top);
                if (!line.isEmpty() && bboxHeightPt > 0 && bboxWidthPt > 0) {
                    // Scale the text width to fit the OCR bbox
                    float fontSize = PdfCreatorUtil.calculateFontSize(
                            new Document(pdfCanvas.getDocument()),
                            line, font, bboxHeightPt, bboxWidthPt);
                    float lineWidth = font.getWidth(line, fontSize);

                    float deltaX = PdfCreatorUtil.getPoints(left);
                    float deltaY = imageSize.getHeight()
                            - PdfCreatorUtil.getPoints(bottom);

                    float descent = font.getDescent(line, fontSize);

                    Canvas canvas = new Canvas(pdfCanvas, pageMediaBox);

                    Text text = new Text(line)
                            .setHorizontalScaling(bboxWidthPt / lineWidth);

                    Paragraph paragraph = new Paragraph(text)
                            .setMargin(0)
                            .setMultipliedLeading(1.2f);
                    paragraph.setFont(font)
                            .setFontSize(fontSize);
                    paragraph.setWidth(bboxWidthPt * 1.5f);

                    if (ocrPdfCreatorProperties.getTextColor() != null) {
                        paragraph.setFontColor(
                                ocrPdfCreatorProperties.getTextColor());
                    } else {
                        paragraph.setTextRenderingMode(
                                TextRenderingMode.INVISIBLE);
                    }

                    canvas.showTextAligned(paragraph, deltaX + x,
                            deltaY + y, TextAlignment.LEFT);
                    canvas.close();
                }
            }
        }
    }
}
