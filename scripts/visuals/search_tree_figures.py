"""Search-tree figures; fixtures in examples-search_tree.properties.

Every tree is drawn from a shape string such as ``8(3(1,6),10)``: a key, then an optional
``(left,right)`` pair in which a missing child is empty. Red-black shapes append ``B`` or
``R`` to each key. VisualExamplesSearchTreeTest checks the same strings against the library
and an independent reference model.
"""

from drawing import ints

TRAVERSAL_IDEA = "Pre, in and post order differ only in when the root is read."
DELETE_IDEA = "The successor's key replaces the deleted key; only its old node is unlinked."
TWO_THREE_IDEA = "A red link and its black parent make one 3-node."
FIXUP_IDEA = "Rotations keep sorted order; a flip splits a 4-node."

NODE_R = 22


def parse(text):
    """Parse a shape string into ``(key, color, left, right)`` tuples; ``color`` may be ''."""
    position = 0

    def read():
        nonlocal position
        start = position
        while position < len(text) and text[position].isdigit():
            position += 1
        if start == position:
            return None
        key = int(text[start:position])
        color = ""
        if position < len(text) and text[position] in "BR":
            color = text[position]
            position += 1
        left = right = None
        if position < len(text) and text[position] == "(":
            position += 1
            left = read()
            position += 1  # the comma
            right = read()
            position += 1  # the closing bracket
        return key, color, left, right

    return read()


def layout(tree, x0, spacing, y0, dy):
    """Place nodes by in-order index across and by depth down.

    Returns a list of dicts (``key``, ``color``, ``x``, ``y``, ``depth``, ``parent``, ``leaf``)
    in in-order, where ``parent`` indexes the same list.
    """
    nodes = []

    def walk(node, depth, parent):
        if node is None:
            return None
        key, color, left, right = node
        record = {"key": key, "color": color, "depth": depth, "parent": parent,
                  "leaf": left is None and right is None, "y": y0 + depth * dy}
        walk(left, depth + 1, record)
        record["x"] = x0 + len(nodes) * spacing
        nodes.append(record)
        walk(right, depth + 1, record)
        return record

    walk(tree, 0, None)
    return nodes


def draw_tree(c, nodes, state_of, dashed_edges=()):
    """Edges first, then nodes; ``state_of(record)`` returns a state name or None."""
    for record in nodes:
        parent = record["parent"]
        if parent is not None:
            color = "red" if record["color"] == "R" else "line"
            c.edge((parent["x"], parent["y"]), (record["x"], record["y"]), radius=NODE_R,
                   directed=False, color=color, dashed=record["key"] in dashed_edges)
    for record in nodes:
        state = state_of(record)
        c.node(record["x"], record["y"], record["key"], radius=NODE_R, state=state)


def rb_state(record):
    """Red nodes use the red state; black nodes keep a plain node with an ink outline."""
    return "bad" if record["color"] == "R" else None


def draw_rb_tree(c, nodes):
    for record in nodes:
        parent = record["parent"]
        if parent is not None:
            c.edge((parent["x"], parent["y"]), (record["x"], record["y"]), radius=NODE_R, directed=False,
                   color="red" if record["color"] == "R" else "line")
    for record in nodes:
        c.node(record["x"], record["y"], record["key"], radius=NODE_R, state=rb_state(record),
               color=None if record["color"] == "R" else "ink")


def rb_legend(c, y):
    """Swatches for black and red nodes; the text names each color."""
    c.parts.append(f'<circle cx="40" cy="{y - 6}" r="10" fill="{c.color("paper")}" '
                   f'stroke="{c.color("ink")}" stroke-width="2"/>')
    c.text(58, y, "black node", size=18)
    c.parts.append(f'<circle cx="190" cy="{y - 6}" r="10" fill="{c.color("rose")}" '
                   f'stroke="{c.color("red")}" stroke-width="3"/>')
    c.text(208, y, "red node, red link", size=18)
    return y + 10


def tree_traversals(c, fixtures):
    """One tree, its parts named once, then the four sequences it produces."""
    tree = parse(fixtures["search_tree.traversal.shape"])
    top, dy = 204, 46
    nodes = layout(tree, 130, 46, top, dy)
    height = int(fixtures["search_tree.traversal.height"])
    leaves = set(ints(fixtures, "search_tree.traversal.leaves"))
    root = next(n for n in nodes if n["parent"] is None)
    deepest = max(nodes, key=lambda n: (n["depth"], n["x"]))

    c.step(156, 1, "Root, leaves, depth and height")
    draw_tree(c, nodes, lambda n: "focus" if n["parent"] is None else "good" if n["leaf"] else None)
    for depth in range(height + 1):
        c.text(28, top + depth * dy + 6, f"depth {depth}", size=18, color="muted")
    c.chip(root["x"] - 112, root["y"] - 15, "root", color="blue")
    c.chip(deepest["x"] + 36, deepest["y"] - 15, f"height {height}", color="muted")

    def row(y, title, keys):
        c.text(36, y, title, size=18, color="ink", weight="700")
        states = ["focus" if k == root["key"] else "good" if k in leaves else None for k in keys]
        return c.cell_row(36, y + 8, keys, cell=50, gap=8, height=38, states=states)

    y = c.step(top + height * dy + 58, 2, "Read the same keys four ways")
    y = row(y + 14, "pre-order: root, left, right", ints(fixtures, "search_tree.traversal.preOrder"))
    y = row(y + 28, "in-order: left, root, right", ints(fixtures, "search_tree.traversal.inOrder"))
    y = row(y + 28, "post-order: left, right, root", ints(fixtures, "search_tree.traversal.postOrder"))
    y = row(y + 28, "level order: depth 0, then 1, 2, 3", ints(fixtures, "search_tree.traversal.levelOrder"))

    y = c.legend(y + 28, [("focus", "root"), ("good", "leaf")])
    c.note(y + 14, TRAVERSAL_IDEA)


def bst_delete_successor(c, fixtures):
    """Delete a two-child node by copying its in-order successor into it."""
    key = int(fixtures["search_tree.delete.key"])
    successor = int(fixtures["search_tree.delete.successor"])
    before_order = ints(fixtures, "search_tree.delete.beforeInOrder")
    after_order = ints(fixtures, "search_tree.delete.afterInOrder")

    def place(shape, top):
        nodes = layout(parse(shape), 70, 62, top, 60)
        return nodes, {n["key"]: n for n in nodes}, max(n["y"] for n in nodes)

    y = c.step(156, 1, f"delete({key}): the node has two children")
    before, node_of, bottom = place(fixtures["search_tree.delete.before"], y + 26)
    draw_tree(c, before, lambda n: "bad" if n["key"] == key else "focus" if n["key"] == successor else None)
    c.text(node_of[key]["x"] + 36, node_of[key]["y"] + 7, "delete", size=18, color="red", weight="700")
    c.text(node_of[successor]["x"] + 34, node_of[successor]["y"] + 7, "successor", size=18,
           color="blue", weight="700")
    c.text(300, y + 40, f"From {key}, go right once,", size=18, color="muted")
    c.text(300, y + 66, "then left until there is", size=18, color="muted")
    c.text(300, y + 92, f"no left child: {successor}.", size=18, color="muted")

    y = c.step(bottom + 52, 2, f"Copy {successor} up, unlink its old node")
    after, node_of, bottom = place(fixtures["search_tree.delete.after"], y + 26)
    draw_tree(c, after, lambda n: "good" if n["key"] == successor else None)
    c.text(node_of[successor]["x"] + 36, node_of[successor]["y"] + 7, f"was {key}", size=18,
           color="green", weight="700")
    # The old position of the successor is empty after the unlink.
    parent = node_of[max(k for k in node_of)]
    gx, gy = parent["x"] - 62, parent["y"] + 60
    c.edge((parent["x"], parent["y"]), (gx, gy), radius=NODE_R, directed=False, color="line", dashed=True)
    c.node(gx, gy, "", radius=NODE_R, state="muted")
    c.text(gx + 32, gy + 7, f"old {successor}, now empty", size=18, color="muted")

    y = c.step(gy + 52, 3, "Order and height after")
    c.text(66, y + 12, "in-order  " + ", ".join(map(str, before_order)) + "  \u2192  "
           + ", ".join(map(str, after_order)), size=18, color="ink")
    c.text(66, y + 42, f"height  {fixtures['search_tree.delete.beforeHeight']}  \u2192  "
           f"{fixtures['search_tree.delete.afterHeight']}", size=18, color="ink")

    y = c.legend(y + 76, [("bad", "deleted key"), ("focus", "successor"), ("good", "key copied up")])
    c.note(y + 14, DELETE_IDEA)


def red_black_23_nodes(c, fixtures):
    """The same five keys as a 2-3 tree and as the RedBlackTreeMap shape."""
    root_keys = ints(fixtures, "search_tree.twothree.rootNode")
    child_keys = ints(fixtures, "search_tree.twothree.childNodes")
    tree = parse(fixtures["search_tree.twothree.shape"])
    puts = ints(fixtures, "search_tree.twothree.putOrder")

    y = c.step(156, 1, "A 2-3 tree holds the keys")
    cell, mid = 50, 260
    root_x = mid - cell
    c.cell_row(root_x, y + 18, root_keys, cell=cell, gap=0, height=40, states=["alt"] * 2)
    c.text(root_x + 2 * cell + 14, y + 44, "3-node", size=18, color="violet", weight="700")
    child_centres = [130, 260, 390]
    anchors = [root_x, mid, root_x + 2 * cell]
    for anchor, centre in zip(anchors, child_centres):
        c.line(anchor, y + 58, centre, y + 104, color="line", width=2.5)
    top = y + 104
    for centre, key in zip(child_centres, child_keys):
        c.cell_row(centre - cell / 2, top, [key], cell=cell, gap=0, height=40)
    c.text(mid, top + 70, "2-nodes: one key each", size=18, color="muted", anchor="middle")

    y = c.step(top + 118, 2, f"RedBlackTreeMap after put {puts[0]} to {puts[-1]}")
    nodes = layout(tree, 110, 70, y + 30, 62)
    draw_rb_tree(c, nodes)
    by_key = {n["key"]: n for n in nodes}
    red = next(n for n in nodes if n["color"] == "R")
    c.text(red["x"] - 36, red["y"] - 30, "red link", size=18, color="red", weight="700", anchor="end")
    bottom = max(n["y"] for n in nodes)
    c.text(40, bottom + 56, f"Black {by_key[root_keys[1]]['key']} + red {by_key[root_keys[0]]['key']} = "
           f"3-node [{root_keys[0]} {root_keys[1]}]", size=18)
    c.text(40, bottom + 84, "Black 1, 3, 5 = three 2-nodes", size=18)

    y = rb_legend(c, bottom + 126)
    c.note(y + 14, TWO_THREE_IDEA)


def red_black_fixups(c, fixtures):
    """The three local repairs, each on three keys, before and after."""
    steps = [
        ("rotateLeft", "Rotate left at h", "puts"),
        ("rotateRight", "Rotate right at h", "puts"),
        ("flip", "Flip colors at h", "puts"),
    ]
    y = 156
    for number, (name, title, _) in enumerate(steps, 1):
        prefix = f"search_tree.fix.{name}"
        puts = ints(fixtures, prefix + ".puts")
        y = c.step(y, number, title + ", e.g. put " + ", ".join(map(str, puts)))
        before = layout(parse(fixtures[prefix + ".before"]), 66, 56, y + 26, 58)
        after = layout(parse(fixtures[prefix + ".after"]), 322, 56, y + 26, 58)
        draw_rb_tree(c, before)
        draw_rb_tree(c, after)
        mid_y = y + 26
        c.arrow(240, mid_y + 4, 292, mid_y + 4, color="blue")
        old_top = next(n for n in before if n["parent"] is None)
        after_h = next(n for n in after if n["key"] == old_top["key"])
        side = 36 if name == "rotateRight" else -36
        for record in (old_top, after_h):
            c.text(record["x"] + side, record["y"] + 7, "h", size=18, color="blue", weight="700",
                   anchor="middle")
        y = max(n["y"] for n in before + after) + 62

    y = rb_legend(c, y - 6)
    c.note(y + 14, FIXUP_IDEA)


FIGURES = [
    {
        "id": "tree-traversals",
        "title": "Four ways to walk a tree",
        "category": "Trees and ranges",
        "description": ("A binary search tree built from 8, 3, 10, 1, 6, 14, 4 and 7 has root 8, leaves 1, 4, 7 and 14, "
                        "and height 3. Pre-order reads 8 3 1 6 4 7 10 14, in-order reads the keys sorted, "
                        "post-order ends at the root 8, and level order reads one depth at a time."),
        "lesson": "data-structures/trees/binary-trees.md",
        "keyIdea": TRAVERSAL_IDEA,
        "height": 890,
        "draw": tree_traversals,
    },
    {
        "id": "bst-delete-successor",
        "title": "Delete with two children",
        "category": "Trees and ranges",
        "description": ("Deleting key 2 from a BinarySearchTree whose root 2 has children 1 and 4, with 3 below 4, "
                        "copies the in-order successor 3 into the root and unlinks the old 3. "
                        "The in-order keys become 1, 3, 4 and the height drops from 2 to 1."),
        "lesson": "data-structures/trees/binary-search-trees.md",
        "keyIdea": DELETE_IDEA,
        "height": 880,
        "draw": bst_delete_successor,
    },
    {
        "id": "red-black-23-nodes",
        "title": "Red links make 3-nodes",
        "category": "Trees and ranges",
        "description": ("After put 1 to 5, a RedBlackTreeMap has black root 4 with red left child 2, black children 1 "
                        "and 3 under 2, and black 5. Read as a 2-3 tree it is the 3-node [2 4] over the 2-nodes "
                        "[1], [3] and [5]; the height is 2 as a binary tree and 1 as a 2-3 tree."),
        "lesson": "data-structures/trees/red-black-trees.md",
        "keyIdea": TWO_THREE_IDEA,
        "height": 860,
        "draw": red_black_23_nodes,
    },
    {
        "id": "red-black-fixups",
        "title": "Three local repairs",
        "category": "Trees and ranges",
        "description": ("Rotate left turns black 1 with a red right child 2 into black 2 with a red left child 1. "
                        "Rotate right turns black 3 over red 2 over red 1 into black 2 with red children 1 and 3. "
                        "Flip colors turns black 2 with red children into red 2 with black children."),
        "lesson": "data-structures/trees/red-black-trees.md",
        "keyIdea": FIXUP_IDEA,
        "height": 870,
        "draw": red_black_fixups,
    },
]
