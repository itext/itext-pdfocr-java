package com.itextpdf.ocr.general;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class BasicTesseractIntegrationTestExecutable extends BasicTesseractIntegrationTest {
    public BasicTesseractIntegrationTestExecutable() {
        super("executable");
    }
}
