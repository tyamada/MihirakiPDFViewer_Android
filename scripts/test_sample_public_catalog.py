"""Public entry-point checks use small text fixtures and never download manga PDFs."""
from pathlib import Path
import tempfile
import unittest
import sample_public_catalog as catalog

class PublicSampleCatalogTests(unittest.TestCase):
    def fixture(self,root):
        (root/'docs').mkdir()
        for name in ['README.md','README_ja.md','SAMPLE_PDF.md','SAMPLE_PDF_ja.md','docs/index.html','docs/support_ja.html']:
            p=root/name;p.write_text(f'<a href="{catalog.destination(p)}">Samples</a>' if p.suffix=='.html' else f'[Samples]({catalog.destination(p)})')
    def test_all_entrypoints_are_checked(self):
        with tempfile.TemporaryDirectory() as d:
            r=Path(d);self.fixture(r);self.assertEqual(0,catalog.verify_public_lists(r)['public_pdf_links'])
            (r/'docs/support_ja.html').write_text('No link')
            with self.assertRaisesRegex(ValueError,'centralized'):catalog.verify_public_lists(r)
    def test_direct_pdf_in_privacy_or_readme_is_rejected(self):
        for target in ['docs/privacy.html','README.md','SAMPLE_PDF_ja.md']:
            with tempfile.TemporaryDirectory() as d:
                r=Path(d);self.fixture(r);p=r/target
                with p.open('a') as f:f.write('<a href="testdata/tameshibu_episode1_2_ja.pdf">PDF</a>')
                with self.assertRaisesRegex(ValueError,'individual manga'):catalog.verify_public_lists(r)
    def test_aliases_and_region_anchors(self):
        for file,end in [('index_zh-CN.html','zh-Hans/'),('support_zh-TW.html','zh-Hant/#zh_tw'),('index_zh_hk.html','zh-Hant/#zh_hk'),('index_tl.html','fil/'),('support_in.html','id/'),('index_ms.html','en/')]:
            self.assertEqual(catalog.BASE+end,catalog.destination(Path(file)))
    def test_other_pdfs_and_links_are_preserved(self):
        with tempfile.TemporaryDirectory() as d:
            r=Path(d);self.fixture(r);p=r/'docs/index.html';p.write_text(p.read_text()+'<a href="regression.pdf">test</a><a href="https://youtube.com/">video</a>');catalog.verify_public_lists(r)
    def test_missing_page_and_unknown_locale_fail(self):
        with tempfile.TemporaryDirectory() as d:
            r=Path(d);self.fixture(r);(r/'SAMPLE_PDF.md').unlink()
            with self.assertRaisesRegex(ValueError,'Missing public'):catalog.verify_public_lists(r)
        with self.assertRaisesRegex(ValueError,'Unknown public'):catalog.destination(Path('index_new.html'))

if __name__=='__main__':unittest.main()
