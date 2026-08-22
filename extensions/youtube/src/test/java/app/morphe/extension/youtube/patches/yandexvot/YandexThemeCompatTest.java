package app.morphe.extension.youtube.patches.yandexvot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Test;

public class YandexThemeCompatTest {
    private static final String THEME_UTILS_CLASS =
            "app.morphe.extension.shared.theme.ThemeUtils";
    private static final String LEGACY_UTILS_CLASS =
            "app.morphe.extension.shared.Utils";

    @Test
    public void prefersNewThemeUtilsApi() throws Exception {
        MappedClassLoader loader = new MappedClassLoader(NewThemeUtils.class, LegacyUtils.class);

        assertEquals(
                NewThemeUtils.COLOR,
                (int) YandexThemeCompat.resolveMethod(loader).invoke(null)
        );
        assertEquals(
                NewThemeUtils.class,
                YandexThemeCompat.resolveMethod(loader).getDeclaringClass()
        );
    }

    @Test
    public void fallsBackToLegacyUtilsApi() throws Exception {
        MappedClassLoader loader = new MappedClassLoader(null, LegacyUtils.class);

        assertEquals(
                LegacyUtils.class,
                YandexThemeCompat.resolveMethod(loader).getDeclaringClass()
        );
        assertEquals(
                LegacyUtils.COLOR,
                (int) YandexThemeCompat.resolveMethod(loader).invoke(null)
        );
    }

    @Test
    public void failsClosedWhenNoSupportedColorApiExists() {
        MappedClassLoader loader = new MappedClassLoader(null, null);

        assertThrows(IllegalStateException.class, () -> YandexThemeCompat.resolveMethod(loader));
    }

    @Test
    public void bottomSheetUsesCompatibilityHelperWithoutDirectHostThemeImports() throws IOException {
        String source = new String(Files.readAllBytes(findSource()), StandardCharsets.UTF_8);

        assertEquals(9, countMatches(source, "YandexThemeCompat.getAppForegroundColor()"));
        assertEquals(0, countMatches(source, "import app.morphe.extension.shared.Utils;"));
        assertEquals(
                0,
                countMatches(source, "import app.morphe.extension.shared.theme.ThemeUtils;")
        );
        assertEquals(0, countDirectHostApiCalls(source));
    }

    private static Path findSource() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(Paths.get(
                    "extensions", "youtube", "src", "main", "java", "app", "morphe",
                    "extension", "youtube", "patches", "yandexvot",
                    "YandexVoiceOverTranslationBottomSheet.java"
            ));
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        throw new AssertionError("Could not locate Yandex bottom-sheet source");
    }

    private static int countMatches(String value, String token) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static int countDirectHostApiCalls(String source) {
        Pattern directCall = Pattern.compile(
                "(?<!YandexThemeCompat\\.)\\b(?:Utils|ThemeUtils)\\.getAppForegroundColor\\s*\\("
        );
        return (int) directCall.matcher(source).results().count();
    }

    private static final class MappedClassLoader extends ClassLoader {
        private final Class<?> themeUtils;
        private final Class<?> legacyUtils;

        private MappedClassLoader(Class<?> themeUtils, Class<?> legacyUtils) {
            super(MappedClassLoader.class.getClassLoader());
            this.themeUtils = themeUtils;
            this.legacyUtils = legacyUtils;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (THEME_UTILS_CLASS.equals(name)) {
                if (themeUtils == null) throw new ClassNotFoundException(name);
                return themeUtils;
            }
            if (LEGACY_UTILS_CLASS.equals(name)) {
                if (legacyUtils == null) throw new ClassNotFoundException(name);
                return legacyUtils;
            }
            return super.loadClass(name, resolve);
        }
    }

    private static final class NewThemeUtils {
        private static final int COLOR = 0xff111213;

        public static int getAppForegroundColor() {
            return COLOR;
        }
    }

    private static final class LegacyUtils {
        private static final int COLOR = 0xff313233;

        public static int getAppForegroundColor() {
            return COLOR;
        }
    }
}
