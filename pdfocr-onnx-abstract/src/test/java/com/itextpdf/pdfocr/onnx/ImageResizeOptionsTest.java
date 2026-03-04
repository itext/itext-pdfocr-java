/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx;

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class ImageResizeOptionsTest extends ExtendedITextTest {
    @Test
    public void initWithInvalidChannelConfiguration() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new ImageResizeOptions(null, 800, 600)
        );
    }

    @Test
    public void initWithInvalidPaddingStrategy() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 800, 600, null)
        );
    }

    @Test
    public void initWithInvalidMinWidth() {
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 0, 10, 100, 100)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.MIN_WIDTH_SHOULD_BE_POSITIVE, 0
                ),
                e.getMessage()
        );
    }

    @Test
    public void initWithInvalidMinHeight() {
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 0, 100, 100)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.MIN_HEIGHT_SHOULD_BE_POSITIVE, 0
                ),
                e.getMessage()
        );
    }

    @Test
    public void initWithInvalidMaxWidth() {
        // max < min
        final Exception e1 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 10, 9, 100)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.MAX_WIDTH_SHOULD_NOT_BE_LESS_THAN_MIN, 9
                ),
                e1.getMessage()
        );
        // max not multiple
        final Exception e2 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 10, 99, 100, 10, 1,
                        PaddingStrategy.BOTTOM_RIGHT_BLACK)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.MAX_WIDTH_SHOULD_BE_A_MULTIPLE, 10, 99
                ),
                e2.getMessage()
        );
    }

    @Test
    public void initWithInvalidMaxHeight() {
        // max < min
        final Exception e1 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 10, 100, 9)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.MAX_HEIGHT_SHOULD_NOT_BE_LESS_THAN_MIN, 9
                ),
                e1.getMessage()
        );
        // max not multiple
        final Exception e2 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 10, 100, 99, 1, 10,
                        PaddingStrategy.BOTTOM_RIGHT_BLACK)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.MAX_HEIGHT_SHOULD_BE_A_MULTIPLE, 10, 99
                ),
                e2.getMessage()
        );
    }

    @Test
    public void initWithInvalidWidthMultiple() {
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 10, 100, 100, 0, 10,
                        PaddingStrategy.BOTTOM_RIGHT_BLACK)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.WIDTH_MULTIPLE_SHOULD_BE_POSITIVE, 0
                ),
                e.getMessage()
        );
    }

    @Test
    public void initWithInvalidHeightMultiple() {
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageResizeOptions(ImageChannelConfiguration.BGR, 10, 10, 100, 100, 10, 0,
                        PaddingStrategy.BOTTOM_RIGHT_BLACK)
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxExceptionMessageConstant.HEIGHT_MULTIPLE_SHOULD_BE_POSITIVE, 0
                ),
                e.getMessage()
        );
    }

    @Test
    public void validWithVariableSize() {
        final ImageResizeOptions opts = new ImageResizeOptions(
                ImageChannelConfiguration.GRAYSCALE,
                640, 480,
                800, 600,
                16, 8,
                PaddingStrategy.SYMMETRIC_BLACK
        );
        Assertions.assertEquals(ImageChannelConfiguration.GRAYSCALE, opts.getChannelConfiguration());
        Assertions.assertEquals(640, opts.getMinWidth());
        Assertions.assertEquals(480, opts.getMinHeight());
        Assertions.assertEquals(800, opts.getMaxWidth());
        Assertions.assertEquals(600, opts.getMaxHeight());
        Assertions.assertEquals(16, opts.getWidthMultiple());
        Assertions.assertEquals(8, opts.getHeightMultiple());
        Assertions.assertEquals(PaddingStrategy.SYMMETRIC_BLACK, opts.getPaddingStrategy());
        Assertions.assertFalse(opts.isFixedSize());

        final ImageResizeOptions optsCopy = new ImageResizeOptions(
                ImageChannelConfiguration.GRAYSCALE,
                640, 480,
                800, 600,
                16, 8,
                PaddingStrategy.SYMMETRIC_BLACK
        );
        Assertions.assertEquals(optsCopy.hashCode(), opts.hashCode());
        Assertions.assertEquals(optsCopy, opts);
        Assertions.assertNotEquals(new ImageResizeOptions(ImageChannelConfiguration.GRAYSCALE, 640, 480), opts);
    }

    @Test
    public void validWithFixedSize() {
        final ImageResizeOptions opts = new ImageResizeOptions(ImageChannelConfiguration.BGR, 1920, 1080);
        Assertions.assertEquals(ImageChannelConfiguration.BGR, opts.getChannelConfiguration());
        Assertions.assertEquals(1920, opts.getMinWidth());
        Assertions.assertEquals(1080, opts.getMinHeight());
        Assertions.assertEquals(1920, opts.getMaxWidth());
        Assertions.assertEquals(1080, opts.getMaxHeight());
        Assertions.assertEquals(1, opts.getWidthMultiple());
        Assertions.assertEquals(1, opts.getHeightMultiple());
        Assertions.assertEquals(PaddingStrategy.BOTTOM_RIGHT_BLACK, opts.getPaddingStrategy());
        Assertions.assertTrue(opts.isFixedSize());

        final ImageResizeOptions optsCopy = new ImageResizeOptions(ImageChannelConfiguration.BGR, 1920, 1080);
        Assertions.assertEquals(optsCopy.hashCode(), opts.hashCode());
        Assertions.assertEquals(optsCopy, opts);
        Assertions.assertNotEquals(new ImageResizeOptions(ImageChannelConfiguration.RGB, 1920, 1080), opts);
    }
}
