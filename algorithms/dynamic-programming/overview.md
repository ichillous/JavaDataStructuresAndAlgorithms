# Dynamic programming starts with a state definition

Dynamic programming replaces repeated subproblems with stored answers. Use it
when a complete solution can be expressed through smaller states and those states
recur. The maintained
[DynamicProgramming](../../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java)
contains bottom-up examples with explicit validation and no shared mutable state.

A useful design sequence is: define one state in a sentence, derive its recurrence,
identify base cases, choose an order that satisfies dependencies, then decide what
must be retained. Writing loops before the state often hides a different problem.

## Worked example: unlimited coin change

Let `best[a]` mean the minimum number of coins totaling exactly `a`.
`best[0] = 0`; unreachable amounts have an infinity sentinel. For positive coin
value `c <= a`, a valid predecessor gives candidate `best[a-c] + 1`. Minimize over
all denominations. Ascending amounts make every smaller dependency available.

For coins `[1, 3, 4]`:

| Amount `a` | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `best[a]` | 0 | 1 | 2 | 1 | 1 | 2 | 2 |
| One final choice | — | 1 | 1 | 3 | 4 | 1 | 3 |

The loop invariant is that every table entry below the current amount already
contains its optimum. Any optimum for the current amount ends with some coin;
removing it produces exactly one of the predecessor states considered.

```java
import io.github.ichillous.dsa.algorithms.DynamicProgramming;

assert DynamicProgramming.minimumCoins(new int[] {1, 3, 4}, 6) == 2;
assert DynamicProgramming.minimumCoins(new int[] {2, 4}, 3) == -1;
assert DynamicProgramming.minimumCoins(new int[0], 0) == 0;
```

Coins must be strictly positive; duplicates are permitted, each denomination may
be reused, and amount must be nonnegative. Null is rejected. Input is unchanged.
Validation/allocation included, time is O((c + 1)(a + 1)) and working space is
O(a + 1), where `c` is the number of supplied denominations.

This is **pseudopolynomial**: storing the numeric amount needs O(log(a + 1)) bits,
but the table has `a + 1` entries. A valid large amount can still exhaust memory.

## Memoization versus tabulation

Memoization discovers states through recursion and caches results. Tabulation
visits states in a chosen dependency order. Both require a correct state identity;
forgetting a parameter in a cache key can combine unrelated subproblems. Memoized
recursion also retains call frames, and Java does not guarantee tail-call removal.

**Counterexample:** always choosing the largest coin picks `4 + 1 + 1` for amount
6, while the table finds `3 + 3`. A plausible local rule is not a proof of global
optimality. Conversely, DP is unnecessary when a simple greedy rule has a proof.

## Practice

1. Fill the table for denominations `[2, 5]` through amount 7, marking unreachable
   states rather than adding to an infinity sentinel.
2. Explain which extra information would reconstruct the chosen coins.
3. Change unlimited coin reuse to “each supplied coin once.” Which state or loop
   direction changes? Continue with [0/1 knapsack](problems.md).

Compare with the [solutions](../../docs/solutions.md) after deriving the recurrence.
