package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
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
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextChunkLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.element.Image;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractIntegrationTest extends ExtendedITextTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractIntegrationTest.class);

    // path to hocr script for tesseract executable
    protected static String pathToHocrScript = null;
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
        if (pathToHocrScript == null) {
            pathToHocrScript = path + "hocr" + java.io.File.separatorChar;
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

    protected static TesseractReader getTesseractReader(String type) {
        if ("lib".equals(type)) {
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

    protected static String getPathToHocrScript() {
        return pathToHocrScript;
    }

    /**
     * Retrieve image from given pdf document.
     *
     * @param tesseractReader
     * @param file
     * @param scaleMode
     * @param pageSize
     * @return
     * @throws IOException
     */
    protected Image getImageFromPdf(TesseractReader tesseractReader,
                          File file, IPdfRenderer.ScaleMode scaleMode,
            com.itextpdf.kernel.geom.Rectangle pageSize) throws IOException {
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(file), scaleMode);

        pdfRenderer.setPageSize(pageSize);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter());

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
     * Retrieve text from specified page from given pdf document.
     *
     * @param tesseractReader
     * @param file
     * @param page
     * @param languages
     * @param fontPath
     * @return
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
     *
     * @param tesseractReader
     * @param file
     * @param languages
     * @param fontPath
     * @return
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file,
                          List<String> languages, String fontPath) {
        return getTextFromPdf(tesseractReader, file, 1, languages, fontPath);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param tesseractReader
     * @param file
     * @param languages
     * @return
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file,
            List<String> languages) {
        return getTextFromPdf(tesseractReader, file, 1, languages, null);
    }

    /**
     * Retrieve text from the required page of given pdf document.
     *
     * @param tesseractReader
     * @param file
     * @param page
     * @param languages
     * @return
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file, int page,
                          List<String> languages) {
        return getTextFromPdf(tesseractReader, file, page, languages, null);
    }

    /**
     * Retrieve text from specified page from given pdf document.
     *
     * @param tesseractReader
     * @param file
     * @param page
     * @return
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file,
                          int page) {
        return getTextFromPdf(tesseractReader, file, page, null, null);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param tesseractReader
     * @param file
     * @return
     */
    protected String getTextFromPdf(TesseractReader tesseractReader, File file) {
        return getTextFromPdf(tesseractReader, file, 1, null, null);
    }

    /**
     * Get text from layer specified by name from page.
     *
     * @param pdfPath
     * @param layerName
     * @param page
     * @return
     * @throws IOException
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
     *
     * @param tesseractReader
     * @param input
     * @param languages
     * @return
     */
    protected String getOCRedTextFromTextFile(TesseractReader tesseractReader, String input,
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
     *
     * @param tesseractReader
     * @param input
     * @return
     */
    protected String getOCRedTextFromTextFile(TesseractReader tesseractReader, String input) {
        return getOCRedTextFromTextFile(tesseractReader, input, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result to text file.
     *
     * @param tesseractReader
     * @param imgPath
     * @param txtPath
     * @param languages
     */
    protected void doOcrAndSaveToTextFile(TesseractReader tesseractReader, String imgPath,
                               String txtPath, List<String> languages) {
        if (languages != null) {
            tesseractReader.setLanguages(languages);
        }

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(new File(imgPath)));

        pdfRenderer.doPdfOcr(txtPath);

        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getLanguagesAsList().size());
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     *
     * @param tesseractReader
     * @param imgPath
     * @param pdfPath
     * @param languages
     * @param fontPath
     * @param color
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
            String pdfPath, List<String> languages,
            String fontPath,
            com.itextpdf.kernel.colors.Color color) {
        if (languages != null) {
            tesseractReader.setLanguages(languages);
        }

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(new File(imgPath)));
        pdfRenderer.setScaleMode(IPdfRenderer.ScaleMode.keepOriginalSize);
        if (fontPath != null && !fontPath.isEmpty()) {
            pdfRenderer.setFontPath(fontPath);
        }
        if (color != null) {
            pdfRenderer.setTextColor(color);
        }

        PdfDocument doc = null;
        try {
            doc = pdfRenderer.doPdfOcr(getPdfWriter(pdfPath));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getLanguagesAsList().size());
        }

        Assert.assertNotNull(doc);
        if (!doc.isClosed()) {
            doc.close();
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     *
     * @param tesseractReader
     * @param imgPath
     * @param pdfPath
     * @param languages
     * @param color
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
     *
     * @param tesseractReader
     * @param imgPath
     * @param pdfPath
     * @param languages
     * @param fontPath
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath, List<String> languages, String fontPath) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, fontPath, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     *
     * @param tesseractReader
     * @param imgPath
     * @param pdfPath
     * @param fontPath
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath, String fontPath) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                null, fontPath, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     *   (Method uses default font path)
     *
     * @param tesseractReader
     * @param imgPath
     * @param pdfPath
     * @param languages
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath, List<String> languages) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath,
                languages, null, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     *
     * @param tesseractReader
     * @param imgPath
     * @param pdfPath
     */
    protected void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath) {
        doOcrAndSavePdfToPath(tesseractReader, imgPath, pdfPath, null,
                null, null);
    }

    /**
     * Retrieve text from given txt file.
     *
     * @param file
     * @return
     */
    protected String getTextFromTextFile(File file) {
        return UtilService.readTxtFile(file);
    }

    /**
     * Delete file using provided path.
     *
     * @param filePath
     */
    protected void deleteFile(String filePath) {
        UtilService.deleteFile(filePath);
    }

    /**
     * Do OCR for given image and compare result etxt file with expected one.
     *
     * @param tesseractReader
     * @param imgPath
     * @param expectedPath
     * @param languages
     * @return
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
     *
     * @param expectedFilePath
     * @param resultFilePath
     * @return
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
     * Create pdfWriter using provided ByteArrayOutputStream.
     *
     * @param baos
     * @return
     */
    protected PdfWriter getPdfWriter(ByteArrayOutputStream baos) {
        return new PdfWriter(baos, new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Create pdfWriter using provided path to destination file.
     *
     * @param pdfPath
     * @return
     * @throws FileNotFoundException
     */
    protected PdfWriter getPdfWriter(String pdfPath) throws FileNotFoundException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Creates pdf cmyk output intent for tests.
     *
     * @return
     * @throws FileNotFoundException
     */
    protected  PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(defaultCMYKColorProfilePath);
        return new PdfOutputIntent("Custom",
                "","http://www.color.org",
                "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * Creates pdf rgb output intent for tests.
     *
     * @return
     * @throws FileNotFoundException
     */
    protected  PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(defaultRGBColorProfilePath);
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * Create pdfWriter.
     *
     * @return
     * @throws IOException
     */
    protected PdfWriter getPdfWriter() throws IOException {
       return new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().addUAXmpMetadata());
    }

    public static class ExtractionStrategy extends LocationTextExtractionStrategy {
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

        @Override
        protected boolean isChunkAtWordBoundary(TextChunk chunk,
                                                TextChunk previousChunk) {
            String cur = chunk.getText();
            String prev = previousChunk.getText();
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
            if (EventType.RENDER_TEXT.equals(type)) {
                TextRenderInfo renderInfo = (TextRenderInfo) data;
                java.util.List<CanvasTag> tagHierarchy = renderInfo
                        .getCanvasTagHierarchy();
                for (CanvasTag tag : tagHierarchy) {
                    PdfDictionary dict = tag.getProperties();
                    String name = dict.get(PdfName.Name).toString();
                    if (layerName.equals(name)) {
                        setFillColor(renderInfo.getGraphicsState()
                                .getFillColor());
                        setPdfFont(renderInfo.getGraphicsState().getFont());
                        super.eventOccurred(data, type);
                        break;
                    }
                }
            }
        }
    }
}
