package com.itextpdf.ocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.io.util.ResourceUtil;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
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
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.ocr.IOcrReader.OutputFormat;
import com.itextpdf.pdfa.PdfADocument;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PdfRenderer} is the class that creates Pdf documents containing input
 * images and text that was recognized using provided {@link IOcrReader}.
 *
 * {@link PdfRenderer} provides possibilities to set list of input images to
 * be used for OCR, to set scaling mode for images, to set color of text in
 * output PDF document, to set fixed size of the PDF document's page and to
 * perform OCR using given images and to return
 * {@link com.itextpdf.kernel.pdf.PdfDocument} as result.
 * PDFRenderer's OCR is based on the provided {@link IOcrReader}
 * (e.g. tesseract reader). This parameter is obligatory and it should be
 * provided in constructor
 * or using setter.
 */
public class PdfRenderer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfRenderer.class);

    /**
     * Path to default font file.
     * "LiberationSans-Regular" by default.
     */
    private static final String DEFAULT_FONT_NAME =
            "LiberationSans-Regular.ttf";

    /**
     * Selected {@link IOcrReader}.
     */
    private IOcrReader ocrReader;

    /**
     * Set of properties.
     */
    private OcrPdfCreatorProperties ocrPdfCreatorProperties;

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param ocrReader {@link IOcrReader} selected OCR Reader
     */
    public PdfRenderer(final IOcrReader ocrReader) {
        setOcrReader(ocrReader);
        setOcrPdfCreatorProperties(new OcrPdfCreatorProperties());
    }

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param ocrReader selected OCR Reader {@link IOcrReader}
     * @param ocrPdfCreatorProperties set of properties for {@link PdfRenderer}
     */
    public PdfRenderer(final IOcrReader ocrReader,
            final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        setOcrReader(ocrReader);
        setOcrPdfCreatorProperties(ocrPdfCreatorProperties);
    }

    /**
     * Gets properties for {@link PdfRenderer}.
     *
     * @return set properties {@link OcrPdfCreatorProperties}
     */
    public final OcrPdfCreatorProperties getOcrPdfCreatorProperties() {
        return ocrPdfCreatorProperties;
    }

    /**
     * Sets properties for {@link PdfRenderer}.
     *
     * @param ocrPdfCreatorProperties set of properties
     * {@link OcrPdfCreatorProperties} for {@link PdfRenderer}
     */
    public final void setOcrPdfCreatorProperties(
            final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        this.ocrPdfCreatorProperties = ocrPdfCreatorProperties;
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrReader} and
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
                    ocrReader.doImageOcr(inputImage));
        }

        // create PdfDocument
        return createPdfDocument(pdfWriter, pdfOutputIntent, imagesTextData);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrReader} and
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
     * Performs OCR using provided {@link IOcrReader} for the given list of
     * input images and saves output to a text file using provided path.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param path path as {@link java.lang.String} to file to be
     *                     created
     */
    public void createTxt(final List<File> inputImages, final String path) {
        LOGGER.info(MessageFormatUtil.format(
                LogMessageConstant.StartOcrForImages,
                inputImages.size()));

        StringBuilder content = new StringBuilder();
        for (File inputImage : inputImages) {
            content.append(ocrReader.doImageOcr(inputImage, OutputFormat.TXT));
        }

        // write to file
        writeToTextFile(path, content.toString());
    }

    /**
     * Gets path to the default font.
     *
     * @return {@link java.lang.String} path to default font
     */
    public String getDefaultFontName() {
        return TesseractUtil.FONT_RESOURCE_PATH + DEFAULT_FONT_NAME;
    }

    /**
     * Gets used {@link IOcrReader}.
     *
     * Returns {@link IOcrReader} reader object to perform OCR.
     * @return selected {@link IOcrReader} instance
     */
    public final IOcrReader getOcrReader() {
        return ocrReader;
    }

    /**
     * Sets {@link IOcrReader} reader object to perform OCR.
     * @param reader selected {@link IOcrReader} instance
     */
    public final void setOcrReader(final IOcrReader reader) {
        ocrReader = reader;
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
                .getResourceStream(getDefaultFontName())) {
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
                / UtilService.getPoints(imageData.getWidth());
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
     * Writes provided {@link java.lang.String} to text file using
     * provided path.
     *
     * @param path path as {@link java.lang.String} to file to be created
     * @param data text data in required format as {@link java.lang.String}
     */
    private void writeToTextFile(final String path,
            final String data) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path),
                StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    LogMessageConstant.CannotWriteToFile,
                    path,
                    e.getMessage()));
        }
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
                List<ImageData> imageDataList = getImageData(inputImage);
                LOGGER.info(MessageFormatUtil.format(
                        LogMessageConstant.NumberOfPagesInImage,
                        inputImage.toString(), imageDataList.size()));

                Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
                if (imageTextData.keySet().size() > 0) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        com.itextpdf.kernel.geom.Rectangle imageSize =
                                UtilService.calculateImageSize(
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
     * Retrieves {@link com.itextpdf.io.image.ImageData} from the
     * input {@link java.io.File}.
     *
     * @param inputImage input image as {@link java.io.File}
     * @return list of {@link com.itextpdf.io.image.ImageData} objects
     * (more than one element in the list if it is a multipage tiff)
     * @throws OcrException if error occurred during reading a file
     * @throws IOException if error occurred during reading a file
     */
    private List<ImageData> getImageData(final File inputImage)
            throws OcrException, IOException {
        List<ImageData> images = new ArrayList<ImageData>();

        String ext = "";
        int index = inputImage.getAbsolutePath().lastIndexOf('.');
        if (index > 0) {
            ext = new String(inputImage.getAbsolutePath().toCharArray(),
                    index + 1,
                    inputImage.getAbsolutePath().length() - index - 1);

            if ("tiff".equals(ext.toLowerCase())
                    || "tif".equals(ext.toLowerCase())) {
                int tiffPages = ImageUtil.getNumberOfPageTiff(inputImage);

                for (int page = 0; page < tiffPages; page++) {
                    byte[] bytes = Files.readAllBytes(inputImage.toPath());
                    ImageData imageData = ImageDataFactory
                            .createTiff(bytes, true,
                                    page + 1, true);
                    images.add(imageData);
                }
            } else {
                try {
                    ImageData imageData = ImageDataFactory
                            .create(inputImage.getAbsolutePath());
                    images.add(imageData);
                } catch (com.itextpdf.io.IOException e) {
                    LOGGER.info(MessageFormatUtil.format(
                            LogMessageConstant.AttemptToConvertToPng,
                            inputImage.getAbsolutePath(),
                            e.getMessage()));
                    try {
                        BufferedImage bufferedImage = null;
                        try {
                            bufferedImage = ImageUtil
                                    .readImageFromFile(inputImage);
                        } catch (IllegalArgumentException | IOException ex) {
                            LOGGER.info(MessageFormatUtil.format(
                                    LogMessageConstant.ReadingImageAsPix,
                                    inputImage.getAbsolutePath(),
                                    ex.getMessage()));
                            bufferedImage = ImageUtil
                                    .readAsPixAndConvertToBufferedImage(
                                            inputImage);
                        }
                        ByteArrayOutputStream baos =
                                new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage,
                                TesseractUtil.getPngImageFormat(), baos);
                        ImageData imageData = ImageDataFactory
                                .create(baos.toByteArray());
                        images.add(imageData);
                    } catch (com.itextpdf.io.IOException | IOException
                            | IllegalArgumentException ex) {
                        LOGGER.error(MessageFormatUtil.format(
                                LogMessageConstant.CannotReadInputImage,
                                ex.getMessage()));
                        throw new OcrException(
                                OcrException.CannotReadInputImage);
                    }
                }
            }
        }
        return images;
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
                List<Float> coordinates = calculateImageCoordinates(
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
            List<Float> imageCoordinates = calculateImageCoordinates(
                    ocrPdfCreatorProperties.getPageSize(), imageSize);
            float x = imageCoordinates.get(0);
            float y = imageCoordinates.get(1);
            for (TextInfo item : pageText) {
                String line = item.getText();
                List<Float> coordinates = item.getCoordinates();
                final float left = coordinates.get(0) * multiplier;
                final float right = (coordinates.get(2) + 1) * multiplier - 1;
                final float top = coordinates.get(1) * multiplier;
                final float bottom = (coordinates.get(3) + 1) * multiplier - 1;

                float bboxWidthPt = UtilService
                        .getPoints(right - left);
                float bboxHeightPt = UtilService
                        .getPoints(bottom - top);
                if (!line.isEmpty() && bboxHeightPt > 0 && bboxWidthPt > 0) {
                    // Scale the text width to fit the OCR bbox
                    float fontSize = calculateFontSize(
                            new Document(pdfCanvas.getDocument()),
                            line, font, bboxHeightPt, bboxWidthPt);
                    float lineWidth = font.getWidth(line, fontSize);

                    float deltaX = UtilService.getPoints(left);
                    float deltaY = imageSize.getHeight() - UtilService
                            .getPoints(bottom);

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

    /**
     * Calculates font size according to given bbox height, width and selected
     * font.
     *
     * @param document pdf document as a {@link com.itextpdf.layout.Document}
     *                object
     * @param line text line
     * @param font font for the placed text (could be custom or default)
     * @param bboxHeightPt height of bbox calculated by OCR Reader
     * @param bboxWidthPt width of bbox calculated by OCR Reader
     * @return font size
     */
    private float calculateFontSize(Document document, final String line,
            final PdfFont font,
            final float bboxHeightPt, final float bboxWidthPt) {
        Rectangle bbox = new Rectangle(bboxWidthPt * 1.5f,
                bboxHeightPt * 1.5f);
        Paragraph paragraph = new Paragraph(line);
        paragraph.setWidth(bboxWidthPt);
        paragraph.setFont(font);

        // setting minimum and maximum (approx.) values for font size
        float fontSize = 1;
        float maxFontSize = bboxHeightPt * 2;

        while (Math.abs(fontSize - maxFontSize) > 1e-1) {
            float curFontSize = (fontSize + maxFontSize) / 2;
            paragraph.setFontSize(curFontSize);
            IRenderer renderer = paragraph.createRendererSubTree()
                    .setParent(document.getRenderer());
            LayoutContext context = new LayoutContext(
                    new LayoutArea(1, bbox));
            if (renderer.layout(context).getStatus() == LayoutResult.FULL) {
                fontSize = curFontSize;
            } else {
                maxFontSize = curFontSize;
            }
        }
        return fontSize;
    }

    /**
     * Calculates image coordinates on the page.
     *
     * @param size size of the page
     * @param imageSize size of the image
     * @return list of two elements (coordinates): first - x, second - y.
     */
    private List<Float> calculateImageCoordinates(
            final com.itextpdf.kernel.geom.Rectangle size,
            final com.itextpdf.kernel.geom.Rectangle imageSize) {
        float x = 0;
        float y = 0;
        if (ocrPdfCreatorProperties.getPageSize() != null) {
            if (imageSize.getHeight() < size.getHeight()) {
                y = (size.getHeight() - imageSize.getHeight()) / 2;
            }
            if (imageSize.getWidth() < size.getWidth()) {
                x = (size.getWidth() - imageSize.getWidth()) / 2;
            }
        }
        return Arrays.<Float>asList(x, y);
    }
}
