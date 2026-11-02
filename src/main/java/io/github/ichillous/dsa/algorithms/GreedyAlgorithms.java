package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Greedy activity selection: maximize the number of compatible unweighted intervals. */
public final class GreedyAlgorithms {
    private GreedyAlgorithms() {}

    /** A nonempty half-open interval [start, end); intervals touching at a boundary are compatible. */
    public record Interval(int start, int end) {
        /** Rejects empty or backwards intervals. Integer extremes are valid endpoints. */
        public Interval {
            if (start >= end) {
                throw new IllegalArgumentException("an interval must have start < end");
            }
        }
    }

    /**
     * Returns an immutable maximum-cardinality schedule, ordered by end then start. The input
     * collection is not modified. Null input/elements are rejected. Earliest finishing activity
     * leaves at least as much remaining time as any alternative, giving the exchange argument.
     * O(n log n) time and O(n) extra space including the returned schedule.
     */
    public static List<Interval> selectActivities(Collection<Interval> intervals) {
        Objects.requireNonNull(intervals, "intervals");
        List<Interval> ordered = new ArrayList<>(intervals.size());
        for (Interval interval : intervals) {
            ordered.add(Objects.requireNonNull(interval, "interval"));
        }
        ordered.sort(Comparator.comparingInt(Interval::end).thenComparingInt(Interval::start));
        List<Interval> selected = new ArrayList<>();
        for (Interval interval : ordered) {
            if (selected.isEmpty() || interval.start() >= selected.get(selected.size() - 1).end()) {
                selected.add(interval);
            }
        }
        return List.copyOf(selected);
    }
}
