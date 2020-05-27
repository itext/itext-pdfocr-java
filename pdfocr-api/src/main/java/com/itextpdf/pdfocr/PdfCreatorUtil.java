package com.itextpdf.pdfocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.renderer.IRenderer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PdfCreatorUtil {

    /**
     * The Constant to convert pixels to points.
     */
    static final float PX_TO_PT = 3f / 4f;

    /**
     * The Constant for points per inch.
     */
    private static final float POINTS_PER_INCH = 72.0f;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdfCreatorUtil.class);

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
    static float calculateFontSize(Document document, final String line,
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
    static List<Float> calculateImageCoordinates(
            final com.itextpdf.kernel.geom.Rectangle size,
            final com.itextpdf.kernel.geom.Rectangle imageSize) {
        float x = 0;
        float y = 0;
        if (size != null) {
            if (imageSize.getHeight() < size.getHeight()) {
                y = (size.getHeight() - imageSize.getHeight()) / 2;
            }
            if (imageSize.getWidth() < size.getWidth()) {
                x = (size.getWidth() - imageSize.getWidth()) / 2;
            }
        }
        return Arrays.<Float>asList(x, y);
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
    static List<ImageData> getImageData(final File inputImage)
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
                int tiffPages = getNumberOfPageTiff(inputImage);

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
                    LOGGER.error(MessageFormatUtil.format(
                            PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE,
                            e.getMessage()));
                    throw new OcrException(
                            OcrException.CANNOT_READ_INPUT_IMAGE);
                }
            }
        }
        return images;
    }

    /**
     * Calculates the size of the PDF document page according to the provided
     * {@link ScaleMode}.
     *
     * @param imageData input image or its one page as
     *                  {@link com.itextpdf.io.image.ImageData}
     * @param scaleMode required {@link ScaleMode} that could be
     *                  set using {@link OcrPdfCreatorProperties#setScaleMode}
     *                  method
     * @param requiredSize size of the page that could be using
     *                     {@link OcrPdfCreatorProperties#setPageSize} method
     * @return {@link com.itextpdf.kernel.geom.Rectangle}
     */
    static com.itextpdf.kernel.geom.Rectangle calculateImageSize(
            final ImageData imageData,
            final ScaleMode scaleMode,
            final com.itextpdf.kernel.geom.Rectangle requiredSize) {
        // Adjust image size and dpi
        // The resolution of a PDF file is 72pt per inch
        float dotsPerPointX = 1.0f;
        float dotsPerPointY = 1.0f;
        if (imageData != null && imageData.getDpiX() > 0
                && imageData.getDpiY() > 0) {
            dotsPerPointX = imageData.getDpiX() / POINTS_PER_INCH;
            dotsPerPointY = imageData.getDpiY() / POINTS_PER_INCH;
        }

        if (imageData != null) {
            float imgWidthPt = getPoints(imageData.getWidth());
            float imgHeightPt = getPoints(imageData.getHeight());
            // page size will be equal to the image size if page size or
            // scale mode are not set
            if (requiredSize == null || scaleMode == null) {
                return new com.itextpdf.kernel.geom.Rectangle(imgWidthPt,
                        imgHeightPt);
            } else {
                com.itextpdf.kernel.geom.Rectangle size =
                        new com.itextpdf.kernel.geom.Rectangle(
                                requiredSize.getWidth(),
                                requiredSize.getHeight());
                // scale image according to the page size and scale mode
                if (scaleMode == ScaleMode.SCALE_HEIGHT) {
                    float newHeight = imgHeightPt
                            * requiredSize.getWidth() / imgWidthPt;
                    size.setHeight(newHeight);
                } else if (scaleMode == ScaleMode.SCALE_WIDTH) {
                    float newWidth = imgWidthPt
                            * requiredSize.getHeight() / imgHeightPt;
                    size.setWidth(newWidth);
                } else if (scaleMode == ScaleMode.SCALE_TO_FIT) {
                    float ratio = Math.min(
                            requiredSize.getWidth() / imgWidthPt,
                            requiredSize.getHeight() / imgHeightPt);
                    size.setWidth(imgWidthPt * ratio);
                    size.setHeight(imgHeightPt * ratio);
                }
                return size;
            }
        } else {
            return requiredSize;
        }
    }

    /**
     * Converts value from pixels to points.
     *
     * @param pixels input value in pixels
     * @return result value in points
     */
    static float getPoints(final float pixels) {
        return pixels * PX_TO_PT;
    }

    /**
     * Counts number of pages in the provided tiff image.
     *
     * @param inputImage input image {@link java.io.File}
     * @return number of pages in the provided TIFF image
     * @throws IOException if error occurred during creating a
     * {@link com.itextpdf.io.source.IRandomAccessSource} based on a filename
     * string
     */
    private static int getNumberOfPageTiff(final File inputImage)
            throws IOException {
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(
                new RandomAccessSourceFactory()
                        .createBestSource(
                                inputImage.getAbsolutePath()));
        int numOfPages = TiffImageData.getNumberOfPages(raf);
        raf.close();
        return numOfPages;
    }
}
