package com.itextpdf.ocr;

import java.io.File;
import java.net.URL;

public class TestUtils {

    // directory with test files
    protected static String testDirectory = "com/itextpdf/ocr/";

    /**
     * Return current test directory.
     *
     * @return String
     */
    public static String getCurrentDirectory() {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(testDirectory);
        return new File(url.getFile()).getAbsolutePath() + File.separatorChar;
    }
}
