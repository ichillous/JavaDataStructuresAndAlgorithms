#!/usr/bin/env python3
"""Check repository-relative Markdown file links, without network requests.

Fragments and external URLs are deliberately outside this check. Includes tracked
and new nonignored Markdown, and ignores fenced code examples.
"""
from pathlib import Path
import re
import subprocess
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
LINK = re.compile(r"!?\[[^\]\n]*\]\(\s*(<[^>]+>|[^\s)]+)(?:\s+[^)]*)?\)")
REFERENCE = re.compile(r"^\s{0,3}\[[^\]]+\]:\s*(<[^>]+>|\S+)", re.MULTILINE)


def main():
    names = subprocess.check_output(
        ["git", "ls-files", "-z", "--cached", "--others", "--exclude-standard"], cwd=ROOT
    ).decode().split("\0")
    errors = []
    checked = 0
    for name in sorted(set(names)):
        path = ROOT / name
        if not name.endswith(".md") or not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        text = re.sub(r"(?ms)^\s*(`{3,}|~{3,})[^\n]*\n.*?^\s*\1\s*$", "", text)
        for match in list(LINK.finditer(text)) + list(REFERENCE.finditer(text)):
            target = match.group(1).strip("<>")
            parsed = urlsplit(target)
            if parsed.scheme or parsed.netloc or not parsed.path:
                continue
            checked += 1
            destination = (ROOT if parsed.path.startswith("/") else path.parent) / unquote(parsed.path).lstrip("/")
            if not destination.exists():
                errors.append(f"{name}: missing local link {target}")
    for error in errors:
        print(error)
    print(f"Checked {checked} local file links; {len(errors)} broken. External URLs and fragments not checked.")
    return bool(errors)


if __name__ == "__main__":
    raise SystemExit(main())
