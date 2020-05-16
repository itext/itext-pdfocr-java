package com.itextpdf.ocr.pdfa3u;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfA3UIntegrationLibTest extends PdfA3UIntegrationTest {
    public PdfA3UIntegrationLibTest() {
        super(ReaderType.LIB);
    }
}
