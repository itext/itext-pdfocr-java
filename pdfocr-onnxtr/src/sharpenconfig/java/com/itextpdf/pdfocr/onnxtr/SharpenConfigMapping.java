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
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import sharpen.config.MappingConfiguration;
import sharpen.config.MappingConfigurator;
import sharpen.config.ModuleOption;
import sharpen.config.ModulesConfigurator;
import sharpen.config.OptionsConfigurator;

/**
 * Service implementation of {@link sharpen.config.MappingConfiguration} containing the module's Sharpen configuration.
 */
public class SharpenConfigMapping implements MappingConfiguration {

    @Override
    public int getMappingPriority() {
        return 7;
    }

    @Override
    public String getModuleName() {
        return "pdfocr-onnxtr";
    }

    @Override
    public void applyMappingConfiguration(MappingConfigurator configurator) {
        configurator.addCustomUsingForMethodInvocation("java.awt.image.BufferedImage",
                Collections.singletonList("iText.Pdfocr.Onnxtr.Util"));
        mapOpenCv(configurator);
    }

    private void mapOpenCv(MappingConfigurator configurator) {
        configurator.addFullName("iText.Kernel.Geom.Point");
        configurator.mapType("org.bytedeco.opencv.opencv_core.Mat", "OpenCvSharp.Mat");
        configurator.mapType("org.bytedeco.opencv.opencv_core.MatVector", "OpenCvSharp.Internal.Vectors.VectorOfMat");
        configurator.mapProperty("org.bytedeco.opencv.opencv_core.MatVector.size", "Size");
        configurator.mapProperty("org.bytedeco.opencv.opencv_core.RotatedRect.size", "Size");
        configurator.mapProperty("org.bytedeco.opencv.opencv_core.RotatedRect.angle", "Angle");
        configurator.mapProperty("org.bytedeco.opencv.opencv_core.Size2f.width", "Width");
        configurator.mapProperty("org.bytedeco.opencv.opencv_core.Size2f.height", "Height");
        configurator.mapProperty("java.awt.image.BufferedImage.getWidth", "Width");
        configurator.mapProperty("java.awt.image.BufferedImage.getHeight", "Height");
        configurator.mapMethod("java.nio.FloatBuffer.array", "");

        configurator.removeMethod("org.bytedeco.javacpp.Pointer.close");

        configurator.mapMethod("org.bytedeco.opencv.opencv_core.Size2f.close", "");
        configurator.mapType("ai.onnxruntime.NodeInfo", "Microsoft.ML.OnnxRuntime.NodeMetadata");
        configurator.mapType("ai.onnxruntime.OnnxValue", "Microsoft.ML.OnnxRuntime.DisposableNamedOnnxValue");
        configurator.mapType("ai.onnxruntime.TensorInfo", "Microsoft.ML.OnnxRuntime.Tensors.Tensor");
        configurator.mapType("org.bytedeco.opencv.opencv_core.Size2f", "OpenCvSharp.Size2f");
        configurator.mapType("org.bytedeco.opencv.opencv_core.RotatedRect", "OpenCvSharp.RotatedRect");
        configurator.mapType("org.bytedeco.opencv.global.opencv_imgproc", "OpenCvSharp.Cv2");
        configurator.mapField("org.bytedeco.opencv.global.opencv_imgproc.MORPH_OPEN", "OpenCvSharp.MorphTypes.Open");
        configurator.mapField("org.bytedeco.opencv.global.opencv_imgproc.RETR_EXTERNAL", "OpenCvSharp.RetrievalModes.External");
        configurator.mapField("org.bytedeco.opencv.global.opencv_imgproc.CHAIN_APPROX_SIMPLE", "OpenCvSharp.ContourApproximationModes.ApproxSimple");
        configurator.mapIndexer("org.bytedeco.opencv.opencv_core.MatVector.get");
        configurator.mapType("org.opencv.core.CvType", "OpenCvSharp.MatType");

    }

    @Override
    public void applySharpenOptions(OptionsConfigurator configurator) {

    }

    @Override
    public void applyConfigModuleSettings(ModulesConfigurator configurator) {

    }

    @Override
    public void setConfigModuleSettings(ModulesConfigurator modulesConfigurator) {
    }

    @Override
    public Collection<ModuleOption> getAvailableModuleSettings() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Collection<String> getDependencies() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<String> getIgnoredSourceFiles() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<String> getIgnoredResources() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SimpleImmutableEntry<String, String>> getOverwrittenResources() {
        return Collections.EMPTY_LIST;
    }
}