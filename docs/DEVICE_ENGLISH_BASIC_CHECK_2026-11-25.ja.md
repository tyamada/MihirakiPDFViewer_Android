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


## Galaxy A54 5G SC-53D：English基本操作の追加確認（2026-11-25）

今回接続されたGalaxy A54 5G SC-53DはAndroid 16、実インストール版2.1.10（53）。開始時はロック画面のため利用者に解除を依頼し、解除の回答後に操作した。システム言語・言語一覧はいずれもja-JPのみ、TalkBackはオフだった。

Samsungの言語設定で提示されたEnglish (Japan)を一時追加して標準言語に設定し、en-JP、en-JP,ja-JPへの切替を確認した。Playアカウントの国設定は変更していない。

| 項目 | 結果・範囲 |
|---|---|
| チュートリアル | Getting Started、Open PDF、Sample PDFs、Skip等を英語表示。Skipで次画面へ進めた。 |
| サンプル | Englishフィルター、最新話順、英語タイトルとDownloadを確認。第3話（22.3 MiB）をダウンロードし、完了後のOpen/Deleteを確認。Openで英語PDFを表示した。 |
| PDF・ページ移動 | 英語第3話を描画し、Next pageボタンで1/8から2/8へ移動。先頭ページでの逆方向スワイプはページが変わらなかったため、スワイプ移動の合格根拠にはしない。 |
| ツールバー | 中央タップで表示。Table of contents、Sample PDFs、Open PDF、Settings、Search PDF、Previous/Next page、Page navigationの英語ラベルを確認。 |
| Settings | Display settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document informationを英語表示。値は変更していない。 |
| Help | 英語セクション見出しとGetting Startedの展開を確認。 |
| Version | 2.1.10、Build number 53、取得済みバッジの英語ラベルを確認。 |

操作中にクラッシュ画面や操作停止は観測しなかった。購入・購入復元・TalkBack実発話/フォーカス・回転・目次/検索/スライダーの個別操作は今回未実施。別端末の検証結果をこの端末の結果とは扱わない。

終了時は今回だけ追加したEnglish (Japan)を言語一覧から削除し、システム言語・言語一覧がともにja-JPのみへ戻ったことを確認した。TalkBackは開始時同様オフ（accessibility_enabled=0）。回転設定は変更していない。既存アプリデータは消去せず、今回ダウンロードした英語サンプルは保存したまま保持した。再インストール・購入・ストア提出・公開は行っていない。既存差分と現地アカウント実インストール等の保留事項を維持する。

ローカル証拠：galaxy-a54-english-20261125の06-launch.xml、07-home.xml、09-toolbar.xml（実内容はサンプル一覧）、11-sample-download.xml、12-open-sample.xml、14-toolbar.xml、15-next-page.xml、17-settings.xml、18-help.xml、19-help-expanded.xml、20-version.xml、25-language-restored.xml。07/08時点はPDF未表示であり、描画・ページ移動の成功根拠には使っていない。


## Galaxy Tab A SM-T510：English基本操作の追加確認（2026-11-25）

対象はSamsung Galaxy Tab A SM-T510、Android 9。実機から取得したViewerは2.1.10（53）、minSdk=26。開始時はシステム言語・言語一覧がともにja-JPのみ、TalkBackはオフ。English (United States)を一時追加し、en-US、en-US,ja-JPになったことを確認して検証した。

| 項目 | 結果・範囲 |
|---|---|
| チュートリアル・ホーム | 英語Getting Started、Open PDF、Sample PDFs、Skipを確認。ホームの英語ボタンとこよみ無料付与通知を表示し、通知を閉じた。 |
| サンプル | Englishフィルター、最新話順、英語3話分の案内を確認。第3話22.3 MiBをダウンロードし、Open/Delete表示になった。 |
| PDF初回オープン | Open後の記録09-pdf.xmlはViewerではなく設定アプリの言語追加画面。直後のpidofでViewerプロセス不在を確認した。Viewerのクラッシュと断定できるスタックは得られず、初回の終了挙動は原因未確定として要再確認。無条件合格にしない。 |
| PDF再試行 | 再起動して同じサンプルを開くと英語第3話表紙を描画し、Viewerプロセスの存続を確認。右向きスワイプで1/8から2/8へ移動できた。 |
| ツールバー | 再試行で中央タップにより表示。Search PDF、Table of contents、Sample PDFs、Open PDF、Settings、Previous/Next page、Page navigationの英語ラベルを確認。 |
| Settings | Display settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、文書情報、Mouse Navigation、Single page in portrait、Cover setting、Resetの英語表示を確認。値は変更していない。 |
| Help | 英語見出しとGetting Startedの展開を確認。 |
| Version | 2.1.10、Build number 53、取得済みバッジの英語ラベルを確認。 |

今回の総合判定はFollow-up needed。再試行は成功したが、初回PDFオープン時の終了の原因切り分けは残す。確認したcrashバッファにはAndroid Studio画面共有エージェントの別プロセス障害があったが、Viewer障害の根拠として流用していない。メモリー不足やR8等を原因と断定しない。以前の低RAM端末のキャッシュ無効化に関する注記も今回再検証済みとは扱わない。

終了後、一時追加したEnglishだけを削除し、システム言語・言語一覧がともにja-JPのみで開始時と一致することを確認。TalkBackは0のまま、回転設定は変更していない。既存アプリデータは消去せず、今回ダウンロードしたサンプルは保持。購入・購入復元・TalkBack・回転・新規ローカルPDF選択の操作確認は今回未実施。現地アカウント実インストール等の既存保留事項を維持する。コード修正・再インストール・ストア提出・公開はしていない。

ローカル証拠：tab-a-english-20261125の04-launch.xml、07-samples.xml、08-download.xml、09-pdf.xml（初回終了後の別画面）、11-relaunch.xml、15-open-retry-settled.png、16-page.xml、17-toolbar.xml、19-settings.xml、20-help.xml、21-help-expanded.xml、22-version.xml、27-language-restored.xml。終了挙動を再現可能なアプリクラッシュとして確定した記録ではない。


## Galaxy Tab A：初回PDFオープン時の終了原因と再検証（2026-11-25）

前節の原因未確定事項を追加調査し、直接原因を特定した。今回の終了はPDF描画の例外ではなく、検証中のパッケージ更新に伴うOSの強制停止だった。前節は調査前の履歴として保持し、この終了事象についての判定をRetest passedへ更新する。

### 根拠

- 端末の2026-11-25 09:52:37.720 JSTのActivityManager記録：`Force stopping com.github.tyamada.MihirakiPDFViewer_Android appid=10231 user=-1: installPackageLI`。
- 同09:52:37.723に、前面Viewerプロセス2301を`stop ...,installPackageLI`の理由で終了した記録。
- 同09:52:37.747に、そのプロセス終了に伴いActivityを除去した記録。
- PackageManagerの`lastUpdateTime=2026-11-25 09:52:37`が停止時刻と一致。インストーラーは`com.android.vending`。更新後も版番号は2.1.10（53）。番号から更新の有無を推測せず、パッケージ置換記録と更新時刻で照合した。

これにより、初回に設定画面へ戻りViewerプロセスが不在だった理由を説明できる。Playの自動更新設定や、更新を開始した具体的な操作までは特定していない。別プロセスのAndroid Studio画面共有エージェントのクラッシュ、メモリー不足、R8をこの終了の原因とは扱わない。

### 更新完了後の再検証

開始時の日本語のみ・TalkBackオフ・自動回転1・user_rotation未設定を記録し、Englishへ一時切替。既存データを保持したforce-stopによるコールド起動を使用した。これは検証のための明示的停止であり、問題の再発とは区別する。

| 経路 | 結果 |
|---|---|
| 第3話のコールド起動・復帰 | 英語チュートリアルのSkip後に保存ページ2/8を描画。 |
| 別PDFの初回オープン | 未取得だった英語第2話23.9 MiBを追加ダウンロードし、初めて開いて表紙1/8を描画。スクリーンショットとプロセス11250の存続を確認。 |
| 第2話ページ移動 | スワイプで1/8から2/8へ移動。 |
| 第2話のコールド起動・復帰 | 再度データ非消去で再起動し、保存ページ2/8を描画。プロセス13266の存続を確認。 |
| 追加更新の有無 | 再検証後もlastUpdateTimeは09:52:37、版2.1.10（53）のまま。追加更新と重ならず、同じ終了挙動は再発しなかった。 |

この終了事象に対するアプリコード修正・新しいビルドは不要と判断した。今後の実機検証では開始時・異常発生時・終了時の版番号とlastUpdateTimeを記録し、installPackageLIがあれば更新完了後に再起動して再検証する。ログを消去して再現の根拠を失わない。将来別のクラッシュが発生した場合にまで問題なしとする判断ではない。

### 復元・保持事項

終了後、一時追加したEnglishを削除。システム言語ja-JP、言語一覧ja-JPのみ、accessibility_enabled=0、accelerometer_rotation=1、user_rotation=nullが開始時と一致した。回転設定は今回変更していない。アプリデータ・既存PDF・購入権利は消去せず、追加取得した第2話も保持。調査中にアプリを再インストール・更新する操作は行っていない。ストアへの提出・公開は行わず、既存の他の差分・現地アカウント確認等の保留事項は維持する。

今回の証拠はローカルtab-a-open-investigation-20261125記録に保持：events-filtered.txt、system-filtered.txt、06-cold-ready.xml、10-first-open-episode2.xml/png、11-episode2-page.xml、13-second-cold-pdf.xml、18-settings-restored.xml、cause-and-retest-result.json。抽出ログはアプリのライフサイクル記録に限定し、アカウント識別情報等を公開資料に含めない。


## aiwa tab AG10 JA3-TBA1003：English基本操作の追加確認（2026-11-25）

実機はaiwa tab AG10（JA3-TBA1003）、Android 13、インストール版2.1.10（53）。開始時のシステム言語・言語一覧はja-JPのみ、TalkBackはオフ。通常のEnglish (United States)を追加して最優先へ並べ替え、en-US、en-US,ja-JPへ切り替えた。言語候補にあった疑似言語は選んでいない。Playアカウントの国設定は変更していない。

横向きで英語Getting Started、Open PDF、Sample PDFs、Skipを確認。Skipでホームへ移動し、こよみ無料付与通知を閉じた。サンプル一覧はEnglishフィルター・最新話順・英語3話分を表示。第3話22.3 MiBをダウンロードし、OpenでPDF1/8を描画した。スワイプで2/8へ進み、中央タップで英語ツールバーを表示。Search PDF、Table of contents、Sample PDFs、Open PDF、Settings、ページ移動のラベルを確認した。

SettingsのDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document informationを英語で確認し、設定値は変更していない。Helpの英語セクションとGetting Startedの展開を確認。Versionでは2.1.10、Build number 53、取得済みバッジのラベルを確認した。今回の操作中にクラッシュ画面・アプリ終了・操作停止は観測しなかった。

終了後は一時追加したEnglishのみを削除し、システム言語・言語一覧がともにja-JPのみへ戻ったことを確認。TalkBackは0のまま。画面の向きは開始時の横向きのままで、回転設定は変更していない。最終更新時刻は開始前・終了後とも2026-11-25 10:34:54で、検証中の版変更はなかった。

既存データは消去せず、今回取得したサンプルは保持した。今回この端末での購入・購入復元・TalkBack実発話/フォーカス・回転遷移・検索/目次/スライダーの個別操作は未実施。ほかの端末での結果と混同しない。既存差分と現地アカウント実インストール等の保留事項は維持し、再インストール・ストア提出・公開は行っていない。

ローカル証拠：aiwa-english-20261125の05-language-order.xml、06-launch.xml、09-samples.xml、11-download-complete.xml、12-pdf.xml、13-page.xml、14-toolbar.xml、16-settings.xml、17-help.xml、18-help-expanded.xml、19-version.xml、25-language-restored.xml。


## Xperia 10 VI XQ-ES44：English基本操作の追加確認（2026-11-25）

実機Sony Xperia 10 VI（XQ-ES44）、Android 16、インストール版2.1.10（53）を確認。開始時はシステム言語ja-JP、言語一覧ja-JP,en-US、TalkBackオフ。既存のEnglish (United States)を最優先へ移動し、確認ダイアログを承認してen-US、en-US,ja-JPへの変更を確認した。

英語Getting StartedとSkipを確認し、ホームの無料付与通知を閉じた。Englishフィルター・最新話順のサンプル一覧で英語第3話22.3 MiBを取得し、OpenでPDF1/8を描画。スワイプで2/8へ移動し、中央タップで英語ツールバーを表示した。SettingsのDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document information等を英語表示し、値は変更していない。Helpの英語セクションとGetting Startedの展開、Versionの2.1.10・Build number 53・取得済みバッジラベルを確認。今回の操作中にアプリ終了・クラッシュ画面・操作停止は観測しなかった。

終了時、日本語を最優先へ戻す確認を承認。ja-JP、ja-JP,en-US、accessibility_enabled=0が開始時と一致した。回転設定は変更していない。最終更新時刻は開始前後とも2026-11-22 04:48:00で、検証中の更新はなかった。既存データを消去せず、追加取得したサンプルは保持。

今回この端末での購入・購入復元・TalkBack実発話/フォーカス・回転・検索/目次/スライダー等の個別操作は未実施。現地アカウント実インストール等の保留事項は維持する。再インストール・ストア提出・公開は行っていない。

モデルごとに最新結果だけを残す一覧ルールに従い、VERIFICATION_LISTの旧XQ-ES44行を今回の行に置換した。以前の詳細記録は保持し、限定的な最新検証で未検証項目を合格扱いにしない。

ローカル証拠：xperia10vi-english-20261125の02-launch.xml、05-samples.xml、07-download-complete.xml、08-pdf.xml、09-page.xml、10-toolbar.xml、12-settings.xml、13-help.xml、14-help-expanded.xml、15-version.xml、18-language-restored.xml。


## OPPO Reno11 A CPH2603：English基本操作の追加確認（2026-11-25）

実機OPPO Reno11 A（CPH2603）は今回取得したOS情報ではAndroid 16、Viewer 2.1.10（53）。旧一覧のAndroid 15は過去結果であり、今回の実取得値に更新した。開始時はシステム言語・言語一覧ともja-JPのみ、地域日本、TalkBackオフ。言語編集でEnglishを一時追加して標準言語にし、en-JP、en-JP,ja-JPへの切替を確認した。地域は日本のまま、Playアカウントの国設定も変更していない。

英語Getting StartedとSkipを確認。ホームでこよみ無料付与通知を閉じ、English・最新話順のサンプル一覧を表示した。英語第3話22.3 MiBをダウンロードし、Openで1/8を描画。スワイプで2/8へ進み、中央タップで英語ツールバーを表示した。SettingsのDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document informationを英語で表示し、値は変更していない。Helpの英語セクションとGetting Startedの展開、Versionの2.1.10・Build number 53・取得済みバッジラベルを確認した。今回の操作中にアプリ終了・クラッシュ画面・操作停止は観測しなかった。

終了時は一時追加したEnglishのみを削除し、日本語への変更を承認。システム言語・言語一覧ともja-JPのみ、地域日本、accessibility_enabled=0が開始時と一致した。回転設定は変更していない。最終更新時刻は開始前後とも2026-11-25 11:06:31で、検証中の版変更はなかった。既存データを消去せず、今回ダウンロードしたサンプルは保持した。

今回この端末の購入・購入復元・TalkBack実発話/フォーカス・回転・検索/目次/スライダーの個別操作は未実施。既存保留事項を維持し、再インストール・ストア提出・公開は行っていない。一覧は英語でモデルごとに最新1行とするルールに従い、既存CPH2603行を置換した。

ローカル証拠：oppo-english-20261125の04-launch.xml、07-samples.xml、08-download.xml、09-pdf.xml、10-page.xml、11-toolbar.xml、13-settings.xml、14-help.xml、15-help-expanded.xml、16-version.xml、20-restored-edit.xml、21-settings-restored.xml。


## 一覧簡潔化前の結果表（2026-11-25時点の履歴）

VERIFICATION_LISTを結果表と使用上の注意だけに整理する前の説明を、履歴としてこちらへ移した。最新の到達範囲・保留事項は各端末の本文と合わせて参照する。以下は最新一覧を更新するための別の一覧ではなく、整理時点のスナップショット。

| Category   | Manufacturer | Device Name           | OS Version | App Version |  Result   | Notes |
|:-----------|:-------------|:----------------------|:----------:|:-----------:|:---------:|:------|
| Smartphone | FCNT         | arrows We2 M07        |     16     |  2.1.3(42)  | No issues | -     |
| Smartphone | Google | Pixel 8a | 17 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, existing PDF rendering/page swipe, toolbar, Settings, Help expansion and Version/build display. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored system language order to Japanese then English, and region to Japan; TalkBack remained off. Purchases, restoration, rotation and downloads were not tested on this device in this session. |
| Smartphone | motorola | moto g24 | 14 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, PDF open/render/page swipe, toolbar, portrait/landscape, Settings, Help, sample list, Version and existing purchased badges. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Follow-up: after a data-preserving process restart, BRONZE/SILVER/GOLD returned as Purchased; English TalkBack speech was user-confirmed and Koyomi detail focus returned to its source after rotation/Close (screen-confirmed). Further follow-up: confirmed actual BRONZE detail opened with English TalkBack; after rotation/Close, focus returned to BRONZE (speech and screenshot). In a separate cloned Play emulator, all three paid tiers restored after clearing only Viewer app data with automatic backup restore disabled; Play account/cache retained. Language, TalkBack and rotation settings match the initial state. |
| Smartphone | OPPO | Reno11 A CPH2603 | 16 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-JP): tutorial, English sample download/open, PDF rendering, swipe 1/8 to 2/8, toolbar, Settings, Help expansion and Version. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored Japanese-only language list (ja-JP) and confirmed region Japan; TalkBack stayed off and package update time was unchanged. Purchases, restoration, TalkBack and rotation were not tested in this session. |
| Smartphone | Samsung | Galaxy A54 5G SC-53D | 16 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (Japan), en-JP: tutorial, English sample list/download/open, PDF rendering, next-page button, toolbar, Settings, Help expansion and Version/build display. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original Japanese-only language list (ja-JP); TalkBack remained off. Purchases, restoration, TalkBack and rotation were not tested on this device in this session. |
| Smartphone | Sharp        | AQUOS sense4 SH-41A   |     12     |  2.1.3(42)  | No issues | -     |
| Smartphone | Sony | Xperia 10 VI XQ-ES44 | 16 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, English sample download/open, PDF rendering, swipe 1/8 to 2/8, toolbar, Settings, Help expansion and Version. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original language order Japanese then English; TalkBack stayed off and package update time was unchanged. Purchases, restoration, TalkBack and rotation were not tested in this session. |
| Smartphone | Sony         | Xperia 10VIII XQ-GH44 |     17     |  2.1.3(42)  | No issues | -     |
| Tablet | Samsung | Galaxy Tab A SM-T510 | 9 | 2.1.10(53) | Retest passed | 2026-11-25, system English (en-US): basic checks and PDF retests passed. Initial interruption was OS package replacement: installPackageLI killed Viewer at 09:52:37 JST, matching lastUpdateTime. After update, Episode 3 cold resume, new Episode 2 first open/page swipe and Episode 2 cold resume passed. No app-code fix required for this event. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original Japanese-only settings; other holds unchanged. |
| Tablet | aiwa | tab AG10 (JA3-TBA1003) | 13 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US), landscape: tutorial, English sample download/open, PDF render, swipe 1/8 to 2/8, toolbar, Settings, Help expansion and Version. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original Japanese-only language list (ja-JP); TalkBack stayed off and package update time was unchanged. Purchases, restoration, TalkBack and rotation transitions were not tested in this session. |
| Desktop    | ASUS         | Chromebook CM30       |     13     |  2.1.3(42)  | No issues | Window mode and tablet (full-screen) mode ready. |


## FCNT arrows We2 M07：English基本操作の追加確認（2026-11-25）

実機arrows We2 M07、Android 16、Viewer 2.1.10（53）を確認した。開始時はシステム言語・言語一覧ともja-JPのみ、TalkBackオフ。通常のEnglish (United States)を一時追加して最優先へ移動。システムの言語変更確認に続き端末固有の再起動確認があり、承認後の再起動と利用者のロック解除を経てen-US、en-US,ja-JPを確認した。疑似言語は使用していない。

英語Getting Started、Skip、ホームを確認。Englishフィルター・最新話順のサンプル一覧から英語第3話22.3 MiBをダウンロードし、Openで1/8を描画。スワイプで2/8へ進み、中央タップで英語ツールバーを表示した。SettingsのDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、Document informationを英語で確認し、設定値は変更していない。Helpの英語見出しとGetting Startedの展開、Versionの2.1.10・Build number 53・バッジラベルを確認した。今回の操作中にアプリ終了・クラッシュ画面・操作停止は観測しなかった。

終了時は今回追加したEnglishのみを選択・削除し、日本語への変更確認と再起動確認を承認。再接続後、システム言語ja-JP、言語一覧ja-JPのみ、accessibility_enabled=0が開始時と一致した。Viewerの最終更新時刻は開始前後とも2026-11-25 11:21:42、版2.1.10（53）を保持。回転設定は変更していない。データ消去・再インストールは行わず、追加取得したサンプルは保持した。

今回この端末の購入・復元・TalkBack実発話/フォーカス・回転・検索/目次/スライダー等の個別操作は未実施。既存保留事項を維持し、ストア提出・公開は行っていない。一覧は英語の結果表と使用上のNotesのみ、モデルごとに最新1行とするルールに従い、M07の古い行を今回の結果へ置換した。詳細は本メモに保存する。

ローカル証拠：arrows-english-20261125の08-english-launch.xml、10-samples.xml、12-download-complete.xml、13-pdf.xml、14-page.xml、15-toolbar.xml、17-settings.xml、18-help.xml、19-help-expanded.xml、20-version.xml、25-remove-confirm.xml、27-reboot-confirm.xml。05-launch.xmlは言語変更後の再起動承認前で日本語のため、英語表示の成功根拠には含めない。


## Sharp AQUOS sense4 SH-41A：English基本操作の追加確認（2026-11-25）

実機AQUOS sense4 SH-41A、Android 12、インストール版2.1.10（53）。開始時はシステム言語・言語一覧ともja-JPのみ、TalkBackオフ。通常のEnglish (United States)を一時追加して最優先へ移動し、en-US、en-US,ja-JPを確認した。疑似言語は選んでいない。

英語Getting StartedとSkipを確認し、保存済みPDF「ためし部 第1話 ひと息マップ」を描画。日本語タイトルは文書由来。スワイプで1/8から2/8へ進み、中央タップで英語ツールバーを表示した。Table of contents、Sample PDFs、Open PDF、Settings、Search PDF、ページ移動ラベルを確認。SettingsのDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、文書情報を英語表示し、値は変更していない。

無料付与通知を閉じ、Helpの英語見出しとGetting Started展開後の説明文を確認。Versionで2.1.10・Build number 53・取得済みバッジラベルを確認した。今回の操作中にアプリ終了・クラッシュ画面・操作停止は観測しなかった。チュートリアル全ページや大文字サイズの全配置を網羅確認したものではない。

終了後は今回追加したEnglishのみを削除し、システム言語・言語一覧がともにja-JPのみへ戻ったことを確認。accessibility_enabled=0を保持。回転設定は変更していない。Viewerの最終更新時刻は開始前後とも2026-11-25 11:58:32で、版番号も維持した。データ消去・再インストールは行っていない。

購入・購入復元・TalkBack・回転・サンプルの新規ダウンロード・新規PDFのファイル選択・検索/目次/スライダーの個別操作は今回未実施。既存保留事項を維持し、ストア提出・公開は行っていない。VERIFICATION_LISTではSH-41Aの既存行を英語の簡潔な最新結果に置き換え、詳細は本メモに保存した。

ローカル証拠：aquos-english-20261125の05-launch.xml、06-home.xml、07-page.xml、08-toolbar.xml、10-settings.xml、11-help.xml、12-help-expanded.xml、13-version.xml、19-language-restored.xml。


## Sony Xperia XQ-GH44：Wi-Fi接続でのEnglish基本操作確認（2026-11-25）

Wi-Fiデバッグで接続済みのSony XQ-GH44、Android 17、実インストール版2.1.10（53）を確認した。ワイヤレスデバッグのペア設定や接続設定は変更していない。開始時はシステム言語・言語一覧ともja-JPのみ、地域日本、TalkBackオフ。English (Japan)を一時追加して優先へ移動し、言語変更を承認。en-JP、en-JP,ja-JP、地域日本を確認した。Playアカウントの国設定も変更していない。

英語Getting StartedとSkipを確認後、保存済みPDF「ためし部 第2話 机、ひろがる。」を表示。スワイプで1/8から2/8へ進み、中央タップで英語ツールバーを表示した。Sample PDFs、Open PDF、Settings、Search PDF、ページ移動ラベルを確認。文書タイトルの日本語はPDF由来。SettingsではDisplay settings、High quality、Sharpness、Two-page view、Show cover、Reading direction、文書情報を英語で確認し、設定値は変更していない。

無料付与通知を閉じ、Helpの英語セクションとGetting Startedの展開・説明表示を確認した。Versionで2.1.10、Build number 53、取得済みバッジラベルを確認。今回の操作中にアプリ終了・クラッシュ画面・操作停止は観測しなかった。

終了時は今回追加したEnglish (Japan)を削除し、日本語への変更を承認。システム言語・言語一覧がともにja-JPのみ、地域日本、accessibility_enabled=0へ戻り、開始時と一致した。Wi-Fiデバッグ接続は継続し、回転設定は変更していない。アプリの最終更新時刻は開始前後とも2026-11-25 12:29:04、版2.1.10（53）のまま。既存データを消去せず、再インストールも行っていない。

今回の購入・購入復元・TalkBack実発話/フォーカス・回転・サンプル新規ダウンロード・新規PDF選択・検索/目次/スライダーの個別操作は未実施。現地アカウント実インストール等の保留事項は維持。ストア提出・公開は行っていない。VERIFICATION_LISTのXQ-GH44の既存行を、英語の簡潔な最新結果に置き換えた。

ローカル証拠：xq-gh44-english-20261125の01-languages.xml、06-launch.xml、07-home.xml、08-page.xml、09-toolbar.xml、11-settings.xml、12-help.xml、13-help-expanded.xml、14-version.xml、18-language-restored.xml。公開資料にWi-Fi接続先IP・ペア情報・端末識別子は記載していない。


## ASUS Chromebook CM30：Englishでの利用者手動確認（2026-11-25）

### 確認環境と根拠

利用者の申告：ChromeOS 158.0.8127.65（公式ビルド・64ビット）、Viewer 2.1.10（53）、検証中の表示言語English (United States)。元は日本語のみで、今回の作業のため利用者がEnglishへ変更した。Android実行環境のOS版は今回未取得のため、旧一覧の13を新規確認値として流用せず、一覧には取得できたChromeOS版を記載した。

ADBはMacからもCM30内Linuxからも接続できず、adb devicesの一覧は空、arcの名前解決は可能だが接続拒否。ChromeOS側ADBはオン、Android側デバッグはグレー表示との申告。ChromeOS通常再起動後も変わらなかった。原因は未確定であり、ADB接続成功や遠隔自動確認の結果としては扱わない。データ保持のためADB設定のオン/オフ・Powerwash・ChromeOSのデベロッパーモード切替は案内していない。利用者はLinuxにadbを追加したが、Androidアプリの再インストール・データ消去はしていない。

### 利用者が実施・報告した基本確認

- PDFの表示とページを進める・戻す操作。
- 英語ツールバー、Settings、Help、Getting Startedの展開。
- ウィンドウ表示、最大化、全画面表示。

利用者は上記すべてについて問題なしと回答した。こちらが画面を直接観測・スクリーンショット収集したものではなく、本人操作の報告に基づく結果として記録した。購入・購入復元・TalkBack・PDF新規ダウンロード・タッチ/キーボード/マウスの各入力方式の網羅は今回確認していない。

### 元の言語設定への復元：利用者確認で完了

ChromeOSのSettingsでLanguages（見つからない場合はlanguage検索）を開き、Device languageのChangeから日本語を選び、Confirm and restartで反映する手順を案内。再ログイン後、表示言語が日本語へ戻ったことを確認し、優先言語/ウェブコンテンツ言語に今回追加したEnglishが残っている場合は、その追加分のみを削除して日本語のみへ戻す。キーボード配列や入力方式は変更対象にしない。

手順案内後、利用者から「日本語のみに戻した」と完了報告を受けた。元の日本語のみへの復元は利用者確認で完了した。こちらからの画面観測・ADBでの設定値取得はできていないため、直接測定した復元結果とは区別する。ADB設定は元からオンのため維持し、データを消す操作はしない。既存データ・差分・現地アカウント実インストール等の保留事項は維持。ストア提出・公開は行っていない。

VERIFICATION_LISTは既存CM30行を簡潔な英語結果へ置換し、表外Notesで利用者の手動確認である旨を明記した。根拠は本チャットでの利用者報告と接続試行結果。ローカル接続先IPや個人アカウントは公開資料に記載しない。


## Pixel 8a：公開版53の第4話フォローアップ（2026-11-28）

システム言語は日本語のまま、Play配布2.1.10（53）で第4話の一覧表示・ダウンロード・容量/ハッシュ照合・表紙と本文表示を確認した。今回の確認は英語基本動作の再実施ではない。詳細・検証範囲と設定維持は[第4話の検証記録](EPISODE4_MANIFEST_VERIFICATION_2026-11-28.ja.md)参照。旧第1〜3話と取得した第4話を保持し、最後に元の第1話3ページ目へ戻した。既存の購入復元・現地アカウント等の保留は維持。


### Pixel 8a：第4話の横向き追加確認（2026-11-28）

公開版2.1.10（53）で、横向きR2L見開き（右2/左3、右4/左5、右6/左7）、最終8ページ目、方向キーによる進む・戻るを確認。縦向き復帰を確認し、自動回転1・user_rotation0・日本語・TalkBackオフを開始時と同じ状態へ戻した。最後に第1話3ページ目へ復帰し、既存PDF・データを保持した。これは日本語環境での追加確認で、英語基本操作やTalkBack・購入復元の再実施ではない。[第4話の詳細記録](EPISODE4_MANIFEST_VERIFICATION_2026-11-28.ja.md)参照。


### Pixel 8a：第4話のタッチスワイプ追加確認（2026-11-28）

公開版2.1.10（53）で、本人の指による縦向き2→3→2、横向き見開き2 / 8→4 / 8→2 / 8の往復をユーザー回答で確認。画面転送のドラッグ試行と本人操作の結果を区別して[第4話の詳細記録](EPISODE4_MANIFEST_VERIFICATION_2026-11-28.ja.md)へ追記した。日本語環境での確認で、英語基本動作の再実施ではない。終了時に自動回転1・user_rotation0・ja-JP・TalkBackオフを復元確認し、既存データ・保留事項を保持した。
