> **翻訳・Lintの状態を更新しました。** [v5検証結果](LOCALIZATION_ALIGNMENT_REVIEW_v5.ja.md)を参照してください。以下は修正前の記録です。

# Viewer ローカル修正・確認結果 v4

v2/v3報告の完了判断を訂正する。現在も翻訳不足があり、全対応完了ではない。本書とLOCALIZATION_AUDIT_v4.jsonを現在の確認結果とする。

## 今回の変更

- 基本言語の利用者向け133項目の不適切なtranslatable=false指定を解除。ページ移動・購入・案内・ヘルプ・プライバシー等であり、機械IDではない。各キーの分類はJSONに記録。
- 翻訳側にも残っていた非翻訳指定を解除し、既存訳が矛盾した指定を持つ状態を是正。件数はJSONに記録。
- 独・仏・西・伊の主要操作・購入・案内を追加。同一の英語原文を持つUtilityの既存訳も照合して再利用。全体で183項目を補った。母語話者校正は未実施であり、既存訳の再利用を品質認定とはしない。
- 英語ストアの短い説明を94文字から76文字へ短縮。scripts/validate_store_metadata.pyで実原稿の3フィールドを再検査可能にした。
- INTERNATIONAL_DISTRIBUTION.mdから未検証価格レンジの推奨と税務の一律説明を除去。法令の適用・適合は未評価とし、言語カバレッジの断定を修正。
- v2の相対リンクを修正し、v2/v3から本書への訂正案内を追加。

## Android名称と残る英語フォールバック

| リソース構成 | 実際のapp_name | 文字数 | 不足キー数 |
|---|---|---:|---:|
| values-de | Mihiraki: Doppelseitiger PDF-Reader | 35 | 142 |
| values-es | Mihiraki: Lector PDF de Doble Página | 36 | 141 |
| values-fil | MihirakiPDFViewer | 17 | 30 |
| values-fr | Mihiraki : Lecteur PDF Double Page | 34 | 143 |
| values-id | MihirakiPDFViewer | 17 | 30 |
| values-it | Mihiraki: Lettore PDF a Doppia Pagina | 37 | 142 |
| values-ja | 見開きPDFビューア | 10 | 0 |
| values-ko | 미히라키 PDF 뷰어 | 11 | 0 |
| values-ne | MihirakiPDFViewer | 17 | 30 |
| values-pt | MihirakiPDFViewer | 17 | 30 |
| values-vi | MihirakiPDFViewer | 17 | 30 |
| values-zh-rCN | MihirakiPDFViewer | 17 | 27 |
| values-zh-rTW | MihirakiPDFViewer: 雙頁 PDF 閱讀器 | 29 | 40 |

不足キー数は基本言語に存在して当該構成に定義のない項目数。実際の端末では言語選択規則に従いフォールバックする。全キーはJSONに列挙。英語と同文のキーも別に記録し、固有名詞等の妥当性を含め内容の再確認が必要。Androidの表示名とストアタイトルは用途が異なるため、差があること自体を不具合としない。

## 実ストア原稿の測定

タイトル／短い説明／詳細説明の順。見出しを除き、詳細説明の改行は含める。全10原稿が30／80／4000文字の範囲内。

| 原稿 | 実タイトル | 文字数 |
|---|---|---|
| STORE_LISTING.de.md | MihirakiPDFViewer: Doppelseite | 30 / 71 / 2491 |
| STORE_LISTING.en.md | Mihiraki: Two-Page PDF Reader | 29 / 76 / 2837 |
| STORE_LISTING.es-ES.md | MihirakiPDFViewer: Doble | 24 / 70 / 561 |
| STORE_LISTING.fr.md | MihirakiPDFViewer: Double page | 30 / 58 / 2647 |
| STORE_LISTING.it.md | MihirakiPDFViewer - Spread | 26 / 65 / 3117 |
| STORE_LISTING.ja.md | 見開きPDFビューア - 見開き・右綴じ | 20 / 51 / 1481 |
| STORE_LISTING.ko.md | 미히라키 PDF 뷰어 | 11 / 62 / 1428 |
| STORE_LISTING.pt-BR.md | MihirakiPDFViewer: Duplas | 25 / 67 / 546 |
| STORE_LISTING.zh-CN.md | MihirakiPDFViewer - 双页阅读 | 24 / 46 / 858 |
| STORE_LISTING.zh-TW.md | MihirakiPDFViewer - 雙頁閱讀 | 24 / 37 / 863 |

## 今回の検証

- 全14構成のstrings.xmlをXMLとして解析。構文を確認。
- 実原稿の文字数検査：成功。翻訳の自然さ・機能説明の完全性は保証しない。
- :app:assembleDebug --offline：今回成功（最初のリソース変更後）。その後に翻訳側の非翻訳属性解除を実施したため、最終版ビルド結果は下記追記を参照。
- :app:lintDebug --offline：今回失敗。MissingTranslationが150件。これは言語ごとの不足キー総数とは異なるLintの集計。Untranslatable警告は解消。警告抑制・ベースライン追加は行っていない。
- 単体テスト、端末画面確認、母語話者校正：今回未実施。ビルド成功をこれらの成功とは扱わない。
- Lint詳細：app/build/reports/lint-results-debug.xml。既存の未使用リソースや他の警告も残る。

## 商品・外部状態

商品IDと課金処理は変更していない。既存Console登録状態・公開状態・現行価格・税の適用は未確認。今回の外部反映は実施なし。姉妹アプリ間の商品権利の共有やID変更は行っていない。

## 未完了

- JSONに列挙した不足翻訳、英語同文キーの妥当性確認、追加訳の校正。
- Lintの未翻訳エラー解消と表示確認。翻訳不足を再び非翻訳指定で隠さない。
- 各言語Web・アプリ・ストアの役割説明、用語、画像の完全な照合。
- 国別PRの根拠URL・調査日・検証指標・取得可能性。旧報告の市場需要は未検証仮説、国内端末試験は互換性確認と扱う。
- 公開前のConsole読取・価格比較・実機確認。公開・価格変更は今回の範囲外。

## 最終版のビルド追記

翻訳側の非翻訳属性解除後に :app:assembleDebug --offline を再実行し、成功を確認。Lintは上記の未翻訳エラーにより未通過のまま。
