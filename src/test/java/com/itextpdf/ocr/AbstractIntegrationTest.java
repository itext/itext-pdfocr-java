package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;

class AbstractIntegrationTest {

    // directory with test files
    static String testDirectory = "src/test/resources/com/itextpdf/ocr/";
    // path to hocr script for tesseract executable
    static String pathToHocrScript = "src/test/resources/com/itextpdf/ocr/hocr";
    // directory with trained data for tests
    static String langTessDataDirectory = "src/test/resources/com/itextpdf/ocr/tessdata";
    // directory with trained data for tests
    static String scriptTessDataDirectory = "src/test/resources/com/itextpdf/ocr/tessdata/script";
    // directory with test image files
    static String testImagesDirectory = testDirectory + "images/";
    // directory with fonts
    static String testFontsDirectory = testDirectory + "fonts/";
    // directory with fonts
    static String testDocumentsDirectory = testDirectory + "documents/";

    // path to font for hindi
    static String notoSansFontPath = testFontsDirectory + "NotoSans-Regular.ttf";
    // path to font for japanese
    static String kosugiFontPath = testFontsDirectory + "Kosugi-Regular.ttf";
    // path to font for chinese
    static String notoSansSCFontPath = testFontsDirectory + "NotoSansSC-Regular.otf";
    // path to font for arabic
    static String cairoFontPath = testFontsDirectory + "Cairo-Regular.ttf";
    // path to font for georgian
    static String freeSansFontPath = testFontsDirectory + "FreeSans.ttf";

    static float delta = 1e-4f;

    static String getTesseractDirectory() {
        String tesseractDir = System.getProperty("tesseractDir");
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("win") && tesseractDir != null
                && !tesseractDir.isEmpty()
                ? tesseractDir + "\\tesseract.exe" : "tesseract";
    }

    static String getTessDataDirectory() {
        return langTessDataDirectory;
    }

    static String getPathToHocrScript() {
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
    Image getImageFromPdf(TesseractReader tesseractReader,
                          File file, IPdfRenderer.ScaleMode scaleMode,
                          Rectangle pageSize) throws IOException {
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
    String getTextFromPdf(TesseractReader tesseractReader, File file, int page,
                          List<String> languages, String fontPath) {
        String result = null;
        File pdf = null;
        try {
            pdf = File.createTempFile(UUID.randomUUID().toString(),
                    ".pdf");
            doOcrAndSavePdfToPath(tesseractReader, file.getAbsolutePath(),
                    pdf.getAbsolutePath(), languages, fontPath);
            result = getTextFromPdfLayer(pdf.getAbsolutePath(), "Text Layer",
                    page);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            deleteFile(pdf);
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
    String getTextFromPdf(TesseractReader tesseractReader, File file,
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
    String getTextFromPdf(TesseractReader tesseractReader, File file,
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
    String getTextFromPdf(TesseractReader tesseractReader, File file, int page,
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
    String getTextFromPdf(TesseractReader tesseractReader, File file,
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
    String getTextFromPdf(TesseractReader tesseractReader, File file) {
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
    String getTextFromPdfLayer(String pdfPath, String layerName,
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
    String getOCRedTextFromTextFile(TesseractReader tesseractReader, String input,
            List<String> languages) {
        String result = null;
        File txt = null;
        try {
            txt = File.createTempFile(UUID.randomUUID().toString(),
                    ".txt");
            doOcrAndSaveToTextFile(tesseractReader, input, txt.getAbsolutePath(), languages);
            result = getTextFromTextFile(txt);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            deleteFile(txt);
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
    String getOCRedTextFromTextFile(TesseractReader tesseractReader, String input) {
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
    void doOcrAndSaveToTextFile(TesseractReader tesseractReader, String imgPath,
                               String txtPath, List<String> languages) {
        if (languages != null) {
            tesseractReader.setLanguages(languages);
        }

        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.<File>singletonList(new File(imgPath)));

        pdfRenderer.doPdfOcr(txtPath);

        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getLanguages().size());
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
    void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath, List<String> languages, String fontPath,
                               Color color) {
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
            e.printStackTrace();
        }

        if (languages != null) {
            Assert.assertEquals(languages.size(),
                    tesseractReader.getLanguages().size());
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
    void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
                               String pdfPath, List<String> languages, Color color) {
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
    void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
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
    void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
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
    void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
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
    void doOcrAndSavePdfToPath(TesseractReader tesseractReader, String imgPath,
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
    String getTextFromTextFile(File file) {
        return UtilService.readTxtFile(file);
    }

    /**
     * Delete file using provided path.
     *
     * @param filePath
     */
    void deleteFile(String filePath) {
        UtilService.deleteFile(new File(filePath));
    }

    void deleteFile(File file) {
        UtilService.deleteFile(file);
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
    boolean doOcrAndCompareTxtFiles(TesseractReader tesseractReader, String imgPath,
            String expectedPath, List<String> languages) {
        boolean result = false;
        File resutTxtFile = null;
        try {
            resutTxtFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
            doOcrAndSaveToTextFile(tesseractReader, imgPath, resutTxtFile.getAbsolutePath(), languages);
            result = compareTxtFiles(expectedPath, resutTxtFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert resutTxtFile != null;
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
    boolean compareTxtFiles(String expectedFilePath, String resultFilePath) {
        boolean areEqual = true;
        try (BufferedReader reader1 = new BufferedReader(new FileReader(expectedFilePath));
            BufferedReader reader2 = new BufferedReader(new FileReader(resultFilePath))) {

            String line1 = reader1.readLine();
            String line2 = reader2.readLine();
            while (line1 != null || line2 != null) {
                if (line1 == null || line2 == null) {
                    areEqual = false;
                    break;
                } else if (!line1.equals(line2)) {
                    areEqual = false;
                    break;
                }
                line1 = reader1.readLine();
                line2 = reader2.readLine();
            }
        } catch (IOException e) {
            areEqual = false;
            e.printStackTrace();
        }

        return areEqual;
    }
    /**
     * Create pdfWriter using provided ByteArrayOutputStream.
     *
     * @param baos
     * @return
     */
    PdfWriter getPdfWriter(ByteArrayOutputStream baos) {
        return new PdfWriter(baos, new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Create pdfWriter using provided path to destination file.
     *
     * @param pdfPath
     * @return
     * @throws FileNotFoundException
     */
    PdfWriter getPdfWriter(String pdfPath) throws FileNotFoundException {
        return new PdfWriter(pdfPath,
                new WriterProperties().addUAXmpMetadata());
    }

    /**
     * Creates pdf cmyk output intent for tests.
     *
     * @return
     * @throws FileNotFoundException
     */
    PdfOutputIntent getCMYKPdfOutputIntent() throws FileNotFoundException {
        InputStream is = new FileInputStream(getDefaultCMYKColorProfilePath());
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

        void setPdfFont(PdfFont font) {
            pdfFont = font;
        }

        PdfFont getPdfFont() {
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
