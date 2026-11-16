# Multi-pattern matching and suffix arrays

Use [AhoCorasick](../../src/main/java/io/github/ichillous/dsa/algorithms/AhoCorasick.java)
to find every occurrence of many patterns in one pass over a text. Use
[SuffixArray](../../src/main/java/io/github/ichillous/dsa/algorithms/SuffixArray.java)
when the text is fixed and you will ask many questions about it: occurrences of
arbitrary patterns, distinct substrings, or the longest repeated substring.
Positions are UTF-16 code-unit indices, matching `StringAlgorithms.kmpFindAll`;
no case folding or normalization happens. Both classes copy their input and
never mutate it. Aho-Corasick rejects empty patterns and collapses duplicates.

## The invariants

**Aho-Corasick.** Patterns form a trie. For a node spelling `s`, its failure
link is the node for the longest *proper suffix* of `s` that is also a trie
prefix. Scanning keeps the longest trie prefix that ends at the current text
position; on a mismatch it follows failure links, and each text character
advances the depth by at most one, so all failure steps together are O(t).
A second link per node jumps to the nearest pattern ending on the failure chain,
so every match is emitted exactly once. Links are built breadth first, so
shallow nodes are final before deeper ones need them and long patterns cannot
overflow the stack.

**Suffix array.** `suffixes()[r]` is the start of the r-th smallest suffix.
Prefix doubling ranks suffixes by their first `k` characters, then by `2k`,
`4k`, and so on. Each round sorts the pair (rank of the first half, rank of the
second half) with two stable counting passes, giving O(n log n) overall.
Kasai's algorithm then fills `lcp[i]`, the common prefix length of ranks `i - 1`
and `i`, in O(n): moving from suffix `i` to `i + 1` can lose at most one
matching character.

Searching `"ushers"` for `he`, `she`, `his`, `hers`:

```text
pos char  trie prefix kept  matches reported
 0   u    (root)
 1   s    s
 2   h    sh
 3   e    she               she at 1, then via output link he at 2
 4   r    her               (fell back from she to he, then extended)
 5   s    hers              hers at 2
result ordered by start, then length: (1,she) (2,he) (2,hers)
```

Suffix array of `banana`:

```text
rank suffix   start  lcp with previous
 0   a          5     0
 1   ana        3     1
 2   anana      1     3
 3   banana     0     0
 4   na         4     0
 5   nana       2     2
distinct substrings = 6*7/2 - (0+1+3+0+0+2) = 15
longest repeated    = "ana" (largest lcp, 3)
```

```java
import io.github.ichillous.dsa.algorithms.AhoCorasick;
import io.github.ichillous.dsa.algorithms.AhoCorasick.Match;
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import io.github.ichillous.dsa.algorithms.SuffixArray;
import java.util.Arrays;
import java.util.List;

AhoCorasick dictionary = new AhoCorasick(List.of("he", "she", "his", "hers"));
assert dictionary.findAll("ushers").equals(
        List.of(new Match(1, "she"), new Match(2, "he"), new Match(2, "hers")));
assert dictionary.findAll("").isEmpty();

SuffixArray banana = new SuffixArray("banana");
assert Arrays.equals(banana.suffixes(), new int[] {5, 3, 1, 0, 4, 2});
assert Arrays.equals(banana.lcp(), new int[] {0, 1, 3, 0, 0, 2});
assert banana.occurrences("ana").equals(List.of(1, 3));
assert banana.occurrences("ana").equals(StringAlgorithms.kmpFindAll("banana", "ana"));
assert banana.occurrences("").equals(List.of(0, 1, 2, 3, 4, 5, 6));
assert banana.distinctSubstrings() == 15L;
assert banana.longestRepeatedSubstring().equals("ana");
assert new SuffixArray("abc").longestRepeatedSubstring().isEmpty();
```

## Costs

For text length `t`, total pattern length `P` and `k` matches in Aho-Corasick; for
text length `n`, pattern length `p` and `k` hits in the suffix array:

| Operation | Time | Extra space |
| --- | --- | --- |
| `AhoCorasick` constructor | O(P) expected (hash-map children) | O(P) |
| `AhoCorasick.findAll` | O(t + k) expected | O(t + k) |
| `SuffixArray` constructor | O(n log n) | O(n + 65,536) counting table |
| `lcp()`, `suffixes()` | O(n) copy | O(n) |
| `occurrences` | O(p log n + k log k) | O(k) |
| `distinctSubstrings`, `longestRepeatedSubstring` | O(n) | O(1) beyond the result |

`occurrences` sorts its `k` hits into ascending text order, which is where the
`k log k` term comes from; the binary search itself is O(p log n). The number of
matches can be quadratic in the input (`"aaaa..."` against `a`, `aa`, `aaa`, ...),
so O(t + k) is output-sensitive, not O(t).

**Pitfall.** Running KMP once per pattern costs O(t) *per pattern*, so a
dictionary of thousands of words rescans the text thousands of times;
Aho-Corasick is one scan. Conversely, Aho-Corasick answers only the patterns
fixed at construction. For a fixed text and arbitrary later patterns, build a
suffix array. Mind the empty pattern: `SuffixArray.occurrences("")` returns
every boundary `0..n`, like `kmpFindAll`, while `AhoCorasick` refuses it.

Not implemented here: the suffix automaton (a minimal automaton of all
substrings, with online construction) and the Burrows-Wheeler transform with an
FM-index. Both build on the same ideas but are outside this library.

## Practice

1. Trace the failure links for patterns `a`, `ab`, `bab`, `bc`, `bca`, `c`,
   `caa` and list the output link of each terminal node.
2. Using only `suffixes()` and `lcp()`, find the longest substring that occurs at
   least three times. (Hint: look at two consecutive lcp values at once.)
3. Explain why `distinctSubstrings` for `"aaaa"` is 4, and compute the count for
   `"abab"` by hand from the lcp array.

See the [catalog](../../docs/catalog.md) for the source and tests and the
[complexity guide](../../docs/complexity.md) for the shared assumptions.
