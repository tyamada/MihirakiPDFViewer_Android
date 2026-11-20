#!/usr/bin/env python3
"""Resolve verified, pinned manga PDFs from an explicit external work cache."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile

MANIFEST = 'docs/sample-releases/manga-pdf-20261103-v1/pdf-manifest.json'
PREFIX = 'https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261103-v1/'


def sample_id(language, episode=1):
    key = language.lower().replace('-', '_')
    key = {'en_us':'en', 'ja_jp':'ja', 'ko_kr':'ko', 'zh_cn':'zh_cn', 'zh_hans':'zh_cn',
           'zh_tw':'zh_tw', 'zh_hant':'zh_tw', 'tl':'fil', 'in':'id'}.get(key, key)
    return f'ep{episode}_{key}'


def entries(root):
    rows = json.loads((Path(root) / MANIFEST).read_text())['entries']
    result = {row['sample_id']: row for row in rows}
    if len(result) != len(rows):
        raise RuntimeError('Duplicate pinned sample IDs')
    for row in rows:
        name = row['filename']
        if Path(name).name != name or not name.startswith('tameshibu') or not name.endswith('.pdf'):
            raise RuntimeError('Unsafe sample filename')
        if row['new_download_url'] != PREFIX + name:
            raise RuntimeError('Sample URL is not the pinned release')
    return result


def external_directory(root, input_dir=None):
    value = input_dir or os.environ.get('VIEWER_SAMPLE_PDF_DIR')
    if not value:
        raise RuntimeError('FATAL: set --sample-pdf-dir / external VIEWER_SAMPLE_PDF_DIR; run scripts/prepare_sample_pdfs.py first')
    path = Path(value).expanduser().resolve()
    if path.is_relative_to(Path(root).resolve()):
        raise RuntimeError('Sample work cache must be outside this repository (never docs/ or testdata/)')
    return path


def verify_file(path, entry):
    if not path.is_file() or path.stat().st_size != entry['bytes']:
        raise RuntimeError(f'FATAL: missing or wrong-size sample PDF: {path}')
    with path.open('rb') as stream:
        if stream.read(5) != b'%PDF-':
            raise RuntimeError(f'FATAL: invalid PDF header: {path}')
        stream.seek(0)
        digest = hashlib.file_digest(stream, 'sha256').hexdigest()
    if digest != entry['sha256']:
        raise RuntimeError(f'FATAL: sample PDF SHA-256 mismatch: {path}')
    return path


def resolve_inputs(root, sample_ids, input_dir=None):
    directory = external_directory(root, input_dir)
    pinned = entries(root)
    result = {}
    for ident in dict.fromkeys(sample_ids):
        if ident not in pinned:
            raise RuntimeError(f'Unknown pinned sample ID: {ident}')
        row = pinned[ident]
        result[ident] = verify_file(directory / row['filename'], row)
    return result


def prepare_inputs(root, sample_ids, input_dir, download=False):
    directory = external_directory(root, input_dir)
    pinned = entries(root)
    for ident in dict.fromkeys(sample_ids):
        if ident not in pinned:
            raise RuntimeError(f'Unknown pinned sample ID: {ident}')
        row = pinned[ident]; target = directory / row['filename']
        if target.exists() or not download:
            verify_file(target, row)  # Do not overwrite unknown or corrupt user inputs.
            continue
        directory.mkdir(parents=True, exist_ok=True)
        with tempfile.NamedTemporaryFile(prefix='.viewer-pdf-', suffix='.part', dir=directory, delete=False) as stream:
            temporary = Path(stream.name)
        try:
            subprocess.run(['curl', '--fail', '--silent', '--show-error', '--location', '--proto', '=https',
                            '--proto-redir', '=https', '--connect-timeout', '30', '--max-time', '300',
                            '--output', str(temporary), row['new_download_url']], check=True)
            verify_file(temporary, row)
            temporary.replace(target)
        finally:
            temporary.unlink(missing_ok=True)
    return resolve_inputs(root, sample_ids, directory)
