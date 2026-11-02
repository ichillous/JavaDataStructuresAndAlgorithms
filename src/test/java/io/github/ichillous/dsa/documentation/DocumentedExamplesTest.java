package io.github.ichillous.dsa.documentation;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Compiles and executes every fenced Java example, rather than copying it into a test. */
class DocumentedExamplesTest {
    @TempDir
    Path temporary;

    private record Snippet(String location, String body) {}

    @TestFactory
    Stream<DynamicTest> allJavaFencesAreExecutableContracts() throws Exception {
        Path root = Path.of(System.getProperty("basedir", ".")).toAbsolutePath().normalize();
        List<Snippet> snippets = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md"))
                    .filter(path -> !root.relativize(path).startsWith("target"))
                    .filter(path -> !root.relativize(path).startsWith(".git"))
                    .filter(path -> !root.relativize(path).startsWith(".idea"))
                    .sorted().toList()) {
                collect(root.relativize(file).toString(), Files.readAllLines(file), snippets);
            }
        }
        assertFalse(snippets.isEmpty(), "No Java lesson examples discovered");
        AtomicInteger number = new AtomicInteger();
        return snippets.stream().map(snippet -> DynamicTest.dynamicTest(snippet.location(), () -> {
            assertTrue(snippet.body().matches("(?s).*\\bassert\\s+.*"),
                    "Java examples must assert an expected result: " + snippet.location());
            compileAndRun(snippet, "LessonExample" + number.getAndIncrement());
        }));
    }

    @Test
    void generatedExamplesReallyExecuteWithAssertionsEnabled() {
        AssertionError error = assertThrows(AssertionError.class,
                () -> compileAndRun(new Snippet("assertion probe", "assert false : \"probe ran\";"),
                        "AssertionProbe"));
        assertEquals("probe ran", error.getMessage());
    }

    private static void collect(String name, List<String> lines, List<Snippet> snippets) {
        String delimiter = null;
        boolean javaSnippet = false;
        int start = 0;
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (delimiter == null) {
                if (line.startsWith("```") || line.startsWith("~~~")) {
                    char marker = line.charAt(0);
                    int length = 0;
                    while (length < line.length() && line.charAt(length) == marker) {
                        length++;
                    }
                    delimiter = line.substring(0, length);
                    String language = line.substring(length).strip();
                    if (!language.isEmpty() && language.split("\\s+", 2)[0].equalsIgnoreCase("java")) {
                        assertEquals("java", language, "Use a plain java fence in " + name + ":" + (i + 1));
                    }
                    javaSnippet = language.equals("java");
                    start = i + 1;
                    body.setLength(0);
                }
            } else if (line.matches(java.util.regex.Pattern.quote(delimiter.charAt(0) + "")
                    + "{" + delimiter.length() + ",}")) {
                if (javaSnippet) {
                    snippets.add(new Snippet(name + ":" + (start + 1), body.toString()));
                }
                delimiter = null;
            } else if (javaSnippet) {
                body.append(lines.get(i)).append('\n');
            }
        }
        assertNull(delimiter, "Unclosed code fence in " + name + ":" + start);
    }

    private void compileAndRun(Snippet snippet, String className) throws Throwable {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "A full JDK 17+ is required to compile documentation examples");
        StringBuilder imports = new StringBuilder();
        StringBuilder statements = new StringBuilder();
        boolean started = false;
        for (String line : snippet.body().split("\\R")) {
            String trimmed = line.strip();
            if (!started && trimmed.matches("import\\s+(static\\s+)?[\\w.*]+\\s*;")) {
                imports.append(line).append('\n');
            } else {
                if (!trimmed.isEmpty() && !trimmed.startsWith("//")) {
                    started = true;
                }
                statements.append(line).append('\n');
            }
        }
        Path output = Files.createDirectory(temporary.resolve(className));
        Path source = output.resolve(className + ".java");
        Files.writeString(source, imports + "\npublic final class " + className
                + " { public static void main(String[] args) throws Exception {\n"
                + statements + "\n} }\n", StandardCharsets.UTF_8);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StringWriter messages = new StringWriter();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(
                diagnostics, null, StandardCharsets.UTF_8)) {
            List<String> options = List.of("--release", "17", "-Xlint:all", "-Werror",
                    "-classpath", System.getProperty("java.class.path"), "-d", output.toString());
            boolean passed = compiler.getTask(messages, manager, diagnostics, options, null,
                    manager.getJavaFileObjects(source)).call();
            assertTrue(passed, () -> snippet.location() + " did not compile:\n"
                    + diagnostics.getDiagnostics() + "\n" + messages);
        }
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{output.toUri().toURL()},
                getClass().getClassLoader())) {
            loader.setDefaultAssertionStatus(true);
            try {
                loader.loadClass(className).getMethod("main", String[].class)
                        .invoke(null, (Object) new String[0]);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }
    }
}
