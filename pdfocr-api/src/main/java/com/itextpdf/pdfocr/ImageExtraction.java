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
package com.itextpdf.pdfocr;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.pdfocr.util.PdfOcrFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Class to extract images on page content stream processing, see {@link PdfCanvasProcessor}.
 */
final class ImageExtraction {

    private ImageExtraction() {
        // Empty constructor to forbid instantiation
    }

    /**
     * @return map where the key is an image path and the value is a position on the page
     */
    static List<PageImageData> extractImagesFromPdfPage(PdfPage pdfPage) throws IOException {
        CanvasImageExtractor listener = new CanvasImageExtractor();
        PdfCanvasProcessor processor = new PdfCanvasProcessor(listener);
        processor.processPageContent(pdfPage);
        Map<PdfImageXObject, Rectangle> images = listener.getImages();

        // Now output to temp files
        List<PageImageData> pageImageData = new ArrayList<>(images.size());
        for (Map.Entry<PdfImageXObject, Rectangle> image : images.entrySet()) {
            final String extension = image.getKey().identifyImageFileExtension();
            final String imageFilePath = PdfOcrFileUtil.getTempFilePath(
                    "pdfocr_img_" + UUID.randomUUID(), "." + extension);
            try (OutputStream fos = FileUtil.getFileOutputStream(imageFilePath)) {
                byte[] imageBytes = image.getKey().getImageBytes();
                fos.write(imageBytes, 0, imageBytes.length);
                pageImageData.add(new PageImageData(new File(imageFilePath), image.getKey(), image.getValue()));
            }
        }

        return pageImageData;
    }

    static final class PageImageData {
        private File file;
        private PdfImageXObject xObject;
        private Rectangle pagePosition;

        PageImageData(File file, PdfImageXObject xObject, Rectangle pagePosition) {
            this.file = file;
            this.xObject = xObject;
            this.pagePosition = pagePosition;
        }

        File getPath() {
            return file;
        }

        PdfImageXObject getXObject() {
            return xObject;
        }

        Rectangle getPagePosition() {
            return pagePosition;
        }

        @Override
        public int hashCode() {
            return Objects.hash((Object) file, xObject, pagePosition);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PageImageData that = (PageImageData) o;
            return file == that.file && xObject == that.xObject && pagePosition == that.pagePosition;
        }
    }

    // Consider moving to kernel if reused anywhere else
    private static final class CanvasImageExtractor implements IEventListener {
        // Image xobject - position on a page
        private final Map<PdfImageXObject, Rectangle> images = new LinkedHashMap<>();

        CanvasImageExtractor() {
            // Empty constructor
        }

        public void eventOccurred(IEventData data, EventType type) {
            if (type == EventType.RENDER_IMAGE) {
                ImageRenderInfo renderInfo = (ImageRenderInfo) data;
                final Matrix imageCtm = renderInfo.getImageCtm();
                final Rectangle bbox = calcImageRect(imageCtm);
                images.put(renderInfo.getImage(), bbox);
            }
        }

        @Override
        public Set<EventType> getSupportedEvents() {
            return new HashSet<>(Collections.singletonList(EventType.RENDER_IMAGE));
        }

        Map<PdfImageXObject, Rectangle> getImages() {
            return images;
        }

        private Rectangle calcImageRect(Matrix ctm) {
            Point[] points = transformPoints(ctm,
                    new Point(0, 0), new Point(0, 1),
                    new Point(1, 0), new Point(1, 1));

            return Rectangle.calculateBBox(Arrays.asList(points));
        }

        private Point[] transformPoints(Matrix transformationMatrix, Point... points) {
            AffineTransform t = new AffineTransform(transformationMatrix.get(Matrix.I11),
                    transformationMatrix.get(Matrix.I12),
                    transformationMatrix.get(Matrix.I21), transformationMatrix.get(Matrix.I22),
                    transformationMatrix.get(Matrix.I31), transformationMatrix.get(Matrix.I32));
            Point[] transformed = new Point[points.length];
            t.transform(points, 0, transformed, 0, points.length);

            return transformed;
        }
    }
}
