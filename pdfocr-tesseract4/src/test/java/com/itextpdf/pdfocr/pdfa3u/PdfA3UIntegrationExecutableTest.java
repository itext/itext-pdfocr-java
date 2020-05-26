package com.itextpdf.pdfocr.pdfa3u;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfA3UIntegrationExecutableTest extends PdfA3UIntegrationTest {
    public PdfA3UIntegrationExecutableTest() {
        super(ReaderType.EXECUTABLE);
    }
}
