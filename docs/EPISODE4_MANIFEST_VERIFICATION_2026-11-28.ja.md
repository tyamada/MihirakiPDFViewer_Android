# 第4話・最新版JSONの検証（2026年11月28日）

## 結論と確認範囲

開発mainを基点とする2.1.10（53）のPlay Debugビルドで、最新版JSONの52冊目 `ep4_ja` を一覧表示し、アプリ内ダウンロード・容量/SHA-256検証・8ページの読み込みと描画まで確認した。アプリ本体の変更は不要。既存のネットワークテストの51冊固定条件を拡張し、第4話の固定フィクスチャと任意実行のUIテストを追加した。

- 公開版53：公開用保存ソース `work/viewer53-release` と現行の `SamplePdfManifest.kt` はSHA-256一致（`a616062a3992b0a810f6b95d58c0cf8cec5dea5b4bc58946aafec17c558649d3`）。52冊目と第4話を受け入れることはコード確認済み。今回、Play配信バイナリーを実機で動作検証したわけではない。
- 開発版：コミット `cb3891e86de18a7e13d359a0afb4d31c6c974efa` を基点に検証コードのみ追加。アプリ本体・版番号・配信設定は変更していない。
- エミュレーター実動作：Android 17/API37のPixel_10を読み取り専用・スナップショット保存なしで起動し、Play Debug版を検証。実機Pixel 8aにはインストール・操作していない。
- 実機での第4話確認：未実施。既存の現地アカウント確認などの保留は維持。

## 確認した取得先と整合性

- JSON：https://raw.githubusercontent.com/tyamada/manga-prompt-kit/main/releases/pdf-manifest.json
- schema_version=1、52冊。第4話は日本語版のみ。
- sample_id=`ep4_ja`、episode=4、language_key=`ja`、locale=`ja-JP`。
- 固定PDF：https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261127-ep4-ja-v1/tameshibu_episode4_ja.pdf
- filename=`tameshibu_episode4_ja.pdf`、8ページ、99,981,486バイト。
- SHA-256：`7c4bde39dc25800aa097854c6e47f31934abe12b3a05d97450fb73523af3c3dc`。
- アプリで取得した実ファイルをホストへ読み出し、容量・ハッシュを独立に再照合した。

## 実装とテスト

`SamplePdfManifest` は1〜1000項目・話数1〜10000を許容し、各項目の `new_download_url` を取得に使用する。トップレベルの旧 `fixed_tag` / `release_url` からURLを組み立てない。旧項目欠落時は一覧全体を拒否し、既存IDの保存名を保持する。新規項目は自身のfilenameを使用する。

ViewModelはALLまたはlanguage_key一致で絞り込む。第4話は日本語一覧で実表示を確認。日本語版のみのため、英語など他の言語フィルターで表示されないのは仕様。並び順はIDの話数を数値化し、第3話固定上限はない。

- `SamplePdfManifestTest`：7件成功。追加フィクスチャで52冊・第4話の別Release URL・容量・ハッシュ・旧51冊の保存名保持・保存済み52冊のオフライン再読込を確認。
- `SamplePdfManifestNetworkTest`：51冊固定を取り除き、旧同梱項目を保持することを確認する条件に変更。任意sampleIdを指定可能にした。この既存テスト自体は今回実行せず、実ネットワーク検証は下記UIテストで実施。
- `EpisodeFourNetworkUiTest`：最初の取得試験は99.5秒で成功。日本語一覧→第4話→ダウンロード→開くを実操作。最終の取得済み再利用・横向き試験は18.192秒で成功。
- opt-in引数：`episodeFourNetwork=true`。既存利用者データのある端末では実行せず、使い捨て検証環境に限定する。

PDFメタデータは `/PageLayout /TwoPageRight`、`/ViewerPreferences /Direction /R2L`。これとは別に実ビューアのdirection=R2L、effective_layout=SPREAD、左右2つのbitmapを確認し、画面画像でも右にPDF2ページ目・左にPDF3ページ目の表示を確認した。全ページの内容校正・全操作・TalkBack確認は本試験の範囲外。

## 失敗試行と制限

横向き追加テストの初回はテスト本体開始前の起動ANRで終了した。ログにはCPU・メモリー負荷、Playの自動更新、他アプリの検証が同じエミュレーターで実行された記録がある。第4話PDF処理が原因と断定せず、成功結果だけに置き換えない。一時環境のPlayプロセスだけを停止した後、再実行は成功した。

再撮影時には一覧の初期化前に日本語選択を探すテスト側の待機不足が発生。言語項目が現れるまで待機し、最終試行は成功した。最初の横向き画像は画面更新前の表紙だったため採用せず、画面更新完了後の最終画像を根拠とした。

元AVDは読み取り専用、スナップショット保存なし。実機データ・既存ソース差分・旧PDFは変更/削除せず、ストア提出・公開・配信設定変更は行っていない。検証エミュレーターで他アプリの試験が観測された。最終試験後の確認時にはエミュレーターは停止済みで、起動ログではスナップショット保存要求が無視されたことを確認。ADB一覧には既存Pixel 8aだけが残っていた。他作業の停止操作は行っていない。
