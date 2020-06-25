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

import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.Version;
import com.itextpdf.kernel.counter.ContextManager;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ReflectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    private static final String KERNEL_PACKAGE = "com.itextpdf.kernel.";
    private static final String LICENSEKEY_PACKAGE = "com.itextpdf.licensekey.";

    private static final String CONTEXT_MANAGER = "counter.ContextManager";
    private static final String LICENSEKEY = "LicenseKey";
    private static final String LICENSEKEY_PRODUCT = "LicenseKeyProduct";
    private static final String LICENSEKEY_FEATURE = "LicenseKeyProductFeature";

    private static final String REGISTER_GENERIC_CONTEXT = "registerGenericContext";
    private static final String SCHEDULED_CHECK = "scheduledCheck";

    private static final String NO_PDFOCR_TESSERACT4 = "No license loaded for product pdfOcr-Tesseract4. Please use LicenseKey.loadLicense(...) to load one.";

    private static Map<String, Class<?>> cachedClasses = new HashMap<>();
    private static Map<MethodSignature, AccessibleObject> cachedMethods = new HashMap<>();

    static {
        try {
            ContextManager contextManager = ContextManager.getInstance();
            callMethod(KERNEL_PACKAGE + CONTEXT_MANAGER, REGISTER_GENERIC_CONTEXT, contextManager,
                    new Class[] {Collection.class, Collection.class},
                    Collections.singletonList("com.itextpdf.pdfocr"),
                    Collections.singletonList("com.itextpdf.pdfocr.tesseract4"));
            callMethod(KERNEL_PACKAGE + CONTEXT_MANAGER, REGISTER_GENERIC_CONTEXT, contextManager,
                    new Class[] {Collection.class, Collection.class},
                    Collections.singletonList("com.itextpdf.pdfocr.tesseract4"),
                    Collections.singletonList("com.itextpdf.pdfocr.tesseract4"));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private ReflectionUtils() {
    }

    public static void scheduledCheck() {
        try {
            Class licenseKeyClass = getClass(LICENSEKEY_PACKAGE + LICENSEKEY);
            Class licenseKeyProductClass = getClass(LICENSEKEY_PACKAGE + LICENSEKEY_PRODUCT);
            Class licenseKeyProductFeatureClass = getClass(LICENSEKEY_PACKAGE + LICENSEKEY_FEATURE);

            Object licenseKeyProductFeatureArray = Array.newInstance(licenseKeyProductFeatureClass, 0);

            Class[] params = new Class[] {
                    String.class,
                    Integer.TYPE,
                    Integer.TYPE,
                    licenseKeyProductFeatureArray.getClass()
            };

            Constructor licenseKeyProductConstructor = licenseKeyProductClass.getConstructor(params);

            Object licenseKeyProductObject = licenseKeyProductConstructor.newInstance(
                    PdfOcrTesseract4ProductInfo.PRODUCT_NAME,
                    PdfOcrTesseract4ProductInfo.MAJOR_VERSION,
                    PdfOcrTesseract4ProductInfo.MINOR_VERSION,
                    licenseKeyProductFeatureArray
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

    private static Object callMethod(String className, String methodName, Object target, Class[] parameterTypes,
            Object... args) {
        try {
            Method method = findMethod(className, methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (NoSuchMethodException e) {
            logger.warn(MessageFormatUtil.format("Cannot find method {0} for class {1}", methodName, className));
        } catch (ClassNotFoundException e) {
            logger.warn(MessageFormatUtil.format("Cannot find class {0}", className));
        } catch (IllegalArgumentException e) {
            logger.warn(MessageFormatUtil
                    .format("Illegal arguments passed to {0}#{1} method call: {2}", className, methodName,
                            e.getMessage()));
        } catch (Exception e) {
            // Converting checked exceptions to unchecked RuntimeException (java-specific comment).
            //
            // If kernel utils throws an exception at this point, we consider it as unrecoverable situation for
            // its callers (pdfOcr methods).
            // It's might be more suitable to wrap checked exceptions at a bit higher level, but we do it here for
            // the sake of convenience.
            throw new RuntimeException(e.toString(), e);
        }
        return null;
    }

    private static Method findMethod(String className, String methodName, Class[] parameterTypes)
            throws NoSuchMethodException, ClassNotFoundException {
        MethodSignature tm = new MethodSignature(className, parameterTypes, methodName);
        Method m = (Method) cachedMethods.get(tm);
        if (m == null) {
            m = findClass(className).getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            cachedMethods.put(tm, m);
        }
        return m;
    }

    private static Class<?> findClass(String className) throws ClassNotFoundException {
        Class<?> c = cachedClasses.get(className);
        if (c == null) {
            c = getClass(className);
            cachedClasses.put(className, c);
        }
        return c;
    }

    private static Class<?> getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    private static class MethodSignature {
        protected final String className;
        private final String methodName;
        protected Class[] parameterTypes;

        MethodSignature(String className, Class[] parameterTypes, String methodName) {
            this.methodName = methodName;
            this.className = className;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + Arrays.hashCode(parameterTypes);
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MethodSignature that = (MethodSignature) o;

            if (!className.equals(that.className)) {
                return false;
            }
            if (!Arrays.equals(parameterTypes, that.parameterTypes)) {
                return false;
            }
            return methodName != null ? methodName.equals(that.methodName) : that.methodName == null;

        }
    }
}
