"""Deterministic conceptual algorithm traces derived from canonical fixtures.

The viewer consumes frames containing title/explanation/summary and a view mapping.
Array/DP views use values, labels, states, range, markers, optional lanes/candidates.
Optional view keys: rangeLabel, legend ([[state, label], ...]), orderLabel, tableLabel.
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


def _array_view(values, states, labels=None, **extra):
    view = {'type': 'array', 'values': list(values),
            'labels': list(range(len(values))) if labels is None else labels, 'states': list(states)}
    view.update(extra)
    return view


def _digit(value, place):
    return (value // 10 ** (place - 1)) % 10


def _insertion(f):
    original = _ints(f, 'traces.insertion.values')
    values, frames, size = list(original), [], len(original)
    legend = [['chosen', 'Sorted prefix'], ['current', 'Key being placed'],
              ['boundary', 'Shifted one place right'], ['active', 'Not yet inserted']]

    def view(prefix, key_at=None, shifted=(), lane=None):
        states = ['active'] * size
        for index in range(prefix):
            states[index] = 'boundary' if index in shifted else 'chosen'
        if key_at is not None:
            states[key_at] = 'current'
        return _array_view(values, states, range=[0, prefix], rangeLabel=f'Sorted prefix [0, {prefix})',
                           lanes=lane or [], legend=legend,
                           note='Take the next value, shift larger sorted values right, drop it into the gap.')

    _frame(frames, 'A one-value prefix is sorted',
           f'Index 0 alone is a sorted prefix of {values}. Each step inserts the next value into that prefix.', view(1))
    for index in range(1, size):
        key, position = values[index], index
        _frame(frames, f'Lift {key} from index {index}',
               f'The prefix [0, {index}) is sorted. Hold {key} aside and walk left past every value strictly greater than it.',
               view(index, index, lane=[{'label': 'Key held aside', 'values': [key], 'states': ['current']}]))
        while position > 0 and values[position - 1] > key:
            values[position] = values[position - 1]
            position -= 1
        values[position] = key
        shifted = set(range(position + 1, index + 1))
        tie = position > 0 and values[position - 1] == key
        _frame(frames, f'Insert {key} at index {position}',
               (f'{len(shifted)} larger value{"s" if len(shifted) != 1 else ""} moved one place right. '
                if shifted else f'Nothing in the prefix is larger than {key}, so it stays where it is. ')
               + ('It stops after an equal value, so equal values keep their original order. ' if tie else '')
               + f'The prefix [0, {index + 1}) is sorted again.', view(index + 1, position, shifted))
    _require(values, _ints(f, 'traces.insertion.sorted'), 'insertion result')
    _frame(frames, 'Every value is inserted', f'The array is sorted: {values}. Equal values kept their original order.',
           _array_view(values, ['chosen'] * size, range=[0, size], rangeLabel=f'Sorted prefix [0, {size})', legend=legend))
    return _trace('insertion', 'Insertion sort: grow a sorted prefix', 'Sorting',
                  'Insert each value into an already sorted prefix by shifting larger values right.',
                  'The prefix is sorted after every insertion; shifting only strictly larger values keeps equal values stable.',
                  'algorithms/sorting/bubble-sort.md', frames)


def _quickselect(f):
    original, k = _ints(f, 'traces.quickselect.values'), int(f['traces.quickselect.k'])
    values, frames, size, low, high = list(original), [], len(original), 0, len(original)
    legend = [['current', 'Pivot (final position)'], ['boundary', 'Smaller than the pivot'],
              ['active', 'Active, not yet ordered'], ['excluded', 'Outside the active range'], ['chosen', 'Answer']]

    def view(low, high, pivot=None):
        states = ['excluded' if i < low or i >= high else 'active' for i in range(size)]
        if pivot is not None:
            for index in range(low, high):
                states[index] = 'boundary' if index < pivot else 'active'
            states[pivot] = 'current'
        markers = {'k': k}
        if pivot is not None and pivot != k:
            markers['pivot'] = pivot
        return _array_view(values, states, range=[low, high], rangeLabel=f'Active range [{low}, {high})',
                           markers=markers, legend=legend,
                           note=f'Find the value with zero-based rank k = {k}: the one at index {k} once sorted.')

    _frame(frames, f'Look for index {k}', f'Rank k = {k} means the value that would sit at index {k} after sorting. '
           'Partitioning places one pivot at its final index; only the side containing k needs more work.', view(low, high))
    while True:
        pivot_value = values[high - 1]
        boundary = low
        for index in range(low, high - 1):
            if values[index] < pivot_value:
                values[index], values[boundary] = values[boundary], values[index]
                boundary += 1
        values[high - 1], values[boundary] = values[boundary], values[high - 1]
        _frame(frames, f'Partition around {pivot_value}',
               f'Take the last value of [{low}, {high}) as the pivot and move smaller values before it. '
               f'The pivot lands at its final sorted index {boundary}.', view(low, high, boundary))
        if boundary == k:
            break
        if k < boundary:
            high = boundary
            side = f'k = {k} is left of index {boundary}, so keep [{low}, {high}) and drop the pivot and everything right of it.'
        else:
            low = boundary + 1
            side = f'k = {k} is right of index {boundary}, so keep [{low}, {high}) and drop the pivot and everything left of it.'
        _frame(frames, 'Keep the side that holds k', side + f' Index {k} is still inside the range.', view(low, high))
    _require(values[k], int(f['traces.quickselect.answer']), 'quickselect answer')
    _require(values[k], sorted(original)[k], 'quickselect rank')
    states = ['excluded'] * size
    states[k] = 'chosen'
    _frame(frames, f'The pivot is index {k}', f'The pivot landed exactly at index {k}, so {values[k]} is the value of rank {k}. '
           'The rest of the array is only partly ordered, which is all selection needs.',
           _array_view(values, states, range=[k, k + 1], rangeLabel=f'Active range [{k}, {k + 1})',
                       markers={'k': k}, legend=legend))
    return _trace('quickselect', 'Quickselect: partition one side', 'Searching',
                  'Find the k-th smallest value by partitioning and keeping only the side that contains index k. '
                  'The trace uses a last-element pivot; the library chooses pivots differently.',
                  'Unlike a full sort, each round discards a side; the answer index never leaves the active range.',
                  'algorithms/searching/selection.md', frames)


def _radix(f):
    original = _ints(f, 'traces.radix.values')
    if min(original) < 0 or max(original) > 999:
        raise ValueError('radix trace expects nonnegative values of at most three digits')
    order, frames, size = list(original), [], len(original)
    names = {1: 'ones', 2: 'tens', 3: 'hundreds'}
    legend = [['active', 'Waiting for the next pass'], ['current', 'Digit being read'],
              ['chosen', 'Ordered by the digits read so far']]

    def view(values, state, place=None):
        lanes = []
        if place:
            lanes = [{'label': f'{names[place].capitalize()} digit of each value',
                      'values': [_digit(value, place) for value in values], 'states': ['current'] * size}]
        return _array_view(values, [state] * size, lanes=lanes, legend=legend,
                           note='Stable passes from the least significant digit; later passes never reorder ties.')

    _frame(frames, 'Start with the unsorted numbers',
           'Least-significant-digit radix sort makes one stable pass per digit. After pass d the values are ordered by their last d digits.',
           view(order, 'active'))
    for place in (1, 2, 3):
        _frame(frames, f'Pass {place}: read the {names[place]} digit',
               f'Each value goes into the bucket named by its {names[place]} digit. Buckets are read from 0 to 9, each in arrival order.',
               view(order, 'active', place))
        after = sorted(order, key=lambda value: value % 10 ** place)  # stable, like a counting pass
        groups = {}
        for value in after:
            groups.setdefault(_digit(value, place), []).append(value)
        ties = '; '.join(f'digit {digit}: {members}' for digit, members in groups.items() if len(members) > 1)
        _frame(frames, f'Pass {place}: collect the buckets',
               f'Values are ordered by their last {place} digit{"s" if place > 1 else ""}. '
               f'Ties keep their earlier order ({ties or "none in this pass"}).', view(after, 'chosen', place))
        order = after
    _require(order, _ints(f, 'traces.radix.sorted'), 'radix result')
    _frame(frames, 'Sorted by all three digits', f'Three stable passes sorted the numbers: {order}.', view(order, 'chosen'))
    return _trace('radix', 'Radix sort: one digit per pass', 'Sorting',
                  'Sort three-digit numbers with stable passes from the ones digit to the hundreds digit. '
                  'The library uses four 8-bit digits instead of decimal ones; the idea is the same.',
                  'Stability is what lets a later, more significant digit sort without undoing earlier work.',
                  'algorithms/sorting/non-comparison-and-comparator-sorts.md', frames)


def _two_pointers(f):
    values, target = _ints(f, 'traces.two.values'), int(f['traces.two.target'])
    if values != sorted(values):
        raise ValueError('traces.two.values must be ascending')
    size, left, right, frames = len(values), 0, len(values) - 1, []
    legend = [['current', 'Left / right pointer'], ['active', 'Still possible'],
              ['excluded', 'Cannot be in any answer'], ['chosen', 'Pair found']]

    def view(left, right, found=False):
        states = ['excluded' if i < left or i > right else 'active' for i in range(size)]
        states[left] = states[right] = 'chosen' if found else 'current'
        return _array_view(values, states, range=[left, right + 1], rangeLabel=f'Candidate indices [{left}, {right}] inclusive',
                           markers={'left': left, 'right': right}, legend=legend,
                           note=f'Sorted values · target sum {target}')

    _frame(frames, 'Start at both ends', f'Look for two values summing to {target}. Start with the smallest and the largest value.', view(left, right))
    result = None
    while left < right:
        total = values[left] + values[right]
        if total == target:
            result = (left, right)
            break
        if total < target:
            _frame(frames, f'{values[left]} + {values[right]} = {total}',
                   f'{total} is below {target}. Even the largest remaining partner is too small for {values[left]}, so move left to {left + 1}.',
                   view(left, right))
            left += 1
        else:
            _frame(frames, f'{values[left]} + {values[right]} = {total}',
                   f'{total} is above {target}. Even the smallest remaining partner is too large for {values[right]}, so move right to {right - 1}.',
                   view(left, right))
            right -= 1
    _require(result, tuple(map(int, f['traces.two.result'].split(':'))), 'two-pointer pair')
    _frame(frames, f'{values[left]} + {values[right]} = {target}',
           f'The sum equals the target at indices {left} and {right}. Every discarded index was ruled out by a sorted comparison, so no pair was skipped.',
           view(left, right, True))
    return _trace('two-pointers', 'Two pointers: pair with a target sum', 'Array patterns',
                  'Move pointers inward on a sorted array, discarding one end per comparison.',
                  'Sorted order means each comparison rules out an entire index, so no valid pair is ever skipped.',
                  'algorithms/array-patterns/overview.md', frames)


def _window(f):
    text, frames = f['traces.window.text'], []
    size, start, last, best = len(text), 0, {}, (0, 0)
    legend = [['current', 'Character just read'], ['active', 'Window (all distinct)'], ['excluded', 'Dropped from the window'],
              ['unseen', 'Not read yet'], ['chosen', 'Best window']]

    def view(start, end, best, finished=False):
        states = ['unseen'] * size
        for index in range(size):
            if finished:
                states[index] = 'chosen' if best[0] <= index < best[1] else 'excluded'
            elif end is not None and index <= end:
                states[index] = 'excluded' if index < start else 'active'
        if end is not None and not finished:
            states[end] = 'current'
        markers = {} if end is None or finished else {'start': start, 'end': end}
        lane = {'label': 'Best window so far', 'values': list(text[best[0]:best[1]]),
                'states': ['chosen'] * (best[1] - best[0])}
        return _array_view(list(text), states, markers=markers, lanes=[lane], legend=legend,
                           note='Window = characters from start to end, all distinct.')

    _frame(frames, 'Start with an empty window', f'Scan "{text}" once. The window always holds distinct characters; a repeat moves its left edge.', view(0, None, best))
    for index, char in enumerate(text):
        previous = last.get(char)
        last[char] = index
        if previous is not None and previous >= start:
            start = previous + 1
            move = f'{char!r} already appears at index {previous}, inside the window, so start jumps to {start}.'
        elif previous is not None:
            move = f'{char!r} appeared at index {previous}, but that is outside the window, so start stays at {start}.'
        else:
            move = f'{char!r} is new, so the window grows.'
        length = index - start + 1
        if length > best[1] - best[0]:
            best = (start, index + 1)
            record = f' Length {length} is a new best.'
        else:
            record = f' Length {length} does not beat the best {best[1] - best[0]}; ties keep the earlier window.'
        _frame(frames, f'Read {char!r} at index {index}', move + record, view(start, index, best))
    _require(best, tuple(map(int, f['traces.window.result'].split(':'))), 'window span')
    _frame(frames, f'Longest window is "{text[best[0]:best[1]]}"',
           f'Indices [{best[0]}, {best[1]}) hold {best[1] - best[0]} distinct characters, the earliest window of that length.',
           view(start, size - 1, best, finished=True))
    return _trace('window', 'Sliding window: no repeated character', 'Array patterns',
                  'Slide a window over a string, jumping its left edge past a repeated character.',
                  'The left edge only moves right, and the window never contains a repeat, so one pass is enough.',
                  'algorithms/array-patterns/overview.md', frames)


def _z_function(f):
    text, frames = f['traces.z.text'], []
    size = len(text)
    z, left, right = [None] * size, 0, 0
    z[0] = size
    legend = [['current', 'Index being computed'], ['active', 'Rightmost matching window'], ['unseen', 'Not yet computed'],
              ['chosen', 'Computed z value']]

    def view(index=None, finished=False):
        states = ['chosen' if finished else 'unseen'] * size
        if not finished:
            for position in range(left, right):
                states[position] = 'active'
        markers = {}
        if index is not None:
            states[index] = 'current'
            markers['i'] = index
            if right > left:
                markers['window'] = left
        lane_states = ['unseen' if value is None else 'chosen' for value in z]
        if index is not None:
            lane_states[index] = 'current'
        return _array_view(list(text), states, markers=markers, legend=legend,
                           lanes=[{'label': 'Z-array: longest prefix match at each index (— = not computed)',
                                   'values': list(z), 'states': lane_states}],
                           note=f'Text "{text}" · z[i] compares the text with its own suffix from index i.')

    _frame(frames, 'z[0] is the whole text', f'By the repository convention z[0] = {size}. Every other entry compares the text with its suffix.', view())
    for index in range(1, size):
        inside = index < right
        match = min(right - index, z[index - left]) if inside else 0
        copied = match
        while index + match < size and text[match] == text[index + match]:
            match += 1
        z[index] = match
        window_start = left
        if index + match > right:
            left, right = index, index + match
        if inside:
            how = (f'Index {index} lies inside the window starting at {window_start}, so it starts from z[{index - window_start}]: '
                   f'{copied} character{"s" if copied != 1 else ""} copied without rescanning, {match - copied} compared fresh.')
        else:
            how = f'Index {index} is outside every known window, so compare the text with the suffix from scratch.'
        _frame(frames, f'Compute z[{index}] = {match}', f'{how} Longest prefix match: "{text[:match]}" ({match}).', view(index))
    _require(z, _ints(f, 'traces.z.result'), 'Z-array')
    _frame(frames, 'The Z-array is complete', f'z = {z}. Reusing the rightmost window keeps the total work linear.', view(finished=True))
    return _trace('z-function', 'Z-function: reuse the matching window', 'Strings',
                  'Build the Z-array while reusing the rightmost prefix match instead of rescanning.',
                  'Inside a known match window, earlier z values bound the new one, so characters are not compared twice.',
                  'algorithms/strings/pattern-matching.md', frames)


def _adjacency(size, edges, directed):
    adjacency = [[] for _ in range(size)]
    for a, b in edges:
        if b not in adjacency[a]:
            adjacency[a].append(b)
        if not directed and a not in adjacency[b]:
            adjacency[b].append(a)
    return adjacency


def _dfs(f):
    size, edges = int(f['traces.dfs.vertices']), _edges(f, 'traces.dfs.edges')
    adjacency = _adjacency(size, edges, False)
    frames, stack, order, processed, parents, cursor = [], [0], [0], set(), {0: None}, [0] * size
    legend = [['current', 'Top of the stack (being explored)'], ['frontier', 'On the stack (open path)'],
              ['done', 'Finished'], ['unseen', 'Not discovered']]

    def view(current=None, active=None):
        result = _graph_view(size, edges, set(parents), processed, current, active, parents,
                             queue=stack[:], order=order[:], queue_label='Stack (bottom → top, the open path)')
        result['legend'] = legend
        return result

    _frame(frames, 'Visit the source', 'Mark 0 visited and push it. The stack always holds the path from the source to the vertex being explored.', view(0))
    while stack:
        vertex = stack[-1]
        while cursor[vertex] < len(adjacency[vertex]) and adjacency[vertex][cursor[vertex]] in parents:
            cursor[vertex] += 1
        if cursor[vertex] < len(adjacency[vertex]):
            neighbor = adjacency[vertex][cursor[vertex]]
            parents[neighbor] = vertex
            stack.append(neighbor)
            order.append(neighbor)
            _frame(frames, f'Descend from {vertex} to {neighbor}',
                   f'{neighbor} is the first unvisited neighbor of {vertex}. Mark and push it, going as deep as possible before trying other branches.',
                   view(neighbor, (vertex, neighbor)))
        else:
            stack.pop()
            processed.add(vertex)
            _frame(frames, f'Backtrack from {vertex}',
                   f'Every neighbor of {vertex} is visited, so pop it. '
                   + (f'Exploration resumes at {stack[-1]}.' if stack else 'The stack is empty.'),
                   view(stack[-1] if stack else None))
    _require(order, _ints(f, 'traces.dfs.order'), 'DFS order')
    unreachable = [i for i in range(size) if i not in parents]
    _frame(frames, 'The reachable component is complete',
           f'Preorder: {order}. Unreachable from 0: {unreachable}.', view())
    return _trace('dfs', 'DFS: follow one path, then backtrack', 'Graphs',
                  'Follow an explicit stack that always holds the path from the source.',
                  'Mark a vertex when it is first reached; the stack is exactly the open path back to the source.',
                  'algorithms/graph/dfs.md', frames)


def _topological(f):
    size, edges = int(f['traces.topo.vertices']), _edges(f, 'traces.topo.edges')
    adjacency = _adjacency(size, edges, True)
    remaining = [0] * size
    for _, target in edges:
        remaining[target] += 1
    ready, order, frames = [v for v in range(size) if remaining[v] == 0], [], []
    legend = [['current', 'Just emitted'], ['frontier', 'Ready: no unemitted predecessor'],
              ['done', 'Emitted / edge removed'], ['unseen', 'Still waiting on a predecessor']]

    def view(current=None):
        result = _graph_view(size, edges, set(ready) | set(order), set(order), current, None, None, directed=True,
                             queue=ready[:], order=order[:], distances=remaining[:],
                             queue_label='Ready queue (front first)')
        for edge in result['edges']:
            if edge['from'] in order:
                edge['state'] = 'tree'
        result.update({'legend': legend, 'orderLabel': 'Emitted order',
                       'tableLabel': 'Unemitted predecessors per vertex'})
        return result

    _frame(frames, 'Find the vertices with no predecessors',
           f"Count incoming edges. Vertices {ready} have none, so they may come first. Kahn's algorithm repeatedly emits a ready vertex and removes its outgoing edges.", view())
    while ready:
        vertex = ready.pop(0)
        order.append(vertex)
        released = []
        for neighbor in adjacency[vertex]:
            remaining[neighbor] -= 1
            if remaining[neighbor] == 0:
                ready.append(neighbor)
                released.append(neighbor)
        _frame(frames, f'Emit vertex {vertex}',
               f'All predecessors of {vertex} are already emitted. Remove its outgoing edges; '
               + (f'{released} now have no unemitted predecessor and join the queue.' if released else 'no vertex became ready.'),
               view(vertex))
    _require(order, _ints(f, 'traces.topo.order'), 'topological order')
    if len(order) != size:
        raise ValueError('topological trace requires an acyclic fixture')
    _frame(frames, 'Every vertex is emitted',
           f'Order {order}: each vertex appears after all of its predecessors. With a cycle, some vertices would never become ready.', view())
    return _trace('topological', "Topological sort: Kahn's algorithm", 'Graphs',
                  'Repeatedly emit a vertex with no unemitted predecessors from a directed acyclic graph.',
                  'A vertex becomes ready only when every predecessor is emitted, so the order respects every edge.',
                  'algorithms/graph/dfs.md', frames)


def build_traces(fixtures):
    """Return thirteen deterministic traces; reject disagreement with canonical expected results."""
    return [_binary(fixtures), _merge(fixtures), _bfs(fixtures), _dijkstra(fixtures), _coins(fixtures),
            _insertion(fixtures), _quickselect(fixtures), _radix(fixtures), _two_pointers(fixtures),
            _window(fixtures), _z_function(fixtures), _dfs(fixtures), _topological(fixtures)]
