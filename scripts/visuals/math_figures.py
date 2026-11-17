"""Math, randomness and bit-manipulation figures; fixtures in examples-math.properties.

Every value drawn comes from the fixture file, and ``VisualExamplesMathTest`` re-derives the
same values from ``NumberTheory``, ``Sampling``, ``AliasTable`` and the JDK.
"""

from fractions import Fraction

from drawing import ints

CATEGORY = "Math and bits"
MINUS = "−"


def _fractions(fixtures, key):
    return [Fraction(part) for part in fixtures[key].split(",")]


def _fmt(value):
    value = Fraction(value)
    return str(value.numerator) if value.denominator == 1 else f"{value.numerator}/{value.denominator}"


def _combination(pairs):
    """Render ``coef x value`` terms such as ``2x46 - 9x10``: positive terms first, ``x1`` omitted."""
    ordered = sorted(pairs, key=lambda pair: pair[0] < 0)
    out = ""
    for coefficient, value in ordered:
        magnitude = abs(coefficient)
        term = f"{value}" if magnitude == 1 else f"{magnitude}×{value}"
        if not out:
            out = (MINUS if coefficient < 0 else "") + term
        else:
            out += f" {MINUS if coefficient < 0 else '+'} {term}"
    return out


# ---------------------------------------------------------------- euclid-steps

EUCLID_IDEA = "The last nonzero remainder is the gcd, and undoing each division writes it as x×a + y×b."


def euclid_steps(c, fixtures):
    a, b = int(fixtures["math.euclid.a"]), int(fixtures["math.euclid.b"])
    quotients = ints(fixtures, "math.euclid.quotients")
    remainders = ints(fixtures, "math.euclid.remainders")
    gcd = int(fixtures["math.euclid.gcd"])
    x, y = int(fixtures["math.euclid.x"]), int(fixtures["math.euclid.y"])
    left = ints(fixtures, "math.euclid.back.left")
    right = ints(fixtures, "math.euclid.back.right")
    left_coef = ints(fixtures, "math.euclid.back.leftCoef")
    right_coef = ints(fixtures, "math.euclid.back.rightCoef")

    # 1. Divide, then divide the divisor by the remainder, until the remainder is 0.
    y0 = c.step(156, 1, "Divide again with the remainder")
    rows = [[remainders[i], quotients[i], remainders[i + 1], remainders[i + 2]]
            for i in range(len(quotients))]
    last = len(rows) - 1
    states = {(last - 1, 3): "good", (last, 2): "good", (last, 3): "muted"}
    grid_x = 60
    y0 = c.grid(grid_x, y0 + 28, rows, cell_w=100, cell_h=34,
                col_labels=["dividend", "quotient", "divisor", "remainder"], states=states)
    c.text(grid_x, y0 + 26, "dividend = quotient × divisor + remainder", size=18, color="muted")

    # 2. Substitute each remainder away, from the last division back to the first.
    y1 = c.step(y0 + 72, 2, "Substitute back, last remainder first")
    base = y1 + 12
    for row in range(len(left)):
        baseline = base + row * 30
        equation = f"{gcd} = " + _combination([(left_coef[row], left[row]), (right_coef[row], right[row])])
        final = row == len(left) - 1
        c.text(48, baseline, equation, color="good" if final else "ink", weight="700" if final else "400")
        if row == 0:
            side = f"from {left[0]} = {quotients[len(quotients) - 2]}×{right[0]} + {gcd}"
        else:
            step_q = quotients[len(left) - 1 - row]
            side = f"{right[row - 1]} → {left[row]} {MINUS} " + (
                f"{step_q}×{right[row]}" if step_q != 1 else f"{right[row]}")
        c.text(310, baseline, side, size=18, color="muted")
    end = base + (len(left) - 1) * 30 + 34
    c.text(48, end, f"extendedGcd({a}, {b}) returns x = {str(x).replace('-', MINUS)}, y = {y}",
           size=18, color="muted")

    y = c.legend(end + 34, [("good", f"gcd = {gcd}, last nonzero remainder"), ("muted", "remainder 0: stop")])
    c.note(y + 14, EUCLID_IDEA)


# --------------------------------------------------------------- modpow-squares

MODPOW_IDEA = "Square the base once per exponent bit and multiply it in only for 1 bits: O(log e) steps."


def modpow_squares(c, fixtures):
    base = int(fixtures["math.modpow.base"])
    exponent = int(fixtures["math.modpow.exponent"])
    modulus = int(fixtures["math.modpow.modulus"])
    bits = ints(fixtures, "math.modpow.bits")
    squares = ints(fixtures, "math.modpow.squares")
    results = ints(fixtures, "math.modpow.results")
    answer = int(fixtures["math.modpow.answer"])
    product = int(fixtures["math.modpow.product"])
    binary = "".join(str(bit) for bit in reversed(bits))
    parts = [1 << i for i, bit in enumerate(bits) if bit]

    # 1. The exponent's bits, lowest first: the order modPow consumes them.
    y = c.step(156, 1, f"{exponent} is {binary} in binary")
    states = ["good" if bit else "muted" for bit in bits]
    y = c.cell_row(61, y, bits, states=states, labels=[f"bit {i}" for i in range(len(bits))])
    c.text(61, y + 26, f"{exponent} = " + " + ".join(str(p) for p in parts), size=18, color="muted")

    # 2. Square every step; multiply into the result when the bit is 1.
    y = c.step(y + 70, 2, f"Square {base} each step, multiply on 1 bits")
    rows = [[bits[i], squares[i], results[i]] for i in range(len(bits))]
    cell_states = {}
    for i, bit in enumerate(bits):
        cell_states[(i, 1)] = "focus" if bit else "muted"
        cell_states[(i, 2)] = "good" if bit else "muted"
    y = c.grid(150, y + 32, rows, cell_w=100, cell_h=36,
               row_labels=[f"bit {i}" for i in range(len(bits))],
               col_labels=["bit", f"{base}^(2^i)", "result"], states=cell_states)

    # 3. The answer is the product of the squares chosen by 1 bits, reduced once at the end.
    y = c.step(y + 32, 3, f"{base}^{exponent} mod {modulus} = {answer}")
    chosen = [squares[i] for i, bit in enumerate(bits) if bit]
    c.chip(61, y + 4, " × ".join(str(v) for v in chosen) + f" = {product}", color="green")
    y = c.legend(y + 64, [("focus", "square multiplied in"), ("good", "result updated"),
                          ("muted", "bit 0: skipped")])
    c.note(y + 14, MODPOW_IDEA)


# ------------------------------------------------------------------ sieve-grid

SIEVE_IDEA = "A prime p starts crossing at p×p, because smaller multiples have a smaller prime factor."
COLUMNS = 5


def sieve_grid(c, fixtures):
    limit = int(fixtures["math.sieve.limit"])
    starts = ints(fixtures, "math.sieve.starts")
    marks = ints(fixtures, "math.sieve.newMarks")
    crossed = ints(fixtures, "math.sieve.crossedBy")
    primes = ints(fixtures, "math.sieve.primes")
    sieving = [2, 3, 5]
    past = 7 * 7

    # 1. Each prime starts at its square; earlier multiples already have a smaller factor.
    y = c.step(156, 1, "Each prime starts at its square")
    baseline = y + 12
    for index, prime in enumerate(sieving):
        skipped = ints(fixtures, f"math.sieve.skipped.{prime}") if f"math.sieve.skipped.{prime}" in fixtures else []
        line = f"p = {prime}: start at {starts[index]}"
        if skipped:
            line += ", skip " + ", ".join(str(s) for s in skipped)
        c.text(48, baseline, line, weight="700")
        c.text(492, baseline, f"{marks[index]} new", size=18, color="muted", anchor="end")
        baseline += 30
    c.text(48, baseline, f"7×7 = {past} is past {limit}: stop", color="muted")

    # 2. The result: every number labelled with the prime that first crossed it out.
    y = c.step(baseline + 46, 2, "Crossed by its smallest prime")
    by_state = {2: "bad", 3: "alt", 5: "warn"}
    rows, states = [], {}
    for n in range(1, limit + 1):
        row, column = divmod(n - 1, COLUMNS)
        if column == 0:
            rows.append([])
        rows[row].append(n)
        if n == 1:
            states[(row, column)] = "muted"
        elif n in primes:
            states[(row, column)] = "good"
        else:
            states[(row, column)] = by_state[crossed[n - 1]]
    y = c.grid(100, y + 14, rows, cell_w=64, cell_h=40, states=states)
    y = c.legend(y + 34, [("good", "prime"), ("bad", "crossed by 2"), ("alt", "crossed by 3"),
                          ("warn", "crossed by 5"), ("muted", "1: neither")])
    c.note(y + 14, SIEVE_IDEA)


# ---------------------------------------------------------- reservoir-sampling

RESERVOIR_IDEA = ("Arrival i is kept with chance k/i, so every item seen so far ends up in the "
                  "sample with equal chance.")


def reservoir_sampling(c, fixtures):
    items = fixtures["math.reservoir.items"].split(",")
    k = int(fixtures["math.reservoir.k"])
    seed = int(fixtures["math.reservoir.seed"])
    draws = ints(fixtures, "math.reservoir.draws")
    slots = {i: fixtures[f"math.reservoir.slots.{i}"].split(",") for i in range(k, len(items) + 1)}

    # 1. The first k items fill the reservoir without any draw.
    y = c.step(156, 1, f"The first {k} items fill the reservoir")
    y = c.cell_row(61, y, slots[k], states=["good"] * k, labels=[f"slot {i}" for i in range(k)])

    # 2. Later arrival i draws j in [0, i): j < k replaces slot j, else the item is dropped.
    y = c.step(y + 30, 2, "Arrival i draws j below i")
    rows, states, labels = [], {}, []
    for index, arrival in enumerate(range(k + 1, len(items) + 1)):
        j = draws[index]
        rows.append([f"{k}/{arrival}", j] + slots[arrival])
        labels.append(f"{items[arrival - 1]} #{arrival}")
        states[(index, 1)] = "focus" if j < k else "muted"
        if j < k:
            states[(index, 2 + j)] = "focus"
    y = c.grid(122, y + 36, rows, cell_w=70, cell_h=36, row_labels=labels,
               col_labels=["k/i", "j"] + [f"slot {i}" for i in range(k)], states=states)

    # 3. The library's answer for this seed.
    y = c.step(y + 36, 3, f"Result for seed {seed}")
    c.chip(66, y + 4, "[" + ", ".join(slots[len(items)]) + "]", color="green")
    y = c.legend(y + 66, [("focus", f"j < {k}: written here"), ("muted", f"j ≥ {k}: item dropped")])
    c.note(y + 14, RESERVOIR_IDEA)


# ----------------------------------------------------------------- alias-table

ALIAS_IDEA = ("Equal columns split between two items let one uniform column pick and one coin flip "
              "draw any weight.")
ITEM_LOOK = [("blue", "soft"), ("green", "mint"), ("violet", "lilac"), ("gold", "note")]


def alias_table(c, fixtures):
    names = fixtures["math.alias.names"].split(",")
    weights = ints(fixtures, "math.alias.weights")
    probability = _fractions(fixtures, "math.alias.probability")
    cutoff = _fractions(fixtures, "math.alias.cutoff")
    alias = ints(fixtures, "math.alias.alias")
    n, total = len(names), sum(weights)
    cell, gap, left = 100, 16, 36

    # 1. Weights, and the probability each one stands for.
    y = c.step(156, 1, f"Weights sum to {total}")
    states = [("focus", "good", "alt", "warn")[i] for i in range(n)]
    y = c.cell_row(left, y, weights, cell=cell, gap=gap, states=states,
                   labels=[f"{names[i]}: {_fmt(probability[i])}" for i in range(n)])

    # 2. Columns of equal height: the item below, its alias above.
    y = c.step(y + 42, 2, "Each column: item below, alias above")
    top, height = y + 40, 120
    c.brace(left, left + n * cell + (n - 1) * gap, top - 14, f"{n} columns of equal height",
            color="blue", above=True)
    for column in range(n):
        x = left + column * (cell + gap)
        own = height * float(cutoff[column])
        _fill_segment(c, x, top + height - own, cell, own, column, f"{names[column]} {_fmt(cutoff[column])}")
        if cutoff[column] < 1:
            other = alias[column]
            _fill_segment(c, x, top, cell, height - own, other,
                          f"{names[other]} {_fmt(1 - cutoff[column])}")
        c.text(x + cell / 2, top + height + 28, f"cutoff {_fmt(cutoff[column])}", anchor="middle",
               size=18, color="muted")

    # 3. Add each item's pieces back up: the mass matches its probability.
    y = c.step(top + height + 78, 3, f"Add each item's pieces, divide by {n}")
    baseline = y + 12
    for j in range(n):
        pieces = [cutoff[j]] + [1 - cutoff[i] for i in range(n) if i != j and alias[i] == j]
        total_mass = sum(pieces)
        text = " + ".join(_fmt(p) for p in pieces)
        expression = f"({text})" if len(pieces) > 1 else text
        c.text(48, baseline, f"{names[j]}: {expression} ÷ {n} = {_fmt(total_mass / n)}",
               color=ITEM_LOOK[j][0], weight="700")
        baseline += 28
    c.note(baseline - 4, ALIAS_IDEA)


def _fill_segment(c, x, y, width, height, item, label):
    stroke, fill = ITEM_LOOK[item]
    c.rect(x, y, width, height, fill=fill, stroke=stroke, radius=3)
    c.text(x + width / 2, y + height / 2 + 7, label, anchor="middle", weight="700", color=stroke)


# -------------------------------------------------------------- bitmask-subsets

BITS_IDEA = ("A set is an int: test, set and clear bits in one operation, and (sub − 1) & mask "
             "visits every subset.")


def bitmask_subsets(c, fixtures):
    width = int(fixtures["math.bits.width"])
    mask = int(fixtures["math.bits.mask"])
    test_bit, test_result = int(fixtures["math.bits.testBit"]), int(fixtures["math.bits.testResult"])
    set_bit, set_result = int(fixtures["math.bits.setBit"]), int(fixtures["math.bits.setResult"])
    clear_bit, clear_result = int(fixtures["math.bits.clearBit"]), int(fixtures["math.bits.clearResult"])
    subsets = ints(fixtures, "math.bits.subsets")
    minus_one = ints(fixtures, "math.bits.minusOne")

    def binary(value):
        return format(value, f"0{width}b")

    members = [i for i in range(width) if mask >> i & 1]
    high_first = list(range(width - 1, -1, -1))

    # 1. A set of small integers stored as the bits of one int.
    y = c.step(156, 1, "A set as one int")
    states = ["focus" if mask >> i & 1 else "muted" for i in high_first]
    y = c.cell_row(60, y, [mask >> i & 1 for i in high_first], states=states,
                   labels=[f"bit {i}" for i in high_first])
    c.text(346, y - 52, f"mask = {mask}", weight="700", color="blue")
    c.text(346, y - 24, "set {" + ", ".join(str(m) for m in members) + "}", size=18, color="muted")

    # 2. Test, set and clear one bit.
    y = c.step(y + 38, 2, "Test, set and clear a bit")
    baseline = y + 12
    c.text(48, baseline, f"test bit {test_bit}: ({mask} >> {test_bit}) & 1 = {test_result}")
    c.text(48, baseline + 30,
           f"set bit {set_bit}: {binary(mask)} | {binary(1 << set_bit)} = {binary(set_result)}")
    clear_mask = ((1 << width) - 1) & ~(1 << clear_bit)
    c.text(48, baseline + 60,
           f"clear bit {clear_bit}: {binary(mask)} & {binary(clear_mask)} = {binary(clear_result)}")

    # 3. Walk every subset of the mask, largest first, without touching bits outside it.
    y = c.step(baseline + 104, 3, f"Walk subsets: (sub {MINUS} 1) & mask")
    rows, states, labels = [], {}, []
    for index, sub in enumerate(subsets):
        rows.append([sub >> i & 1 for i in high_first])
        labels.append(str(sub))
        for column, bit in enumerate(high_first):
            if not mask >> bit & 1:
                states[(index, column)] = "muted"
            elif sub >> bit & 1:
                states[(index, column)] = "focus"
    grid_x = 100
    top = y + 34
    y = c.grid(grid_x, top, rows, cell_w=56, cell_h=40, row_labels=labels,
               col_labels=[f"bit {i}" for i in high_first], states=states)
    for index in range(len(minus_one)):
        c.text(grid_x + width * 56 + 14, top + index * 40 + 27,
               f"{minus_one[index]} & {mask} = {subsets[index + 1]}", size=18, color="muted")
    c.text(grid_x + width * 56 + 14, top + len(minus_one) * 40 + 27, "0: stop", size=18, color="muted")
    y = c.legend(y + 34, [("focus", "1: in the subset"), ("muted", "not in the mask")])
    c.note(y + 14, BITS_IDEA)


FIGURES = [
    {
        "id": "euclid-steps",
        "title": "Euclid and back-substitution",
        "category": CATEGORY,
        "description": ("NumberTheory.gcd(240, 46) divides 240 by 46, 46 by 10, 10 by 6, 6 by 4 and 4 by 2, "
                        "reaching remainder 0, so the gcd is 2. Substituting the divisions backwards gives "
                        "2 = 47 times 46 minus 9 times 240, the coefficients of extendedGcd."),
        "keyIdea": EUCLID_IDEA,
        "lesson": "algorithms/math/number-theory.md",
        "height": 856,
        "draw": euclid_steps,
    },
    {
        "id": "modpow-squares",
        "title": "modPow by repeated squaring",
        "category": CATEGORY,
        "description": ("NumberTheory.modPow(3, 13, 1000) reads the bits of 13 = 1101 from the lowest: the "
                        "squares of 3 are 3, 9, 81 and 561 modulo 1000, and the result 3, 3, 243, 323 "
                        "changes only on the 1 bits, ending at 323."),
        "keyIdea": MODPOW_IDEA,
        "lesson": "algorithms/math/number-theory.md",
        "height": 860,
        "draw": modpow_squares,
    },
    {
        "id": "sieve-grid",
        "title": "Sieve of Eratosthenes",
        "category": CATEGORY,
        "description": ("NumberTheory.primesUpTo(30) on a grid of 1 to 30: 2 crosses out 14 multiples from 4, "
                        "3 crosses out 4 more from 9, 5 crosses out 25, and the ten numbers never crossed "
                        "are the primes 2, 3, 5, 7, 11, 13, 17, 19, 23 and 29."),
        "keyIdea": SIEVE_IDEA,
        "lesson": "algorithms/math/number-theory.md",
        "height": 852,
        "draw": sieve_grid,
    },
    {
        "id": "reservoir-sampling",
        "title": "Reservoir sampling",
        "category": CATEGORY,
        "description": ("Sampling.reservoirSample over the items A to H with k = 3 and seed 17 fills the "
                        "reservoir with A, B, C, then draws j for each later arrival with replacement chance "
                        "3/4 down to 3/8; D, F and H replace slots 0, 0 and 1, giving F, H, C."),
        "keyIdea": RESERVOIR_IDEA,
        "lesson": "algorithms/math/randomized-sampling.md",
        "height": 834,
        "draw": reservoir_sampling,
    },
    {
        "id": "alias-table",
        "title": "Alias table columns",
        "category": CATEGORY,
        "description": ("AliasTable for the weights 1, 1, 2, 4 has four equal columns: A and B each keep half "
                        "their column and give the other half to D, while C and D fill their columns. Adding "
                        "the pieces back gives the probabilities 1/8, 1/8, 1/4 and 1/2."),
        "keyIdea": ALIAS_IDEA,
        "lesson": "algorithms/math/randomized-sampling.md",
        "height": 862,
        "draw": alias_table,
    },
    {
        "id": "bitmask-subsets",
        "title": "A set as a bit mask",
        "category": CATEGORY,
        "description": ("The set {1, 3} of four elements is the int mask 1010 = 10. Testing bit 3 gives 1, "
                        "setting bit 2 gives 1110, clearing bit 3 gives 0010, and (sub - 1) & mask walks "
                        "the subsets 10, 8, 2 and 0."),
        "keyIdea": BITS_IDEA,
        "lesson": "algorithms/bit-manipulation/overview.md",
        "height": 868,
        "draw": bitmask_subsets,
    },
]
