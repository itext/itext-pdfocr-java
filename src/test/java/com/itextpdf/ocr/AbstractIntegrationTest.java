package com.itextpdf.ocr;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.element.Image;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class AbstractIntegrationTest {

    // directory with test files
    static String directory = "src/test/resources/com/itextpdf/ocr/";
    // directory with trained data for tests
    static String tessDataDirectory = "src/test/resources/com/itextpdf/ocr/tessdata/";

    static float delta = 1e-4f;

    String getTesseractDirectory() {
        String tesseractDir = System.getProperty("tesseractDir");
        return tesseractDir != null && !tesseractDir.isEmpty() ? tesseractDir : "tesseract";
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
        IOcrReader tesseractReader = new TesseractReader();
        IPdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(file), scaleMode);

        pdfRenderer.setPageSize(pageSize);

        PdfDocument doc = pdfRenderer.doPdfOcr(getPdfWriter(), false);

        Image image = null;

        assert doc != null;
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
    String getTextFromPdfFile(File file, int page, String tessDataDir, List<String> languages) {
        try {
            InputStream stream = doOcr(file, tessDataDir, languages);
            PdfDocument pdf = new PdfDocument(new PdfReader(stream));

            ITextExtractionStrategy strategy = new SimpleTextExtractionStrategy();

            stream.close();
            return PdfTextExtractor.getTextFromPage(pdf.getPage(page), strategy);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param file
     * @return
     */
    String getTextFromPdfFile(File file, String tessDataDir, List<String> languages) {
        return getTextFromPdfFile(file, 1, tessDataDir, languages);
    }

    /**
     * Retrieve text from specified page from given pdf document.
     *
     * @param file
     * @param page
     * @return
     */
    String getTextFromPdfFile(File file, int page) {
        return getTextFromPdfFile(file, page, null, null);
    }

    /**
     * Retrieve text from the first page of given pdf document.
     *
     * @param file
     * @return
     */
    String getTextFromPdfFile(File file) {
        return getTextFromPdfFile(file, 1, null, null);
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
    void doOcrAndSaveToPath(String imgPath, String pdfPath, String tessDataDir, List<String> languages) {
        TesseractReader tesseractReader = new TesseractReader();
        if (tessDataDir != null) {
            tesseractReader.setPathToTessData(tessDataDir);
        }
        if (languages != null) {
            tesseractReader.setLanguages(languages);
        }
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader,
                Collections.singletonList(new File(imgPath)));

        PdfDocument doc = null;
        try {
            doc = pdfRenderer.doPdfOcr(createPdfWriter(pdfPath), false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert doc != null;
        if (!doc.isClosed()) {
            doc.close();
        }
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
        doOcrAndSaveToPath(imgPath, pdfPath, null, null);
    }

    /**
     * Performs OCR for provided image file providing path to tessData
     * and list of required languages.
     *
     * @param file
     * @return InputStream
     */
    InputStream doOcr(File file, String tessDataDir, List<String> languages) throws FileNotFoundException {
        TesseractReader tesseractReader = new TesseractReader();
        if (tessDataDir != null) {
            tesseractReader.setPathToTessData(tessDataDir);
        }
        if (languages != null) {
            tesseractReader.setLanguages(languages);
        }
        PdfRenderer pdfRenderer = new PdfRenderer(tesseractReader, Collections.singletonList(file));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pw = createPdfWriter(baos);
        PdfDocument doc = pdfRenderer.doPdfOcr(pw, false);
        assert doc != null;
        doc.close();

        InputStream stream = new ByteArrayInputStream(baos.toByteArray());
        return stream;
    }

    /**
     * Delete file using provided path.
     *
     * @param filePath
     */
    void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
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
        return new PdfOutputIntent("Custom", "",
                "http://www.color.org", "Coated FOGRA27 (ISO 12647 - 2:2004)", is);
    }

    /**
     * @return path to default cmyk color profile
     */
    String getDefaultCMYKColorProfilePath() {
        return "src/main/resources/com/itextpdf/ocr/CoatedFOGRA27.icc";
    }

    /**
     * @return path to default rgb color profile
     */
    String getDefaultRGBColorProfilePath() {
        return "src/main/resources/com/itextpdf/ocr/sRGB_CS_profile.icm";
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
