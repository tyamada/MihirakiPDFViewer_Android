#!/usr/bin/env python3
"""Prepare only selected fixed-release PDFs in an external work cache; no app/ADB operation."""
import argparse
import json
from pathlib import Path
from sample_pdf_inputs import entries, prepare_inputs


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--input-dir', type=Path, required=True)
    parser.add_argument('--id', action='append', dest='ids', help='Stable sample ID, repeat as needed; required unless --all')
    parser.add_argument('--all', action='store_true', help='Explicitly select all 51 PDFs for full integration verification')
    parser.add_argument('--download', action='store_true', help='Download missing selected inputs; existing mismatches are never overwritten')
    args = parser.parse_args(); root = Path(__file__).resolve().parents[1]
    if bool(args.ids) == args.all:
        parser.error('Choose either repeated --id values or --all')
    ids = list(entries(root)) if args.all else args.ids
    result = prepare_inputs(root, ids, args.input_dir, args.download)
    print(json.dumps({'verified_count':len(result), 'files':{key:str(value) for key,value in result.items()}}, indent=2))

if __name__ == '__main__':
    main()
