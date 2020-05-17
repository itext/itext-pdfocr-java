package com.itextpdf.pdfocr.pdflayers;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfLayersIntegrationLibTest extends PdfLayersIntegrationTest {
    public PdfLayersIntegrationLibTest() {
        super(ReaderType.LIB);
    }
}
