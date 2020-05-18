package com.itextpdf.pdfocr.helpers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.slf4j.LoggerFactory;

public class TestDirectoryUtils {
    public static final String DEFAULT_IMAGE_NAME = "numbers_01.jpg";
    public static final String DEFAULT_EXPECTED_RESULT = "619121";
    // directory with test files
    private static final String testDirectory = "./src/test/resources/com"
            + "/itextpdf/pdfocr/";

    /**
     * Return images test directory.
     *
     * @return String
     */
    public static String getImagesTestDirectory() {
        return getCurrentDirectory() + "images/";
    }

    public static String getFreeSansFontPath() {
        return getCurrentDirectory() + "fonts/FreeSans.ttf";
    }

    /**
     * Return current test directory.
     *
     * @return String
     */
    public static String getCurrentDirectory() {
        return testDirectory;
    }
}
