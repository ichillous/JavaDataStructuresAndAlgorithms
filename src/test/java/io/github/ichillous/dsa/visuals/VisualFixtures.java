package io.github.ichillous.dsa.visuals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reads one canonical visual fixture file from {@code docs/visuals/data}, the same file the
 * Python figure generator draws from. Values are comma-separated ASCII without escapes; an
 * empty value is an empty list. A missing key or a malformed number fails the calling test
 * with the key and file name. Instances are immutable after loading.
 */
final class VisualFixtures {
    private final String fileName;
    private final Properties values;

    private VisualFixtures(String fileName, Properties values) {
        this.fileName = fileName;
        this.values = values;
    }

    /** Loads {@code docs/visuals/data/<fileName>} relative to Maven's {@code basedir}. */
    static VisualFixtures load(String fileName) {
        Path path = Path.of(System.getProperty("basedir", "."), "docs", "visuals", "data", fileName);
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(path)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read visual fixtures " + path, exception);
        }
        return new VisualFixtures(fileName, properties);
    }

    /** The raw value of {@code key}. */
    String text(String key) {
        String value = values.getProperty(key);
        if (value == null) {
            fail("Missing visual fixture " + key + " in " + fileName);
        }
        return value;
    }

    /** The value of {@code key} as one {@code int}. */
    int number(String key) {
        try {
            return Integer.parseInt(text(key).trim());
        } catch (NumberFormatException exception) {
            return fail("Visual fixture " + key + " in " + fileName + " is not an int", exception);
        }
    }

    /** The comma-separated {@code int} values of {@code key}. */
    int[] ints(String key) {
        return strings(key).stream().mapToInt(part -> parse(key, part)).toArray();
    }

    /** The comma-separated {@code long} values of {@code key}. */
    long[] longs(String key) {
        try {
            return strings(key).stream().mapToLong(Long::parseLong).toArray();
        } catch (NumberFormatException exception) {
            return fail("Visual fixture " + key + " in " + fileName + " is not a list of longs", exception);
        }
    }

    /** The comma-separated {@code int} values of {@code key} as an unmodifiable list. */
    List<Integer> list(String key) {
        return Arrays.stream(ints(key)).boxed().toList();
    }

    /** The comma-separated, trimmed parts of {@code key} as an unmodifiable list. */
    List<String> strings(String key) {
        String value = text(key).trim();
        return value.isEmpty() ? List.of() : Arrays.stream(value.split(",")).map(String::trim).toList();
    }

    private int parse(String key, String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException exception) {
            return fail("Visual fixture " + key + " in " + fileName + " is not a list of ints", exception);
        }
    }
}
