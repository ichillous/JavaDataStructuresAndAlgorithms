package io.github.ichillous.dsa.examples;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LearningLabTest {
    private record Result(int status, String output) {}

    private static Result run(String... arguments) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            int status = LearningLab.run(arguments, out);
            return new Result(status, bytes.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    void helpAndListExposeEveryRunnableLesson() {
        for (String[] command : new String[][]{{}, {"--help"}, {"learn", "list"}}) {
            Result result = run(command);
            assertEquals(0, result.status());
            for (String topic : new String[]{"binary-search", "negative-edges", "range-queries", "lru"}) {
                assertTrue(result.output().contains(topic));
            }
        }
    }

    @Test
    void searchTraceHandlesDuplicatesMissesAndIntegerLimits() {
        String[] targets = {"2", "0", "4", "6", "-2147483648", "2147483647"};
        int[] bounds = {1, 0, 4, 5, 0, 5};
        int[] matches = {1, -1, -1, -1, -1, -1};
        for (int i = 0; i < targets.length; i++) {
            Result result = run("learn", "binary-search", targets[i]);
            assertEquals(0, result.status());
            assertTrue(result.output().contains("Lower bound: " + bounds[i] + "; first match: " + matches[i]));
        }
        assertEquals(run("learn", "binary-search", "2").output(), run("learn", "binary-search").output());
    }

    @Test
    void substantiveLessonsShowHandCalculatedResults() {
        Result graph = run("learn", "negative-edges");
        assertEquals(0, graph.status());
        assertTrue(graph.output().contains("path: [0, 1, 2]; cost: -3"));
        assertTrue(graph.output().contains("Dijkstra rejects"));
        Result range = run("learn", "range-queries");
        assertEquals(0, range.status());
        assertTrue(range.output().contains("Before: sum=3, min=-1"));
        assertTrue(range.output().contains("index 1: sum=8, min=4"));
        Result lru = run("learn", "lru");
        assertEquals(0, lru.status());
        assertTrue(lru.output().contains("B evicted; B=null, size=2"));
    }

    @Test
    void invalidInputHasUsefulUsageAndNoStackTrace() {
        for (String[] command : new String[][]{{"wat"}, {"learn"}, {"learn", "missing"},
                {"learn", "binary-search", "x"}, {"learn", "binary-search", "2147483648"},
                {"learn", "binary-search", "1", "2"}, {"learn", "lru", "extra"}}) {
            Result result = run(command);
            assertEquals(2, result.status());
            assertTrue(result.output().startsWith("Usage error:"));
            assertTrue(result.output().contains("Usage:"));
        }
    }
}
