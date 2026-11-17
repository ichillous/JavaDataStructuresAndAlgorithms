"""String-algorithm figures; fixtures in examples-string.properties.

Every displayed value comes from the fixture file and is asserted against the library (or an
independent model) by VisualExamplesStringTest.
"""

from drawing import ints

CATEGORY = "Strings"

# One row geometry for every character row, so boxes and mirrors line up from step to step.
X, CELL, GAP = 28, 38, 4
PITCH = CELL + GAP
ROW_H = 38


def _cx(index):
    return X + index * PITCH + CELL / 2


def _left(index):
    return X + index * PITCH


def _right(index):
    return X + index * PITCH + CELL


def _chars(c, y, text, states=None, labels=True):
    """A row of the characters of ``text`` with index labels below; returns the y below it."""
    return c.cell_row(X, y, list(text), cell=CELL, gap=GAP, height=ROW_H, states=states,
                      labels=[str(i) for i in range(len(text))] if labels else None)


def _mirror_row(c, y, text, lo, hi, index, mirror, sentence):
    """Characters with the box lo..hi (inclusive) in focus, the current index, its mirror, a
    curve between them and one explanatory sentence; returns the y below the sentence."""
    states = []
    for i in range(len(text)):
        if i == index:
            states.append("alt")
        elif i == mirror:
            states.append("good")
        elif lo <= i <= hi:
            states.append("focus")
        else:
            states.append(None)
    row = y + 30
    c.curve(_cx(index), row - 2, _cx(mirror), row - 2, bend=-14, color="violet")
    y = _chars(c, row, text, states=states)
    c.text(X, y + 22, sentence, size=18, color="muted")
    return y + 22


# ---------------------------------------------------------------------------------- z-box

Z_IDEA = ("Inside a Z-box, z[i] comes from its mirror; only the edge costs comparisons.")


def z_box(c, f):
    text = f["string.z.text"]
    z = ints(f, "string.z.array")
    start, end = int(f["string.z.boxStart"]), int(f["string.z.boxEnd"])
    copy_i, copy_m, copy_v = (int(f["string.z.copyIndex"]), int(f["string.z.copyMirror"]),
                              int(f["string.z.copyValue"]))
    cap_i, cap_m, cap_mv, cap_v = (int(f["string.z.capIndex"]), int(f["string.z.capMirror"]),
                                   int(f["string.z.capMirrorValue"]), int(f["string.z.capValue"]))
    n = len(text)

    # 1. The Z-array; the match at `start` is the Z-box.
    y = c.step(156, 1, f"Z-array of {text}")
    c.brace(_left(0), _right(end - start - 1), y + 20, f"prefix [0, {end - start})", color="green",
            above=True)
    box = ["focus" if start <= i < end else None for i in range(n)]
    y = _chars(c, y + 32, text, states=box)
    y = c.cell_row(X, y + 4, [str(v) for v in z], cell=CELL, gap=GAP, height=ROW_H,
                   states=["focus" if i == start else None for i in range(n)])

    # 2. Index inside the box whose mirror value fits.
    y = c.step(y + 32, 2, f"Index {copy_i} reads its mirror {copy_m}")
    y = _mirror_row(c, y, text, start, end - 1, copy_i, copy_m,
                    f"z[{copy_m}] = {copy_v} fits in {end - copy_i} left, so z[{copy_i}] = {copy_v}.")

    # 3. Index whose mirror value overruns the box.
    y = c.step(y + 32, 3, f"Index {cap_i} reads its mirror {cap_m}")
    y = _mirror_row(c, y, text, start, end - 1, cap_i, cap_m,
                    f"z[{cap_m}] = {cap_mv} exceeds {end - cap_i} left, so z[{cap_i}] starts at {cap_v}.")

    y = c.legend(y + 32, [("focus", f"Z-box, z[{start}] = {z[start]}"), ("alt", "current index"), ("good", "mirror")])
    c.note(y + 14, Z_IDEA)


# --------------------------------------------------------------------------- rabin-karp-rolling

RK_IDEA = "Hash hits only choose where to compare; verification rejects impostors."


def rabin_karp_rolling(c, f):
    text, pattern = f["string.rk.text"], f["string.rk.pattern"]
    p = len(pattern)
    pattern_hash = int(f["string.rk.patternHash"])
    hashes = [int(v) for v in f["string.rk.windowHashes"].split(",")]
    impostor, match = int(f["string.rk.impostorStart"]), int(f["string.rk.matchStart"])
    mismatch = int(f["string.rk.mismatchUnit"])
    base, modulus = int(f["string.rk.base"]), int(f["string.rk.modulus"])

    # 1. Slide the window one unit at a time; each hash comes from the previous in O(1).
    y = c.step(156, 1, f"Slide a {p}-unit window over {text}")
    c.text(X, y + 8, f"pattern {pattern}, hash {pattern_hash}", weight="700")
    c.text(X, y + 36, f"hash = sum(unit * {base}^(p-1-j)) mod {modulus}", size=18, color="muted")
    cols = {"start": 36, "window": 96, "hash": 192, "result": 340}
    head = y + 74
    for name, label in (("start", "start"), ("window", "window"), ("hash", "hash"),
                        ("result", "vs pattern hash")):
        c.text(cols[name], head, label, size=18, color="muted")
    top = head + 12
    for start, value in enumerate(hashes):
        row = top + start * 38
        hit = value == pattern_hash
        c.rect(X, row, 464, 34, fill="note" if hit else "paper", stroke="gold" if hit else "line", radius=6)
        c.text(cols["start"] + 8, row + 24, start)
        c.text(cols["window"], row + 24, text[start:start + p], weight="700" if hit else "400")
        c.text(cols["hash"], row + 24, value, weight="700" if hit else "400")
        c.text(cols["result"], row + 24, "equal" if hit else "differs", weight="700" if hit else "400",
               color="gold" if hit else "muted")
    y = top + len(hashes) * 38
    c.text(X, y + 24, "Each hash drops one unit and adds one: O(1).", size=18, color="muted")

    # 2. Verify each hit.
    y = c.step(y + 66, 2, "Compare the units of each hit")
    window = text[impostor:impostor + p]
    w = c.chip(X, y - 4, f"reject {impostor}", color="red")
    c.text(X + w + 10, y + 17, f"{window} has unit {mismatch} {window[mismatch]}, not {pattern[mismatch]}",
           size=18)
    w = c.chip(X, y + 40, f"report {match}", color="green")
    c.text(X + w + 10, y + 61, f"{text[match:match + p]} equals the pattern", size=18)
    y += 80

    y = c.legend(y + 26, [("warn", "hash equals"), ("bad", "rejected"), ("good", "confirmed")])
    c.note(y + 14, RK_IDEA)


# ------------------------------------------------------------------------------ manacher-mirror

MAN_IDEA = "Inside a palindrome a center copies its mirror's radius, capped at the edge."


def manacher_mirror(c, f):
    text = f["string.man.text"]
    radii = ints(f, "string.man.radii")
    center, left, right = int(f["string.man.center"]), int(f["string.man.left"]), int(f["string.man.right"])
    fit_i, fit_m = int(f["string.man.fitIndex"]), int(f["string.man.fitMirror"])
    cap_i, cap_m = int(f["string.man.capIndex"]), int(f["string.man.capMirror"])
    cap_mr, cap_start = int(f["string.man.capMirrorRadius"]), int(f["string.man.capStart"])
    n = len(text)

    # 1. Odd radii; the widest palindrome so far is the mirror box.
    y = c.step(156, 1, f"Odd radii of {text}")
    c.brace(_left(left), _right(right), y + 20, f"palindrome [{left}, {right}]", color="blue", above=True)
    y = _chars(c, y + 32, text, states=["focus" if left <= i <= right else None for i in range(n)])
    y = c.cell_row(X, y + 4, [str(r) for r in radii], cell=CELL, gap=GAP, height=ROW_H,
                   states=["focus" if i == center else None for i in range(n)])

    # 2. Radius fits inside.
    y = c.step(y + 32, 2, f"Center {fit_i} reads its mirror {fit_m}")
    y = _mirror_row(c, y, text, left, right, fit_i, fit_m,
                    f"radius {radii[fit_m]} fits in {right - fit_i + 1} left, so r = {radii[fit_i]}.")

    # 3. Radius overruns the right edge.
    y = c.step(y + 32, 3, f"Center {cap_i} reads its mirror {cap_m}")
    y = _mirror_row(c, y, text, left, right, cap_i, cap_m,
                    f"radius {cap_mr} exceeds {cap_start} left, so start at {cap_start}.")

    y = c.legend(y + 32, [("focus", f"palindrome, radius {radii[center]} at {center}"), ("alt", "current center"), ("good", "mirror")])
    c.note(y + 14, MAN_IDEA)


# ---------------------------------------------------------------------------- aho-corasick-links

AC_IDEA = "Failure links keep the scan moving forward: no text unit is read twice."

TRIE_X = {"": 260, "s": 80, "sh": 80, "she": 80, "h": 330, "he": 250, "her": 250, "hers": 250,
          "hi": 410, "his": 410}


def aho_corasick_links(c, f):
    patterns = f["string.ac.patterns"].split(",")
    text = f["string.ac.text"]
    nodes = f["string.ac.nodes"].split(",")
    failure = dict(link.split(">") for link in f["string.ac.failure"].split(","))
    kept = f["string.ac.kept"].split(",")
    matches = [entry.split(":") for entry in f["string.ac.matches"].split(",")]

    # 1. The trie and its failure links.
    y = c.step(156, 1, f"Trie of {', '.join(patterns)}")
    top, gap, radius = y + 30, 64, 27
    pos = {name: (TRIE_X[name], top + len(name) * gap) for name in [""] + nodes}
    for name in nodes:
        c.edge(pos[name[:-1]], pos[name], color="line", radius=radius, directed=False)
    c.node(*pos[""], "root", radius=radius, state="muted")
    for name in nodes:
        c.node(*pos[name], name, radius=radius, state="good" if name in patterns else None)
    for name, target in failure.items():
        nx, ny = pos[name]
        if name == "his":
            c.chip(nx - 40, ny + radius + 6, f"link {target}", color="violet")
        else:
            c.chip(nx + radius + 8, ny - 15, f"link {target}", color="violet")
    y = top + 4 * gap + radius

    # 2. Scan the text.
    y = c.step(y + 28, 2, f"Scan {text}, keep the longest prefix")
    c.cell_row(X, y, list(text), cell=60, gap=10)
    y += 56
    c.cell_row(X, y, kept, cell=60, gap=10, states=["good" if k in patterns else None for k in kept])
    y += 82
    left = X
    for start, pattern in matches:
        left += c.chip(left, y + 6, f"{pattern} at {start}", color="green") + 10
    y += 40

    y = c.legend(y + 26, [("good", "pattern ends"), ("alt", "failure link; rest go to root")])
    c.note(y + 14, AC_IDEA)


# --------------------------------------------------------------------------- suffix-array-lcp

SA_IDEA = "Suffixes sharing a prefix sit side by side: a lookup is one range."


def suffix_array_lcp(c, f):
    text = f["string.sa.text"]
    suffixes = ints(f, "string.sa.suffixes")
    lcp = ints(f, "string.sa.lcp")
    pattern = f["string.sa.pattern"]
    low, high = int(f["string.sa.low"]), int(f["string.sa.high"])
    hits = ints(f, "string.sa.occurrences")
    n = len(text)

    # 1. Sorted suffixes with the lcp of each neighbor pair.
    y = c.step(156, 1, f"Suffixes of {text}, sorted")
    rows = [[suffixes[r], text[suffixes[r]:], lcp[r]] for r in range(n)]
    y = c.grid(96, y + 36, rows, cell_w=100, cell_h=38, row_labels=[f"rank {r}" for r in range(n)],
               col_labels=["start", "suffix", "lcp"])

    # 2. The pattern is a rank range.
    y = c.step(y + 44, 2, f"{pattern} is the rank range [{low}, {high})")
    row = y + 16
    cell, gap = 72, 4
    values = [text[suffixes[r]:] for r in range(n)]
    states = ["good" if low <= r < high else None for r in range(n)]
    y = c.cell_row(X, row, values, cell=cell, gap=gap, states=states,
                   labels=[str(r) for r in range(n)])
    c.brace(X + low * (cell + gap), X + (high - 1) * (cell + gap) + cell, y - 2,
            f"low {low}, high {high}", color="green")
    c.text(X, y + 56, "Starts " + ", ".join(str(suffixes[r]) for r in range(low, high))
           + " sorted: " + ", ".join(str(h) for h in hits), size=18, weight="700")
    y += 56

    y = c.legend(y + 30, [("good", f"suffix starts with {pattern}")])
    c.note(y + 14, SA_IDEA)


# ---------------------------------------------------------------------------------- registry

FIGURES = [
    {
        "id": "z-box", "title": "Z-array and the Z-box", "category": CATEGORY,
        "description": ("The Z-array of aabxaabxaab is 11, 1, 0, 0, 7, 1, 0, 0, 3, 1, 0; the match at "
                        "index 4 is a Z-box [4, 11), index 5 copies z[1] = 1 from it, and index 8 "
                        "starts at 3 because z[4] = 7 overruns the box."),
        "keyIdea": Z_IDEA, "lesson": "algorithms/strings/pattern-matching.md", "height": 890,
        "draw": z_box,
    },
    {
        "id": "rabin-karp-rolling", "title": "Rabin-Karp: hash, then verify", "category": CATEGORY,
        "description": ("Searching aeaecbagab for bagab slides a five-unit window; windows 0 and 5 "
                        "have the pattern's hash, window 0 (aeaec) is rejected at unit 0 and window "
                        "5 is reported."),
        "keyIdea": RK_IDEA, "lesson": "algorithms/strings/pattern-matching.md", "height": 845,
        "draw": rabin_karp_rolling,
    },
    {
        "id": "manacher-mirror", "title": "Manacher: reuse the mirror", "category": CATEGORY,
        "description": ("In cabacabad the palindrome abacaba around index 4 spans [1, 7]; index 5 "
                        "copies radius 1 from index 3 and index 6 caps radius 3 from index 2 at the "
                        "2 units left, giving odd radii 1, 1, 3, 1, 4, 1, 2, 1, 1."),
        "keyIdea": MAN_IDEA, "lesson": "algorithms/strings/pattern-matching.md", "height": 895,
        "draw": manacher_mirror,
    },
    {
        "id": "aho-corasick-links", "title": "Aho-Corasick links",
        "category": CATEGORY,
        "description": ("The trie of he, she, his and hers has failure links, labelled beside their nodes, sh to h, she to he, "
                        "his to s and hers to s; scanning ushers keeps s, sh, she, her, hers and "
                        "reports she at 1, he at 2 and hers at 2."),
        "keyIdea": AC_IDEA, "lesson": "algorithms/strings/multi-pattern-and-suffix-arrays.md",
        "height": 900, "draw": aho_corasick_links,
    },
    {
        "id": "suffix-array-lcp", "title": "Suffix array and a pattern range", "category": CATEGORY,
        "description": ("The sorted suffixes of banana start at 5, 3, 1, 0, 4, 2 with lcp 0, 1, 3, 0, 0, 2; "
                        "the pattern ana is the rank range [1, 3), whose starts 3 and 1 sort to 1, 3."),
        "keyIdea": SA_IDEA, "lesson": "algorithms/strings/multi-pattern-and-suffix-arrays.md",
        "height": 850, "draw": suffix_array_lcp,
    },
]
