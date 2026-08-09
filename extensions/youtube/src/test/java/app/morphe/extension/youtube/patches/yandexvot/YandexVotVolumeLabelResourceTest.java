package app.morphe.extension.youtube.patches.yandexvot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class YandexVotVolumeLabelResourceTest {
    private static final String RESOURCE_KEY = "morphe_yandex_vot_volume_label_value";
    private static final Pattern STRING = Pattern.compile(
            "<string name=\\\"([^\\\"]+)\\\">([^<]+)</string>"
    );

    @Test
    public void volumeLabelFormatsEveryPackagedLocaleAndIsUsedByTheBottomSheet() throws IOException {
        Path resourcesRoot = findBaseStrings().getParent().getParent().getParent();
        for (String locale : new String[] {"values", "values-ru-rRU", "values-uk-rUA"}) {
            String strings = readStrings(resourcesRoot.resolve(locale).resolve("youtube").resolve("strings.xml"));
            String formatted = format(strings, RESOURCE_KEY, "Volume", "100%");
            assertEquals("Volume: 100%", formatted);
            assertTrue("Volume label must retain the percentage", formatted.endsWith("100%"));
        }

        String bottomSheet = readStrings(findSource());
        assertTrue("Bottom sheet must request the localized volume label",
                bottomSheet.contains("str(\"" + RESOURCE_KEY + "\","));
    }

    private static Path findBaseStrings() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(Paths.get(
                    "patches", "src", "main", "resources", "addresources", "values", "youtube", "strings.xml"
            ));
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        throw new AssertionError("Could not locate add-on resources");
    }

    private static Path findSource() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(Paths.get(
                    "extensions", "youtube", "src", "main", "java", "app", "morphe", "extension",
                    "youtube", "patches", "yandexvot", "YandexVoiceOverTranslationBottomSheet.java"
            ));
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        throw new AssertionError("Could not locate Yandex bottom-sheet source");
    }

    private static String format(String strings, String resourceKey, Object... arguments) {
        Matcher matcher = STRING.matcher(strings);
        while (matcher.find()) {
            if (resourceKey.equals(matcher.group(1))) {
                return String.format(Locale.ROOT, matcher.group(2), arguments);
            }
        }
        throw new AssertionError("Missing volume label resource: " + resourceKey);
    }

    private static String readStrings(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
