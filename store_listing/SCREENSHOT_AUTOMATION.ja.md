# ストア画像の自動撮影

## 実行

```sh
python3 scripts/capture_store_screenshots.py
```

指定の3つのAVD × 4言語 × 6画面を順番に撮影する。Play Consoleへのアップロードは行わない。

```sh
# まず1端末・1言語
python3 scripts/capture_store_screenshots.py --device Smartphone_6.3 --language ja

# 完了済み画像を検証してスキップし、未完了分を撮影
python3 scripts/capture_store_screenshots.py --resume

# 1画面だけ撮り直す（--resumeを付けない）
python3 scripts/capture_store_screenshots.py --device Tablet_7 --language ko --scene 04_search

# APKを既にビルドしている場合のみ
python3 scripts/capture_store_screenshots.py --skip-build --resume

# ホスト側の安全性・画像検査テスト（端末操作なし）
python3 -m unittest discover -s scripts -p 'test_capture_store_screenshots.py' -v
```

Python 3、Pillow、Android SDK、Gradle用JDKが必要。SDKはANDROID_SDK_ROOT、ANDROID_HOME、macOS標準の順で探索する。
元PDFが4言語とも揃っていることを、ビルドや端末操作より前に確認する。一部言語だけの実行でも4ファイルすべて必須。

## 保存物

- `store_listing/screenshots/Smartphone_6.3/`：Smartphone 6.3
- `store_listing/screenshots/Tablet_7/`：Tablet 7
- `store_listing/screenshots/Tablet_10/`：Tablet 10
- PNGと同名のJSON：言語、向き、AVD、シリアル、PDF・画像・APK等のハッシュ、ページ、検索語、設定、検査日時。
- `.artifacts/store-capture/`：ビルド・エミュレーター・撮影失敗のログ、一時画像。

macOSの大文字小文字を区別しないファイルシステムで既存のiPhone用 `smartphone` と衝突しないよう、保存先にはAVD名を使用する。既存画像は上書きしない。

画像名は `{lang}_{scene}_{portrait|landscape}.png`。構成は `SCREENSHOT_PLAN.ja.md` を参照。
失敗時は最大3回まで、言語・向き・転送確認と画面の再構成からやり直す。直らない場合は非ゼロ終了し、全体完了とは扱わない。

`--resume` は画像の存在だけではスキップしない。PDF、アプリAPK、テストAPK、撮影スクリプト、保存画像のハッシュが一致し、PNGの画像検査に合格した場合だけスキップする。
画像生成に関わるAPKやスクリプトを変更した場合は、古い画像を再撮影する。今回のADB転送権限の修正だけは画像生成が同一であるため、画像生成が同一で転送・再起動処理だけが異なる既知のスクリプトSHA-256を互換リストに登録し、同一APKで撮影した原本の再利用を許可した。元の撮影fingerprintは書き換えず保存している。任意の古いスクリプトを許可するものではない。

## 最終確認用レポート

```sh
python3 scripts/review_store_screenshots.py
```

全72枚の撮影条件・ハッシュ・重複を検査し、`store_listing/SCREENSHOT_CAPTURE_REPORT.ja.md` と `store_listing/screenshot_review/` の12枚の一覧画像を作る。欠落や古い撮影条件があれば失敗する。
一覧と検索画面を実際に目視確認した後だけ、次を実行して目視完了を記録する。

```sh
python3 scripts/review_store_screenshots.py --mark-reviewed
```

一覧画像は縮小した確認用JPEG。Play Consoleには各デバイスフォルダのPNG原本を使用する。

## 端末管理と言語

ADB操作はすべて対象シリアルを指定。すでに対象AVDが起動していれば再利用し、関係のない実機やAVDは終了しない。新たに起動したAVDは通常終了時に停止する。`--keep-device` で起動を維持できる。

開始・再開時と各再試行時に、`persist.sys.locale` と `system_locales` の先頭言語を確認する。中国語の `zh-Hans-CN` と `zh-CN` は同じ簡体字中国語として扱う。
異なる場合は、Google APIsイメージの `adb root` とテストAPK内の `StoreLocaleSetter` を使い、Androidの永続システム設定を更新して必ずエミュレーターを終了・起動し直す。Tablet 10のAndroid 15イメージでは `adb reboot` 後に共有ストレージへの新規書き込みが拒否されたため、OSだけの再起動から完全なAVD再起動へ変更した。プロパティだけを書き換えて完了扱いにはしない。
言語設定後は `adb unroot` で通常のshell UIDに戻してからPDFを転送する。root状態を撮影処理に持ち越さないための手順。共有ストレージの問題はroot解除だけでは解消せず、上述の完全なAVD再起動も必要だった。
撮影テストでもアプリが認識している言語・地域を確認する。

LocaleSetterはテストAPKだけに含まれ、製品アプリの権限や機能を追加しない。Android内部APIを使うため、別のOSイメージでは再検証が必要。root不可のPlay Storeイメージでは言語変更に失敗した時点で中断する。

向きは自動回転を無効にして指定。端末の自然な向きを考慮して回転値を決め、アプリのConfigurationと保存PNGの縦横も確認する。テスト中はUiDeviceでも物理ディスプレイの回転を固定し、端末画像が縦のままでアプリだけ横向きになる状態を除外する。ライトテーマで統一する。

## PDFを開く方法

元PDFと `/sdcard/Download/tameshibu_episode1_2_{lang}.pdf` のSHA-256を比較し、必要な場合だけ転送する。
AndroidのScoped Storageに合わせ、同一バイト列を `run-as` でアプリの `files/store_capture/{lang}.pdf` に配置し、ホストと撮影テストの双方でSHA-256を検証する。
撮影テストはこの確認済みPDFを開く。テストAPK内の別のサンプルで代用せず、製品アプリに全ストレージアクセス権限も付与しない。

## 撮影と検査

各画像を独立したテスト実行で作成する。アプリを再起動し、同じPDF・ページ・表示設定から画面を再構成する。
Composeテストで画面移動とスクロールを行い、ビットマップ更新・ページ・見開き両ページ・一時ヒント消滅を待つ。
UI Automatorで前面パッケージを確認し、OSバーを含む画面全体をPNGで保存する。

検索語は実際のPDF検索エンジンで試し、A5撮影用PDFの6ページ目の本文領域にヒットする候補を選ぶ。日本語の縦書きでは「ひと息マップ」がページ下部のタイトルだけに一致する場合があるため、「完成」、さらにセリフ内の「今」を候補とする。見出しだけの一致では採用しない。
検索画面ではビットマップと保存画像の赤いハイライトも検査する。実際の検索語と6ページ目の矩形はJSONに記録する。

次の自動検査に不合格なら完成画像に昇格させない。

- アプリエラー、PDF未描画、目的の設定項目が非表示。
- 対象アプリ以外が前面、画面OFF、言語・向きの不一致。
- PNGの破損、極端な黒・白一色、ほぼ均一な本文領域。
- 検索結果が6ページ目の本文にない、ハイライト未描画。

平均輝度だけで漫画ページを不合格にはしない。自動検査では細かな文字切れや美観を完全に保証できないため、最終的には全画像を目視で確認する。JSONの `validation` は自動検査合格と目視未確認を区別する。

## アプリ側の修正

撮影試験で確認した描画の問題に対処した。

- キャッシュキーに幅・画質・シャープネス・検索矩形を含め、検索後にハイライトなしのキャッシュを返さない。
- Composeがまだ参照しているビットマップを即座にrecycleしない。ページ切り替え・設定変更時の `Canvas: trying to use a recycled bitmap` を防ぎ、参照がなくなった画像はGCに任せる。低メモリ端末のキャッシュ無効化・解像度制限は維持する。

- 描画・検索ジョブのキャンセル完了を待ってからPDFを閉じ、処理中のネイティブ文書へのアクセス競合を防ぐ。終了処理はViewModelの破棄後も完了できる独立したIO処理で行う。

従来の `capture_screenshots.py` と `scripts/capture_screenshots.py` はそのまま残しているが、今回の撮影には使用しない。
通常の `connectedAndroidTest` では、撮影引数のないStoreScreenshotTestはスキップされる。
