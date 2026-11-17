package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.collections.DynamicArray;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Checks every value the linear_more figures display against the maintained classes. */
class VisualExamplesLinearMoreTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-linear_more.properties");

    @Test
    void appendingToAFullDynamicArrayDoublesCapacityAndCopiesTheOldValues() {
        int initialCapacity = DATA.number("linear_more.growth.capacity");
        DynamicArray<Integer> values = new DynamicArray<>(initialCapacity);
        for (int value : DATA.ints("linear_more.growth.before")) {
            values.add(value);
        }
        assertEquals(initialCapacity, values.size(), "the figure starts from a full array");
        assertEquals(initialCapacity, capacity(values));

        values.add(DATA.number("linear_more.growth.value"));
        assertEquals(DATA.number("linear_more.growth.grownCapacity"), capacity(values));
        assertEquals(DATA.list("linear_more.growth.after"), contents(values));
    }

    @Test
    void theRunningCopyCountMatchesObservedGrowthEvents() {
        int initialCapacity = DATA.number("linear_more.growth.capacity");
        int shown = DATA.list("linear_more.growth.after").size();
        int nextGrowth = DATA.number("linear_more.growth.nextGrowthAppend");
        DynamicArray<Integer> values = new DynamicArray<>(initialCapacity);
        int copies = 0;
        int copiesAtShown = -1;
        int firstLaterGrowth = -1;
        int laterCopies = -1;
        for (int append = 1; append <= nextGrowth; append++) {
            int before = capacity(values);
            int sizeBefore = values.size();
            values.add(append * 10);
            if (capacity(values) != before) {
                // A resize copies every live element into the new array.
                copies += sizeBefore;
                if (append > shown && firstLaterGrowth < 0) {
                    firstLaterGrowth = append;
                    laterCopies = sizeBefore;
                }
            }
            if (append == shown) {
                copiesAtShown = copies;
            }
        }
        assertEquals(DATA.number("linear_more.growth.copies"), copiesAtShown);
        assertEquals(nextGrowth, firstLaterGrowth);
        assertEquals(DATA.number("linear_more.growth.nextCopies"), laterCopies);
        assertEquals(DATA.number("linear_more.growth.totalCopies"), copies);
    }

    private static List<Integer> contents(DynamicArray<Integer> values) {
        List<Integer> observed = new ArrayList<>();
        values.forEach(observed::add);
        return observed;
    }

    /**
     * Capacity is not part of DynamicArray's public API, but the figure displays it and the
     * class documents geometric growth, so this reads the backing array's length directly.
     */
    private static int capacity(DynamicArray<?> values) {
        try {
            Field elements = DynamicArray.class.getDeclaredField("elements");
            elements.setAccessible(true);
            return ((Object[]) elements.get(values)).length;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("DynamicArray no longer stores an elements array", exception);
        }
    }
}
