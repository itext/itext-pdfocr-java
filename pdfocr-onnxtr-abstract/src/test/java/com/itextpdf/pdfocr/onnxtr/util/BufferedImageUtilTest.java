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
package com.itextpdf.pdfocr.onnxtr.util;

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.pdfocr.onnxtr.ImageChannelConfiguration;
import com.itextpdf.pdfocr.onnxtr.ImageResizeOptions;
import com.itextpdf.pdfocr.onnxtr.OnnxInputProperties;
import com.itextpdf.pdfocr.onnxtr.PaddingStrategy;
import com.itextpdf.test.ExtendedITextTest;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("UnitTest")
public class BufferedImageUtilTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY =
            "./src/test/resources/com/itextpdf/pdfocr/onnxtr/util/BufferedImageUtilTest/";

    @Test
    public void toBchwInputGrayBasicTest() {
        // Intent is to test normalization and BufferedImage to
        // FloatBufferMdArray conversion, no resizing is expected here
        final long[] expectedShape = new long[]{2, 1, 2, 3};
        final float[] expectedData = new float[]{
                -0.333333F, -0.249673F, -0.166013F,
                -0.307190F, -0.223529F, -0.139869F,

                -0.082353F,  0.001307F,  0.084967F,
                -0.056209F,  0.027451F,  0.111111F,
        };
        final Collection<BufferedImage> images = Arrays.asList(
                newGrayImage(3, 2, new byte[]{
                        0x00, 0x10, 0x20,
                        0x05, 0x15, 0x25,
                }),
                newGrayImage(3, 2, new byte[]{
                        0x30, 0x40, 0x50,
                        0x35, 0x45, 0x55,
                })
        );
        final OnnxInputProperties props = new OnnxInputProperties(
                new ImageResizeOptions(ImageChannelConfiguration.GRAYSCALE, 3, 2),
                new float[]{0.25F},
                new float[]{0.75F},
                3
        );
        toBchwInputBasicTest(expectedShape, expectedData, images, props);
    }

    @Test
    public void toBchwInputRgbBasicTest() {
        final long[] expectedShape = new long[]{2, 3, 1, 2};
        final float[] expectedData = new float[]{
                 0.616073F, -0.211813F,
                -2.294872F,  0.148567F,
                -0.015602F,  0.549441F,

                -0.308642F,  0.199710F,
                -0.228507F,  0.978130F,
                 0.815096F,  0.532574F,
        };
        final List<BufferedImage> images = Arrays.asList(
                newRgbImage(2, 1, new int[]{
                        0xBF2220, 0x14C4A6,
                }),
                newRgbImage(2, 1, new int[]{
                        0x00ABE5, 0x69FBA2,
                })
        );
        final OnnxInputProperties props = new OnnxInputProperties(
                new ImageResizeOptions(ImageChannelConfiguration.RGB, 2, 1),
                new float[]{0.25F, 0.73F, 0.14F},
                new float[]{0.81F, 0.26F, 0.93F},
                4
        );
        toBchwInputBasicTest(expectedShape, expectedData, images, props);
    }

    @Test
    public void toBchwInputBgrBasicTest() {
        final long[] expectedShape = new long[]{2, 3, 1, 2};
        final float[] expectedData = new float[]{
                 0.423770F,  0.403762F,
                 2.203361F,  1.564706F,
                -1.071012F, -0.625861F,

                -0.016407F,  0.679872F,
                 1.844818F,  2.024090F,
                -1.696343F,  0.200848F,
        };
        final List<BufferedImage> images = Arrays.asList(
                newRgbImage(2, 1, new int[]{
                        0x83F0A2, 0xADB79D,
                }),
                newRgbImage(2, 1, new int[]{
                        0x48D034, 0xFBE0E2,
                })
        );
        final OnnxInputProperties props = new OnnxInputProperties(
                new ImageResizeOptions(ImageChannelConfiguration.BGR, 2, 1),
                new float[]{0.22F, 0.17F, 0.91F},
                new float[]{0.98F, 0.35F, 0.37F},
                5
        );
        toBchwInputBasicTest(expectedShape, expectedData, images, props);
    }

    public static Iterable<Object[]> resizeTestParams() {
        return Arrays.asList(new Object[][] {
                {"resize_120_80_brb.png", 120, 80, PaddingStrategy.BOTTOM_RIGHT_BLACK},
                {"resize_120_80_brg.png", 120, 80, PaddingStrategy.BOTTOM_RIGHT_GRAY},
                {"resize_120_80_brw.png", 120, 80, PaddingStrategy.BOTTOM_RIGHT_WHITE},
                {"resize_120_80_bre.png", 120, 80, PaddingStrategy.BOTTOM_RIGHT_EDGE},
                {"resize_120_80_sb.png", 120, 80, PaddingStrategy.SYMMETRIC_BLACK},
                {"resize_120_80_sg.png", 120, 80, PaddingStrategy.SYMMETRIC_GRAY},
                {"resize_120_80_sw.png", 120, 80, PaddingStrategy.SYMMETRIC_WHITE},
                {"resize_120_80_se.png", 120, 80, PaddingStrategy.SYMMETRIC_EDGE},
                {"resize_100_80_brb.png", 100, 80, PaddingStrategy.BOTTOM_RIGHT_BLACK},
                {"resize_100_80_brg.png", 100, 80, PaddingStrategy.BOTTOM_RIGHT_GRAY},
                {"resize_100_80_brw.png", 100, 80, PaddingStrategy.BOTTOM_RIGHT_WHITE},
                {"resize_100_80_bre.png", 100, 80, PaddingStrategy.BOTTOM_RIGHT_EDGE},
                {"resize_100_80_sb.png", 100, 80, PaddingStrategy.SYMMETRIC_BLACK},
                {"resize_100_80_sg.png", 100, 80, PaddingStrategy.SYMMETRIC_GRAY},
                {"resize_100_80_sw.png", 100, 80, PaddingStrategy.SYMMETRIC_WHITE},
                {"resize_100_80_se.png", 100, 80, PaddingStrategy.SYMMETRIC_EDGE},
        });
    }

    @ParameterizedTest(name = "resize: {0}")
    @MethodSource("resizeTestParams")
    public void resizeTest(String cmpFileName, int width, int height, PaddingStrategy paddingStrategy) throws IOException {
        final BufferedImage inputImage = ImageIO.read(new File(TEST_DIRECTORY + "resize_base.png"));
        final BufferedImage expectedImage = ImageIO.read(new File(TEST_DIRECTORY + cmpFileName));
        final BufferedImage actualImage = BufferedImageUtil.resize(
                inputImage, width, height, paddingStrategy, expectedImage.getType()
        );
        assertImagesEqual(expectedImage, actualImage);
    }

    public static Iterable<Object[]> calcOutputDimensionsSingleTestParams() {
        return Arrays.asList(new Object[][] {
                // Fixed output size: should always be the same
                {new Dimensions2D(800, 600), new Dimensions2D(20, 30),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 800, 600)},
                {new Dimensions2D(800, 600), new Dimensions2D(30, 20),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 800, 600)},
                {new Dimensions2D(800, 600), new Dimensions2D(1000, 900),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 800, 600)},
                {new Dimensions2D(800, 600), new Dimensions2D(900, 1000),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 800, 600)},
                // Variable output size with matching input: input should remain as-is
                {new Dimensions2D(250, 350), new Dimensions2D(250, 350),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 200, 200, 400, 400)},
                // Variable output size with non-matching input: input should get resized/padded
                {new Dimensions2D(150, 300), new Dimensions2D(75, 150),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(100, 400), new Dimensions2D(75, 300),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(200, 300), new Dimensions2D(100, 150),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(150, 400), new Dimensions2D(300, 800),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(200, 300), new Dimensions2D(400, 600),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(200, 300), new Dimensions2D(600, 600),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(100, 400), new Dimensions2D(50, 300),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                {new Dimensions2D(200, 300), new Dimensions2D(150, 200),
                        new ImageResizeOptions(ImageChannelConfiguration.RGB, 100, 300, 200, 400)},
                // Multiple sanity test
                {new Dimensions2D(155, 350), new Dimensions2D(152, 343),
                        new ImageResizeOptions(
                                ImageChannelConfiguration.RGB,
                                100, 300,
                                200, 400,
                                5, 10,
                                PaddingStrategy.BOTTOM_RIGHT_BLACK
                        )},
        });
    }

    @ParameterizedTest(name = "calcOutputDimensionsSingle: #{index}")
    @MethodSource("calcOutputDimensionsSingleTestParams")
    public void calcOutputDimensionsSingleTest(
            Dimensions2D expectedOutputDimensions,
            Dimensions2D inputDimensions,
            ImageResizeOptions resizeOptions
    ) {
        final BufferedImage img = newBlankInputImage(inputDimensions);
        final Dimensions2D actualOutputDimensions = BufferedImageUtil.calcOutputDimensions(img, resizeOptions);
        Assertions.assertEquals(expectedOutputDimensions, actualOutputDimensions);
    }

    public static Iterable<Object[]> truncateToRatioTestParams() {
        return Arrays.asList(new Object[][] {
                {new Dimensions2D(100, 20), new Dimensions2D(100, 20), 8.},
                {new Dimensions2D(160, 20), new Dimensions2D(1000, 20), 8.},
                {new Dimensions2D(100, 800), new Dimensions2D(100, 2000), 8.},
        });
    }

    @ParameterizedTest(name = "truncateToRatioTest: {1}")
    @MethodSource("truncateToRatioTestParams")
    public void truncateToRatioTest(Dimensions2D expectedSize, Dimensions2D inputSize, double ratioLimit) {
        final BufferedImage img = newBlankInputImage(inputSize);
        final BufferedImage truncated = BufferedImageUtil.truncateToRatio(img, ratioLimit);
        Assertions.assertEquals(expectedSize.getWidth(), truncated.getWidth());
        Assertions.assertEquals(expectedSize.getHeight(), truncated.getHeight());
    }

    private static void toBchwInputBasicTest(
            long[] expectedShape,
            float[] expectedData,
            Collection<BufferedImage> images,
            OnnxInputProperties props
    ) {
        final FloatBufferMdArray result = BufferedImageUtil.toBchwInput(images, props);
        Assertions.assertArrayEquals(expectedShape, result.getShape());
        final float[] actualData = new float[12];
        result.getData().get(actualData);
        Assertions.assertArrayEquals(expectedData, actualData, 1E-6F);
    }

    private static BufferedImage newBlankInputImage(Dimensions2D dims) {
        return new BufferedImage(dims.getWidth(), dims.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    }

    private static BufferedImage newGrayImage(int width, int height, byte[] pixels) {
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        final WritableRaster raster = img.getRaster();
        raster.setDataElements(0, 0, width, height, pixels);
        return img;
    }

    private static BufferedImage newRgbImage(int width, int height, int[] pixels) {
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final WritableRaster raster = img.getRaster();
        raster.setDataElements(0, 0, width, height, pixels);
        return img;
    }

    private static void assertImagesEqual(BufferedImage expected, BufferedImage actual) {
        Assertions.assertEquals(expected.getWidth(), actual.getWidth(), "Image width differs");
        Assertions.assertEquals(expected.getHeight(), actual.getHeight(), "Image height differs");
        Assertions.assertEquals(expected.getType(), actual.getType(), "Image type differs");

        // Kind of slow, but should be fine for small test images
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Assertions.assertEquals(
                        expected.getRGB(x, y),
                        actual.getRGB(x, y),
                        String.format("Image pixel value differs at (%d, %d)", x, y)
                );
            }
        }
    }
}
