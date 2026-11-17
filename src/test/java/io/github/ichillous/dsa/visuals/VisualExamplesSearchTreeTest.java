package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.structures.BinarySearchTree;
import io.github.ichillous.dsa.structures.RedBlackTreeMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every value the search_tree figures display. The library keeps its tree shapes
 * private, so shapes are checked in two ways: the public observations (keys, size,
 * height) are asserted on the real classes, and each drawn shape is compared with a
 * small reference model in this file that follows the documented rules independently.
 */
class VisualExamplesSearchTreeTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-search_tree.properties");

    // ---- tree-traversals ----

    @Test
    void traversalFigureMatchesTheLibraryAndAnIndependentReference() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        Model reference = null;
        for (int key : DATA.ints("search_tree.traversal.insertOrder")) {
            assertTrue(tree.add(key));
            reference = insert(reference, key);
        }
        assertEquals(DATA.number("search_tree.traversal.size"), tree.size());
        assertEquals(DATA.number("search_tree.traversal.height"), tree.height());
        assertEquals(DATA.list("search_tree.traversal.inOrder"), tree.inOrder());

        // The library offers only in-order; the others come from the reference model.
        assertEquals(DATA.text("search_tree.traversal.shape"), reference.shape());
        assertEquals(DATA.number("search_tree.traversal.height"), reference.height());
        assertEquals(DATA.list("search_tree.traversal.inOrder"), reference.inOrder());
        assertEquals(DATA.list("search_tree.traversal.preOrder"), reference.preOrder());
        assertEquals(DATA.list("search_tree.traversal.postOrder"), reference.postOrder());
        assertEquals(DATA.list("search_tree.traversal.levelOrder"), reference.levelOrder());
        assertEquals(DATA.list("search_tree.traversal.leaves"), reference.leaves());
    }

    // ---- bst-delete-successor ----

    @Test
    void deletingATwoChildNodeUsesTheInOrderSuccessor() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        Model reference = null;
        for (int key : DATA.ints("search_tree.delete.insertOrder")) {
            tree.add(key);
            reference = insert(reference, key);
        }
        int key = DATA.number("search_tree.delete.key");
        assertEquals(DATA.list("search_tree.delete.beforeInOrder"), tree.inOrder());
        assertEquals(DATA.number("search_tree.delete.beforeHeight"), tree.height());
        assertEquals(DATA.text("search_tree.delete.before"), reference.shape());

        Model node = reference.find(key);
        assertTrue(node.left != null && node.right != null, "the deleted node has two children");
        assertEquals(DATA.number("search_tree.delete.successor"), node.right.minimum().key);

        assertTrue(tree.remove(key));
        assertEquals(DATA.list("search_tree.delete.afterInOrder"), tree.inOrder());
        assertEquals(DATA.number("search_tree.delete.afterHeight"), tree.height());
        assertFalse(tree.contains(key));
        assertEquals(DATA.text("search_tree.delete.after"), reference.removeWithSuccessor(key).shape());

        // Three keys at height 1 admit only one shape. Using the predecessor instead would
        // leave a taller tree, so the library's height shows which neighbour it used.
        Model viaPredecessor = null;
        for (int inserted : DATA.ints("search_tree.delete.insertOrder")) {
            viaPredecessor = insert(viaPredecessor, inserted);
        }
        assertNotEquals(tree.height(), viaPredecessor.removeWithPredecessor(key).height());
    }

    // ---- red-black-23-nodes ----

    @Test
    void redBlackShapeEncodesTheTwoThreeTreeOfTheSameKeys() {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        for (int key : DATA.ints("search_tree.twothree.putOrder")) {
            assertEquals(null, map.put(key, "v" + key));
        }
        assertEquals(DATA.list("search_tree.twothree.putOrder"), map.keys());
        assertEquals(DATA.number("search_tree.twothree.height"), map.height());

        assertEquals(map.size(), puts(DATA.ints("search_tree.twothree.putOrder"), new ArrayList<>()).inOrder().size());
        assertEquals(3, map.rank(4));
        assertEquals(4, map.select(3));

        Model shape = Model.parse(DATA.text("search_tree.twothree.shape"));
        // The drawn shape is what the documented left-leaning insertion builds for these puts.
        assertEquals(DATA.text("search_tree.twothree.shape"),
                puts(DATA.ints("search_tree.twothree.putOrder"), new ArrayList<>()).coloredShape());
        assertEquals(map.keys(), shape.inOrder());
        assertEquals(map.height(), shape.height());
        shape.assertLeftLeaning(true);
        assertFalse(shape.red, "the root is black");

        // Merge each red node into its black parent: the black links are the 2-3 edges.
        assertEquals(DATA.list("search_tree.twothree.rootNode"), shape.twoThreeKeys());
        List<Integer> children = new ArrayList<>();
        for (Model child : shape.twoThreeChildren()) {
            assertEquals(1, child.twoThreeKeys().size(), "each child here is a 2-node");
            children.add(child.key);
        }
        assertEquals(DATA.list("search_tree.twothree.childNodes"), children);
        assertEquals(DATA.number("search_tree.twothree.twoThreeHeight"), shape.blackHeight() - 1);
    }

    // ---- red-black-fixups ----

    @Test
    void rotateLeftTurnsARightLeaningRedLinkIntoALeftLeaningOne() {
        checkFixup("rotateLeft", Model::rotateLeft);
    }

    @Test
    void rotateRightBalancesTwoRedLinksInARow() {
        checkFixup("rotateRight", Model::rotateRight);
    }

    @Test
    void flipColorsSplitsAFourNode() {
        checkFixup("flip", Model::flipColors);
    }

    private static void checkFixup(String name, java.util.function.UnaryOperator<Model> repair) {
        String prefix = "search_tree.fix." + name;
        Model before = Model.parse(DATA.text(prefix + ".before"));
        Model after = repair.apply(Model.parse(DATA.text(prefix + ".before")));

        // The documented insertion applies this repair to exactly these shapes while running the puts.
        List<String[]> applied = new ArrayList<>();
        puts(DATA.ints(prefix + ".puts"), applied);
        String op = name.equals("flip") ? "flip" : name;
        assertTrue(applied.stream().anyMatch(step -> step[0].equals(op)
                        && step[1].equals(DATA.text(prefix + ".before"))
                        && step[2].equals(DATA.text(prefix + ".after"))),
                "the puts reach " + DATA.text(prefix + ".before") + " and repair it with " + op);
        assertEquals(DATA.text(prefix + ".after"), after.coloredShape());
        assertEquals(before.inOrder(), after.inOrder(), "a repair keeps the sorted order");
        assertEquals(before.blackHeight(), after.blackHeight(), "a repair keeps the black height");

        // The puts that trigger the repair finish with a sorted, perfectly balanced map.
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        for (int key : DATA.ints(prefix + ".puts")) {
            map.put(key, "v" + key);
        }
        assertEquals(before.inOrder(), map.keys());
        assertEquals(DATA.number("search_tree.fix.height"), map.height());
    }

    // ---- reference model: a plain node with the documented rules, independent of the library ----

    /**
     * Runs left-leaning red-black insertion (the class Javadoc and lesson: add a red leaf, then
     * on the way up rotate left, rotate right, flip colors, and color the root black). Each
     * repair is recorded as {operation, shape before, shape after} of the subtree it changed.
     */
    private static Model puts(int[] keys, List<String[]> applied) {
        Model root = null;
        for (int key : keys) {
            root = put(root, key, applied);
            root.red = false;
        }
        return root;
    }

    private static Model put(Model node, int key, List<String[]> applied) {
        if (node == null) {
            return new Model(key, true, null, null);
        }
        if (key < node.key) {
            node.left = put(node.left, key, applied);
        } else if (key > node.key) {
            node.right = put(node.right, key, applied);
        }
        if (isRed(node.right) && !isRed(node.left)) {
            node = record(applied, "rotateLeft", node, Model::rotateLeft);
        }
        if (isRed(node.left) && isRed(node.left.left)) {
            node = record(applied, "rotateRight", node, Model::rotateRight);
        }
        if (isRed(node.left) && isRed(node.right)) {
            node = record(applied, "flip", node, Model::flipColors);
        }
        return node;
    }

    private static Model record(List<String[]> applied, String operation, Model node,
                                java.util.function.UnaryOperator<Model> repair) {
        String before = node.coloredShape();
        Model result = repair.apply(node);
        applied.add(new String[] {operation, before, result.coloredShape()});
        return result;
    }

    private static boolean isRed(Model node) {
        return node != null && node.red;
    }

    private static Model insert(Model node, int key) {
        if (node == null) {
            return new Model(key, false, null, null);
        }
        if (key < node.key) {
            node.left = insert(node.left, key);
        } else if (key > node.key) {
            node.right = insert(node.right, key);
        }
        return node;
    }

    private static final class Model {
        int key;
        boolean red;
        Model left;
        Model right;

        Model(int key, boolean red, Model left, Model right) {
            this.key = key;
            this.red = red;
            this.left = left;
            this.right = right;
        }

        /** Parses {@code key[B|R](left,right)}; a missing child is empty, a leaf has no brackets. */
        static Model parse(String text) {
            int[] position = {0};
            return parse(text, position);
        }

        private static Model parse(String text, int[] position) {
            int start = position[0];
            while (position[0] < text.length() && Character.isDigit(text.charAt(position[0]))) {
                position[0]++;
            }
            if (start == position[0]) {
                return null;
            }
            int key = Integer.parseInt(text.substring(start, position[0]));
            boolean red = false;
            if (position[0] < text.length() && (text.charAt(position[0]) == 'R' || text.charAt(position[0]) == 'B')) {
                red = text.charAt(position[0]) == 'R';
                position[0]++;
            }
            Model left = null;
            Model right = null;
            if (position[0] < text.length() && text.charAt(position[0]) == '(') {
                position[0]++;
                left = parse(text, position);
                position[0]++; // the comma
                right = parse(text, position);
                position[0]++; // the closing bracket
            }
            return new Model(key, red, left, right);
        }

        String shape() {
            return shape(false);
        }

        private String shape(boolean colored) {
            String self = key + (colored ? (red ? "R" : "B") : "");
            if (left == null && right == null) {
                return self;
            }
            return self + "(" + (left == null ? "" : left.shape(colored)) + ","
                    + (right == null ? "" : right.shape(colored)) + ")";
        }

        Model find(int target) {
            Model node = this;
            while (node != null && node.key != target) {
                node = target < node.key ? node.left : node.right;
            }
            return node;
        }

        Model minimum() {
            Model node = this;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        Model maximum() {
            Model node = this;
            while (node.right != null) {
                node = node.right;
            }
            return node;
        }

        int height() {
            return 1 + Math.max(left == null ? -1 : left.height(), right == null ? -1 : right.height());
        }

        List<Integer> inOrder() {
            List<Integer> keys = new ArrayList<>();
            collect(keys, 1);
            return keys;
        }

        List<Integer> preOrder() {
            List<Integer> keys = new ArrayList<>();
            collect(keys, 0);
            return keys;
        }

        List<Integer> postOrder() {
            List<Integer> keys = new ArrayList<>();
            collect(keys, 2);
            return keys;
        }

        private void collect(List<Integer> keys, int rootPosition) {
            if (rootPosition == 0) {
                keys.add(key);
            }
            if (left != null) {
                left.collect(keys, rootPosition);
            }
            if (rootPosition == 1) {
                keys.add(key);
            }
            if (right != null) {
                right.collect(keys, rootPosition);
            }
            if (rootPosition == 2) {
                keys.add(key);
            }
        }

        List<Integer> levelOrder() {
            List<Integer> keys = new ArrayList<>();
            ArrayDeque<Model> queue = new ArrayDeque<>();
            queue.add(this);
            while (!queue.isEmpty()) {
                Model node = queue.remove();
                keys.add(node.key);
                if (node.left != null) {
                    queue.add(node.left);
                }
                if (node.right != null) {
                    queue.add(node.right);
                }
            }
            return keys;
        }

        List<Integer> leaves() {
            return inOrderNodes().stream().filter(node -> node.left == null && node.right == null)
                    .map(node -> node.key).toList();
        }

        private List<Model> inOrderNodes() {
            List<Model> nodes = new ArrayList<>();
            if (left != null) {
                nodes.addAll(left.inOrderNodes());
            }
            nodes.add(this);
            if (right != null) {
                nodes.addAll(right.inOrderNodes());
            }
            return nodes;
        }

        /** Removes the key by copying its in-order successor, the rule the lesson documents. */
        Model removeWithSuccessor(int target) {
            return remove(target, true);
        }

        Model removeWithPredecessor(int target) {
            return remove(target, false);
        }

        private Model remove(int target, boolean successor) {
            if (target < key) {
                left = left.remove(target, successor);
                return this;
            }
            if (target > key) {
                right = right.remove(target, successor);
                return this;
            }
            if (left == null || right == null) {
                return left == null ? right : left;
            }
            if (successor) {
                key = right.minimum().key;
                right = right.remove(key, true);
            } else {
                key = left.maximum().key;
                left = left.remove(key, false);
            }
            return this;
        }

        // Left-leaning red-black repairs; each returns the new top of the subtree.

        Model rotateLeft() {
            Model top = right;
            right = top.left;
            top.left = this;
            top.red = red;
            red = true;
            return top;
        }

        Model rotateRight() {
            Model top = left;
            left = top.right;
            top.right = this;
            top.red = red;
            red = true;
            return top;
        }

        Model flipColors() {
            red = !red;
            left.red = !left.red;
            right.red = !right.red;
            return this;
        }

        /** The shape text with a B or R after every key. */
        String coloredShape() {
            return shape(true);
        }

        /** The number of black nodes on each root-to-empty-link path below and including this node. */
        private java.util.Set<Integer> pathBlacks() {
            java.util.Set<Integer> counts = new java.util.TreeSet<>();
            for (Model child : new Model[] {left, right}) {
                if (child == null) {
                    counts.add(red ? 0 : 1);
                } else {
                    for (int below : child.pathBlacks()) {
                        counts.add(below + (red ? 0 : 1));
                    }
                }
            }
            return counts;
        }

        /** The black height; fails unless every path to an empty link has the same count. */
        int blackHeight() {
            java.util.Set<Integer> counts = pathBlacks();
            assertEquals(1, counts.size(), "every path has the same black height: " + counts);
            return counts.iterator().next();
        }

        void assertLeftLeaning(boolean root) {
            if (left != null) {
                left.assertLeftLeaning(false);
            }
            if (right != null) {
                assertFalse(right.red, "a red link leans left");
                right.assertLeftLeaning(false);
            }
            if (red) {
                assertTrue(left == null || !left.red, "no red node has a red child");
                assertFalse(root, "the root is black");
            }
            blackHeight();
        }

        /** The black nodes one 2-3 edge below this node's 2-3 node, left to right. */
        List<Model> twoThreeChildren() {
            List<Model> children = new ArrayList<>();
            Model first = left != null && left.red ? left : null;
            if (first != null) {
                children.addAll(List.of(first.left, first.right));
            } else if (left != null) {
                children.add(left);
            }
            if (right != null) {
                children.add(right);
            }
            return children;
        }

        /** The keys of the 2-3 node this black node heads: itself plus a red left child. */
        List<Integer> twoThreeKeys() {
            return left != null && left.red ? List.of(left.key, key) : List.of(key);
        }
    }
}
