package app.morphe.extension.youtube.videoplayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class YandexVotTimerResourceTest {
    private static final Pattern STRING = Pattern.compile(
            "<string name=\\\"([^\\\"]+)\\\">([^<]+)</string>"
    );

    @Test
    public void timerResourceKeysFormatCompactValuesInEveryPackagedLocale() throws IOException {
        Path baseStrings = findBaseStrings();
        Path resourcesRoot = baseStrings.getParent().getParent().getParent();
        for (String[] locale : new String[][] {
                {"values", "1m", "59s"},
                {"values-ru-rRU", "1м", "59с"},
                {"values-uk-rUA", "1хв", "59с"}
        }) {
            String strings = readStrings(
                    resourcesRoot.resolve(locale[0]).resolve("youtube").resolve("strings.xml")
            );
            String minutes = format(
                    strings,
                    YandexVotButton.YandexCountdownButton.TIMER_MINUTES_RESOURCE,
                    1
            );
            String seconds = format(
                    strings,
                    YandexVotButton.YandexCountdownButton.TIMER_SECONDS_RESOURCE,
                    59
            );
            assertEquals(locale[1], minutes);
            assertEquals(locale[2], seconds);
            assertFalse("Timer text must be space-free", minutes.contains(" "));
            assertFalse("Timer text must be space-free", seconds.contains(" "));
        }
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
        throw new AssertionError("Could not locate add-on timer resources");
    }

    private static String format(String strings, String resourceKey, int value) {
        Matcher matcher = STRING.matcher(strings);
        while (matcher.find()) {
            if (resourceKey.equals(matcher.group(1))) {
                String formatted = String.format(Locale.ROOT, matcher.group(2), value);
                assertFalse("Timer text must not expose its resource key", formatted.contains(resourceKey));
                return formatted;
            }
        }
        throw new AssertionError("Missing timer resource: " + resourceKey);
    }

    private static String readStrings(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
