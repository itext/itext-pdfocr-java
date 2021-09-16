/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

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

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.util.ResourceUtil;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.font.FontSet;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;

public class PdfOcrFontProvider extends FontProvider {

    /**
     * Path to the default font.
     */
    private static final String DEFAULT_FONT_PATH = "com/itextpdf/pdfocr/fonts/LiberationSans-Regular.ttf";

    /**
     * Default font family.
     */
    private static final String DEFAULT_FONT_FAMILY = "LiberationSans";

    /**
     * Creates a new {@link PdfOcrFontProvider} instance with the default font
     * and the default font family.
     */
    public PdfOcrFontProvider() {
        super(DEFAULT_FONT_FAMILY);
        this.addFont(getDefaultFont(), PdfEncodings.IDENTITY_H);
    }

    /**
     * Creates a new {@link PdfOcrFontProvider} instance.
     */
    public PdfOcrFontProvider(FontSet fontSet,
            String defaultFontFamily) {
        super(fontSet, defaultFontFamily);
    }

    /**
     * Gets default font family.
     *
     * @return default font family as a string
     */
    @Override
    public String getDefaultFontFamily() {
        return DEFAULT_FONT_FAMILY;
    }

    /**
     * Gets default font as a byte array.
     *
     * @return default font as byte[]
     */
    private byte[] getDefaultFont() {
        try (InputStream stream = ResourceUtil
                .getResourceStream(DEFAULT_FONT_PATH)) {
            return StreamUtil.inputStreamToArray(stream);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass())
                    .error(MessageFormatUtil.format(
                            PdfOcrLogMessageConstant.CANNOT_READ_DEFAULT_FONT,
                            e.getMessage()));
            return new byte[0];
        }
    }
}
