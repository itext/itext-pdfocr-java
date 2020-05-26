package com.itextpdf.pdfocr.imageformats;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ImageFormatIntegrationLibTest extends ImageFormatIntegrationTest {
    public ImageFormatIntegrationLibTest() {
        super(ReaderType.LIB);
    }
}
