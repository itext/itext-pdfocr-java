package com.itextpdf.pdfocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.util.MessageFormatUtil;
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
