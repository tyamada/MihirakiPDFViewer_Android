# サンプルPDFの配布先移行

2026年11月12日の移行作業。対象は `tyamada/MihirakiPDFViewer_Android`。

## 配布先と根拠資料

現在の取得先は [manga-pdf-20261103-v1](https://github.com/tyamada/manga-prompt-kit/releases/tag/manga-pdf-20261103-v1)。各PDFはこのタグの固定URLから取得する。

根拠資料の原本を `docs/sample-releases/manga-pdf-20261103-v1/` に保存した。`pdf-manifest.json`、`pdf-migration.csv`、`SHA256SUMS`、`LICENSE.md` はRelease添付、`pdf-public-verification.json` は制作資料リポジトリの公開検証記録である。manifest・移行CSV・ライセンスのSHA-256は添付チェックサムと一致した。

## 既存データとの互換性

全51冊について、旧Viewer `sample-manga-v2` とID・ファイル名・タイトル・言語キー・locale・話数・ページ数・サイズ・SHA-256が一致した。今回変更するのは取得先URLである。17言語地域区分それぞれに第1〜3話の3冊がある。

フィリピン語は `fil` と `ep1_fil` / `ep2_fil` / `ep3_fil` を維持する。PDF名は `tameshibu_episode1_3_tl.pdf`、`tameshibu_episode2_3_tl.pdf`、`tameshibu_episode3_2_tl.pdf`。移行CSVのViewer行51件はすべて旧ファイル名と新ファイル名が一致する。Utility側の第1・2話は旧 `_fil.pdf` から `_tl.pdf`、第3話は旧新とも `_tl.pdf` とCSVで確認した。この対応をViewerのID変更に適用しない。

キャッシュは `verified/<SHA-256>/<fileName>` と旧同名ファイルを参照するため、URL変更でキーが変わらない。同一ハッシュの取得済みPDFは検証後にそのまま再利用する。カタログ以外のダウンロード実装、読書履歴、表示設定、ユーザーPDFの保存処理は変更していない。作者「こまいろ日和 / Komairo Hiyori」、CC BY 4.0、第三者素材の個別ライセンス案内を維持する。

ハッシュが異なるさらに古いPDFは従来どおり保持し、現行版を別のハッシュ付きパスへ取得する。旧配布先の資料は履歴と互換性の比較用に保存する。旧Releaseと旧Pages上のPDFの廃止は、利用アプリ全体の移行完了後に別途判断する。

## 検証

`python3 scripts/verify_sample_release_assets.py` で新旧manifest、Viewerの移行CSV51行、チェックサム、Kotlinカタログ、公開案内15ページを照合する。51冊のID・ファイル名・URLの一意性、17言語地域×3話、サイズ・ハッシュ・ページ数を検査する。

`python3 scripts/verify_sample_release_assets.py --remote --download-dir <リポジトリ外フォルダー> --output <結果JSON>` で匿名HTTPS取得を再検証する。新固定URL全51件で最終HTTP 200、PDFヘッダー、サイズ、SHA-256、8ページ構成を確認済み。合計1,254,726,790バイト。

`python3 -m unittest discover -s scripts -p 'test_sample_public_catalog.py'` で8件合格。公開案内の欠落・重複・並び順・作者表示・サイズ・誤URLの拒否と再生成の冪等性を確認した。

Android単体テスト25件は失敗0。URL変更後の検証済みキャッシュと旧同名ファイルの通信なし再利用を追加試験した。Debug APK、AndroidTest APK、署名付きRelease AABのビルドが成功した。Debug/Release Lintはいずれもエラー0、警告11件。警告は依存関係の更新案内7件、既存画面のConfiguration API利用1件、依存JAR内のTrustAllX509TrustManager検出3件で、今回のURL変更による抑制や設定変更は加えていない。

AABはJarFileで全300内容エントリの署名を検証し、11月10日の公開用ビルドと同一の証明書SHA-256であることを確認した。jarsignerには自己署名・信頼チェーン・タイムスタンプ・ZIP内Manifest順序に関する警告が残る。

専用エミュレーターはAndroid 17 / API 37、arm64、4KBページ、1080×2424の新規AVD。端末テスト5件が214.356秒で成功した。補強した見開き撮影テストの再確認1件も14.703秒で成功した。

| 確認内容 | 最終結果 |
| --- | --- |
| 全PDFの描画 | 51冊・408ページを順に描画し、全ページが空白でないことを確認 |
| 検索とコピー | 全51冊の表紙語でページ0の検索座標を確認。抽出本文のOSクリップボード書き込み・読み戻し一致 |
| 地域版の実画面 | 中南米スペイン語・ポルトガル版・香港版の第1話で検索、単ページ、見開き、次の見開きへの移動を確認 |
| 見開きの画像証跡 | Composeの実画面が3/8へ更新されたことを待って撮影。香港版などで右3ページ・左4ページを画像でも確認 |
| 新URLからの取得 | 香港版第3話をアプリの実HTTPSダウンロード処理で新規取得し、サイズとSHA-256を検証 |
| オフライン | 上記の検証済みPDFを通信なしで再利用。未取得・破損状態の通信失敗を正しく返す |
| 破損と再取得 | 同サイズの1バイト改変を拒否し、元ファイルを保持して再取得に成功 |
| 旧PDF保全 | 合成旧コピーを保持。同一ハッシュの旧形式・検証済み形式はURL変更後も再取得せず同じファイルを返す |
| 一覧と設定 | 全17区分それぞれの3話と「すべて」51冊、順序変更の保存を確認 |

最初の画面テストは内部状態の検査を通過したが、UiAutomationの保存画像には切り替え前の表紙が残った。ページ番号が3/8となるComposeツリーの反映を待ち、Compose自身の画像取得を使うようテストを補強して再実行し、最終画像を目視確認した。アプリ本体への追加変更は不要だった。

本文の選択・コピーUIはViewerに実装されていないため、コピー確認はPDF本文抽出とOSクリップボードの往復である。実機、旧Android、他の画面サイズ、利用者の実データを使った上書き更新は未検証。既存取得済みファイルはテスト用の合成キャッシュで検証し、利用者データを作り替えて試験していない。

検証用AAB SHA-256は `b01f55f5ab9d0cfea8d24046d25dfd26c15dcf9723d8135ca85c876ef5b0f082`。ビルドには開始時点の未コミット変更も含まれるため、次の配信用成果物は採用範囲を確認した上で作り直す。

## 配信状態との区別

2026年11月12日にPlay Consoleを読み取りで再確認し、製品版は2.1.6（46）、配布先は日本・米国・韓国・台湾・香港・マカオ・カナダの7地域である。最終公開日は11月10日、管理対象の公開はオンであり、今回の移行変更は審査へ送信していない。今回のローカル変更はその公開済みビルドに含まれない。

今回の版番号は既存作業の2.1.6（46）を維持した検証用ビルドとする。更新配信の前に未コミット変更全体の採否確認、次の版番号設定、署名付き再ビルド、実機での更新確認とPlay内部テストを行う。ストア申請・本番配信・配信地域や価格の変更は今回の範囲に含まない。

## 検証環境の取り違えと対処

最初に選んだADB接続番号5580が、既存の読み取り専用Pixel_10エミュレーターと重なった。識別名の照合が遅れ、その一時実行環境のViewerへ今回のDebug APK・端末テストAPKを適用し、検証PDFを配置して端末テストを開始した。気づいた時点でViewerのテストを停止し、今回配置したPDF51コピーと付随するtarメタデータだけを照合して除去した。既存の一時実行環境のViewer検証APKは更新済みのままである。Pixel_10の元ディスクは読み取り専用起動により保持されている。Utilityアプリへは変更を加えていない。

最終試験は接続番号5586の新規AVD `Pdf_Migration_QA` で完了した。起動後にAVD名を照合し、インストール前のinstrumentation一覧が空であることを記録した。実機は接続されていなかった。

## 変更ファイル

今回の移行で更新・追加した30ファイルは以下のとおり。既存の別作業の差分とは区別している。

- `README.md`
- `README_ja.md`
- `SAMPLE_PDF.md`
- `SAMPLE_PDF_ja.md`
- `app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/data/SamplePdfCatalog.kt`
- `app/src/test/java/com/github/tyamada/mihirakipdfviewer_android/SamplePdfCatalogTest.kt`
- `app/src/test/java/com/github/tyamada/mihirakipdfviewer_android/SamplePdfCacheTest.kt`
- `scripts/sample_public_catalog.py`
- `scripts/test_sample_public_catalog.py`
- `scripts/verify_sample_release_assets.py`
- `docs/SAMPLE_PDF_DISTRIBUTION_MIGRATION.ja.md`
- `docs/index.html`
- `docs/index_de.html`
- `docs/index_es.html`
- `docs/index_fil.html`
- `docs/index_fr.html`
- `docs/index_id.html`
- `docs/index_ja.html`
- `docs/index_ko.html`
- `docs/index_ne.html`
- `docs/index_pt.html`
- `docs/index_vi.html`
- `docs/index_zh-CN.html`
- `docs/index_zh-TW.html`
- `docs/sample-releases/manga-pdf-20261103-v1/LICENSE.md`
- `docs/sample-releases/manga-pdf-20261103-v1/SHA256SUMS`
- `docs/sample-releases/manga-pdf-20261103-v1/pdf-manifest.json`
- `docs/sample-releases/manga-pdf-20261103-v1/pdf-migration.csv`
- `docs/sample-releases/manga-pdf-20261103-v1/pdf-public-verification.json`
- `app/src/androidTest/java/com/github/tyamada/mihirakipdfviewer_android/SamplePdfV2LayoutTest.kt`

検証コマンドは `./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest bundleRelease lintRelease`。作業開始時の無関係な追跡差分29件とステージ差分の一致を確認した。Gitへのcommit・pushは行っていない。
