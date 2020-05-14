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

    /**
     * Gets current system temporary directory.
     *
     * @return path to system temporary directory
     */
    public static String getTempDir() {
        String tempDir = System.getProperty("java.io.tmpdir") == null
                ? System.getProperty("TEMP")
                : System.getProperty("java.io.tmpdir");
        if (!(tempDir.endsWith("/") || tempDir.endsWith("\\"))) {
            tempDir = tempDir + java.io.File.separatorChar;
        }
        return tempDir;
    }
}
