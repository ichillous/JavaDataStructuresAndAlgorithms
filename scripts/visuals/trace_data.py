"""Deterministic conceptual algorithm traces derived from canonical fixtures.

The viewer consumes frames containing title/explanation/summary and a view mapping.
Array/DP views use values, labels, states, range, markers, optional lanes/candidates.
Graph views use nodes(id,state), edges(from,to,weight?,state), directed, queue,
queueLabel, order, distances, and optional path. None means unknown/unreachable;
textual explanation distinguishes the two. These are teaching states, not timings.
"""
from copy import deepcopy
import heapq


def _ints(fixtures, key):
    return [int(item) for item in fixtures[key].split(',') if item]


def _edges(fixtures, key):
    return [tuple(map(int, item.split(':'))) for item in fixtures[key].split(',') if item]


def _require(actual, expected, label):
    if actual != expected:
        raise ValueError(f'{label}: calculated {actual!r}, fixture expects {expected!r}')


def _frame(frames, title, explanation, view, summary=None):
    frames.append({'title': title, 'explanation': explanation,
                   'summary': summary or explanation, 'view': deepcopy(view)})


def _trace(trace_id, title, category, description, idea, lesson, frames):
    return {'id': trace_id, 'title': title, 'category': category,
            'description': description, 'keyIdea': idea, 'lesson': '../../' + lesson,
            'frames': frames}


def _binary(f):
    values, target = _ints(f, 'binary.values'), int(f['binary.target'])
    if values != sorted(values):
        raise ValueError('binary.values must be sorted')
    frames, steps, low, high = [], [], 0, len(values)

    def view(mid=None, result=None):
        states = [('excluded' if i < low or i > high else ('boundary' if i == high else 'active')) for i in range(len(values))]
        markers = {'low': low, 'high': high}
        if mid is not None:
            markers['mid'] = mid
            states[mid] = 'current'
        if result is not None and result < len(values):
            markers['result'] = result
            states[result] = 'chosen'
        return {'type': 'array', 'values': values, 'labels': list(range(len(values))),
                'states': states, 'range': [low, high], 'markers': markers,
                'note': f'Target {target} · inspect [{low}, {high}); possible answer boundaries [{low}, {high}]'}

    _frame(frames, 'Start with every position',
           f'Find the first index whose value is at least {target}. The answer may be the end boundary.', view())
    while low < high:
        mid = low + (high - low) // 2
        steps.append((low, high, mid))
        move_high = values[mid] >= target
        _frame(frames, f'Inspect index {mid}',
               f'Value {values[mid]} is {"at least" if move_high else "less than"} target {target}. '
               + (f'Keep this possible first position by moving high to {mid}.' if move_high
                  else f'Discard this index and everything before it by moving low to {mid + 1}.'), view(mid))
        if move_high:
            high = mid
        else:
            low = mid + 1
        _frame(frames, 'Narrow the interval',
               f'Unknown predicates remain in [{low}, {high}). Values before low are too small; '
               'high is a retained possible answer boundary, not a discarded match.', view())
    _require(steps, _edges(f, 'binary.steps'), 'binary steps')
    _require(low, int(f['binary.result']), 'binary result')
    _frame(frames, f'Lower bound is index {low}',
           f'Low equals high. The first value at least {target} is at index {low}; duplicates to its right do not change the answer.',
           view(result=low))
    return _trace('binary', 'Binary search: lower bound', 'Searching',
                  'Shrink a half-open interval while keeping the first possible match.',
                  'A matching midpoint is a candidate, so retain it by moving high.',
                  'algorithms/searching/binary-search.md', frames)


def _merge(f):
    values = _ints(f, 'merge.values')
    frames = []

    def view(lo=0, hi=None, lanes=None, done=False):
        hi = len(values) if hi is None else hi
        return {'type': 'array', 'values': values, 'labels': list(range(len(values))),
                'states': ['sorted' if done else ('active' if lo <= i < hi else 'excluded')
                           for i in range(len(values))], 'range': [lo, hi], 'lanes': lanes or [],
                'note': 'Each merge reads copied sorted runs and builds a separate output buffer.'}

    _frame(frames, 'Divide, then merge sorted runs',
           'Split until runs contain one value. Merge sorted neighbors without dropping or inventing values.', view())

    def sort(lo, hi):
        if hi - lo < 2:
            return
        mid = lo + (hi - lo) // 2
        _frame(frames, f'Split [{lo}, {hi}) at {mid}',
               f'Sort [{lo}, {mid}) and [{mid}, {hi}) independently before merging them.', view(lo, hi))
        sort(lo, mid)
        sort(mid, hi)
        left, right, output, i, j = values[lo:mid], values[mid:hi], [], 0, 0
        while i < len(left) or j < len(right):
            from_left = j == len(right) or (i < len(left) and left[i] <= right[j])
            selected = left[i] if from_left else right[j]
            if from_left:
                i += 1
            else:
                j += 1
            output.append(selected)
            lanes = [{'label': 'Copied left run', 'values': left,
                      'states': ['excluded' if k < i else ('current' if k == i else 'active') for k in range(len(left))]},
                     {'label': 'Copied right run', 'values': right,
                      'states': ['excluded' if k < j else ('current' if k == j else 'active') for k in range(len(right))]},
                     {'label': 'Output buffer', 'values': output, 'states': ['chosen'] * len(output)}]
            _frame(frames, f'Append {selected} from the {"left" if from_left else "right"}',
                   'Choose the smaller unconsumed front. Equal fronts choose the left run, preserving stability. '
                   'The main row stays unchanged until this output buffer is complete.', view(lo, hi, lanes))
        values[lo:hi] = output
        _frame(frames, f'Commit sorted run [{lo}, {hi})',
               f'Replace just this range with {output}. Its values and multiplicities are preserved.', view(lo, hi))

    sort(0, len(values))
    _require(values, _ints(f, 'merge.sorted'), 'merge result')
    _frame(frames, 'Every run is merged', f'The complete array is now sorted: {values}.', view(done=True))
    return _trace('merge', 'Merge sort: build sorted runs', 'Sorting',
                  'Watch copied input runs feed an output buffer, then commit one sorted interval.',
                  'Each merge consumes each value once; copies are labeled so input and output remain distinct.',
                  'algorithms/sorting/merge-sort.md', frames)


def _graph_view(size, edges, discovered, processed, current=None, active=None, parents=None,
                directed=False, queue=None, order=None, distances=None, path=None, queue_label='Queue (front first)'):
    parents = parents or {}
    result_edges = []
    for edge in edges:
        source, target = edge[:2]
        is_tree = parents.get(target) == source or (not directed and parents.get(source) == target)
        is_active = active == (source, target) or (not directed and active == (target, source))
        item = {'from': source, 'to': target, 'state': 'current' if is_active else ('tree' if is_tree else 'default')}
        if len(edge) == 3:
            item['weight'] = edge[2]
        result_edges.append(item)
    return {'type': 'graph', 'directed': directed,
            'nodes': [{'id': i, 'state': 'current' if i == current else ('done' if i in processed else ('frontier' if i in discovered else 'unseen'))}
                      for i in range(size)], 'edges': result_edges, 'queue': queue or [],
            'queueLabel': queue_label, 'order': order or [], 'distances': distances, 'path': path or []}


def _path(parents, target):
    if target not in parents:
        return []
    path = [target]
    while parents[path[-1]] is not None:
        path.append(parents[path[-1]])
    return list(reversed(path))


def _bfs(f):
    size, edges = int(f['bfs.vertices']), _edges(f, 'bfs.edges')
    adjacency = [[] for _ in range(size)]
    for a, b in edges:
        if b not in adjacency[a]:
            adjacency[a].append(b)
        if a not in adjacency[b]:
            adjacency[b].append(a)
    frames, queue, order, processed, parents = [], [0], [], set(), {0: None}

    def view(current=None, active=None):
        return _graph_view(size, edges, set(parents), processed, current, active,
                           parents, queue=queue, order=order)

    _frame(frames, 'Discover the source', 'Mark vertex 0 discovered before enqueueing it. No vertex enters the queue twice.', view())
    while queue:
        current = queue.pop(0)
        order.append(current)
        _frame(frames, f'Dequeue vertex {current}',
               f'Process {current} next because it is at the FIFO front. Inspect neighbors in insertion order.', view(current))
        for neighbor in adjacency[current]:
            if neighbor not in parents:
                parents[neighbor] = current
                queue.append(neighbor)
                _frame(frames, f'Discover {neighbor} from {current}',
                       f'Record parent[{neighbor}] = {current}, mark it now, and append it to the queue. '
                       'First discovery gives a shortest path in edge count.', view(current, (current, neighbor)))
        processed.add(current)
    _require(order, _ints(f, 'bfs.order'), 'BFS order')
    expected_path = _ints(f, 'bfs.path')
    path = _path(parents, expected_path[-1])
    _require(path, expected_path, 'BFS path')
    final = view()
    final['path'] = path
    unreachable = [i for i in range(size) if i not in parents]
    _frame(frames, 'The reachable component is complete',
           f'Visit order: {order}. Shortest path to {path[-1]}: {path}. Unreachable vertices: {unreachable}.', final)
    return _trace('bfs', 'BFS: discover one layer at a time', 'Graphs',
                  'Follow a FIFO queue through a graph with a cycle and an isolated vertex.',
                  'Mark on enqueue, so shared neighbors cannot schedule the same vertex twice.',
                  'algorithms/graph/bfs.md', frames)


def _dijkstra(f):
    size, edges = int(f['dijkstra.vertices']), _edges(f, 'dijkstra.edges')
    adjacency = [[] for _ in range(size)]
    for a, b, weight in edges:
        if weight < 0:
            raise ValueError('Dijkstra trace requires nonnegative weights')
        adjacency[a].append((b, weight))
    frames, distances, heap, processed, parents = [], [None] * size, [(0, 0)], set(), {0: None}
    distances[0] = 0

    def view(current=None, active=None):
        return _graph_view(size, edges, set(parents), processed, current, active, parents,
                           directed=True, queue=[f'{vertex}:{cost}' for cost, vertex in sorted(heap)],
                           distances=distances, queue_label='Heap candidates (vertex:distance)')

    _frame(frames, 'Initialize tentative distances',
           'Distance to source 0 is zero. Other distances are not yet known. Heap rows show pending candidates in priority order, not physical heap storage.', view())
    while heap:
        cost, current = heapq.heappop(heap)
        if distances[current] != cost:
            _frame(frames, f'Ignore stale candidate {current}:{cost}',
                   f'Vertex {current} already has the better distance {distances[current]}. Lazy heaps keep old entries; skip them without rescanning edges.', view())
            continue
        processed.add(current)
        _frame(frames, f'Settle vertex {current} at distance {cost}',
               'This is the smallest pending distance. Nonnegative edges ensure no later route can improve this settled value.', view(current))
        for neighbor, weight in adjacency[current]:
            candidate = cost + weight
            if distances[neighbor] is None or candidate < distances[neighbor]:
                old = distances[neighbor]
                distances[neighbor] = candidate
                parents[neighbor] = current
                heapq.heappush(heap, (candidate, neighbor))
                _frame(frames, f'Relax {current} → {neighbor}',
                       f'{cost} + edge weight {weight} = {candidate}, improving {"unknown" if old is None else old}. '
                       f'Record parent {current} and push candidate {neighbor}:{candidate}.', view(current, (current, neighbor)))
    expected = [None if item == 'unreachable' else int(item) for item in f['dijkstra.distances'].split(',')]
    _require(distances, expected, 'Dijkstra distances')
    expected_path = _ints(f, 'dijkstra.path')
    path = _path(parents, expected_path[-1])
    _require(path, expected_path, 'Dijkstra path')
    final = view()
    final['path'] = path
    _frame(frames, 'All reachable shortest distances are final',
           f'The path to {path[-1]} is {path}, with distance {distances[path[-1]]}. '
           'Infinity now means unreachable, not merely undiscovered.', final)
    return _trace('dijkstra', 'Dijkstra: improve, then settle', 'Graphs',
                  'Track tentative distances, relax directed weighted edges, and discard stale heap entries.',
                  'Choose the smallest tentative distance; a FIFO queue cannot replace this priority rule.',
                  'data-structures/graphs/traversal.md', frames)


def _coins(f):
    coins, amount = _ints(f, 'coins.values'), int(f['coins.amount'])
    if amount < 0 or any(coin <= 0 for coin in coins):
        raise ValueError('coin trace requires positive denominations and nonnegative amount')
    table, frames = [0] + [None] * amount, []

    def view(current=0, candidates=None):
        chosen = next((item['from'] for item in candidates or [] if item['chosen']), None)
        return {'type': 'dp', 'values': table, 'labels': list(range(amount + 1)),
                'states': ['current' if i == current else ('chosen' if i == chosen else ('sorted' if value is not None else 'unseen'))
                           for i, value in enumerate(table)], 'current': current,
                'candidates': candidates or [], 'note': f'Denominations {coins} · dp[a] = minimum coins for amount a · — = not computed'}

    _frame(frames, 'The empty amount costs zero',
           'Set dp[0] = 0. Build amounts in increasing order so every predecessor has already been solved.', view())
    for current in range(1, amount + 1):
        candidates, best, chosen = [], None, None
        for coin in coins:
            if coin <= current and table[current - coin] is not None:
                cost = table[current - coin] + 1
                candidates.append({'coin': coin, 'from': current - coin, 'previous': table[current - coin], 'cost': cost, 'chosen': False})
                if best is None or cost < best:
                    best, chosen = cost, len(candidates) - 1
        if chosen is not None:
            candidates[chosen]['chosen'] = True
        table[current] = best
        explanation = (f'For amount {current}, compare each usable last coin. '
                       + (f'Choose coin {candidates[chosen]["coin"]}: dp[{candidates[chosen]["from"]}] + 1 = {best}. '
                          'This reuses an optimal smaller amount, not a largest-coin-first guess.' if chosen is not None
                          else 'No reachable predecessor exists, so this amount is unreachable.'))
        _frame(frames, f'Solve amount {current}', explanation, view(current, candidates))
    _require(table, _ints(f, 'coins.table'), 'coin-change table')
    _frame(frames, f'Amount {amount} needs {table[amount]} coins',
           f'The completed table is {table}. Every solved amount compares all usable last coins instead of assuming a greedy choice.', view(amount))
    return _trace('coins', 'Coin change: reuse smaller answers', 'Dynamic programming',
                  'Fill a minimum-coin table and inspect every last-coin candidate.',
                  'A correct recurrence compares all valid predecessors; a greedy choice can miss the optimum.',
                  'algorithms/dynamic-programming/problems.md', frames)


def build_traces(fixtures):
    """Return five deterministic traces; reject disagreement with canonical expected results."""
    return [_binary(fixtures), _merge(fixtures), _bfs(fixtures), _dijkstra(fixtures), _coins(fixtures)]
