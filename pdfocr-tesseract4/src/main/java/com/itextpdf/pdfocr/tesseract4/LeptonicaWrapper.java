/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.pdfocr.tesseract4;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.ochafik.lang.jnaerator.runtime.NativeSizeByReference;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.nio.ByteBuffer;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Leptonica1;
import net.sourceforge.lept4j.Pix;

/**
 * Wrapper class around {@link net.sourceforge.lept4j.Leptonica} and {@link net.sourceforge.lept4j.Leptonica1}.
 * It uses one of the above classes based on JDK version. {@link net.sourceforge.lept4j.Leptonica}
 * is not supported for JDK 19 and above. But {@link net.sourceforge.lept4j.Leptonica1} requires a fresh version
 * of leptonica native library on Linux.
 */
final class LeptonicaWrapper {
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
