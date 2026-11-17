package io.github.ichillous.dsa.visuals;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that every {@code Class.method} the two folder maps name is public API, so a map
 * cannot keep naming a class or method after it is renamed.
 */
class VisualExamplesMapTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-map.properties");
    private static final List<String> PACKAGES = List.of("algorithms", "collections", "graphs", "structures");

    @Test
    void everyStructureCardNamesAPublicMethodOfAPublicClass() {
        assertEquals(9, assertCards("map.structures"));
    }

    @Test
    void everyAlgorithmCardNamesAPublicMethodOfAPublicClass() {
        assertEquals(9, assertCards("map.algorithms"));
    }

    @Test
    void aRenamedMethodOrClassIsDetected() {
        assertTrue(publicMethods(resolve("DynamicArray")).contains("get"));
        assertFalse(publicMethods(resolve("DynamicArray")).contains("fetch"));
        assertTrue(candidates("NoSuchStructure").isEmpty());
    }

    private static int assertCards(String prefix) {
        List<String> rows = DATA.strings(prefix + ".rows");
        for (String row : rows) {
            String api = DATA.text(prefix + "." + row + ".api").trim();
            int dot = api.indexOf('.');
            assertTrue(dot > 0 && dot == api.lastIndexOf('.'), api + " must be written Class.method");
            Class<?> type = resolve(api.substring(0, dot));
            assertTrue(publicMethods(type).contains(api.substring(dot + 1)),
                    api + ": " + type.getName() + " has no public method " + api.substring(dot + 1));
            assertFalse(DATA.text(prefix + "." + row + ".label").isBlank());
            assertFalse(DATA.text(prefix + "." + row + ".family").isBlank());
            assertFalse(DATA.strings(prefix + "." + row + ".folders").isEmpty());
        }
        return rows.size();
    }

    private static Class<?> resolve(String simpleName) {
        List<Class<?>> found = candidates(simpleName);
        assertEquals(1, found.size(), "exactly one public class named " + simpleName + " in " + PACKAGES);
        return found.get(0);
    }

    private static List<Class<?>> candidates(String simpleName) {
        List<Class<?>> found = new ArrayList<>();
        for (String name : PACKAGES) {
            try {
                Class<?> type = Class.forName("io.github.ichillous.dsa." + name + "." + simpleName);
                if (Modifier.isPublic(type.getModifiers())) {
                    found.add(type);
                }
            } catch (ClassNotFoundException absent) {
                // Not in this package; try the next one.
            }
        }
        return found;
    }

    private static List<String> publicMethods(Class<?> type) {
        return Arrays.stream(type.getMethods()).map(Method::getName).toList();
    }
}
