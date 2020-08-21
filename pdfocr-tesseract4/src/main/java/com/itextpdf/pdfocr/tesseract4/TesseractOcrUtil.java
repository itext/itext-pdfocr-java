/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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
package com.itextpdf.pdfocr.tesseract4;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.MessageFormatUtil;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.lept4j.ILeptonica;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class to work with tesseract command line tool and image
 * preprocessing using {@link net.sourceforge.lept4j.ILeptonica}.
 * These all methods have to be ported to .Net manually.
 */
class TesseractOcrUtil {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TesseractOcrUtil.class);


    /**
     * Rotation constants.
     */
    private static final int ROTATION_0 = 0;
    private static final int ROTATION_90 = 90;
    private static final int ROTATION_180 = 180;
    private static final int ROTATION_270 = 270;
    private static final int EXIF_ROTATION_0 = 1;
    private static final int EXIF_ROTATION_90 = 6;
    private static final int EXIF_ROTATION_180 = 3;
    private static final int EXIF_ROTATION_270 = 8;


    /**
     * List of pages of the image that is being processed.
     */
    private List<BufferedImage> imagePages =
            Collections.<BufferedImage>emptyList();

    /**
     * Creates a new {@link TesseractOcrUtil} instance.
     */
    TesseractOcrUtil() {
    }

    /**
     * Reads required page from provided tiff image.
     * Note that rotation is always applied when image read.
     *
     * @param inputFile input image as {@link java.io.File}
     * @param pageNumber number of page
     * @return result {@link net.sourceforge.lept4j.Pix} object created from
     * given image
     */
    static Pix readPixPageFromTiff(final File inputFile,
            final int pageNumber) {
        Pix pix = null;
        BufferedImage img = TesseractOcrUtil
                .getImagePage(inputFile, pageNumber);
        if (img != null) {
            pix = readPix(img);
        }
        // return required page to be preprocessed
        return pix;
    }

    /**
     * Performs default image preprocessing.
     * It includes the following actions:
     * converting to grayscale,
     * thresholding.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    static Pix preprocessPix(Pix pix) {
        pix = convertToGrayscale(pix);
        pix = otsuImageThresholding(pix);
        return pix;
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix} to grayscale.
     * In .Net image is converted only if this is 32bpp image. In java image is
     * converted anyway using different Leptonica methods depending on
     * image depth.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    static Pix convertToGrayscale(final Pix pix) {
        Leptonica instance = Leptonica.INSTANCE;
        if (pix != null) {
            int depth = instance.pixGetDepth(pix);

            if (depth == 32) {
                return instance.pixConvertRGBToLuminance(pix);
            } else {
                return instance.pixRemoveColormap(pix,
                        instance.REMOVE_CMAP_TO_GRAYSCALE);
            }
        } else {
            return pix;
        }
    }

    /**
     * Performs Leptonica Otsu adaptive image thresholding using
     * {@link net.sourceforge.lept4j.Leptonica#pixOtsuAdaptiveThreshold}
     * method.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be processed
     * @return {@link net.sourceforge.lept4j.Pix} object after thresholding
     */
    static Pix otsuImageThresholding(final Pix pix) {
        if (pix != null) {
            Pix thresholdPix = null;
            if (pix.d == 8) {
                PointerByReference pointer = new PointerByReference();
                Leptonica.INSTANCE
                        .pixOtsuAdaptiveThreshold(pix, pix.w, pix.h,
                                0, 0, 0,
                                null, pointer);
                thresholdPix = new Pix(pointer.getValue());
                if (thresholdPix.w > 0 && thresholdPix.h > 0) {
                    // destroying original pix
                    destroyPix(pix);
                    return thresholdPix;
                } else {
                    LOGGER.info(MessageFormatUtil.format(
                            Tesseract4LogMessageConstant.CANNOT_BINARIZE_IMAGE,
                            pix.d));
                    // destroying created PointerByReference object
                    destroyPix(thresholdPix);
                    return pix;
                }
            } else {
                LOGGER.info(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.CANNOT_BINARIZE_IMAGE,
                        pix.d));
                return pix;
            }
        } else {
            return pix;
        }
    }

    /**
     * Destroys {@link net.sourceforge.lept4j.Pix} object.
     *
     * @param pix {@link net.sourceforge.lept4j.Pix} object to be destroyed
     */
    static void destroyPix(Pix pix) {
        if (pix != null) {
            Leptonica.INSTANCE.lept_free(pix.getPointer());
        }
    }

    /**
     * Sets tesseract properties.
     * The following properties are set in this method:
     * In java: path to tess data, languages, psm
     * In .Net: psm
     * This means that other properties have been set during the
     * initialization of tesseract instance previously or tesseract library
     * doesn't provide such possibilities in api for .Net or java.
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract} object
     * @param tessData path to tess data directory
     * @param languages list of languages in required format
     *                  as {@link java.lang.String}
     * @param pageSegMode page segmentation mode {@link java.lang.Integer}
     * @param userWordsFilePath path to a temporary file with user words
     */
    static void setTesseractProperties(
            final ITesseract tesseractInstance,
            final String tessData, final String languages,
            final Integer pageSegMode, final String userWordsFilePath) {
        tesseractInstance.setDatapath(tessData);
        tesseractInstance.setLanguage(languages);
        if (pageSegMode != null) {
            tesseractInstance.setPageSegMode(pageSegMode);
        }
        tesseractInstance.setOcrEngineMode(userWordsFilePath != null ? 0 : 3);
    }

    /**
     * Creates tesseract instance with parameters.
     * Method is used to initialize tesseract instance with parameters if it
     * haven't been initialized yet.
     * In this method in java 'tessData', 'languages' and 'userWordsFilePath'
     * properties are unused as they will be set using setters in
     * {@link #setTesseractProperties} method. In .Net all these properties
     * are needed to be provided in tesseract constructor in order to
     * initialize tesseract instance. Thus, tesseract initialization takes
     * place in {@link Tesseract4LibOcrEngine#Tesseract4LibOcrEngine} constructor in
     * java, but in .Net it happens only after all properties are validated,
     * i.e. just before OCR process.
     *
     * @param isWindows true is current os is windows
     * @param tessData path to tess data directory
     * @param languages list of languages in required format as
     *                  {@link java.lang.String}
     * @param userWordsFilePath path to a temporary file with user words
     * @return initialized {@link net.sourceforge.tess4j.ITesseract} object
     */
    static ITesseract initializeTesseractInstance(final boolean isWindows,
            final String tessData, final String languages,
            final String userWordsFilePath) {
        try {
            if (isWindows) {
                return new Tesseract1();
            } else {
                return new Tesseract();
            }
        } catch (LinkageError e) {
            throw new Tesseract4OcrException(isWindows ?
                    Tesseract4OcrException.TESSERACT_LIB_NOT_INSTALLED_WIN :
                    Tesseract4OcrException.TESSERACT_LIB_NOT_INSTALLED, e);
        }
    }

    /**
     * Returns true if tesseract instance has been already disposed.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to check
     * @return true if tesseract instance is disposed.
     */
    static boolean isTesseractInstanceDisposed(
            final ITesseract tesseractInstance) {
        return false;
    }

    /**
     * Disposes {@link net.sourceforge.tess4j.ITesseract} instance.
     * (used in .net version)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to dispose
     */
    static void disposeTesseractInstance(
            final ITesseract tesseractInstance) {
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix}
     * to {@link java.awt.image.BufferedImage} with
     * {@link net.sourceforge.lept4j.ILeptonica#IFF_PNG} image format.
     *
     * @param pix input {@link net.sourceforge.lept4j.Pix} object
     * @return result {@link java.awt.image.BufferedImage} object
     * @throws IOException if it is not possible to convert
     */
    static BufferedImage convertPixToImage(final Pix pix)
            throws IOException {
        if (pix != null) {
            Leptonica instance = Leptonica.INSTANCE;
            BufferedImage bi = null;
            PointerByReference pdata = new PointerByReference();
            try {
                NativeSizeByReference psize = new NativeSizeByReference();
                instance.pixWriteMem(pdata, psize, pix, ILeptonica.IFF_PNG);
                byte[] b = pdata.getValue().getByteArray(0,
                        psize.getValue().intValue());
                try (InputStream in = new ByteArrayInputStream(b)) {
                    bi = ImageIO.read(in);
                }
            } finally {
                instance.lept_free(pdata.getValue());
            }
            return bi;
        } else {
            return null;
        }
    }

    /**
     * Gets path to temp file in current system temporary directory.
     *
     * @return path to temp file in the system temporary directory
     */
    static String getTempFilePath(String name, String suffix) {
        String tmpFileName = name + suffix;
        try {
            Path tempPath = Files.createTempFile(name, suffix);
            tmpFileName = tempPath.toString();
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.info(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_GET_TEMPORARY_DIRECTORY,
                    e.getMessage()));
        }
        return tmpFileName;
    }

    /**
     * Gets requested image page from the provided image.
     *
     * @param inputFile input image
     * @param page requested image page
     * @return requested image page as a {@link java.awt.image.BufferedImage}
     */
    static BufferedImage getImagePage(File inputFile, int page)
    {
        BufferedImage img = null;
        try (InputStream is =
                new FileInputStream(inputFile.getAbsolutePath())) {
            List<BufferedImage> pages = Imaging.getAllBufferedImages(is,
                    inputFile.getAbsolutePath());
            if (page >= pages.size()) {
                LOGGER.warn(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.PAGE_NUMBER_IS_INCORRECT,
                        page,
                        inputFile.getAbsolutePath()));
                return null;
            }
            img = pages.get(page);
        } catch (ImageReadException | IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant
                            .CANNOT_RETRIEVE_PAGES_FROM_IMAGE,
                    inputFile.getAbsolutePath(),
                    e.getMessage()));
        }
        return img;
    }

    /**
     * Saves passed {@link java.awt.image.BufferedImage} to given path
     *
     * @param tmpFileName provided file path to save the
     * {@link java.awt.image.BufferedImage}
     * @param image provided {@link java.awt.image.BufferedImage} to be saved
     */
    static void saveImageToTempPngFile(final String tmpFileName,
            final BufferedImage image) {
        if (image != null) {
            try {
                ImageIO.write(image, "png", new File(tmpFileName));
            } catch (Exception e) { // NOSONAR
                LOGGER.error(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.CANNOT_PROCESS_IMAGE,
                        e.getMessage()));
            }
        }
    }

    /**
     * Saves passed {@link net.sourceforge.lept4j.Pix} to given path
     *
     * @param tmpFileName provided file path to save the
     * {@link net.sourceforge.lept4j.Pix}
     * @param pix provided {@link net.sourceforge.lept4j.Pix} to be saved
     */
    static void savePixToTempPngFile(final String tmpFileName,
            final Pix pix) {
        if (pix != null) {
            try {
                Leptonica.INSTANCE.pixWritePng(tmpFileName, pix,
                        ILeptonica.IFF_PNG);
            } catch (Exception e) { // NOSONAR
                LOGGER.info(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.CANNOT_PROCESS_IMAGE,
                        e.getMessage()));
            }
        }
    }

    /**
     * Create temporary copy of input file to avoid issue with tesseract and
     * different encodings in the path.
     *
     * @param src path to the source image
     * @param dst destination path
     */
    static void createTempFileCopy(final String src, final String dst)
            throws IOException {
        Files.copy(Paths.get(src), Paths.get(dst),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Returns parent directory for the passed path.
     *
     * @param path path to file
     * @return parent directory where the file is located
     */
    static String getParentDirectory(final String path) {
        return new File(path).getParent();
    }

    /**
     * Retrieves list of pages from provided image as list of
     * {@link java.awt.image.BufferedImage}, one per page and updates
     * this list for the image using {@link #setListOfPages} method.
     *
     * @param inputFile input image {@link java.io.File}
     */
    void initializeImagesListFromTiff(
            final File inputFile) {
        try (InputStream is =
                new FileInputStream(inputFile.getAbsolutePath())) {
            setListOfPages(Imaging
                    .getAllBufferedImages(is,
                            inputFile.getAbsolutePath()));
        } catch (Exception e) { // NOSONAR
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant
                            .CANNOT_RETRIEVE_PAGES_FROM_IMAGE,
                    inputFile.getAbsolutePath(),
                    e.getMessage()));
        }
    }

    /**
     * Gets list of page of processing image as list of
     * {@link java.awt.image.BufferedImage}, one per page.
     *
     * @return result {@link java.util.List} of pages
     */
    List<BufferedImage> getListOfPages() {
        return new ArrayList<BufferedImage>(imagePages);
    }

    /**
     * Sets list of page of processing image as list of
     * {@link java.awt.image.BufferedImage}, one per page.
     *
     * @param listOfPages list of {@link java.awt.image.BufferedImage} for
     *                    each page.
     */
    void setListOfPages(final List<BufferedImage> listOfPages) {
        imagePages = Collections.<BufferedImage>unmodifiableList(listOfPages);
    }

    /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link OutputFormat} is used in .Net version,
     * in java output format should already be set)
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param image input {@link java.awt.image.BufferedImage} to be processed
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final BufferedImage image, final OutputFormat outputFormat)
            throws TesseractException {
        if (image != null) {
            return tesseractInstance.doOCR(image);
        } else {
            return null;
        }
    }

    /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link OutputFormat} is used in .Net version, in java output format
     * should already be set)
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param image input image as {@link java.io.File} to be
     *              processed
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     */
    String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final File image, final OutputFormat outputFormat)
            throws TesseractException {
        if (image != null) {
            return tesseractInstance.doOCR(image);
        } else {
            return null;
        }
    }

     /**
     * Performs ocr for the provided image
     * and returns result as string in required format.
     * ({@link OutputFormat} is used in .Net version, in java output format
      * should already be set)
     *
     * @param tesseractInstance {@link net.sourceforge.tess4j.ITesseract}
     *                          object to perform OCR
     * @param pix input image as {@link net.sourceforge.lept4j.Pix} to be
     *              processed
     * @param outputFormat selected {@link OutputFormat} for tesseract
     * @return result as {@link java.lang.String} in required format
     * @throws TesseractException if tesseract recognition failed
     * @throws IOException if it is not possible to convert input image
     */
    String getOcrResultAsString(
            final ITesseract tesseractInstance,
            final Pix pix, final OutputFormat outputFormat)
            throws TesseractException, IOException {
        if (pix != null) {
            BufferedImage bufferedImage = convertPixToImage(pix);
            return getOcrResultAsString(tesseractInstance,
                    bufferedImage, outputFormat);
        } else {
            return null;
        }
    }

    /**
     * Read {@link net.sourceforge.lept4j.Pix} from {@link java.awt.image.BufferedImage}.
     * Note that rotation is always applied when image read.
     *
     * @param image {@link java.awt.image.BufferedImage} to read from
     * @return Pix result {@link net.sourceforge.lept4j.Pix}
     */
    static Pix readPix(final BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return readPix(baos.toByteArray());
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE,
                    e.getMessage()));
            return null;
        }
    }

    /**
     * Read {@link net.sourceforge.lept4j.Pix} from {@link java.io.File}.
     * Note that rotation is always applied when image read.
     *
     * @param inputFile {@link java.io.File} to read from
     * @return Pix result {@link net.sourceforge.lept4j.Pix}
     */
    static Pix readPix(final File inputFile) {
        try {
            return readPix(Files.readAllBytes(inputFile.toPath()));
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE,
                    e.getMessage()));
            return null;
        }
    }

    /**
     * Read {@link net.sourceforge.lept4j.Pix} from byte array.
     * Note that rotation is always applied when image read.
     *
     * @param imageBytes to read from
     * @return Pix result {@link net.sourceforge.lept4j.Pix}
     */
    static Pix readPix(final byte[] imageBytes) {
        Pix pix = null;
        try {
            ByteBuffer bb = ByteBuffer.wrap(imageBytes);
            NativeSize size = new NativeSize(imageBytes.length);
            pix = Leptonica.INSTANCE.pixReadMem(bb, size);
        } catch (Exception e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE,
                    e.getMessage()));
        }
        if (pix != null) {
            int rotation = detectRotation(imageBytes);
            pix = rotate(pix, rotation);
        }
        return pix;
    }

    /**
     * Detect rotation specified by image metadata.
     *
     * @param file file to detect rotation
     * @return image rotation as specified in metadata
     */
    static int detectRotation(final File file) {
        try {
            return detectRotation(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            LOGGER.error(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_INPUT_IMAGE,
                    e.getMessage()));
            return ROTATION_0;
        }
    }

    /**
     * Detect rotation specified by image metadata.
     *
     * @param imageData to detect rotation
     * @return image rotation as specified in metadata
     */
    static int detectRotation(final ImageData imageData) {
        return detectRotation(imageData.getData());
    }

    /**
     * Detect rotation specified by image metadata.
     *
     * @param data image data to detect rotation
     * @return image rotation as specified in metadata
     */
    static int detectRotation(final byte[] data) {
        int rotation = ROTATION_0;
        try {
            final ImageMetadata metadata = Imaging.getMetadata(data);
            TiffImageMetadata tiffImageMetadata;
            if (metadata instanceof JpegImageMetadata) {
                tiffImageMetadata = ((JpegImageMetadata) metadata).getExif();
            } else if (metadata instanceof TiffImageMetadata) {
                tiffImageMetadata = (TiffImageMetadata) metadata;
            } else {
                tiffImageMetadata = null;
            }
            if (tiffImageMetadata != null) {
                rotation = readRotationFromMetadata(tiffImageMetadata);
            }
        } catch (Exception e) {
            LOGGER.info(MessageFormatUtil.format(
                    Tesseract4LogMessageConstant.CANNOT_READ_IMAGE_METADATA,
                    e.getMessage()));
        }
        return rotation;
    }

    /**
     * Reads image orientation from metadata and converts to rotation.
     * @param tiffImageMetadata image metadata
     * @return rotation
     */
    static int readRotationFromMetadata(TiffImageMetadata tiffImageMetadata) {
        final List items = tiffImageMetadata.getItems();
        for (final Object item : items) {
            if (item instanceof TiffImageMetadata.TiffMetadataItem &&
                    "Orientation".equals(((TiffImageMetadata.TiffMetadataItem) item).getKeyword())) {
                int orientation = Integer.parseInt(((TiffImageMetadata.TiffMetadataItem) item).getText());
                switch (orientation) {
                    case EXIF_ROTATION_0:
                        return ROTATION_0;
                    case EXIF_ROTATION_90:
                        return ROTATION_90;
                    case EXIF_ROTATION_180:
                        return ROTATION_180;
                    case EXIF_ROTATION_270:
                        return ROTATION_270;
                    default:
                        LOGGER.warn(MessageFormatUtil.format(
                                Tesseract4LogMessageConstant.UNSUPPORTED_EXIF_ORIENTATION_VALUE,
                                orientation));
                        return ROTATION_0;
                }
            }
        }
        return ROTATION_0;
    }

    /**
     * Rotates image by specified angle.
     *
     * @param pix image source represented by {@link net.sourceforge.lept4j.Pix}
     * @param rotation to rotate image at
     * @return rotated image, if rotation differs from 0
     */
    static Pix rotate(final Pix pix, int rotation) {
        final Leptonica instance = Leptonica.INSTANCE;
        switch (rotation) {
            case ROTATION_90:
                return instance.pixRotate90(pix, 1);
            case ROTATION_180:
                return instance.pixRotate180(pix, pix);
            case ROTATION_270:
                return instance.pixRotate90(pix, -1);
            default:
                return pix;
        }
    }

    /**
     * Detects and applies rotation to image.
     *
     * @param imageData source image to rotate if needed
     * @return rotated image, if rotation differs from 0
     */
    static ImageData applyRotation(final ImageData imageData) {
        Pix pix = readPix(imageData.getData());
        if (pix == null) {
            return imageData;
        } else {
            ImageData newImageData = imageData;
            try {
                PointerByReference data = new PointerByReference();
                NativeSizeByReference size = new NativeSizeByReference();
                if (Leptonica.INSTANCE.pixWriteMemPng(data, size, pix, 0) == 0) {
                    newImageData = ImageDataFactory.create(
                            data.getValue().getByteArray(0, size.getValue().intValue())
                    );
                }
            } finally {
                destroyPix(pix);
            }
            return newImageData;
        }
    }
}
