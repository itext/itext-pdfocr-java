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

import com.itextpdf.kernel.Version;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class ReflectionUtils {

    private static final String LICENSEKEY_PACKAGE = "com.itextpdf.licensekey.";

    private static final String LICENSEKEY = "LicenseKey";
    private static final String LICENSEKEY_PRODUCT = "LicenseKeyProduct";

    private static final String SCHEDULED_CHECK = "scheduledCheck";

    private static final String NO_PDFOCR_TESSERACT4 = "No license loaded for product pdfOcr-Tesseract4. Please use LicenseKey.loadLicense(...) to load one.";

    private ReflectionUtils() {
    }

    public static void scheduledCheck() {
        try {
            Class licenseKeyClass = getClass(LICENSEKEY_PACKAGE + LICENSEKEY);
            Class licenseKeyProductClass = getClass(LICENSEKEY_PACKAGE + LICENSEKEY_PRODUCT);

            Class[] params = new Class[] {
                    String.class, String.class, String.class
            };

            Constructor licenseKeyProductConstructor = licenseKeyProductClass.getConstructor(params);

            Object licenseKeyProductObject = licenseKeyProductConstructor.newInstance(
                    PdfOcrTesseract4ProductInfo.PRODUCT_NAME,
                    String.valueOf(PdfOcrTesseract4ProductInfo.MAJOR_VERSION),
                    String.valueOf(PdfOcrTesseract4ProductInfo.MINOR_VERSION)
            );

            Method method = licenseKeyClass.getMethod(SCHEDULED_CHECK, licenseKeyProductClass);
            method.invoke(null, licenseKeyProductObject);
        } catch (Exception e) {
            if (null != e && null != e.getCause()) {
                String message = e.getCause().getMessage();
                if (NO_PDFOCR_TESSERACT4.equals(message)) {
                    throw new RuntimeException(message, e.getCause());
                }
            }
            if (!Version.isAGPLVersion()) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private static Class<?> getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
