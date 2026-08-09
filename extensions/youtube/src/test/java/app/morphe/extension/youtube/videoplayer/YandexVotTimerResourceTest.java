package app.morphe.extension.youtube.videoplayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

public class YandexVotTimerResourceTest {
    private static final Pattern STRING = Pattern.compile(
            "<string name=\\\"([^\\\"]+)\\\">([^<]+)</string>"
    );

    @Test
    public void timerResourceKeysExistAndFormatCompactValues() throws IOException {
        String strings = readStrings(findBaseStrings());

        assertEquals("1 min", format(strings, YandexVotButton.YandexCountdownButton.TIMER_MINUTES_RESOURCE, 1));
        assertEquals("59 sec", format(strings, YandexVotButton.YandexCountdownButton.TIMER_SECONDS_RESOURCE, 59));
    }

    @Test
    public void timerKeysArePresentInEveryPackagedLocale() throws IOException {
        Path baseStrings = findBaseStrings();
        Path resourcesRoot = baseStrings.getParent().getParent().getParent();
        for (String locale : new String[] {"values", "values-ru-rRU", "values-uk-rUA"}) {
            String strings = readStrings(
                    resourcesRoot.resolve(locale).resolve("youtube").resolve("strings.xml")
            );
            assertTrue(strings.contains("name=\""
                    + YandexVotButton.YandexCountdownButton.TIMER_MINUTES_RESOURCE + "\""));
            assertTrue(strings.contains("name=\""
                    + YandexVotButton.YandexCountdownButton.TIMER_SECONDS_RESOURCE + "\""));
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
