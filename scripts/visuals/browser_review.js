#!/usr/bin/env node
/*
 * Rendered review of the committed figures and the offline viewer, using the pre-installed
 * Playwright Chromium and nothing else. Optional tooling: not part of the Maven build. It
 * regenerates nothing; run `python3 scripts/generate_visuals.py` first.
 *
 *   node scripts/visuals/browser_review.js --figures ID1,ID2 --sheets DIR
 *       Renders the named figures in both themes, reports real text boxes that overlap or
 *       leave the canvas, and writes contact sheets (at most four figures each) to DIR.
 *   node scripts/visuals/browser_review.js --all [--sheets DIR] [--write] [--previews]
 *       Checks every figure, then drives docs/visuals/index.html at 1440px and 390px in both
 *       themes through every trace frame. --write rewrites docs/visuals/data/browser-review.json;
 *       --previews also recaptures docs/visuals/previews/*.png.
 *
 * Exits 0 when clean, 1 when it finds issues, 2 on bad arguments.
 */
'use strict';
const fs = require('fs');
const http = require('http');
const path = require('path');
const { chromium } = require('playwright');

const ROOT = path.resolve(__dirname, '..', '..');
const VISUALS = path.join(ROOT, 'docs', 'visuals');
const TOLERANCE = 2;
const BOTTOM_MARGIN = 20;
const PER_SHEET = 4;
const VIEWPORTS = [{ width: 1440, height: 1050 }, { width: 390, height: 844 }];
const THEMES = ['light', 'dark'];

function usage(message) {
  if (message) console.error(message);
  console.error('Usage: node scripts/visuals/browser_review.js (--figures ID[,ID...] | --all) [--sheets DIR] [--write] [--previews]');
  process.exit(2);
}

function parseArguments(argv) {
  const options = { figures: null, all: false, sheets: null, write: false, previews: false };
  for (let index = 0; index < argv.length; index++) {
    const argument = argv[index];
    if (argument === '--figures') options.figures = (argv[++index] || '').split(',').map((id) => id.trim()).filter(Boolean);
    else if (argument === '--sheets') options.sheets = argv[++index];
    else if (argument === '--all') options.all = true;
    else if (argument === '--write') options.write = true;
    else if (argument === '--previews') options.previews = true;
    else usage('Unknown argument: ' + argument);
  }
  if (!options.all && !(options.figures && options.figures.length)) usage('Name figures with --figures or use --all.');
  if (options.all && options.figures) usage('Use either --figures or --all, not both.');
  if ((options.write || options.previews) && !options.all) usage('--write and --previews need --all.');
  if (options.sheets === undefined) usage('--sheets needs a directory.');
  return options;
}

/* Pairwise overlap and bounds checks on boxes {x, y, width, height, text}. */
function boxIssues(where, boxes, width, height, bottomMargin) {
  const issues = [];
  boxes.forEach((box) => {
    const right = box.x + box.width;
    const bottom = box.y + box.height;
    if (box.x < -0.5 || box.y < -0.5 || right > width + 0.5 || bottom > height + 0.5) {
      issues.push(`${where}: text ${JSON.stringify(box.text)} leaves the ${width}x${height} canvas`);
    } else if (bottomMargin && bottom > height - bottomMargin + 0.5) {
      issues.push(`${where}: text ${JSON.stringify(box.text)} enters the bottom ${bottomMargin}-unit margin`);
    }
  });
  for (let first = 0; first < boxes.length; first++) {
    for (let second = first + 1; second < boxes.length; second++) {
      const a = boxes[first];
      const b = boxes[second];
      const overlapX = Math.min(a.x + a.width, b.x + b.width) - Math.max(a.x, b.x);
      const overlapY = Math.min(a.y + a.height, b.y + b.height) - Math.max(a.y, b.y);
      if (overlapX > TOLERANCE && overlapY > TOLERANCE) {
        issues.push(`${where}: text ${JSON.stringify(a.text)} overlaps ${JSON.stringify(b.text)} by ${overlapX.toFixed(0)}x${overlapY.toFixed(0)}`);
      }
    }
  }
  return issues;
}

async function reviewFigures(browser, figures) {
  const page = await browser.newPage({ viewport: { width: 600, height: 800 } });
  const issues = [];
  let variants = 0;
  for (const figure of figures) {
    for (const theme of THEMES) {
      const svg = fs.readFileSync(path.join(VISUALS, figure[theme]), 'utf8');
      await page.setContent('<!doctype html><html><body style="margin:0">' + svg + '</body></html>');
      const result = await page.evaluate(() => {
        const root = document.querySelector('svg');
        const [, , width, height] = root.getAttribute('viewBox').split(/\s+/).map(Number);
        const boxes = [...root.querySelectorAll('text')].map((text) => {
          const box = text.getBBox();
          return { x: box.x, y: box.y, width: box.width, height: box.height, text: text.textContent };
        });
        return { width, height, boxes };
      });
      issues.push(...boxIssues(`${figure.id} (${theme})`, result.boxes, result.width, result.height, BOTTOM_MARGIN));
      variants++;
    }
  }
  await page.close();
  return { issues, variants };
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[character]);
}

async function writeSheets(browser, figures, directory) {
  fs.mkdirSync(directory, { recursive: true });
  const page = await browser.newPage({ viewport: { width: 1100, height: 900 } });
  const written = [];
  for (let start = 0; start < figures.length; start += PER_SHEET) {
    const group = figures.slice(start, start + PER_SHEET);
    const blocks = group.map((figure) => {
      const images = THEMES.map((theme) => {
        const data = Buffer.from(fs.readFileSync(path.join(VISUALS, figure[theme]))).toString('base64');
        return `<figure><img src="data:image/svg+xml;base64,${data}" alt=""><figcaption>${theme}</figcaption></figure>`;
      }).join('');
      return `<section><h2>${escapeHtml(figure.id)} <span>${String(figure.number).padStart(2, '0')} · ${escapeHtml(figure.title)}</span></h2><div class="pair">${images}</div></section>`;
    }).join('');
    await page.setContent(`<!doctype html><html><head><style>
      body{margin:0;padding:16px 20px;width:1060px;background:#c9ced6;font:16px system-ui,sans-serif;color:#111}
      section{margin:0 0 28px} h2{margin:0 0 8px;font-size:22px} h2 span{font-weight:400;color:#333;font-size:18px}
      .pair{display:flex;gap:20px;align-items:flex-start} figure{margin:0} img{display:block;width:520px}
      figcaption{font-size:14px;color:#333;margin-top:4px}
    </style></head><body>${blocks}</body></html>`);
    await page.waitForFunction(() => [...document.images].every((image) => image.complete && image.naturalWidth > 0));
    const name = `sheet-${String(start / PER_SHEET + 1).padStart(2, '0')}.png`;
    await page.screenshot({ path: path.join(directory, name), fullPage: true });
    written.push({ file: path.join(directory, name), ids: group.map((figure) => figure.id) });
  }
  await page.close();
  return written;
}

const TYPES = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.svg': 'image/svg+xml',
  '.json': 'application/json', '.png': 'image/png', '.md': 'text/markdown', '.ico': 'image/x-icon' };

function serveRepository() {
  const server = http.createServer((request, response) => {
    const pathname = decodeURIComponent(new URL(request.url, 'http://127.0.0.1').pathname);
    const file = path.resolve(ROOT, '.' + pathname);
    if (!file.startsWith(ROOT + path.sep) || !fs.existsSync(file) || !fs.statSync(file).isFile()) {
      response.writeHead(404);
      response.end('Not found');
      return;
    }
    response.writeHead(200, { 'Content-Type': TYPES[path.extname(file)] || 'application/octet-stream' });
    fs.createReadStream(file).pipe(response);
  });
  return new Promise((resolve) => server.listen(0, '127.0.0.1', () => resolve(server)));
}

async function reviewViewer(browser, base, figures, capturePreviews) {
  const issues = [];
  const screenshots = [];
  let frameStates = 0;
  for (const viewport of VIEWPORTS) {
    for (const theme of THEMES) {
      const layout = `${viewport.width}px ${theme}`;
      const context = await browser.newContext({ viewport, colorScheme: theme });
      const page = await context.newPage();
      page.on('request', (request) => {
        if (!request.url().startsWith(base) && !request.url().startsWith('data:')) issues.push(`${layout}: external request ${request.url()}`);
      });
      page.on('pageerror', (error) => issues.push(`${layout}: page error ${error.message}`));
      await page.goto(base + '/docs/visuals/index.html', { waitUntil: 'load' });
      await page.selectOption('#theme', theme);
      const state = await page.evaluate(() => ({
        theme: document.documentElement.dataset.theme,
        sources: [...document.querySelectorAll('#gallery img')].map((image) => image.getAttribute('src')),
        count: document.getElementById('figure-count').textContent,
      }));
      if (state.theme !== theme) issues.push(`${layout}: viewer theme is ${state.theme}`);
      if (state.sources.length !== figures.length || state.count !== String(figures.length)) {
        issues.push(`${layout}: gallery shows ${state.sources.length} figures, expected ${figures.length}`);
      }
      state.sources.forEach((source, index) => {
        const expected = figures[index] && figures[index][theme];
        if (source !== expected) issues.push(`${layout}: gallery image ${index + 1} is ${source}, expected ${expected}`);
      });
      const traceCount = await page.locator('#algorithm option').count();
      for (let trace = 0; trace < traceCount; trace++) {
        await page.selectOption('#algorithm', String(trace));
        await page.click('#reset');
        const frames = Number(await page.getAttribute('#step-position', 'max')) + 1;
        for (let frame = 0; frame < frames; frame++) {
          const result = await page.evaluate(() => {
            const visual = document.getElementById('state-visual');
            const area = visual.getBoundingClientRect();
            const boxes = [...visual.querySelectorAll('text')].map((text) => {
              const box = text.getBoundingClientRect();
              return { x: box.x - area.x, y: box.y - area.y, width: box.width, height: box.height, text: text.textContent };
            });
            return {
              overflow: document.documentElement.scrollWidth - document.documentElement.clientWidth,
              width: area.width, height: area.height, boxes,
              position: document.getElementById('frame-number').textContent,
              nextDisabled: document.getElementById('next').disabled,
            };
          });
          const where = `${layout} trace ${trace + 1} frame ${frame + 1}`;
          if (result.overflow > 0) issues.push(`${where}: page scrolls sideways by ${result.overflow}px`);
          issues.push(...boxIssues(where, result.boxes, result.width, result.height, 0));
          if (!result.position.startsWith(String(frame + 1).padStart(2, '0'))) issues.push(`${where}: shows position ${result.position}`);
          if (result.nextDisabled !== (frame === frames - 1)) issues.push(`${where}: Next button enabled state is wrong`);
          frameStates++;
          if (frame < frames - 1) await page.click('#next');
        }
      }
      if (capturePreviews) {
        await page.selectOption('#algorithm', '0');
        await page.click('#reset');
        const name = `${viewport.width > 600 ? 'desktop' : 'mobile'}-${theme}.png`;
        await page.locator('.trace-shell').screenshot({ path: path.join(VISUALS, 'previews', name) });
        screenshots.push('previews/' + name);
      }
      await context.close();
    }
  }
  return { issues, frameStates, screenshots };
}

async function main() {
  const options = parseArguments(process.argv.slice(2));
  const manifest = JSON.parse(fs.readFileSync(path.join(VISUALS, 'data', 'manifest.json'), 'utf8'));
  let figures = manifest.figures;
  if (options.figures) {
    const known = new Map(figures.map((figure) => [figure.id, figure]));
    const unknown = options.figures.filter((id) => !known.has(id));
    if (unknown.length) usage('Unknown figure id(s): ' + unknown.join(', ') + '. Did you run the generator?');
    figures = options.figures.map((id) => known.get(id));
  }
  const browser = await chromium.launch();
  try {
    const figureReview = await reviewFigures(browser, figures);
    const issues = [...figureReview.issues];
    console.log(`Figures: ${figures.length} checked in ${figureReview.variants} SVG variants; ${figureReview.issues.length} issue(s).`);
    if (options.sheets) {
      for (const sheet of await writeSheets(browser, figures, path.resolve(options.sheets))) {
        console.log(`Sheet ${sheet.file}: ${sheet.ids.join(', ')}`);
      }
    }
    if (options.all) {
      const server = await serveRepository();
      const base = `http://127.0.0.1:${server.address().port}`;
      try {
        const viewer = await reviewViewer(browser, base, figures, options.previews);
        issues.push(...viewer.issues);
        console.log(`Viewer: ${viewer.frameStates} frame states at ${VIEWPORTS.map((v) => v.width + 'px').join(' and ')} in both themes; ${viewer.issues.length} issue(s).`);
        if (options.write) {
          const reportPath = path.join(VISUALS, 'data', 'browser-review.json');
          const previous = JSON.parse(fs.readFileSync(reportPath, 'utf8'));
          const report = {
            svg_variants: figureReview.variants,
            tested_frame_states: viewer.frameStates,
            screenshots: viewer.screenshots.length ? viewer.screenshots : previous.screenshots,
            issues,
            viewports: VIEWPORTS,
            themes: THEMES,
            review_mode: 'Chromium over loopback HTTP; direct file URL navigation blocked by managed browser policy',
            date: new Date().toISOString().slice(0, 10),
          };
          fs.writeFileSync(reportPath, JSON.stringify(report, null, 2) + '\n');
          console.log('Wrote ' + path.relative(ROOT, reportPath));
        }
      } finally {
        server.close();
      }
    }
    issues.forEach((issue) => console.log('ISSUE ' + issue));
    console.log(issues.length ? `${issues.length} issue(s) found.` : 'No issues found.');
    process.exitCode = issues.length ? 1 : 0;
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(2);
});
