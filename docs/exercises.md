# Exercises with evidence

Use the [catalog](catalog.md) to find source and corresponding tests. Complete each exercise by writing down a contract, providing a hand-worked example, implementing the change, adding a regression test and stating the time and space costs. The hints below describe a direction, not a required implementation.

| Exercise | Input or scenario | Success criteria and hint |
| --- | --- | --- |
| Dynamic-array insertion | Insert into an empty list, then insert at the front, middle and `size` | Match `ArrayList`; reject negative indices and indices above `size`. Trace the direction of the shift so values are not overwritten. |
| Queue wraparound | Enqueue, dequeue enough to move the head, then enqueue until growth occurs | Match `ArrayDeque` before and after growth. Logical order must survive copying from two physical array segments. |
| Iterator contract | Create an iterator, mutate its collection, then call `next` | Test the documented fail-fast policy, exhaustion and unsupported `remove`. A value replacement may differ from a structural change; read the API contract. |
| Adversarial hashing | Give many distinct keys the same hash code; also use a key whose hash is `Integer.MIN_VALUE` | Lookup, replacement, removal and resizing must match `HashMap` under the repository's non-null policy. Collision-heavy operations are linear, not guaranteed constant time. |
| Binary-search boundaries | `[1, 2, 2, 2, 5]` with targets `0`, `2`, `4`, `6` | `upperBound - lowerBound` gives the occurrence count. State that input must be sorted; do not hide an O(n) validation scan inside an advertised O(log n) search. |
| Sorting oracle | Empty, all-equal, ascending, descending, duplicate-heavy and extreme integer arrays | Compare every sort to `Arrays.sort` on a copy. Check that length and element multiplicities are preserved. Explain why subtraction is unsafe for comparing arbitrary `int` values. |
| Tree deletion | Delete a leaf, a one-child node, a two-child root, then an absent key | Compare inorder contents and size with `TreeSet` after every operation. Verify AVL balance after both insertion and deletion, including all rotation directions. |
| Trie pruning | Add `""`, `"car"`, `"cart"`, `"cat"`; remove `"car"` and then `"cart"` | Preserve `"cat"`, distinguish whole-word membership from prefix existence, and respect the result limit. A node's end-of-word flag is independent of whether it has children. |
| Disjoint-set model | Repeatedly connect random vertex pairs | Match a simple connected-component reference. A repeated union must not reduce the component count. The physical representative is an implementation detail. |
| Fenwick range sums | Initialize `[2, -1, 4, 3]`, add `5` at index `1`, and query `[1, 3)` | Compare prefixes and ranges with a simple array after each point update. Invalid ranges must fail; an overflowing update must leave every previously queryable sum unchanged. |
| Graph reachability | A graph containing a cycle, an isolated vertex and disconnected components | BFS and DFS visit only reachable vertices, once each. Check paths by validating each consecutive edge rather than demanding one arbitrary shortest path when ties exist. |
| Dijkstra oracle | Generate small graphs with nonnegative weights | Compare all distances to a small Floyd–Warshall reference. Include unreachable vertices, zero-cost edges and parallel edges; test the documented arithmetic policy separately. |
| Minimum spanning forest | Two disconnected weighted triangles plus an isolated vertex | Verify acyclicity, component preservation, selected-edge count and total weight. For tiny graphs, enumerate candidate forests independently. |
| Greedy counterexample | Coin denominations `[1, 3, 4]`, amount `6` | Largest-coin-first chooses three coins; the optimal answer uses two. Contrast this failure with earliest-finishing activity selection, whose exchange argument establishes optimality for that problem. |
| Knapsack loop direction | One item of weight `2`, value `3`, capacity `4` | A 0/1 solution returns `3`, not `6`. Explain how an ascending capacity loop accidentally permits reuse. Check zero-weight items against the documented contract. |
| String matching | Text `"aaaaa"`, pattern `"aaa"`; then an empty pattern | Find overlapping starts `[0, 1, 2]`. Explain the prefix-function fallback and the chosen empty-pattern behavior. UTF-16 indices are not grapheme-cluster indices. |
| Unique permutations | `[1, 1, 2]`, then `[]` | Produce exactly three unique permutations in the first case and one empty permutation in the second. Track positions as well as values. |
| N-Queens verification | Solve sizes `0`, `1`, `2`, `3`, `4` | Validate every returned board independently for row, column and diagonal conflicts. Check the empty-board convention and keep enumeration sizes small. |

## A repeatable review exercise

Choose a structure and intentionally introduce one defect: skip resetting the queue head on clear, fail to decrement tree size, or remove a trie prefix with its descendants. Confirm that a relevant test detects it, then restore the correct implementation. This is a local learning exercise; do not commit intentionally broken code.

For an extension, add a small, justified API such as a deque or a segment tree. Decide behavior before implementation: nulls, index boundaries, mutation, overflow and empty results. Add the extension to the catalog and explain when it is preferable to an existing JDK type. More classes alone do not make a stronger learning resource.
