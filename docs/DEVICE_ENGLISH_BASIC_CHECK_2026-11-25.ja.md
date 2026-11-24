# moto g24 English基本動作確認（2026-11-25）

対象はAndroid版見開きPDFビューア。端末から取得したインストール版は2.1.10（53）、Android 14。ソース既定の版番号ではなく実機の情報を採用した。ユーザーに対象と端末の使用可否を確認してから操作した。

## 言語・端末設定

開始時からシステム言語は `en-US`、言語一覧は `en-US,ja-JP`（English優先、日本語第2言語）。今回、言語を変更する必要はなかった。終了時にも両値が開始時と一致した。自動回転=1、user_rotation=0を記録し、横向き確認後に両値へ復元、縦向き表示を確認した。端末データの消去、再インストール、購入、ストア設定変更は行っていない。

## 確認結果

| 項目 | 結果・範囲 |
|---|---|
| 起動・チュートリアル | Getting Started、Open PDF、Sample PDFs、Previous/Next/Skipを英語で表示。SkipでViewerへ進めた。チュートリアル全ページの精査は未実施。 |
| 既存PDF | 保存済み韓国語マンガを描画。スワイプで1/8から2/8へ移動。文書本文・タイトルの韓国語はPDF由来でありUIの英語表示と区別。 |
| ツールバー | 中央タップで表示。Table of contents、Sample PDFs、Open PDF、Settings、Search PDF、Previous/Next page、Page navigationを英語で確認。検索・目次・スライダーの個別操作は未実施。 |
| Open PDF | 英語のシステムファイル選択から既存のenglish-verification-20261125.pdfを開き、1/3を描画。ファイル内容は変更していない。 |
| 回転 | 同PDFが横向きでも描画され、元の回転設定へ戻した後に縦向き表示を確認。見開きの各設定組合せは未実施。 |
| Settings | Display settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document information等を英語で表示。値は変更していない。 |
| Help | 英語のセクション見出しとGetting Startedの展開・説明表示、戻る操作を確認。 |
| Sample PDFs | Englishフィルター、最新話順、3話分の英語タイトル・Downloadボタン・クレジットを確認。ダウンロードは実行していない。 |
| Version | 2.1.10、Build number 53を確認。取得済みBRONZE詳細を開き、英語説明・Closeで閉じられた。 |
| Support | 無料機能・こよみ無料付与説明を英語で表示。スクロールしてBRONZE/SILVER/GOLDのPurchasedを確認。価格は¥200/¥500/¥1,000。Englishは言語設定であり地域変更ではない。 |

今回の基本操作中にクラッシュ・ANRの画面や操作停止は観測しなかった。網羅的な安定性検証やPlay Consoleの障害統計確認ではない。

購入・新規状態からの復元・TalkBackの読み上げ/フォーカス・全翻訳/大文字サイズ・ネットワークダウンロードは今回未実施。既存の検証結果は履歴として保持し、今回実施済みと混同しない。現地アカウントによる実インストール確認は保留を維持する。

UI XMLとスクリーンショットは開発者のローカル検証記録に保持。対象画面確認前に取得した01-launch.xmlは別アプリのためViewerの合格根拠に含めない。実際のViewer記録は02-viewer.xml以降。端末識別子を公開資料に記載していない。
