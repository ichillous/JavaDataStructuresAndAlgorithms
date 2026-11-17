package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.BacktrackingPuzzles;
import io.github.ichillous.dsa.algorithms.DynamicProgramming;
import io.github.ichillous.dsa.algorithms.DynamicProgramming.EditStep;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.ChainOrder;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.Schedule;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.Tour;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.WeightedInterval;
import io.github.ichillous.dsa.algorithms.GreedyAlgorithms;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every value the optimization figures display. The dynamic-programming tables are
 * private to the library, so each test asserts the public result (string, script, cost, tour,
 * schedule, code lengths, indices) and re-derives the displayed table cells with plain code
 * written here, independently of the library.
 */
class VisualExamplesOptimizationTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-optimization.properties");

    /** Parses "a:b=value" check entries into a map keyed by "a:b". */
    private static Map<String, Integer> cells(String key) {
        Map<String, Integer> result = new HashMap<>();
        for (String entry : DATA.strings(key)) {
            String[] parts = entry.split("=");
            result.put(parts[0], Integer.parseInt(parts[1]));
        }
        return result;
    }

    // ---- lcs-traceback ----

    private static int[][] lcsTable(String a, String b) {
        int[][] table = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                table[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? table[i - 1][j - 1] + 1
                        : Math.max(table[i - 1][j], table[i][j - 1]);
            }
        }
        return table;
    }

    @Test
    void lcsReturnsTheFixtureStringAndLength() {
        String a = DATA.text("optimization.lcs.a");
        String b = DATA.text("optimization.lcs.b");
        assertEquals(DATA.text("optimization.lcs.result"), DynamicProgramming.longestCommonSubsequence(a, b));
        assertEquals(DATA.number("optimization.lcs.length"),
                DynamicProgramming.longestCommonSubsequenceLength(a, b));
    }

    @Test
    void lcsTableCellsAndTheTracebackMatchTheDocumentedTieRule() {
        String a = DATA.text("optimization.lcs.a");
        String b = DATA.text("optimization.lcs.b");
        int[][] table = lcsTable(a, b);
        for (Map.Entry<String, Integer> cell : cells("optimization.lcs.cells").entrySet()) {
            String[] at = cell.getKey().split(":");
            assertEquals(cell.getValue(), table[Integer.parseInt(at[0])][Integer.parseInt(at[1])], cell.getKey());
        }
        // The documented rule: take equal units; otherwise up when dp[i-1][j] >= dp[i][j-1].
        List<String> moves = new ArrayList<>();
        StringBuilder taken = new StringBuilder();
        int i = a.length();
        int j = b.length();
        assertEquals(DATA.number("optimization.lcs.cornerUp"), table[i - 1][j]);
        assertEquals(DATA.number("optimization.lcs.cornerLeft"), table[i][j - 1]);
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                taken.append(a.charAt(i - 1));
                moves.add("take");
                i--;
                j--;
            } else if (table[i - 1][j] >= table[i][j - 1]) {
                moves.add("up");
                i--;
            } else {
                moves.add("left");
                j--;
            }
        }
        assertEquals(DATA.strings("optimization.lcs.path"), moves);
        assertEquals(DynamicProgramming.longestCommonSubsequence(a, b), taken.reverse().toString());
    }

    @Test
    void anotherLongestCommonSubsequenceExistsSoTheTieRuleMatters() {
        String alternative = DATA.text("optimization.lcs.alternative");
        assertEquals(DATA.number("optimization.lcs.length"), alternative.length());
        assertTrue(isSubsequence(alternative, DATA.text("optimization.lcs.a")));
        assertTrue(isSubsequence(alternative, DATA.text("optimization.lcs.b")));
    }

    private static boolean isSubsequence(String small, String large) {
        int next = 0;
        for (int i = 0; i < large.length() && next < small.length(); i++) {
            if (large.charAt(i) == small.charAt(next)) {
                next++;
            }
        }
        return next == small.length();
    }

    // ---- edit-distance-grid ----

    @Test
    void editScriptMatchesTheFixtureStepByStep() {
        String source = DATA.text("optimization.edit.source");
        String target = DATA.text("optimization.edit.target");
        assertEquals(DATA.number("optimization.edit.distance"), DynamicProgramming.editDistance(source, target));
        List<String> shown = new ArrayList<>();
        for (EditStep step : DynamicProgramming.editScript(source, target)) {
            shown.add(step.operation() + ":" + step.sourceIndex() + ":" + step.targetIndex());
        }
        assertEquals(DATA.strings("optimization.edit.steps"), shown);
    }

    @Test
    void editTableCellsMatchTheRecurrence() {
        String source = DATA.text("optimization.edit.source");
        String target = DATA.text("optimization.edit.target");
        int[][] table = new int[source.length() + 1][target.length() + 1];
        for (int i = 0; i <= source.length(); i++) {
            table[i][0] = i;
        }
        for (int j = 0; j <= target.length(); j++) {
            table[0][j] = j;
        }
        for (int i = 1; i <= source.length(); i++) {
            for (int j = 1; j <= target.length(); j++) {
                int diagonal = table[i - 1][j - 1] + (source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1);
                table[i][j] = Math.min(diagonal, Math.min(table[i - 1][j], table[i][j - 1]) + 1);
            }
        }
        for (Map.Entry<String, Integer> cell : cells("optimization.edit.cells").entrySet()) {
            String[] at = cell.getKey().split(":");
            assertEquals(cell.getValue(), table[Integer.parseInt(at[0])][Integer.parseInt(at[1])], cell.getKey());
        }
    }

    // ---- matrix-chain-splits ----

    @Test
    void matrixChainReturnsTheFixtureCostAndParenthesization() {
        ChainOrder order = DynamicProgrammingClassics.matrixChainOrder(DATA.ints("optimization.chain.dimensions"));
        assertEquals(DATA.number("optimization.chain.cost"), order.cost());
        assertEquals(DATA.text("optimization.chain.parenthesization"), order.parenthesization());
    }

    @Test
    void matrixChainCostSplitAndCandidateCellsMatchTheRecurrence() {
        int[] d = DATA.ints("optimization.chain.dimensions");
        int n = d.length - 1;
        long[][] cost = new long[n + 2][n + 2];
        int[][] split = new int[n + 2][n + 2];
        for (int length = 2; length <= n; length++) {
            for (int i = 1; i + length - 1 <= n; i++) {
                int j = i + length - 1;
                cost[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long candidate = cost[i][k] + cost[k + 1][j] + (long) d[i - 1] * d[k] * d[j];
                    if (candidate < cost[i][j]) { // strict: equal costs keep the smallest k
                        cost[i][j] = candidate;
                        split[i][j] = k;
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> cell : cells("optimization.chain.costs").entrySet()) {
            String[] at = cell.getKey().split(":");
            assertEquals((long) cell.getValue(), cost[Integer.parseInt(at[0])][Integer.parseInt(at[1])], cell.getKey());
        }
        for (Map.Entry<String, Integer> cell : cells("optimization.chain.splits").entrySet()) {
            String[] at = cell.getKey().split(":");
            assertEquals(cell.getValue(), split[Integer.parseInt(at[0])][Integer.parseInt(at[1])], cell.getKey());
        }
        for (Map.Entry<String, Integer> candidate : cells("optimization.chain.candidates").entrySet()) {
            int k = Integer.parseInt(candidate.getKey());
            assertEquals((long) candidate.getValue(), cost[1][k] + cost[k + 1][n] + (long) d[0] * d[k] * d[n], "k=" + k);
        }
    }

    // ---- held-karp-masks ----

    private static long[][] distances() {
        String[] rows = DATA.text("optimization.tsp.distances").split(";");
        long[][] result = new long[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            result[i] = Arrays.stream(rows[i].split(",")).mapToLong(Long::parseLong).toArray();
        }
        return result;
    }

    @Test
    void travelingSalesmanReturnsTheFixtureTourAndCost() {
        Tour tour = DynamicProgrammingClassics.travelingSalesman(distances());
        assertEquals(DATA.list("optimization.tsp.tour"), tour.order());
        assertEquals(DATA.number("optimization.tsp.cost"), tour.cost());
        // The reverse direction ties on this symmetric matrix; the tie rule picked the other tour.
        List<Integer> reverse = DATA.list("optimization.tsp.alternative");
        long total = 0;
        long[][] d = distances();
        for (int i = 0; i < reverse.size(); i++) {
            total += d[reverse.get(i)][reverse.get((i + 1) % reverse.size())];
        }
        assertEquals(tour.cost(), total);
    }

    @Test
    void heldKarpStatesMatchTheRecurrence() {
        long[][] d = distances();
        int others = d.length - 1;
        long[][] best = new long[1 << others][others];
        for (long[] row : best) {
            Arrays.fill(row, Long.MAX_VALUE);
        }
        for (int set = 1; set < (1 << others); set++) {
            for (int last = 0; last < others; last++) {
                if ((set & (1 << last)) == 0) {
                    continue;
                }
                int rest = set & ~(1 << last);
                if (rest == 0) {
                    best[set][last] = d[0][last + 1];
                    continue;
                }
                for (int previous = 0; previous < others; previous++) {
                    if ((rest & (1 << previous)) != 0) {
                        best[set][last] = Math.min(best[set][last], best[rest][previous] + d[previous + 1][last + 1]);
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> state : cells("optimization.tsp.states").entrySet()) {
            String[] at = state.getKey().split("@");
            int set = 0;
            for (char city : at[0].toCharArray()) {
                set |= 1 << (city - '1');
            }
            int last = Integer.parseInt(at[1]) - 1;
            assertEquals((long) state.getValue(), best[set][last], state.getKey());
        }
        List<Integer> closing = DATA.list("optimization.tsp.closing");
        for (int last = 0; last < others; last++) {
            assertEquals((long) closing.get(last), best[(1 << others) - 1][last] + d[last + 1][0], "end " + (last + 1));
        }
    }

    // ---- weighted-interval-choice ----

    private static List<WeightedInterval> intervals() {
        List<WeightedInterval> result = new ArrayList<>();
        for (String entry : DATA.strings("optimization.interval.intervals")) {
            String[] parts = entry.split(":");
            result.add(new WeightedInterval(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Long.parseLong(parts[2])));
        }
        return result;
    }

    @Test
    void weightedIntervalSchedulingChoosesTheFixtureIntervals() {
        List<WeightedInterval> sorted = intervals();
        Schedule schedule = DynamicProgrammingClassics.weightedIntervalScheduling(sorted);
        List<WeightedInterval> chosen = new ArrayList<>();
        for (int index : DATA.ints("optimization.interval.chosen")) {
            chosen.add(sorted.get(index));
        }
        assertEquals(chosen, schedule.chosen());
        assertEquals(DATA.number("optimization.interval.total"), schedule.totalWeight());
    }

    @Test
    void takeSkipAndLastCompatibleValuesMatchTheRecurrence() {
        List<WeightedInterval> sorted = intervals();
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i - 1).end() <= sorted.get(i).end(), "the fixture lists intervals by end");
        }
        int count = sorted.size();
        long[] best = new long[count + 1];
        List<Integer> compatible = new ArrayList<>();
        List<Long> take = new ArrayList<>();
        List<Long> skip = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            WeightedInterval interval = sorted.get(i - 1);
            int p = 0;
            while (p < i - 1 && sorted.get(p).end() <= interval.start()) {
                p++;
            }
            compatible.add(p);
            take.add(interval.weight() + best[p]);
            skip.add(best[i - 1]);
            best[i] = Math.max(best[i - 1], interval.weight() + best[p]);
        }
        assertEquals(DATA.list("optimization.interval.compatible"), compatible);
        assertEquals(Arrays.stream(DATA.longs("optimization.interval.take")).boxed().toList(), take);
        assertEquals(Arrays.stream(DATA.longs("optimization.interval.skip")).boxed().toList(), skip);
        assertEquals(Arrays.stream(DATA.longs("optimization.interval.best")).boxed().toList(),
                Arrays.stream(best, 1, count + 1).boxed().toList());
    }

    // ---- huffman-tree ----

    @Test
    void huffmanLengthsMatchTheFixtureAndTheWeightedCost() {
        long[] frequencies = DATA.longs("optimization.huffman.frequencies");
        int[] lengths = GreedyAlgorithms.huffmanCodeLengths(frequencies);
        assertEquals(DATA.list("optimization.huffman.lengths"), Arrays.stream(lengths).boxed().toList());
        long cost = 0;
        for (int i = 0; i < lengths.length; i++) {
            cost += frequencies[i] * lengths[i];
        }
        assertEquals(DATA.number("optimization.huffman.cost"), cost);
    }

    @Test
    void huffmanMergesAreTheTwoLightestNodesEachTime() {
        PriorityQueue<Long> queue = new PriorityQueue<>();
        for (long frequency : DATA.longs("optimization.huffman.frequencies")) {
            queue.add(frequency);
        }
        List<String> merges = new ArrayList<>();
        long mergedTotal = 0;
        while (queue.size() > 1) {
            long first = queue.remove();
            long second = queue.remove();
            merges.add(first + "+" + second + "=" + (first + second));
            mergedTotal += first + second;
            queue.add(first + second);
        }
        assertEquals(DATA.strings("optimization.huffman.merges"), merges);
        // Every merge adds its weight to the weighted code length.
        assertEquals(DATA.number("optimization.huffman.cost"), mergedTotal);
    }

    @Test
    void huffmanTreeChildrenFollowTheDocumentedTieRule() {
        long[] frequencies = DATA.longs("optimization.huffman.frequencies");
        // Order by weight, then node id: leaves (symbol index) before internal nodes (creation order).
        PriorityQueue<long[]> queue = new PriorityQueue<>(
                (x, y) -> x[0] != y[0] ? Long.compare(x[0], y[0]) : Long.compare(x[1], y[1]));
        for (int symbol = 0; symbol < frequencies.length; symbol++) {
            queue.add(new long[] {frequencies[symbol], symbol});
        }
        List<String> children = new ArrayList<>();
        int next = frequencies.length;
        while (queue.size() > 1) {
            long[] first = queue.remove();
            long[] second = queue.remove();
            children.add(next + "=" + first[1] + "+" + second[1]);
            queue.add(new long[] {first[0] + second[0], next});
            next++;
        }
        assertEquals(DATA.strings("optimization.huffman.children"), children);
    }

    // ---- subset-sum-halves ----

    private static List<Integer> sums(int[] values) {
        List<Integer> result = new ArrayList<>();
        for (int mask = 0; mask < (1 << values.length); mask++) {
            int sum = 0;
            for (int bit = 0; bit < values.length; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    sum += values[bit];
                }
            }
            result.add(sum);
        }
        return result;
    }

    @Test
    void subsetSumReturnsTheFixtureWitness() {
        int[] values = DATA.ints("optimization.subset.values");
        List<Integer> witness = BacktrackingPuzzles.subsetSum(values, DATA.number("optimization.subset.target"))
                .orElseThrow();
        assertEquals(DATA.list("optimization.subset.witness"), witness);
        assertEquals(DATA.number("optimization.subset.target"), witness.stream().mapToInt(i -> values[i]).sum());
    }

    @Test
    void halfSumsAndTheFirstMatchMatchTheMaskEnumeration() {
        int[] values = DATA.ints("optimization.subset.values");
        int target = DATA.number("optimization.subset.target");
        int middle = values.length / 2;
        List<Integer> left = sums(Arrays.copyOfRange(values, 0, middle));
        List<Integer> right = sums(Arrays.copyOfRange(values, middle, values.length));
        assertEquals(DATA.list("optimization.subset.leftSums"), left);
        assertEquals(DATA.list("optimization.subset.rightSums"), right);
        // Smallest left mask per sum, then the first right mask whose complement is stored.
        int firstRight = -1;
        int matchedLeft = -1;
        for (int rightMask = 0; rightMask < right.size() && firstRight < 0; rightMask++) {
            int complement = target - right.get(rightMask);
            if (left.contains(complement)) {
                firstRight = rightMask;
                matchedLeft = left.indexOf(complement);
            }
        }
        assertEquals(DATA.number("optimization.subset.rightMask"), firstRight);
        assertEquals(DATA.number("optimization.subset.leftMask"), matchedLeft);
        // Two left masks share a sum; only the smaller mask is kept.
        assertEquals(DATA.number("optimization.subset.duplicateSum"), left.get(0));
        assertEquals(left.get(0), left.get(left.size() - 1));
    }
}
