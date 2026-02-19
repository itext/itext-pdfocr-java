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
package com.itextpdf.pdfocr.onnxtr.recognition;

import com.itextpdf.pdfocr.onnxtr.FloatBufferMdArray;
import com.itextpdf.test.ExtendedITextTest;

import java.nio.FloatBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class CtcLabelPostProcessorTest extends ExtendedITextTest {
    @Test
    public void initWithInvalidLabelMapper() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new CtcLabelPostProcessor(null)
        );
    }

    @Test
    public void process() {
        final StringMapper mapper = new StringMapper(new String[]{"AB", "CD", "EF", "GH", " "});
        final CtcLabelPostProcessor processor = new CtcLabelPostProcessor(mapper);
        final FloatBuffer probs = FloatBuffer.wrap(new float[]{
                0.97F, 0.11F, 0.15F, 0.13F, 0.12F, 0.11F,    // [blank]
                0.01F, 0.75F, 0.22F, 0.14F, 0.56F, 0.67F,    // "AB"
                0.96F, 0.24F, 0.14F, 0.10F, 0.42F, 0.13F,    // [blank]
                0.04F, 0.88F, 0.42F, 0.24F, 0.17F, 0.12F,    // "AB"
                0.06F, 0.66F, 0.33F, 0.17F, 0.52F, 0.47F,    // "AB" (should get skipped)
                0.03F, 0.24F, 0.17F, 0.34F, 0.18F, 0.89F,    // " "
                0.92F, 0.13F, 0.17F, 0.24F, 0.16F, 0.19F,    // [blank]
                0.05F, 0.05F, 0.27F, 0.34F, 0.77F, 0.24F,    // "GH"
                0.89F, 0.14F, 0.27F, 0.12F, 0.36F, 0.56F,    // [blank]
        });
        final FloatBufferMdArray buffer = new FloatBufferMdArray(
                probs, new long[]{9, mapper.size() + 1}
        );
        Assertions.assertEquals("ABAB GH", processor.process(buffer));
    }
}
