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
package com.itextpdf.pdfocr.util;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.exceptions.PdfOcrException;
import com.itextpdf.pdfocr.exceptions.PdfOcrExceptionMessageConstant;
import com.itextpdf.pdfocr.exceptions.PdfOcrInputException;
import com.itextpdf.pdfocr.logs.PdfOcrLogMessageConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Utility class for working with files.
 */
public final class PdfOcrFileUtil {
    private PdfOcrFileUtil() {
        // do nothing
    }

    /**
     * Writes provided {@link java.lang.String} to text file using provided path.
     *
     * @param path path as {@link java.lang.String} to file to be created
     * @param data text data in required format as {@link java.lang.String}
     */
    public static void writeToTextFile(final String path, final String data) {
        try (Writer writer = new OutputStreamWriter(FileUtil.getFileOutputStream(path), StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            throw new PdfOcrException(MessageFormatUtil.format(PdfOcrExceptionMessageConstant.CANNOT_WRITE_TO_FILE,
                    path, e.getMessage()), e);
        }
    }

    /**
     * Gets path to temp file in current system temporary directory.
     *
     * @param name temp file name
     * @param extension temp file extension
     *
     * @return path to temp file in the system temporary directory
     *
     * @throws IOException when temp file cannot be obtained
     */
    public static String getTempFilePath(String name, String extension) throws IOException {
        Path tempPath = Files.createTempFile(name, extension);
        return tempPath.toString();
    }

    /**
     * Writes provided {@link java.lang.String} to stream.
     *
     * @param stream {@link OutputStream} to write string data to
     * @param data text data in required format as {@link java.lang.String}
     */
    public static void writeToStream(final OutputStream stream, final String data) {
        try (Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            throw new PdfOcrException(MessageFormatUtil.format(PdfOcrExceptionMessageConstant.CANNOT_WRITE_TO_STREAM,
                    e.getMessage()), e);
        }
    }

    /**
     * Creates list of {@link InputStream} based on given list of {@link File}.
     *
     * @param inputImages list of {@link File} to be used as source
     *
     * @return created list of {@link InputStream}
     */
    public static List<InputStream> convertToInputStreams(List<File> inputImages) {
        List<InputStream> inputImageStreams = new ArrayList<>();
        for (File file : inputImages) {
            try {
                inputImageStreams.add(FileUtil.getInputStreamForFile(file));
            } catch (IOException e) {
                LoggerFactory.getLogger(PdfOcrFileUtil.class).error(MessageFormatUtil.format(
                        PdfOcrLogMessageConstant.CANNOT_OPEN_INPUT_STREAM, file.getAbsolutePath()));
                throw new PdfOcrInputException(MessageFormatUtil.format(
                        PdfOcrExceptionMessageConstant.CANNOT_OPEN_INPUT_STREAM, file.getAbsolutePath()));
            }
        }
        return inputImageStreams;
    }

    /**
     * Creates output stream based on given {@link File}.
     *
     * @param file the file to be used as output
     *
     * @return the created output stream
     */
    public static OutputStream convertToOutputStream(File file) {
        try {
            return FileUtil.getFileOutputStream(file);
        } catch (IOException e) {
            LoggerFactory.getLogger(PdfOcrFileUtil.class).error(MessageFormatUtil.format(
                    PdfOcrLogMessageConstant.CANNOT_OPEN_OUTPUT_STREAM, file.getAbsolutePath()));
            throw new PdfOcrInputException(MessageFormatUtil.format(
                    PdfOcrExceptionMessageConstant.CANNOT_OPEN_OUTPUT_STREAM, file.getAbsolutePath()));
        }
    }
}
