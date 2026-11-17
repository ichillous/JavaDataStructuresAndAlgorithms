"""Linear-structure figures added after the original set; fixtures in examples-linear_more.properties.

``dynamic_array_growth`` is the reference example for new figures: numbered steps read top
to bottom, every value comes from the fixture file, every state is named in a label or the
legend, and the figure ends with the KEY IDEA note.
"""

from drawing import ints

GROWTH_IDEA = ("Doubling makes copies rare: n appends copy fewer than 2n values in total, "
               "so one append costs O(1) amortized.")

# One row geometry for every array in the figure, so the old and new arrays line up.
ROW_X, CELL, GAP = 39, 50, 6


def dynamic_array_growth(c, fixtures):
    """Append to a full DynamicArray: allocate double, copy the live prefix, then write."""
    capacity = int(fixtures["linear_more.growth.capacity"])
    before = ints(fixtures, "linear_more.growth.before")
    value = int(fixtures["linear_more.growth.value"])
    grown = int(fixtures["linear_more.growth.grownCapacity"])
    after = ints(fixtures, "linear_more.growth.after")
    copies = int(fixtures["linear_more.growth.copies"])
    next_append = int(fixtures["linear_more.growth.nextGrowthAppend"])
    next_copies = int(fixtures["linear_more.growth.nextCopies"])
    total = int(fixtures["linear_more.growth.totalCopies"])

    def x(index):
        return c.cell_center(ROW_X, index, CELL, GAP)

    def edge(index, side):
        return ROW_X + index * (CELL + GAP) + (0 if side == "left" else CELL)

    # 1. The old array is full, so the new value has nowhere to go.
    y = c.step(156, 1, f"add({value}) finds no free slot")
    c.cell_row(ROW_X, y, before, cell=CELL, gap=GAP)
    waiting = len(before) + 1
    c.cell_row(edge(waiting, "left"), y, [value], cell=CELL, gap=GAP, states=["warn"])
    c.text(x(waiting), y + 74, "waiting", anchor="middle", size=18, color="gold", weight="700")
    c.brace(edge(0, "left"), edge(len(before) - 1, "right"), y + 60,
            f"size {len(before)} = capacity {capacity}")

    # 2. A new array of double the capacity receives copies, then the new value.
    y = c.step(312, 2, f"Allocate {grown} slots, copy {copies}, write {value}")
    c.brace(edge(0, "left"), edge(grown - 1, "right"), y + 40, f"new capacity {grown}",
            color="blue", above=True)
    row_y = y + 52
    states = ["good"] * len(before) + ["focus"] + ["muted"] * (grown - len(after))
    c.cell_row(ROW_X, row_y, after + [""] * (grown - len(after)), cell=CELL, gap=GAP, states=states)
    c.brace(edge(0, "left"), edge(len(before) - 1, "right"), row_y + 60, f"{copies} copies",
            color="green")
    c.pointer(x(len(after)), row_y + 48, f"size = {len(after)}")

    # 3. The running count: copies happen only when the array is full.
    y = c.step(540, 3, "Running copy count")
    left = 66
    left += c.chip(left, y, f"{len(after)} appends", color="blue") + 10
    c.chip(left, y, f"{copies} copies so far", color="green")
    c.text(66, y + 60, f"Append {next_append} copies {next_copies} more: {total} copies in {next_append} appends.",
           size=18, color="muted")

    y = c.legend(y + 106, [("good", "copied"), ("focus", "new"), ("muted", "free slot"),
                           ("warn", "waiting")])
    c.note(y + 14, GROWTH_IDEA)


FIGURES = [
    {
        "id": "dynamic-array-growth",
        "title": "Appending to a full array",
        "category": "Linear structures",
        "description": ("A DynamicArray with capacity 4 holds 10, 20, 30 and 40, so add(50) allocates 8 "
                        "slots, copies the four values and writes 50 at index 4. That append copies 4 "
                        "values; the next copy happens at append 9 and moves 8, for 12 copies in 9 appends."),
        "lesson": "data-structures/arrays/operations.md",
        "keyIdea": GROWTH_IDEA,
        "height": 856,
        "draw": dynamic_array_growth,
    },
]
