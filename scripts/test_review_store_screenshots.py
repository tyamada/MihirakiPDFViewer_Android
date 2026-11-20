"""Review output regression tests; no device access."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
from PIL import Image, ImageDraw
import capture_store_screenshots as capture
import review_store_screenshots as review


class ReviewTest(unittest.TestCase):
    def write_capture(self, output, avd, lang, scene, pdf_digest, *, fingerprint='stamp'):
        target = capture.orientation(avd, scene)
        portrait = target == 'portrait'
        size = capture.expected_size(avd, portrait)
        path = output / avd / f'{lang}_{scene}_{target}.png'
        path.parent.mkdir(parents=True, exist_ok=True)
        with Image.new('RGB', size, 'white') as image:
            x = 80 + 30 * capture.selected_scenes(avd).index(scene)
            draw = ImageDraw.Draw(image)
            draw.rectangle((x, 300, x + 330, 750), fill='black')
            draw.rectangle((x + 20, 400, x + 50, 430), fill='red')
            image.save(path)
        record = path.with_suffix('.json')
        record.write_text(json.dumps(dict(
            fingerprint=fingerprint, pdfSha256=pdf_digest, avd=avd, language=lang,
            scene=scene, portrait=portrait, versionCode=capture.CONFIG['versionCode'],
            dimensions=list(size), imageSha256=capture.sha(path), pageCount=capture.CONFIG['pageCount'],
            outlineVerified=True, page6SearchRects=[dict(top=50, bottom=100)],
            validation='automated-pass; visual review pending',
        )))
        return record

    def test_seven_scene_sheet_and_partial_report(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder) / 'repo'
            output = root / 'store_listing/screenshots'
            avd, lang = 'Smartphone_6.3', 'ja'
            pdf = Path(folder) / 'episode-one.pdf'
            pdf.write_bytes(b'%PDF-1.7\nfixture\n')
            digest = capture.sha(pdf)
            for scene in capture.selected_scenes(avd):
                self.write_capture(output, avd, lang, scene, digest)
            args = ['review', '--device', avd, '--language', lang, '--sample-pdf-dir', folder]
            with patch.object(capture, 'ROOT', root), patch.object(capture, 'OUT', output), \
                    patch.object(capture, 'fingerprint', return_value='stamp'), \
                    patch.object(capture, 'resolve_inputs', return_value={'ep1_ja': pdf}) as resolve, \
                    patch.object(sys, 'argv', args):
                review.main()
                resolve.assert_called_once_with(root, ['ep1_ja'], pdf.parent)
            report = root / 'store_listing/SCREENSHOT_CAPTURE_REPORT.partial.ja.md'
            self.assertIn('全体未完了', report.read_text())
            with Image.open(root / f'store_listing/screenshot_review/{avd}-{lang}.jpg') as sheet:
                self.assertEqual((1440, 2550), sheet.size)

    def test_stale_pdf_with_compatible_fingerprint_does_not_mark_any_record_reviewed(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder) / 'repo'
            output = root / 'store_listing/screenshots'
            avd, lang = 'Smartphone_6.3', 'ja'
            pdf = Path(folder) / 'episode-one.pdf'
            pdf.write_bytes(b'%PDF-1.7\nfixture\n')
            first, second = capture.selected_scenes(avd)[:2]
            current = self.write_capture(output, avd, lang, first, capture.sha(pdf))
            stale = self.write_capture(output, avd, lang, second, 'old pdf digest', fingerprint='old capture')
            original_records = {record: record.read_text() for record in (current, stale)}
            args = ['review', '--device', avd, '--language', lang, '--mark-reviewed',
                    '--compatible-fingerprint', 'old capture']
            with patch.object(capture, 'ROOT', root), patch.object(capture, 'OUT', output), \
                    patch.object(capture, 'fingerprint', return_value='stamp'), \
                    patch.object(capture, 'resolve_inputs', return_value={'ep1_ja': pdf}) as resolve, \
                    patch.object(sys, 'argv', args):
                with self.assertRaisesRegex(RuntimeError, f'Missing/stale/invalid capture: .*{stale.stem}\\.png'):
                    review.main()
                resolve.assert_called_once_with(root, ['ep1_ja'], None)
            for record, original in original_records.items():
                self.assertEqual(original, record.read_text())


if __name__ == '__main__':
    unittest.main()
