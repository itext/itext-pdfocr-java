package com.itextpdf.pdfocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.pdfocr.helpers.CustomOcrEngine;
import com.itextpdf.pdfocr.helpers.ExtractionStrategy;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.helpers.TestDirectoryUtils;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ScaleModeTest extends ExtendedITextTest {

    private static float delta = 1e-4f;

    @Test
    public void testScaleWidthMode() throws IOException {
        String testName = "testScaleWidthMode";
        String srcPath = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";

        File file = new File(srcPath);

        float pageWidthPt = 400f;
        float pageHeightPt = 400f;

        com.itextpdf.kernel.geom.Rectangle pageSize =
                new com.itextpdf.kernel.geom.Rectangle(pageWidthPt, pageHeightPt);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setScaleMode(ScaleMode.SCALE_WIDTH);
        properties.setPageSize(pageSize);

        PdfHelper.createPdf(pdfPath, file, properties);

        com.itextpdf.kernel.geom.Rectangle rect = getImageBBoxRectangleFromPdf(pdfPath);
        ImageData originalImageData = ImageDataFactory.create(file.getAbsolutePath());

        // page size should be equal to the result image size
        // result image height should be equal to the value that
        // was set as page height result image width should be scaled
        // proportionally according to the provided image height
        // and original image size
        Assert.assertEquals(pageHeightPt, rect.getHeight(), delta);
        Assert.assertEquals(originalImageData.getWidth() / originalImageData.getHeight(),
                rect.getWidth() / rect.getHeight(), delta);
    }

    @Test
    public void testScaleHeightMode() throws IOException {
        String testName = "testScaleHeightMode";
        String srcPath = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        String pdfPath = TestDirectoryUtils.getCurrentDirectory() + testName + ".pdf";

        File file = new File(srcPath);

        float pageWidthPt = 400f;
        float pageHeightPt = 400f;

        com.itextpdf.kernel.geom.Rectangle pageSize =
                new com.itextpdf.kernel.geom.Rectangle(pageWidthPt, pageHeightPt);

        OcrPdfCreatorProperties properties = new OcrPdfCreatorProperties();
        properties.setScaleMode(ScaleMode.SCALE_HEIGHT);
        properties.setPageSize(pageSize);

        PdfHelper.createPdf(pdfPath, file, properties);

        com.itextpdf.kernel.geom.Rectangle rect = getImageBBoxRectangleFromPdf(pdfPath);
        ImageData originalImageData = ImageDataFactory.create(file.getAbsolutePath());

        Assert.assertEquals(pageWidthPt, rect.getWidth(), delta);
        Assert.assertEquals(originalImageData.getWidth() / originalImageData.getHeight(),
                rect.getWidth() / rect.getHeight(), delta);
    }

    @Test
    public void testOriginalSizeScaleMode() throws IOException {
        String filePath = TestDirectoryUtils.getImagesTestDirectory() + TestDirectoryUtils.DEFAULT_IMAGE_NAME;
        File file = new File(filePath);

        PdfRenderer pdfRenderer = new PdfRenderer(new CustomOcrEngine());
        PdfDocument doc =
                pdfRenderer.createPdf(Collections.<File>singletonList(file),
                        PdfHelper.getPdfWriter());

        Assert.assertNotNull(doc);

        ImageData imageData = ImageDataFactory.create(file.getAbsolutePath());

        float imageWidth = getPoints(imageData.getWidth());
        float imageHeight = getPoints(imageData.getHeight());
        float realWidth = doc.getFirstPage().getPageSize().getWidth();
        float realHeight = doc.getFirstPage().getPageSize().getHeight();

        Assert.assertEquals(imageWidth, realWidth, delta);
        Assert.assertEquals(imageHeight, realHeight, delta);

        doc.close();
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
     * Retrieve image BBox rectangle from the first page from given pdf document.
     */
    public static Rectangle getImageBBoxRectangleFromPdf(String path)
            throws IOException {
        ExtractionStrategy extractionStrategy =
                PdfHelper.getExtractionStrategy(path, "Image Layer");
        return extractionStrategy.getImageBBoxRectangle();
    }
}
