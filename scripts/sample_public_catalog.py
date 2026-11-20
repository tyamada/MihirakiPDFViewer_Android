#!/usr/bin/env python3
"""Check central language links and reject individual manga links on public entry points.

No page generation, PDF download or app catalog rewriting is performed.
"""
import argparse
from html.parser import HTMLParser
import json
from pathlib import Path
import re
from urllib.parse import unquote,urlsplit


def is_manga_url(url):
    name=unquote(urlsplit(url).path).rsplit('/',1)[-1].lower()
    return name.startswith('tameshibu') and name.endswith('.pdf')


BASE = 'https://tyamada.github.io/manga-prompt-kit/samples/'
ALIASES = {'zh':'zh-Hans', 'zh-CN':'zh-Hans', 'zh_hans':'zh-Hans', 'zh-TW':'zh-Hant',
           'zh-HK':'zh-Hant', 'zh_hant':'zh-Hant', 'zh_hk':'zh-Hant', 'tl':'fil', 'in':'id', 'es_419':'es', 'pt_pt':'pt'}
LANGUAGES = {'en','ja','ko','zh-Hans','zh-Hant','de','fr','es','pt','vi','fil','ne','id','it'}


def destination(path):
    if path.name in ('README.md', 'SAMPLE_PDF.md'): return BASE + 'en/'
    if path.name in ('README_ja.md', 'SAMPLE_PDF_ja.md'): return BASE + 'ja/'
    stem = path.stem
    code = stem.split('_',1)[1] if '_' in stem else 'en'
    code = ALIASES.get(code,code)
    if code == 'ms': return BASE + 'en/'
    if code not in LANGUAGES: raise ValueError(f'Unknown public page language: {path}')
    anchor = ('#zh_hk' if ('zh_hk' in stem or 'zh-HK' in stem) else '#zh_tw') if code == 'zh-Hant' else ('#es_419' if 'es_419' in stem else '#es') if code == 'es' else ('#pt_pt' if 'pt_pt' in stem else '#pt') if code == 'pt' else ''
    return BASE + code + '/' + anchor


def target_pages(root):
    return [root/name for name in ['README.md','README_ja.md','SAMPLE_PDF.md','SAMPLE_PDF_ja.md']] + sorted(set((root/'docs').glob('index*.html')) | set((root/'docs').glob('support*.html')))


class Links(HTMLParser):
    def __init__(self,text):
        super().__init__(convert_charrefs=True);self.links=[];self.feed(text)
    def handle_starttag(self,tag,attrs):
        self.links += [value for key,value in attrs if key in ('href','src','data') and value]


def page_links(path,text):
    if path.suffix == '.html': links = Links(text).links
    else: links = re.findall(r'\]\(([^)]+)\)',text) + re.findall(r'(?:href|src)=["\']([^"\']+)',text)
    return links


def direct_links(path,text):
    return [url for url in page_links(path,text) if is_manga_url(url)]


def verify_public_lists(root):
    rows=[]
    for path in target_pages(root):
        if not path.is_file(): raise ValueError(f'Missing public page: {path}')
        text=path.read_text(); expected=destination(path)
        if expected not in page_links(path,text): raise ValueError(f'{path}: missing centralized destination {expected}')
        if '_ms.' in path.name and not re.search(r'bahasa Inggeris|English',text,re.I):
            raise ValueError(f'{path}: identify the English download guide for Malay readers')
        rows.append({'file':path.relative_to(root).as_posix(),'destination':expected})
    # Privacy/legal pages need not advertise samples, but may not retain individual manga links.
    checked=set(target_pages(root)) | set((root/'docs').glob('*.html')) | set(root.glob('README*.md'))
    for path in checked:
        found=direct_links(path,path.read_text())
        if found: raise ValueError(f'{path}: individual manga PDF links are not allowed: {found}')
    return {'public_pages_checked':len(checked),'public_pdf_links':0,'public_list_order':'centralized language pages', 'public_lists':rows}


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.parse_args()
    print(json.dumps(verify_public_lists(Path(__file__).resolve().parents[1]),ensure_ascii=False,indent=2))

if __name__ == '__main__': main()
