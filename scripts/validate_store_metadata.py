#!/usr/bin/env python3
"""Check the first three store fields; report actual values, not draft report values."""
from pathlib import Path
import re, json
root = Path(__file__).resolve().parents[1]
rows = []
for f in sorted((root / 'store_listing').glob('STORE_LISTING.*.md')):
    sections = re.split(r'^## ', f.read_text(), flags=re.M)[1:]
    assert len(sections) >= 3, f.name
    values = [s.partition('\n')[2].strip() for s in sections[:3]]
    sizes = list(map(len, values))
    rows.append({'file': f.name, 'title': values[0], 'lengths': sizes, 'valid': all(0 < n <= limit for n, limit in zip(sizes, [30,80,4000]))})
print(json.dumps(rows, ensure_ascii=False, indent=2))
raise SystemExit(0 if rows and all(r['valid'] for r in rows) else 1)
