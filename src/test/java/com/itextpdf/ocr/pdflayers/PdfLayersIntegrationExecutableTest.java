package com.itextpdf.ocr.pdflayers;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfLayersIntegrationExecutableTest extends PdfLayersIntegrationTest {
    public PdfLayersIntegrationExecutableTest() {
        super(ReaderType.EXECUTABLE);
    }
}
