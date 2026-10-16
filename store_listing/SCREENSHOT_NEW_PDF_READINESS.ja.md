# 追加PDFの撮影可否確認

最新状況: イタリア語の設定追加と先行検証4枚が完了。現在14言語・378枚が対象。詳細は [イタリア語先行検証結果](SCREENSHOT_ITALIAN_PREFLIGHT.ja.md) を参照。以下の13言語・イタリア語未登録の記述は追加前の確認履歴。

2026-10-16。PDF構造の照合と、Android上の検索・目次・表示の先行検証を完了。全画面撮影は未完了。

## 結果

未撮影6言語すべてに第1話と第2話のPDFが存在する。既存シーンと検索語に合う第1話を使用すれば、4端末・各言語27枚、合計162枚の追加撮影が可能な候補となる。AndroidエミュレーターのPDFエンジンによる先行検証は6言語すべて成功。

| 言語コード | 使用候補（testdata/） | 先頭目次名 | 6ページ目本文の検索一致 |
|---|---|---|---|
| es | tameshibu_episode1_es.pdf | Portada | descanso / flecha / mapa |
| pt | tameshibu_episode1_pt.pdf | Capa | seta / mapa（pausaは不一致） |
| vi | tameshibu_episode1_vi.pdf | Bìa | nghỉ / mũi tên / bản đồ |
| fil | tameshibu_episode1_tl.pdf | Pabalat | pahinga / palaso / mapa |
| ne | tameshibu_episode1_ne.pdf | आवरण | आराम / तीर / नक्सा |
| id | tameshibu_episode1_id.pdf | Sampul | istirahat / panah / peta |

各候補は8ページ、目次8項目、目次の遷移先は1〜8ページ。先頭目次名は撮影設定と完全一致。検索語はPyMuPDFで6ページ目の本文領域（top > 40、bottom < 560）に一致することを確認した。

## 撮影前に必要な作業

1. 撮影・検品スクリプトは現在 `testdata/tameshibu_episode1_2_{lang}.pdf` を固定参照する。追加PDFはこの名前ではないため、そのままでは事前確認に失敗する。両スクリプトで共有する言語別PDF参照を追加するか、期待する名前の撮影用コピーを用意する。元PDFは保持する。
2. フィリピン語はファイル名の `tl` と撮影言語コード `fil` を対応付ける。
3. ポルトガル語は最初の検索候補 `pausa` に一致しないが、後続の `seta` と `mapa` が一致するため現行フォールバックでも撮影候補になる。先頭候補を `descanso` に変更する場合はAPKの再ビルドが必要。
4. 6言語の検索・目次をスマホとChromebookで先行検証し、本文言語とハイライトを目視確認する。その後、全4端末162枚を撮影する。
5. 既存189枚と記録を保持し、追加対象のみを検品する。入力設定やスクリプトの変更でfingerprintが変わるため、既存画像を新しい入力で検品し直して上書きしない。

## 注意点

ネパール語PDFは一部見出しのテキスト抽出にNUL文字が含まれる。6ページ目の本文検索語と目次名は正常に抽出でき、同ページのレンダリングでも文字表示を確認した。Android PDFエンジン上での検索・表示は先行撮影で確認する。

第2話も各言語8ページ・目次8項目だが、6ページ目の本文が第1話と異なるため、現行の検索語を前提とする今回の候補には採用しない。

端末別枚数はSmartphone_6.3が42枚、Tablet_7が42枚、Tablet_10が42枚、Chromebook_FHDが36枚。追加後の枚数は13言語351枚になるが、既存108枚はversionCode 31、追加済み81枚は32のため、全体を同一版のセットとは扱わない。

## Android先行検証の結果

Smartphone_6.3とChromebook_FHDで各言語の検索・目次、計24枚をversionCode 32で撮影。自動検査と全24枚の目視確認に合格。先頭目次名、8項目の遷移先、目次からのページ移動、6ページ目本文への検索一致とハイライト、本文言語、端末ロケールを確認した。Chromebookは最大化状態も検証済み。

使用検索語はes=descanso、pt=seta、vi=nghỉ、fil=pahinga、ne=आराम、id=istirahat。ポルトガル語は既存の候補フォールバックが機能するため、撮影コードやAPKの変更は不要だった。

元PDFを保持して `tameshibu_episode1_2_{lang}.pdf` の撮影用コピーを6言語分作成した。filのコピー元は `tameshibu_episode1_tl.pdf`。コピー元と撮影用コピーは同じ内容。

注意点: 目次ダイアログの見出しは6言語とも「Table of contents」。インドネシア語の検索欄は「Search PDF」、決定ボタンは「OK」で英語が残る。Chromebookの検索画面では下部ページ操作バーの一部がシェルフ付近で切れている。PDF本文・目次の検証は成功したが、ストア掲載時の見栄えを確認する際はこの表示を考慮する。

ネパール語も本文と目次の表示・検索に成功。一部見出しのテキスト抽出制限は残るが、今回の撮影を妨げていない。

既存189枚とJSON計378ファイルは開始前のSHA-256と一致。APK2点、撮影スクリプト、設定は前回の入力ハッシュと一致。撮影用エミュレーターは終了済み。先行検証の画像とJSONは `.artifacts/store-capture/new-pdf-check/outputs/` にも保存。

6言語全体162枚のうち24枚が先行確認済み。残り138枚（全端末の他シーンとTablet_7・Tablet_10）は未撮影・未確認。全画面撮影の準備条件は満たした。Play Consoleへの登録は未実施。

| 端末 | 言語 | シーン | 原本 |
|---|---|---|---|
| Smartphone_6.3 | es | 04_search | [es_04_search_portrait.png](screenshots/Smartphone_6.3/es_04_search_portrait.png) |
| Smartphone_6.3 | es | 07_outline | [es_07_outline_portrait.png](screenshots/Smartphone_6.3/es_07_outline_portrait.png) |
| Chromebook_FHD | es | 04_search | [es_04_search_landscape.png](screenshots/Chromebook_FHD/es_04_search_landscape.png) |
| Chromebook_FHD | es | 07_outline | [es_07_outline_landscape.png](screenshots/Chromebook_FHD/es_07_outline_landscape.png) |
| Smartphone_6.3 | pt | 04_search | [pt_04_search_portrait.png](screenshots/Smartphone_6.3/pt_04_search_portrait.png) |
| Smartphone_6.3 | pt | 07_outline | [pt_07_outline_portrait.png](screenshots/Smartphone_6.3/pt_07_outline_portrait.png) |
| Chromebook_FHD | pt | 04_search | [pt_04_search_landscape.png](screenshots/Chromebook_FHD/pt_04_search_landscape.png) |
| Chromebook_FHD | pt | 07_outline | [pt_07_outline_landscape.png](screenshots/Chromebook_FHD/pt_07_outline_landscape.png) |
| Smartphone_6.3 | vi | 04_search | [vi_04_search_portrait.png](screenshots/Smartphone_6.3/vi_04_search_portrait.png) |
| Smartphone_6.3 | vi | 07_outline | [vi_07_outline_portrait.png](screenshots/Smartphone_6.3/vi_07_outline_portrait.png) |
| Chromebook_FHD | vi | 04_search | [vi_04_search_landscape.png](screenshots/Chromebook_FHD/vi_04_search_landscape.png) |
| Chromebook_FHD | vi | 07_outline | [vi_07_outline_landscape.png](screenshots/Chromebook_FHD/vi_07_outline_landscape.png) |
| Smartphone_6.3 | fil | 04_search | [fil_04_search_portrait.png](screenshots/Smartphone_6.3/fil_04_search_portrait.png) |
| Smartphone_6.3 | fil | 07_outline | [fil_07_outline_portrait.png](screenshots/Smartphone_6.3/fil_07_outline_portrait.png) |
| Chromebook_FHD | fil | 04_search | [fil_04_search_landscape.png](screenshots/Chromebook_FHD/fil_04_search_landscape.png) |
| Chromebook_FHD | fil | 07_outline | [fil_07_outline_landscape.png](screenshots/Chromebook_FHD/fil_07_outline_landscape.png) |
| Smartphone_6.3 | ne | 04_search | [ne_04_search_portrait.png](screenshots/Smartphone_6.3/ne_04_search_portrait.png) |
| Smartphone_6.3 | ne | 07_outline | [ne_07_outline_portrait.png](screenshots/Smartphone_6.3/ne_07_outline_portrait.png) |
| Chromebook_FHD | ne | 04_search | [ne_04_search_landscape.png](screenshots/Chromebook_FHD/ne_04_search_landscape.png) |
| Chromebook_FHD | ne | 07_outline | [ne_07_outline_landscape.png](screenshots/Chromebook_FHD/ne_07_outline_landscape.png) |
| Smartphone_6.3 | id | 04_search | [id_04_search_portrait.png](screenshots/Smartphone_6.3/id_04_search_portrait.png) |
| Smartphone_6.3 | id | 07_outline | [id_07_outline_portrait.png](screenshots/Smartphone_6.3/id_07_outline_portrait.png) |
| Chromebook_FHD | id | 04_search | [id_04_search_landscape.png](screenshots/Chromebook_FHD/id_04_search_landscape.png) |
| Chromebook_FHD | id | 07_outline | [id_07_outline_landscape.png](screenshots/Chromebook_FHD/id_07_outline_landscape.png) |

## 再確認: イタリア語PDFの追加

2026-10-16の再確認で、前回一覧にない `testdata/tameshibu_episode1_it.pdf` と `testdata/tameshibu_episode2_it.pdf` を確認した。両方とも8ページ・目次8項目で、先頭目次名は `Copertina`、遷移先は1〜8ページ。

第1話の6ページ目本文に `mappa` / `pause` / `frecce` が一致する。単数形の `pausa` / `freccia` は同ページに不一致。第1話6ページ目をレンダリングし、本文表示も確認した。Android上の検証は未実施。

イタリア語はユーザー指定の13言語に含まれておらず、現在の撮影設定、ロケール許可リスト、検索候補、先頭目次名の定義に未登録。アプリの `values-it` 翻訳リソースも存在しない。PDFの構造上は撮影候補にできるが、現在の撮影コマンドでは `--language it` を指定できない。今回、自動的な対象追加や既存設定の変更は行っていない。

イタリア語を対象に加える場合の準備作業は、`it-IT` と先頭目次名 `Copertina`、検索候補 `mappa` / `pause` / `frecce` の登録、撮影用PDF参照の準備、撮影用APKの再ビルド、スマホとChromebookでの先行検証となる。UIのイタリア語表示を求める場合は翻訳リソースも必要。4端末すべてを撮影する場合の追加対象は27枚。

既存6言語の撮影用PDFは先行検証JSONのPDFハッシュと一致し、検証済み24枚の画像ハッシュも一致。以前の189枚とJSON計378ファイルに変更・欠落はない。指定済み13言語については、前回と同じく残り138枚の撮影作業を進められる状態。
