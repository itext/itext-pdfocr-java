/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4Exception;
import com.itextpdf.pdfocr.tesseract4.exceptions.PdfOcrTesseract4ExceptionMessageConstant;
import com.itextpdf.pdfocr.tesseract4.logs.Tesseract4LogMessageConstant;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.Pointer;
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
import net.sourceforge.lept4j.Leptonica1;
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
     * @param pix {@link Pix} object to be processed
     * @param imagePreprocessingOptions {@link ImagePreprocessingOptions}
     * @return preprocessed {@link net.sourceforge.lept4j.Pix} object
     */
    static Pix preprocessPix(final Pix pix,
                             final ImagePreprocessingOptions imagePreprocessingOptions) {
        Pix pix1 = convertToGrayscale(pix);
        pix1 = otsuImageThresholding(pix1, imagePreprocessingOptions);
        return pix1;
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
        if (pix != null) {
            int depth = LeptonicaWrapper.pixGetDepth(pix);

            if (depth == 32) {
                return LeptonicaWrapper.pixConvertRGBToLuminance(pix);
            } else {
                return LeptonicaWrapper.pixRemoveColormap(pix,
                        ILeptonica.REMOVE_CMAP_TO_GRAYSCALE);
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
     * @param inputPix {@link Pix} object to be processed
     * @param imagePreprocessingOptions {@link ImagePreprocessingOptions}
     * @return {@link net.sourceforge.lept4j.Pix} object after thresholding
     */
    static Pix otsuImageThresholding(final Pix inputPix,
                                     final ImagePreprocessingOptions imagePreprocessingOptions) {
        if (inputPix != null) {
            Pix binarizedPix = null;
            if (inputPix.d == 8) {
                PointerByReference pointer = new PointerByReference();
                LeptonicaWrapper
                        .pixOtsuAdaptiveThreshold(inputPix,
                                getOtsuAdaptiveThresholdTileSize(inputPix.w,
                                        imagePreprocessingOptions.getTileWidth()),
                                getOtsuAdaptiveThresholdTileSize(inputPix.h,
                                        imagePreprocessingOptions.getTileHeight()),
                                getOtsuAdaptiveThresholdSmoothingTileSize(inputPix.w,
                                        imagePreprocessingOptions.isSmoothTiling()),
                                getOtsuAdaptiveThresholdSmoothingTileSize(inputPix.h,
                                        imagePreprocessingOptions.isSmoothTiling()),
                                0,null, pointer);
                binarizedPix = new Pix(pointer.getValue());
                if (binarizedPix.w > 0 && binarizedPix.h > 0) {
                    // destroying original pix
                    destroyPix(inputPix);
                    return binarizedPix;
                } else {
                    final String logMessage = MessageFormatUtil.format(
                            Tesseract4LogMessageConstant.CANNOT_BINARIZE_IMAGE,
                            inputPix.d);
                    LOGGER.info(logMessage);
                    // destroying created PointerByReference object
                    destroyPix(binarizedPix);
                    return inputPix;
                }
            } else {
                LOGGER.info(MessageFormatUtil.format(
                        Tesseract4LogMessageConstant.CANNOT_BINARIZE_IMAGE,
                        inputPix.d));
                return inputPix;
            }
        } else {
            return inputPix;
        }
    }

    /**
     * Gets adaptive threshold tile size.
     */
    static int getOtsuAdaptiveThresholdTileSize(int sizeOfImage, int sizeOfTile) {
        if (sizeOfTile == 0) {
            return sizeOfImage;
        } else {
            return sizeOfTile;
        }
    }

    /**
     * Gets adaptive threshold smoothing tile size.
     * Can be either equal to page size or 0.
     */
    static int getOtsuAdaptiveThresholdSmoothingTileSize(int sizeOfImage, boolean isSmoothTiling) {
        if (isSmoothTiling) {
            return sizeOfImage;
        } else {
            return 0;
        }
    }

    /**
     * Gets an integer pixel in the default RGB color model.
     */
    static int getImagePixelColor(BufferedImage image, int x, int y) {
        return image.getRGB(x, y);
    }

    /**
     * Destroys {@link net.sourceforge.lept4j.Pix} object.
     *
     * @param inputPix {@link net.sourceforge.lept4j.Pix} object to be destroyed
     */
    static void destroyPix(Pix inputPix) {
        if (inputPix != null) {
            LeptonicaWrapper.lept_free(inputPix.getPointer());
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
     * @param pageSegmentationMode page segmentation mode {@link java.lang.Integer}
     * @param userWordsFilePath path to a temporary file with user words
     */
    static void setTesseractProperties(
            final ITesseract tesseractInstance,
            final String tessData, final String languages,
            final Integer pageSegmentationMode, final String userWordsFilePath) {
        tesseractInstance.setDatapath(tessData);
        tesseractInstance.setLanguage(languages);
        if (pageSegmentationMode != null) {
            tesseractInstance.setPageSegMode(pageSegmentationMode);
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
     * @param isWindowsPlatform true is current os is windows
     * @param tessData path to tess data directory
     * @param languages list of languages in required format as
     *                  {@link java.lang.String}
     * @param userWordsFilePath path to a temporary file with user words
     * @return initialized {@link net.sourceforge.tess4j.ITesseract} object
     */
    static ITesseract initializeTesseractInstance(final boolean isWindowsPlatform,
            final String tessData, final String languages,
            final String userWordsFilePath) {
        try {
            if (isWindowsPlatform) {
                return new Tesseract1();
            } else {
                return new Tesseract();
            }
        } catch (LinkageError e) {
            throw new PdfOcrTesseract4Exception(isWindowsPlatform ?
                    PdfOcrTesseract4ExceptionMessageConstant.TESSERACT_LIB_NOT_INSTALLED_WIN :
                    PdfOcrTesseract4ExceptionMessageConstant.TESSERACT_LIB_NOT_INSTALLED, e);
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

    static Pix readPixFromFile(File inputImage) {
        return LeptonicaWrapper.pixRead(inputImage.getAbsolutePath());
    }

    /**
     * Converts Leptonica {@link net.sourceforge.lept4j.Pix}
     * to {@link java.awt.image.BufferedImage} with
     * {@link net.sourceforge.lept4j.ILeptonica#IFF_PNG} image format.
     *
     * @param inputPix input {@link net.sourceforge.lept4j.Pix} object
     * @return result {@link java.awt.image.BufferedImage} object
     * @throws IOException if it is not possible to convert
     */
    static BufferedImage convertPixToImage(final Pix inputPix)
            throws IOException {
        if (inputPix != null) {
            BufferedImage bi = null;
            PointerByReference pdata = new PointerByReference();
            try {
                NativeSizeByReference psize = new NativeSizeByReference();
                LeptonicaWrapper.pixWriteMem(pdata, psize, inputPix, ILeptonica.IFF_PNG);
                byte[] b = pdata.getValue().getByteArray(0,
                        psize.getValue().intValue());
                try (InputStream in = new ByteArrayInputStream(b)) {
                    bi = ImageIO.read(in);
                }
            } finally {
                LeptonicaWrapper.lept_free(pdata.getValue());
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
     * @param filename provided file path to save the
     * {@link net.sourceforge.lept4j.Pix}
     * @param pix provided {@link net.sourceforge.lept4j.Pix} to be saved
     */
    static void savePixToPngFile(final String filename,
                                 final Pix pix) {
        if (pix != null) {
            try {
                LeptonicaWrapper.pixWritePng(filename, pix,
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
    static String getParentDirectoryFile(final String path) {
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
            pix = LeptonicaWrapper.pixReadMem(bb, size);
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
        switch (rotation) {
            case ROTATION_90:
                return LeptonicaWrapper.pixRotate90(pix, 1);
            case ROTATION_180:
                return LeptonicaWrapper.pixRotate180(pix, pix);
            case ROTATION_270:
                return LeptonicaWrapper.pixRotate90(pix, -1);
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
                if (LeptonicaWrapper.pixWriteMemPng(data, size, pix, 0) == 0) {
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

    private static final class LeptonicaWrapper {
        private static final int LEPTONICA_NOT_SUPPORTED_JDK_VERSION = 19;
        private static final int JDK_MAJOR_VERSION = getJavaMajorVersion();

        private LeptonicaWrapper() {}

        public static int pixGetDepth(Pix pix) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixGetDepth(pix);
            }
            return Leptonica1.pixGetDepth(pix);
        }

        public static Pix pixConvertRGBToLuminance(Pix pix) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixConvertRGBToLuminance(pix);
            }
            return Leptonica1.pixConvertRGBToLuminance(pix);
        }

        public static Pix pixRemoveColormap(Pix pix, int option) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixRemoveColormap(pix, option);
            }
            return Leptonica1.pixRemoveColormap(pix, option);
        }

        public static Pix pixRead(String var1) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixRead(var1);
            }
            return Leptonica1.pixRead(var1);
        }

        public static void pixOtsuAdaptiveThreshold(Pix pix, int i, int i1, int i2, int i3, float v,
                PointerByReference pointerByReference, PointerByReference pointerByReference1) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                Leptonica.INSTANCE.pixOtsuAdaptiveThreshold(pix, i, i1, i2, i3, v, pointerByReference,
                        pointerByReference1);
                return;
            }
            Leptonica1.pixOtsuAdaptiveThreshold(pix, i, i1, i2, i3, v, pointerByReference, pointerByReference1);
        }

        public static void lept_free(Pointer pointer) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                Leptonica.INSTANCE.lept_free(pointer);
                return;
            }
            Leptonica1.lept_free(pointer);
        }

        public static void pixWriteMem(PointerByReference pointer, NativeSizeByReference nativeSize, Pix pix, int i) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                Leptonica.INSTANCE.pixWriteMem(pointer, nativeSize, pix, i);
                return;
            }
            Leptonica1.pixWriteMem(pointer, nativeSize, pix, i);
        }

        public static int pixWriteMemPng(PointerByReference pointer, NativeSizeByReference nativeSize, Pix pix, int i) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixWriteMemPng(pointer, nativeSize, pix, i);
            }
            return Leptonica1.pixWriteMemPng(pointer, nativeSize, pix, i);
        }

        public static void pixWritePng(String s, Pix pix, float v) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                Leptonica.INSTANCE.pixWritePng(s, pix, v);
                return;
            }
            Leptonica1.pixWritePng(s, pix, v);
        }

        public static Pix pixReadMem(ByteBuffer buffer, NativeSize nativeSize) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixReadMem(buffer, nativeSize);
            }
            return Leptonica1.pixReadMem(buffer, nativeSize);
        }

        public static Pix pixRotate90(Pix pix, int i) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixRotate90(pix, i);
            }
            return Leptonica1.pixRotate90(pix, i);
        }

        public static Pix pixRotate180(Pix pix1, Pix pix2) {
            if (JDK_MAJOR_VERSION < LEPTONICA_NOT_SUPPORTED_JDK_VERSION) {
                return Leptonica.INSTANCE.pixRotate180(pix1, pix2);
            }
            return Leptonica1.pixRotate180(pix1, pix2);
        }

        /**
         * gets java runtime version.
         *
         * @return major version of runtime java
         */
        private static int getJavaMajorVersion() {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf('.');
                if(dot != -1) {
                    version = version.substring(0, dot);
                }
            }
            return Integer.parseInt(version);
        }
    }
}
