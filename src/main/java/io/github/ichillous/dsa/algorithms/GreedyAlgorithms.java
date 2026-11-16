package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/** Greedy algorithms: activity selection and Huffman code lengths with canonical codes. */
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

    /** A Huffman tree node: leaves are numbered by symbol, internal nodes after all symbols. */
    private record HuffmanNode(long weight, int id) {}

    /**
     * Returns the code length of every symbol in an optimal prefix code (Huffman's algorithm) for
     * the given frequencies. The result has the same length as {@code frequencies} and is a new
     * array; the input is not modified.
     *
     * <p>Symbols with frequency 0 are unused and get length 0. If exactly one symbol is used it
     * gets length 1 (a code needs at least one bit); if none is used every length is 0. Otherwise
     * the two lowest-weight nodes are repeatedly merged. Equal weights are ordered by node number:
     * leaves (numbered by symbol index) come before internal nodes, which are numbered in creation
     * order. This makes the lengths deterministic; other tie orders give different but equally
     * optimal lengths. Exchange argument: some optimal tree makes the two least frequent symbols
     * deepest siblings, and merging them leaves a smaller instance of the same problem.
     *
     * <p>Overflow policy: merged weights use checked addition. {@link ArithmeticException} is
     * thrown if the sum of all frequencies exceeds {@code Long.MAX_VALUE}. Time O(n log n), extra
     * space O(n). Lengths are at most the number of used symbols minus one.
     *
     * @param frequencies the symbol frequencies, each at least 0
     * @return the code length per symbol
     * @throws NullPointerException if {@code frequencies} is null
     * @throws IllegalArgumentException if a frequency is negative
     * @throws ArithmeticException if the total frequency overflows {@code long}
     */
    public static int[] huffmanCodeLengths(long[] frequencies) {
        Objects.requireNonNull(frequencies, "frequencies");
        int symbols = frequencies.length;
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>(
                Comparator.comparingLong(HuffmanNode::weight).thenComparingInt(HuffmanNode::id));
        int used = 0;
        int lastUsed = -1;
        for (int symbol = 0; symbol < symbols; symbol++) {
            if (frequencies[symbol] < 0) {
                throw new IllegalArgumentException("frequencies must be at least 0");
            }
            if (frequencies[symbol] > 0) {
                queue.add(new HuffmanNode(frequencies[symbol], symbol));
                used++;
                lastUsed = symbol;
            }
        }
        int[] lengths = new int[symbols];
        if (used == 1) {
            lengths[lastUsed] = 1;
            return lengths;
        }
        int[] parent = new int[symbols + Math.max(used - 1, 0)];
        int next = symbols;
        while (queue.size() > 1) {
            HuffmanNode first = queue.poll();
            HuffmanNode second = queue.poll();
            parent[first.id()] = next;
            parent[second.id()] = next;
            queue.add(new HuffmanNode(Math.addExact(first.weight(), second.weight()), next));
            next++;
        }
        // Internal nodes were created children-first, so walking them in reverse sets each
        // parent's depth before its children's.
        int[] depth = new int[parent.length];
        for (int node = next - 2; node >= symbols; node--) {
            depth[node] = depth[parent[node]] + 1;
        }
        for (int symbol = 0; symbol < symbols; symbol++) {
            if (frequencies[symbol] > 0) {
                lengths[symbol] = depth[parent[symbol]] + 1;
            }
        }
        return lengths;
    }

    /**
     * Assigns canonical prefix codes to the given code lengths. Symbols are ordered by length and
     * then by index; the first code of the shortest length is all zeros, each next code is the
     * previous one plus 1 as a binary number, and moving to a longer length appends zeros. A
     * length of 0 means "unused" and yields {@code ""}. The codes are returned as strings of
     * {@code '0'} and {@code '1'}, indexed like the input.
     *
     * <p>The lengths must satisfy the Kraft inequality, the sum of 2^-length over used symbols
     * at most 1, which is exactly the condition for a prefix code to exist; it is checked exactly
     * with integer arithmetic and violations throw {@link IllegalArgumentException}, as do negative
     * lengths. Incomplete codes (Kraft sum below 1) are accepted. The result is immutable and the
     * input is not modified. Time O(n log n + total code length), where the output size is the sum
     * of all lengths, so a caller who passes very large lengths can exhaust memory
     * ({@link OutOfMemoryError}).
     *
     * @param codeLengths the length of each symbol's code, at least 0
     * @return the canonical codes
     * @throws NullPointerException if {@code codeLengths} is null
     * @throws IllegalArgumentException if a length is negative or the Kraft sum exceeds 1
     */
    public static List<String> canonicalCodes(int[] codeLengths) {
        Objects.requireNonNull(codeLengths, "codeLengths");
        int used = 0;
        for (int length : codeLengths) {
            if (length < 0) {
                throw new IllegalArgumentException("code lengths must be at least 0");
            }
            if (length > 0) {
                used++;
            }
        }
        Integer[] order = new Integer[codeLengths.length];
        for (int symbol = 0; symbol < order.length; symbol++) {
            order[symbol] = symbol;
        }
        Arrays.sort(order, Comparator.<Integer>comparingInt(symbol -> codeLengths[symbol])
                .thenComparingInt(symbol -> symbol));
        // Kraft check over the sorted lengths: "open" counts free codewords at the current depth.
        // It is capped at the number of used symbols (it can then never go negative), which also
        // avoids overflow and needs no array indexed by length.
        long open = 1;
        int depth = 0;
        for (int symbol : order) {
            int length = codeLengths[symbol];
            if (length == 0) {
                continue;
            }
            int shift = length - depth;
            open = shift >= 32 ? used : Math.min(open << shift, used);
            open--;
            depth = length;
            if (open < 0) {
                throw new IllegalArgumentException("code lengths violate the Kraft inequality");
            }
        }
        String[] codes = new String[codeLengths.length];
        char[] current = new char[0];
        boolean first = true;
        for (int symbol : order) {
            int length = codeLengths[symbol];
            if (length == 0) {
                codes[symbol] = "";
                continue;
            }
            if (first) {
                current = new char[length];
                Arrays.fill(current, '0');
                first = false;
            } else {
                int position = current.length - 1;
                while (current[position] == '1') {
                    current[position--] = '0';
                }
                current[position] = '1';
                int oldLength = current.length;
                current = Arrays.copyOf(current, length);
                Arrays.fill(current, oldLength, length, '0');
            }
            codes[symbol] = new String(current);
        }
        return List.of(codes);
    }
}
