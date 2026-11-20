"""External cache validation uses tiny fixtures; real PDFs are opt-in integration inputs."""
from pathlib import Path
import tempfile,hashlib,json,unittest
from unittest.mock import patch
import sample_pdf_inputs as pdf

class VerifiedInputTests(unittest.TestCase):
    def fixture(self,base):
        root=base/'repo';root.mkdir();cache=base/'cache';cache.mkdir();data=b'%PDF-1.5\nminimal fixture\n';name='tameshibu_episode1_3_tl.pdf'
        row={'sample_id':'ep1_fil','filename':name,'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest(),'new_download_url':pdf.PREFIX+name}
        p=root/pdf.MANIFEST;p.parent.mkdir(parents=True);p.write_text(json.dumps({'entries':[row]}));(cache/name).write_bytes(data)
        return root,cache,row,data
    def test_verified_file_and_alias(self):
        with tempfile.TemporaryDirectory() as d:
            r,c,e,_=self.fixture(Path(d));self.assertEqual((c/e['filename']).resolve(),pdf.resolve_inputs(r,['ep1_fil'],c)['ep1_fil']);self.assertEqual('ep1_fil',pdf.sample_id('tl'));self.assertEqual('ep1_id',pdf.sample_id('in'))
    def test_missing_and_same_size_corruption_fail(self):
        with tempfile.TemporaryDirectory() as d:
            r,c,e,data=self.fixture(Path(d));p=c/e['filename'];p.write_bytes(data[:-1]+b'x')
            with self.assertRaisesRegex(RuntimeError,'SHA-256'):pdf.resolve_inputs(r,['ep1_fil'],c)
            p.unlink()
            with self.assertRaisesRegex(RuntimeError,'missing'):pdf.resolve_inputs(r,['ep1_fil'],c)
    def test_unknown_repository_and_unsafe_url_fail(self):
        with tempfile.TemporaryDirectory() as d:
            r,c,e,_=self.fixture(Path(d))
            with self.assertRaisesRegex(RuntimeError,'outside'):pdf.resolve_inputs(r,['ep1_fil'],r/'testdata')
            with self.assertRaisesRegex(RuntimeError,'Unknown'):pdf.resolve_inputs(r,['ep1_missing'],c)
            e['new_download_url']='http://example.org/'+e['filename'];(r/pdf.MANIFEST).write_text(json.dumps({'entries':[e]}))
            with self.assertRaisesRegex(RuntimeError,'pinned'):pdf.resolve_inputs(r,['ep1_fil'],c)
    def test_download_only_missing_and_never_overwrites_bad_input(self):
        with tempfile.TemporaryDirectory() as d:
            r,c,e,data=self.fixture(Path(d));p=c/e['filename'];p.unlink()
            def download(args,**kw):Path(args[args.index('--output')+1]).write_bytes(data)
            with patch.object(pdf.subprocess,'run',side_effect=download) as run:pdf.prepare_inputs(r,['ep1_fil'],c,True);self.assertEqual(1,run.call_count)
            with patch.object(pdf.subprocess,'run') as run:pdf.prepare_inputs(r,['ep1_fil'],c,True);run.assert_not_called()
            p.write_bytes(b'bad')
            with patch.object(pdf.subprocess,'run') as run:
                with self.assertRaises(RuntimeError):pdf.prepare_inputs(r,['ep1_fil'],c,True)
                run.assert_not_called();self.assertEqual(b'bad',p.read_bytes())

if __name__=='__main__':unittest.main()
