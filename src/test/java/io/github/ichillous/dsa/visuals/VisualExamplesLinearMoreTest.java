package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.collections.ArrayStack;
import io.github.ichillous.dsa.collections.DoublyLinkedList;
import io.github.ichillous.dsa.collections.DynamicArray;
import io.github.ichillous.dsa.collections.RingDeque;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(initialCapacity, values.capacity());

        values.add(DATA.number("linear_more.growth.value"));
        assertEquals(DATA.number("linear_more.growth.grownCapacity"), values.capacity());
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
            int before = values.capacity();
            int sizeBefore = values.size();
            values.add(append * 10);
            if (values.capacity() != before) {
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

    /** A minimal singly linked node, local to this test: the library ships no singly linked list. */
    private static final class Node {
        final int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private static Node chain(int[] values) {
        Node head = null;
        for (int index = values.length - 1; index >= 0; index--) {
            Node node = new Node(values[index]);
            node.next = head;
            head = node;
        }
        return head;
    }

    private static Node nodeAt(Node head, int index) {
        Node node = head;
        for (int hop = 0; hop < index; hop++) {
            node = node.next;
        }
        return node;
    }

    @Test
    void insertingAfterAKnownNodeInTheRightOrderMatchesTheJdkAndTheMaintainedList() {
        int[] before = DATA.ints("linear_more.insert.before");
        int predecessor = DATA.number("linear_more.insert.predecessorIndex");
        int value = DATA.number("linear_more.insert.value");

        Node head = chain(before);
        Node pred = nodeAt(head, predecessor);
        Node added = new Node(value);
        added.next = pred.next; // First: the new node takes over the old successor link.
        pred.next = added;      // Second: the predecessor now points at the new node.
        List<Integer> observed = new ArrayList<>();
        for (Node node = head; node != null; node = node.next) {
            observed.add(node.value);
        }

        LinkedList<Integer> jdk = new LinkedList<>(Arrays.stream(before).boxed().toList());
        jdk.add(predecessor + 1, value);
        DoublyLinkedList<Integer> maintained = new DoublyLinkedList<>();
        for (int element : DATA.ints("linear_more.insert.result")) {
            maintained.addLast(element);
        }
        List<Integer> maintainedContents = new ArrayList<>();
        maintained.forEach(maintainedContents::add);

        assertEquals(DATA.list("linear_more.insert.result"), observed);
        assertEquals(DATA.list("linear_more.insert.result"), jdk);
        assertEquals(DATA.list("linear_more.insert.result"), maintainedContents);
    }

    @Test
    void writingThePredecessorLinkFirstLosesTheTail() {
        int[] before = DATA.ints("linear_more.insert.before");
        int predecessor = DATA.number("linear_more.insert.predecessorIndex");
        Node head = chain(before);
        Node pred = nodeAt(head, predecessor);
        Node originalSuccessor = pred.next;
        Node added = new Node(DATA.number("linear_more.insert.value"));
        pred.next = added;       // Wrong first write: the only reference to the old successor is gone.
        added.next = pred.next;  // Reads the new node itself.

        assertSame(added, added.next, "the new node points at itself");
        List<Integer> reachable = new ArrayList<>();
        Set<Node> seen = new HashSet<>();
        for (Node node = head; node != null && seen.add(node); node = node.next) {
            reachable.add(node.value);
        }
        assertEquals(DATA.list("linear_more.insert.wrongReachable"), reachable);
        List<Integer> lost = new ArrayList<>();
        for (Node node = originalSuccessor; node != null; node = node.next) {
            assertFalse(seen.contains(node), "the old successor is unreachable from head");
            lost.add(node.value);
        }
        assertEquals(DATA.list("linear_more.insert.wrongLost"), lost);
    }

    /** Pops every element (top first), then restores the stack; returns bottom-to-top text. */
    private static String stackText(ArrayStack<Character> stack) {
        List<Character> popped = new ArrayList<>();
        while (!stack.isEmpty()) {
            popped.add(stack.pop());
        }
        StringBuilder text = new StringBuilder();
        for (int index = popped.size() - 1; index >= 0; index--) {
            text.append(popped.get(index));
            stack.push(popped.get(index));
        }
        return text.length() == 0 ? "-" : text.toString();
    }

    private static boolean opens(char token) {
        return "([{".indexOf(token) >= 0;
    }

    private static char opener(char closer) {
        return "([{".charAt(")]}".indexOf(closer));
    }

    @Test
    void theBalancedBracketTraceHasTheStackContentsShownAfterEachCharacter() {
        String input = DATA.text("linear_more.brackets.balanced");
        ArrayStack<Character> stack = new ArrayStack<>();
        List<String> trace = new ArrayList<>();
        for (char token : input.toCharArray()) {
            if (opens(token)) {
                stack.push(token);
            } else {
                assertFalse(stack.isEmpty());
                assertEquals(opener(token), stack.pop());
            }
            trace.add(stackText(stack));
        }
        assertEquals(DATA.strings("linear_more.brackets.balancedStacks"), trace);
        assertTrue(stack.isEmpty(), "balanced input ends with an empty stack");
    }

    @Test
    void theUnbalancedBracketTraceStopsAtTheFirstMismatch() {
        String input = DATA.text("linear_more.brackets.unbalanced");
        ArrayStack<Character> stack = new ArrayStack<>();
        List<String> trace = new ArrayList<>();
        int mismatchAt = -1;
        char mismatchTop = 0;
        for (int index = 0; index < input.length() && mismatchAt < 0; index++) {
            char token = input.charAt(index);
            if (opens(token)) {
                stack.push(token);
                trace.add(stackText(stack));
            } else if (stack.isEmpty() || stack.peek() != opener(token)) {
                mismatchAt = index;
                mismatchTop = stack.peek();
            } else {
                stack.pop();
                trace.add(stackText(stack));
            }
        }
        List<String> expected = DATA.strings("linear_more.brackets.unbalancedStacks");
        assertEquals(expected, trace);
        assertEquals(DATA.number("linear_more.brackets.mismatchAt"), mismatchAt);
        assertEquals(DATA.text("linear_more.brackets.mismatchTop").charAt(0), mismatchTop);
        assertEquals(expected.get(expected.size() - 1), stackText(stack),
                "the stack still holds the unmatched openers when the mismatch is found");
    }

    /** Reads a private field. The public RingDeque API cannot show physical slots or the head. */
    private static Object field(Object target, Class<?> type, String name) {
        try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(type.getSimpleName() + " no longer has a field named " + name, exception);
        }
    }

    private static void assertDequeStep(RingDeque<Integer> deque, int step) {
        String prefix = "linear_more.deque.step" + step + ".";
        Object[] elements = (Object[]) field(deque, RingDeque.class, "elements");
        List<String> slots = new ArrayList<>();
        for (Object element : elements) {
            slots.add(element == null ? "." : element.toString());
        }
        int head = (Integer) field(deque, RingDeque.class, "head");
        List<Integer> order = DATA.list(prefix + "order");
        assertEquals(DATA.strings(prefix + "slots"), slots, "slots after step " + step);
        assertEquals(DATA.number(prefix + "head"), head, "head after step " + step);
        assertEquals(DATA.number(prefix + "tail"), (head + deque.size() - 1) % elements.length,
                "tail slot after step " + step);
        assertEquals(order.size(), deque.size(), "size after step " + step);
        assertEquals(order.get(0), deque.peekFirst(), "first after step " + step);
        assertEquals(order.get(order.size() - 1), deque.peekLast(), "last after step " + step);
    }

    @Test
    void theDequeStepsMoveHeadAndTailAroundTheCircularBuffer() {
        RingDeque<Integer> deque = new RingDeque<>(DATA.number("linear_more.deque.capacity"));
        deque.addLast(20);
        deque.addLast(30);
        assertDequeStep(deque, 1);
        deque.addFirst(10);
        assertDequeStep(deque, 2);
        deque.addLast(40);
        assertDequeStep(deque, 3);
        assertEquals(DATA.number("linear_more.deque.step4.removedLast"), deque.removeLast());
        assertEquals(DATA.number("linear_more.deque.step4.removedFirst"), deque.removeFirst());
        assertDequeStep(deque, 4);
    }

    @Test
    void indexedAccessIsOneJumpInAnArrayAndHopsFromTheNearerEndInTheList() {
        int[] values = DATA.ints("linear_more.access.values");
        int index = DATA.number("linear_more.access.index");
        DynamicArray<Integer> array = new DynamicArray<>();
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        for (int value : values) {
            array.add(value);
            list.addLast(value);
        }
        assertEquals(DATA.number("linear_more.access.value"), array.get(index));
        assertEquals(DATA.number("linear_more.access.value"), list.get(index));

        int[] hops = DATA.ints("linear_more.access.hops");
        assertEquals(values.length, hops.length);
        Object head = field(list, DoublyLinkedList.class, "head");
        Object tail = field(list, DoublyLinkedList.class, "tail");
        for (int position = 0; position < values.length; position++) {
            // The rule in DoublyLinkedList.nodeAt: the first half starts at the head, the rest at the tail.
            boolean fromHead = position < values.length / 2;
            Object node = fromHead ? head : tail;
            int target = fromHead ? position : values.length - 1 - position;
            for (int walked = 0; walked < target; walked++) {
                node = field(node, node.getClass(), fromHead ? "next" : "previous");
            }
            assertEquals(hops[position], target, "hops to index " + position);
            assertEquals(values[position], field(node, node.getClass(), "element"),
                    "node reached for index " + position);
            assertEquals(values[position], list.get(position));
            assertEquals(values[position], array.get(position));
            assertEquals(hops[position], Math.min(position, values.length - 1 - position));
        }
    }

    private static List<Integer> contents(DynamicArray<Integer> values) {
        List<Integer> observed = new ArrayList<>();
        values.forEach(observed::add);
        return observed;
    }
}
