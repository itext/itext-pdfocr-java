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
package com.itextpdf.pdfocr.onnx.orientation;

import com.itextpdf.pdfocr.onnx.DefaultOrtSessionOptionsCreator;
import com.itextpdf.pdfocr.onnx.ImageChannelConfiguration;
import com.itextpdf.pdfocr.onnx.ImageResizeOptions;
import com.itextpdf.pdfocr.onnx.OnnxInputProperties;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class OnnxOrientationPredictorPropertiesTest extends ExtendedITextTest {
    private static final String BASE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String MOBILENETV3 = BASE_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";

    @Test
    public void equalsWithConstructorsTest() {
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions(ImageChannelConfiguration.RGB, 10, 10);
        DefaultOrientationMapper outputMapper = new DefaultOrientationMapper();
        OnnxOrientationPredictorProperties first = new OnnxOrientationPredictorProperties(
                MOBILENETV3, new OnnxInputProperties(imageResizeOptions), outputMapper);

        OnnxOrientationPredictorProperties second = new OnnxOrientationPredictorProperties(
                MOBILENETV3, new OnnxInputProperties(imageResizeOptions), outputMapper, new DefaultOrtSessionOptionsCreator());

        Assertions.assertNotEquals(first, second);
        Assertions.assertNotEquals(first.hashCode(), second.hashCode());

        OnnxOrientationPredictorProperties third = new OnnxOrientationPredictorProperties(
                MOBILENETV3, new OnnxInputProperties(imageResizeOptions), new DefaultOrientationMapper());
        Assertions.assertNotEquals(first, third);
        Assertions.assertNotEquals(first.hashCode(), third.hashCode());

        OnnxOrientationPredictorProperties fourth = new OnnxOrientationPredictorProperties(
                MOBILENETV3, new OnnxInputProperties(imageResizeOptions), outputMapper);
        Assertions.assertEquals(first, fourth);
        Assertions.assertEquals(first.hashCode(), fourth.hashCode());
    }
}
