package com.itextpdf.ocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.colors.Color;
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
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.BaseDirection;
import com.itextpdf.ocr.IOcrReader.OutputFormat;
import com.itextpdf.pdfa.PdfADocument;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
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
    private String defaultFontPath = "src/main/resources/com/"
            + "itextpdf/ocr/fonts/LiberationSans-Regular.ttf";

    /**
     * List of Files with input images.
     */
    private List<File> inputImages = Collections.emptyList();

    /**
     * CMYK color of the text in the output PDF document.
     * Text will be transparent by default
     */
    private Color textColor = null;

    /**
     * Scale mode for input images: "scaleToFit" by default.
     */
    private ScaleMode scaleMode = ScaleMode.scaleToFit;

    /**
     * Size of the PDF document pages: "A4" by default.
     * This parameter is taken into account only if "scaleMode" is scaleWidth,
     * scaleHeight or scaleToFit
     */
    private Rectangle pageSize = new Rectangle(PageSize.A4.getX(),
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
        inputImages = Collections.unmodifiableList(images);
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
        inputImages = Collections.unmodifiableList(images);
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
            final Color newColor) {
        ocrReader = reader;
        inputImages = Collections.unmodifiableList(images);
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
            final Color newColor, final ScaleMode mode) {
        ocrReader = reader;
        textColor = newColor;
        inputImages = Collections.unmodifiableList(images);
        scaleMode = mode;
    }

    /**
     * Set list of input images for OCR.
     *
     * @param images List<File>
     */
    public void setInputImages(final List<File> images) {
        inputImages = Collections.unmodifiableList(images);
    }

    /**
     * Get list of provided input images for OCR.
     *
     * @return List<File>
     */
    public final List<File> getInputImages() {
        return new ArrayList<>(inputImages);
    }

    /**
     * Set text color (should be CMYK) in output PDF document.
     *
     * @param newColor CMYK Color
     */
    public final void setTextColor(final Color newColor) {
        textColor = newColor;
    }

    /**
     * Get text color in output PDF document.
     *
     * @return Color
     */
    public final Color getTextColor() {
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
    public final void setPageSize(final Rectangle size) {
        pageSize = size;
    }

    /**
     * Get size for output document.
     *
     * @return Rectangle
     */
    public final Rectangle getPageSize() {
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
        return fontPath != null && !fontPath.isEmpty()
                ? fontPath : getDefaultFontPath();
    }

    /**
     * @return path to default font
     */
    public String getDefaultFontPath() {
        return defaultFontPath;
    }

    /**
     * Set default font.
     *
     * @param defaultFont Path to default font
     */
    public void setDefaultFontPath(final String defaultFont) {
        defaultFontPath = defaultFont;
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
     * Perform OCR for the given list of input images and
     * save output to a text file with provided path.
     *
     * @param absolutePath String
     * @return PdfDocument
     * @throws IOException if provided font is incorrect
     */
    public void doPdfOcr(String absolutePath) {
        LOGGER.info("Starting ocr for " + getInputImages().size() + " image(s)");

        // map contains image files as keys and retrieved text data as values
        StringBuilder content = new StringBuilder();
        for (File inputImage : getInputImages()) {
            content.append(doOCRForImages(inputImage, OutputFormat.txt));
            content.append("\n");
        }

        // write to file
        writeToTextFile(absolutePath, content.toString());
    }

    /**
     * Perform OCR for the given list of input images using provided pdfWriter.
     *
     * @param pdfWriter PdfWriter
     * @return PdfDocument
     * @throws IOException if provided font is incorrect
     */
    public final PdfDocument doPdfOcr(final PdfWriter pdfWriter)
            throws IOException {
        return doPdfOcr(pdfWriter, null);
    }

    /**
     * Perform OCR for the given list of input images using provided pdfWriter.
     * PDF/A-3u document will be created if pdfOutputIntent is not null
     *
     * @param pdfWriter PdfWriter
     * @param pdfOutputIntent PdfOutputIntent
     * @return PDF/A-3u document if pdfOutputIntent is not null
     * @throws IOException if provided font or output intent is incorrect
     */
    public final PdfDocument doPdfOcr(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent)
            throws IOException {
        LOGGER.info("Starting ocr for " + getInputImages().size() + " image(s)");

        // map contains image files as keys and retrieved text data as values
        Map<File, List<TextInfo>> imagesTextData = new HashMap<>();
        for (File inputImage : getInputImages()) {
            imagesTextData.put(inputImage, doOCRForImages(inputImage));
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
     * @param imagesTextData Map<File, List<TextInfo>>
     * @return PdfDocument
     * @throws IOException
     */
    private PdfDocument createPdfDocument(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent,
            final Map<File, List<TextInfo>> imagesTextData) throws IOException {
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
            defaultFont = PdfFontFactory.createFont(getFontPath(),
                    PdfEncodings.IDENTITY_H, true);
        } catch (com.itextpdf.io.IOException | IOException e) {
            LOGGER.error("Error occurred when setting default font: "
                    + e.getMessage());
            defaultFont = PdfFontFactory.createFont(getDefaultFontPath(),
                    PdfEncodings.IDENTITY_H, true);
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
        try {
            File file = new File(path);
            boolean created = file.createNewFile();
            if (!created) {
                LOGGER.error("File " + path + " cannot be created");
            }
            Writer targetFileWriter = new FileWriter(file);
            targetFileWriter.write(data);
            targetFileWriter.close();
        } catch (IOException e) {
            LOGGER.error("Error occurred during writing to file " + path);
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
        if (!validateImageFormat(inputImage)) {
            throw new OCRException(OCRException.INCORRECT_INPUT_IMAGE_FORMAT)
                    .setMessageParams(
                            FilenameUtils.getExtension(inputImage.getName()));
        } else {
            data = ocrReader.readDataFromInput(inputImage, outputFormat);
        }
        return data;
    }

    /**
     * Validate image format and perform OCR using hOCR output format.
     *
     * @param inputImage File
     * @return List<TextInfo>
     */
    private List<TextInfo> doOCRForImages(final File inputImage) {
        List<TextInfo> data;
        if (!validateImageFormat(inputImage)) {
            throw new OCRException(OCRException.INCORRECT_INPUT_IMAGE_FORMAT)
                    .setMessageParams(
                            FilenameUtils.getExtension(inputImage.getName()));
        } else {
            data = new ArrayList<>(ocrReader.readDataFromInput(inputImage));
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
    private boolean validateImageFormat(final File image) {
        String ext = FilenameUtils.getExtension(image.getName());

        boolean isValid = true;
        try {
            ImageFormat.valueOf(ext.toLowerCase());
        } catch (IllegalArgumentException ex) {
            isValid = false;
            LOGGER.error("Image format is invalid: " + ext);
        }

        return isValid;
    }

    /**
     * Perform OCR for input image.
     *
     * @param imagesTextData  Map<File, List<TextInfo>> - map that contains input image
     *                        files as keys, and text data as value for each image
     * @param pdfDocument output pdf document
     * @param defaultFont default font
     * @throws OCRException if input image cannot be read
     */
    private void addDataToPdfDocument(final Map<File, List<TextInfo>> imagesTextData,
            final PdfDocument pdfDocument,
            final PdfFont defaultFont) throws OCRException {
        for (File inputImage : imagesTextData.keySet()) {
            try {
                List<ImageData> imageDataList = getImageData(inputImage);
                LOGGER.info(inputImage.toString() + " image contains "
                        + imageDataList.size() + " page(s)");

                List<TextInfo> data = imagesTextData.get(inputImage);
                if (!data.isEmpty()) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        Rectangle imageSize = UtilService
                                .calculateImageSize(imageData, getScaleMode(),
                                        getPageSize());

                        LOGGER.info("Started parsing image "
                                + inputImage.getName());

                        addToCanvas(pdfDocument, defaultFont, imageSize,
                                UtilService.getTextForPage(data,
                                        page + 1),
                                imageData);
                    }
                } else {
                    ImageData imageData = imageDataList.get(0);
                    Rectangle imageSize = UtilService
                            .calculateImageSize(imageData, getScaleMode(),
                                    getPageSize());
                    addToCanvas(pdfDocument, defaultFont, imageSize,
                            new ArrayList<>(), imageData);
                }
            } catch (IOException e) {
                LOGGER.error("Error occurred: " + e.getLocalizedMessage());
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
            final Rectangle imageSize,
            final List<TextInfo> pageText, final ImageData imageData) {
        Rectangle size = getScaleMode() == ScaleMode.keepOriginalSize
                ? imageSize : getPageSize();
        PageSize pageSize = new PageSize(size);
        PdfPage pdfPage = pdfDocument.addNewPage(pageSize);
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
        addTextToCanvas(imageSize, pageText, canvas, defaultFont, multiplier);
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
        List<ImageData> images = new ArrayList<>();

        String ext = FilenameUtils.getExtension(inputImage.getName());

        if ("tiff".equals(ext.toLowerCase())
                || "tif".equals(ext.toLowerCase())) {
            Path tiffFile = Paths.get(inputImage.getPath());

            RandomAccessFileOrArray raf = new RandomAccessFileOrArray(
                    new RandomAccessSourceFactory()
                            .createBestSource(tiffFile.toString()));
            int tiffPages = TiffImageData.getNumberOfPages(raf);

            for (int page = 0; page < tiffPages; page++) {
                ImageData imageData = ImageDataFactory
                        .createTiff(tiffFile.toUri().toURL(),
                                true, page + 1, true);
                images.add(imageData);
            }
            raf.close();
        } else {
            try {
                ImageData imageData = ImageDataFactory
                        .create(inputImage.getAbsolutePath());
                images.add(imageData);
            } catch (com.itextpdf.io.IOException e) {
                try {
                    BufferedImage bufferedImage = ImageIO.read(inputImage);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", baos);
                    ImageData imageData = ImageDataFactory.create(baos.toByteArray());
                    images.add(imageData);
                } catch (com.itextpdf.io.IOException | IOException | IllegalArgumentException ex) {
                    LOGGER.error("Cannot parse " + inputImage.getAbsolutePath()
                            + " image " + e.getLocalizedMessage());
                    throw new OCRException(OCRException.CANNOT_READ_INPUT_IMAGE);
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
            final Rectangle imageSize,
            final PdfCanvas pdfCanvas) {
        if (imageData != null) {
            if (getScaleMode() == ScaleMode.keepOriginalSize) {
                pdfCanvas.addImage(imageData, imageSize, false);
            } else {
                List<Float> coordinates = calculateImageCoordinates(
                        getPageSize(), imageSize, getScaleMode());
                Rectangle rect = new Rectangle(coordinates.get(0),
                        coordinates.get(1),
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
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void addTextToCanvas(final Rectangle imageSize,
            final List<TextInfo> data,
            final PdfCanvas pdfCanvas,
            final PdfFont defaultFont,
            final float multiplier) {
        if (data == null || data.isEmpty()) {
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
                final Float left = coordinates.get(0) * multiplier;
                final Float right = (coordinates.get(2) + 1) * multiplier - 1;
                final Float top = coordinates.get(1) * multiplier;
                final Float bottom = (coordinates.get(3) + 1) * multiplier - 1;
                final float delta = 0.05f;

                float bboxWidthPt = UtilService
                        .getPoints(right - left);
                float bboxHeightPt = UtilService
                        .getPoints(bottom - top);
                if (!line.isEmpty() && bboxHeightPt > 0 && bboxWidthPt > 0) {
                    // Scale the text width to fit the OCR bbox
                    float fontSize = calculateFontSize(line, defaultFont,
                            bboxWidthPt, bboxHeightPt, delta);

                    // logger.info("Setting font size " + fontSize);

                    float deltaX = UtilService.getPoints(left);
                    float deltaY = imageSize.getHeight() - UtilService
                            .getPoints(bottom);

                    Rectangle rectangle = new Rectangle(deltaX + x,
                            deltaY + fontSize / 2 + y, bboxWidthPt * 1.5f,
                            bboxHeightPt);
                    Canvas canvas = new Canvas(pdfCanvas,
                            pdfCanvas.getDocument(), rectangle);
                    Text text = new Text(line).setFont(defaultFont)
                            .setFontSize(fontSize)
                            .setBaseDirection(BaseDirection.LEFT_TO_RIGHT);
                    if (getTextColor() != null) {
                        text.setFontColor(getTextColor());
                    } else {
                        text.setOpacity(0.0f);
                    }
                    canvas.add(new Paragraph(text));
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
     * @param bboxWidthPt   bbox width
     * @param bboxHeightPt  bbox height
     * @param fontSizeDelta float
     * @return float
     */
    private float calculateFontSize(final String line,
            final PdfFont defaultFont,
            final float bboxWidthPt,
            final float bboxHeightPt,
            final float fontSizeDelta) {
        float fontSize = bboxHeightPt;
        boolean textScaled = false;

        float lineWidth = defaultFont.getWidth(line, fontSize);
        boolean increaseSize = lineWidth < bboxWidthPt;
        while (!textScaled) {
            lineWidth = defaultFont.getWidth(line, fontSize);
            if (Math.abs(lineWidth - bboxWidthPt) < 1) {
                textScaled = true;
            } else if (lineWidth < bboxWidthPt) {
                fontSize += fontSizeDelta * (increaseSize ? 1f : 0.5f);
            } else {
                fontSize -= fontSizeDelta * (increaseSize ? 0.5f : 1f);
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
    private List<Float> calculateImageCoordinates(final Rectangle size,
            final Rectangle imageSize,
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
        return Arrays.asList(x, y);
    }
}
