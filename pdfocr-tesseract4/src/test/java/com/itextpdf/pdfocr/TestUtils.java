package com.itextpdf.pdfocr;

public class TestUtils {

    // directory with test files
    private static final String testDirectory = "./src/test/resources/com/itextpdf/ocr/";

    /**
     * Return current test directory.
     *
     * @return String
     */
    protected static String getCurrentDirectory() {
        return testDirectory;
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
