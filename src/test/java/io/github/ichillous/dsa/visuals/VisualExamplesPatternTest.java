package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.ArrayPatterns;
import io.github.ichillous.dsa.algorithms.ArrayPatterns.IndexPair;
import io.github.ichillous.dsa.algorithms.ArrayPatterns.RangeAdd;
import io.github.ichillous.dsa.algorithms.GreedyAlgorithms.Interval;
import io.github.ichillous.dsa.algorithms.SequenceAlgorithms;
import io.github.ichillous.dsa.algorithms.StringAlgorithms.Span;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks every value the array-pattern, divide-and-conquer and complexity figures display.
 * Pointer positions, stacks and windows are private to the library, so each test asserts the
 * public result with the library and re-derives every displayed intermediate state with plain
 * code that follows the invariant documented on the method.
 */
class VisualExamplesPatternTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-pattern.properties");

    @Test
    void twoPointersMoveTheEndInwardAndFindTheDocumentedPair() {
        int[] values = DATA.ints("pattern.pair.values");
        long target = DATA.number("pattern.pair.target");
        assertEquals(Optional.of(new IndexPair(DATA.number("pattern.pair.first"),
                DATA.number("pattern.pair.second"))), ArrayPatterns.pairWithSum(values, target));

        List<Integer> lefts = new ArrayList<>();
        List<Integer> rights = new ArrayList<>();
        List<Integer> sums = new ArrayList<>();
        int left = 0;
        int right = values.length - 1;
        while (left < right) {
            int sum = values[left] + values[right];
            lefts.add(left);
            rights.add(right);
            sums.add(sum);
            if (sum == target) {
                break;
            }
            if (sum < target) {
                // The largest remaining value is too small, so values[left] has no partner.
                left++;
            } else {
                // The smallest remaining value is too large, so values[right] has no partner.
                right--;
            }
        }
        assertEquals(DATA.list("pattern.pair.lefts"), lefts);
        assertEquals(DATA.list("pattern.pair.rights"), rights);
        assertEquals(DATA.list("pattern.pair.sums"), sums);
    }

    @Test
    void theWindowJumpsPastARepeatAndIgnoresAStaleOne() {
        String text = DATA.text("pattern.window.text");
        assertEquals(new Span(DATA.number("pattern.window.spanStart"), DATA.number("pattern.window.spanEnd")),
                ArrayPatterns.longestSubstringWithoutRepeats(text));

        List<Integer> shown = DATA.list("pattern.window.shown");
        List<Integer> previous = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        List<Integer> bests = new ArrayList<>();
        Map<Character, Integer> lastSeen = new HashMap<>();
        int start = 0;
        int best = 0;
        for (int i = 0; i < text.length(); i++) {
            Integer seen = lastSeen.put(text.charAt(i), i);
            if (seen != null && seen >= start) {
                start = seen + 1;
            }
            best = Math.max(best, i - start + 1);
            if (shown.contains(i)) {
                previous.add(seen == null ? -1 : seen);
                starts.add(start);
                bests.add(best);
            }
        }
        assertEquals(DATA.list("pattern.window.previous"), previous);
        assertEquals(DATA.list("pattern.window.starts"), starts);
        assertEquals(DATA.list("pattern.window.bests"), bests);
    }

    @Test
    void theMonotonicStackResolvesSmallerValuesWhenABiggerOneArrives() {
        int[] values = DATA.ints("pattern.stack.values");
        assertArrayEquals(DATA.ints("pattern.stack.result"), ArrayPatterns.nextGreaterIndices(values));

        List<Integer> stack = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            List<Integer> pops = new ArrayList<>();
            while (!stack.isEmpty() && values[stack.get(stack.size() - 1)] < values[i]) {
                pops.add(stack.remove(stack.size() - 1));
            }
            stack.add(i);
            assertEquals(DATA.list("pattern.stack.pops" + i), pops, "pops at index " + i);
            assertEquals(DATA.list("pattern.stack.after" + i), stack, "stack after index " + i);
        }
    }

    @Test
    void theDifferenceArrayMarksTwoBoundariesPerUpdateThenOnePrefixPass() {
        int length = DATA.number("pattern.diff.length");
        int[] first = DATA.ints("pattern.diff.first");
        int[] second = DATA.ints("pattern.diff.second");
        List<RangeAdd> updates = List.of(new RangeAdd(first[0], first[1], first[2]),
                new RangeAdd(second[0], second[1], second[2]));
        long[] result = ArrayPatterns.applyRangeAdds(new long[length], updates);
        assertArrayEquals(DATA.longs("pattern.diff.result"), result);

        long[] diff = new long[length + 1];
        diff[first[0]] += first[2];
        diff[first[1]] -= first[2];
        assertArrayEquals(DATA.longs("pattern.diff.afterFirst"), diff);
        diff[second[0]] += second[2];
        diff[second[1]] -= second[2];
        assertArrayEquals(DATA.longs("pattern.diff.afterSecond"), diff);
        long[] prefix = new long[length];
        long running = 0;
        for (int i = 0; i < length; i++) {
            running += diff[i];
            prefix[i] = running;
        }
        assertArrayEquals(result, prefix);
    }

    @Test
    void theSweepProcessesEndsBeforeStartsAtEqualTimesSoTouchingIntervalsShareARoom() {
        int[] starts = DATA.ints("pattern.sweep.starts");
        int[] ends = DATA.ints("pattern.sweep.ends");
        List<Interval> intervals = new ArrayList<>();
        for (int i = 0; i < starts.length; i++) {
            intervals.add(new Interval(starts[i], ends[i]));
        }
        assertEquals(DATA.number("pattern.sweep.rooms"), ArrayPatterns.minimumMeetingRooms(intervals));

        // Events as (time, kind): kind 0 is an end, kind 1 a start, so an end sorts first at a tie.
        List<int[]> events = new ArrayList<>();
        for (int i = 0; i < starts.length; i++) {
            events.add(new int[] {starts[i], 1});
            events.add(new int[] {ends[i], 0});
        }
        events.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        List<String> names = new ArrayList<>();
        List<Integer> depths = new ArrayList<>();
        int depth = 0;
        int deepest = 0;
        for (int[] event : events) {
            depth += event[1] == 1 ? 1 : -1;
            deepest = Math.max(deepest, depth);
            names.add((event[1] == 1 ? "S" : "E") + event[0]);
            depths.add(depth);
        }
        assertEquals(DATA.strings("pattern.sweep.events"), names);
        assertEquals(DATA.list("pattern.sweep.depths"), depths);
        assertEquals(DATA.number("pattern.sweep.rooms"), deepest);

        // An independent model: the most intervals covering one integer time unit [t, t + 1).
        int covered = 0;
        for (int t = 0; t < 20; t++) {
            int count = 0;
            for (int i = 0; i < starts.length; i++) {
                if (starts[i] <= t && t < ends[i]) {
                    count++;
                }
            }
            covered = Math.max(covered, count);
        }
        assertEquals(DATA.number("pattern.sweep.rooms"), covered);
    }

    @Test
    void aSmallerRightValueJumpsOverEveryRemainingLeftValueDuringTheMerge() {
        int[] left = DATA.ints("pattern.inversion.left");
        int[] right = DATA.ints("pattern.inversion.right");
        int[] whole = new int[left.length + right.length];
        System.arraycopy(left, 0, whole, 0, left.length);
        System.arraycopy(right, 0, whole, left.length, right.length);

        // The library splits at length / 2, which is exactly the fixture's left and right halves.
        assertEquals(left.length, whole.length / 2);
        assertEquals(0L, SequenceAlgorithms.countInversions(left));
        assertEquals(0L, SequenceAlgorithms.countInversions(right));
        assertEquals(DATA.number("pattern.inversion.total"), SequenceAlgorithms.countInversions(whole));

        List<Integer> emitted = new ArrayList<>();
        List<Integer> adds = new ArrayList<>();
        int l = 0;
        int r = 0;
        while (l < left.length || r < right.length) {
            if (l < left.length && (r == right.length || left[l] <= right[r])) {
                emitted.add(left[l++]);
                adds.add(0);
            } else {
                emitted.add(right[r++]);
                adds.add(left.length - l);
            }
        }
        assertEquals(DATA.list("pattern.inversion.emitted"), emitted);
        assertEquals(DATA.list("pattern.inversion.adds"), adds);
        assertEquals(DATA.number("pattern.inversion.total"), adds.stream().mapToInt(Integer::intValue).sum());

        // An independent quadratic count of the pairs i < j with whole[i] > whole[j].
        int pairs = 0;
        for (int i = 0; i < whole.length; i++) {
            for (int j = i + 1; j < whole.length; j++) {
                if (whole[i] > whole[j]) {
                    pairs++;
                }
            }
        }
        assertEquals(DATA.number("pattern.inversion.total"), pairs);
        assertEquals(List.of(1, 2, 3, 4, 5), Arrays.stream(whole).sorted().boxed().toList());
    }
}
