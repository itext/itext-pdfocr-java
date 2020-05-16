package com.itextpdf.ocr.tessdata;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class TessDataIntegrationExecutableTest extends TessDataIntegrationTest {
    public TessDataIntegrationExecutableTest() {
        super(ReaderType.EXECUTABLE);
    }
}
