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
package com.itextpdf.pdfocr.util;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.image.ImageType;
import com.itextpdf.io.image.ImageTypeDetector;
import com.itextpdf.io.util.UrlUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle tiff images.
 */
public final class TiffImageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TiffImageUtil.class);

    private TiffImageUtil() {
        // Private constructor will prevent the instantiation of this class directly.
    }

    /**
     * Retrieves all images from a TIFF file.
     *
     * @param inputFile TIFF file to retrieve images from
     *
     * @return the list of {@link BufferedImage}'s in the TIFF file
     */
    public static List<BufferedImage> getAllImages(final File inputFile) {
        try (InputStream is = FileUtil.getInputStreamForFile(inputFile.getAbsolutePath())) {
            return Imaging.getAllBufferedImages(is, inputFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error(MessageFormatUtil.format(PdfOcrLogMessageConstant.CANNOT_RETRIEVE_PAGES_FROM_IMAGE,
                    inputFile.getAbsolutePath(), e.getMessage()));
        }

        return new ArrayList<BufferedImage>();
    }

    /**
     * Checks whether image type is TIFF.
     *
     * @param inputImage input {@link java.io.File} to check for TIFF image type
     *
     * @return {@code true} if provided image is TIFF image, {@code false} otherwise
     */
    public static boolean isTiffImage(final File inputImage) {
        return getImageType(inputImage) == ImageType.TIFF;
    }

    /**
     * Gets the image type.
     *
     * @param inputImage input {@link java.io.File} to get image type for
     *
     * @return image type {@link com.itextpdf.io.image.ImageType}
     */
    public static ImageType getImageType(final File inputImage) {
        ImageType type;
        try {
            type = ImageTypeDetector.detectImageType(UrlUtil.toURL(inputImage.getAbsolutePath()));
        } catch (Exception e) {
            LOGGER.error(MessageFormatUtil.format(PdfOcrLogMessageConstant.CANNOT_READ_INPUT_IMAGE, e.getMessage()));
            throw new PdfOcrInputException(PdfOcrExceptionMessageConstant.CANNOT_READ_INPUT_IMAGE_PARAMS)
                    .setMessageParams(inputImage.getAbsolutePath());
        }

        return type;
    }
}
