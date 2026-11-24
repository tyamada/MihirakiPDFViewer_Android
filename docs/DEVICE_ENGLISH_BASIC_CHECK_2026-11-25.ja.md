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


## 追加確認：購入再照会・英語TalkBack（2026-11-25）

上記の「今回未実施」は最初の基本動作確認時点の記録。今回、以下の範囲を追加確認した。

| 項目 | 結果・確認根拠 |
|---|---|
| 既存データを保持した購入再照会 | 2.1.10（53）をforce-stop後に再起動し、Supportを再表示。BRONZE・SILVER・GOLDがすべてPurchasedとなることを画面のUI XMLで確認。公開版ソースでは購入一覧は空のメモリー状態から始まり、起動時およびSupport表示時にPlay BillingのqueryPurchasesAsyncを実行する。プロセス再起動後の再取得を確認したもので、データ消去・再インストール・購入取消・再購入は行っていない。 |
| 英語TalkBackの実発話 | 利用者が詳細の説明文と操作案内の双方が英語で聞こえたと報告。TalkBackのバインド・タッチ探索有効を端末情報でも確認。音声をこちらで直接聴取・録音した結果とは区別する。 |
| 実際に開いた詳細 | 利用者の回答ではBRONZEだったが、スクリーンショットで確認できた対象は「Ver.2.0 Release Commemoration Koyomi Badge」。実際の対象を優先し、BRONZEのTalkBack往復確認を合格扱いにしない。 |
| 回転後の閉じる・フォーカス復帰 | 縦向きのこよみ詳細を画面確認後に横向きへ回転。利用者がTalkBackでCloseを選択・ダブルタップして閉じ、こよみ詳細ボタンが読み上げられたと報告。画面を触らない状態のスクリーンショットで、詳細が消え、元のこよみ詳細ボタンに緑のフォーカス枠が戻っていることを確認。Backへの移動は今回の確認では観測しなかった。 |

### 終了時の設定復元

開始前・終了後とも、システム言語=en-US、言語一覧=en-US,ja-JP、enabled_accessibility_services=null、accessibility_enabled=0、accelerometer_rotation=1、user_rotation=0。TalkBackのBound servicesは空、タッチ探索は無効へ戻った。言語は元からEnglishのため変更せず、TalkBackと回転の一時変更を復元した。既存アプリデータ・アカウント・購入権利は消去していない。

### 残る確認範囲・保留

今回の購入確認は既存データを保持した再照会。新規状態からの購入復元は今回実施していない。BRONZE詳細のTalkBack開閉・回転後復帰、全画面のTalkBack網羅確認も未実施。こよみの無料付与と3商品のPlay購入権利は区別する。現地アカウントによる実インストール確認等、既存の保留事項は維持する。追加のコード修正が必要な不具合は、今回確認した範囲では見つからなかった。

追加根拠は開発者のローカル検証記録に保持：23-restored-support-top.xml、24-restored-purchases.xml、29-bronze-open.png（ファイル名に反して実画像はこよみ）、31-koyomi-close-focus.png、31-accessibility-state.txt、32-settings-restored.png。26/27のUIダンプ時には詳細が開いておらず、詳細表示・復帰成功の根拠には用いていない。


## 追加確認：BRONZE実対象のTalkBack・別環境の新規アプリ状態からの復元（2026-11-25）

前節で未実施とした2項目を追加確認した。前節の未実施記述は当時の履歴として保持し、現在の到達範囲は本節に示す。

### moto g24：英語TalkBackのBRONZE詳細

端末のインストール版2.1.10（53）、English優先・日本語第2言語を再確認。TalkBackを一時的に有効化し、縦向きのSupportで利用者がBRONZEの詳細画像を選択・ダブルタップした。35-bronze-confirmed-open.pngで見出しBRONZE、銅色画像、英語説明、Closeがある実対象を確認。利用者は英語の説明が聞こえたと報告した。

その詳細を横向きにし、利用者がTalkBackでCloseを選択・ダブルタップして閉じた。利用者の「BRONZEの詳細ボタンが読み上げられた」という報告と、38-bronze-returned-focus.pngの元のBRONZE詳細画像を囲む緑のフォーカス枠が一致した。Backへ移動せず、開いた元の項目へ復帰したことを確認した。今回の実対象はこよみと混同していない。全画面・全バッジ・全ジェスチャーを網羅した意味ではない。

終了時は言語en-US、言語一覧en-US,ja-JP、enabled_accessibility_services=null、accessibility_enabled=0、accelerometer_rotation=1、user_rotation=0へ戻し、開始時と一致した。moto g24のデータ消去・再インストール・購入取消・再購入は行っていない。

### 別環境：新規アプリ状態からの購入復元

使用前に作業用SSDのUUID 0A844419-D16F-4996-9EE2-82D492AB0771と空き約163.4GBを確認。停止中の既存Play復元AVDから、今回専用のViewer53-Restore-20261125を独立した複製として作成し、通常ファイル23件・2,446,383,342バイトのサイズ・SHA-256を照合した。元AVDは起動・変更していない。複製のAVD内部設定だけを複製先へ向けた。

複製側に限り、pm clearがSuccessとなることを確認してViewerの保存データを初期化した。Androidのbackup_auto_restoreは開始前から0で、そのまま維持した。Playのアカウント・キャッシュは保持。既存のPlayインストール版2.1.10（53）、installer=com.android.vendingを使用し、今回APKの置換や再インストールはしていない。

初回チュートリアルとこよみ無料付与通知を確認後、SupportでBRONZE・SILVER・GOLDすべてがPurchasedへ復元され、¥200・¥500・¥1,000を表示することをUI XMLとスクリーンショットで確認した。Viewerの新規保存状態からPlay購入権利を再取得する確認であり、motoの保存済みデータを参照した結果ではない。こよみの無料付与は有料3商品の購入復元とは別に扱う。

Play自体のキャッシュは消していないため、新規Googleアカウント、全ストア地域、ネットワーク経由の最新サーバーレシートの取得を証明したものではない。購入トークンや注文番号の生ログは取得・公開していない。

終了時は今回専用AVDを停止し、検証データを保持した。元AVDの23通常ファイルが開始時のサイズ・SHA-256と一致することを再確認した。既存の現地アカウント実インストール等の保留事項は維持する。今回確認した2項目で追加のアプリ修正が必要な問題は見つからなかった。

追加証拠：35-bronze-confirmed-open.png、36-bronze-landscape.png、38-bronze-returned-focus.png、37-fresh-launch.xml、39-fresh-home.xml、41-fresh-support.xml、42-fresh-purchased.xml/png、fresh-avd-copy-manifest.json、bronze-fresh-restore-result.jsonをローカルに保持。過去の「未実施」をこれらの追加結果で更新し、元の記録は履歴として残している。


## Pixel 8a：English基本操作の追加確認（2026-11-25）

今回接続された端末はPixel 8a、Android 17、実インストール版2.1.10（53）。開始時はシステム言語ja-JP、言語順序ja-JP,en-US、システム地域は日本、TalkBackはオフ。言語設定画面からEnglishを最優先にし、確認ダイアログを承認後、en-USおよびen-US,ja-JPへの切替を確認した。Playアカウントの国設定は変更していない。

英語のGetting Started、Skip、Open PDF、Sample PDFsの案内を確認し、SkipでViewerへ移動。保存済みの日本語PDFを描画し、スワイプで2/8から3/8へ移動した。中央タップでツールバーを表示し、Table of contents、Sample PDFs、Open PDF、Settings、Search PDF、ページ移動ラベルが英語であることを確認。文書タイトルの日本語はPDF由来。

SettingsのDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document informationの英語表示を確認。設定値は変更せず戻った。Helpの見出しを確認し、Getting Startedを展開できた。Versionで2.1.10、Build number 53、取得済みバッジのラベルを確認した。操作停止やクラッシュ画面は今回の操作中に観測しなかった。

終了後、システム言語を日本語へ戻す確認ダイアログを承認。ja-JP、ja-JP,en-US、システム地域日本、TalkBackオフが開始時と一致することを確認した。回転設定は変更していない。アプリのデータ消去・再インストールは行っていない。

今回Pixel 8aでは購入・購入復元・TalkBack・回転・PDF新規オープン・ネットワークダウンロードの個別操作は未実施。別端末での確認結果と混同しない。既存差分と現地アカウント実インストール等の保留事項は維持する。

証拠はローカルのpixel8a-english-20261125記録に保持：05-english-launch.xml、06-home.xml、07-page.xml、08-toolbar.xml、10-settings.xml、11-help.xml、12-help-expanded.xml、13-version.xml、17-restored-language.xml。03-viewer.xmlは言語変更確認の承認前で日本語のため、英語表示成功の根拠には使っていない。
