/*
 * Copyright (C) 2026 Morphe
 *
 * This file is part of the morphe-patches project:
 * https://github.com/MorpheApp/morphe-patches
 *
 * Original author(s):
 * - Jav1x (https://github.com/Jav1x)
 *
 * Licensed under the GNU General Public License v3.0.
 */


package app.morphe.extension.youtube.patches.yandexvot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Resolves the host color API without linking this add-on to either its old or
 * new location. Morphe moved the method from {@code Utils} to {@code ThemeUtils}
 * in v1.40.0-dev.15.
 */
final class YandexThemeCompat {
    private static final String THEME_UTILS_CLASS =
            "app.morphe.extension.shared.theme.ThemeUtils";
    private static final String LEGACY_UTILS_CLASS =
            "app.morphe.extension.shared.Utils";
    private static final String METHOD_NAME = "getAppForegroundColor";

    private static volatile Method foregroundColorMethod;

    private YandexThemeCompat() {
    }

    static int getAppForegroundColor() {
        Method method = foregroundColorMethod;
        if (method == null) {
            synchronized (YandexThemeCompat.class) {
                method = foregroundColorMethod;
                if (method == null) {
                    method = resolveMethod(YandexThemeCompat.class.getClassLoader());
                    foregroundColorMethod = method;
                }
            }
        }

        try {
            Object result = method.invoke(null);
            if (!(result instanceof Integer)) {
                throw new IllegalStateException(METHOD_NAME + "() did not return an int color");
            }
            return (Integer) result;
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Cannot access " + method, ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            throw new IllegalStateException(METHOD_NAME + "() failed", cause == null ? ex : cause);
        }
    }

    static Method resolveMethod(ClassLoader classLoader) {
        String[] classNames = {
                THEME_UTILS_CLASS,
                LEGACY_UTILS_CLASS,
        };
        for (String className : classNames) {
            try {
                Method candidate = classLoader.loadClass(className)
                        .getDeclaredMethod(METHOD_NAME);
                candidate.setAccessible(true);
                return candidate;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                // Try the next supported host API generation.
            }
        }

        throw new IllegalStateException(
                "No supported Morphe foreground-color API found (ThemeUtils or Utils)");
    }
}
