# 追加スクリーンショット撮影計画

2026-10-16。対象: 繁体字中国語 (`zh_tw`)、ドイツ語 (`de`)、フランス語 (`fr`)。
現在の対象版はversionCode 32。既存の108枚はversionCode 31として保持する。

| 端末 | 3言語の撮影枚数 |
|---|---:|
| Smartphone_6.3 | 21 |
| Tablet_7 | 21 |
| Tablet_10 | 21 |
| Chromebook_FHD | 18 |
| 合計 | 81 |

1. `docs/sample` から3言語のPDFを `testdata` にコピーし、ハッシュ・8ページ・目次8項目・先頭目次名・6ページ目の検索語を確認する。
2. 撮影用StoreLocaleSetterの許可言語と検索候補を追加し、選択した言語のPDFのみを事前確認する。対象版を32に合わせる。
3. APKをビルドし、スマホとChromebookの検索・目次12枚で先行確認する。
4. 各言語についてスマホ、7インチ、10インチ、Chromebookの順に全画面を撮影する。連続した言語切替でapp_processが134となる既知事象を避けるため、端末と言語ごとに別プロセスで撮影し、終了後5秒待つ。
5. 端末ごとに `.artifacts/store-capture/additional-20261016/outputs` に画像とJSONをバックアップする。
6. `python3 scripts/review_store_screenshots.py --language zh_tw de fr` で自動検品し、全81枚と12組の一覧を目視確認する。確認後だけ `--mark-reviewed` を使用する。
7. 追加分の専用レポートを保存し、既存108枚の画像・JSONのハッシュ不変を確認する。端末の表示設定復元と撮影エミュレーターの終了を確認する。

## 実行コマンド

```sh
./gradlew assembleDebug assembleDebugAndroidTest
python3 .artifacts/store-capture/additional-20261016/run_additional.py
python3 scripts/review_store_screenshots.py --language zh_tw de fr
```

バックアップ付き実行処理は、以下のコマンドを各端末・各言語に対して順番に実行する。

```sh
python3 scripts/capture_store_screenshots.py --skip-build --device <端末名> --language <言語コード> --resume
```

## 範囲と注意事項

PDFのない6言語は今回の対象外。既存108枚と追加81枚は版が異なるため、189枚を同一版の完成セットとして扱わない。
検品スクリプトの母数は13言語・351枚のため、追加81枚だけの検品では全体未完了と表示される。
既存の全体レポートを上書きせず、部分レポートの出力を追加分専用レポートへ保存する。
目次見出しと一部設定名は現行アプリの英語表記を維持する。
7インチタブレットの横画面目次は既存日本語画像でも背景が黒く、スクロール領域の上部だけが見える。撮影時の制限として記録する。
Play Consoleへの登録は対象外。

## 途中復旧

撮影中に並行処理と思われる操作により追加設定・計画書・生成済み追加画像が削除された。ユーザーの「意図したものではないので、復元して再開」の指示により設定を復元し、追加分を再撮影する。既存108枚の原本と記録はハッシュ一致を確認済み。

## 目視確認で検出した本文言語の不一致

言語切替後の最初の見開き撮影で、記録上のPDFハッシュと言語は正しい一方、画像に前の言語の本文が残る事象を検出した（Tablet_7のドイツ語、Tablet_10のフランス語）。アプリの前回閲覧状態・起動時の読み込みが関係する可能性があるが、根本原因は未確定。自動検査だけで合格扱いにせず全原本で本文言語を照合する。
該当する撮影専用エミュレーター上のアプリデータを `pm clear` で初期化し、PDFを再転送して2枚だけを撮り直す。既存画像や実端末のデータは対象外。実行補助は `.artifacts/store-capture/additional-20261016/recapture_selected.py`。
