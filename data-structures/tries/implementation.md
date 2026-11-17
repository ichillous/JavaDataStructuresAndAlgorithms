# Trie updates: prune paths without deleting other words

[`Trie`](../../src/main/java/io/github/ichillous/dsa/structures/Trie.java) owns its nodes and exposes word operations rather than mutable node references. Ordered child maps give deterministic suggestions; iterative traversal avoids one Java call frame per character.

| At a glance | |
| --- | --- |
| **Use it when** | You need to know exactly how a trie inserts, prunes on delete and collects suggestions with a limit. |
| **Avoid it when** | You want a high-level overview first; start with [tries](overview.md). |
| **Cost** | Add, remove and lookup O(L log A); suggestions O(P log A) plus visited nodes and output characters. |
| **In this repo** | [`Trie.add`, `remove` and `wordsWithPrefix`](../../src/main/java/io/github/ichillous/dsa/structures/Trie.java) |
| **In the JDK** | no direct equivalent; see `TreeSet#tailSet` in [tries](overview.md) |

<!-- visual:generated trie-pruning -->
<picture><source media="(prefers-color-scheme: dark)" srcset="../../docs/visuals/assets/trie-pruning-dark.svg"><img src="../../docs/visuals/assets/trie-pruning.svg" alt="The words car, cart and cat share a c-a prefix. Deleting car clears the r node&#x27;s word marker but preserves its t child for cart and the sibling t branch for cat." width="520"></picture>

**Figure 09.** The words car, cart and cat share a c-a prefix. Deleting car clears the r node's word marker but preserves its t child for cart and the sibling t branch for cat. [Visual index and step-throughs](../../docs/visuals/README.md).
<!-- /visual:generated -->

## Insert and delete have different stopping rules

Insertion follows existing edges and creates only missing suffix nodes. Marking an already marked endpoint is a duplicate, so size does not increase.

Deletion first finds the complete word. If its endpoint is absent or unmarked, return false without changing the trie. Otherwise unmark it, decrease size once, and walk the saved path backward. Prune a node only when it has **no children and no word marker**.

```text
before: c -- a -- r* -- t*     words car, cart
remove car:
        c -- a -- r  -- t*     retain path to cart
remove cart:
        root                  remove unused suffix all the way back
```

The helper's question “may this node be pruned?” is different from the public result “was a word removed?”. Confusing those results can report failure even after correctly removing a word that shares a prefix.

```java
import io.github.ichillous.dsa.structures.Trie;
import java.util.List;

Trie words = new Trie();
words.add("car");
words.add("cart");
words.add("cat");
boolean removed = words.remove("car");
assert removed && !words.contains("car");
assert words.contains("cart") && words.contains("cat");
assert words.wordsWithPrefix("ca", 10).equals(List.of("cart", "cat"));
boolean absent = words.remove("car");
assert !absent && words.size() == 2;
```

A fixed child array's `length` is its capacity, not its live child count. Checking `children.length == 0` on a 26-slot alphabet array never detects an empty node. The maintained implementation checks whether its child map is empty.

## Suggestions keep traversal state explicitly

Find the prefix endpoint, then visit child entries in sorted order. A stack of iterator frames remembers unfinished child scans. A shared `StringBuilder` holds the current path; popping a frame restores the parent's length before trying the next branch.

```java
import io.github.ichillous.dsa.structures.Trie;
import java.util.List;

Trie words = new Trie();
for (String word : List.of("", "ab", "abc", "abd", "b")) words.add(word);
assert words.wordsWithPrefix("", 3).equals(List.of("", "ab", "abc"));
assert words.wordsWithPrefix("ab", 0).isEmpty();
assert words.wordsWithPrefix("missing", 5).isEmpty();
assert words.wordsWithPrefix("ab", 9).equals(List.of("ab", "abc", "abd"));
```

<!-- visual:generated trie-prefix-walk -->
<picture><source media="(prefers-color-scheme: dark)" srcset="../../docs/visuals/assets/trie-prefix-walk-dark.svg"><img src="../../docs/visuals/assets/trie-prefix-walk.svg" alt="For the words car, card, care, cart, cat and dog, wordsWithPrefix(&quot;ca&quot;, 3) walks the c and a nodes, then emits car, card and care in sorted order and stops before cart and cat; the dog branch is never visited." width="520"></picture>

**Figure 29.** For the words car, card, care, cart, cat and dog, wordsWithPrefix("ca", 3) walks the c and a nodes, then emits car, card and care in sorted order and stops before cart and cat; the dog branch is never visited. [Visual index and step-throughs](../../docs/visuals/README.md).
<!-- /visual:generated -->

Negative limits are rejected. A zero limit returns no results. Results are detached immutable snapshots; later updates do not change a previous suggestion list. Failing to shorten the path builder when backtracking can silently concatenate sibling labels into nonexistent words.

## Character model is part of correctness

The trie follows Java `char` values, not Unicode grapheme clusters. A supplementary symbol needs two code units, and canonically equivalent spellings remain distinct without normalization.

```java
import io.github.ichillous.dsa.structures.Trie;

String symbol = "🙂";
Trie words = new Trie();
words.add(symbol);
words.add("Car");
words.add("car");
assert symbol.length() == 2;
assert words.contains(symbol);
assert words.size() == 3;
assert !words.contains("CAR");
```

Lookup and update take O(L log A) time for word length L and maximum branching A; removal saves O(L) path state. Suggestions cost O(P log A + visited nodes + output characters) time, with traversal workspace and output storage, for prefix length P. Long words still consume memory even with iterative traversal.

## In real Java

The trie compares UTF-16 code units and does no normalization, so `"\u00e9"` (one precomposed character) and `"e\u0301"` (`e` plus a combining accent) are different words. `java.text.Normalizer` converts both to one canonical form; normalize every word on the way in and every query on the way out.

```java
import io.github.ichillous.dsa.structures.Trie;
import java.text.Normalizer;

String composed = "\u00e9";
String decomposed = "e\u0301";
Trie raw = new Trie();
raw.add(composed);
assert !raw.contains(decomposed);

Trie normalized = new Trie();
normalized.add(Normalizer.normalize(composed, Normalizer.Form.NFC));
assert normalized.contains(Normalizer.normalize(decomposed, Normalizer.Form.NFC));
```

## Common mistakes

1. **Pruning on the word flag alone.** Removing `car` from `{car, cart}` and then deleting every unmarked node deletes the node after `r`, and `cart` with it. A node may be pruned only when it has no children and no word marker, which keeps the path of every stored word intact.
2. **Not shortening the path builder when backtracking.** With words `ab` and `ac`, leaving the builder at `ab` when moving to the sibling `c` appends to give `abc`, a word that was never stored. Restore the builder to the parent's length before each next child.
3. **Checking the limit once per subtree.** For `car`, `card` and `care` with limit 1, testing the limit only after finishing a whole child subtree returns all three. The size check belongs after every emitted word, so the result never exceeds the limit.

## Check yourself

<details>
<summary>Why does `remove` return false and change nothing for a stored prefix that is not a word?</summary>

Removal needs a marked endpoint. For `ca` in `{car, cart}` the node exists but is unmarked, so there is no word to remove, size must not change, and no node may be pruned.

</details>

<details>
<summary>In the figure, why are `cart` and `cat` never visited when the limit is 3?</summary>

The walk emits `car`, `card` and `care` in sorted order and stops as soon as the result holds three words. The nodes for `cart` and `cat` come later in sorted order and are never reached.

</details>

<details>
<summary>Why does the traversal keep a stack of iterators instead of recursing?</summary>

Each frame holds the unfinished child scan of one node, so depth costs heap memory rather than one Java call frame per character. A very long word therefore cannot overflow the call stack.

</details>

## Practice

1. Remove `ab` from the suggestion example; state which descendants remain and what size becomes.
2. Retain an old suggestion list, mutate the trie, and check snapshot isolation and immutability.
3. Compare suggestions with a lexicographically sorted reference set across shared prefixes, the empty word, uppercase letters and supplementary symbols. Add a very long word to exercise iterative depth handling.

Use the [worked solutions](../../docs/solutions.md) to verify pruning and backtracking invariants.
