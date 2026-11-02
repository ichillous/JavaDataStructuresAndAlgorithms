# Greedy algorithms need an exchange argument

A greedy algorithm commits to a local choice instead of exploring all alternatives.
Use it only when a proof shows that some optimum can contain that choice. The
maintained [GreedyAlgorithms](../../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java)
solves **maximum-cardinality unweighted activity selection**; it does not solve
every scheduling problem.

Intervals are nonempty and half-open `[start, end)`. Touching intervals are
compatible: one ending at 2 can precede one starting at 2. Integer-extreme
endpoints are valid. Null collections/elements and empty/backward intervals are
rejected. The method sorts a copy and returns an immutable schedule.

## Choose the next activity that finishes first

Among activities still compatible with the chosen prefix, take the one with the
earliest end. Suppose an optimal continuation starts with another activity `o`.
Replacing `o` with the earliest-finishing activity `g` leaves at least as much
remaining time because `end(g) <= end(o)`. The number of selected activities is
unchanged, and later selections remain feasible. Repeating the exchange proves
that the greedy schedule has optimal cardinality.

The loop invariant is that selected intervals are compatible and their prefix can
be extended to an optimal schedule. Maintaining only compatibility would prove
feasibility, not optimality.

```text
activity       interval    decision in finish-time order
B              [1, 2)      take
C              [2, 3)      take; touching is permitted
A              [0, 4)      skip; overlaps chosen prefix
D              [3, 5)      take
```

```java
import io.github.ichillous.dsa.algorithms.GreedyAlgorithms;
import io.github.ichillous.dsa.algorithms.GreedyAlgorithms.Interval;
import java.util.List;

List<Interval> input = List.of(
        new Interval(0, 4), new Interval(1, 2),
        new Interval(2, 3), new Interval(3, 5));
var selected = GreedyAlgorithms.selectActivities(input);
assert selected.equals(List.of(new Interval(1, 2), new Interval(2, 3), new Interval(3, 5)));
assert input.size() == 4;
assert GreedyAlgorithms.selectActivities(List.of()).isEmpty();
```

Sorting takes O(n log(n + 1)) time, scanning takes O(n), and the sorted copy plus
output use O(n) space. Equal finishes are ordered by start. Comparing endpoints
with a comparator is safer than subtracting them, which can overflow.

**Counterexamples:** choosing earliest start accepts `[0, 4)` before `[1, 2)` and
`[2, 3)`, losing an activity. For a weighted objective, a single valuable interval
might beat several short cheap ones; earliest finish has no proof for total value.

## Practice

1. Trace two compatible activities that share an endpoint and two that overlap.
2. Write a tiny exhaustive subset oracle and compare its maximum count with the
   greedy schedule, including equal finishes and negative endpoints.
3. Add interval values and construct a counterexample to earliest finish. State
   a DP subproblem that could solve the weighted version.

See [solutions](../../docs/solutions.md) and
[other greedy problems](problems.md) for where proof obligations change.
