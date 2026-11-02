/* Offline viewer: data arrives through a local classic script, never a request. */
(() => {
  'use strict';
  const byId = (id) => document.getElementById(id);
  const element = (tag, className, text) => {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = String(text);
    return node;
  };
  const svgElement = (tag, attributes, text) => {
    const node = document.createElementNS('http://www.w3.org/2000/svg', tag);
    Object.entries(attributes || {}).forEach(([name, value]) => node.setAttribute(name, String(value)));
    if (text !== undefined) node.textContent = String(text);
    return node;
  };
  const media = window.matchMedia('(prefers-color-scheme: dark)');
  let preference = 'system';
  try { preference = localStorage.getItem('dsa-visuals-theme') || 'system'; } catch (_) { /* file:// may deny storage */ }
  if (!['system', 'light', 'dark'].includes(preference)) preference = 'system';
  const themeSelect = byId('theme');
  themeSelect.value = preference;
  let currentTheme = 'light';
  let expandedFigure = null;
  const data = globalThis.DSA_VISUALS_DATA;
  const figures = data && Array.isArray(data.figures) ? data.figures : [];
  const traces = data && Array.isArray(data.traces) ? data.traces : [];
  const cards = new Map();

  function applyTheme() {
    currentTheme = preference === 'system' ? (media.matches ? 'dark' : 'light') : preference;
    document.documentElement.dataset.theme = currentTheme;
    document.documentElement.style.colorScheme = currentTheme;
    figures.forEach((figure) => {
      const card = cards.get(figure.id);
      if (card) card.querySelector('img').src = currentTheme === 'dark' ? figure.dark : figure.light;
    });
    if (expandedFigure) byId('dialog-image').src = currentTheme === 'dark' ? expandedFigure.dark : expandedFigure.light;
  }
  themeSelect.addEventListener('change', () => {
    preference = themeSelect.value;
    try { localStorage.setItem('dsa-visuals-theme', preference); } catch (_) { /* optional persistence */ }
    applyTheme();
  });
  media.addEventListener('change', applyTheme);
  applyTheme();

  if (!data || !figures.length || !traces.length) {
    byId('data-error').hidden = false;
    byId('frame-title').textContent = 'Visual data is unavailable';
    byId('frame-explanation').textContent = 'Generate the assets, then reopen this local page. No server is required.';
    ['previous', 'next', 'reset', 'step-position', 'algorithm'].forEach((id) => { byId(id).disabled = true; });
    return;
  }
  byId('figure-count').textContent = figures.length;
  byId('trace-count').textContent = traces.length;
  const heroTrace = traces.find((trace) => trace.id === 'binary');
  if (heroTrace) {
    const initial = heroTrace.frames[0].view;
    const final = heroTrace.frames[heroTrace.frames.length - 1].view;
    (initial.values || []).forEach((value, index) => {
      const cell = element('span', final.markers && final.markers.result === index ? 'mini-active' : '', value);
      cell.append(element('small', '', index));
      byId('hero-array').append(cell);
    });
  }

  /* Gallery: text is assigned as text, including all filter/query-dependent state. */
  const dialog = byId('figure-dialog');
  function expandFigure(figure) {
    expandedFigure = figure;
    byId('dialog-title').textContent = figure.title;
    byId('dialog-category').textContent = figure.category;
    byId('dialog-description').textContent = figure.description;
    byId('dialog-idea').textContent = 'Key idea: ' + figure.keyIdea;
    byId('dialog-image').src = currentTheme === 'dark' ? figure.dark : figure.light;
    byId('dialog-image').alt = figure.description + ' Key idea: ' + figure.keyIdea;
    byId('dialog-lesson').href = figure.lesson;
    dialog.showModal();
  }
  byId('close-dialog').addEventListener('click', () => dialog.close());
  dialog.addEventListener('close', () => { expandedFigure = null; });
  dialog.addEventListener('click', (event) => {
    if (event.target !== dialog) return;
    const box = dialog.getBoundingClientRect();
    if (event.clientX < box.left || event.clientX > box.right || event.clientY < box.top || event.clientY > box.bottom) dialog.close();
  });
  figures.forEach((figure) => {
    const card = element('article', 'figure-card');
    const preview = element('button', 'figure-preview');
    preview.type = 'button';
    preview.setAttribute('aria-label', 'Expand figure: ' + figure.title);
    const image = element('img');
    image.src = currentTheme === 'dark' ? figure.dark : figure.light;
    image.alt = figure.description;
    image.loading = 'lazy';
    image.decoding = 'async';
    preview.append(image, element('span', 'expand-hint', 'Expand →'));
    preview.addEventListener('click', () => expandFigure(figure));
    const copy = element('div', 'figure-copy');
    copy.append(element('span', 'eyebrow', figure.category), element('h3', '', figure.title), element('p', '', figure.description));
    const idea = element('p', 'figure-idea');
    idea.append(element('strong', '', 'Key idea. '), document.createTextNode(figure.keyIdea));
    const lesson = element('a', 'text-link', 'Read the lesson →');
    lesson.href = figure.lesson;
    lesson.setAttribute('aria-label', 'Read the lesson for ' + figure.title);
    copy.append(idea, lesson);
    card.append(preview, copy);
    cards.set(figure.id, card);
    byId('gallery').append(card);
  });
  let category = 'All';
  const categories = ['All', ...new Set(figures.map((figure) => figure.category))];
  categories.forEach((name) => {
    const count = name === 'All' ? figures.length : figures.filter((figure) => figure.category === name).length;
    const button = element('button', 'category-filter', name + ' · ' + count);
    button.type = 'button';
    button.dataset.category = name;
    button.setAttribute('aria-pressed', String(name === category));
    button.addEventListener('click', () => {
      category = name;
      document.querySelectorAll('.category-filter').forEach((item) => item.setAttribute('aria-pressed', String(item.dataset.category === category)));
      filterGallery();
    });
    byId('category-filters').append(button);
  });
  function filterGallery() {
    const terms = byId('search').value.trim().toLocaleLowerCase().split(/\s+/).filter(Boolean);
    let visible = 0;
    figures.forEach((figure) => {
      const text = [figure.title, figure.category, figure.description, figure.keyIdea].join(' ').toLocaleLowerCase();
      const matches = (category === 'All' || figure.category === category) && terms.every((term) => text.includes(term));
      cards.get(figure.id).hidden = !matches;
      if (matches) visible++;
    });
    byId('results-count').textContent = visible + ' of ' + figures.length + ' diagrams';
    byId('empty-state').hidden = visible !== 0;
  }
  byId('search').addEventListener('input', filterGallery);
  byId('clear-filters').addEventListener('click', () => {
    byId('search').value = '';
    category = 'All';
    document.querySelectorAll('.category-filter').forEach((item) => item.setAttribute('aria-pressed', String(item.dataset.category === 'All')));
    filterGallery();
    byId('search').focus();
  });
  filterGallery();

  /* Array and table views keep labels, state, and copied buffers distinct. */
  function arrayRow(values, labels, states, markers, compact) {
    const row = element('div', 'array-row');
    row.setAttribute('role', 'list');
    values.forEach((value, index) => {
      const state = states && states[index] ? states[index] : 'active';
      const cell = element('div', 'array-cell');
      cell.dataset.state = state;
      cell.setAttribute('role', 'listitem');
      const displayed = value === null ? '—' : String(value);
      cell.setAttribute('aria-label', (labels ? 'Index ' + labels[index] + ', ' : '') + 'value ' + displayed + ', ' + state);
      cell.append(element('div', 'array-value', displayed));
      if (labels) cell.append(element('div', 'array-index', labels[index]));
      if (markers) Object.entries(markers).forEach(([name, position]) => {
        if (position === index) cell.append(element('span', 'array-marker', name));
      });
      row.append(cell);
    });
    if (!values.length) row.append(element('span', 'state-note', compact ? 'Empty buffer' : 'No values'));
    return row;
  }
  function renderArray(view, container) {
    if (view.note) container.append(element('p', 'state-note', view.note));
    container.append(arrayRow(view.values || [], view.labels, view.states, view.markers));
    if (view.range) {
      let label = (view.markers ? 'Inspect ' : 'Active run ') + '[' + view.range.join(', ') + ')';
      if (view.markers && view.markers.high === view.values.length) label += ' · high is the end boundary';
      container.append(element('p', 'range-label', label));
    }
    (view.lanes || []).forEach((lane) => {
      const section = element('div', 'lane');
      section.append(element('p', 'lane-label', lane.label), arrayRow(lane.values, null, lane.states, null, true));
      container.append(section);
    });
    if (view.type === 'dp' && view.candidates && view.candidates.length) {
      const table = element('table', 'candidate-table');
      table.append(element('caption', '', 'Try each usable last coin'));
      const header = element('tr');
      ['Coin', 'From amount', 'dp[from] + 1', 'Choice'].forEach((text) => {
        const heading = element('th', '', text);
        heading.scope = 'col';
        header.append(heading);
      });
      const head = element('thead'); head.append(header); table.append(head);
      const body = element('tbody');
      view.candidates.forEach((candidate) => {
        const row = element('tr', candidate.chosen ? 'chosen' : '');
        [candidate.coin, candidate.from, candidate.previous + ' + 1 = ' + candidate.cost, candidate.chosen ? 'minimum' : ''].forEach((text) => row.append(element('td', '', text)));
        body.append(row);
      });
      table.append(body);
      container.append(table);
    }
  }

  function graphPositions(count) {
    if (count === 6) return [[65, 150], [230, 60], [230, 240], [415, 60], [415, 240], [555, 150]];
    if (count === 5) return [[65, 150], [250, 60], [250, 240], [435, 150], [555, 250]];
    return Array.from({length: count}, (_, index) => {
      const angle = index * Math.PI * 2 / Math.max(count, 1) - Math.PI / 2;
      return [300 + 220 * Math.cos(angle), 150 + 110 * Math.sin(angle)];
    });
  }
  function factRow(label, value) {
    const row = element('div', 'fact-row');
    row.append(element('span', 'fact-label', label), element('span', 'fact-value', value));
    return row;
  }
  function renderGraph(view, container, summary) {
    const nodes = view.nodes || [];
    const points = graphPositions(nodes.length);
    const svg = svgElement('svg', {viewBox: '0 0 600 300', class: 'graph-svg', role: 'img', 'aria-label': summary});
    const defs = svgElement('defs');
    const marker = svgElement('marker', {id: 'trace-arrow', viewBox: '0 0 10 10', refX: 9, refY: 5, markerWidth: 7, markerHeight: 7, orient: 'auto-start-reverse'});
    marker.append(svgElement('path', {d: 'M 0 1 L 9 5 L 0 9 Z', class: 'graph-arrow'}));
    defs.append(marker); svg.append(defs);
    const weightLabels = [];
    (view.edges || []).forEach((edge) => {
      const start = points[edge.from], end = points[edge.to];
      if (!start || !end) return;
      const dx = end[0] - start[0], dy = end[1] - start[1];
      const length = Math.hypot(dx, dy) || 1;
      const line = svgElement('line', {x1: start[0] + dx / length * 25, y1: start[1] + dy / length * 25,
        x2: end[0] - dx / length * 28, y2: end[1] - dy / length * 28, class: 'graph-edge ' + (edge.state || 'default')});
      if (view.directed) line.setAttribute('marker-end', 'url(#trace-arrow)');
      svg.append(line);
      if (edge.weight !== undefined) {
        const x = (start[0] + end[0]) / 2, y = (start[1] + end[1]) / 2;
        const group = svgElement('g');
        group.append(svgElement('rect', {x: x - 15, y: y - 12, width: 30, height: 24, rx: 4, class: 'graph-weight-bg'}));
        group.append(svgElement('text', {x, y, class: 'graph-weight'}, edge.weight));
        weightLabels.push(group);
      }
    });
    weightLabels.forEach((label) => svg.append(label));
    nodes.forEach((node, index) => {
      const [x, y] = points[index];
      const group = svgElement('g');
      group.append(svgElement('circle', {cx: x, cy: y, r: 24, class: 'graph-node ' + node.state}));
      group.append(svgElement('text', {x, y, class: 'graph-node-label'}, node.id));
      svg.append(group);
    });
    container.append(svg);
    const facts = element('div', 'state-facts');
    facts.append(factRow(view.queueLabel || 'Queue', view.queue.length ? '[ ' + view.queue.join(', ') + ' ]' : '[ empty ]'));
    if (!view.directed || (view.order && view.order.length)) facts.append(factRow('Visit order', view.order.length ? view.order.join(' → ') : 'Not started'));
    if (view.path && view.path.length) facts.append(factRow('Shortest path', view.path.join(' → ')));
    container.append(facts);
    if (view.distances) {
      const distances = element('div', 'distance-table');
      distances.setAttribute('aria-label', 'Tentative distances by vertex');
      view.distances.forEach((distance, index) => {
        const item = element('div', 'distance-item');
        item.append(element('span', '', 'vertex ' + index), document.createTextNode(distance === null ? '∞' : String(distance)));
        distances.append(item);
      });
      container.append(distances);
    }
  }
  function renderLegend(trace, view) {
    let labels;
    if (view.type === 'graph') labels = [['current', 'Current / relaxed edge'], ['frontier', 'Discovered, pending'], ['done', 'Processed / parent edge'], ['unseen', 'Not discovered']];
    else if (trace.id === 'binary') labels = [['current', 'Inspect / midpoint'], ['boundary', 'Retained answer boundary'], ['chosen', 'Final answer'], ['excluded', 'Discarded']];
    else if (view.type === 'dp') labels = [['current', 'Current amount'], ['chosen', 'Chosen predecessor / solved'], ['unseen', 'Not computed']];
    else labels = [['current', 'Active run / next front'], ['chosen', 'Output / sorted'], ['excluded', 'Outside run / consumed']];
    const legend = byId('state-legend');
    legend.replaceChildren();
    labels.forEach(([state, label]) => {
      const item = element('span', 'legend-item');
      item.append(element('span', 'legend-swatch ' + state), document.createTextNode(label));
      legend.append(item);
    });
  }

  let traceIndex = 0;
  let frameIndex = 0;
  const algorithmSelect = byId('algorithm');
  traces.forEach((trace, index) => {
    const option = element('option', '', trace.title);
    option.value = String(index); algorithmSelect.append(option);
  });
  function renderFrame() {
    const trace = traces[traceIndex];
    const frame = trace.frames[frameIndex];
    byId('trace-description').textContent = trace.description;
    byId('trace-key-idea').textContent = trace.keyIdea;
    byId('trace-lesson').href = trace.lesson;
    byId('trace-type').textContent = trace.category;
    byId('frame-title').textContent = frame.title;
    byId('frame-explanation').textContent = frame.explanation;
    byId('frame-number').textContent = String(frameIndex + 1).padStart(2, '0') + ' / ' + String(trace.frames.length).padStart(2, '0');
    byId('step-output').textContent = frameIndex + 1;
    const slider = byId('step-position');
    slider.max = trace.frames.length - 1;
    slider.value = frameIndex;
    slider.setAttribute('aria-valuetext', 'Step ' + (frameIndex + 1) + ' of ' + trace.frames.length + ': ' + frame.title);
    byId('previous').disabled = frameIndex === 0;
    byId('next').disabled = frameIndex === trace.frames.length - 1;
    const visual = byId('state-visual');
    visual.replaceChildren();
    visual.setAttribute('aria-label', frame.summary || frame.explanation);
    if (frame.view.type === 'graph') renderGraph(frame.view, visual, frame.summary || frame.explanation);
    else renderArray(frame.view, visual);
    renderLegend(trace, frame.view);
  }
  function goToFrame(position) {
    frameIndex = Math.max(0, Math.min(traces[traceIndex].frames.length - 1, position));
    renderFrame();
  }
  algorithmSelect.addEventListener('change', () => { traceIndex = Number(algorithmSelect.value); frameIndex = 0; renderFrame(); });
  byId('previous').addEventListener('click', () => goToFrame(frameIndex - 1));
  byId('next').addEventListener('click', () => goToFrame(frameIndex + 1));
  byId('reset').addEventListener('click', () => goToFrame(0));
  byId('step-position').addEventListener('input', () => goToFrame(Number(byId('step-position').value)));
  byId('trace-workspace').addEventListener('keydown', (event) => {
    if (event.altKey || event.ctrlKey || event.metaKey) return;
    const commands = {ArrowLeft: () => goToFrame(frameIndex - 1), ArrowRight: () => goToFrame(frameIndex + 1), Home: () => goToFrame(0), End: () => goToFrame(traces[traceIndex].frames.length - 1)};
    if (commands[event.key]) { event.preventDefault(); commands[event.key](); }
  });
  renderFrame();
})();
