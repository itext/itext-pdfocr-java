package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
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
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.element.Image;
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
import java.util.UUID;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(IntegrationTest.class)
public class AbstractIntegrationTest extends ExtendedITextTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractIntegrationTest.class);

    protected enum ReaderType {
        LIB,
        EXECUTABLE
    }

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

    static TesseractLibReader tesseractLibReader = null;
    static TesseractExecutableReader tesseractExecutableReader = null;

    public AbstractIntegrationTest() {
        setResourceDirectories();
        tesseractLibReader = new TesseractLibReader(getTessDataDirectory());
        tesseractExecutableReader = new TesseractExecutableReader(
                    getTesseractDirectory(), getTessDataDirectory());
    }

    static void setResourceDirectories() {
        String path = TestUtils.getCurrentDirectory();
        if (testImagesDirectory == null) {
            testImagesDirectory = path + "images" + java.io.File.separatorChar;
        }
        if (langTessDataDirectory == null) {
            langTessDataDirectory = path + "tessdata";
        }
        if (scriptTessDataDirectory == null) {
            scriptTessDataDirectory = path + "tessdata" + java.io.File.separatorChar + "script";
        }
        if (testFontsDirectory == null) {
            testFontsDirectory = path + "fonts" + java.io.File.separatorChar;
            updateFonts();
        }
        if (testDocumentsDirectory == null) {
            testDocumentsDirectory = path + "documents" + java.io.File.separatorChar;
        }
        if (defaultCMYKColorProfilePath == null) {
            defaultCMYKColorProfilePath = path + "CoatedFOGRA27.icc";
        }
        if (defaultRGBColorProfilePath == null) {
            defaultRGBColorProfilePath = path + "sRGB_CS_profile.icm";
        }
    }

    static void updateFonts() {
        notoSansFontPath = testFontsDirectory + "NotoSans-Regular.ttf";
        kosugiFontPath = testFontsDirectory + "Kosugi-Regular.ttf";
        notoSansSCFontPath = testFontsDirectory + "NotoSansSC-Regular.otf";
        cairoFontPath = testFontsDirectory + "Cairo-Regular.ttf";
        freeSansFontPath = testFontsDirectory + "FreeSans.ttf";
    }

    protected static TesseractReader getTesseractReader(ReaderType type) {
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

    protected static String getTessDataDirectory() {
        return langTessDataDirectory;
    }

    /**
     * Retrieve image from given pdf document.
     */
    protected Image getImageFromPdf(TesseractReader tesseractReader,
                          File file, ScaleMode scaleMode,
            com.itextpdf.kernel.geom.Rectangle pageSize) {
        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setScaleMode(scaleMode);
        properties.setPageSize(pageSize);

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, properties);

        PdfDocument doc = pdfRenderer.createPdf(
                Collections.<File>singletonList(file), getPdfWriter());

        Image image = null;

        Assert.assertNotNull(doc);
        if (!doc.isClosed()) {
            PdfDictionary pageDict = doc.getFirstPage().getPdfObject();
            PdfDictionary pageResources = pageDict
                    .getAsDictionary(PdfName.Resources);
            PdfDictionary pageXObjects = pageResources
                    .getAsDictionary(PdfName.XObject);
            List<PdfName> pdfNames = new ArrayList<PdfName>(pageXObjects.keySet());
            PdfName imgRef = pdfNames.get(0);
            PdfStream imgStream = pageXObjects.getAsStream(imgRef);

            PdfImageXObject imgObject = new PdfImageXObject(imgStream);

            image = new Image(imgObject);
            doc.close();
        }

        return image;
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
    protected String getTextFromPdf(TesseractReader tesseractReader, File file, int page,
                          List<String> languages, String fontPath) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = TesseractUtil.getTempDir() + UUID.randomUUID().toString() +
                    ".pdf";
            doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                    pdfPath, languages, fontPath);
            result = getTextFromPdfLayer(pdfPath, "Text Layer", page);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            deleteFile(pdfPath);
        }

        return result;
    }

    /**
     * Retrieve text from the first page of given pdf document setting font.
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file,
                          List<String> languages, String fontPath) {
        return getTextFromPdf(tesseractReader, file, 1, languages, fontPath);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file,
            List<String> languages) {
        return getTextFromPdf(tesseractReader, file, 1, languages, null);
    }

    /**
     * Retrieve text from the required page of given pdf document.
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file, int page,
                          List<String> languages) {
        return getTextFromPdf(tesseractReader, file, page, languages, null);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file) {
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
    protected String getRecognizedTextFromTextFile(TesseractReader tesseractReader, String input,
            List<String> languages) {
        String result = null;
        String txtPath = null;
        try {
            txtPath = TesseractUtil.getTempDir()
                    + UUID.randomUUID().toString() + ".txt";
            doOcrAndSaveToTextFile(tesseractReader, input, txtPath, languages);
            result = getTextFromTextFile(new File(txtPath));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            deleteFile(txtPath);
        }

        return result;
    }

    /**
     * Perform OCR using provided path to image (imgPath),
     * save to file and get text from file.
     */
    protected String getRecognizedTextFromTextFile(TesseractReader tesseractReader, String input) {
        return getRecognizedTextFromTextFile(tesseractReader, input, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result to text file.
     */
    protected void doOcrAndSaveToTextFile(TesseractReader tesseractReader, String imgPath,
                               String txtPath, List<String> languages) {
        if (languages != null) {
            tesseractReader.setLanguages(languages);
        }
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                new OcrPdfCreatorProperties());

        pdfRenderer.createTxt(Collections.<File>singletonList(new File(imgPath)),
                txtPath);

        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getLanguagesAsList().size());
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
            String pdfPath, List<String> languages,
            String fontPath,
            com.itextpdf.kernel.colors.Color color) {
        if (languages != null) {
            tesseractReader.setLanguages(languages);
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
                    tesseractReader.getLanguagesAsList().size());
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
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
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
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath, List<String> languages, String fontPath) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, fontPath, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
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
                    LogMessageConstant.CannotReadFile,
                    file.getAbsolutePath(),
                    e.getMessage()));
        }
        return content;
    }

    /**
     * Delete file using provided path.
     */
    protected void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()
                    && Files.exists(java.nio.file.Paths.get(filePath))) {
                Files.delete(java.nio.file.Paths.get(filePath));
            }
        } catch (IOException | SecurityException e) {
            LOGGER.info(MessageFormatUtil.format(
                    LogMessageConstant.CannotDeleteFile,
                    filePath,
                    e.getMessage()));
        }
    }

    /**
     * Do OCR for given image and compare result etxt file with expected one.
     */
    protected boolean doOcrAndCompareTxtFiles(TesseractReader tesseractReader, String imgPath,
            String expectedPath, List<String> languages) {
        boolean result = false;
        String resutTxtFile = null;
        try {
            resutTxtFile = TesseractUtil.getTempDir()
                            + UUID.randomUUID().toString() + ".txt";
            doOcrAndSaveToTextFile(tesseractReader, imgPath, resutTxtFile, languages);
            result = compareTxtFiles(expectedPath, resutTxtFile);
        } finally {
            deleteFile(resutTxtFile);
        }

        return result;
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
     * Converts value from pixels to points.
     *
     * @param pixels input value in pixels
     * @return result value in points
     */
    protected float getPoints(final float pixels) {
        return pixels * 3f / 4f;
    }

    /**
     * Create pdfWriter.
     */
    protected PdfWriter getPdfWriter() {
       return new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().addUAXmpMetadata());
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
