/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.commons.utils.SystemUtil;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.pdfocr.TextInfo;
import com.itextpdf.pdfocr.tesseract4.exceptions.Tesseract4OcrException;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
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
    private static final Pattern WCONF_PATTERN = Pattern.compile("^.*(x_wconf *\\d+).*$");

    /**
     * Size of the array containing bbox.
     */
    private static final int BBOX_ARRAY_SIZE = 4;

    /**
     * Indices in array representing bbox.
     */
    private static final int LEFT_IDX = 0;
    private static final int TOP_IDX = 1;
    private static final int RIGHT_IDX = 2;
    private static final int BOTTOM_IDX = 3;

    /**
     * The Constant to convert pixels to points.
     */
    private static final float PX_TO_PT = 3F / 4F;

    private static final String NEW_LINE_PATTERN = "\n+";
    private static final String SPACE_PATTERN = " +";
    private static final String NEW_LINE_OR_SPACE_PATTERN = "[\n ]+";
    private static final String PAGE_PREFIX_PATTERN = "page_";

    private static final String OCR_PAGE = "ocr_page";
    private static final String OCR_LINE = "ocr_line";
    private static final String OCR_CAPTION = "ocr_caption";
    private static final String OCRX_WORD = "ocrx_word";
    private static final String TITLE = "title";
    private static final String X_WCONF = "x_wconf";


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
     * @param txtInputFiles list of input files in txt format used to make hocr recognition result more precise.
     *                      This is needed for cases of Thai language or some Chinese dialects
     *                      where every character is interpreted as a single word.
     *                      For more information see https://github.com/tesseract-ocr/tesseract/issues/2702
     * @param tesseract4OcrEngineProperties {@link Tesseract4OcrEngineProperties}
     * @return {@link java.util.Map} where key is {@link java.lang.Integer}
     * representing the number of the page and value is
     * {@link java.util.List} of {@link TextInfo} elements where each
     * {@link TextInfo} element contains a word or a line and its 4
     * coordinates(bbox)
     * @throws IOException if error occurred during reading one the provided
     * files
     */
    static Map<Integer, List<TextInfo>> parseHocrFile(
            final List<File> inputFiles, final List<File> txtInputFiles,
            final Tesseract4OcrEngineProperties tesseract4OcrEngineProperties)
            throws IOException {
        Map<Integer, List<TextInfo>> imageData =
                new LinkedHashMap<Integer, List<TextInfo>>();
        Map<String, Node> unparsedBBoxes = new LinkedHashMap<>();

        for (int inputFileIdx = 0; inputFileIdx < inputFiles.size(); inputFileIdx++) {
            final File inputFile = inputFiles.get(inputFileIdx);
            List<String> txt = null;
            if (txtInputFiles != null) {
                final File txtInputFile = txtInputFiles.get(inputFileIdx);
                txt = Files.readAllLines(txtInputFile.toPath(), StandardCharsets.UTF_8);
            }
            if (inputFile != null
                    && Files.exists(
                    java.nio.file.Paths
                            .get(inputFile.getAbsolutePath()))) {
                FileInputStream fileInputStream =
                        new FileInputStream(inputFile.getAbsolutePath());
                Document doc = Jsoup.parse(fileInputStream,
                        java.nio.charset.StandardCharsets.UTF_8.name(),
                        inputFile.getAbsolutePath());
                Elements pages = doc.getElementsByClass(OCR_PAGE);

                for (Element page : pages) {
                    String[] pageNum = page.id().split(PAGE_PREFIX_PATTERN);
                    int pageNumber = Integer
                            .parseInt(pageNum[pageNum.length - 1]);
                    final List<TextInfo> textData = getTextData(page,
                            tesseract4OcrEngineProperties,
                            txt,
                            unparsedBBoxes);
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
    static Rectangle getAlignedBBox(Element object,
                                      TextPositioning textPositioning,
                                      Rectangle pageBbox,
                                      Map<String, Node> unparsedBBoxes) {
        final Rectangle box = parseBBox(object, pageBbox, unparsedBBoxes);
        if (TextPositioning.BY_WORDS_AND_LINES == textPositioning
                || TextPositioning.BY_WORDS == textPositioning) {
            Node line = object.parent();
            final Rectangle lineBbox = parseBBox(line, pageBbox, unparsedBBoxes);
            if (TextPositioning.BY_WORDS_AND_LINES == textPositioning) {
                box.setBbox(box.getLeft(),
                        lineBbox.getBottom(),
                        box.getRight(),
                        lineBbox.getTop());
            }
            detectAndFixBrokenBBoxes(object, box,
                    lineBbox, pageBbox, unparsedBBoxes);
        }
        return box;
    }

    /**
     * Parses element bbox.
     *
     * @param node element containing bbox
     * @param pageBBox element containing parent page bbox
     * @param unparsedBBoxes list of element ids with bboxes which could not be parsed
     * @return parsed bbox
     */
    static Rectangle parseBBox(Node node, Rectangle pageBBox, Map<String, Node> unparsedBBoxes) {
        List<Float> bbox = new ArrayList<>();
        Matcher bboxMatcher = BBOX_PATTERN.matcher(node.attr(TITLE));
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
        if (pageBBox == null) {
            return new Rectangle(toPoints(bbox.get(LEFT_IDX)),
                    toPoints(bbox.get(TOP_IDX)),
                    toPoints(bbox.get(RIGHT_IDX)),
                    toPoints(bbox.get(BOTTOM_IDX) - bbox.get(TOP_IDX)));
        } else {
            return new Rectangle(0, 0).setBbox(toPoints(bbox.get(LEFT_IDX)),
                    pageBBox.getTop() - toPoints(bbox.get(TOP_IDX)),
                    toPoints(bbox.get(RIGHT_IDX)),
                    pageBBox.getTop() - toPoints(bbox.get(BOTTOM_IDX)));
        }
    }

    /**
     * Sometimes hOCR file contains broke character bboxes which are equal to page bbox.
     * This method attempts to detect and fix them.
     */
    static void detectAndFixBrokenBBoxes(Element object, Rectangle bbox,
                                         Rectangle lineBbox, Rectangle pageBbox,
                                         Map<String, Node> unparsedBBoxes) {
        if (bbox.getLeft() < lineBbox.getLeft()
                || bbox.getLeft() > lineBbox.getRight()) {
            if (object.previousElementSibling() == null) {
                bbox.setX(lineBbox.getLeft());
            } else {
                Element sibling = object.previousElementSibling();
                final Rectangle siblingBBox = parseBBox(sibling, pageBbox, unparsedBBoxes);
                bbox.setX(siblingBBox.getRight());
            }
        }
        if (bbox.getRight() > lineBbox.getRight()
                || bbox.getRight() < lineBbox.getLeft()) {
            if (object.nextElementSibling() == null) {
                bbox.setBbox(bbox.getLeft(),
                        bbox.getBottom(),
                        lineBbox.getRight(),
                        bbox.getTop());
            } else {
                Element sibling = object.nextElementSibling();
                final Rectangle siblingBBox = parseBBox(sibling, pageBbox, unparsedBBoxes);
                bbox.setBbox(bbox.getLeft(),
                        bbox.getBottom(),
                        siblingBBox.getLeft(),
                        bbox.getTop());
            }
        }
    }

    /**
     * Converts points to pixels.
     */
    static float toPixels(float pt) {
        return pt / PX_TO_PT;
    }

    /**
     * Converts pixels to points.
     */
    static float toPoints(float px) {
        return px * PX_TO_PT;
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
        runCommand(execPath, paramsList, null);
    }

    /**
     * Runs given command from the specific working directory.
     *
     * @param execPath path to the executable
     * @param paramsList {@link java.util.List} of command line arguments
     * @param workingDirPath path to the working directory
     * @throws Tesseract4OcrException if provided command failed
     */
    static void runCommand(final String execPath,
                           final List<String> paramsList,
                           final String workingDirPath) throws Tesseract4OcrException {
        try {
            String params = String.join(" ", paramsList);
            boolean cmdSucceeded = SystemUtil
                    .runProcessAndWait(execPath, params, workingDirPath);

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

    /**
     * Gets list of text infos from hocr page.
     */
    private static List<TextInfo> getTextData(Element page,
                                              Tesseract4OcrEngineProperties tesseract4OcrEngineProperties,
                                              List<String> txt,
                                              Map<String, Node> unparsedBBoxes) {
        final Rectangle pageBbox = parseBBox(page, null, unparsedBBoxes);
        final List<String> searchedClasses = Arrays.<String>asList(OCR_LINE, OCR_CAPTION);
        Elements objects = new Elements();
        for (int i = 0; i < searchedClasses.size(); i++) {
            Elements foundElements = page
                    .getElementsByClass(
                            searchedClasses.get(i));
            for (int j = 0; j < foundElements.size(); j++) {
                objects.add(foundElements.get(j));
            }
        }
        return getTextData(objects,
                tesseract4OcrEngineProperties,
                txt,
                pageBbox,
                unparsedBBoxes);
    }

    /**
     * Gets list of text infos from elements within hocr page.
     */
    private static List<TextInfo> getTextData(List<Element> pageObjects,
                                              Tesseract4OcrEngineProperties tesseract4OcrEngineProperties,
                                              List<String> txt,
                                              Rectangle pageBbox,
                                              Map<String, Node> unparsedBBoxes) {
        List<TextInfo> textData = new ArrayList<TextInfo>();
        for (Element lineOrCaption : pageObjects) {
            if (!lineOrCaption.text().isEmpty() && isElementConfident(lineOrCaption,
                    tesseract4OcrEngineProperties.getMinimalConfidenceLevel())) {
                String hocrLineInTxt = findHocrLineInTxt(lineOrCaption, txt);
                if (tesseract4OcrEngineProperties.getTextPositioning() == TextPositioning.BY_WORDS
                        || tesseract4OcrEngineProperties.getTextPositioning() == TextPositioning.BY_WORDS_AND_LINES) {
                    for (TextInfo ti : getTextDataForWords(lineOrCaption,
                            hocrLineInTxt,
                            tesseract4OcrEngineProperties.getTextPositioning(),
                            pageBbox,
                            unparsedBBoxes)) {
                        textData.add(ti);
                    }
                } else {
                    for (TextInfo ti : getTextDataForLines(lineOrCaption,
                            hocrLineInTxt,
                            pageBbox,
                            unparsedBBoxes)) {
                        textData.add(ti);
                    }
                }
            }
        }
        return textData;
    }

    /**
     * Decides if <code>lineOrCaption</code> is confident or not given into account
     * minimalConfidenceLevel property of {@link Tesseract4OcrEngineProperties}.
     */
    private static boolean isElementConfident(Element lineOrCaption, int minimalConfidenceLevel) {
        if (minimalConfidenceLevel == 0) {
            return true;
        } else {
            int wconfTotal = 0;
            int wconfCount = 0;
            for (Node node : lineOrCaption.childNodes()) {
                if (node instanceof Element) {
                    String title = ((Element)node).attr(TITLE);
                    Matcher matcher = WCONF_PATTERN.matcher(title);
                    if (matcher.matches()) {
                        String wconf = null;
                        try {
                            wconf = matcher.group(1);
                        } catch (Exception e) {
                            //No need to do anything here
                        }
                        if (wconf != null) {
                            wconf = wconf.replaceAll(X_WCONF, "").trim();
                            wconfTotal += Integer.parseInt(wconf);
                            wconfCount++;
                        }
                    }
                }
            }
            if (wconfCount > 0) {
                return wconfTotal / wconfCount >= minimalConfidenceLevel;
            } else {
                return true;
            }
        }
    }

    /**
     * Gets list of words represented by text infos from hocr line.
     */
    private static List<TextInfo> getTextDataForWords(Element lineOrCaption,
                                                      String txtLine,
                                                      TextPositioning textPositioning,
                                                      Rectangle pageBbox,
                                                      Map<String, Node> unparsedBBoxes) {
        List<TextInfo> textData = new ArrayList<TextInfo>();
        if (txtLine == null) {
            for (Element word : lineOrCaption.getElementsByClass(OCRX_WORD)) {
                final Rectangle bboxRect = getAlignedBBox(word,
                        textPositioning, pageBbox,
                        unparsedBBoxes);
                addToTextData(textData, word.text(), bboxRect);
            }
        } else {
            List<TextInfo> textInfos = new ArrayList<>();
            final String txtLine1 = txtLine.replaceAll(NEW_LINE_PATTERN, "");
            final String txtLine2 = txtLine1.replaceAll(SPACE_PATTERN, " ");
            String[] lineItems = txtLine2.split(" ");
            for (Element word : lineOrCaption.getElementsByClass(OCRX_WORD)) {
                final Rectangle bboxRect = getAlignedBBox(word,
                        textPositioning, pageBbox,
                        unparsedBBoxes);
                textInfos.add(new TextInfo(word.text(),
                        bboxRect));
                if (lineItems[0].replaceAll(NEW_LINE_OR_SPACE_PATTERN, "")
                        .equals(getTextInfosText(textInfos).replaceAll(SPACE_PATTERN, ""))) {
                    lineItems = Arrays.copyOfRange(lineItems, 1, lineItems.length);
                    addToTextData(textData, mergeTextInfos(textInfos));
                    textInfos.clear();
                }
            }
        }
        return textData;
    }

    /**
     * Gets list of lines represented by text infos from hocr line.
     */
    private static List<TextInfo> getTextDataForLines(Element lineOrCaption,
                                                      String txtLine,
                                                      Rectangle pageBbox,
                                                      Map<String, Node> unparsedBBoxes) {
        List<TextInfo> textData = new ArrayList<TextInfo>();
        final Rectangle bboxRect = getAlignedBBox(lineOrCaption,
                TextPositioning.BY_LINES, pageBbox,
                unparsedBBoxes);
        if (txtLine == null) {
            addToTextData(textData, lineOrCaption.text(), bboxRect);
        } else {
            addToTextData(textData, txtLine, bboxRect);
        }
        return textData;
    }

    /**
     * Add text chunk represented by text and bbox to list of text infos.
     */
    private static void addToTextData(List<TextInfo> textData,
                                      String text,
                                      Rectangle bboxRect) {
        final TextInfo textInfo = new TextInfo(text, bboxRect);
        textData.add(textInfo);
    }

    /**
     * Add text chunk represented by text info to list of text infos.
     */
    private static void addToTextData(List<TextInfo> textData,
                                      TextInfo textInfo) {
        String text = textInfo.getText();
        Rectangle bboxRect = textInfo.getBboxRect();
        addToTextData(textData, text, bboxRect);
    }

    /**
     * Gets common text for list of text infos.
     */
    private static String getTextInfosText(List<TextInfo> textInfos) {
        StringBuilder text = new StringBuilder();
        for (TextInfo textInfo : textInfos) {
            text.append(textInfo.getText());
        }
        return text.toString();
    }

    /**
     * Merges text infos.
     *
     * @param textInfos source to merge
     * @return merged text info
     */
    private static TextInfo mergeTextInfos(List<TextInfo> textInfos) {
        TextInfo textInfo = new TextInfo(textInfos.get(0));
        for (int i = 1; i < textInfos.size(); i++) {
            textInfo.setText(textInfo.getText() + textInfos.get(i).getText());
            Rectangle leftBBox = textInfo.getBboxRect();
            Rectangle rightBBox = textInfos.get(i).getBboxRect();
            textInfo.setBboxRect(new Rectangle(0, 0).setBbox(
                    leftBBox.getLeft(),
                    Math.min(leftBBox.getBottom(), rightBBox.getBottom()),
                    rightBBox.getRight(),
                    Math.max(leftBBox.getTop(), rightBBox.getTop())
            ));
        }
        return textInfo;
    }

    /**
     * Attempts to find HOCR line text in provided TXT.
     *
     * @return text line if found, otherwise null
     */
    private static String findHocrLineInTxt(Element line, List<String> txt) {
        if (txt == null) {
            return null;
        }
        String hocrLineText = line.text().replaceAll(SPACE_PATTERN, "");
        if (hocrLineText.isEmpty()) {
            return null;
        }
        for (String txtLine : txt) {
            if (txtLine.replaceAll(SPACE_PATTERN, "").equals(hocrLineText)) {
                return txtLine;
            }
        }
        return null;
    }
}
