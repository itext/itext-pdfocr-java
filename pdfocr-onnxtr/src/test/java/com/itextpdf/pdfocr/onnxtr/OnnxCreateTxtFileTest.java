package com.itextpdf.pdfocr.onnxtr;

import com.itextpdf.commons.utils.FileUtil;
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
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OnnxCreateTxtFileTest extends ExtendedITextTest {

    private static final String TEST_DIRECTORY = "./src/test/resources/com/itextpdf/pdfocr/";
    private static final String TEST_IMAGE_DIRECTORY = TEST_DIRECTORY + "images/";
    private static final String TARGET_DIRECTORY = "./target/test/resources/com/itextpdf/pdfocr/OnnxCreateTxtFileTest";
    private final static String FAST = TEST_DIRECTORY + "models/rep_fast_tiny-28867779.onnx";
    private final static String CRNNVGG16 = TEST_DIRECTORY + "models/crnn_vgg16_bn-662979cc.onnx";
    private final static String MOBILENETV3 = TEST_DIRECTORY + "models/mobilenet_v3_small_crop_orientation-5620cf7e.onnx";

    private static OnnxTrOcrEngine OCR_ENGINE;

    @BeforeAll
    public static void beforeClass() {
        createOrClearDestinationFolder(TARGET_DIRECTORY);

        IDetectionPredictor detectionPredictor = OnnxDetectionPredictor.fast(FAST);
        IRecognitionPredictor recognitionPredictor = OnnxRecognitionPredictor.crnnVgg16(CRNNVGG16);
        IOrientationPredictor orientationPredictor = OnnxOrientationPredictor.mobileNetV3(MOBILENETV3);

        OCR_ENGINE = new OnnxTrOcrEngine(detectionPredictor, orientationPredictor, recognitionPredictor);
    }

    @Test
    //TODO DEVSIX-9153: Add tests for ocrEngine.createTxtFile();
    public void createTxtFileNullEventHelperTest() throws IOException {
        String src = TEST_DIRECTORY + "images/numbers_01.jpg";
        File imgFile = new File(src);
        String src1 = TEST_DIRECTORY + "images/270_degrees_rotated.jpg";
        File imgFile1 = new File(src1);
        String src2 = TEST_DIRECTORY + "images/example_04.png";
        File imgFile2 = new File(src2);

        OCR_ENGINE.createTxtFile(Arrays.asList(imgFile, imgFile1, imgFile2), FileUtil.createTempFile("test", ".txt"),
                new OcrProcessContext(null));
    }
}
