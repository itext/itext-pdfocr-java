/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

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
package com.itextpdf.pdfocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.pdfocr.tesseract4.AbstractTesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LibOcrEngine;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.LeptonicaImageRotationHandler;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("IntegrationTest")
public class IntegrationTestHelper extends ExtendedITextTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IntegrationTestHelper.class);

    // directory with test files
    public static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TARGET_FOLDER = "./target/test/resources/com/itextpdf/pdfocr/";
    private static final String NON_ASCII_TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/ñoñ-ascîî/";

    // directory with trained data for tests
    protected static final String LANG_TESS_DATA_DIRECTORY = TEST_DIRECTORY + "tessdata";
    // directory with trained data for tests
    protected static final String SCRIPT_TESS_DATA_DIRECTORY = TEST_DIRECTORY + "tessdata" + File.separator + "script";
    // directory with trained data for tests
    protected static final String NON_ASCII_TESS_DATA_DIRECTORY = TEST_DIRECTORY + "tessdata" + File.separator + "ñoñ-ascîî";
    // directory with test image files
    protected static final String TEST_IMAGES_DIRECTORY = TEST_DIRECTORY + "images" + File.separator;
    // directory with fonts
    protected static final String TEST_FONTS_DIRECTORY = TEST_DIRECTORY + "fonts" + File.separator;
    // directory with fonts
    protected static final String TEST_DOCUMENTS_DIRECTORY = TEST_DIRECTORY + "documents" + File.separator;

    // path to font for hindi
    protected static final String NOTO_SANS_FONT_PATH = TEST_FONTS_DIRECTORY + "NotoSans-Regular.ttf";
    // path to font for thai
    protected static final String NOTO_SANS_THAI_FONT_PATH = TEST_FONTS_DIRECTORY + "NotoSansThai-Regular.ttf";
    // path to font for japanese
    protected static final String KOSUGI_FONT_PATH = TEST_FONTS_DIRECTORY + "Kosugi-Regular.ttf";
    // path to font for chinese
    protected static final String NOTO_SANS_SC_FONT_PATH = TEST_FONTS_DIRECTORY + "NotoSansSC-Regular.otf";
    // path to font for arabic
    protected static final String CAIRO_FONT_PATH = TEST_FONTS_DIRECTORY + "Cairo-Regular.ttf";
    // path to font for georgian
    protected static final String FREE_SANS_FONT_PATH = TEST_FONTS_DIRECTORY + "FreeSans.ttf";

    protected static final Map<String, String> FONT_PATH_TO_FONT_NAME_MAP;

    static {
        Map<String, String> fontPathToNameMap = new HashMap<>();
        fontPathToNameMap.put(NOTO_SANS_FONT_PATH, "NotoSans");
        fontPathToNameMap.put(NOTO_SANS_THAI_FONT_PATH, "NotoSansThai");
        fontPathToNameMap.put(KOSUGI_FONT_PATH, "Kosugi");
        fontPathToNameMap.put(NOTO_SANS_SC_FONT_PATH, "NotoSansSC");
        fontPathToNameMap.put(CAIRO_FONT_PATH, "Cairo");
        fontPathToNameMap.put(FREE_SANS_FONT_PATH, "FreeSans");
        FONT_PATH_TO_FONT_NAME_MAP = Collections.unmodifiableMap(fontPathToNameMap);
    }

    public enum ReaderType {
        LIB,
        EXECUTABLE
    }

    private static Tesseract4LibOcrEngine tesseractLibReader = null;
    private static Tesseract4ExecutableOcrEngine tesseractExecutableReader = null;

    public IntegrationTestHelper() {
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractLibReader = new Tesseract4LibOcrEngine(ocrEngineProperties);
        tesseractExecutableReader = new Tesseract4ExecutableOcrEngine(ocrEngineProperties);
    }

    protected static AbstractTesseract4OcrEngine getTesseractReader(ReaderType type) {
        if (type.equals(ReaderType.LIB)) {
            return tesseractLibReader;
        } else {
            return tesseractExecutableReader;
        }
    }

    protected static Tesseract4LibOcrEngine getTesseract4LibOcrEngine() {
        return tesseractLibReader;
    }

    /**
     * Returns target directory (because target/test could not exist).
     */
    public static String getTargetDirectory() {
        if (!Files.exists(java.nio.file.Paths.get(TARGET_FOLDER))) {
            createDestinationFolder(TARGET_FOLDER);
        }
        return TARGET_FOLDER;
    }

    /**
     * Returns a non ascii target directory.
     */
    public static String getNonAsciiTargetDirectory() {
        if (!Files.exists(java.nio.file.Paths.get(NON_ASCII_TARGET_DIRECTORY))) {
            createDestinationFolder(NON_ASCII_TARGET_DIRECTORY);
        }
        return NON_ASCII_TARGET_DIRECTORY;
    }

    protected static File getTessDataDirectory() {
        return new File(LANG_TESS_DATA_DIRECTORY);
    }

    /**
     * Retrieve text from specified page from given PDF document.
     */
    protected String getTextFromPdf(AbstractTesseract4OcrEngine tesseractReader,
                                    File file, int page, List<String> languages, List<String> fonts) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = getTargetDirectory() + getImageName(file.getAbsolutePath(), languages) + ".pdf";
            doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                    pdfPath, languages, fonts);
            result = getTextFromPdfLayer(pdfPath, null, page);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    /**
     * Retrieve text from specified page from given PDF document.
     */
    protected String getTextFromPdf(AbstractTesseract4OcrEngine tesseractReader,
                                    File file, int page, List<String> languages, String fontPath) {
        return getTextFromPdf(tesseractReader, file, page, languages,
                Collections.<String>singletonList(fontPath));
    }

    /**
     * Retrieve text from the first page of given PDF document setting font.
     */
    protected String getTextFromPdf(AbstractTesseract4OcrEngine tesseractReader, File file,
                                    List<String> languages, String fontPath) {
        return getTextFromPdf(tesseractReader, file, 1, languages, fontPath);
    }

    /**
     * Retrieve text from the first page of given PDF document.
     */
    protected String getTextFromPdf(AbstractTesseract4OcrEngine tesseractReader, File file,
                                    List<String> languages) {
        return getTextFromPdf(tesseractReader, file, 1, languages,
                new ArrayList<String>());
    }

    /**
     * Retrieve text from the required page of given PDF document.
     */
    protected String getTextFromPdf(AbstractTesseract4OcrEngine tesseractReader, File file, int page,
                                    List<String> languages) {
        return getTextFromPdf(tesseractReader, file, page, languages, new ArrayList<String>());
    }

    /**
     * Retrieve text from the first page of given PDF document.
     */
    protected String getTextFromPdf(AbstractTesseract4OcrEngine tesseractReader, File file) {
        return getTextFromPdf(tesseractReader, file, 1, null, new ArrayList<String>());
    }

    /**
     * Get text from layer specified by name from page.
     */
    protected String getTextFromPdfLayer(String pdfPath, String layerName,
                                         int page, boolean useActualText) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy textExtractionStrategy = new ExtractionStrategy(
                layerName);
        textExtractionStrategy.setUseActualText(useActualText);
        PdfCanvasProcessor processor = new PdfCanvasProcessor(
                textExtractionStrategy);
        processor.processPageContent(pdfDocument.getPage(page));

        pdfDocument.close();
        return textExtractionStrategy.getResultantText();
    }

    /**
     * Get text from layer specified by name from page.
     */
    protected String getTextFromPdfLayer(String pdfPath, String layerName,
                                         int page) throws IOException {
        return getTextFromPdfLayer(pdfPath, layerName, page, false);
    }

    /**
     * Get text from layer specified by name from page
     * removing unnecessary space that were added after each glyph in
     * {@link LocationTextExtractionStrategy#getResultantText()}.
     */
    protected String getTextFromPdfLayerUsingActualText(String pdfPath,
                                                        String layerName, int page) throws IOException {
        return getTextFromPdfLayer(pdfPath, layerName, page, true)
                .replace(" ", "");
    }

    /**
     * Perform OCR using provided path to image (imgPath),
     * save to file and get text from file.
     */
    protected String getRecognizedTextFromTextFile(
            AbstractTesseract4OcrEngine tesseractReader, String input,
            List<String> languages) {
        String result = null;
        String txtPath = null;
        try {
            txtPath = getTargetDirectory()
                    + getImageName(input, languages) + ".txt";
            doOcrAndSaveToTextFile(tesseractReader, input, txtPath, languages);
            result = getTextFromTextFile(new File(txtPath));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    /**
     * Perform OCR using provided path to image (imgPath),
     * save to file and get text from file.
     */
    protected String getRecognizedTextFromTextFile(
            AbstractTesseract4OcrEngine tesseractReader, String input) {
        return getRecognizedTextFromTextFile(tesseractReader, input, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result to text file.
     */
    protected void doOcrAndSaveToTextFile(
            AbstractTesseract4OcrEngine tesseractReader, String imgPath,
            String txtPath, List<String> languages) {
        if (languages != null) {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setLanguages(languages);
            tesseractReader.setTesseract4OcrEngineProperties(properties);
        }

        tesseractReader.createTxtFile(Collections.<File>singletonList(new File(imgPath)),
                new File(txtPath));

        if (languages != null) {
            Assertions.assertEquals(languages.size(),
                    tesseractReader.getTesseract4OcrEngineProperties().getLanguages().size());
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result PDF document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(
            AbstractTesseract4OcrEngine tesseractReader, String imgPath,
            String pdfPath, List<String> languages,
            List<String> fonts, com.itextpdf.kernel.colors.Color color) {
        doOcrAndSavePdfToPath(tesseractReader,
                imgPath, pdfPath,
                languages, fonts, color, false);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result PDF document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(
            AbstractTesseract4OcrEngine tesseractReader, String imgPath,
            String pdfPath, List<String> languages,
            List<String> fonts, com.itextpdf.kernel.colors.Color color, boolean applyRotation) {
        if (languages != null) {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setLanguages(languages);
            tesseractReader.setTesseract4OcrEngineProperties(properties);
        }

        OcrPdfCreatorProperties properties =  new OcrPdfCreatorProperties();
        properties.setPdfLang("en-US");
        properties.setTitle("");
        if (applyRotation) {
            properties.setImageRotationHandler(new LeptonicaImageRotationHandler());
        }

        if (fonts != null && fonts.size() > 0) {
            FontProvider fontProvider = new FontProvider();
            for (String fontPath : fonts) {
                String name = FONT_PATH_TO_FONT_NAME_MAP.get(fontPath);
                fontProvider.getFontSet().addFont(fontPath, PdfEncodings.IDENTITY_H, name);
            }
            properties.setFontProvider(fontProvider);
        }
        if (color != null) {
            properties.setTextColor(color);
        }
        if (languages != null) {
            Assertions.assertEquals(languages.size(),
                    tesseractReader.getTesseract4OcrEngineProperties().getLanguages().size());
        }

        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(tesseractReader, properties);
        try (PdfWriter pdfWriter = getPdfWriter(pdfPath)) {
            PdfDocument doc = ocrPdfCreator.createPdf(
                    Collections.<File>singletonList(new File(imgPath)),
                    pdfWriter);

            Assertions.assertNotNull(doc);
            doc.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result PDF document to "pdfPath".
     */
    protected void doOcrAndSavePdfToPath(
            AbstractTesseract4OcrEngine tesseractReader, String imgPath,
            String pdfPath, List<String> languages,
            com.itextpdf.kernel.colors.Color color) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, null, color);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result PDF document to "pdfPath".
     * (Text will be invisible)
     */
    protected void doOcrAndSavePdfToPath(AbstractTesseract4OcrEngine tesseractReader, String imgPath,
                                         String pdfPath, List<String> languages, List<String> fonts) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, fonts, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result PDF document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(
            AbstractTesseract4OcrEngine tesseractReader, String imgPath,
            String pdfPath) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath, null,
                null, null);
    }

    /**
     * Retrieve text from given txt file.
     */
    protected String getTextFromTextFile(File file) {
        String content = null;
        try {
            content = new String(
                    Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_FILE,
                    file.getAbsolutePath(),
                    e.getMessage()));
        }
        return content;
    }

    /**
     * Create pdfWriter using provided path to destination file.
     */
    protected PdfWriter getPdfWriter(String pdfPath) throws IOException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Gets image name from path.
     */
    protected String getImageName(String path, List<String> languages) {
        String lang = (languages != null && languages.size() > 0) ?
                "_" + String.join("", languages) : "";
        String img = path
                .substring(path.lastIndexOf(java.io.File.separator))
                .substring(1)
                .replace(".", "_");
        return img + lang;
    }

    public static class ExtractionStrategy extends LocationTextExtractionStrategy {
        private com.itextpdf.kernel.geom.Rectangle imageBBoxRectangle;
        private com.itextpdf.kernel.colors.Color fillColor;
        private String layerName;
        private PdfFont pdfFont;

        public ExtractionStrategy(String name) {
            super();
            layerName = name;
        }

        public void setFillColor(com.itextpdf.kernel.colors.Color color) {
            fillColor = color;
        }

        public com.itextpdf.kernel.colors.Color getFillColor() {
            return fillColor;
        }

        public void setPdfFont(PdfFont font) {
            pdfFont = font;
        }

        public PdfFont getPdfFont() {
            return pdfFont;
        }

        public com.itextpdf.kernel.geom.Rectangle getImageBBoxRectangle() { return this.imageBBoxRectangle; }

        public void setImageBBoxRectangle(com.itextpdf.kernel.geom.Rectangle imageBBoxRectangle) {
            this.imageBBoxRectangle = imageBBoxRectangle;
        }

        @Override
        protected boolean isChunkAtWordBoundary(TextChunk chunk,
                                                TextChunk previousChunk) {
            ITextChunkLocation curLoc = chunk.getLocation();
            ITextChunkLocation prevLoc = previousChunk.getLocation();

            if (curLoc.getStartLocation().equals(curLoc.getEndLocation()) ||
                    prevLoc.getEndLocation()
                            .equals(prevLoc.getStartLocation())) {
                return false;
            }

            return curLoc.distParallelEnd() - prevLoc.distParallelStart() >
                    (curLoc.getCharSpaceWidth() + prevLoc.getCharSpaceWidth())
                            / 2.0f;
        }

        @Override
        public void eventOccurred(IEventData data, EventType type) {
            if (type.equals(EventType.RENDER_TEXT) || type.equals(EventType.RENDER_IMAGE)) {
                String tagName = getTagName(data, type);
                if ((tagName == null && layerName == null) || (layerName != null && layerName.equals(tagName))) {
                    if (type.equals(EventType.RENDER_TEXT)) {
                        TextRenderInfo renderInfo = (TextRenderInfo) data;
                        setFillColor(renderInfo.getGraphicsState()
                                .getFillColor());
                        setPdfFont(renderInfo.getGraphicsState().getFont());
                        super.eventOccurred(data, type);
                    }
                    else if (type.equals(EventType.RENDER_IMAGE)) {
                        ImageRenderInfo renderInfo = (ImageRenderInfo) data;
                        com.itextpdf.kernel.geom.Matrix ctm = renderInfo.getImageCtm();
                        setImageBBoxRectangle(new com.itextpdf.kernel.geom.Rectangle(ctm.get(6), ctm.get(7),
                                ctm.get(0), ctm.get(4)));
                    }
                }
            }
        }

        private String getTagName(IEventData data, EventType type) {
            java.util.List<CanvasTag> tagHierarchy = null;
            if (type.equals(EventType.RENDER_TEXT)) {
                TextRenderInfo textRenderInfo = (TextRenderInfo) data;
                tagHierarchy = textRenderInfo.getCanvasTagHierarchy();
            }
            else if (type.equals(EventType.RENDER_IMAGE)) {
                ImageRenderInfo imageRenderInfo = (ImageRenderInfo) data;
                tagHierarchy = imageRenderInfo.getCanvasTagHierarchy();
            }
            return (tagHierarchy == null || tagHierarchy.size() == 0
                    || tagHierarchy.get(0).getProperties().get(PdfName.Name) == null)
                    ? null
                    : tagHierarchy.get(0).getProperties().get(PdfName.Name).toString();
        }
    }
}
