# Reading companions and primary references

Use a reference to answer a concrete question, then return to a small implementation
and an independent test. Reading every book cover to cover is not a prerequisite
for using this repository. The [roadmap](../docs/learning-roadmap.md) supplies a
practical order, and the [catalog](../docs/catalog.md) identifies maintained APIs.

## Choose a reference by the question

| Reference | Useful purpose | Pair it with |
| --- | --- | --- |
| [Java 17 collections API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/package-summary.html) | Exact JDK contracts, types and documented behavior | Compare `ArrayDeque`, `TreeSet`, `HashMap` and `PriorityQueue` with the educational implementations |
| [Introduction to Algorithms, fourth edition](https://mitpress.mit.edu/9780262046305/introduction-to-algorithms/), Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest and Clifford Stein | Formal invariants, recurrences and correctness arguments; algorithms are presented in pseudocode | Sorting proofs, amortized analysis, graph cuts and dynamic programming |
| [The Algorithm Design Manual](https://www.algorist.com/), Steven Skiena | Recognizing problem structures and selecting algorithmic techniques | State a problem's feasible solutions and objective before selecting a technique |

These are companions with different purposes, not a performance ranking or a list
of dependencies. Check an edition's own errata when working through a derivation.
Avoid assuming that a book is Java-specific merely because a problem can be
implemented in Java.

## Read contracts rather than matching names

A reference algorithm and an API with the same name can have different semantics.
For duplicates, this repository's `binarySearch` promises the first matching index;
JDK `Arrays.binarySearch` does not promise which equal element is found.

```java
import io.github.ichillous.dsa.algorithms.SearchAlgorithms;
import java.util.Arrays;

int[] sorted = {1, 2, 2, 2, 5};
int jdkResult = Arrays.binarySearch(sorted, 2);
assert jdkResult >= 0 && sorted[jdkResult] == 2;
assert SearchAlgorithms.binarySearch(sorted, 2) == 1;
assert SearchAlgorithms.upperBound(sorted, 2) - SearchAlgorithms.lowerBound(sorted, 2) == 3;
```

The shared invariant is that a sorted search can discard a half only when its
ordering excludes the answer. The first-match extension must keep searching toward
the left boundary. Both are logarithmic queries on already sorted input, but a test
requiring identical duplicate indices would compare incompatible contracts.

## A repeatable reading session

1. State the input, output and preconditions in your own words.
2. Trace a tiny example, including one empty, duplicate or boundary case.
3. Identify the invariant or exchange argument that proves correctness.
4. Derive time and live-space bounds, including output and numeric assumptions.
5. Run the matching maintained example and test against a simpler reference model.

**Pitfall:** a short snippet that prints one plausible answer is not evidence that
an implementation handles empty input, repeated values or overflow. Likewise,
passing tests cannot prove an asymptotic claim; count operations and state the
assumptions. The tested examples here establish behavior for their fixtures and
are accompanied by broader implementation tests.

## Practice

Pick one book or API page and compare its contract with a maintained method. Record
one difference in representation, tie handling, mutation or numeric model. Then
write a test that would reject the wrong interpretation. Use the
[worked solutions](../docs/solutions.md) after drafting your own proof or oracle.
