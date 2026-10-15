"""Safety/QA regression checks; no emulator or adb is used."""
from pathlib import Path
import sys
import json
import tempfile
import unittest
from unittest.mock import patch

from PIL import Image, ImageDraw
import capture_store_screenshots as capture


class CaptureSafetyTest(unittest.TestCase):
    def test_missing_source_is_fatal_before_build_or_device_operations(self):
        original = Path.is_file
        def is_file(path):
            return False if path.name == 'tameshibu_episode1_2_ko.pdf' else original(path)
        with patch.object(sys, 'argv', ['capture', '--device', 'Smartphone_6.3', '--language', 'ja']), \
                patch.object(Path, 'is_file', is_file), patch.object(capture, 'run') as run, \
                patch.object(capture.subprocess, 'run') as subprocess_run:
            with self.assertRaisesRegex(RuntimeError, 'FATAL: source PDFs missing.*ko.pdf'):
                capture.main()
            run.assert_not_called()
            subprocess_run.assert_not_called()

    def test_resume_rejects_stale_corrupt_or_wrong_scene_outputs(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'screen.png'
            record = path.with_suffix('.json')
            image = Image.new('RGB', (400, 700), 'white')
            ImageDraw.Draw(image).rectangle((40, 100, 360, 500), fill='black')
            image.save(path)
            expected = dict(fingerprint='current', pdfSha256='source', avd='Smartphone_6.3',
                            language='ja', scene='01_reader', portrait=True)
            record.write_text(json.dumps(dict(expected, imageSha256=capture.sha(path))))
            self.assertTrue(capture.can_resume(path, record, expected))
            self.assertFalse(capture.can_resume(path, record, dict(expected, fingerprint='new APK')))
            self.assertTrue(capture.can_resume(path, record, dict(expected, fingerprint='compatible transport fix'), ('current',)))
            self.assertFalse(capture.can_resume(path, record, dict(expected, scene='04_search')))
            path.write_bytes(b'broken PNG')
            self.assertFalse(capture.can_resume(path, record, expected))
            record.write_text('{invalid JSON')
            self.assertFalse(capture.can_resume(path, record, expected))

    def test_blank_or_wrong_orientation_images_are_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'screen.png'
            for color in ['white', 'black']:
                Image.new('RGB', (400, 700), color).save(path)
                with self.assertRaisesRegex(RuntimeError, 'Blank/black/white'):
                    capture.validate_image(path, 'portrait')
            image = Image.new('RGB', (400, 700), 'white')
            draw = ImageDraw.Draw(image)
            for y in range(100, 600, 60):
                draw.rectangle((30, y, 370, y + 20), fill='black')
            image.save(path)
            capture.validate_image(path, 'portrait')
            with self.assertRaisesRegex(RuntimeError, 'orientation mismatch'):
                capture.validate_image(path, 'landscape')
            with self.assertRaisesRegex(RuntimeError, 'highlight missing'):
                capture.validate_image(path, 'portrait', search=True)


if __name__ == '__main__':
    unittest.main()
