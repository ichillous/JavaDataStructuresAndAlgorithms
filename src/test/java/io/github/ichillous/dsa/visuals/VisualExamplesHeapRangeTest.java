package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.Selection;
import io.github.ichillous.dsa.structures.BinaryMinHeap;
import io.github.ichillous.dsa.structures.LazySegmentTree;
import io.github.ichillous.dsa.structures.SparseTable;
import io.github.ichillous.dsa.structures.Trie;
import io.github.ichillous.dsa.structures.UnionFind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every value the heap_range figures display. The library classes keep their arrays and
 * parent pointers private, so each test recomputes the displayed structure with a small
 * independent model and compares the observable results with the library.
 */
class VisualExamplesHeapRangeTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-heap_range.properties");

    // heap-sift-paths

    @Test
    void siftUpAndSiftDownFollowTheDisplayedPaths() {
        List<Integer> base = DATA.list("heap_range.sift.base");
        int value = DATA.number("heap_range.sift.insert");

        ArrayList<Integer> array = new ArrayList<>(base);
        List<Integer> upPath = siftUp(array, value);
        assertEquals(DATA.list("heap_range.sift.insertPath"), upPath);
        assertEquals(DATA.list("heap_range.sift.afterInsert"), array);

        ArrayList<Integer> afterInsert = new ArrayList<>(array);
        int removed = afterInsert.get(0);
        List<Integer> downPath = removeRoot(afterInsert);
        assertEquals(DATA.number("heap_range.sift.removed"), removed);
        assertEquals(DATA.list("heap_range.sift.removePath"), downPath);
        assertEquals(DATA.list("heap_range.sift.afterRemove"), afterInsert);
    }

    @Test
    void heapMatchesTheDisplayedSiftResults() {
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>(DATA.list("heap_range.sift.base"));
        heap.add(DATA.number("heap_range.sift.insert"));
        assertEquals(DATA.list("heap_range.sift.afterInsert").get(0), heap.peek());
        assertEquals(DATA.list("heap_range.sift.removalOrderAfterInsert"), drain(heap));

        BinaryMinHeap<Integer> second = new BinaryMinHeap<>(DATA.list("heap_range.sift.base"));
        second.add(DATA.number("heap_range.sift.insert"));
        assertEquals(DATA.number("heap_range.sift.removed"), second.remove());
        assertEquals(DATA.list("heap_range.sift.afterRemove").get(0), second.peek());
        assertEquals(DATA.list("heap_range.sift.removalOrderAfterRemove"), drain(second));
    }

    // bounded-heap-topk

    private record Tagged(int value, int arrival) { }

    @Test
    void smallestKKeepsTheDisplayedValuesAfterEveryArrival() {
        int k = DATA.number("heap_range.topk.k");
        List<Integer> stream = DATA.list("heap_range.topk.stream");
        List<String> actions = DATA.strings("heap_range.topk.actions");
        String[] keptAfter = DATA.text("heap_range.topk.keptAfter").split("\\|");
        assertEquals(stream.size(), actions.size());
        assertEquals(stream.size(), keptAfter.length);

        List<Integer> previous = List.of();
        for (int arrival = 0; arrival < stream.size(); arrival++) {
            List<Integer> prefix = stream.subList(0, arrival + 1);
            List<Integer> kept = Selection.smallestK(prefix, k, Comparator.naturalOrder());
            assertEquals(parse(keptAfter[arrival]), kept, "kept after arrival " + arrival);

            String expected;
            if (previous.size() < k) {
                expected = "fill";
            } else if (kept.equals(previous)) {
                expected = "drop";
            } else {
                List<Integer> evicted = new ArrayList<>(previous);
                for (int survivor : kept) {
                    evicted.remove(Integer.valueOf(survivor));
                }
                assertEquals(1, evicted.size());
                expected = "replace:" + evicted.get(0);
                assertEquals(Collections.max(previous), evicted.get(0), "the root is what gets evicted");
            }
            assertEquals(expected, actions.get(arrival), "action for arrival " + arrival);
            previous = kept;
        }
        assertEquals(DATA.list("heap_range.topk.result"),
                Selection.smallestK(stream, k, Comparator.naturalOrder()));
    }

    @Test
    void anEqualArrivalIsDroppedAndTheEarlierOneStays() {
        int k = DATA.number("heap_range.topk.k");
        List<Integer> stream = DATA.list("heap_range.topk.stream");
        List<Tagged> tagged = new ArrayList<>();
        for (int arrival = 0; arrival < stream.size(); arrival++) {
            tagged.add(new Tagged(stream.get(arrival), arrival));
        }
        int dropped = DATA.strings("heap_range.topk.actions").indexOf("drop");
        assertTrue(dropped > 0);
        Comparator<Tagged> byValue = Comparator.comparingInt(Tagged::value);
        List<Tagged> kept = Selection.smallestK(tagged.subList(0, dropped + 1), k, byValue);
        assertFalse(kept.contains(tagged.get(dropped)), "the arriving equal value is not kept");
        int earlier = stream.indexOf(stream.get(dropped));
        assertSame(tagged.get(earlier), kept.stream().filter(t -> t.value() == stream.get(dropped)).findFirst().orElseThrow());
    }

    // trie-prefix-walk

    @Test
    void wordsWithPrefixStopsAtTheLimitInSortedOrder() {
        Trie trie = new Trie();
        for (String word : DATA.strings("heap_range.trie.words")) {
            trie.add(word);
        }
        String prefix = DATA.text("heap_range.trie.prefix");
        int limit = DATA.number("heap_range.trie.limit");
        assertTrue(trie.startsWith(prefix));
        assertFalse(trie.contains(prefix), "the prefix itself is not a stored word");
        assertEquals(DATA.strings("heap_range.trie.result"), trie.wordsWithPrefix(prefix, limit));
        List<String> everything = trie.wordsWithPrefix(prefix, 100);
        assertEquals(DATA.strings("heap_range.trie.unlimited"), everything);
        assertEquals(everything.subList(0, limit), trie.wordsWithPrefix(prefix, limit));
        assertTrue(DATA.strings("heap_range.trie.words").stream().anyMatch(word -> !word.startsWith(prefix)),
                "the figure shows a branch outside the prefix");
    }

    @Test
    void thePrefixCanItselfBeAWordAndIsEmittedFirst() {
        Trie trie = new Trie();
        for (String word : DATA.strings("heap_range.trie.words")) {
            trie.add(word);
        }
        String shown = DATA.strings("heap_range.trie.result").get(0);
        assertTrue(trie.contains(shown));
        assertEquals(shown, trie.wordsWithPrefix(shown, 1).get(0));
    }

    // sparse-table-overlap

    @Test
    void sparseTableCombinesTwoOverlappingBlocks() {
        long[] values = DATA.longs("heap_range.sparse.values");
        int from = DATA.number("heap_range.sparse.from");
        int to = DATA.number("heap_range.sparse.to");
        int level = DATA.number("heap_range.sparse.level");
        int block = 1 << level;
        int length = to - from;
        assertTrue(block <= length && length < 2 * block, "the block length is floor(log2(length))");

        SparseTable table = new SparseTable(values);
        long[] row = DATA.longs("heap_range.sparse.row");
        assertEquals(values.length - block + 1, row.length);
        for (int start = 0; start < row.length; start++) {
            assertEquals(row[start], table.rangeMin(start, start + block).getAsLong(), "row entry " + start);
        }
        int[] first = DATA.ints("heap_range.sparse.firstBlock");
        int[] second = DATA.ints("heap_range.sparse.secondBlock");
        assertEquals(from, first[0]);
        assertEquals(from + block, first[1]);
        assertEquals(to - block, second[0]);
        assertEquals(to, second[1]);
        long answer = Math.min(row[first[0]], row[second[0]]);
        assertEquals(DATA.number("heap_range.sparse.answer"), answer);
        assertEquals(answer, table.rangeMin(from, to).getAsLong());
        assertTrue(second[0] < first[1], "the blocks overlap");
    }

    @Test
    void addingTheOverlappingBlocksWouldCountTheOverlapTwice() {
        long[] values = DATA.longs("heap_range.sparse.values");
        int[] first = DATA.ints("heap_range.sparse.firstBlock");
        int[] second = DATA.ints("heap_range.sparse.secondBlock");
        LazySegmentTree sums = new LazySegmentTree(values);
        long[] blockSums = DATA.longs("heap_range.sparse.blockSums");
        assertEquals(blockSums[0], sums.rangeSum(first[0], first[1]));
        assertEquals(blockSums[1], sums.rangeSum(second[0], second[1]));
        long trueSum = sums.rangeSum(DATA.number("heap_range.sparse.from"), DATA.number("heap_range.sparse.to"));
        assertEquals(DATA.number("heap_range.sparse.trueSum"), trueSum);
        long overlap = sums.rangeSum(second[0], first[1]);
        assertEquals(blockSums[0] + blockSums[1] - overlap, trueSum);
        assertEquals(overlap, blockSums[0] + blockSums[1] - trueSum, "the surplus is the overlap");
    }

    // lazy-propagation

    @Test
    void rangeAddLeavesAPendingValueThatQueriesReadThrough() {
        long[] values = DATA.longs("heap_range.lazy.values");
        int addFrom = DATA.number("heap_range.lazy.addFrom");
        int addTo = DATA.number("heap_range.lazy.addTo");
        long delta = DATA.number("heap_range.lazy.delta");
        LazySegmentTree tree = new LazySegmentTree(values);
        long[] before = DATA.longs("heap_range.lazy.sumsBefore");
        assertEquals(before[0], tree.rangeSum(0, 4));
        assertEquals(before[1], tree.rangeSum(0, 2));
        assertEquals(before[2], tree.rangeSum(2, 4));

        tree.rangeAdd(addFrom, addTo, delta);
        long[] after = DATA.longs("heap_range.lazy.sumsAfter");
        assertEquals(after[0], tree.rangeSum(0, 4));
        assertEquals(after[1], tree.rangeSum(0, 2));
        assertEquals(after[2], tree.rangeSum(2, 4));
        assertEquals(before[2] + delta * (addTo - addFrom), after[2], "the covering node gets delta per element");

        long[] trueValues = DATA.longs("heap_range.lazy.trueValues");
        for (int index = 0; index < trueValues.length; index++) {
            assertEquals(trueValues[index], tree.rangeSum(index, index + 1), "true value " + index);
        }
        int from = DATA.number("heap_range.lazy.queryFrom");
        int to = DATA.number("heap_range.lazy.queryTo");
        long stored = DATA.number("heap_range.lazy.queryStored");
        assertEquals(values[from], stored, "the leaf below the tag still stores its old value");
        assertEquals(DATA.number("heap_range.lazy.queryAnswer"), tree.rangeSum(from, to));
        assertEquals(stored + delta * (to - from), tree.rangeSum(from, to));
    }

    // union-find-compression

    @Test
    void unionBySizeAndCompressionMatchTheDisplayedForest() {
        int size = DATA.number("heap_range.uf.size");
        UnionFind forest = new UnionFind(size);
        int[] parent = new int[size];
        int[] sizes = new int[size];
        for (int index = 0; index < size; index++) {
            parent[index] = index;
            sizes[index] = 1;
        }
        for (String pair : DATA.strings("heap_range.uf.unions")) {
            String[] parts = pair.split(":");
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            assertTrue(forest.union(first, second));
            modelUnion(parent, sizes, first, second);
            for (int index = 0; index < size; index++) {
                assertEquals(root(parent, index), forest.find(index), "representative of " + index);
            }
        }
        int[] rootSizes = DATA.ints("heap_range.uf.rootSizes");
        int mergeFirst = DATA.number("heap_range.uf.mergeFirst");
        int mergeSecond = DATA.number("heap_range.uf.mergeSecond");
        assertEquals(rootSizes[0], forest.componentSize(mergeFirst));
        assertEquals(rootSizes[1], forest.componentSize(mergeSecond));
        assertEquals(2, forest.components());

        // union(0, 5) compresses only the paths of 0 and 5, so 7 keeps its long path.
        assertTrue(forest.union(mergeFirst, mergeSecond));
        modelUnion(parent, sizes, mergeFirst, mergeSecond);
        assertEquals(DATA.list("heap_range.uf.parentsBefore"), Arrays.stream(parent).boxed().toList());
        int element = DATA.number("heap_range.uf.findElement");
        List<Integer> path = new ArrayList<>();
        for (int node = element; ; node = parent[node]) {
            path.add(node);
            if (parent[node] == node) {
                break;
            }
        }
        assertEquals(DATA.list("heap_range.uf.pathBefore"), path);

        int rootBefore = forest.find(element);
        modelFind(parent, element);
        assertEquals(DATA.list("heap_range.uf.parentsAfter"), Arrays.stream(parent).boxed().toList());
        assertEquals(path.get(path.size() - 1), rootBefore);
        assertEquals(DATA.number("heap_range.uf.components"), forest.components());
        assertEquals(DATA.number("heap_range.uf.componentSize"), forest.componentSize(element));
        assertTrue(forest.connected(element, mergeFirst));
    }

    // Independent reference models.

    private static List<Integer> siftUp(ArrayList<Integer> array, int value) {
        array.add(value);
        List<Integer> path = new ArrayList<>();
        int slot = array.size() - 1;
        path.add(slot);
        while (slot > 0 && array.get((slot - 1) / 2) > value) {
            array.set(slot, array.get((slot - 1) / 2));
            slot = (slot - 1) / 2;
            path.add(slot);
        }
        array.set(slot, value);
        return path;
    }

    private static List<Integer> removeRoot(ArrayList<Integer> array) {
        int last = array.remove(array.size() - 1);
        List<Integer> path = new ArrayList<>();
        path.add(0);
        if (array.isEmpty()) {
            return path;
        }
        int slot = 0;
        while (2 * slot + 1 < array.size()) {
            int child = 2 * slot + 1;
            if (child + 1 < array.size() && array.get(child + 1) < array.get(child)) {
                child++;
            }
            if (last <= array.get(child)) {
                break;
            }
            array.set(slot, array.get(child));
            slot = child;
            path.add(slot);
        }
        array.set(slot, last);
        return path;
    }

    private static List<Integer> drain(BinaryMinHeap<Integer> heap) {
        List<Integer> order = new ArrayList<>();
        while (!heap.isEmpty()) {
            order.add(heap.remove());
        }
        return order;
    }

    private static List<Integer> parse(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(",")).map(String::trim).map(Integer::valueOf).toList();
    }

    private static int root(int[] parent, int node) {
        while (parent[node] != node) {
            node = parent[node];
        }
        return node;
    }

    private static void modelFind(int[] parent, int element) {
        int root = root(parent, element);
        while (parent[element] != element) {
            int next = parent[element];
            parent[element] = root;
            element = next;
        }
    }

    private static void modelUnion(int[] parent, int[] sizes, int first, int second) {
        modelFind(parent, first);
        modelFind(parent, second);
        int firstRoot = root(parent, first);
        int secondRoot = root(parent, second);
        if (firstRoot == secondRoot) {
            return;
        }
        if (sizes[firstRoot] < sizes[secondRoot]) {
            int swap = firstRoot;
            firstRoot = secondRoot;
            secondRoot = swap;
        }
        parent[secondRoot] = firstRoot;
        sizes[firstRoot] += sizes[secondRoot];
    }
}
