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
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.BaseDirection;
import com.itextpdf.layout.property.TextAlignment;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link IPdfRenderer}.
 *
 * {@link IPdfRenderer} provides possibilities to set list of input images to
 * be used for OCR, to set scaling mode for images, to set color of text in
 * output PDF document, to set fixed size of the PDF document's page and to
 * perform OCR using given images and to return
 * {@link com.itextpdf.kernel.pdf.PdfDocument} as result.
 * PDFRenderer's OCR is based on the provided {@link IOcrReader}
 * (e.g. tesseract reader). This parameter is obligatory and it should be
 * provided in constructor
 * or using setter.
 */
public class PdfRenderer implements IPdfRenderer {

    /**
     * Supported image formats.
     */
    private static final Set<String> SUPPORTED_IMAGE_FORMATS =
            Collections.unmodifiableSet(new HashSet<>(
                    Arrays.<String>asList("bmp", "png", "pnm", "pgm",
                            "ppm", "pbm", "tiff", "tif", "jpeg",
                            "jpg", "jpe", "jfif")));

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
     * List of Files with input images.
     */
    private List<File> inputImages = Collections.<File>emptyList();

    /**
     * Color of the text in the output PDF document.
     * Text will be transparent by default.
     */
    private com.itextpdf.kernel.colors.Color textColor = null;

    /**
     * Scale mode for input images.
     * {@link IPdfRenderer.ScaleMode#SCALE_TO_FIT} by default.
     */
    private ScaleMode scaleMode = ScaleMode.SCALE_TO_FIT;

    /**
     * Size of the PDF document pages.
     * "A4" by default.
     * This parameter is taken into account only if
     * {@link IPdfRenderer.ScaleMode} is
     * {@link IPdfRenderer.ScaleMode#SCALE_WIDTH},
     * {@link IPdfRenderer.ScaleMode#SCALE_HEIGHT} or
     * {@link IPdfRenderer.ScaleMode#SCALE_TO_FIT}
     */
    private com.itextpdf.kernel.geom.Rectangle pageSize =
            new com.itextpdf.kernel.geom.Rectangle(PageSize.A4.getX(),
            PageSize.A4.getY(), PageSize.A4.getWidth(),
            PageSize.A4.getHeight());

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
     * Selected {@link IOcrReader}.
     */
    private IOcrReader ocrReader;

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param reader {@link IOcrReader} selected OCR Reader
     */
    public PdfRenderer(final IOcrReader reader) {
        ocrReader = reader;
    }

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param reader selected OCR Reader {@link IOcrReader}
     * @param images {@link java.util.List} of images to be OCRed
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images) {
        ocrReader = reader;
        inputImages = Collections.<File>unmodifiableList(images);
    }

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param reader selected OCR Reader {@link IOcrReader}
     * @param images {@link java.util.List} of images to be OCRed
     * @param mode   {@link IPdfRenderer.ScaleMode}
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
            final ScaleMode mode) {
        ocrReader = reader;
        inputImages = Collections.<File>unmodifiableList(images);
        scaleMode = mode;
    }

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param reader selected OCR Reader {@link IOcrReader}
     * @param images {@link java.util.List} of images to be OCRed
     * @param color selected text {@link com.itextpdf.kernel.colors.Color}
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
            final com.itextpdf.kernel.colors.Color color) {
        ocrReader = reader;
        inputImages = Collections.<File>unmodifiableList(images);
        textColor = color;
        scaleMode = ScaleMode.KEEP_ORIGINAL_SIZE;
    }

    /**
     * Creates a new {@link PdfRenderer} instance.
     *
     * @param reader selected OCR Reader {@link IOcrReader}
     * @param images {@link java.util.List} of images to be OCRed
     * @param color selected text {@link com.itextpdf.kernel.colors.Color}
     * @param mode {@link IPdfRenderer.ScaleMode} for input images
     *                                           and pdf pages
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
            final com.itextpdf.kernel.colors.Color color,
            final ScaleMode mode) {
        ocrReader = reader;
        textColor = color;
        inputImages = Collections.<File>unmodifiableList(images);
        scaleMode = mode;
    }

    /**
     * Gets list of provided input images for OCR.
     *
     * @return {@link java.util.List} of images to be OCRed
     */
    public final List<File> getInputImages() {
        return new ArrayList<File>(inputImages);
    }

    /**
     * Sets list of input images for OCR.
     *
     * @param images {@link java.util.List} of images to be OCRed
     */
    public void setInputImages(final List<File> images) {
        inputImages = Collections.<File>unmodifiableList(images);
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
     */
    public final void setTextColor(
            final com.itextpdf.kernel.colors.Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Gets scale mode for input images.
     *
     * @return selected {@link IPdfRenderer.ScaleMode}
     */
    public final ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * Sets scale mode for input images using available options
     * from {@link com.itextpdf.ocr.IPdfRenderer.ScaleMode} enumeration.
     *
     * @param scaleMode selected {@link IPdfRenderer.ScaleMode}
     */
    public final void setScaleMode(final ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
    }

    /**
     * Gets required size for output PDF document. Real size of the page will
     * be calculates according to the selected {@link IPdfRenderer.ScaleMode}
     *
     * @return required page size as {@link com.itextpdf.kernel.geom.Rectangle}
     */
    public final com.itextpdf.kernel.geom.Rectangle getPageSize() {
        return pageSize;
    }

    /**
     * Sets required size for output PDF document. Real size of the page will be
     * calculates according to the selected {@link IPdfRenderer.ScaleMode}.
     *
     * @param pageSize required page
     *                size as {@link com.itextpdf.kernel.geom.Rectangle}
     */
    public final void setPageSize(
            final com.itextpdf.kernel.geom.Rectangle pageSize) {
        this.pageSize = pageSize;
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
     */
    public final void setImageLayerName(final String layerName) {
        imageLayerName = layerName;
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
     */
    public final void setTextLayerName(final String layerName) {
        textLayerName = layerName;
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
     */
    public final void setPdfLang(final String language) {
        pdfLang = language;
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
     */
    public final void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Returns path to font to be used in pdf document.
     * @return path to the required font
     */
    public String getFontPath() {
        return fontPath;
    }

    /**
     * Sets path to font to be used in pdf document.
     * @param path path to the required font
     */
    public void setFontPath(final String path) {
        fontPath = path;
    }

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
    public final PdfDocument doPdfOcr(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent)
            throws OcrException {
        LOGGER.info(MessageFormatUtil.format(
                LogMessageConstant.StartOcrForImages,
                getInputImages().size()));

        // map contains:
        // keys: image files
        // values:
        // map pageNumber -> retrieved text data(text and its coordinates)
        Map<File, Map<Integer, List<TextInfo>>> imagesTextData =
                new LinkedHashMap<File, Map<Integer, List<TextInfo>>>();
        for (File inputImage : getInputImages()) {
            imagesTextData.put(inputImage,
                    doOcrForImage(inputImage));
        }

        // create PdfDocument
        return createPdfDocument(pdfWriter, pdfOutputIntent, imagesTextData);
    }

    /**
     * Performs OCR using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final pdf document to
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     * @throws OcrException if provided font is incorrect
     */
    public final PdfDocument doPdfOcr(final PdfWriter pdfWriter)
            throws OcrException {
        return doPdfOcr(pdfWriter, null);
    }

    /**
     * Performs OCR for the given list of input images and saves output to a
     * text file using provided path.
     *
     * @param path path as {@link java.lang.String} to file to be
     *                     created
     */
    public void doPdfOcr(final String path) {
        LOGGER.info(MessageFormatUtil.format(
                LogMessageConstant.StartOcrForImages,
                getInputImages().size()));

        StringBuilder content = new StringBuilder();
        for (File inputImage : getInputImages()) {
            content.append(doOcrForImage(inputImage, OutputFormat.TXT));
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
    public final byte[] getFont() {
        if (fontPath != null && !fontPath.isEmpty()) {
            try {
                return Files.readAllBytes(java.nio.file.Paths.get(fontPath));
            } catch (IOException | OutOfMemoryError e) {
                LOGGER.error(MessageFormatUtil.format
                        (LogMessageConstant.CannotReadProvidedFont,
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
    public final byte[] getDefaultFont() {
        try (InputStream stream = ResourceUtil
                .getResourceStream(getDefaultFontName())) {
            return StreamUtil.inputStreamToArray(stream);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format
                    (LogMessageConstant.CannotReadDefaultFont,
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
     *                  {@link IPdfRenderer.ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param imageData input image if it is a single page or its one page if
     *                 this is a multi-page image
     */
    void addToCanvas(final PdfDocument pdfDocument, final PdfFont font,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> pageText, final ImageData imageData) {
        com.itextpdf.kernel.geom.Rectangle rectangleSize =
                getScaleMode() == ScaleMode.KEEP_ORIGINAL_SIZE
                ? imageSize : getPageSize();
        PageSize size = new PageSize(rectangleSize);
        PdfPage pdfPage = pdfDocument.addNewPage(size);
        PdfCanvas canvas = new PdfCanvas(pdfPage);

        PdfLayer imageLayer = new PdfLayer(getImageLayerName(), pdfDocument);
        PdfLayer textLayer = new PdfLayer(getTextLayerName(), pdfDocument);

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
        pdfDocument.getCatalog().setLang(new PdfString(getPdfLang()));
        pdfDocument.getCatalog().setViewerPreferences(
                new PdfViewerPreferences().setDisplayDocTitle(true));
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
        info.setTitle(getTitle());

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
     * Writes provided {@link java.lang.String} to text file using provided path.
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
     * Reads data from input image using selected output format if provided
     * image has valid extension.
     *
     * @param inputImage input image {@link java.io.File}
     * @param outputFormat {@link IOcrReader.OutputFormat} for the result
     *                     returned by {@link IOcrReader}
     * @return result text data as {@link java.lang.String}
     */
    private String doOcrForImage(final File inputImage,
            final OutputFormat outputFormat) {
        String data = null;
        if (isValidImageFormat(inputImage)) {
            data = ocrReader.readDataFromInput(inputImage, outputFormat);
        }
        return data;
    }

    /**
     * Reads data from input image using @link IOcrReader.OutputFormat#HOCR}
     * output format if provided image has valid extension.
     *
     * @param inputImage input image {@link java.io.File}
     * @return result text data as {@link java.lang.String}
     */
    private Map<Integer, List<TextInfo>> doOcrForImage(
            final File inputImage) {
        Map<Integer, List<TextInfo>> data = new LinkedHashMap<Integer, List<TextInfo>>();
        if (isValidImageFormat(inputImage)) {
            data = ocrReader.readDataFromInput(inputImage);
        }
        return data;
    }

    /**
     * Validates input image format.
     * Allowed image formats are listed
     * in {@link PdfRenderer#SUPPORTED_IMAGE_FORMATS}
     *
     * @param image input image {@link java.io.File}
     * @return true if image extension is valid, false - if not
     */
    private boolean isValidImageFormat(final File image) {
        boolean isValid = false;
        String extension = "incorrect extension";
        int index = image.getAbsolutePath().lastIndexOf('.');
        if (index > 0) {
            extension = new String(image.getAbsolutePath().toCharArray(),
                    index + 1,
                    image.getAbsolutePath().length() - index - 1);
            for (String format : SUPPORTED_IMAGE_FORMATS) {
                if (format.equals(extension.toLowerCase())) {
                    isValid = true;
                    break;
                }
            }
        }
        if (!isValid) {
            LOGGER.error(MessageFormatUtil
                    .format(LogMessageConstant.CannotReadInputImage,
                            image.getAbsolutePath()));
            throw new OcrException(OcrException.IncorrectInputImageFormat)
                    .setMessageParams(extension);
        }
        return isValid;
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
                LOGGER.info(MessageFormatUtil.format
                        (LogMessageConstant.NumberOfPagesInImage,
                                inputImage.toString(),
                                imageDataList.size()));

                Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
                if (imageTextData.keySet().size() > 0) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        com.itextpdf.kernel.geom.Rectangle imageSize =
                                UtilService.calculateImageSize(
                                        imageData, getScaleMode(),
                                        getPageSize());

                        addToCanvas(pdfDocument, font, imageSize,
                                imageTextData.get(page + 1),
                                imageData);
                    }
                } else {
                    ImageData imageData = imageDataList.get(0);
                    com.itextpdf.kernel.geom.Rectangle imageSize = UtilService
                            .calculateImageSize(imageData, getScaleMode(),
                                    getPageSize());
                    addToCanvas(pdfDocument, font, imageSize,
                            new ArrayList<TextInfo>(), imageData);
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
     *                  {@link IPdfRenderer.ScaleMode}
     * @param pdfCanvas canvas to place the image
     */
    private void addImageToCanvas(final ImageData imageData,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final PdfCanvas pdfCanvas) {
        if (imageData != null) {
            if (getScaleMode() == ScaleMode.KEEP_ORIGINAL_SIZE) {
                pdfCanvas.addImage(imageData, imageSize, false);
            } else {
                List<Float> coordinates = calculateImageCoordinates(
                        getPageSize(), imageSize, getScaleMode());
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
     *                  {@link IPdfRenderer.ScaleMode}
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
            pdfCanvas.showText("").endText();
        } else {
            List<Float> imageCoordinates = calculateImageCoordinates(
                    getPageSize(), imageSize, getScaleMode());
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
                    float fontSize = calculateFontSize(line, font,
                            bboxHeightPt);
                    float lineWidth = font.getWidth(line, fontSize);

                    float deltaX = UtilService.getPoints(left);
                    float deltaY = imageSize.getHeight() - UtilService
                            .getPoints(bottom);

                    float descent = font.getDescent(line, fontSize);

                    Canvas canvas = new Canvas(pdfCanvas, pageMediaBox);

                    Text text = new Text(line)
                            .setHorizontalScaling(bboxWidthPt / lineWidth)
                            .setBaseDirection(BaseDirection.LEFT_TO_RIGHT);

                    Paragraph paragraph = new Paragraph(text)
                            .setMargin(0)
                            .setMultipliedLeading(1);
                    paragraph.setFont(font)
                            .setFontSize(fontSize);
                    paragraph.setWidth(bboxWidthPt * 1.5f);

                    if (getTextColor() != null) {
                        paragraph.setFontColor(getTextColor());
                    } else {
                        paragraph.setOpacity(0.0f);
                    }

                    canvas.showTextAligned(paragraph, deltaX + x,
                            deltaY + y + descent, TextAlignment.LEFT);
                    canvas.close();
                }
            }
        }
    }

    /**
     * Calculates font size according to given bbox height and selected font
     * parameters.
     *
     * @param line text line
     * @param font font for the placed text (could be custom or default)
     * @param bboxHeightPt height of bbox calculated by OCR Reader
     * @return font size
     */
    private float calculateFontSize(final String line,
            final PdfFont font,
            final float bboxHeightPt) {
        float fontSize = bboxHeightPt;
        boolean textScaled = false;

        float realTextSize;
        while (!textScaled) {
            float ascent = font.getAscent(line, fontSize);
            float descent = font.getDescent(line, fontSize);
            realTextSize = ascent - descent;
            if (realTextSize - bboxHeightPt <= 0.5) {
                textScaled = true;
            } else if (realTextSize - bboxHeightPt < 0.5) {
                fontSize += 0.5f;
            } else {
                fontSize -= 0.5f;
            }
        }
        return fontSize;
    }

    /**
     * Calculates image coordinates on the page.
     *
     * @param size size of the page
     * @param imageSize size of the image
     * @param pageScaleMode selected {@link IPdfRenderer.ScaleMode}
     * @return list of two elements (coordinates): first - x, second - y.
     */
    private List<Float> calculateImageCoordinates(
            final com.itextpdf.kernel.geom.Rectangle size,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final ScaleMode pageScaleMode) {
        float x = 0;
        float y = 0;
        if (pageScaleMode != ScaleMode.KEEP_ORIGINAL_SIZE) {
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
