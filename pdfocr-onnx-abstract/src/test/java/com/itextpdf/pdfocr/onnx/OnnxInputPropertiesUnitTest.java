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
import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class OnnxInputPropertiesUnitTest extends ExtendedITextTest {
    @Test
    public void unexpectedMeanChannelCountTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MEAN_CHANNEL_COUNT, 3), e.getMessage());
    }

    @Test
    public void unexpectedStdChannelCountTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F, 0.772F};
        long[] shape = new long[]{2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_STD_CHANNEL_COUNT, 3), e.getMessage());
    }

    @Test
    public void unexpectedShapeSizeTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 3, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_SHAPE_SIZE, 4), e.getMessage());
    }

    @Test
    public void unexpectedShapeChannelCountTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{2, 4, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(PdfOcrOnnxTrExceptionMessageConstant.MODEL_ONLY_SUPPORTS_RGB, e.getMessage());
    }

    @Test
    public void unexpectedDimensionValueTest() {
        float[] mean = new float[]{0.798F, 0.785F, 0.772F};
        float[] std = new float[]{0.264F, 0.2749F, 0.287F};
        long[] shape = new long[]{-2, 3, 1024, 1024};
        Exception e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new OnnxInputProperties(mean, std, shape, true));
        Assertions.assertEquals(MessageFormatUtil.format(
                PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_DIMENSION_VALUE, -2), e.getMessage());
    }

    @Test
    public void initWithInvalidImageResizeOptions() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new OnnxInputProperties(null)
        );
    }

    @Test
    public void initWithInvalidMean() {
        final ImageResizeOptions resizeOptions = new ImageResizeOptions(
                ImageChannelConfiguration.RGB, 800, 600
        );
        // null
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new OnnxInputProperties(
                        resizeOptions,
                        null,
                        new float[]{1F, 2F, 3F}
                )
        );
        // invalid size
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new OnnxInputProperties(
                        resizeOptions,
                        new float[]{0.3F, 0.4F, 0.5F, 0.6F},
                        new float[]{1F, 2F, 3F}
                )
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_MEAN_CHANNEL_COUNT,
                        resizeOptions.getChannelConfiguration().getChannelCount()
                ),
                e.getMessage()
        );
    }

    @Test
    public void initWithInvalidStd() {
        final ImageResizeOptions resizeOptions = new ImageResizeOptions(
                ImageChannelConfiguration.RGB, 800, 600
        );
        // null
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new OnnxInputProperties(
                        resizeOptions,
                        new float[]{0.3F, 0.4F, 0.5F},
                        null
                )
        );
        // invalid size
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new OnnxInputProperties(
                        resizeOptions,
                        new float[]{0.3F, 0.4F, 0.5F},
                        new float[]{1F, 2F}
                )
        );
        Assertions.assertEquals(
                MessageFormatUtil.format(
                        PdfOcrOnnxTrExceptionMessageConstant.UNEXPECTED_STD_CHANNEL_COUNT,
                        resizeOptions.getChannelConfiguration().getChannelCount()
                ),
                e.getMessage()
        );
    }

    @Test
    public void initWithInvalidBatchSize() {
        final ImageResizeOptions resizeOptions = new ImageResizeOptions(
                ImageChannelConfiguration.RGB, 800, 600
        );
        final Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new OnnxInputProperties(resizeOptions, 0)
        );
        Assertions.assertEquals(
                PdfOcrOnnxTrExceptionMessageConstant.BATCH_SIZE_SHOULD_BE_POSITIVE,
                e.getMessage()
        );
    }

    @Test
    public void valid() {
        final ImageResizeOptions resizeOptions = new ImageResizeOptions(
                ImageChannelConfiguration.BGR, 800, 600, 1024, 768, 4, 2, PaddingStrategy.BOTTOM_RIGHT_WHITE
        );
        final float[] mean = new float[]{0.1F, 0.2F, 0.3F};
        final float[] std = new float[]{1F, 2F, 3F};
        final int batchSize = 5;
        final OnnxInputProperties props = new OnnxInputProperties(resizeOptions, mean, std, batchSize);

        Assertions.assertSame(resizeOptions, props.getImageResizeOptions());

        Assertions.assertArrayEquals(mean, props.getMean());
        Assertions.assertEquals(mean[0], props.getGrayMean());
        Assertions.assertEquals(mean[2], props.getRedMean());
        Assertions.assertEquals(mean[1], props.getGreenMean());
        Assertions.assertEquals(mean[0], props.getBlueMean());

        Assertions.assertArrayEquals(std, props.getStd());
        Assertions.assertEquals(std[0], props.getGrayStd());
        Assertions.assertEquals(std[2], props.getRedStd());
        Assertions.assertEquals(std[1], props.getGreenStd());
        Assertions.assertEquals(std[0], props.getBlueStd());

        final long[] expectedShape = new long[]{
                batchSize,
                resizeOptions.getChannelConfiguration().getChannelCount(),
                resizeOptions.getMinHeight(),
                resizeOptions.getMinWidth()
        };
        Assertions.assertArrayEquals(expectedShape, props.getShape());
        Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> props.getShape(-1)
        );
        for (int i = 0; i < 4; ++i) {
            Assertions.assertEquals(expectedShape[i], props.getShape(i));
        }
        Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> props.getShape(4)
        );
        Assertions.assertEquals(batchSize, props.getBatchSize());
        Assertions.assertEquals(resizeOptions.getChannelConfiguration().getChannelCount(), props.getChannelCount());
        Assertions.assertEquals(resizeOptions.getMinHeight(), props.getHeight());
        Assertions.assertEquals(resizeOptions.getMinWidth(), props.getWidth());

        Assertions.assertEquals(resizeOptions.getPaddingStrategy().usesSymmetricPadding(), props.useSymmetricPad());
        Assertions.assertEquals(resizeOptions.getPaddingStrategy(), props.getPaddingStrategy());

        final OnnxInputProperties propsCopy = new OnnxInputProperties(
                new ImageResizeOptions(
                        ImageChannelConfiguration.BGR, 800, 600, 1024, 768, 4, 2, PaddingStrategy.BOTTOM_RIGHT_WHITE
                ),
                new float[]{0.1F, 0.2F, 0.3F},
                new float[]{1F, 2F, 3F},
                5
        );
        Assertions.assertEquals(propsCopy.hashCode(), props.hashCode());
        Assertions.assertEquals(propsCopy, props);
        Assertions.assertNotEquals(
                new OnnxInputProperties(
                        new ImageResizeOptions(ImageChannelConfiguration.GRAYSCALE, 800, 600)
                ),
                props
        );
    }
}
