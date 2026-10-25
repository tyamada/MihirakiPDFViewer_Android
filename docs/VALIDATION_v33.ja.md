# versionCode 33：PDF統一と動作確認

2026-10-16。作業計画1（PDFの正本統一）・2（versionCode 33の動作確認）の結果。

## PDF

`testdata/` を正本としてカタログ28件を照合し、配布用の `docs/sample/` と全件のSHA-256が一致することを確認した。

- 日本語・英語・韓国語の第1・第2話、簡体字中国語の第1・第2話、繁体字中国語の第2話の配布用コピーを正本へ同期（計9件）。
- ドイツ語・フランス語の第2話は正本側に存在しなかったため、既存の配布用PDFを `testdata/` に追加（計2件）。
- カタログの14言語・各2話、IDとファイル名の一意性、URLとローカル配布先の対応を単体テストで確認。
- 照合結果は `.artifacts/sample-validation-v33/pdf-hashes.json` に保存。

## 実装レビューと修正

既存のversionCode 33、サンプル一覧の追加7言語、言語選択表示を確認した。イタリア語リソースの追加に伴うヘルプ翻訳不足を補完し、8言語で不足していた目次見出しの翻訳と、ViewerScreenのインデントを修正した。言語選択欄へテスト用の識別タグを追加した。

署名設定は作業開始時の `local.properties` を使用する設定を維持した。今回の検証対象はデバッグAPKであり、リリース署名の検証は含まない。

## 検証結果

| 検証 | 結果 |
|---|---|
| `assembleDebug` | 成功。APKメタデータのversionCode 33を確認 |
| `assembleDebugAndroidTest` | 成功 |
| `testDebugUnitTest` | 9件合格 |
| `lintDebug` | 成功。エラー0件、警告126件 |
| Pythonスクリプト単体テスト | 10件合格 |
| `SamplePdfSmokeTest` | Smartphone_6.3で3件合格 |
| テキストの `git diff --check` | 合格（PDFは対象外） |

Lintはスペイン語のTypos警告の修正候補生成時に内部例外を出力するが、タスクは正常終了しHTML/XMLレポートを生成した。PDF内の空白はファイル形式の一部なので整形していない。

端末テストでは28件をアプリ専用領域へ転送し、SHA-256を再照合した。追加7言語（es、it、pt、vi、id、fil、ne）について一覧・絞り込み、各2話の描画、8ページと目次8項目、目次の遷移先、ページ移動を確認した。第1話では6ページ目の検索一致とハイライト座標の存在を確認した。イタリア語の検索・目次・設定リソースはイタリア語Configurationで解決を確認した。

初回のテストは復元された閲覧状態、読み取り専用欄の識別、非同期描画の待機条件で失敗したため、テストを修正した。最終実行は `OK (3 tests)`。ログは `.artifacts/sample-validation-v33/instrumentation.log`。

再実行手順は [開発・実機テストガイド](../DEV_TESTING_GUIDE.md) を参照。

## 撮影・検品結果

撮影設定をversionCode 33に更新し、4種類のAVD（Smartphone_6.3、Tablet_7、Tablet_10、Chromebook_FHD）と14言語（ja、en、ko、zh_tw、de、fr、es、pt、zh_cn、vi、fil、ne、id、it）で全378枚を撮影した。すべての撮影ジョブが終了コード0で完了し、PNG 378枚とJSON 378件のversionCode、画像SHA-256、PDF SHA-256、画面寸法を照合した。欠落・不一致は0件。

全378枚の自動検査に合格した。検索画面の本文ヒット、目次、Chromebookの最大化状態などを記録で検証し、56組の端末・言語別コンタクトシートを生成して目視確認済み。詳細と原本リンクは[スクリーンショット検品結果](../store_listing/SCREENSHOT_CAPTURE_REPORT.ja.md)を参照。旧撮影runnerのfingerprintを持つ一部記録も、versionCode・PDF・画像ハッシュを再検証したうえで検品対象に含めた。

Play Consoleへの登録と、配布用PDFの公開反映は未実施。
