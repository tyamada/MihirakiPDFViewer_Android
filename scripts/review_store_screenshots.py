#!/usr/bin/env python3
"""Verify all 72 original captures and build contact sheets and a review report."""
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw
import capture_store_screenshots as capture


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--mark-reviewed', action='store_true', help='Only use after visually inspecting all contact sheets and search screenshots')
    options = parser.parse_args()
    apks = [capture.ROOT / 'app/build/outputs/apk/debug/app-debug.apk', capture.ROOT / 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk']
    fingerprint = hashlib.sha256((''.join(capture.sha(p) for p in apks) + capture.sha(Path(capture.__file__))).encode()).hexdigest()
    report_dir = capture.ROOT / 'store_listing/screenshot_review'
    report_dir.mkdir(exist_ok=True)
    rows = []
    seen = set()
    for avd, folder in capture.DEVICES.items():
        for lang in capture.LANGUAGES:
            sheet = Image.new('RGB', (1440, 1700), '#707070')
            draw = ImageDraw.Draw(sheet)
            originals = []
            query = ''
            sizes = set()
            for i, scene in enumerate(capture.SCENES):
                target = capture.orientation(avd, scene)
                path = capture.OUT / folder / f'{lang}_{scene}_{target}.png'
                record = path.with_suffix('.json')
                expected = dict(fingerprint=fingerprint, pdfSha256=capture.sha(capture.ROOT / f'testdata/tameshibu_episode1_2_{lang}.pdf'),
                                avd=avd, language=lang, scene=scene, portrait=target == 'portrait')
                if not capture.can_resume(path, record, expected, capture.compatible_fingerprints(apks)):
                    raise RuntimeError(f'Missing/stale/invalid capture: {path}')
                digest = capture.sha(path)
                if digest in seen:
                    raise RuntimeError(f'Duplicate capture: {path}')
                seen.add(digest)
                info = json.loads(record.read_text())
                if info['pageCount'] != 8 or info['versionCode'] != 29:
                    raise RuntimeError(f'Unexpected PDF or app: {path}')
                if scene == '04_search':
                    query = info['query']
                    if not any(r['top'] > 40 and r['bottom'] < 560 for r in info['page6SearchRects']):
                        raise RuntimeError(f'No dialogue hit: {path}')
                if options.mark_reviewed:
                    info['validation'] = 'automated-pass; visual-reviewed'
                    record.write_text(json.dumps(info, ensure_ascii=False, indent=2) + '\n')
                with Image.open(path) as raw:
                    sizes.add(f'{raw.width}x{raw.height}')
                    image = raw.convert('RGB')
                    image.thumbnail((468, 800))
                    x = i % 3 * 480
                    y = i // 3 * 850
                    sheet.paste(image, (x + (480 - image.width) // 2, y + 35))
                    draw.text((x + 8, y + 10), f'{lang} {scene} {raw.width}x{raw.height}', fill='white')
                originals.append(f'[{i + 1:02}](screenshots/{folder}/{path.name})')
            name = f'{avd}-{lang}.jpg'
            sheet.save(report_dir / name, quality=92)
            rows.append(f'| {avd} | {lang} | ' + ' / '.join(originals) + f' | [一覧](screenshot_review/{name}) | {query} | ' + ', '.join(sorted(sizes)) + ' |')
    status = '自動検査・目視確認済み' if options.mark_reviewed else '自動検査済み。目視確認は未完了'
    text = f'''# スクリーンショット撮影結果

v2.0.0（29）。3デバイス × 4言語 × 6画面 = 72枚。{status}。
Play Consoleへのアップロードは未実施。

画像の原本・撮影記録は `screenshots/[AVD名]/`、確認用の一覧画像は `screenshot_review/` に保存。
原本はOSバーを含む実際の端末画面のPNG。確認用一覧は縮小したJPEGであり、ストア登録には使わない。

| AVD | 言語 | PNG原本（掲載順） | 6枚の一覧 | 検索語 | 解像度 |
| --- | --- | --- | --- | --- | --- |
''' + '\n'.join(rows) + '''

検索語はすべて6ページ目のセリフに含まれ、本文内のハイライトを確認した。
日本語は縦書きの長い語がタイトルだけに一致するため「今」を使用。
撮影条件・PDFのSHA-256・言語・向き・検索矩形は原本と同名のJSONに記録。
韓国語・中国語の一部設定名は、現行アプリの英語表示をそのまま撮影している。システム言語の設定漏れではない。

撮影・再開方法は [SCREENSHOT_AUTOMATION.ja.md](SCREENSHOT_AUTOMATION.ja.md) を参照。

検証: アプリ・テストAPKのビルド、72画像の撮影条件・ハッシュ・画像検査・重複検査、撮影スクリプトの安全性テスト。
再開時も言語・向き・転送状態を確認し、ハッシュと画像検査に合格した完成画像だけをスキップする。

アプリ側では、検索を反映するキャッシュキー、描画中Bitmapの解放タイミング、PDF描画中の文書終了処理を修正した。
言語設定はroot可能な指定Google APIs AVDで検証。別のシステムイメージには再検証が必要。
'''
    output = capture.ROOT / 'store_listing/SCREENSHOT_CAPTURE_REPORT.ja.md'
    output.write_text(text)
    print(f'Validated {len(seen)} originals; wrote {len(rows)} contact sheets and {output}')


if __name__ == '__main__':
    main()
