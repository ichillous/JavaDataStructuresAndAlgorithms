package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ichillous.dsa.algorithms.GreedyAlgorithms.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GreedyAlgorithmsTest {
    @Test
    void earliestFinishBeatsChoosingEarliestStartAndAllowsTouching() {
        Interval longInterval = new Interval(0, 10);
        Interval first = new Interval(1, 3);
        Interval second = new Interval(3, 5);
        Interval third = new Interval(5, 6);
        assertEquals(List.of(first, second, third),
                GreedyAlgorithms.selectActivities(List.of(longInterval, third, second, first)));
        assertEquals(List.of(), GreedyAlgorithms.selectActivities(List.of()));
    }

    @Test
    void handlesNegativeAndExtremeEndpointsAndDuplicateActivities() {
        Interval first = new Interval(Integer.MIN_VALUE, -1);
        Interval second = new Interval(-1, Integer.MAX_VALUE);
        assertEquals(List.of(first, second),
                GreedyAlgorithms.selectActivities(List.of(second, first, first)));
    }

    @Test
    void resultIsImmutableAndInputOrderDoesNotChange() {
        List<Interval> intervals = new ArrayList<>(List.of(new Interval(4, 6), new Interval(0, 2)));
        List<Interval> original = List.copyOf(intervals);
        List<Interval> result = GreedyAlgorithms.selectActivities(intervals);
        assertEquals(original, intervals);
        assertThrows(UnsupportedOperationException.class, () -> result.add(new Interval(7, 8)));
    }

    @Test
    void scheduleCardinalityAgreesWithExhaustiveCompatibleSubsets() {
        Random random = new Random(0x622ED);
        for (int trial = 0; trial < 300; trial++) {
            List<Interval> intervals = new ArrayList<>();
            int size = random.nextInt(11);
            for (int index = 0; index < size; index++) {
                int start = random.nextInt(20) - 10;
                intervals.add(new Interval(start, start + random.nextInt(8) + 1));
            }
            int expected = 0;
            for (int mask = 0; mask < (1 << size); mask++) {
                boolean compatible = true;
                for (int first = 0; first < size && compatible; first++) {
                    if ((mask & (1 << first)) == 0) {
                        continue;
                    }
                    for (int second = first + 1; second < size; second++) {
                        if ((mask & (1 << second)) != 0
                                && overlaps(intervals.get(first), intervals.get(second))) {
                            compatible = false;
                            break;
                        }
                    }
                }
                if (compatible) {
                    expected = Math.max(expected, Integer.bitCount(mask));
                }
            }
            List<Interval> selected = GreedyAlgorithms.selectActivities(intervals);
            assertEquals(expected, selected.size(), "trial " + trial);
            for (int index = 0; index < selected.size(); index++) {
                assertTrue(intervals.contains(selected.get(index)));
                if (index > 0) {
                    assertTrue(selected.get(index - 1).end() <= selected.get(index).start());
                }
            }
        }
    }

    @Test
    void rejectsEmptyBackwardsAndNullIntervals() {
        assertThrows(IllegalArgumentException.class, () -> new Interval(1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Interval(4, 1));
        assertThrows(NullPointerException.class, () -> GreedyAlgorithms.selectActivities(null));
        assertThrows(NullPointerException.class,
                () -> GreedyAlgorithms.selectActivities(Arrays.asList(new Interval(0, 1), null)));
    }

    private static boolean overlaps(Interval first, Interval second) {
        return first.start() < second.end() && second.start() < first.end();
    }
}
