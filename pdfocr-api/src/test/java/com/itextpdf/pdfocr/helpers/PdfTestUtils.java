package com.itextpdf.pdfocr.helpers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.slf4j.LoggerFactory;

public class PdfTestUtils {
    // directory with test files
    protected static String testDirectory = "com/itextpdf/pdfocr/";

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
