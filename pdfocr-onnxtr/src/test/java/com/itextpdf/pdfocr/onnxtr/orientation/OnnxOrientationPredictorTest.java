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
package com.itextpdf.pdfocr.onnxtr.orientation;

import com.itextpdf.pdfocr.TextOrientation;
import com.itextpdf.test.ExtendedITextTest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("IntegrationTest")
public class OnnxOrientationPredictorTest extends ExtendedITextTest {
    private static final String TEST_DIRECTORY =
            "./src/test/resources/com/itextpdf/pdfocr/onnxtr/orientation/OnnxOrientationPredictorTest/";
    private static final String TARGET_DIRECTORY =
            "./target/test/resources/com/itextpdf/pdfocr/onnxtr/orientation/OnnxOrientationPredictorTest/";
    private static final String MOBILENETV3 =
            "./src/test/resources/com/itextpdf/pdfocr/models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";
    private static IOrientationPredictor PREDICTOR;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);
        PREDICTOR = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        PREDICTOR.close();
    }

    public static Iterable<Object[]> predictWithLongLinesParams() {
        return Arrays.asList(new Object[][] {
                {TextOrientation.HORIZONTAL, "line_0.png"},
                {TextOrientation.HORIZONTAL_ROTATED_90, "line_90.png"},
                {TextOrientation.HORIZONTAL_ROTATED_180, "line_180.png"},
                {TextOrientation.HORIZONTAL_ROTATED_270, "line_270.png"},
        });
    }

    @ParameterizedTest(name = "predictWithLongLines: {1}")
    @MethodSource("predictWithLongLinesParams")
    public void predictWithLongLines(TextOrientation expectedResult, String inputFileName) throws IOException {
        final BufferedImage inputImage = ImageIO.read(new File(TEST_DIRECTORY + inputFileName));
        final TextOrientation actualResult = PREDICTOR.predict(Collections.singleton(inputImage)).next();
        Assertions.assertEquals(expectedResult, actualResult);
    }
}
