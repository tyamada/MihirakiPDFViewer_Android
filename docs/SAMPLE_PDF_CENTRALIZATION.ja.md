# サンプル漫画PDFの導線と検証用入力（2026-11-20）

公開ダウンロード案内は [manga-prompt-kit](https://tyamada.github.io/manga-prompt-kit/samples/) に集約する。中央ページは ja / en / ko / zh-Hans / zh-Hant / de / fr / es / pt / vi / fil / ne / id / it の14言語。Viewer側の既存13言語のindex・supportと日英README・SAMPLE_PDFは、それぞれ対応する中央ページへ案内する。繁体字・スペイン語・ポルトガル語は該当地域のアンカーも指定する。

アプリのSamplePdfCatalog、一覧JSON、固定Release URL、ID・ファイル名・容量・SHA-256は変更しない。2.1.10（53）の既存成果物、ストア設定、審査確認予約も変更しない。

## 検証用PDF

漫画PDFはリポジトリー内へコピーしない。リポジトリー外の作業キャッシュを明示し、固定manifestと容量・SHA-256を照合する。`latest`やViewerの旧testdataへのフォールバックは行わない。既存ファイルの不一致は上書きせず失敗する。

```sh
# 必要な2件だけ準備する例。外付けHDDの実体・UUID・空き容量を事前確認する。
python3 scripts/prepare_sample_pdfs.py --input-dir /path/outside/repository/viewer-samples --id ep1_ja --id ep1_en --download
export VIEWER_SAMPLE_PDF_DIR=/path/outside/repository/viewer-samples
python3 scripts/capture_store_screenshots.py --language ja en
python3 scripts/review_store_screenshots.py --language ja en
```

取得なしの再照合は同じprepareコマンドから`--download`を外す。全51件が必要な実PDF統合検証だけは`--all --download`を明示する。選択した言語・エピソード分だけを用意し、通常CIは全件取得しない。旧capture_screenshots.pyは新ランナーへの入口であり、旧add_episode3_samples.pyのdocs/sample再コピー処理は廃止した。

端末へのPDF配備とインストールを伴う統合検証は、通常の単体試験と区別する。NavigationTestの漫画撮影はep1_ja/ep1_en、SamplePdfV2LayoutTestは地域版3件、SamplePdfV2DeviceTestは全51件を対象とする。アプリの読取可能な検証専用ディレクトリーへ必要分を配備し、instrumentation引数`samplePdfFixtureDir`を渡す。VerifiedSampleInputsは端末側でも容量・SHA-256を照合し、欠落・不一致を失敗にする。入力なしのスキップを合格として数えない。

`run_sample_pdf_smoke.py --device <既存AVDプロファイル> --sample-pdf-dir <外部キャッシュ>`は、es/it/pt/vi/fil/ne/idの各3話、計21件を事前照合し、明示したAVDへ配備して3件の統合試験を実行する。事前に通常の`assembleDebug assembleDebugAndroidTest`でAPKを作成する。これは実機・エミュレーター操作を伴うため、今回の資料整理の確認では実行していない。

PDF処理の単体試験は小さな生成fixtureを用い、公開ページとキャッシュ解決の試験も小さなテキストfixtureで行う。一般の検索・パスワード・レイアウトPDFは今回の削除対象ではない。

## 重複コピーの整理と互換性

分離した作業ブランチのtestdataにあった追跡対象の漫画49件（1,193,912,050バイト）を削除した。削除前に各ファイルの容量・SHA-256を再計算し、公開済み旧Release（sample-manga-v1またはv2）の同一資産のdigest・容量、匿名HTTPSアクセス200と一致することを確認した。復元URL・個別照合値は [整理台帳](sample-releases/testdata-retirement-20261120.json) に記録する。同名でも世代違いがあるため、復元時はファイル名だけでなく台帳のSHA-256を照合する。過去ReleaseとGit履歴は保持する。

**docs/sampleの42件（1,021,951,329バイト）は保持する。** 公開済み旧アプリがPagesの固定URLを参照するため、表示用リンクを外しただけでは削除できない。廃止には旧クライアント利用状況と代替配信の別途確認が必要。

元の開発フォルダーは変更せず、そこにある追跡対象91件・未追跡のテスト素材18件（440,659,379バイト）、他の作業コピーと成果物も保持した。用途不明・異なるSHA-256の素材、利用者PDF、SEASIDE_LIVING、一般PDF、一覧JSONは削除していない。作業ツリーのコピー整理であり、Git履歴や他コピーを含むMac全体の容量削減値ではない。

過去の移行記録にあるsample_public_catalog.py --writeは当時の履歴であり、現行手順では使用しない。現在は中央リンクの検証のみを行う。
