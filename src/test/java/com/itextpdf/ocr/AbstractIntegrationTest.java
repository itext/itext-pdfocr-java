package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
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

import java.util.ArrayList;
import java.util.UUID;

import com.itextpdf.licensekey.LicenseKey;
import org.junit.Assert;

import java.io.*;
import java.util.Collections;
import java.util.List;

class AbstractIntegrationTest {

    // directory with test files
    static String testDirectory = "src/test/resources/com/itextpdf/ocr/";
    // directory with trained data for tests
    static String langTessDataDirectory = "src/test/resources/com/itextpdf/ocr/tessdata/";
    // directory with trained data for tests
    static String scriptTessDataDirectory = "src/test/resources/com/itextpdf/ocr/tessdata/script/";
    // directory with test image files
    static String testImagesDirectory = testDirectory + "images/";
    // directory with fonts
    static String testFontsDirectory = testDirectory + "fonts/";
    // directory with fonts
    static String testPdfDirectory = testDirectory + "documents/";

    // path to font for hindi
    static String notoSansFontPath = testFontsDirectory + "NotoSans-Regular.ttf";
    // path to font for japanese
    static String notoSansJPFontPath = testFontsDirectory + "NotoSansJP-Regular.otf";
    // path to font for arabic
    static String cairoFontPath = testFontsDirectory + "Cairo-Regular.ttf";
    // path to font for georgian
    static String freeSansFontPath = testFontsDirectory + "FreeSans.ttf";

    static float delta = 1e-4f;

    String getTesseractDirectory() {
        String tesseractDir = System.getProperty("tesseractDir");
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("win") && tesseractDir != null
                && !tesseractDir.isEmpty() ? tesseractDir + "\\tesseract.exe" : "tesseract";
    }

    /**
     * Provide license for typography add-on
     */
    static void initializeLicense() {
        LicenseKey.loadLicenseFile(testDirectory + "itextkey1575287294081_0.xml");
    }

    /**
     * Retrieve image from given pdf document.
     *
     * @param file
     * @param scaleMode
     * @param pageSize
     * @return
     */
    Image getImageFromPdf(File file, IPdfRenderer.ScaleMode scaleMode,
                                  Rectangle pageSize) throws IOException {
        IOcrReader tesseractReader = new TesseractReader(getTesseractDirectory());
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file), scaleMode);

        pdfRenderer.setPageSize(pageSize);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter());

        Image image = null;

        Assert.assertNotNull(doc);
        if (!doc.isClosed()) {
            PdfDictionary pageDict = doc.getFirstPage().getPdfObject();
            PdfDictionary pageResources = pageDict.getAsDictionary(PdfName.Resources);
            PdfDictionary pageXObjects = pageResources.getAsDictionary(PdfName.XObject);
            PdfName imgRef = pageXObjects.keySet().iterator().next();
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
     * @param file
     * @param page
     * @return
     */
    String getTextFromPdf(File file, int page, String tessDataDir, List<String> languages,
            String fontPath) {
        String result = null;
        String pdfPath = null;
        try {
            pdfPath = File.createTempFile(UUID.randomUUID().toString(), ".pdf")
                    .getAbsolutePath();
            doOcrAndSaveToPath(file.getAbsolutePath(), pdfPath, tessDataDir, languages, fontPath);
            result = getTextFromPdfLayer(pdfPath, "Text Layer", page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteFile(pdfPath);

        return result;
    }

    /**
     * Retrieve text from the first page of given pdf document setting font.
     *
     * @param file
     * @return
     */
    String getTextFromPdf(File file, String tessDataDir, List<String> languages, String fontPath) {
        return getTextFromPdf(file, 1, tessDataDir, languages, fontPath);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param file
     * @return
     */
    String getTextFromPdf(File file, String tessDataDir, List<String> languages) {
        return getTextFromPdf(file, 1, tessDataDir, languages, null);
    }

    /**
     * Retrieve text from specified page from given pdf document.
     *
     * @param file
     * @param page
     * @return
     */
    String getTextFromPdf(File file, int page) {
        return getTextFromPdf(file, page, null, null, null);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param file
     * @return
     */
    String getTextFromPdf(File file) {
        return getTextFromPdf(file, 1, null, null, null);
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
    String getTextFromPdfLayer(String pdfPath, String layerName,
                                       int page) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        ExtractionStrategy textExtractionStrategy = new ExtractionStrategy(layerName);

        PdfCanvasProcessor processor = new PdfCanvasProcessor(textExtractionStrategy);
        processor.processPageContent(pdfDocument.getPage(page));

        return textExtractionStrategy.getResultantText();
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     *
     * @param imgPath
     * @param pdfPath
     */
    void doOcrAndSaveToPath(String imgPath, String pdfPath, String tessDataDir,
            List<String> languages, String fontPath) {
        TesseractReader tesseractReader = new TesseractReader(getTesseractDirectory());
        if (languages == null) {
            tesseractReader.setPathToTessData(tessDataDir);
        } else if (tessDataDir == null) {
            tesseractReader.setLanguages(languages);
        } else {
            tesseractReader = new TesseractReader(getTesseractDirectory(), languages, tessDataDir);
        }

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(new File(imgPath)));
        if (fontPath != null && !fontPath.isEmpty()) {
            pdfRenderer.setFontPath(fontPath);
        }

        PdfDocument doc = null;
        try {
            doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (languages != null) {
            Assert.assertEquals(languages.size(), tesseractReader.getLanguages().size());
        }

        Assert.assertNotNull(doc);
        if (!doc.isClosed()) {
            doc.close();
        }
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method uses default font path)
     *
     * @param imgPath
     * @param pdfPath
     */
    void doOcrAndSaveToPath(String imgPath, String pdfPath, String tessDataDir,
                            List<String> languages) {
        doOcrAndSaveToPath(imgPath, pdfPath, tessDataDir, languages, null);
    }

    /**
     * Perform OCR using provided path to image (imgPath)
     * and save result pdf document to "pdfPath".
     * (Method is used for compare tool)
     *
     * @param imgPath
     * @param pdfPath
     */
    void doOcrAndSaveToPath(String imgPath, String pdfPath) {
        doOcrAndSaveToPath(imgPath, pdfPath, null, null, null);
    }

    /**
     * Delete file using provided path.
     *
     * @param filePath
     */
    void deleteFile(String filePath) {
        if (filePath != null & !filePath.isEmpty()) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Create pdfWriter using provided ByteArrayOutputStream.
     *
     * @param baos
     * @return
     */
    PdfWriter createPdfWriter(ByteArrayOutputStream baos) {
        return new PdfWriter(baos, new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Create pdfWriter using provided path to destination file.
     *
     * @param pdfPath
     * @return
     * @throws FileNotFoundException
     */
    PdfWriter createPdfWriter(String pdfPath) throws FileNotFoundException {
        return new PdfWriter(pdfPath, new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Creates pdf cmyk output intent for tests.
     *
     * @return
     * @throws FileNotFoundException
     */
    PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(getDefaultCMYKColorProfilePath());
        return new PdfOutputIntent("Custom", "",
                "http://www.color.org", "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * Creates pdf rgb output intent for tests.
     *
     * @return
     * @throws FileNotFoundException
     */
    PdfOutputIntent getRGBPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(getDefaultRGBColorProfilePath());
        return new PdfOutputIntent("", "",
                "", "sRGB IEC61966-2.1", is);
    }

    /**
     * @return path to default cmyk color profile
     */
    String getDefaultCMYKColorProfilePath() {
        return "src/test/resources/com/itextpdf/ocr/CoatedFOGRA27.icc";
    }

    /**
     * @return path to default rgb color profile
     */
    String getDefaultRGBColorProfilePath() {
        return "src/test/resources/com/itextpdf/ocr/sRGB_CS_profile.icm";
    }

    /**
     * Create pdfWriter.
     *
     * @return
     * @throws IOException
     */
    PdfWriter getPdfWriter() throws IOException {
       return new PdfWriter(new ObjectOutputStream(new ByteArrayOutputStream()),
               new WriterProperties().addUAXmpMetadata());
    }

    static class ExtractionStrategy extends LocationTextExtractionStrategy {
        private com.itextpdf.kernel.colors.Color fillColor;
        private com.itextpdf.kernel.colors.Color strokeColor;
        private String layerName;
        private PdfFont pdfFont;

        ExtractionStrategy(String name) {
            super();
            layerName = name;
        }

        void setFillColor(com.itextpdf.kernel.colors.Color color) {
            fillColor = color;
        }

        com.itextpdf.kernel.colors.Color getFillColor() {
            return fillColor;
        }

        void setStrokeColor(com.itextpdf.kernel.colors.Color color) {
            strokeColor = color;
        }

        com.itextpdf.kernel.colors.Color getStrokeColor() {
            return strokeColor;
        }

        void setPdfFont(PdfFont font) {
            pdfFont = font;
        }

        PdfFont getPdfFont() {
            return pdfFont;
        }

        @Override
        protected boolean isChunkAtWordBoundary(TextChunk chunk, TextChunk previousChunk) {
            String cur = chunk.getText();
            String prev = previousChunk.getText();
            ITextChunkLocation curLoc = chunk.getLocation();
            ITextChunkLocation prevLoc = previousChunk.getLocation();

            if (curLoc.getStartLocation().equals(curLoc.getEndLocation()) ||
                    prevLoc.getEndLocation().equals(prevLoc.getStartLocation())) {
                return false;
            }

            return curLoc.distParallelEnd() - prevLoc.distParallelStart() >
                    (curLoc.getCharSpaceWidth() + prevLoc.getCharSpaceWidth()) / 2.0f;
        }

        @Override
        public void eventOccurred(IEventData data, EventType type) {
            if (EventType.RENDER_TEXT.equals(type)) {
                TextRenderInfo renderInfo = (TextRenderInfo) data;
                java.util.List<CanvasTag> tagHierarchy = renderInfo.getCanvasTagHierarchy();
                for (CanvasTag tag : tagHierarchy) {
                    PdfDictionary dict = tag.getProperties();
                    String name = dict.get(PdfName.Name).toString();
                    if (layerName.equals(name)) {
                        setFillColor(renderInfo.getGraphicsState().getFillColor());
                        setStrokeColor(renderInfo.getGraphicsState().getStrokeColor());
                        setPdfFont(renderInfo.getGraphicsState().getFont());
                        super.eventOccurred(data, type);
                        break;
                    }
                }
            }
        }
    }
}
