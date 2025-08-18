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

import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.onnxtr.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class DependencyLoadCheckerTest extends ExtendedITextTest {

    @Test
    public void processExceptionTest() {
        Assertions.assertDoesNotThrow(() -> DependencyLoadChecker.processException(new RuntimeException("Random.")));
        Assertions.assertDoesNotThrow(
                () -> DependencyLoadChecker.processException(new UnsatisfiedLinkError("Random.")));
        Exception e = Assertions.assertThrows(PdfOcrException.class, () -> DependencyLoadChecker.processException(
                new RuntimeException("Failed to load onnx-runtime library")));
        Assertions.assertTrue(e.getMessage().contains(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_LOAD_ONNXRUNTIME));
        e = Assertions.assertThrows(PdfOcrException.class, () -> DependencyLoadChecker.processException(new
                UnsatisfiedLinkError("onnxruntime.dll: A dynamic link library (DLL) initialization routine failed.")));
        Assertions.assertTrue(e.getMessage().contains(PdfOcrOnnxTrExceptionMessageConstant.FAILED_TO_LOAD_ONNXRUNTIME));
    }
}
