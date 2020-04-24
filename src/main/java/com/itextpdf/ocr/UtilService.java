package com.itextpdf.ocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.ocr.IOcrReader.TextPositioning;
import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import com.itextpdf.styledxmlparser.jsoup.nodes.Document;
import com.itextpdf.styledxmlparser.jsoup.nodes.Element;
import com.itextpdf.styledxmlparser.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class.
 */
public final class UtilService {

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
     * Encoding UTF-8 string.
     */
    private static String encodingUTF8 = "UTF-8";

    /**
     * Constant to convert pixels to points (for tests).
     */
    @SuppressWarnings("checkstyle:magicnumber")
    static final float PX_TO_PT = 3f / 4f;

    /**
     * Private constructor for util class.
     */
    private UtilService() {
    }

    /**
     * Read text file to string.
     *
     * @param txtFile File
     * @return String
     */
    public static String readTxtFile(final File txtFile) {
        String content = null;
        try {
            content = new String(
                    Files.readAllBytes(txtFile.toPath()),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Cannot read file " + txtFile.getAbsolutePath()
                    + " with error " + e.getMessage());
        }
        return content;
    }

    /**
     * Convert from pixels to points.
     *
     * @param pixels float
     * @return float
     */
    public static float getPoints(final float pixels) {
        return pixels * PX_TO_PT;
    }

    /**
     * Delete file using provided path.
     *
     * @param pathToFile String
     */
    public static void deleteFile(final String pathToFile) {
        if (pathToFile != null && !pathToFile.isEmpty()
                && Files.exists(java.nio.file.Paths.get(pathToFile))) {
            try {
                Files.delete(java.nio.file.Paths.get(pathToFile));
            } catch (IOException e) {
                LOGGER.info("File " + pathToFile
                        + " cannot be deleted: " + e.getMessage());
            }
        }
    }

    /**
     * Parse `hocr` file, retrieve text, and return in the format
     * described below.
     * each list element : Map.Entry<String, List<Integer>> contains
     * word or line as a key and its 4 coordinates(bbox) as a values
     *
     * @param inputFiles list ofo input files
     * @param textPositioning TextPositioning
     * @return Map<Integer, List<TextInfo>>
     * @throws IOException IOException
     */
    @SuppressWarnings("checkstyle:magicnumber")
    static Map<Integer, List<TextInfo>> parseHocrFile(final List<File> inputFiles,
            final TextPositioning textPositioning)
            throws IOException {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();

        for (File inputFile : inputFiles) {
            if (inputFile != null
                    && Files.exists(
                            java.nio.file.Paths
                                    .get(inputFile.getAbsolutePath()))) {
                Document doc = Jsoup.parse(
                        new FileInputStream(inputFile.getAbsolutePath()),
                        encodingUTF8, inputFile.getAbsolutePath());
                Elements pages = doc.getElementsByClass("ocr_page");

                Pattern bboxPattern = Pattern.compile(".*bbox(\\s+\\d+){4}.*");
                Pattern bboxCoordinatePattern = Pattern
                        .compile(".*\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*");
                List<String> searchedClasses = TextPositioning.byLines
                        .equals(textPositioning)
                        ? Arrays.<String>asList("ocr_line", "ocr_caption")
                        : Collections.<String>singletonList("ocrx_word");
                for (Element page : pages) {
                    String[] pageNum = page.id().split("page_");
                    int pageNumber = Integer.parseInt(pageNum[pageNum.length - 1]);
                    List<TextInfo> textData = new ArrayList<TextInfo>();
                    if (searchedClasses.size() > 0) {
                        Elements objects = page
                                .getElementsByClass(searchedClasses.get(0));
                        for (int i = 1; i < searchedClasses.size(); i++) {
                            Elements foundElements = page
                                    .getElementsByClass(searchedClasses.get(i));
                            for (int j = 0; j < foundElements.size(); j++) {
                                objects.add(foundElements.get(j));
                            }
                        }
                        for (Element obj : objects) {
                            String value = obj.attr("title");
                            Matcher bboxMatcher = bboxPattern.matcher(value);
                            if (bboxMatcher.matches()) {
                                Matcher bboxCoordinateMatcher = bboxCoordinatePattern
                                        .matcher(bboxMatcher.group());
                                if (bboxCoordinateMatcher.matches()) {
                                    List<Float> coordinates = new ArrayList<Float>();
                                    for (int i = 0; i < 4; i++) {
                                        String coord = bboxCoordinateMatcher
                                                .group(i + 1);
                                        coordinates.add(Float.parseFloat(coord));
                                    }

                                    textData.add(new TextInfo(obj.text(), coordinates));
                                }
                            }
                        }
                    }
                    if (textData.size() > 0) {
                        if (imageData.containsKey(pageNumber)) {
                            pageNumber = Collections.max(imageData.keySet()) + 1;
                        }
                        imageData.put(pageNumber, textData);
                    }
                }
            }
        }
        return imageData;
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
    static com.itextpdf.kernel.geom.Rectangle calculateImageSize(
            final ImageData imageData,
            final IPdfRenderer.ScaleMode scaleMode,
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
            LOGGER.info("Original image size in pixels: ("
                    + imageData.getWidth() + ", "
                    + imageData.getHeight() + ")");
            if (scaleMode == IPdfRenderer.ScaleMode.keepOriginalSize) {
                com.itextpdf.kernel.geom.Rectangle size =
                        new com.itextpdf.kernel.geom.Rectangle(imgWidthPt,
                                imgHeightPt);

                LOGGER.info("Final size in points: (" + size.getWidth() + ", "
                        + size.getHeight() + ")");
                return size;
            } else {
                com.itextpdf.kernel.geom.Rectangle size =
                        new com.itextpdf.kernel.geom.Rectangle(
                                requiredSize.getWidth(),
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
                    float ratio = Math.min(
                            requiredSize.getWidth() / imgWidthPt,
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
}
