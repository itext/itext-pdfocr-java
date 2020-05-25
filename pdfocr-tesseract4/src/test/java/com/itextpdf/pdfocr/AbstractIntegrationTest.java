package com.itextpdf.pdfocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
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
import com.itextpdf.pdfocr.tesseract4.Tesseract4ExecutableOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LibOcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4LogMessageConstant;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngine;
import com.itextpdf.pdfocr.tesseract4.Tesseract4OcrEngineProperties;
import com.itextpdf.pdfocr.tesseract4.TextPositioning;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(IntegrationTest.class)
public class AbstractIntegrationTest extends ExtendedITextTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractIntegrationTest.class);

    // directory with test files
    public static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TARGET_FOLDER = "./target/test/resources/com/itextpdf/pdfocr/";

    // directory with trained data for tests
    protected static String langTessDataDirectory = null;
    // directory with trained data for tests
    protected static String scriptTessDataDirectory = null;
    // directory with test image files
    protected static String testImagesDirectory = null;
    // directory with fonts
    protected static String testFontsDirectory = null;
    // directory with fonts
    protected static String testDocumentsDirectory = null;
    // path to default cmyk color profile
    protected static String defaultCMYKColorProfilePath = null;
    // path to default rgb color profile
    protected static String defaultRGBColorProfilePath = null;

    // path to font for hindi
    protected static String notoSansFontPath = testFontsDirectory + "NotoSans-Regular.ttf";
    // path to font for japanese
    protected static String kosugiFontPath = testFontsDirectory + "Kosugi-Regular.ttf";
    // path to font for chinese
    protected static String notoSansSCFontPath = testFontsDirectory + "NotoSansSC-Regular.otf";
    // path to font for arabic
    protected static String cairoFontPath = testFontsDirectory + "Cairo-Regular.ttf";
    // path to font for georgian
    protected static String freeSansFontPath = testFontsDirectory + "FreeSans.ttf";

    protected static float delta = 1e-4f;

    public enum ReaderType {
        LIB,
        EXECUTABLE
    }

    static Tesseract4LibOcrEngine tesseractLibReader = null;
    static Tesseract4ExecutableOcrEngine tesseractExecutableReader = null;

    @Before
    public void initTesseractProperties() {
        Tesseract4OcrEngineProperties properties =
                new Tesseract4OcrEngineProperties();
        properties.setPreprocessingImages(true);
        properties.setPathToTessData(getTessDataDirectory());
        properties.setLanguages(new ArrayList<String>());
        properties.setUserWords("eng", new ArrayList<String>());
        properties.setTextPositioning(TextPositioning.BY_LINES);
        if (tesseractLibReader != null) {
            tesseractLibReader.setTesseract4OcrEngineProperties(properties);
        }
        if (tesseractExecutableReader != null) {
            tesseractExecutableReader
                    .setPathToExecutable(getTesseractDirectory());
            tesseractExecutableReader
                    .setTesseract4OcrEngineProperties(properties);
        }
    }

    @Test
    public void testSimpleTextOutput() {
        String imgPath = testImagesDirectory + "numbers_01.jpg";
        String expectedOutput = "619121";

        Assert.assertTrue(
                getRecognizedTextFromTextFile(tesseractExecutableReader, imgPath)
                        .contains(expectedOutput));
        Assert.assertTrue(
                getRecognizedTextFromTextFile(tesseractExecutableReader, imgPath)
                        .contains(expectedOutput));
    }

    public AbstractIntegrationTest() {
        setResourceDirectories();
        Tesseract4OcrEngineProperties ocrEngineProperties =
                new Tesseract4OcrEngineProperties();
        ocrEngineProperties.setPathToTessData(getTessDataDirectory());
        tesseractLibReader = new Tesseract4LibOcrEngine(ocrEngineProperties);
        tesseractExecutableReader = new Tesseract4ExecutableOcrEngine(
                    getTesseractDirectory(), ocrEngineProperties);
    }

    static void setResourceDirectories() {
        if (testImagesDirectory == null) {
            testImagesDirectory = TEST_DIRECTORY + "images" + java.io.File.separatorChar;
        }
        if (langTessDataDirectory == null) {
            langTessDataDirectory = TEST_DIRECTORY + "tessdata";
        }
        if (scriptTessDataDirectory == null) {
            scriptTessDataDirectory = TEST_DIRECTORY + "tessdata" + java.io.File.separatorChar + "script";
        }
        if (testFontsDirectory == null) {
            testFontsDirectory = TEST_DIRECTORY + "fonts" + java.io.File.separatorChar;
            updateFonts();
        }
        if (testDocumentsDirectory == null) {
            testDocumentsDirectory = TEST_DIRECTORY + "documents" + java.io.File.separatorChar;
        }
        if (defaultCMYKColorProfilePath == null) {
            defaultCMYKColorProfilePath = TEST_DIRECTORY + "profiles/CoatedFOGRA27.icc";
        }
        if (defaultRGBColorProfilePath == null) {
            defaultRGBColorProfilePath = TEST_DIRECTORY + "profiles/sRGB_CS_profile.icm";
        }
    }

    static void updateFonts() {
        notoSansFontPath = testFontsDirectory + "NotoSans-Regular.ttf";
        kosugiFontPath = testFontsDirectory + "Kosugi-Regular.ttf";
        notoSansSCFontPath = testFontsDirectory + "NotoSansSC-Regular.otf";
        cairoFontPath = testFontsDirectory + "Cairo-Regular.ttf";
        freeSansFontPath = testFontsDirectory + "FreeSans.ttf";
    }

    protected static Tesseract4OcrEngine getTesseractReader(ReaderType type) {
        if (type.equals(ReaderType.LIB)) {
            return tesseractLibReader;
        } else {
            return tesseractExecutableReader;
        }
    }

    protected static String getTesseractDirectory() {
        String tesseractDir = System.getProperty("tesseractDir");
        String os = System.getProperty("os.name") == null
                ? System.getProperty("OS") : System.getProperty("os.name");
        return os.toLowerCase().contains("win") && tesseractDir != null
                && !tesseractDir.isEmpty()
                ? tesseractDir + "\\tesseract.exe" : "tesseract";
    }

    /**
     * Returns target directory (because target/test could not exist).
     */
    public static String getTargetDirectory() {
        if (!Files.exists(java.nio.file.Paths.get(TARGET_FOLDER))) {
            try {
                Files.createDirectories(
                        java.nio.file.Paths.get(TARGET_FOLDER));
            } catch (IOException e) {
                LOGGER.info(TARGET_FOLDER
                        + " directory does not exist: " + e);
            }
        }
        return TARGET_FOLDER;
    }

    protected static String getTessDataDirectory() {
        return langTessDataDirectory;
    }

    /**
     * Retrieve image BBox rectangle from the first page from given pdf document.
     */
    protected com.itextpdf.kernel.geom.Rectangle getImageBBoxRectangleFromPdf(String path) throws IOException {
        PdfDocument doc = new PdfDocument(new PdfReader(path));

        ExtractionStrategy extractionStrategy = new ExtractionStrategy("Image Layer");
        PdfCanvasProcessor processor = new PdfCanvasProcessor(extractionStrategy);
        processor.processPageContent(doc.getFirstPage());

        doc.close();

        return extractionStrategy.getImageBBoxRectangle();
    }

    /**
     * Retrieve text from specified page from given pdf document.
     */
    protected String getTextFromPdf(Tesseract4OcrEngine tesseractReader,
            File file, int page, List<String> languages, String fontPath) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = getTargetDirectory() + getImageName(file.getAbsolutePath(), languages) + ".pdf";
            doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                    pdfPath, languages, fontPath);
            result = getTextFromPdfLayer(pdfPath, "Text Layer", page);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    /**
     * Retrieve text from the first page of given pdf document setting font.
     */
    protected String getTextFromPdf(Tesseract4OcrEngine tesseractReader, File file,
                          List<String> languages, String fontPath) {
        return getTextFromPdf(tesseractReader, file, 1, languages, fontPath);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     */
    protected String getTextFromPdf(Tesseract4OcrEngine tesseractReader, File file,
            List<String> languages) {
        return getTextFromPdf(tesseractReader, file, 1, languages, null);
    }

    /**
     * Retrieve text from the required page of given pdf document.
     */
    protected String getTextFromPdf(Tesseract4OcrEngine tesseractReader, File file, int page,
                          List<String> languages) {
        return getTextFromPdf(tesseractReader, file, page, languages, null);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     */
    protected String getTextFromPdf(Tesseract4OcrEngine tesseractReader, File file) {
        return getTextFromPdf(tesseractReader, file, 1, null, null);
    }

    /**
     * Get text from layer specified by name from page.
     */
    protected String getTextFromPdfLayer(String pdfPath, String layerName,
                                       int page) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy textExtractionStrategy = new ExtractionStrategy(
                layerName);

        PdfCanvasProcessor processor = new PdfCanvasProcessor(
                textExtractionStrategy);
        processor.processPageContent(pdfDocument.getPage(page));

        pdfDocument.close();
        return textExtractionStrategy.getResultantText();
    }

    /**
     * Perform OCR using provided path to image (imgPath),
     * save to file and get text from file.
     */
    protected String getRecognizedTextFromTextFile(Tesseract4OcrEngine tesseractReader, String input,
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
    protected String getRecognizedTextFromTextFile(Tesseract4OcrEngine tesseractReader, String input) {
        return getRecognizedTextFromTextFile(tesseractReader, input, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result to text file.
     */
    protected void doOcrAndSaveToTextFile(Tesseract4OcrEngine tesseractReader, String imgPath,
                               String txtPath, List<String> languages) {
        if (languages != null) {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setLanguages(languages);
            tesseractReader.setTesseract4OcrEngineProperties(properties);
        }

        tesseractReader.createTxt(Collections.<File>singletonList(new File(imgPath)),
                new File(txtPath));

        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getTesseract4OcrEngineProperties().getLanguages().size());
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(Tesseract4OcrEngine tesseractReader, String imgPath,
            String pdfPath, List<String> languages,
            String fontPath,
            com.itextpdf.kernel.colors.Color color) {
        if (languages != null) {
            Tesseract4OcrEngineProperties properties =
                    tesseractReader.getTesseract4OcrEngineProperties();
            properties.setLanguages(languages);
            tesseractReader.setTesseract4OcrEngineProperties(properties);
        }
        OcrPdfCreatorProperties properties =  new OcrPdfCreatorProperties();
        if (fontPath != null && !fontPath.isEmpty()) {
            properties.setFontPath(fontPath);
        }
        if (color != null) {
            properties.setTextColor(color);
        }
        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getTesseract4OcrEngineProperties().getLanguages().size());
        }

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);
        try (PdfWriter pdfWriter = getPdfWriter(pdfPath)) {
            PdfDocument doc = pdfRenderer.createPdf(
                    Collections.<File>singletonList(new File(imgPath)),
                    pdfWriter);

            Assert.assertNotNull(doc);
            doc.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     */
    protected void doOcrAndSavePdfToPath(Tesseract4OcrEngine tesseractReader, String imgPath,
                               String pdfPath, List<String> languages,
            com.itextpdf.kernel.colors.Color color) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, null, color);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Text will be invisible)
     */
    protected void doOcrAndSavePdfToPath(Tesseract4OcrEngine tesseractReader, String imgPath,
                               String pdfPath, List<String> languages, String fontPath) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, fontPath, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(Tesseract4OcrEngine tesseractReader, String imgPath,
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
                    Tesseract4LogMessageConstant.CannotReadFile,
                    file.getAbsolutePath(),
                    e.getMessage()));
        }
        return content;
    }

    /**
     * Do OCR for given image and compare result etxt file with expected one.
     */
    protected boolean doOcrAndCompareTxtFiles(Tesseract4OcrEngine tesseractReader,
            String imgPath, String expectedPath, List<String> languages) {
        String resultTxtFile = getTargetDirectory() + getImageName(imgPath, languages) + ".txt";
        doOcrAndSaveToTextFile(tesseractReader, imgPath, resultTxtFile, languages);
        return compareTxtFiles(expectedPath, resultTxtFile);
    }

    /**
     * Compare two text files using provided paths.
     */
    protected boolean compareTxtFiles(String expectedFilePath, String resultFilePath) {
        boolean areEqual = true;
        try {
            List<String> expected = Files.readAllLines(java.nio.file.Paths.get(expectedFilePath));
            List<String> result = Files.readAllLines(java.nio.file.Paths.get(resultFilePath));

            if (expected.size() != result.size()) {
                return false;
            }

            for (int i = 0; i < expected.size(); i++) {
                String exp = expected.get(i)
                        .replace("\n", "")
                        .replace("\f", "");
                exp = exp.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\u007E]", "");
                String res = result.get(i)
                        .replace("\n", "")
                        .replace("\f", "");
                res = res.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\u007E]", "");
                if (expected.get(i) == null || result.get(i) == null) {
                    areEqual = false;
                    break;
                } else if (!exp.equals(res)) {
                    areEqual = false;
                    break;
                }
            }
        } catch (IOException e) {
            areEqual = false;
            LOGGER.error(e.getMessage());
        }

        return areEqual;
    }

    /**
     * Create pdfWriter using provided path to destination file.
     */
    protected PdfWriter getPdfWriter(String pdfPath) throws FileNotFoundException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Creates pdf cmyk output intent for tests.
     */
    protected  PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(defaultCMYKColorProfilePath);
        return new PdfOutputIntent("Custom",
                "","http://www.color.org",
                "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * Creates pdf rgb output intent for tests.
     */
    protected  PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * Create pdfWriter.
     */
    protected PdfWriter getPdfWriter() {
       return new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Gets image name from path.
     */
    private String getImageName(String path, List<String> languages) {
        String lang = (languages != null && languages.size() > 0) ?
                "_" + String.join("", languages) : "";
        String img = path
                .substring(path.lastIndexOf(java.io.File.separatorChar))
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
            java.util.List<CanvasTag> tagHierarchy = null;
            if (type.equals(EventType.RENDER_TEXT)) {
                TextRenderInfo textRenderInfo = (TextRenderInfo) data;
                tagHierarchy = textRenderInfo.getCanvasTagHierarchy();
            }
            else if (type.equals(EventType.RENDER_IMAGE)) {
                ImageRenderInfo imageRenderInfo = (ImageRenderInfo) data;
                tagHierarchy = imageRenderInfo.getCanvasTagHierarchy();
            }

            if (tagHierarchy != null) {
                for (CanvasTag tag : tagHierarchy) {
                    PdfDictionary dict = tag.getProperties();
                    String name = dict.get(PdfName.Name).toString();
                    if (name.equals(layerName)) {
                        if (type.equals(EventType.RENDER_TEXT)) {
                            TextRenderInfo renderInfo = (TextRenderInfo) data;
                            setFillColor(renderInfo.getGraphicsState()
                                    .getFillColor());
                            setPdfFont(renderInfo.getGraphicsState().getFont());
                            super.eventOccurred(data, type);
                            break;
                        }
                        else if (type.equals(EventType.RENDER_IMAGE)) {
                            ImageRenderInfo renderInfo = (ImageRenderInfo) data;
                            com.itextpdf.kernel.geom.Matrix ctm = renderInfo.getImageCtm();
                            this.imageBBoxRectangle = new com.itextpdf.kernel.geom.Rectangle(ctm.get(6), ctm.get(7),
                                    ctm.get(0), ctm.get(4));
                            break;
                        }
                    }
                }
            }
        }
    }
}
