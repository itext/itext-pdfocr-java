/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.pdfocr;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageType;
import com.itextpdf.io.image.JpegImageData;
import com.itextpdf.io.image.TiffImageData;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.helpers.PdfHelper;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(UnitTest.class)
public class PdfCreatorUtilTest extends ExtendedITextTest {

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @Test
    public void getImageDataFromValidSinglePagedTiffTest() throws IOException {
        File image = new File(PdfHelper.getImagesTestDirectory() + "single7x5cm.tif");
        List<ImageData> images = PdfCreatorUtil.getImageData(image, null);

        Assert.assertEquals(1, images.size());

        ImageData imageDate = images.get(0);
        Assert.assertNotNull(imageDate);
        Assert.assertTrue(imageDate instanceof TiffImageData);
        Assert.assertEquals(ImageType.TIFF, imageDate.getOriginalType());
    }

    @Test
    public void getImageDataFromValidMultiPagedTiffTest() throws IOException {
        File image = new File(PdfHelper.getImagesTestDirectory() + "multipage.tiff");
        List<ImageData> images = PdfCreatorUtil.getImageData(image, null);

        Assert.assertEquals(9, images.size());
        for (ImageData imageDate : images) {
            Assert.assertNotNull(imageDate);
            Assert.assertTrue(imageDate instanceof TiffImageData);
            Assert.assertEquals(ImageType.TIFF, imageDate.getOriginalType());
        }
    }

    @Test
    public void getImageDataFromValidNotTiffTest() throws IOException {
        File image = new File(PdfHelper.getImagesTestDirectory() + "numbers_01.jpg");
        List<ImageData> images = PdfCreatorUtil.getImageData(image, null);

        Assert.assertEquals(1, images.size());

        ImageData imageDate = images.get(0);
        Assert.assertNotNull(imageDate);
        Assert.assertTrue(imageDate instanceof JpegImageData);
        Assert.assertEquals(ImageType.JPEG, imageDate.getOriginalType());
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    public void getImageDataFromNotExistingImageTest() throws IOException {
        junitExpectedException.expect(PdfOcrInputException.class);

        PdfCreatorUtil.getImageData(new File("no such path"), null);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE)
    })
    public void getImageDataFromInvalidImageTest() throws IOException {
        junitExpectedException.expect(PdfOcrInputException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(
                PdfOcrExceptionMessageConstant.CANNOT_READ_INPUT_IMAGE));

        PdfCreatorUtil.getImageData(new File(PdfHelper.getImagesTestDirectory() + "corrupted.jpg"),
                null);
    }
}
