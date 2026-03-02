/*
    Copyright (C) 2021-2024, Mindee | Felix Dittrich.

    This program is licensed under the Apache License 2.0.
    See <https://opensource.org/licenses/Apache-2.0> for full license details.
 */
package com.itextpdf.pdfocr.onnx.recognition;

import com.itextpdf.pdfocr.onnx.exceptions.PdfOcrOnnxTrExceptionMessageConstant;
import com.itextpdf.pdfocr.onnx.util.MathUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Small utility class, which handles text box splitting for text recognition
 * models, based on the algorithm from OnnxTR.
 *
 * <p>
 * This is not really useful for other models, like EasyOCR and PaddleOCR
 * ones, as those have a dynamic input size and can have whatever long lines.
 * They also work on lines and not words, so they wouldn't mind additional
 * information in one go.
 */
class TextBoxSplitter {
    /**
     * Aspect ratio, at which a text box is split for better text recognition.
     */
    private static final float SPLIT_CROPS_MAX_RATIO = 8;

    /**
     * Target aspect ratio for the text box splits.
     */
    private static final float SPLIT_CROPS_TARGET_RATIO = 6;

    /**
     * Multiplier, which controls the overlap between splits. Factor of 1 means, that there will be no overlap.
     *
     * <p>
     * This is for cases, when a split happens in the middle of a character. With some overlap, at least one of the
     * sub-images will contain the character in full.
     */
    private static final float SPLIT_CROPS_DILATION_FACTOR = 1.4F;

    /**
     * State for the current inputs split. Contains the number of images that were spawned from the original image.
     */
    private final ArrayDeque<Integer> mergeQueue = new ArrayDeque<>();

    /**
     * Creates new {@link TextBoxSplitter} instance.
     */
    public TextBoxSplitter() {
        // Empty constructor in order for default one to not be removed if another one is added.
    }

    /**
     * Wrap the iterator of the text recognition predictor inputs, which
     * splits the images, if they have too skewed aspect ratio.
     *
     * @param inputs models inputs to split
     *
     * @return iterator, which outputs split input images
     */
    public Iterator<BufferedImage> mapInputs(Iterator<BufferedImage> inputs) {
        if (!mergeQueue.isEmpty()) {
            throw new IllegalStateException(
                    PdfOcrOnnxTrExceptionMessageConstant.CANNOT_START_ANOTHER_MAPPING_OPERATION
            );
        }
        return new Iterator<BufferedImage>() {
            private final ArrayDeque<BufferedImage> buffer = new ArrayDeque<>();

            @Override
            public boolean hasNext() {
                return !buffer.isEmpty() || inputs.hasNext();
            }

            @Override
            public BufferedImage next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                // Emptying the buffer first
                if (!buffer.isEmpty()) {
                    return buffer.remove();
                }

                final BufferedImage image = inputs.next();
                final int width = image.getWidth();
                final int height = image.getHeight();
                final double aspectRatio = (double) width / height;
                if (aspectRatio < SPLIT_CROPS_MAX_RATIO) {
                    mergeQueue.add(1);
                    // No need to touch the buffer here, just return as-is
                    return image;
                }

                // For some reason here is truncation in OnnxTR...
                int splitCount = (int) Math.ceil(aspectRatio / SPLIT_CROPS_TARGET_RATIO);
                float rawSplitWidth = (float) width / splitCount;
                float targetSplitHalfWidth = (SPLIT_CROPS_DILATION_FACTOR * rawSplitWidth) / 2;
                int nonEmptySplitCount = 0;
                for (int j = 0; j < splitCount; ++j) {
                    final float center = (j + 0.5F) * rawSplitWidth;
                    final int minX = Math.max(0, (int) Math.floor(center - targetSplitHalfWidth));
                    final int maxX = Math.min(width - 1, (int) Math.ceil(center + targetSplitHalfWidth));
                    final int currentSplitWidth = maxX - minX;
                    if (currentSplitWidth == 0) {
                        continue;
                    }
                    ++nonEmptySplitCount;
                    buffer.add(image.getSubimage(minX, 0, currentSplitWidth, height));
                }
                mergeQueue.add(nonEmptySplitCount);
                return buffer.remove();
            }
        };
    }

    /**
     * Wrap the iterator of the text recognition predictor outputs, which
     * merges text back, based on how the input images were split before.
     *
     * @param outputs text outputs to merge
     *
     * @return iterator, which returns merged images
     */
    public Iterator<String> mapOutputs(Iterator<String> outputs) {
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return outputs.hasNext();
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                // Important, that we "touch" the iterator first, as
                // mergeQueue is lazily populated from it
                String text = outputs.next();
                int stringPartsLeft = mergeQueue.remove();
                // Return as-is if no need to merge
                if (stringPartsLeft == 1) {
                    return text;
                }

                // Otherwise doing the merge
                final StringBuilder sb = new StringBuilder(text);
                while (stringPartsLeft > 1) {
                    mergeStrings(sb, outputs.next());
                    --stringPartsLeft;
                }
                return sb.toString();
            }
        };
    }

    /**
     * Merges strings, collected from splits of text images.
     *
     * @param collector string builder collector, which contains the current left part of the string
     * @param nextString next string to add to the collector
     */
    private static void mergeStrings(StringBuilder collector, String nextString) {
        // Comments are also pretty much copies from OnnxTR...
        int commonLength = Math.min(collector.length(), nextString.length());
        double[] scores = new double[commonLength];
        for (int i = 0; i < commonLength; ++i) {
            scores[i] = MathUtil.calculateLevenshteinDistance(
                    collector.substring(collector.length() - i - 1),
                    nextString.substring(0, i + 1)
            ) / (i + 1.0);
        }

        int index = 0;
        // Comparing floats to 0 is fine here, as it only happens, when the
        // integer nominator (i.e. Levenshtein distance) was 0
        if (commonLength > 1 && scores[0] == 0 && scores[1] == 0) {
            // Edge case (split in the middle of char repetitions): if it starts with 2 or more 0

            // Compute n_overlap (number of overlapping chars, geometrically determined)
            final int overlap = (int) Math.round(
                    nextString.length() * (SPLIT_CROPS_DILATION_FACTOR - 1) / SPLIT_CROPS_DILATION_FACTOR);
            // Find the number of consecutive zeros in the scores list
            // Impossible to have a zero after a non-zero score in that case
            final int zeros = (int) Arrays.stream(scores).filter(x -> x == 0).count();
            index = Math.min(zeros, overlap);
        } else {
            // Common case: choose the min score index
            double minScore = 1.0;
            for (int i = 0; i < commonLength; ++i) {
                if (scores[i] < minScore) {
                    minScore = scores[i];
                    index = i + 1;
                }
            }
        }

        if (index == 0) {
            collector.append(nextString);
        } else {
            collector.setLength(Math.max(0, collector.length() - 1));
            collector.append(nextString, index - 1, nextString.length());
        }
    }
}
