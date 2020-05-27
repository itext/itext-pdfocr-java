package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import com.itextpdf.styledxmlparser.jsoup.nodes.Document;
import com.itextpdf.styledxmlparser.jsoup.nodes.Element;
import com.itextpdf.styledxmlparser.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class.
 */
class TesseractHelper {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractHelper.class);

    /**
     * Creates a new {@link TesseractHelper} instance.
     */
    private TesseractHelper() {
    }

    /**
     * Deletes file using provided path.
     *
     * @param pathToFile path to the file to be deleted
     */
    static void deleteFile(final String pathToFile) {
        try {
            if (pathToFile != null && !pathToFile.isEmpty()
                    && Files.exists(java.nio.file.Paths.get(pathToFile))) {
                Files.delete(java.nio.file.Paths.get(pathToFile));
            }
        } catch (IOException | SecurityException e) {
            LOGGER.info(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_DELETE_FILE,
                    pathToFile,
                    e.getMessage()));
        }
    }

    /**
     * Parses each hocr file from the provided list, retrieves text, and
     * returns data in the format described below.
     *
     * @param inputFiles list of input files
     * @param textPositioning {@link TextPositioning}
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     * @throws IOException if error occurred during reading one the provided
     * files
     */
    static Map<Integer, List<TextInfo>> parseHocrFile(
            final List<File> inputFiles,
            final TextPositioning textPositioning)
            throws IOException {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();

        for (File inputFile : inputFiles) {
            if (inputFile != null
                    && Files.exists(
                            java.nio.file.Paths
                                    .get(inputFile.getAbsolutePath()))) {
                FileInputStream fileInputStream =
                        new FileInputStream(inputFile.getAbsolutePath());
                Document doc = Jsoup.parse(fileInputStream,
                        java.nio.charset.StandardCharsets.UTF_8.name(),
                        inputFile.getAbsolutePath());
                Elements pages = doc.getElementsByClass("ocr_page");

                Pattern bboxPattern = Pattern.compile(".*bbox(\\s+\\d+){4}.*");
                Pattern bboxCoordinatePattern = Pattern
                        .compile(
                                ".*\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*");
                List<String> searchedClasses = TextPositioning.BY_LINES
                        .equals(textPositioning)
                        ? Arrays.<String>asList("ocr_line", "ocr_caption")
                        : Collections.<String>singletonList("ocrx_word");
                for (Element page : pages) {
                    String[] pageNum = page.id().split("page_");
                    int pageNumber = Integer
                            .parseInt(pageNum[pageNum.length - 1]);
                    List<TextInfo> textData = new ArrayList<TextInfo>();
                    if (searchedClasses.size() > 0) {
                        Elements objects = page
                                .getElementsByClass(searchedClasses.get(0));
                        for (int i = 1; i < searchedClasses.size(); i++) {
                            Elements foundElements = page
                                    .getElementsByClass(
                                            searchedClasses.get(i));
                            for (int j = 0; j < foundElements.size(); j++) {
                                objects.add(foundElements.get(j));
                            }
                        }
                        for (Element obj : objects) {
                            String value = obj.attr("title");
                            Matcher bboxMatcher = bboxPattern.matcher(value);
                            if (bboxMatcher.matches()) {
                                Matcher bboxCoordinateMatcher =
                                        bboxCoordinatePattern
                                                .matcher(bboxMatcher.group());
                                if (bboxCoordinateMatcher.matches()) {
                                    List<Float> coordinates =
                                            new ArrayList<Float>();
                                    for (int i = 0; i < 4; i++) {
                                        String coord = bboxCoordinateMatcher
                                                .group(i + 1);
                                        coordinates
                                                .add(Float.parseFloat(coord));
                                    }

                                    textData.add(new TextInfo(obj.text(),
                                            coordinates));
                                }
                            }
                        }
                    }
                    if (textData.size() > 0) {
                        if (imageData.containsKey(pageNumber)) {
                            pageNumber = Collections.max(imageData.keySet())
                                    + 1;
                        }
                        imageData.put(pageNumber, textData);
                    }
                }
                fileInputStream.close();
            }
        }
        return imageData;
    }

    /**
     * Reads from text file to string.
     *
     * @param txtFile input {@link java.io.File} to be read
     * @return result {@link java.lang.String} from provided text file
     */
    static String readTxtFile(final File txtFile) {
        String content = null;
        try {
            content = new String(
                    Files.readAllBytes(txtFile.toPath()),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_FILE,
                    txtFile.getAbsolutePath(),
                    e.getMessage()));
        }
        return content;
    }

    /**
     * Writes provided {@link java.lang.String} to text file using
     * provided path.
     *
     * @param path path as {@link java.lang.String} to file to be created
     * @param data text data in required format as {@link java.lang.String}
     */
    static void writeToTextFile(final String path,
            final String data) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path),
                StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_WRITE_TO_FILE,
                    path,
                    e.getMessage()));
        }
    }
}
