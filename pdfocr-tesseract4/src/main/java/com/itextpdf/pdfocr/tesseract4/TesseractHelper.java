/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.io.util.SystemUtil;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import com.itextpdf.styledxmlparser.jsoup.nodes.Document;
import com.itextpdf.styledxmlparser.jsoup.nodes.Element;
import com.itextpdf.styledxmlparser.jsoup.nodes.Node;
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
public class TesseractHelper {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractHelper.class);

    /**
     * Patterns for matching hOCR element bboxes.
     */
    private static final Pattern BBOX_PATTERN = Pattern.compile(".*bbox(\\s+\\d+){4}.*");
    private static final Pattern BBOX_COORDINATE_PATTERN = Pattern
            .compile(
                    ".*\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*");

    /**
     * Indices in array representing bbox.
     */
    private static final int LEFT_IDX = 0;
    private static final int BOTTOM_IDX = 1;
    private static final int RIGHT_IDX = 2;
    private static final int TOP_IDX = 3;

    /**
     * Size of the array containing bbox.
     */
    private static final int BBOX_ARRAY_SIZE = 4;

    /**
     * Creates a new {@link TesseractHelper} instance.
     */
    private TesseractHelper() {
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
    public static Map<Integer, List<TextInfo>> parseHocrFile(
            final List<File> inputFiles,
            final TextPositioning textPositioning)
            throws IOException {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();
        Map<String, Node> unparsedBBoxes = new LinkedHashMap<>();

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
                            List<Float> coordinates = getAlignedBBox(obj,
                                    textPositioning,
                                    unparsedBBoxes);
                            textData.add(new TextInfo(obj.text(),
                                    coordinates));
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
        for (Node node : unparsedBBoxes.values()) {
            LOGGER.warn(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_PARSE_NODE_BBOX,
                    node.toString()
            ));
        }
        return imageData;
    }

    /**
     * Get and align (if needed) bbox of the element.
     */
    static List<Float> getAlignedBBox(Element object,
                                      TextPositioning textPositioning,
                                      Map<String, Node> unparsedBBoxes) {
        final List<Float> coordinates = parseBBox(object, unparsedBBoxes);
        if (TextPositioning.BY_WORDS_AND_LINES == textPositioning
                || TextPositioning.BY_WORDS == textPositioning) {
            Node line = object.parent();
            final List<Float> lineCoordinates = parseBBox(line, unparsedBBoxes);
            if (TextPositioning.BY_WORDS_AND_LINES == textPositioning) {
                coordinates.set(BOTTOM_IDX, lineCoordinates.get(BOTTOM_IDX));
                coordinates.set(TOP_IDX, lineCoordinates.get(TOP_IDX));
            }
            detectAndFixBrokenBBoxes(object, coordinates,
                    lineCoordinates, unparsedBBoxes);
        }
        return coordinates;
    }

    /**
     * Parses element bbox.
     *
     * @param node element containing bbox
     * @param unparsedBBoxes list of element ids with bboxes which could not be parsed
     * @return parsed bbox
     */
    static List<Float> parseBBox(Node node, Map<String, Node> unparsedBBoxes) {
        List<Float> bbox = new ArrayList<>();
        Matcher bboxMatcher = BBOX_PATTERN.matcher(node.attr("title"));
        if (bboxMatcher.matches()) {
            Matcher bboxCoordinateMatcher =
                    BBOX_COORDINATE_PATTERN
                            .matcher(bboxMatcher.group());
            if (bboxCoordinateMatcher.matches()) {
                for (int i = 0; i < BBOX_ARRAY_SIZE; i++) {
                    String coord = bboxCoordinateMatcher
                            .group(i + 1);
                    bbox.add(Float.parseFloat(coord));
                }
            }
        }
        if (bbox.size() == 0) {
            bbox = Arrays.asList(0f, 0f, 0f, 0f);
            String id = node.attr("id");
            if (id != null && !unparsedBBoxes.containsKey(id)) {
                unparsedBBoxes.put(id, node);
            }
        }
        return bbox;
    }

    /**
     * Sometimes hOCR file contains broke character bboxes which are equal to page bbox.
     * This method attempts to detect and fix them.
     */
    static void detectAndFixBrokenBBoxes(Element object, List<Float> coordinates,
                                         List<Float> lineCoordinates,
                                         Map<String, Node> unparsedBBoxes) {
        if (coordinates.get(LEFT_IDX) < lineCoordinates.get(LEFT_IDX)
                || coordinates.get(LEFT_IDX) > lineCoordinates.get(RIGHT_IDX)) {
            if (object.previousElementSibling() == null) {
                coordinates.set(LEFT_IDX, lineCoordinates.get(LEFT_IDX));
            } else {
                Element sibling = object.previousElementSibling();
                List<Float> siblingBBox = parseBBox(sibling, unparsedBBoxes);
                coordinates.set(LEFT_IDX, siblingBBox.get(RIGHT_IDX));
            }
        }
        if (coordinates.get(RIGHT_IDX) > lineCoordinates.get(RIGHT_IDX)
                || coordinates.get(RIGHT_IDX) < lineCoordinates.get(LEFT_IDX)) {
            if (object.nextElementSibling() == null) {
                coordinates.set(RIGHT_IDX, lineCoordinates.get(RIGHT_IDX));
            } else {
                Element sibling = object.nextElementSibling();
                List<Float> siblingBBox = parseBBox(sibling, unparsedBBoxes);
                coordinates.set(RIGHT_IDX, siblingBBox.get(LEFT_IDX));
            }
        }
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

    /**
     * Runs given command.
     *
     * @param execPath path to the executable
     * @param paramsList {@link java.util.List} of command line arguments
     * @throws Tesseract4OcrException if provided command failed
     */
    static void runCommand(final String execPath,
                           final List<String> paramsList) throws Tesseract4OcrException {
        try {
            String params = String.join(" ", paramsList);
            boolean cmdSucceeded = SystemUtil
                    .runProcessAndWait(execPath, params);

            if (!cmdSucceeded) {
                LOGGER.error(MessageFormatUtil
                        .format(Tesseract4LogMessageConstant.COMMAND_FAILED,
                                execPath + " " + params));
                throw new Tesseract4OcrException(
                        Tesseract4OcrException
                                .TESSERACT_FAILED);
            }
        } catch (IOException | InterruptedException e) { // NOSONAR
            LOGGER.error(MessageFormatUtil
                    .format(Tesseract4LogMessageConstant.COMMAND_FAILED,
                            e.getMessage()));
            throw new Tesseract4OcrException(
                    Tesseract4OcrException
                            .TESSERACT_FAILED);
        }
    }
}