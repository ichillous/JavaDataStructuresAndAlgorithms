package io.github.ichillous.dsa.visuals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.ichillous.dsa.algorithms.SearchAlgorithms;
import io.github.ichillous.dsa.algorithms.Selection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks every value the searching figures display against the maintained classes. */
class VisualExamplesSearchingTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-searching.properties");

    // ---- linear-scan-first-match ----

    @Test
    void linearSearchStopsAtTheFirstDuplicateAndScansEverythingOnAMiss() {
        int[] values = DATA.ints("searching.scan.values");
        int hitTarget = DATA.number("searching.scan.hitTarget");
        int missTarget = DATA.number("searching.scan.missTarget");
        int[] original = values.clone();

        int hit = SearchAlgorithms.linearSearch(values, hitTarget);
        assertEquals(DATA.number("searching.scan.hitIndex"), hit);
        // The figure's duplicate really exists later, and is not what the search reports.
        int lastIndex = Arrays.stream(values).boxed().toList().lastIndexOf(hitTarget);
        assertNotEquals(hit, lastIndex);
        assertEquals(DATA.number("searching.scan.missResult"), SearchAlgorithms.linearSearch(values, missTarget));
        assertEquals(DATA.number("searching.scan.hitComparisons"), comparisonsUntilMatch(values, hitTarget));
        assertEquals(DATA.number("searching.scan.missComparisons"), comparisonsUntilMatch(values, missTarget));
        assertEquals(values.length, DATA.number("searching.scan.missComparisons"));
        assertEquals(Arrays.toString(original), Arrays.toString(values));
    }

    private static int comparisonsUntilMatch(int[] values, int target) {
        int comparisons = 0;
        for (int value : values) {
            comparisons++;
            if (value == target) {
                break;
            }
        }
        return comparisons;
    }

    // ---- predicate-boundary ----

    /** Days needed to ship the weights in order with this capacity, by a plain greedy loop. */
    private static int daysNeeded(int[] weights, int capacity) {
        int days = 1;
        int load = 0;
        for (int weight : weights) {
            if (load + weight > capacity) {
                days++;
                load = 0;
            }
            load += weight;
        }
        return days;
    }

    @Test
    void capacityDaysNeededAndTheAnswerMatchBruteForceAndFirstTrue() {
        int[] weights = DATA.ints("searching.capacity.weights");
        int days = DATA.number("searching.capacity.days");
        int low = DATA.number("searching.capacity.low");
        int high = DATA.number("searching.capacity.highExclusive");
        assertEquals(Arrays.stream(weights).max().getAsInt(), low, "no capacity below the heaviest weight works");
        assertEquals(Arrays.stream(weights).sum() + 1, high, "the total weight always works");

        int[] expectedDays = DATA.ints("searching.capacity.daysNeeded");
        assertEquals(high - low, expectedDays.length);
        for (int capacity = low; capacity < high; capacity++) {
            assertEquals(expectedDays[capacity - low], daysNeeded(weights, capacity), "capacity " + capacity);
        }

        long brute = high;
        for (int capacity = low; capacity < high; capacity++) {
            if (daysNeeded(weights, capacity) <= days) {
                brute = capacity;
                break;
            }
        }
        assertEquals(DATA.number("searching.capacity.answer"), brute);
        assertEquals(brute,
                SearchAlgorithms.firstTrue(low, high, capacity -> daysNeeded(weights, (int) capacity) <= days));
    }

    @Test
    void firstTrueProbesTheCapacitiesTheFigureShows() {
        int[] weights = DATA.ints("searching.capacity.weights");
        int days = DATA.number("searching.capacity.days");
        List<String> probes = new ArrayList<>();
        long answer = SearchAlgorithms.firstTrue(DATA.number("searching.capacity.low"),
                DATA.number("searching.capacity.highExclusive"), capacity -> {
                    int needed = daysNeeded(weights, (int) capacity);
                    probes.add(capacity + ":" + needed);
                    return needed <= days;
                });
        assertEquals(DATA.strings("searching.capacity.probes"), probes);
        assertEquals(DATA.number("searching.capacity.answer"), answer);
    }

    // ---- quickselect-shrink ----

    @Test
    void kthSmallestReturnsTheFigureAnswer() {
        int[] values = DATA.ints("searching.quick.values");
        int[] original = values.clone();
        int k = DATA.number("searching.quick.k");
        assertEquals(DATA.number("searching.quick.answer"), Selection.kthSmallest(values, k));
        assertEquals(Arrays.stream(values).sorted().toArray()[k], DATA.number("searching.quick.answer"));
        assertEquals(Arrays.toString(original), Arrays.toString(values));
    }

    @Test
    void eachDisplayedPartitionFollowsTheDocumentedRule() {
        // Re-derives every state: median-of-three pivot over a[low], a[middle], a[high], a
        // three-way partition, then keep the side that holds k.
        int[] a = DATA.ints("searching.quick.values");
        int k = DATA.number("searching.quick.k");
        List<String> rounds = DATA.strings("searching.quick.rounds");
        int low = 0;
        int high = a.length - 1;
        int round = 0;
        while (low < high) {
            int[] sample = {a[low], a[low + (high - low) / 2], a[high]};
            Arrays.sort(sample);
            int pivot = sample[1];
            int lessEnd = low;
            int greaterStart = high;
            int scan = low;
            while (scan <= greaterStart) {
                if (a[scan] < pivot) {
                    swap(a, lessEnd, scan);
                    lessEnd++;
                    scan++;
                } else if (a[scan] > pivot) {
                    swap(a, greaterStart, scan);
                    greaterStart--;
                } else {
                    scan++;
                }
            }
            round++;
            assertEquals(rounds.get(round - 1), low + ":" + high + ":" + pivot + ":" + lessEnd + ":" + greaterStart);
            assertEquals(DATA.list("searching.quick.after" + round), Arrays.stream(a).boxed().toList());
            if (k < lessEnd) {
                high = lessEnd - 1;
            } else if (k > greaterStart) {
                low = greaterStart + 1;
            } else {
                assertEquals(DATA.number("searching.quick.answer"), pivot);
                break;
            }
        }
        assertEquals(rounds.size(), round, "the figure shows every round");
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    // ---- kway-merge-heap ----

    private record Tagged(int value, int list) {}

    private static List<List<Tagged>> taggedLists() {
        List<List<Tagged>> lists = new ArrayList<>();
        for (int list = 0; list < 3; list++) {
            List<Tagged> one = new ArrayList<>();
            for (int value : DATA.ints("searching.merge.list" + list)) {
                one.add(new Tagged(value, list));
            }
            lists.add(one);
        }
        return lists;
    }

    @Test
    void mergeSortedOrdersEqualValuesByListIndex() {
        // The comparator looks only at the value, so the list tag shows which side of a tie won.
        List<Tagged> merged = Selection.mergeSorted(taggedLists(), Comparator.comparingInt(Tagged::value));
        assertEquals(DATA.list("searching.merge.output"), merged.stream().map(Tagged::value).toList());
        assertEquals(DATA.list("searching.merge.sources"), merged.stream().map(Tagged::list).toList());
    }

    @Test
    void theHeapHoldsTheExpectedHeadsAfterThreeOutputs() {
        List<List<Tagged>> lists = taggedLists();
        List<Tagged> merged = Selection.mergeSorted(lists, Comparator.comparingInt(Tagged::value));
        List<Tagged> heads = new ArrayList<>();
        for (List<Tagged> list : lists) {
            List<Tagged> remaining = new ArrayList<>(list);
            remaining.removeAll(merged.subList(0, 3));
            if (!remaining.isEmpty()) {
                heads.add(remaining.get(0));
            }
        }
        heads.sort(Comparator.comparingInt(Tagged::value).thenComparingInt(Tagged::list));
        assertEquals(DATA.strings("searching.merge.headsAfterThree"),
                heads.stream().map(head -> head.value() + ":" + head.list()).toList());
    }
}
