package com.itextpdf.ocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.pdfa.PdfADocument;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PDF Renderer class.
 *
 * The IPdfRenderer provides possibilities to set list of input images
 * to be used for OCR, to set scaling mode for images, color of text in
 * the output PDF document, set fixed size of the PDF document
 * and to perform OCR using given images and return PDFDocument as result
 *
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
     * Path to image placeholder file that is used in case of any error.
     */
    private static final String placeholderImagePath = "src/main/resources/com/itextpdf/ocr/placeholder.jpg";

    /**
     * Path to default font file (Cairo-Regular).
     */
    private String defaultFontPath = "src/main/resources/com/itextpdf/ocr/fonts/Cairo-Regular.ttf";

    /**
     * List of Files with input images.
     */
    private List<File> inputImages = Collections.emptyList();

    /**
     * CMYK color of the text in the output PDF document.
     * "DeviceCmyk.BLACK" by default
     */
    private Color color = DeviceCmyk.BLACK;

    /**
     * Scale mode for input images: "keepOriginalSize" xby default.
     */
    private ScaleMode scaleMode = ScaleMode.keepOriginalSize;

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
        color = newColor;
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
        color = newColor;
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
    public final void setFontColor(final Color newColor) {
        color = newColor;
    }

    /**
     * Get text color in output PDF document.
     *
     * @return Color
     */
    public final Color getFontColor() {
        return color;
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
    public final void setImageLayerName(String name) {
        imageLayerName = name;
    }

    /**
     * Get name of image layer.
     *
     * @return layer's name that was manually set or the default one (="Image layer")
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
    public final void setTextLayerName(String name) {
        textLayerName = name;
    }

    /**
     * @return layer's name that was manually set or the default one (="Text layer")
     */
    public final String getTextLayerName() {
        return textLayerName;
    }

    /**
     * Specify pdf natural language, and optionally locale.
     * @param lang String
     */
    public final void setPdfLang(String lang) {
        pdfLang = lang;
    }

    /**
     *
     * @return pdf document lang
     */
    public final String getPdfLang() {
        return pdfLang;
    }

    /**
     * Set pdf document title.
     * @param name String
     */
    public final void setTitle(String name) {
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
     * @param path
     */
    public void setFontPath(String path) {
        fontPath = path;
    }

    /**
     * @return Font path that was set or default font path
     */
    public String getFontPath() {
        return fontPath != null && !fontPath.isEmpty() ? fontPath : defaultFontPath;
    }

    /**
     * @return path to default font
     */
    public String getDefaultFontPath() {
        return defaultFontPath;
    }

    /**
     * @return path to default font
     */
    /**
     * Set default font
     *
     * @return
     */
    public void setDefaultFontPath(String defaultFont) {
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

    public final PdfDocument doPdfOcr(PdfWriter pdfWriter,
                                      boolean createPdfA3u) throws IOException {
        return doPdfOcr(pdfWriter, createPdfA3u, null);
    }

    /**
     * Perform OCR for the given list of input images using provided pdfWriter.
     *
     * @param pdfWriter PdfWriter
     * @param createPdfA3u - true if output result should PdfADocument
     * @param pdfOutputIntent - required parameter only if createPdfA3u is true
     * @return PdfDocument
     */
    public final PdfDocument doPdfOcr(PdfWriter pdfWriter, boolean createPdfA3u,
        PdfOutputIntent pdfOutputIntent) throws IOException {

        LOGGER.info("Starting ocr for " + inputImages.size() + " image(s)");

        PdfDocument pdfDocument;
        if (createPdfA3u) {
            if (pdfOutputIntent != null) {
                pdfDocument = new PdfADocument(pdfWriter,
                        PdfAConformanceLevel.PDF_A_3U, pdfOutputIntent);
            } else {
                throw new Exception(Exception.OUTPUT_INTENT_CANNOT_BE_NULL);
            }
        } else {
            pdfDocument = new PdfDocument(pdfWriter);
        }

        // add metadata
        pdfDocument.getCatalog().setLang(new PdfString(getPdfLang()));
        pdfDocument.getCatalog().setViewerPreferences(
                new PdfViewerPreferences().setDisplayDocTitle(true));
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
        info.setTitle(getTitle());

        LOGGER.info("Current scale mode: " + getScaleMode());
        PdfFont defaultFont = null;
        try {
            defaultFont = PdfFontFactory.createFont(getFontPath(),
                    PdfEncodings.IDENTITY_H, true);
        } catch (com.itextpdf.io.IOException | IOException e) {
            LOGGER.error("Error occurred when setting default font: " + e.getMessage());
            defaultFont = PdfFontFactory.createFont(getDefaultFontPath(),
                    PdfEncodings.IDENTITY_H, true);
        }

        for (File inputImage : inputImages) {
            doOCRForImage(inputImage, pdfDocument, defaultFont);
        }

        return pdfDocument;
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
     * @param inputImage  input file
     * @param pdfDocument output pdf document
     * @param defaultFont default font
     */
    private void doOCRForImage(final File inputImage, final PdfDocument pdfDocument,
                               final PdfFont defaultFont) {
        if (validateImageFormat(inputImage)) {
            try {
                List<ImageData> imageDataList = getImageData(inputImage);
                LOGGER.info(inputImage.toString() + " image contains "
                        + imageDataList.size() + " page(s)");

                List<TextInfo> data = ocrReader.readDataFromInput(inputImage);

                if (!data.isEmpty()) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        Rectangle size = UtilService
                                .calculatePageSize(imageData, getScaleMode(),
                                        getPageSize());

                        LOGGER.info("Started parsing image " + inputImage.getName());
                        PageSize newPageSize = new PageSize(size);

                        addToCanvas(pdfDocument, defaultFont, newPageSize,
                                    UtilService.getTextForPage(data, page + 1),
                                    imageData);
                    }
                } else {
                    PageSize newPageSize = new PageSize(getPageSize());
                    ImageData imageData = !imageDataList.isEmpty() ? imageDataList.get(0) : null;
                    addToCanvas(pdfDocument, defaultFont, newPageSize, new ArrayList<>(), imageData);
                }
            } catch (IOException e) {
                LOGGER.error("Error occurred:" + e.getLocalizedMessage());
            }
        } else {
            PageSize size = new PageSize(getPageSize());
            addToCanvas(pdfDocument, defaultFont, size, new ArrayList<>(), null);
            LOGGER.error("Invalid image format: "
                    + inputImage.getAbsolutePath());
        }
    }

    /**
     * Add image and text to canvas
     *
     * @param pdfDocument PdfDocument
     * @param defaultFont PdfFont
     * @param pageSize PageSize
     * @param pageText List<TextInfo>
     * @param imageData ImageData
     */
    void addToCanvas(final PdfDocument pdfDocument, final PdfFont defaultFont, PageSize pageSize,
                     List<TextInfo> pageText, ImageData imageData) {
        PdfPage pdfPage = pdfDocument.addNewPage(pageSize);
        PdfCanvas canvas = new PdfCanvas(pdfPage);

        PdfLayer imageLayer = new PdfLayer(getImageLayerName(), pdfDocument);
        PdfLayer textLayer = new PdfLayer(getTextLayerName(), pdfDocument);

        canvas.beginLayer(imageLayer);
        addImageToCanvas(imageData, pageSize, canvas);
        canvas.endLayer();
        LOGGER.info("Added image page to canvas");

        canvas.beginLayer(textLayer);
        addTextToCanvas(imageData == null ? pageSize.getHeight() : imageData.getHeight(),
                        pageText, canvas, defaultFont);
        canvas.endLayer();
    }

    /**
     * Retrieve image data from the file.
     *
     * @param inputImage input file
     * @return list of ImageData objects (in case of multipage tiff)
     * @throws IOException IOException
     */
    private List<ImageData> getImageData(final File inputImage) throws IOException {
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
            ImageData imageData = ImageDataFactory
                    .create(inputImage.getAbsolutePath());
            images.add(imageData);
        }

        return images;
    }

    /**
     * Add image to canvas to background.
     *
     * @param imageData imageData
     * @param newSize   PageSize
     * @param canvas    pdfCanvas
     */
    private void addImageToCanvas(ImageData imageData, final PageSize newSize,
                                  final PdfCanvas canvas) {
        if (imageData == null) {
            List<ImageData> data = new ArrayList<>();
            try {
                data = getImageData(new File(placeholderImagePath));
            } catch (IOException e) {
                LOGGER.error("Error during create image data (using path "
                        + placeholderImagePath + "): "
                        + e.getLocalizedMessage());
            }
            imageData = data.isEmpty() ? null : data.get(0);
        }
        // up to this step imageData should not be null anymore
        if (imageData != null) {
            imageData.setHeight(newSize.getHeight() / UtilService.PX_TO_PT);
            imageData.setWidth(newSize.getWidth() / UtilService.PX_TO_PT);
            canvas.addImage(imageData, newSize, false);
        }
    }

    /**
     * Add retrieved text to canvas.
     *
     * @param pageImagePixelHeight float
     * @param data List<TextInfo>
     * @param pdfCanvas PdfCanvas
     * @param defaultFont PdfFont
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void addTextToCanvas(final float pageImagePixelHeight,
                                 final List<TextInfo> data,
                                 final PdfCanvas pdfCanvas, final PdfFont defaultFont) {
        if (data == null || data.isEmpty()) {
            pdfCanvas.beginText().setFontAndSize(defaultFont, 1);
            pdfCanvas.showText("").endText();
        } else {
            for (TextInfo item : data) {
                String line = item.getText();
                List<Integer> coordinates = item.getCoordinates();
                final Integer left = coordinates.get(0);
                final Integer right = coordinates.get(2);
                final Integer top = coordinates.get(1);
                final Integer bottom = coordinates.get(3);
                final float delta = 0.1f;

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
                    float deltaY = UtilService
                            .getPoints(pageImagePixelHeight - bottom);

//                    pdfCanvas.rectangle(new Rectangle(deltaX, deltaY, bboxWidthPt, bboxHeightPt*2));
//                    pdfCanvas.setStrokeColor(DeviceCmyk.MAGENTA);
//                    pdfCanvas.stroke();

                    Canvas canvas = new Canvas(pdfCanvas, pdfCanvas.getDocument(),
                            new Rectangle(deltaX, deltaY + fontSize,
                                    bboxWidthPt * 1.5f, bboxHeightPt));
                    Text text = new Text(line).setFont(defaultFont)
                                        .setFontColor(getFontColor())
                                        .setFontSize(fontSize);
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

        while (!textScaled) {
            float lineWidth = defaultFont.getWidth(line, fontSize);
            if (Math.abs(lineWidth - bboxWidthPt) < 1) {
                textScaled = true;
            } else if (lineWidth < bboxWidthPt) {
                fontSize += fontSizeDelta;
            } else {
                fontSize -= fontSizeDelta;
            }
        }
        float lineWidth = defaultFont.getWidth(line, fontSize);
        return fontSize;
    }
}
