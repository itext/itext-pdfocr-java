package com.itextpdf.ocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PDF Renderer class.
 * <p>
 * The IPdfRenderer provides possibilities to set list of input images
 * to be used for OCR, to set scaling mode for images, color of text in
 * the output PDF document, set fixed size of the PDF document
 * and to perform OCR using given images and return PDFDocument as result
 * <p>
 * PDFRenderer's ocr is based on the provided IOcrReader (e.g. tesseract).
 * This parameter is obligatory and it should be provided in constructor
 * or using setter
 */
public class PdfRenderer implements IPdfRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfRenderer.class);

    /**
     * Path to default font file (LiberationSans-Regular).
     */
    private String defaultFontName = "LiberationSans-Regular.ttf";

    /**
     * List of Files with input images.
     */
    private List<File> inputImages = Collections.<File>emptyList();

    /**
     * CMYK color of the text in the output PDF document.
     * Text will be transparent by default
     */
    private com.itextpdf.kernel.colors.Color textColor = null;

    /**
     * Scale mode for input images: "scaleToFit" by default.
     */
    private ScaleMode scaleMode = ScaleMode.scaleToFit;

    /**
     * Size of the PDF document pages: "A4" by default.
     * This parameter is taken into account only if "scaleMode" is scaleWidth,
     * scaleHeight or scaleToFit
     */
    private com.itextpdf.kernel.geom.Rectangle pageSize =
            new com.itextpdf.kernel.geom.Rectangle(PageSize.A4.getX(),
            PageSize.A4.getY(), PageSize.A4.getWidth(),
            PageSize.A4.getHeight());

    /**
     * Name of the image layer.
     */
    private String imageLayerName = "Image Layer";

    /**
     * Name of the text layer.
     */
    private String textLayerName = "Text Layer";

    /**
     * Pdf Language (default: "en-US").
     */
    private String pdfLang = "en-US";

    /**
     * Title of the created document (empty by default).
     */
    private String title = "";

    /**
     * Path to font.
     * (should be set explicitly or default font will be used)
     */
    private String fontPath;

    /**
     * Parameter describing selectedOCR reader
     * that corresponds IOcrReader interface.
     */
    private IOcrReader ocrReader;

    /**
     * PdfRenderer constructor with IOcrReader.
     *
     * @param reader IOcrReader
     */
    public PdfRenderer(final IOcrReader reader) {
        ocrReader = reader;
    }

    /**
     * PdfRenderer constructor with IOcrReader and list of input files.
     *
     * @param reader IOcrReader
     * @param images images
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images) {
        ocrReader = reader;
        inputImages = Collections.<File>unmodifiableList(images);
    }

    /**
     * PdfRenderer constructor with IOcrReader, list of input files
     * and scale mode.
     *
     * @param reader IOcrReader
     * @param images List<File>
     * @param mode   ScaleMode
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
            final ScaleMode mode) {
        ocrReader = reader;
        inputImages = Collections.<File>unmodifiableList(images);
        scaleMode = mode;
    }

    /**
     * PdfRenderer constructor with IOcrReader, list of input files
     * and text color.
     *
     * @param reader   IOcrReader
     * @param images   List<File>
     * @param newColor Color
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
            final com.itextpdf.kernel.colors.Color newColor) {
        ocrReader = reader;
        inputImages = Collections.<File>unmodifiableList(images);
        textColor = newColor;
        scaleMode = ScaleMode.keepOriginalSize;
    }

    /**
     * PdfRenderer constructor with IOcrReader, list of input files,
     * text color and scale mode.
     *
     * @param reader   IOcrReader
     * @param images   List<File>
     * @param newColor Color
     * @param mode     ScaleMode
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
            final com.itextpdf.kernel.colors.Color newColor,
            final ScaleMode mode) {
        ocrReader = reader;
        textColor = newColor;
        inputImages = Collections.<File>unmodifiableList(images);
        scaleMode = mode;
    }

    /**
     * Set list of input images for OCR.
     *
     * @param images List<File>
     */
    public void setInputImages(final List<File> images) {
        inputImages = Collections.<File>unmodifiableList(images);
    }

    /**
     * Get list of provided input images for OCR.
     *
     * @return List<File>
     */
    public final List<File> getInputImages() {
        return new ArrayList<File>(inputImages);
    }

    /**
     * Set text color (should be CMYK) in output PDF document.
     *
     * @param newColor CMYK Color
     */
    public final void setTextColor(
            final com.itextpdf.kernel.colors.Color newColor) {
        textColor = newColor;
    }

    /**
     * Get text color in output PDF document.
     *
     * @return Color
     */
    public final com.itextpdf.kernel.colors.Color getTextColor() {
        return textColor;
    }

    /**
     * Set scale mode for input images using available options
     * from ScaleMode enum.
     *
     * @param mode ScaleMode
     */
    public final void setScaleMode(final ScaleMode mode) {
        scaleMode = mode;
    }

    /**
     * Get scale mode for input images.
     *
     * @return ScaleMode
     */
    public final ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * Set fixed size for output PDF document.
     * (this parameter is used only is ScaleMode is set as "fitToSize")
     *
     * @param size pageSize
     */
    public final void setPageSize(
            final com.itextpdf.kernel.geom.Rectangle size) {
        pageSize = size;
    }

    /**
     * Get size for output document.
     *
     * @return Rectangle
     */
    public final com.itextpdf.kernel.geom.Rectangle getPageSize() {
        return pageSize;
    }

    /**
     * Set name for the image layer.
     * (of by default it is "Image layer")
     *
     * @param name layer's name
     */
    public final void setImageLayerName(final String name) {
        imageLayerName = name;
    }

    /**
     * Get name of image layer.
     *
     * @return layer's name that was manually set
     * or the default one (="Image layer")
     */
    public final String getImageLayerName() {
        return imageLayerName;
    }

    /**
     * Set name for the text layer.
     * (of by default it is "Text layer")
     *
     * @param name layer's name
     */
    public final void setTextLayerName(final String name) {
        textLayerName = name;
    }

    /**
     * @return layer's name that was manually set
     * or the default one (="Text layer")
     */
    public final String getTextLayerName() {
        return textLayerName;
    }

    /**
     * Specify pdf natural language, and optionally locale.
     *
     * @param lang String
     */
    public final void setPdfLang(final String lang) {
        pdfLang = lang;
    }

    /**
     * @return pdf document lang
     */
    public final String getPdfLang() {
        return pdfLang;
    }

    /**
     * Set pdf document title.
     *
     * @param name String
     */
    public final void setTitle(final String name) {
        title = name;
    }

    /**
     * @return pdf document title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Set path to font to be used in pdf document.
     * (if it isn't set default font will be used)
     *
     * @param path Path
     */
    public void setFontPath(final String path) {
        fontPath = path;
    }

    /**
     * @return Font path that was set or default font path
     */
    public String getFontPath() {
        return fontPath;
    }

    /**
     * @return path to default font
     */
    public String getDefaultFontName() {
        return TesseractUtil.FONT_RESOURCE_PATH + defaultFontName;
    }

    /**
     * Set IOcrReader reader (e.g. TesseractReader object).
     *
     * @param reader IOcrReader
     */
    public final void setOcrReader(final IOcrReader reader) {
        ocrReader = reader;
    }

    /**
     * Get used ocr reader.
     *
     * @return IOcrReader
     */
    public final IOcrReader getOcrReader() {
        return ocrReader;
    }

    /**
     * Get byte array sing provided font path or the default one.
     *
     * @return byte[]
     */
    public final byte[] getFont() {
        if (fontPath != null && !fontPath.isEmpty()) {
            try {
                return Files.readAllBytes(java.nio.file.Paths.get(fontPath));
            } catch (IOException | OutOfMemoryError e) {
                LOGGER.error("Cannot load provided font: " + e.getMessage());
                return getDefaultFont();
            }
        } else {
            return getDefaultFont();
        }
    }

    /**
     * Get byte array using default font path.
     *
     * @return byte[]
     */
    public final byte[] getDefaultFont() {
        try (InputStream stream = ResourceUtil
                .getResourceStream(getDefaultFontName())) {
            return StreamUtil.inputStreamToArray(stream);
        } catch (IOException e) {
            LOGGER.error("Cannot load default font: " + e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Perform OCR for the given list of input images and
     * save output to a text file with provided path.
     *
     * @param absolutePath String
     */
    public void doPdfOcr(final String absolutePath) {
        LOGGER.info("Starting ocr for " + getInputImages().size()
                + " image(s)");

        StringBuilder content = new StringBuilder();
        for (File inputImage : getInputImages()) {
            content.append(doOCRForImages(inputImage, OutputFormat.txt));
        }

        // write to file
        writeToTextFile(absolutePath, content.toString());
    }

    /**
     * Perform OCR for the given list of input images using provided pdfWriter.
     *
     * @param pdfWriter PdfWriter
     * @return PdfDocument
     * @throws OCRException if provided font is incorrect
     */
    public final PdfDocument doPdfOcr(final PdfWriter pdfWriter)
            throws OCRException {
        return doPdfOcr(pdfWriter, null);
    }

    /**
     * Perform OCR for the given list of input images using provided pdfWriter.
     * PDF/A-3u document will be created if pdfOutputIntent is not null
     *
     * @param pdfWriter PdfWriter
     * @param pdfOutputIntent PdfOutputIntent
     * @return PDF/A-3u document if pdfOutputIntent is not null
     * @throws OCRException if provided font or output intent is incorrect
     */
    public final PdfDocument doPdfOcr(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent)
            throws OCRException {
        LOGGER.info("Starting ocr for " + getInputImages().size()
                + " image(s)");

        // map contains:
        // keys: image files
        // values: map pageNumber -> retrieved text data(text and its coordinates)
        Map<File, Map<Integer, List<TextInfo>>> imagesTextData =
                new LinkedHashMap<File, Map<Integer, List<TextInfo>>>();
        for (File inputImage : getInputImages()) {
            imagesTextData.put(inputImage,
                    doOCRForImages(inputImage));
        }

        // create PdfDocument
        return createPdfDocument(pdfWriter, pdfOutputIntent, imagesTextData);
    }

    /**
     * Create a pdf document using provided properties,
     * add images with parsed text.
     *
     * @param pdfWriter PdfWriter
     * @param pdfOutputIntent PdfOutputIntent
     * @param imagesTextData Map<File, Map<Integer, List<TextInfo>>>
     * @return PdfDocument
     * @throws OCRException OCRException
     */
    private PdfDocument createPdfDocument(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent,
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData)
            throws OCRException {
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
            LOGGER.error("Error occurred when setting default font: "
                    + e.getMessage());
            try {
                defaultFont = PdfFontFactory.createFont(getDefaultFont(),
                        PdfEncodings.IDENTITY_H, true);
            } catch (com.itextpdf.io.IOException
                    | IOException | NullPointerException ex) {
                LOGGER.error("Error occurred when setting default font: "
                        + e.getMessage());
                throw new OCRException(OCRException.CANNOT_READ_FONT);
            }
        }
        LOGGER.info("Current scale mode: " + getScaleMode());
        addDataToPdfDocument(imagesTextData, pdfDocument, defaultFont);

        return pdfDocument;
    }

    /**
     * Write parsed data to text file using provided path.
     *
     * @param path String
     * @param data String
     */
    private void writeToTextFile(final String path,
            final String data) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path),
                StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            LOGGER.error("Error occurred during writing to " + path + " file: "
                    + e.getMessage());
        }
    }

    /**
     * Validate image format and perform OCR specifying output format.
     *
     * @param inputImage File
     * @param outputFormat OutputFormat
     * @return String
     */
    private String doOCRForImages(final File inputImage,
            final OutputFormat outputFormat) {
        String data = null;
        if (isValidImageFormat(inputImage)) {
            data = ocrReader.readDataFromInput(inputImage, outputFormat);
        } else {
            String extension = "incorrect extension";
            int index = inputImage.getAbsolutePath().lastIndexOf('.');
            if (index > 0) {
                extension = new String(inputImage
                        .getAbsolutePath().toCharArray(),
                        index + 1, inputImage
                        .getAbsolutePath().length() - index - 1);
            }
            throw new OCRException(OCRException.INCORRECT_INPUT_IMAGE_FORMAT)
                    .setMessageParams(extension);
        }
        return data;
    }

    /**
     * Validate image format and perform OCR using hOCR output format.
     *
     * @param inputImage File
     * @return Map<Integer, List<TextInfo>>
     */
    private Map<Integer, List<TextInfo>> doOCRForImages(final File inputImage) {
        Map<Integer, List<TextInfo>> data = new LinkedHashMap<Integer, List<TextInfo>>();
        if (isValidImageFormat(inputImage)) {
            data = ocrReader.readDataFromInput(inputImage);
        } else {
            String extension = "incorrect extension";
            int index = inputImage.getAbsolutePath().lastIndexOf('.');
            if (index > 0) {
                extension = new String(inputImage
                        .getAbsolutePath().toCharArray(),
                        index + 1, inputImage
                        .getAbsolutePath().length() - index - 1);
            }
            throw new OCRException(OCRException.INCORRECT_INPUT_IMAGE_FORMAT)
                    .setMessageParams(extension);
        }
        return data;
    }

    /**
     * Validate input image format.
     * allowed image formats are provided in "ImageFormat" format
     *
     * @param image input file
     * @return true if image format is valid, false - if not
     */
    private boolean isValidImageFormat(final File image) {
        boolean isValid = false;
        String extension = "incorrect extension";
        int index = image.getAbsolutePath().lastIndexOf('.');
        if (index > 0) {
            extension = new String(image.getAbsolutePath().toCharArray(),
                    index + 1,
                    image.getAbsolutePath().length() - index - 1);
            for (ImgFormat imageFormat
                    : ImgFormat.class.getEnumConstants()) {
                if (imageFormat.name().equals(extension.toLowerCase())) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                LOGGER.error("Image format is invalid: " + extension);
            }
        }
        return isValid;
    }

    /**
     * Perform OCR for input image.
     *
     * @param imagesTextData Map<File, Map<Integer, List<TextInfo>>> -
     *                       map that contains input image
     *                       files as keys, and as value:
     *                       map pageNumber -> text for the page
     * @param pdfDocument output pdf document
     * @param defaultFont default font
     * @throws OCRException if input image cannot be read
     */
    private void addDataToPdfDocument(
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData,
            final PdfDocument pdfDocument,
            final PdfFont defaultFont) throws OCRException {
        for (Map.Entry<File, Map<Integer, List<TextInfo>>> entry
                : imagesTextData.entrySet()) {
            try {
                File inputImage = entry.getKey();
                List<ImageData> imageDataList = getImageData(inputImage);
                LOGGER.info(inputImage.toString() + " image contains "
                        + imageDataList.size() + " page(s)");

                Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
                if (imageTextData.keySet().size() > 0) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        com.itextpdf.kernel.geom.Rectangle imageSize =
                                UtilService .calculateImageSize(
                                        imageData, getScaleMode(),
                                        getPageSize());

                        LOGGER.info("Started parsing image "
                                + inputImage.getName());

                        addToCanvas(pdfDocument, defaultFont, imageSize,
                                imageTextData.get(page + 1),
                                imageData);
                    }
                } else {
                    ImageData imageData = imageDataList.get(0);
                    com.itextpdf.kernel.geom.Rectangle imageSize = UtilService
                            .calculateImageSize(imageData, getScaleMode(),
                                    getPageSize());
                    addToCanvas(pdfDocument, defaultFont, imageSize,
                            new ArrayList<TextInfo>(), imageData);
                }
            } catch (IOException e) {
                LOGGER.error("Error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Add image and text to canvas.
     *
     * @param pdfDocument PdfDocument
     * @param defaultFont PdfFont
     * @param imageSize   PageSize
     * @param pageText    List<TextInfo>
     * @param imageData   ImageData
     */
    void addToCanvas(final PdfDocument pdfDocument, final PdfFont defaultFont,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> pageText, final ImageData imageData) {
        com.itextpdf.kernel.geom.Rectangle rectangleSize =
                getScaleMode() == ScaleMode.keepOriginalSize
                ? imageSize : getPageSize();
        PageSize size = new PageSize(rectangleSize);
        PdfPage pdfPage = pdfDocument.addNewPage(size);
        PdfCanvas canvas = new PdfCanvas(pdfPage);

        PdfLayer imageLayer = new PdfLayer(getImageLayerName(), pdfDocument);
        PdfLayer textLayer = new PdfLayer(getTextLayerName(), pdfDocument);

        canvas.beginLayer(imageLayer);
        addImageToCanvas(imageData, imageSize, canvas);
        canvas.endLayer();
        LOGGER.info("Added image page to canvas");

        // how much the original image size changed
        float multiplier = imageData == null
                ? 1 : imageSize.getWidth()
                / UtilService.getPoints(imageData.getWidth());
        canvas.beginLayer(textLayer);
        addTextToCanvas(imageSize, pageText, canvas, defaultFont,
                multiplier, pdfPage.getMediaBox());
        canvas.endLayer();
    }

    /**
     * Retrieve image data from the file.
     *
     * @param inputImage input file
     * @return list of ImageData objects (in case of multipage tiff)
     * @throws OCRException OCRException
     * @throws IOException  IOException
     */
    private List<ImageData> getImageData(final File inputImage)
            throws OCRException, IOException {
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
                    String exception = "Cannot open "
                            + inputImage.getAbsolutePath()
                            + " image, converting to png: " + e.getMessage();
                    LOGGER.warn(exception);
                    try {
                        BufferedImage bufferedImage = null;
                        try {
                            bufferedImage = ImageUtil
                                    .readImageFromFile(inputImage);
                        } catch (IllegalArgumentException | IOException ex) {
                            LOGGER.warn("Attempting to convert image: "
                                    + ex.getMessage());
                            bufferedImage = ImageUtil
                                    .readAsPixAndConvertToBufferedImage(inputImage);
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
                        exception = "Cannot parse "
                                + inputImage.getAbsolutePath()
                                + " image " + ex.getMessage();
                        LOGGER.error(exception);
                        throw new OCRException(
                                OCRException.CANNOT_READ_INPUT_IMAGE);
                    }
                }
            }
        }
        return images;
    }

    /**
     * Add image to canvas to background.
     *
     * @param imageData imageData
     * @param imageSize calculated size of the image
     * @param pdfCanvas pdfCanvas
     */
    private void addImageToCanvas(final ImageData imageData,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final PdfCanvas pdfCanvas) {
        if (imageData != null) {
            if (getScaleMode() == ScaleMode.keepOriginalSize) {
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
     * Add retrieved text to canvas.
     *
     * @param imageSize   calculated image size
     * @param data        List<TextInfo>
     * @param pdfCanvas   PdfCanvas
     * @param defaultFont PdfFont
     * @param multiplier  how image was scaled
     * @param pageMediaBox Rectangle
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void addTextToCanvas(
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> data,
            final PdfCanvas pdfCanvas,
            final PdfFont defaultFont,
            final float multiplier,
            final com.itextpdf.kernel.geom.Rectangle pageMediaBox) {
        if (data == null || data.size() == 0) {
            pdfCanvas.beginText().setFontAndSize(defaultFont, 1);
            pdfCanvas.showText("").endText();
        } else {
            List<Float> imageCoordinates = calculateImageCoordinates(
                    getPageSize(), imageSize, getScaleMode());
            float x = imageCoordinates.get(0);
            float y = imageCoordinates.get(1);
            for (TextInfo item : data) {
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
                    float fontSize = calculateFontSize(line, defaultFont,
                            bboxHeightPt);
                    float lineWidth = defaultFont.getWidth(line, fontSize);

                    float deltaX = UtilService.getPoints(left);
                    float deltaY = imageSize.getHeight() - UtilService
                            .getPoints(bottom);

                    float descent = defaultFont.getDescent(line, fontSize);

                    Canvas canvas = new Canvas(pdfCanvas, pageMediaBox);

                    Text text = new Text(line)
                            .setHorizontalScaling(bboxWidthPt / lineWidth)
                            .setBaseDirection(BaseDirection.LEFT_TO_RIGHT);

                    Paragraph paragraph = new Paragraph(text)
                            .setMargin(0)
                            .setMultipliedLeading(1);
                    paragraph.setFont(defaultFont)
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
     * Calculate appropriate font size to fit bbox's width and height.
     *
     * @param line          text line
     * @param defaultFont   default font
     * @param bboxHeightPt  bbox height
     * @return float
     */
    private float calculateFontSize(final String line,
            final PdfFont defaultFont,
            final float bboxHeightPt) {
        float fontSize = bboxHeightPt;
        boolean textScaled = false;

        float realTextSize;
        while (!textScaled) {
            float ascent = defaultFont.getAscent(line, fontSize);
            float descent = defaultFont.getDescent(line, fontSize);
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
     * Calculate image coordinates on the page.
     *
     * @param size          size of the page
     * @param imageSize     calculates size of the image
     * @param pageScaleMode page scale mode
     * @return Pair<Float, Float> containing x and y coordinates
     */
    private List<Float> calculateImageCoordinates(
            final com.itextpdf.kernel.geom.Rectangle size,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final ScaleMode pageScaleMode) {
        float x = 0;
        float y = 0;
        if (pageScaleMode != ScaleMode.keepOriginalSize) {
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
