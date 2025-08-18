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
package com.itextpdf.pdfocr.util;

import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

@Tag("IntegrationTest")
public class TiffImageUtilTest extends ExtendedITextTest {

    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE)
    })
    @Test
    public void getAllImagesMissingFileTest() {
        String path = PdfHelper.getImagesTestDirectory() + "missing.tiff";
        List<BufferedImage> images = TiffImageUtil.getAllImages(new File(path));
        Assertions.assertEquals(0, images.size());
    }

    @Test
    public void getAllImagesTest() {
        String path = PdfHelper.getImagesTestDirectory() + "multipage.tiff";
        List<BufferedImage> images = TiffImageUtil.getAllImages(new File(path));
        Assertions.assertEquals(9, images.size());
    }

    @Test
    public void isTiffImageTest() {
        String path = PdfHelper.getImagesTestDirectory() + "thai.PNG";
        Assertions.assertFalse(TiffImageUtil.isTiffImage(new File(path)));

        path = PdfHelper.getImagesTestDirectory() + "single7x5cm.tif";
        Assertions.assertTrue(TiffImageUtil.isTiffImage(new File(path)));
    }
}
