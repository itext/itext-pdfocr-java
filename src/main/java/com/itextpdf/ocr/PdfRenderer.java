package com.itextpdf.ocr;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
 * This is the obligatory parameter that should be provided in constructor
 * or using setter
 *
 */
public class PdfRenderer implements IPdfRenderer {

    /**
     * Path to output pdf file.
     */
    private String pdfPath;

    /**
     * List of Files with input images.
     */
    private List<File> inputImages;

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
     *
     */
    private Rectangle pageSize = new Rectangle(PageSize.A4.getX(),
            PageSize.A4.getY(), PageSize.A4.getWidth(),
            PageSize.A4.getHeight());

    /**
     * Parameter describing selectedOCR reader
     * that corresponds IOcrReader interface.
     *
     */
    private IOcrReader ocrReader;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
             .getLogger(PdfRenderer.class);


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
        inputImages = images;
    }

    /**
     * PdfRenderer constructor with IOcrReader, list of input files
     * and scale mode.
     *
     * @param reader IOcrReader
     * @param images List<File>
     * @param mode ScaleMode
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
                       final ScaleMode mode) {
        ocrReader = reader;
        inputImages = images;
        scaleMode = mode;
    }

    /**
     * PdfRenderer constructor with IOcrReader, list of input files
     * and text color.
     *
     * @param reader IOcrReader
     * @param images List<File>
     * @param newColor Color
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
                       final Color newColor) {
        ocrReader = reader;
        inputImages = images;
        color = newColor;
        scaleMode = ScaleMode.keepOriginalSize;
    }

    /**
     * PdfRenderer constructor with IOcrReader, list of input files,
     * text color and scale mode.
     *
     * @param reader IOcrReader
     * @param images List<File>
     * @param newColor Color
     * @param mode ScaleMode
     */
    public PdfRenderer(final IOcrReader reader, final List<File> images,
                       final Color newColor, final ScaleMode mode) {
        ocrReader = reader;
        color = newColor;
        inputImages = images;
        scaleMode = mode;
    }

    /**
     * Path to output pdf file.
     *
     * @param path a {@link java.lang.String} object.
     */
    public final void setPdfPath(final String path) {
        pdfPath = path;
    }

    /**
     * Path to output pdf file.
     *
     * @return String
     */
    public final String getPdfPath() {
        return pdfPath;
    }

    /**
     * Set list of input images for OCR.
     *
     * @param images List<File>
     */
    public final void setInputImages(final List<File> images) {
        inputImages = images;
    }

    /**
     * Get list of provided input images for OCR.
     *
     * @return List<File>
     */
    public final List<File> getInputImages() {
        return inputImages;
    }

    /**
     * Set text color (should be CMYK) in output PDF document.
     *
     * @param newColor CMYK Color
     */
    public final void setColor(final Color newColor) {
        color = newColor;
    }

    /**
     * Get text color in output PDF document.
     *
     * @return Color
     */
    public final Color getColor() {
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
     * Set IOcrReader reader (e.g. TesseractReader object).
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
     * Perform OCR for the given list of input images using provided ocrReader
     * and default PdfWriter.
     *
     *
     * @return PdfDocument
     */
    public final PdfDocument doPdfOcr() {
        try {
            PdfWriter writer = new PdfWriter(new ObjectOutputStream(new ByteArrayOutputStream()));
            return doPdfOcr(writer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Perform OCR for the given list of input images using provided ocrReader.
     *
     * @return PDFDocument containing number of pages corresponding
     * the number of input images
     */
    public final PdfDocument doPdfOcr(PdfWriter pdfWriter) {
        try {
            LOGGER.info("Starting ocr for " + inputImages.size() + " image(s)");

            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            LOGGER.info("Current scale mode: " + getScaleMode());
            PdfFont defaultFont = PdfFontFactory
                    .createFont(StandardFonts.HELVETICA);

            for (File inputImage : inputImages) {
                doOCRForImage(inputImage, pdfDocument, defaultFont);
            }

            return pdfDocument;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Perform OCR for input image.
     *
     * @param inputImage input file
     * @param pdfDocument output pdf document
     * @param defaultFont default font
     */
    private void doOCRForImage(final File inputImage,
                               final PdfDocument pdfDocument,
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

                        PageSize newPageSize = new PageSize(size);
                        PdfPage pdfPage = pdfDocument.addNewPage(newPageSize);
                        PdfCanvas canvas = new PdfCanvas(pdfPage);

                        addImageToCanvas(imageData, newPageSize, canvas);
                        LOGGER.info("Added image page to canvas: "
                                + inputImage.getName());

                        addTextToCanvas(imageData,
                                UtilService
                                        .getTextForPage(data, page + 1),
                                canvas, defaultFont);
                    }
                } else {
                    PageSize newPageSize = new PageSize(getPageSize());
                    pdfDocument.addNewPage(newPageSize);
                }
            } catch (IOException e) {
                LOGGER.error("Error occurred:" + e.getLocalizedMessage());
            }
        } else {
            PageSize size = new PageSize(getPageSize());
            pdfDocument.addNewPage(size);
            LOGGER.error("Invalid image format: "
                    + inputImage.getAbsolutePath());
        }
    }

    /**
     * Retrieve image data from the file.
     *
     * @param inputImage input file
     * @return list of ImageData objects (in case of multipage tiff)
     * @throws IOException IOException
     */
    private List<ImageData> getImageData(final File inputImage)
            throws IOException {
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
     * @param newSize PageSize
     * @param canvas pdfCanvas
     */
    private void addImageToCanvas(final ImageData imageData,
                                  final PageSize newSize,
                                  final PdfCanvas canvas) {
        imageData.setHeight(newSize.getHeight() / UtilService.PX_TO_PT);
        imageData.setWidth(newSize.getWidth() / UtilService.PX_TO_PT);

        canvas.addImage(imageData, newSize, false);
    }

    /**
     * Add retrieved text to canvas.
     *
     * @param imageData imageData
     * @param data list of text objects (lines/words)
     * @param canvas pdfCanvas
     * @param defaultFont default font
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void addTextToCanvas(final ImageData imageData,
                                 final List<TextInfo> data,
                                 final PdfCanvas canvas,
                                 final PdfFont defaultFont) {
        float pageImagePixelHeight = imageData.getHeight();

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

                // set font and its size
                canvas.beginText()
                        .setFontAndSize(defaultFont, fontSize)
                        .setColor(getColor(), true);

                // make text invisible
                // canvas.setTextRenderingMode(1); // 3 // 1

                // place text correctly
                canvas.moveText(deltaX, deltaY);

                // add text on canvas
                canvas.showText(line)
                        .endText();
            }
        }
    }

    /**
     * Calculate appropriate font size to fit bbox's width and height.
     *
     * @param line text line
     * @param defaultFont default font
     * @param bboxWidthPt bbox width
     * @param bboxHeightPt bbox height
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
            }  else {
                fontSize -= fontSizeDelta;
            }
        }
        // float lineWidth = defaultFont.getWidth(line, fontSize);
        return fontSize;
    }

}
