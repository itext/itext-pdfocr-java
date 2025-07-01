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

import com.itextpdf.io.util.UrlUtil;
import com.itextpdf.pdfocr.OcrProcessContext;
import com.itextpdf.pdfocr.onnxtr.detection.IDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.detection.OnnxDetectionPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.IOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.orientation.OnnxOrientationPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.IRecognitionPredictor;
import com.itextpdf.pdfocr.onnxtr.recognition.OnnxRecognitionPredictor;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class OnnxCreateTxtFileTest extends ExtendedITextTest {

    private static final String BASE_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = BASE_DIRECTORY + "images/";
    private static final String SOURCE_DIRECTORY = BASE_DIRECTORY + "OnnxCreateTxtFileTest/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxCreateTxtFileTest/";
    private static final String FAST = BASE_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private static final String CRNNVGG16 = BASE_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private static final String MOBILENETV3 = BASE_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";

    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        OCR_ENGINE.close();
    }

    @Test
    public void createTxtFileTest() throws IOException {
        String cmpPath = SOURCE_DIRECTORY + "cmp_createTxtFile.txt";
        String outputPath = TARGET_DIRECTORY + "createTxtFile.txt";

        String[] sourceImages = new String[]{
                TEST_IMAGE_DIRECTORY + "englishText.bmp",
                TEST_IMAGE_DIRECTORY + "rotatedTextBasic.png",
                TEST_IMAGE_DIRECTORY + "scanned_spa_01.png",
                SOURCE_DIRECTORY + "regularText.png",
                SOURCE_DIRECTORY + "basicCloud.png",
                SOURCE_DIRECTORY + "differentSizes.png",
                SOURCE_DIRECTORY + "rotated.png"
        };
        List<File> images = new ArrayList<>(sourceImages.length * 2);
        for (String sourceImage : sourceImages) {
            images.add(new File(sourceImage));
        }
        OCR_ENGINE.createTxtFile(images, new File(outputPath), new OcrProcessContext(null));
        Assertions.assertNull(compareTxt(cmpPath, outputPath));
    }

    private static String compareTxt(String cmp, String dest) throws IOException {
        String errorMessage = null;
        System.out.println("Out txt: " + UrlUtil.getNormalizedFileUriString(dest));
        System.out.println("Cmp txt: " + UrlUtil.getNormalizedFileUriString(cmp) + "\n");

        List<String> result = Files.readAllLines(java.nio.file.Paths.get(dest));
        List<String> expected = Files.readAllLines(java.nio.file.Paths.get(cmp));

        int lineNumber = 0;
        String destLine = readLine(result, lineNumber);
        String cmpLine = readLine(expected, lineNumber);

        while (destLine != null || cmpLine != null) {
            if (destLine == null || cmpLine == null) {
                errorMessage = "The number of lines is different.\n";
                break;
            }

            if (!destLine.equals(cmpLine)) {
                errorMessage = "Txt files differ at line " + (lineNumber + 1)
                        + "\n See difference: cmp file: \"" + cmpLine + "\"\n"
                        + "target file: \"" + destLine + "\n";
            }

            lineNumber++;
            destLine = readLine(result, lineNumber);
            cmpLine = readLine(expected, lineNumber);
        }

        return errorMessage;
    }

    private static String readLine(List<String> lines, int lineNumber) {
        if (lineNumber < lines.size()) {
            return lines.get(lineNumber);
        }
        return null;
    }
}
