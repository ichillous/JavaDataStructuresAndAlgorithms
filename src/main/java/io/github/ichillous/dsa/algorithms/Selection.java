package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.PriorityQueue;

/**
 * Selection problems: the k-th smallest value, the k smallest elements, a k-way merge of
 * sorted lists and the majority element. No method modifies its arguments, and every returned
 * list is immutable. Collection elements must be non-null.
 */
public final class Selection {
    private Selection() {}

    /**
     * Returns the value that would sit at zero-based index {@code k} if {@code values} were
     * sorted ascending; duplicates count separately, so the 0th and 1st smallest of
     * {@code [4, 4]} are both 4. The input is copied and left unchanged.
     *
     * <p>This is introselect: quickselect with a median-of-three pivot and a three-way
     * partition. Invariant: after each partition the k-th element of the whole range lies in
     * the segment that contains index {@code k}, because everything left of that segment is
     * smaller and everything right of it is larger (or the segment of values equal to the
     * pivot already contains {@code k}). Only that segment is searched next. If more than
     * {@code 2 * (32 - Integer.numberOfLeadingZeros(n))} partition rounds are needed, which
     * only adversarial inputs cause, the pivot is chosen by median-of-medians instead, which
     * guarantees a constant fraction of the segment is discarded each round.
     *
     * <p>Time O(n) worst case (expected O(n) with the cheap pivot); auxiliary space O(n) for
     * the copy plus O(log n) recursion in the fallback.
     *
     * @param k zero-based rank, checked with {@link java.util.Objects#checkIndex}
     * @throws NullPointerException if {@code values} is null
     * @throws IndexOutOfBoundsException if {@code k} is outside {@code [0, values.length)}, so
     *     an empty array always throws
     */
    public static int kthSmallest(int[] values, int k) {
        Objects.requireNonNull(values, "values");
        Objects.checkIndex(k, values.length);
        return select(values.clone(), 0, values.length - 1, k, roundLimit(values.length));
    }

    /** Test hook: like {@link #kthSmallest} with an explicit round limit; 0 forces the fallback. */
    static int kthSmallestWithRoundLimit(int[] values, int k, int roundLimit) {
        Objects.checkIndex(k, values.length);
        return select(values.clone(), 0, values.length - 1, k, roundLimit);
    }

    private static int roundLimit(int length) {
        return 2 * (32 - Integer.numberOfLeadingZeros(length));
    }

    /** Selects index k within the inclusive range [low, high] of a, permuting that range. */
    private static int select(int[] a, int low, int high, int k, int roundLimit) {
        int rounds = 0;
        while (low < high) {
            int pivot = rounds >= roundLimit ? medianOfMedians(a, low, high) : medianOfThree(a, low, high);
            rounds++;
            // Invariant: a[low..lt-1] < pivot, a[lt..i-1] == pivot, a[gt+1..high] > pivot.
            int lt = low;
            int gt = high;
            int i = low;
            while (i <= gt) {
                if (a[i] < pivot) {
                    swap(a, lt++, i++);
                } else if (a[i] > pivot) {
                    swap(a, i, gt--);
                } else {
                    i++;
                }
            }
            if (k < lt) {
                high = lt - 1;
            } else if (k > gt) {
                low = gt + 1;
            } else {
                return pivot;
            }
        }
        return a[k];
    }

    private static int medianOfThree(int[] a, int low, int high) {
        int x = a[low];
        int y = a[low + (high - low) / 2];
        int z = a[high];
        return Math.max(Math.min(x, y), Math.min(Math.max(x, y), z));
    }

    /** Returns the median of the group medians; the guaranteed-linear pivot. Permutes the range. */
    private static int medianOfMedians(int[] a, int low, int high) {
        int groups = 0;
        for (int start = low; start <= high; start += 5) {
            int end = Math.min(start + 4, high);
            insertionSort(a, start, end);
            swap(a, low + groups, start + (end - start) / 2);
            groups++;
        }
        // The recursion is over at most a fifth of the range, so its depth is O(log n).
        return select(a, low, low + groups - 1, low + (groups - 1) / 2, 0);
    }

    private static void insertionSort(int[] a, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int value = a[i];
            int j = i - 1;
            while (j >= low && a[j] > value) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = value;
        }
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    private record Ranked<T>(T value, int sequence) {}

    /**
     * Returns the {@code k} smallest elements in ascending order under {@code order}. Equal
     * elements keep their encounter order, so the result equals
     * {@code values.stream().sorted(order).limit(k)}. {@code k = 0} yields an empty list and
     * {@code k >= values.size()} yields every element, sorted. The result is immutable.
     *
     * <p>Invariant: a max-heap of at most {@code k} entries holds the best k seen so far, with
     * the worst of them (largest, and among equals the latest) at the root; a new element
     * replaces the root only if it is strictly smaller than it. Time O(n log(k + 1)) and
     * space O(min(k, n)) beyond the result, which has the same size. The comparator is called
     * with elements of {@code values} only.
     *
     * @throws NullPointerException if an argument or an element is null
     * @throws IllegalArgumentException if {@code k} is negative
     */
    public static <T> List<T> smallestK(Collection<? extends T> values, int k, Comparator<? super T> order) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(order, "order");
        if (k < 0) {
            throw new IllegalArgumentException("k must not be negative");
        }
        Comparator<Ranked<T>> ascending = (left, right) -> {
            int result = order.compare(left.value(), right.value());
            return result != 0 ? result : Integer.compare(left.sequence(), right.sequence());
        };
        int capacity = Math.min(k, values.size());
        PriorityQueue<Ranked<T>> worstFirst = new PriorityQueue<>(Math.max(1, capacity), ascending.reversed());
        int sequence = 0;
        for (T value : values) {
            Objects.requireNonNull(value, "element");
            Ranked<T> candidate = new Ranked<>(value, sequence++);
            if (worstFirst.size() < capacity) {
                worstFirst.add(candidate);
            } else if (capacity > 0 && ascending.compare(candidate, worstFirst.peek()) < 0) {
                worstFirst.poll();
                worstFirst.add(candidate);
            }
        }
        List<Ranked<T>> kept = new ArrayList<>(worstFirst);
        kept.sort(ascending);
        List<T> result = new ArrayList<>(kept.size());
        for (Ranked<T> ranked : kept) {
            result.add(ranked.value());
        }
        return List.copyOf(result);
    }

    private record Cursor<T>(T value, int list, Iterator<? extends T> rest) {}

    /**
     * Merges individually sorted lists into one sorted immutable list. Ties are resolved by
     * the lower list index first; within a list the original order is kept, so the result
     * equals a stable sort of the concatenation of the lists.
     *
     * <p>Invariant: the heap holds the first unconsumed element of every non-exhausted list;
     * its minimum (ties by list index) is the next output element. Each list is also checked
     * as it is consumed: an element smaller than its predecessor in the same list raises
     * {@link IllegalArgumentException} immediately, at O(1) extra cost per element. Because
     * every element is consumed, any unsorted list is detected. Lists are traversed with
     * iterators and may be any {@link List}. Time O(N log(k + 1)) for N total elements across
     * k lists; space O(k) beyond the result.
     *
     * @throws NullPointerException if an argument, a list or an element is null
     * @throws IllegalArgumentException if any list is not sorted ascending under {@code order}
     */
    public static <T> List<T> mergeSorted(List<? extends List<? extends T>> lists, Comparator<? super T> order) {
        Objects.requireNonNull(lists, "lists");
        Objects.requireNonNull(order, "order");
        PriorityQueue<Cursor<T>> heap = new PriorityQueue<>((left, right) -> {
            int result = order.compare(left.value(), right.value());
            return result != 0 ? result : Integer.compare(left.list(), right.list());
        });
        int index = 0;
        for (List<? extends T> list : lists) {
            Objects.requireNonNull(list, "list");
            Iterator<? extends T> iterator = list.iterator();
            if (iterator.hasNext()) {
                heap.add(new Cursor<>(Objects.requireNonNull(iterator.next(), "element"), index, iterator));
            }
            index++;
        }
        List<T> merged = new ArrayList<>();
        while (!heap.isEmpty()) {
            Cursor<T> smallest = heap.poll();
            merged.add(smallest.value());
            if (smallest.rest().hasNext()) {
                T next = Objects.requireNonNull(smallest.rest().next(), "element");
                if (order.compare(smallest.value(), next) > 0) {
                    throw new IllegalArgumentException("list " + smallest.list() + " is not sorted");
                }
                heap.add(new Cursor<>(next, smallest.list(), smallest.rest()));
            }
        }
        return List.copyOf(merged);
    }

    /**
     * Returns the value occurring in strictly more than half of the positions, or empty if
     * there is none (including for an empty array). The input is unchanged.
     *
     * <p>Boyer-Moore invariant: after each element, the elements consumed so far can be
     * paired off into distinct-valued pairs with at most {@code count} copies of the
     * {@code candidate} left over. A majority value cannot be fully cancelled by such pairs,
     * so it must be the final candidate; a second pass counts it, because the first pass
     * alone also returns a candidate when no majority exists. Time O(n), space O(1).
     */
    public static OptionalInt majorityElement(int[] values) {
        Objects.requireNonNull(values, "values");
        int candidate = 0;
        int count = 0;
        for (int value : values) {
            if (count == 0) {
                candidate = value;
                count = 1;
            } else if (value == candidate) {
                count++;
            } else {
                count--;
            }
        }
        int occurrences = 0;
        for (int value : values) {
            if (value == candidate) {
                occurrences++;
            }
        }
        return occurrences > values.length / 2 ? OptionalInt.of(candidate) : OptionalInt.empty();
    }
}
