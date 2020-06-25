/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
    Authors: iText Software.

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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.pdfocr.IntegrationTestHelper;

import java.io.File;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ImagePreprocessingUtilTest extends IntegrationTestHelper{

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @Test
    public void testCheckForInvalidTiff() {
        String path = TEST_IMAGES_DIRECTORY + "example_03_10MB";
        File imgFile = new File(path);
        Assert.assertFalse(ImagePreprocessingUtil.isTiffImage(imgFile));
    }

    @Test
    public void testReadingInvalidImagePath() {
        junitExpectedException.expect(Tesseract4OcrException.class);
        String path = TEST_IMAGES_DIRECTORY + "numbers_02";
        File imgFile = new File(path);
        ImagePreprocessingUtil.preprocessImage(imgFile, 1);
    }
}
