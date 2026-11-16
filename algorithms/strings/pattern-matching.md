# Pattern matching: Z-function, Rabin–Karp and Manacher

Use these when one pattern is searched in one text, or when palindromes are the
question. The maintained
[StringAlgorithms](../../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java)
works on UTF-16 code units, never mutates inputs, rejects null and returns
immutable lists. `kmpFindAll`, `zFindAll` and `rabinKarpFindAll` return identical
results: every start index, overlaps included. An empty pattern matches at every
boundary `0..text.length()`.

## Which matcher

| Need | Choice | Why |
| --- | --- | --- |
| One first match in ordinary text | `String.indexOf` | Highly tuned by the JDK; no code to own |
| Guaranteed linear time, small extra space | KMP (`kmpIndexOf`, `kmpFindAll`) | O(t + p), table of size p |
| Prefix-overlap questions, or a matcher built from one array | Z-function (`zFunction`, `zFindAll`) | The z-array also answers borders and periods |
| Many equal-length windows, or a cheap filter before an exact check | Rabin–Karp (`rabinKarpFindAll`) | Rolling hash; every hit is verified |
| Many patterns in one pass | Aho–Corasick | One automaton for the whole dictionary |
| Many queries against one fixed text | Suffix array | Pay once to index the text, then each query is a binary search |
| Sublinear skipping on large alphabets | Boyer–Moore–Horspool | Not implemented here |

## The invariants

**Z-array.** `z[i]` is the length of the longest common prefix of the text and
its suffix starting at `i`; by convention `z[0] = text.length()`. The algorithm
keeps `[left, right)`, the rightmost prefix match seen so far. Inside it, `z[i]`
starts from `min(right - i, z[i - left])` because the window copies the prefix,
and only positions past `right` are compared again, so total work is O(n).

`zFindAll` computes the z-array of `pattern + text`. A position `p + i` with
`z >= p` means the first `p` units of the combined string, which are the pattern,
also start there, so the pattern occurs at text index `i`. No separator is needed
because a match is read only as "at least `p`", never as an exact length.

**Rabin–Karp.** Each window of length `p` has hash
`sum(c[j] * 257^(p-1-j)) mod (2^31 - 1)`. Removing the outgoing unit and appending
the incoming one updates it in O(1). A window is reported only when its hash equals
the pattern hash *and* `startsWith` confirms the units. Operands stay below 2^31
and units below 2^16, so every product fits in a `long`.

**Manacher.** For every center, the algorithm reuses the mirror image inside the
rightmost palindrome `[left, right]` found so far and expands only past `right`.
Odd and even centers are handled by two arrays. Among palindromes of equal length
the earliest start wins.

## Worked trace

Z-array of `aabxaab`:

```text
i  suffix    common prefix with "aabxaab"  z[i]
0  aabxaab   whole string (convention)      7
1  abxaab    a, then b != a                 1
2  bxaab     none                           0
3  xaab      none                           0
4  aab       a a b, then end                3
5  ab        a, then b != a                 1
6  b         none                           0
```

With `pattern = "aab"` and `text = "aabxaab"`, the combined string
`aabaabxaab` has `z >= 3` at combined positions 3 and 7, giving text starts 0
and 4.

## Executable example

```java
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import java.util.List;

assert java.util.Arrays.equals(StringAlgorithms.zFunction("aabxaab"),
        new int[] {7, 1, 0, 0, 3, 1, 0});
assert StringAlgorithms.zFindAll("aabxaab", "aab").equals(List.of(0, 4));
assert StringAlgorithms.zFindAll("aaaaa", "aaa").equals(List.of(0, 1, 2));
assert StringAlgorithms.rabinKarpFindAll("aaaaa", "aaa").equals(List.of(0, 1, 2));
assert StringAlgorithms.rabinKarpFindAll("abc", "").equals(List.of(0, 1, 2, 3));
assert StringAlgorithms.rabinKarpFindAll("abc", "abc").equals(
        StringAlgorithms.kmpFindAll("abc", "abc"));

StringAlgorithms.Span babad = StringAlgorithms.longestPalindromicSubstring("babad");
assert babad.equals(new StringAlgorithms.Span(0, 3));
assert "babad".substring(babad.startInclusive(), babad.endExclusive()).equals("bab");
assert StringAlgorithms.longestPalindromicSubstring("cbbd")
        .equals(new StringAlgorithms.Span(1, 3));
assert StringAlgorithms.longestPalindromicSubstring("")
        .equals(new StringAlgorithms.Span(0, 0));
```

## Costs

| Method | Time | Extra space |
| --- | --- | --- |
| `zFunction` | O(n) | O(n) result |
| `zFindAll` | O(t + p + k) | O(t + p + k): the concatenation and its z-array |
| `rabinKarpFindAll` | Expected O(t + p); worst O(t·p) | O(1) plus the k results |
| `longestPalindromicSubstring` | O(n) | O(n) |

Rabin–Karp's worst case is real: for text `aaaa…` and pattern `aaa`, every window
matches and is verified in full. With the fixed modulus, an adversary who knows it
can also construct many distinct windows with equal hashes.

## Pitfall: trusting the hash

Two different strings can have the same hash. A matcher that stops at
`windowHash == patternHash` reports false positives. The test suite finds two
different seven-unit strings with equal hashes by seeded search and checks that
`rabinKarpFindAll` rejects the impostor. The verification step is what makes the
answer exact; the hash only decides where to spend an O(p) comparison.

Positions are UTF-16 units. A supplementary character is two units, so a match
index may fall between the halves of a pair, and a palindrome is judged unit by
unit rather than by code point.

## Practice

1. Compute the z-array of `abacaba` by hand, then use it to list every border
   (a proper prefix that is also a suffix) of the string.
2. Rolling-hash `abcd` with window 2 using a tiny base and modulus of your choice.
   Find a different pair of windows that collide, and say what the verified matcher
   does with them.
3. Trace Manacher's algorithm on `abaaba`. Which center gives the longest
   palindrome, and what is its half-open span?

Browse the [catalog](../../docs/catalog.md) and the
[complexity guide](../../docs/complexity.md) for the assumptions behind these bounds.
