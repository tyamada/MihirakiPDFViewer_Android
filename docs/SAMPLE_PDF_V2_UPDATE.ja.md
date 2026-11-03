# Viewer sample-manga-v2 更新・検証記録

この文書は実装検証時点の記録です。後続の2.1.5（45）の版番号・審査準備・価格確認・実機手順は [最新のリリース準備記録](SAMPLE_PDF_V2_RELEASE_45.ja.md) を参照してください。

更新日: 2026-11-03（JST）。対象は `MihirakiPDFViewer_Android` のみ。remote `https://github.com/tyamada/MihirakiPDFViewer_Android.git`、開始時HEAD `2e9e113c4ead908857d417225b95bad4c88f5958` を確認した。リポジトリ内の作業指示ファイルはなく、READMEのビルド・テスト案内と今回の依頼範囲に従った。

## 根拠と更新内容

[Viewerの公開済みsample-manga-v2](https://github.com/tyamada/MihirakiPDFViewer_Android/releases/tag/sample-manga-v2) の資料4件を取得し、原本を `docs/sample-releases/sample-manga-v2/` に保存した。CSVのBOM・改行も保持するため `.gitattributes` に属性を追加した。

| 資料 | 原本SHA-256 |
|---|---|
| `manifest.json` | `95d8d29fb31cca624358820628f4b7214ab6848d3ef26816a76791130be2d7de` |
| `android-handoff.csv` | `3367be06235d99cd9b10582ddb08ce67d3182be09ff57f9b376d08c51f7814e4` |
| `SHA256SUMS` | `70567ed0e47d631e9b44d154c9beba29df425f92dd9b56020b2e5904efe528b0` |
| `public-verification.json` | `afd015fa199fc0012f9bb33aa5baa8c899d586f25acb2ecf13b5ce46fd67a524` |

- カタログを51冊（14言語・17言語地域区分×3話）へ更新。既存42IDと旧ファイルの対応をCSVと照合し、改訂33・同一内容継続9・地域版追加9を確認した。作品名、ファイル名、バイト数、表示サイズ、ページ数、SHA-256、固定URLをマニフェストと一致させた。`latest/download` は使わない。
- `fil` の第1・2話はViewer用 `_tl.pdf`（`tameshibu_episode1_3_tl.pdf`、`tameshibu_episode2_3_tl.pdf`）、第3話も `_tl.pdf` とした。
- `es_419`、`pt_pt`、`zh_hk` を追加。既存のスペイン、ブラジル、台湾と区別し、原語＋英語名、英語名順の並び、端末地域からの初期選択、各3話への絞り込みを統一。香港版は標準書面語であり、口語広東語版ではない。
- 取得ファイルは `verified/<期待SHA-256>/<ファイル名>` へ保存。サイズ・PDFヘッダー・ハッシュを確認した一時ファイルだけを公開する。同名の旧版もハッシュが合わなければ新版として扱わない。表示前にも再検証する。通信はHTTPSのみ、200応答・リダイレクト上限・期待サイズ上限を確認する。
- 旧保存先は完全一致時だけ読み取り再利用する。明示削除・設定リセットも現在版の管理キャッシュだけを対象にし、旧PDFや無関係なファイルを再帰削除しない。削除済みの項目が旧コピーから自動復活しないよう記録する。
- キャッシュ検証をバックグラウンドで行い、ダウンロード進捗ごとに全PDFをハッシュし直すことを避けた。Viewer固有のソート設定の保存を維持した。
- README英日版と公開HTML13ページの案内を51冊へ更新し、計765件のPDFリンクを検査した。作者「こまいろ日和 / Komairo Hiyori」、CC BY 4.0、第三者素材の個別条件を維持した。
- 新しい検証エラー文言をViewerの全14言語リソースに追加した。PDFの内容・期待ハッシュの恣意的な変更は行っていない。

## 実際の検証結果

| 検証 | 結果 |
|---|---|
| 四資料とKotlin一覧・ID・URL・サイズ・ハッシュの整合性 | 51件合格。ID・ファイル名・URLの重複なし |
| 匿名HTTPS取得、最終HTTP 200、PDF実体・サイズ・SHA-256 | Viewerの固定URL全51件で合格。計1,254,726,790バイト |
| PDF構成・ページ順 | 全51冊が8ページ。目次の遷移先が1〜8の順であることを確認 |
| 単体テスト | 23件、失敗0。うちキャッシュ9件、地域初期選択・名称順2件 |
| Debug APK / 端末テストAPK / 署名付きRelease AAB | ビルド成功 |
| Debug / Release Lint | 各エラー0、警告10。抑制は追加していない |
| Android17 / API37、sdk_gphone16k_arm64（16KBページ） | 端末テスト4件合格、最終画面再確認1件合格 |
| 地域フィルター・すべて | 17区分それぞれ3話を確認。「すべて」で51件すべてへスクロールして到達 |
| PDFエンジン描画 | 全51冊・408ページを順に描画し、空白でないことを確認。全表紙画像を保存 |
| 検索・本文抽出 | 全51冊の表紙語でページ0の検索座標を確認。抽出した表紙本文をクリップボードに書き込み、読み戻し一致 |
| 新地域版の実画面 | `es_419` / `pt_pt` / `zh_hk` の検索入力・検索ハイライト、単ページ、見開き、次の見開きへの移動を確認。香港版は右3ページ・左4ページの並びも画像で確認 |
| 実ネットワークとキャッシュ | 香港版第3話で新規取得→検証済みオフライン再利用→同サイズ破損の拒否→オフライン失敗→再取得成功を確認。テスト用の旧コピーを保持 |
| 旧版・再取得・中断の単体試験 | 同名旧版の誤認防止、同一内容旧版のオフライン再利用、別期待ハッシュの版保持、ハッシュ違い・切断・HTML応答の拒否、再試行、削除・中断時の旧／無関係ファイル保持を確認 |
| AAB構造・署名 | bundletool validate成功。JarFileで内容300エントリの署名を検証 |

Lint警告10件は今回変更していない箇所にある、Gradle・依存関係の更新案内6件、既存画面のConfiguration API利用1件、依存JAR内のTrustAllX509TrustManager検出3件。依存関係の更新や関連コードの影響確認は別作業であり、警告を抑制していない。コンパイラーには既存の非推奨API案内も残る。

JDKのjarsignerは署名検証に成功する一方、自己署名証明書・信頼チェーン・タイムスタンプ、およびAABのZIP内Manifest順序に起因するJarInputStreamとの読み取り差について警告した。bundletoolの構造検証とJarFileによる各内容エントリの署名検証は成功した。Play側の受け入れ検証は今回行っていない。

端末テストの初回実行では初回案内画面と、ADBが外部領域に作成した検証用フォルダーの読み取り権限で失敗した。案内を閉じるテスト処理と、Viewer自身の権限による内部テストフォルダーへの配置で修正し、上表の最終実行は全件合格した。検索解除とハイライト描画の完了を待って証跡を撮影するようテストも整えた。

## 再検証方法

通常の確認:

```sh
python3 scripts/verify_sample_release_assets.py
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest bundleRelease lintRelease
```

独立した配布PDFの確認は `python3 scripts/verify_sample_release_assets.py --remote --download-dir <リポジトリ外の検証フォルダー> --output <検証結果JSON>` を使う。PDFを `docs/sample/` や `testdata/` に追加しない。

エミュレーターには検証済み51PDFをViewerの権限で `files/v2-qa` に配置する。Android17ではADBから作った外部ストレージのディレクトリがアプリから読めない場合があるため、デバッグアプリの `run-as` で内部領域へ配置する。既存アプリのアンインストールやデータ消去は不要。

端末テストは `SamplePdfSmokeTest#addedLanguagesAppearAndFilterToTheirEpisodes`、`SamplePdfV2LayoutTest`、`SamplePdfV2NetworkTest`、`SamplePdfV2DeviceTest` を対象とする。`sampleV2Network=true` と `samplePdfFixtureDir=/data/user/0/com.github.tyamada.MihirakiPDFViewer_Android/files/v2-qa` をinstrumentation引数として渡す。ライブ通信試験は明示指定時だけ実行し、一意な名前の合成キャッシュだけを後片付けする。旧 `sampleSmoke=true` の試験は別の既存フィクスチャ用であり、今回のv2全件試験とは区別する。

## 未検証事項・配信までの残作業

- 実機、旧Android、他の画面サイズ・向き・端末フォント設定での確認は未実施。エミュレーター検証はAndroid17の1環境。
- 全51冊はPDFエンジンで確認し、実画面での検索・見開き確認は新地域3版の第1話。すべての組み合わせの手操作・全本文の校正・全検索語の網羅検証ではない。
- ViewerにはPDF本文の選択・コピーUIがないため、その操作は未検証。上記のコピー試験は本文抽出とOSクリップボードの読み戻しであり、新しいコピー機能を追加したものではない。
- 旧版キャッシュの試験は合成データによる単体試験と、実ネットワーク試験用の旧コピー保持で実施。利用者の実データを操作するアップグレード試験は行っていない。
- 今回の範囲で制作側へ報告すべきPDF検索不具合は検出していない。将来問題が見つかった場合は、ファイル名・検索語・ページ・再現手順を添えて報告し、公開PDFや期待値を独断で変更しない。
- バージョンは2.1.4（44）を維持。AABは今回のローカル検証用であり、既存配信済みコードのままアップロードしない。次の配信時は版番号更新、リリースノート、署名付き再ビルド、実機更新確認、Play内部テスト／審査等を行う。
- 今回はcommit・push、GitHub Pagesの公開、Play Console操作、国・価格・配信設定変更、審査送信、アプリ公開を行っていない。UtilityのHEADと追跡差分も開始時と一致する。
- 旧PagesのPDF、旧Releases、既存URLは維持。`docs/sample/`・`testdata/` に変更なし。配布用PDFをGit管理へ追加していない。

ローカル検証用AAB SHA-256: `58397eda04642be331fcfaa03096b4286b864277918408fb45c84229adaf2175`

エミュレーターのViewer専用テストPDFは再検証用に保持している。接続された実機とUtilityアプリのデータは変更していない。
