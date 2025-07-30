/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.commons.utils.StringNormalizer;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;

/**
 * Internal helper class to construct a friendlier error in case some dependency couldn’t be loaded.
 *
 * <p>
 * NOTE: for internal usage only. Be aware that its API and functionality may be changed in the future.
 */
final class DependencyLoadChecker {
    private DependencyLoadChecker() {
        // Private constructor will prevent the instantiation of this class directly.
    }

    /**
     * Processes the exception or error: checks if exception is related to some dependency that couldn’t be loaded and
     * in that case constructs exception with a friendlier error message, otherwise, throws exception as is.
     *
     * @param throwable exception or error to process
     */
    public static void processException(Throwable throwable) {
        String throwableMessage = throwable.getMessage();
        boolean isOnnxRuntime = (throwable instanceof RuntimeException &&
                throwableMessage.contains("Failed to load onnx-runtime library")) ||
                (throwable instanceof UnsatisfiedLinkError && throwableMessage.contains("onnxruntime"));
        if (isOnnxRuntime) {
            String message = getOnnxRuntimeError();
            throw new PdfOcrException(message, throwable);
        }
    }

    private static String getOnnxRuntimeError() {
        String message = PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_LOAD_ONNXRUNTIME;
        if (isWindows()) {
            message += "\nPossible causes for Windows:\n" +
                    "lack of the latest version of the VC++ redistributable " +
                    "(solution: install it);\n" +
                    "for Oracle JVMs, mismatch of MSVC runtime version " +
                    "(solution: upgrade the JVM to a version compiled with newer VC libraries).\n";
        }
        return message;
    }

    /**
     * Checks current OS type.
     *
     * @return boolean {@code true} is current OS is Windows, otherwise - {@code false}
     */
    private static boolean isWindows() {
        return StringNormalizer.toLowerCase(identifyOsType()).contains("win");
    }

    /**
     * Identifies type of current OS and return it (win, linux).
     *
     * @return type of current os as {@link java.lang.String}
     */
    private static String identifyOsType() {
        String os = System.getProperty("os.name") == null
                ? System.getProperty("OS") : System.getProperty("os.name");
        return StringNormalizer.toLowerCase(os);
    }
}
