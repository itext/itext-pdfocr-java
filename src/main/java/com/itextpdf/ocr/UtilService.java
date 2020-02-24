package com.itextpdf.ocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.ocr.IOcrReader.TextPositioning;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class.
 */
final class UtilService {

    /**
     * Constantsfor points per inch (for tests).
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static final float POINTS_PER_INCH = 72.0f;

    /**
     * UtilService logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UtilService.class);

    /**
     * Constant to convert pixels to points (for tests).
     */
    @SuppressWarnings("checkstyle:magicnumber")
    static final float PX_TO_PT = 3f / 4f;

    /**
     * Run given command in command line.
     *
     * @param command   List<String>
     * @param isWindows boolean
     * @throws OCRException if command failed
     */
    public static void runCommand(final List<String> command,
            final boolean isWindows) throws OCRException {
        Process process = null;
        try {
            LOGGER.info("Running command: "
                    + String.join(" ", command));
            if (isWindows) {
                ProcessBuilder pb = new ProcessBuilder(command); //NOSONAR
                process = pb.start();
            } else {
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", //NOSONAR
                        String.join(" ", command)); //NOSONAR
                process = pb.start();
            }
            boolean cmdSucceeded = process.waitFor(3 * 60 * 60 * 1000,
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            if (!cmdSucceeded) {
                LOGGER.error("Error occurred during running command: "
                        + String.join(" ", command));
                throw new OCRException(OCRException.TESSERACT_FAILED);
            }

            process.destroy();
        } catch (NullPointerException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Error occurred:" + e.getMessage());
        }
    }

    /**
     * Parse `hocr` file, retrieve text, and return in the format
     * described below.
     * each list element : Map.Entry<String, List<Integer>> contains
     * word or line as a key and its 4 coordinates(bbox) as a values
     *
     * @param inputFile File
     * @param textPositioning TextPositioning
     * @return List<TextInfo>
     * @throws IOException IOException
     */
    @SuppressWarnings("checkstyle:magicnumber")
    static List<TextInfo> parseHocrFile(final File inputFile,
            final TextPositioning textPositioning)
            throws IOException {
        List<TextInfo> textData = new ArrayList<TextInfo>();

        Document doc = org.jsoup.Jsoup.parse(inputFile.getAbsoluteFile(), String.valueOf(StandardCharsets.UTF_8));
        Elements pages = doc.getElementsByClass("ocr_page");

        Pattern bboxPattern = Pattern.compile("bbox(\\s+\\d+){4}");
        Pattern bboxCoordinatePattern = Pattern
                .compile("(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
        String searchedClass = TextPositioning.byLines.equals(textPositioning)
                ? "ocr_line" : "ocrx_word";
        for (org.jsoup.nodes.Element page : pages) {
            String[] pageNum = page.id().split("page_");
            int pageNumber = Integer.parseInt(pageNum[pageNum.length - 1]);
            Elements objects = page.getElementsByClass(searchedClass);
            for (org.jsoup.nodes.Element obj : objects) {
                String value = obj.attr("title");
                Matcher bboxMatcher = bboxPattern.matcher(value);
                if (bboxMatcher.find()) {
                    Matcher bboxCoordinateMatcher = bboxCoordinatePattern
                            .matcher(bboxMatcher.group());
                    if (bboxCoordinateMatcher.find()) {
                        List<Float> coordinates = new ArrayList<>();
                        for (int i = 0; i < 4; i++) {
                            coordinates.add(Float
                                    .parseFloat(bboxCoordinateMatcher.group(i + 1)));
                        }

                        textData.add(new TextInfo(obj.text(), pageNumber,
                                coordinates));
                    }
                }
            }
        }
        return textData;
    }

    /**
     * Read text file to string.
     *
     * @param txtFile File
     * @return String
     */
    static String readTxtFile(final File txtFile) {
        String content = null;
        try {
            content = new String (Files.readAllBytes(txtFile.toPath()));
        } catch (IOException e) {
            LOGGER.error("Cannot read file " + txtFile.getAbsolutePath()
                    + " with error " + e.getMessage());
        }
        return content;
    }

    /**
     * Calculate the size of the PDF document page
     * should transform pixels to points and according to image resolution.
     *
     * @param imageData    ImageData
     * @param scaleMode    IPdfRenderer.ScaleMode
     * @param requiredSize Rectangle
     * @return Rectangle
     */
    static Rectangle calculateImageSize(final ImageData imageData,
            final IPdfRenderer.ScaleMode scaleMode,
            final Rectangle requiredSize) {
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
            LOGGER.info("Original image size in pixels: ("
                    + imageData.getWidth() + ", "
                    + imageData.getHeight() + ")");
            if (scaleMode == IPdfRenderer.ScaleMode.keepOriginalSize) {
                Rectangle size = new Rectangle(imgWidthPt, imgHeightPt);

                LOGGER.info("Final size in points: (" + size.getWidth() + ", "
                        + size.getHeight() + ")");
                return size;
            } else {
                Rectangle size = new Rectangle(requiredSize.getWidth(),
                        requiredSize.getHeight());
                // scale image according to the page size and scale mode
                if (scaleMode == IPdfRenderer.ScaleMode.scaleHeight) {
                    float newHeight = imgHeightPt
                            * requiredSize.getWidth() / imgWidthPt;
                    size.setHeight(newHeight);
                } else if (scaleMode == IPdfRenderer.ScaleMode.scaleWidth) {
                    float newWidth = imgWidthPt
                            * requiredSize.getHeight() / imgHeightPt;
                    size.setWidth(newWidth);
                } else if (scaleMode == IPdfRenderer.ScaleMode.scaleToFit) {
                    float ratio = Math.min(requiredSize.getWidth() / imgWidthPt,
                            requiredSize.getHeight() / imgHeightPt);
                    size.setWidth(imgWidthPt * ratio);
                    size.setHeight(imgHeightPt * ratio);
                }
                LOGGER.info("Final size in points: (" + size.getWidth()
                        + ", " + size.getHeight() + ")");
                return size;
            }
        } else {
            return requiredSize;
        }
    }

    /**
     * Convert from pixels to points.
     *
     * @param pixels float
     * @return float
     */
    static float getPoints(final float pixels) {
        return pixels * PX_TO_PT;
    }

    /**
     * Retrieve text for specified page.
     *
     * @param data List<TextInfo>
     * @param page Integer
     * @return List<TextInfo>
     */
    static List<TextInfo> getTextForPage(final List<TextInfo> data,
            final Integer page) {
        List<TextInfo> result = new ArrayList<>();
        for (TextInfo item : data) {
            if (item.getPageNumber().equals(page)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Delete file using provided path.
     *
     * @param file File
     */
    static void deleteFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = true;
            deleted = file.delete();
            if (!deleted || !file.exists()) {
                LOGGER.warn("File " + file.getAbsolutePath() + " was not deleted");
            }
        }
    }
}
