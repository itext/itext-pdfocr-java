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
package com.itextpdf.pdfocr.onnxtr.conf.paddle.model;

import java.util.Objects;

/**
 * POJO for the DecodeImage transform operation within a {@code PreProcess}
 * object in a config file.
 */
public class DecodeImage implements TransformOp {
    /**
     * Expected wrapping key for the DecodeImage operation.
     */
    public static final String WRAPPING_KEY = "DecodeImage";

    private final boolean channelFirst;
    private final ImgMode imgMode;

    /**
     * Creates a new POJO for the config file object.
     *
     * @param channelFirst value under the {@code channel_first} key
     * @param imgMode value under the {@code img_mode} key
     */
    public DecodeImage(boolean channelFirst, ImgMode imgMode) {
        this.channelFirst = channelFirst;
        this.imgMode = Objects.requireNonNull(imgMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWrappingKey() {
        return WRAPPING_KEY;
    }

    /**
     * Returns the value under the {@code channel_first} key.
     *
     * @return the value under the {@code channel_first} key
     */
    public boolean getChannelFirst() {
        return channelFirst;
    }

    /**
     * Returns the value under the {@code img_mode} key.
     *
     * @return the value under the {@code img_mode} key
     */
    public ImgMode getImgMode() {
        return imgMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DecodeImage that = (DecodeImage) o;
        return channelFirst == that.channelFirst
                && imgMode == that.imgMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash((Object)channelFirst, imgMode);
    }
}
