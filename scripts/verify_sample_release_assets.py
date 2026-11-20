#!/usr/bin/env python3
"""Validate the pinned manga-prompt-kit release and unchanged Viewer cache identities.

No PDF is required in the repository. --remote verifies all 51 downloads;
--download-dir optionally retains verified PDFs outside docs/ and testdata/.
Live verification requires curl and Poppler pdfinfo.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import csv
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile

from sample_public_catalog import verify_public_lists
from sample_pdf_inputs import prepare_inputs


def verify(root, remote=False, download_dir=None):
    evidence = root / 'docs/sample-releases/sample-manga-v2'
    manifest = json.loads((evidence / 'manifest.json').read_text())
    handoff = list(csv.DictReader((evidence / 'android-handoff.csv').open(encoding='utf-8-sig')))
    public = json.loads((evidence / 'public-verification.json').read_text())
    checksums = dict(line.split('  ', 1)[::-1] for line in (evidence / 'SHA256SUMS').read_text().splitlines())
    assert manifest['repository'] == public['repository'] == 'tyamada/MihirakiPDFViewer_Android'
    assert manifest['fixed_tag'] == public['fixed_tag'] == 'sample-manga-v2'
    entries = manifest['entries']
    assert manifest['pdf_count'] == len(entries) == len(handoff) == len(checksums) == 51
    assert public['all_passed'] and public['pdf_passed'] == 51 and public['sha256sums_match']
    rows = {row['sample_id']: row for row in handoff}
    assert len(rows) == 51
    for entry in entries:
        assert all(str(value) == rows[entry['sample_id']][key] for key, value in entry.items())
        assert checksums[entry['filename']] == entry['sha256']
    assert sum(e['change_type'] == 'revised' for e in entries) == 33
    assert sum(e['change_type'] == 'unchanged' for e in entries) == 9
    assert sum(e['change_type'] == 'new_regional_variant' for e in entries) == 9
    # Keep the old handoff immutable and prove the migration changes only the source URL.
    legacy_entries = {entry['sample_id']: entry for entry in entries}
    current = root / 'docs/sample-releases/manga-pdf-20261103-v1'
    manifest = json.loads((current / 'pdf-manifest.json').read_text())
    public = json.loads((current / 'pdf-public-verification.json').read_text())
    checksums = dict(line.split('  ', 1)[::-1] for line in (current / 'SHA256SUMS').read_text().splitlines())
    migration = list(csv.DictReader((current / 'pdf-migration.csv').open(encoding='utf-8-sig')))
    viewer_rows = [row for row in migration if row['source_repository'] == 'tyamada/MihirakiPDFViewer_Android']
    assert len(viewer_rows) == len({row['sample_id'] for row in viewer_rows}) == 51
    rows = {row['sample_id']: row for row in viewer_rows}
    assert manifest['repository'] == public['repository'] == 'tyamada/manga-prompt-kit'
    assert manifest['fixed_tag'] == public['tag'] == 'manga-pdf-20261103-v1'
    assert manifest['status'] == 'published' and manifest['local_hashes_match_legacy_release_metadata']
    assert public['all_passed'] and public['pdf_count'] == 51 and public['asset_count'] == 55
    assert len(checksums) == 54  # The checksum file lists the other 54 assets, not itself.
    entries = manifest['entries']
    assert manifest['pdf_count'] == len(entries) == 51
    assert set(legacy_entries) == {entry['sample_id'] for entry in entries} == set(rows)
    for filename in ('pdf-manifest.json', 'pdf-migration.csv', 'LICENSE.md'):
        assert hashlib.sha256((current / filename).read_bytes()).hexdigest() == checksums[filename]
    for entry in entries:
        previous = legacy_entries[entry['sample_id']]
        row = rows[entry['sample_id']]
        for key in ('filename', 'bytes', 'sha256', 'pages', 'locale', 'language_key', 'title', 'episode'):
            assert entry[key] == previous[key], (entry['sample_id'], key, 'migration must keep PDF and identity')
        assert row['old_filename'] == row['new_filename'] == entry['filename']
        assert row['old_url'] == previous['fixed_download_url']
        assert row['new_download_url'] == entry['new_download_url']
        assert row['language_key'] == entry['language_key'] and row['sha256'] == entry['sha256']
        assert row['new_repository'] == manifest['repository'] and row['new_tag_candidate'] == manifest['fixed_tag']
        assert checksums[entry['filename']] == entry['sha256']
        # Normalize for the shared catalog and HTTPS checks below, without rewriting evidence.
        entry['fixed_download_url'] = entry['new_download_url']
    for code in {entry['language_key'] for entry in entries}:
        assert {entry['episode'] for entry in entries if entry['language_key'] == code} == {1, 2, 3}
    assert all(entry['language_key'] == 'fil' and entry['filename'].endswith('_tl.pdf')
               for entry in entries if entry['sample_id'].endswith('_fil'))
    public_assets = {check['filename']: check for check in public['results']}
    assert len(public_assets) == len(public['results']) == 55
    for filename in ('pdf-manifest.json', 'pdf-migration.csv', 'LICENSE.md', 'SHA256SUMS'):
        asset = (current / filename).read_bytes()
        assert hashlib.sha256(asset).hexdigest() == public_assets[filename]['sha256']
        assert len(asset) == public_assets[filename]['bytes']
    for filename, expected in checksums.items():
        check = public_assets[filename]
        assert check['anonymous'] and check['final_https'] and check['http_status'] == 200 and check['matches_local']
        assert check['sha256'] == expected
    catalog = next((root / 'app/src/main/java').rglob('SamplePdfCatalog.kt')).read_text()
    prefix = re.search(r'private const val BASE_URL = "([^"]+)"', catalog)[1]
    assert prefix == 'https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261103-v1/'
    blocks = re.findall(r'^        SamplePdfItem\(\n(.*?)\n        \)', catalog, re.M | re.S)
    assert len(blocks) == 51
    by_id = {e['sample_id']: e for e in entries}
    for block in blocks:
        literals = dict((key, json.loads(value)) for key, value in re.findall(r'(\w+) = ("(?:[^"\\]|\\.)*")', block))
        entry = by_id[literals['id']]
        for key, source in [('title', 'title'), ('fileName', 'filename'), ('languageCode', 'language_key'), ('sha256', 'sha256'), ('locale', 'locale')]:
            assert literals[key] == entry[source], (entry['sample_id'], key)
        assert literals['url'].replace('${BASE_URL}', prefix) == entry['fixed_download_url']
        assert literals['fileSize'] == f"{entry['bytes']/1048576:.1f} MiB"
        assert int(re.search(r'bytes = (\d+)L', block)[1]) == entry['bytes']
        assert int(re.search(r'pageCount = (\d+)', block)[1]) == entry['pages']
        assert f'SamplePdfLanguages.displayName("{entry["language_key"]}")' in block
    for field in ['sample_id', 'filename', 'fixed_download_url']:
        assert len(set(e[field] for e in entries)) == 51, field
    assert len(set(e['language_key'] for e in entries)) == 17
    for entry in entries:
        check = public_assets[entry['filename']]
        assert check['sha256'] == entry['sha256'] and check['bytes'] == entry['bytes']
        assert check['pages'] == entry['pages'] == 8 and check['url'] == entry['new_download_url']
    public_lists = verify_public_lists(root)
    links = public_lists['public_pdf_links']

    def check(entry):
        result = dict(name=entry['filename'], bytes=entry['bytes'], sha256=entry['sha256'])
        if remote:
            with tempfile.TemporaryDirectory(prefix='viewer-pdf-') as directory:
                target = (download_dir or Path(directory)) / entry['filename']
                target.parent.mkdir(parents=True, exist_ok=True)
                target = prepare_inputs(root, [entry['sample_id']], target.parent, download=True)[entry['sample_id']]
                info = subprocess.run(['pdfinfo', str(target)], check=True, capture_output=True, text=True)
                assert re.search(r'^Pages:\s+8$', info.stdout, re.M), entry['filename']
                result['remote_verified'] = True
                result['pages'] = 8
        return result
    with ThreadPoolExecutor(max_workers=4) as pool:
        results = list(pool.map(check, entries))
    return dict(repository=manifest['repository'], tag=manifest['fixed_tag'], pdf_count=51,
                region_count=17, public_pdf_links=links,
                public_pages_checked=public_lists['public_pages_checked'],
                public_list_order=public_lists['public_list_order'], remote=remote, files=results)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--remote', action='store_true')
    parser.add_argument('--download-dir', type=Path)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    if args.download_dir and not args.remote:
        parser.error('--download-dir requires --remote')
    root = Path(__file__).resolve().parent.parent
    if args.download_dir:
        destination = args.download_dir.resolve()
        if destination.is_relative_to(root / 'docs') or destination.is_relative_to(root / 'testdata'):
            parser.error('Keep downloaded PDFs outside docs/ and testdata/')
    result = verify(root, args.remote, args.download_dir)
    if args.output:
        args.output.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps({key:value for key,value in result.items() if key != 'files'}, indent=2))
