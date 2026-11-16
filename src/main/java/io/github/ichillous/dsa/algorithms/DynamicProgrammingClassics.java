package io.github.ichillous.dsa.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Classic dynamic programs: coin combinations, word break, longest palindromic subsequence,
 * matrix-chain order, Held-Karp travelling salesman and weighted interval scheduling.
 *
 * <p>All methods are static and stateless. Inputs are never modified, null arguments and null
 * elements throw {@link NullPointerException}, and returned collections are immutable. Every
 * method documents its overflow policy: results that would not fit the stated type throw
 * {@link ArithmeticException} rather than wrap.
 */
public final class DynamicProgrammingClassics {
    /** Largest city count accepted by {@link #travelingSalesman(long[][])}. */
    private static final int MAX_TSP_CITIES = 16;

    /** Largest absolute distance accepted by {@link #travelingSalesman(long[][])}: 2^47. */
    private static final long MAX_TSP_DISTANCE = 1L << 47;

    private DynamicProgrammingClassics() {}

    /**
     * Counts the unordered combinations of coins that sum to {@code amount}: {@code [1, 2]} and
     * {@code [2, 1]} are the same combination. Duplicate denominations collapse into one. Amount 0
     * has exactly one combination (the empty one); the empty coin array only reaches amount 0.
     *
     * <p>State {@code ways[a]} is the number of combinations of the coins processed so far that sum
     * to {@code a}; each coin in turn updates {@code ways[a] += ways[a - coin]} for ascending
     * {@code a}, which allows unlimited reuse while counting each multiset once.
     *
     * <p>Overflow policy: every table entry is a checked {@code long}. If any entry for an amount
     * in {@code [0, amount]} exceeds {@code Long.MAX_VALUE}, {@link ArithmeticException} is thrown,
     * even when the requested amount itself would have been smaller (for example, an unreachable
     * amount). Time is O(d * amount) for d distinct coins and space O(amount). The input array is
     * not modified.
     *
     * @param coins positive denominations; duplicates allowed
     * @param amount target sum, at least 0
     * @return the number of combinations
     * @throws NullPointerException if {@code coins} is null
     * @throws IllegalArgumentException if a coin is not positive or {@code amount} is negative
     * @throws ArithmeticException if a count overflows {@code long}
     */
    public static long countCoinCombinations(int[] coins, int amount) {
        Objects.requireNonNull(coins, "coins");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be at least 0");
        }
        for (int coin : coins) {
            if (coin <= 0) {
                throw new IllegalArgumentException("coins must be positive");
            }
        }
        long[] ways = new long[amount + 1];
        ways[0] = 1;
        for (int coin : Arrays.stream(coins).distinct().toArray()) {
            for (int total = coin; total <= amount; total++) {
                ways[total] = Math.addExact(ways[total], ways[total - coin]);
            }
        }
        return ways[amount];
    }

    /**
     * Splits {@code text} into dictionary words, or reports that no split exists. Words are matched
     * by exact UTF-16 string equality and may be reused. The empty text splits into the empty list.
     *
     * <p>When several segmentations exist the answer is the one whose first word is shortest,
     * breaking ties recursively on the remainder: equivalently, the segmentation with the
     * lexicographically smallest sequence of word lengths. For {@code "catsanddog"} with
     * {@code {cat, cats, and, sand, dog}} the answer is {@code [cat, sand, dog]}.
     *
     * <p>State {@code breakable[i]} says whether the suffix starting at {@code i} can be split;
     * it is computed right to left, then the answer is read left to right choosing the shortest
     * word that leads to a breakable suffix. Time is O(n * L) substring lookups, i.e.
     * O(n * L^2) character work, where {@code L} is the longest dictionary word, and extra space is
     * O(n + dictionary size). Neither input is modified; the dictionary may be any collection and
     * duplicates are ignored.
     *
     * @param text the text to split
     * @param dictionary the allowed words; no null or empty words
     * @return the chosen words, or {@link Optional#empty()} if no segmentation exists
     * @throws NullPointerException if an argument or dictionary element is null
     * @throws IllegalArgumentException if the dictionary contains an empty word
     */
    public static Optional<List<String>> wordBreak(String text, Collection<String> dictionary) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(dictionary, "dictionary");
        Set<String> words = new HashSet<>();
        int longest = 0;
        for (String word : dictionary) {
            Objects.requireNonNull(word, "word");
            if (word.isEmpty()) {
                throw new IllegalArgumentException("dictionary words must not be empty");
            }
            words.add(word);
            longest = Math.max(longest, word.length());
        }
        int length = text.length();
        boolean[] breakable = new boolean[length + 1];
        breakable[length] = true;
        for (int start = length - 1; start >= 0; start--) {
            int limit = (int) Math.min((long) length, (long) start + longest);
            for (int end = start + 1; end <= limit && !breakable[start]; end++) {
                breakable[start] = breakable[end] && words.contains(text.substring(start, end));
            }
        }
        if (!breakable[0]) {
            return Optional.empty();
        }
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < length) {
            int end = start + 1;
            while (!(breakable[end] && words.contains(text.substring(start, end)))) {
                end++;
            }
            result.add(text.substring(start, end));
            start = end;
        }
        return Optional.of(List.copyOf(result));
    }

    /**
     * Returns the length of the longest palindromic subsequence of {@code text}, measured in UTF-16
     * code units (a surrogate pair counts as two). The empty text gives 0.
     *
     * <p>State {@code lps(i, j)} is the answer for {@code text[i..j]}. If the end characters match
     * it is {@code lps(i+1, j-1) + 2}, otherwise the larger of {@code lps(i+1, j)} and
     * {@code lps(i, j-1)}. Rows are computed from the bottom, keeping only two of them. Time is
     * O(n^2), auxiliary space O(n). The input is not modified.
     *
     * @param text the text
     * @return the length of the longest palindromic subsequence
     * @throws NullPointerException if {@code text} is null
     */
    public static int longestPalindromicSubsequenceLength(String text) {
        Objects.requireNonNull(text, "text");
        int length = text.length();
        int[] below = new int[length];
        int[] current = new int[length];
        for (int i = length - 1; i >= 0; i--) {
            current[i] = 1;
            for (int j = i + 1; j < length; j++) {
                if (text.charAt(i) == text.charAt(j)) {
                    current[j] = (j - 1 >= i + 1 ? below[j - 1] : 0) + 2;
                } else {
                    current[j] = Math.max(below[j], current[j - 1]);
                }
            }
            int[] swap = below;
            below = current;
            current = swap;
        }
        return length == 0 ? 0 : below[length - 1];
    }

    /**
     * The optimal multiplication cost and its parenthesization.
     *
     * @param cost the number of scalar multiplications
     * @param parenthesization the product, such as {@code "((A1A2)A3)"}
     */
    public record ChainOrder(long cost, String parenthesization) {
        /** Rejects a null parenthesization. */
        public ChainOrder {
            Objects.requireNonNull(parenthesization, "parenthesization");
        }
    }

    /**
     * Finds the cheapest order to multiply a chain of matrices. Matrix {@code Ai} (1-based) has
     * {@code dimensions[i-1]} rows and {@code dimensions[i]} columns, so {@code n + 1} dimensions
     * describe {@code n} matrices. Multiplying a p x q matrix by a q x r matrix costs p*q*r.
     *
     * <p>The parenthesization is written with matrix names {@code A1..An} and one pair of
     * parentheses per multiplication, for example {@code "((A1A2)A3)"}; a single matrix is
     * {@code "A1"}. State {@code best(i, j)} is the cheapest cost of {@code Ai..Aj}; the transition
     * tries every last split {@code k} in {@code [i, j)}, ordered by increasing chain length. Equal
     * costs go to the smallest split {@code k}, so the answer is deterministic. The string is
     * built iteratively, so deep chains cannot overflow the call stack.
     *
     * <p>Overflow policy: every product and sum is checked; {@link ArithmeticException} is thrown
     * if any candidate cost exceeds {@code Long.MAX_VALUE}, including candidates that would not
     * have been optimal. Time O(n^3), space O(n^2). The input is not modified.
     *
     * @param dimensions at least two positive dimensions
     * @return the optimal cost and parenthesization
     * @throws NullPointerException if {@code dimensions} is null
     * @throws IllegalArgumentException if fewer than two dimensions or a dimension is not positive
     * @throws ArithmeticException if a cost overflows {@code long}
     */
    public static ChainOrder matrixChainOrder(int[] dimensions) {
        Objects.requireNonNull(dimensions, "dimensions");
        if (dimensions.length < 2) {
            throw new IllegalArgumentException("at least two dimensions are required");
        }
        for (int dimension : dimensions) {
            if (dimension <= 0) {
                throw new IllegalArgumentException("dimensions must be positive");
            }
        }
        int matrices = dimensions.length - 1;
        long[][] cost = new long[matrices + 1][matrices + 1];
        int[][] split = new int[matrices + 1][matrices + 1];
        for (int chain = 2; chain <= matrices; chain++) {
            for (int i = 1; i + chain - 1 <= matrices; i++) {
                int j = i + chain - 1;
                long best = Long.MAX_VALUE;
                int bestSplit = i;
                boolean found = false;
                for (int k = i; k < j; k++) {
                    long multiply = Math.multiplyExact(
                            Math.multiplyExact((long) dimensions[i - 1], dimensions[k]),
                            (long) dimensions[j]);
                    long candidate =
                            Math.addExact(Math.addExact(cost[i][k], cost[k + 1][j]), multiply);
                    if (!found || candidate < best) {
                        best = candidate;
                        bestSplit = k;
                        found = true;
                    }
                }
                cost[i][j] = best;
                split[i][j] = bestSplit;
            }
        }
        StringBuilder text = new StringBuilder();
        Deque<Object> work = new ArrayDeque<>();
        work.push(new int[] {1, matrices});
        while (!work.isEmpty()) {
            Object item = work.pop();
            if (item instanceof String literal) {
                text.append(literal);
                continue;
            }
            int[] range = (int[]) item;
            if (range[0] == range[1]) {
                text.append('A').append(range[0]);
            } else {
                int k = split[range[0]][range[1]];
                text.append('(');
                work.push(")");
                work.push(new int[] {k + 1, range[1]});
                work.push(new int[] {range[0], k});
            }
        }
        return new ChainOrder(cost[1][matrices], text.toString());
    }

    /**
     * A closed tour of cities.
     *
     * @param order the visiting order, starting at city 0, with no repeated city
     * @param cost the total distance including the edge back to city 0
     */
    public record Tour(List<Integer> order, long cost) {
        /** Copies the order into an immutable list. */
        public Tour {
            order = List.copyOf(order);
        }
    }

    /**
     * Solves the travelling salesman problem exactly with the Held-Karp subset DP. The tour
     * starts at city 0, visits every other city once and returns to 0; the matrix may be
     * asymmetric and may contain negative entries. Diagonal entries are validated but unused.
     *
     * <p>State {@code best(S, j)} is the cheapest path from city 0 through exactly the set
     * {@code S} of other cities ending at {@code j in S}; the transition extends from every
     * {@code i in S \ {j}}, and the answer closes the cycle over every final city. Sets are
     * processed in increasing bitmask order, so every subset precedes its supersets. Equal costs
     * go to the smallest predecessor city, and equal closing costs to the smallest last city.
     *
     * <p>Limits: {@code 1 <= n <= 16} and every entry in {@code [-2^47, 2^47]}, so a tour sum of at
     * most 16 edges has magnitude at most 2^51 and cannot overflow {@code long}. {@code n = 1}
     * gives {@code ([0], 0)}. Time O(2^n * n^2); space is one {@code long} table of
     * 2^(n-1) * (n-1) entries (about 4 MB at n = 16) plus a byte table of the same size. The input
     * is not modified.
     *
     * @param distances a square matrix where {@code distances[i][j]} is the cost of going from
     *     city {@code i} to city {@code j}
     * @return the optimal tour
     * @throws NullPointerException if the matrix or a row is null
     * @throws IllegalArgumentException if the matrix is not square, is empty, has more than 16
     *     cities, or has an entry outside [-2^47, 2^47]
     */
    public static Tour travelingSalesman(long[][] distances) {
        Objects.requireNonNull(distances, "distances");
        int cities = distances.length;
        if (cities < 1 || cities > MAX_TSP_CITIES) {
            throw new IllegalArgumentException("city count must be between 1 and 16");
        }
        for (long[] row : distances) {
            Objects.requireNonNull(row, "row");
            if (row.length != cities) {
                throw new IllegalArgumentException("distance matrix must be square");
            }
            for (long value : row) {
                if (value < -MAX_TSP_DISTANCE || value > MAX_TSP_DISTANCE) {
                    throw new IllegalArgumentException("distances must lie in [-2^47, 2^47]");
                }
            }
        }
        if (cities == 1) {
            return new Tour(List.of(0), 0);
        }
        int others = cities - 1;
        int subsets = 1 << others;
        long[] best = new long[subsets * others];
        byte[] previous = new byte[subsets * others];
        Arrays.fill(best, Long.MAX_VALUE);
        for (int j = 0; j < others; j++) {
            best[(1 << j) * others + j] = distances[0][j + 1];
            previous[(1 << j) * others + j] = -1;
        }
        for (int mask = 1; mask < subsets; mask++) {
            for (int j = 0; j < others; j++) {
                int rest = mask ^ (1 << j);
                if ((mask & (1 << j)) == 0 || rest == 0) {
                    continue;
                }
                long min = Long.MAX_VALUE;
                int argMin = -1;
                for (int i = 0; i < others; i++) {
                    if ((rest & (1 << i)) == 0) {
                        continue;
                    }
                    long candidate = best[rest * others + i] + distances[i + 1][j + 1];
                    if (candidate < min) {
                        min = candidate;
                        argMin = i;
                    }
                }
                best[mask * others + j] = min;
                previous[mask * others + j] = (byte) argMin;
            }
        }
        int full = subsets - 1;
        long total = Long.MAX_VALUE;
        int last = -1;
        for (int j = 0; j < others; j++) {
            long candidate = best[full * others + j] + distances[j + 1][0];
            if (candidate < total) {
                total = candidate;
                last = j;
            }
        }
        List<Integer> order = new ArrayList<>(cities);
        int mask = full;
        int city = last;
        while (city >= 0) {
            order.add(city + 1);
            int before = previous[mask * others + city];
            mask ^= 1 << city;
            city = before;
        }
        order.add(0);
        Collections.reverse(order);
        return new Tour(order, total);
    }

    /**
     * A half-open interval {@code [start, end)} with a non-negative weight.
     *
     * @param start inclusive start
     * @param end exclusive end, greater than {@code start}
     * @param weight the value of choosing the interval, at least 0
     */
    public record WeightedInterval(int start, int end, long weight) {
        /** Rejects empty or backwards intervals and negative weights. */
        public WeightedInterval {
            if (start >= end) {
                throw new IllegalArgumentException("an interval must have start < end");
            }
            if (weight < 0) {
                throw new IllegalArgumentException("weight must be at least 0");
            }
        }
    }

    /**
     * A chosen set of pairwise compatible intervals.
     *
     * @param chosen the intervals, sorted by end (then start, then weight)
     * @param totalWeight the sum of their weights
     */
    public record Schedule(List<WeightedInterval> chosen, long totalWeight) {
        /** Copies the chosen intervals into an immutable list. */
        public Schedule {
            chosen = List.copyOf(chosen);
        }
    }

    /**
     * Chooses pairwise compatible intervals of maximum total weight. Intervals are half-open, so
     * one ending where another starts is compatible. Duplicates are separate intervals; at most
     * one of identical overlapping copies can be chosen.
     *
     * <p>Sort by end (then start, then weight, so the order is deterministic). State
     * {@code best[i]} is the maximum weight using the first {@code i} intervals; the transition is
     * {@code max(best[i-1], weight_i + best[p(i)])}, where {@code p(i)} counts the sorted
     * intervals ending no later than interval {@code i} starts (found by binary search).
     * Interval {@code i} is taken only when that is strictly better, so when totals tie the
     * interval that sorts later is skipped, and zero-weight intervals are never chosen. The result is sorted by
     * end.
     *
     * <p>Overflow policy: sums are checked; {@link ArithmeticException} is thrown if a schedule's
     * total would exceed {@code Long.MAX_VALUE}. Time O(n log n), space O(n). The input collection
     * is not modified.
     *
     * @param intervals the candidate intervals; none null
     * @return the optimal schedule; empty for no input
     * @throws NullPointerException if the collection or an element is null
     * @throws ArithmeticException if the total weight overflows {@code long}
     */
    public static Schedule weightedIntervalScheduling(Collection<WeightedInterval> intervals) {
        Objects.requireNonNull(intervals, "intervals");
        List<WeightedInterval> sorted = new ArrayList<>(intervals.size());
        for (WeightedInterval interval : intervals) {
            sorted.add(Objects.requireNonNull(interval, "interval"));
        }
        sorted.sort(Comparator.comparingInt(WeightedInterval::end)
                .thenComparingInt(WeightedInterval::start)
                .thenComparingLong(WeightedInterval::weight));
        int count = sorted.size();
        int[] compatible = new int[count + 1];
        long[] best = new long[count + 1];
        for (int i = 1; i <= count; i++) {
            WeightedInterval interval = sorted.get(i - 1);
            int low = 0;
            int high = i - 1;
            while (low < high) {
                int middle = (low + high) >>> 1;
                if (sorted.get(middle).end() <= interval.start()) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            compatible[i] = low;
            long taken = Math.addExact(interval.weight(), best[low]);
            best[i] = Math.max(best[i - 1], taken);
        }
        List<WeightedInterval> chosen = new ArrayList<>();
        int i = count;
        while (i > 0) {
            if (best[i] == best[i - 1]) {
                i--;
            } else {
                chosen.add(sorted.get(i - 1));
                i = compatible[i];
            }
        }
        Collections.reverse(chosen);
        return new Schedule(chosen, best[count]);
    }
}
