/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.logs.KernelLogMessageConstant;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfocr.OcrPdfCreator;
import com.itextpdf.pdfocr.OcrPdfCreatorProperties;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
import com.itextpdf.pdfocr.onnx.util.OcrEngineTypeWithOrientation;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.LogLevelConstants;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OcrPdfTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/OcrPdfTest/";
    private static final String TEST_PDFS_DIRECTORY = TEST_DIRECTORY + "../pdfs/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OcrPdfTest/";


    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {
        makeSearchable("numbers");
    }

    @Test
    public void pageRotationTest() throws IOException, InterruptedException {
        makeSearchable("pageRotation");
    }

    @Test
    public void twoImagesTest() throws IOException, InterruptedException {
        makeSearchable("2images");
    }

    @Test
    public void twoPagesTest() throws IOException, InterruptedException {
        makeSearchable("2pages");
    }

    @Test
    public void rotatedTest() throws IOException, InterruptedException {
        makeSearchable("rotated");
    }

    @Test
    public void mixedRotationTest() throws IOException, InterruptedException {
        makeSearchable("mixedRotation");
    }

    @Test
    public void notRecognizableTest() throws IOException, InterruptedException {
        // OnnxTr engine could recognize
        makeSearchable("notRecognizable");
    }

    @Test
    public void imageIntersectionTest() throws IOException, InterruptedException {
        makeSearchable("imageIntersection");
    }

    @Test
    public void whiteTextTest() throws IOException, InterruptedException {
        // OCRed by onnx. Almost good, w is OCRed as rotated m.
        // If you don't use orientation predictor, the result becomes very good.
        makeSearchable("whiteText");
    }

    @Test
    public void changedImageProportionTest() throws IOException, InterruptedException {
        makeSearchable("changedImageProportion");
    }

    @Test
    public void textWithImagesTest() throws IOException, InterruptedException {
        makeSearchable("textWithImages");
    }

    @Test
    public void invisibleTextImageTest() throws IOException, InterruptedException {
        makeSearchable("invisibleTextImage");
    }

    @Test
    public void layersTest() throws IOException, InterruptedException {
        OcrPdfCreatorProperties ocrPdfCreatorProperties =
                new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.MAGENTA).setTextLayerName("Text");
        makeSearchable("2pages", "layers", ocrPdfCreatorProperties);
    }

    @Test
    public void skewedRotated45Test() throws IOException, InterruptedException {
        makeSearchable("skewedRotated45");
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = KernelLogMessageConstant.JPXDECODE_FILTER_DECODING, logLevel = LogLevelConstants.INFO),
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_OCR_IMAGE, logLevel = LogLevelConstants.ERROR),
    })
    public void jpeg2000Test() {
        Exception e = Assertions.assertThrows(PdfOcrInputException.class,
                () -> makeSearchableWithoutCompare("jpeg2000"));
        Assertions.assertEquals(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE, e.getMessage());
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_OCR_IMAGE, logLevel = LogLevelConstants.ERROR),
    })
    public void jbig2Test() {
        Exception e = Assertions.assertThrows(PdfOcrInputException.class,
                () -> makeSearchableWithoutCompare("jbig2"));
        Assertions.assertEquals(PdfOcrOnnxExceptionMessageConstant.FAILED_TO_READ_IMAGE, e.getMessage());
    }

    private void makeSearchable(String fileName) throws IOException, InterruptedException {
        makeSearchable(fileName, fileName, null);
    }

    private void makeSearchableWithoutCompare(String fileName) {
        makeSearchableWithoutCompare(fileName, fileName, null);
    }

    private void makeSearchable(String fileName, String outFileName, OcrPdfCreatorProperties ocrPdfCreatorProperties)
            throws IOException, InterruptedException {

        String outPath = makeSearchableWithoutCompare(fileName, outFileName, ocrPdfCreatorProperties);
        String cmpPath = TEST_DIRECTORY + "cmp_" + outFileName + ".pdf";
        Assertions.assertNull(new CompareTool().setContentStreamFloatTolerance(0.02f)
                .compareByContent(outPath, cmpPath, TARGET_DIRECTORY, "diff_"));
    }

    private String makeSearchableWithoutCompare(String fileName, String outFileName, OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        String srcPath = TEST_PDFS_DIRECTORY + fileName + ".pdf";
        String outPath = TARGET_DIRECTORY + outFileName + ".pdf";

        if (ocrPdfCreatorProperties == null) {
            ocrPdfCreatorProperties = new OcrPdfCreatorProperties().setTextColor(DeviceCmyk.MAGENTA);
        }
        OcrPdfCreator ocrPdfCreator = new OcrPdfCreator(OcrEngineTypeWithOrientation.DOCTR.get(), ocrPdfCreatorProperties);
        ocrPdfCreator.makePdfSearchable(new File(srcPath), new File(outPath));
        return outPath;
    }
}
