"""Explicit lesson-to-figure mapping; each placement has a teaching purpose."""
LESSONS = {
    "README.md": ("array-insertion", "See the state change, then explore all diagrams and step-throughs in the visual index."),
    "data-structures/arrays/overview.md": ("array-insertion", None),
    "data-structures/arrays/operations.md": ("fenwick-blocks", "The prefix blocks below complement the insertion trace: aggregates answer ranges without shifting array values."),
    "data-structures/linked-lists/overview.md": ("linked-unlink", None),
    "data-structures/linked-lists/singly.md": ("linked-unlink", "Contrast with a singly linked list: the doubly linked variant shown here repairs both directions; singly linked nodes keep only the forward link."),
    "data-structures/linked-lists/doubly.md": ("linked-unlink", None),
    "data-structures/stacks/overview.md": ("stack-lifo", None),
    "data-structures/stacks/implementation.md": ("stack-lifo", None),
    "data-structures/queues/overview.md": ("queue-wrap", None),
    "data-structures/queues/implementation.md": ("queue-wrap", None),
    "data-structures/queues/priority-queues.md": ("heap-anatomy", "A priority queue chooses a minimum, rather than the oldest arrival; array slots represent the complete heap shown below."),
    "data-structures/hash-tables/overview.md": ("lru-recency", "An LRU cache combines hashed lookup with an ordered recency list; a successful lookup changes the next eviction."),
    "data-structures/hash-tables/functions.md": ("hash-chaining", None),
    "data-structures/hash-tables/collision-resolution.md": ("hash-chaining", None),
    "data-structures/trees/overview.md": ("segment-minimum", None),
    "data-structures/trees/binary-trees.md": ("avl-rotation", "The same keys can occupy different shapes. This rotation changes shape while preserving inorder traversal."),
    "data-structures/trees/binary-search-trees.md": ("avl-rotation", "Both shapes satisfy BST order. AVL adds a height invariant and rotates the unbalanced shape into the balanced one."),
    "data-structures/trees/avl-trees.md": ("avl-rotation", None),
    "data-structures/graphs/overview.md": ("bfs-frontier", None),
    "data-structures/graphs/representation.md": ("scc-bridges", "Arrowheads and edge direction change reachability; the lower undirected panel answers a different connectivity question."),
    "data-structures/graphs/traversal.md": ("dijkstra-path", None),
    "data-structures/heaps/overview.md": ("heap-anatomy", None),
    "data-structures/heaps/operations.md": ("heap-anatomy", None),
    "data-structures/tries/overview.md": ("trie-pruning", None),
    "data-structures/tries/implementation.md": ("trie-pruning", None),
    "algorithms/searching/linear-search.md": ("kmp-overlap", "For text, reusing matched-prefix information avoids restarting the entire comparison and preserves overlapping matches."),
    "algorithms/searching/binary-search.md": ("binary-bound", None),
    "algorithms/sorting/bubble-sort.md": ("bubble-swaps", None),
    "algorithms/sorting/merge-sort.md": ("merge-recursion", None),
    "algorithms/sorting/quick-sort.md": ("quick-partition", None),
    "algorithms/sorting/heap-sort.md": ("heap-anatomy", "This is a min-heap for priority removal. In-place ascending heapsort instead builds a max-heap and moves each maximum to the array's end."),
    "algorithms/divide-and-conquer/overview.md": ("merge-recursion", None),
    "algorithms/divide-and-conquer/problems.md": ("quick-partition", None),
    "algorithms/dynamic-programming/overview.md": ("coin-dp", None),
    "algorithms/dynamic-programming/problems.md": ("knapsack-state", None),
    "algorithms/greedy/overview.md": ("activity-selection", None),
    "algorithms/greedy/problems.md": ("activity-selection", None),
    "algorithms/backtracking/overview.md": ("queens-backtracking", None),
    "algorithms/backtracking/problems.md": ("queens-backtracking", "The queens search illustrates choose, reject and undo. Sudoku applies the same pattern with row, column and box constraints."),
    "algorithms/graph/bfs.md": ("bfs-frontier", None),
    "algorithms/graph/dfs.md": ("scc-bridges", None),
    "algorithms/complexity-analysis/time-complexity.md": ("complexity-growth", None),
    "algorithms/complexity-analysis/space-complexity.md": ("merge-recursion", "This call tree depicts operations across time, not simultaneous allocations. Live frames on one branch determine recursion-stack space."),
    "complexity-analysis/time-complexity.md": ("complexity-growth", None),
    "complexity-analysis/space-complexity.md": ("merge-recursion", "Distinguish the whole call tree from live storage: only a root-to-current-call path is on the recursive stack at once."),
    "java-fundamentals/core-java-concepts.md": ("array-insertion", "Read indices, live size and ownership separately: mutation changes one array, while a defensive copy preserves another."),
    "docs/complexity.md": ("complexity-growth", None),
    "docs/solutions.md": ("union-find", "Use the component picture as a checkpoint, then predict each worked solution before revealing it."),
}

START = "<!-- visual:generated -->"
END = "<!-- /visual:generated -->"


def add_region(text, region, readme=False):
    if START in text:
        before, rest = text.split(START, 1)
        _, after = rest.split(END, 1)
        return before + region + after
    heading = "\n## Topic map" if readme else "\n## "
    location = text.find(heading)
    if location == -1:
        return text.rstrip() + "\n\n" + region + "\n"
    return text[:location].rstrip() + "\n\n" + region + "\n" + text[location:]
