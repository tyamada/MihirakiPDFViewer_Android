> **英語残留・空文字と検証結果を更新しました。** [v6報告書](LOCALIZATION_ALIGNMENT_REVIEW_v6.ja.md)が現在の記録です。以下は前回時点の結果です。

# Viewer 不足翻訳補完・検証結果 v5

## 結果

不足していた785項目を追加し、全13翻訳構成で基本言語の208キーを定義した。XML構文、重複キー、不足キー、書式引数の番号・型を検査し、不足・不一致は0件。非翻訳指定の再追加やLint抑制、ベースライン作成は行っていない。

独・仏・西・伊では操作、設定、ヘルプ、診断、ログ、プライバシー、更新案内、チュートリアルを補完。他の一部言語では更新案内・チュートリアル、台湾繁体字では設定ヘルプも追加した。追加キーはLOCALIZATION_COMPLETION_v5.jsonに記録。

| 翻訳構成 | キー数 | 不足 | 書式引数不一致 |
|---|---:|---:|---:|
| values-de | 208 | 0 | 0 |
| values-es | 208 | 0 | 0 |
| values-fil | 208 | 0 | 0 |
| values-fr | 208 | 0 | 0 |
| values-id | 208 | 0 | 0 |
| values-it | 208 | 0 | 0 |
| values-ja | 208 | 0 | 0 |
| values-ko | 208 | 0 | 0 |
| values-ne | 208 | 0 | 0 |
| values-pt | 208 | 0 | 0 |
| values-vi | 208 | 0 | 0 |
| values-zh-rCN | 208 | 0 | 0 |
| values-zh-rTW | 208 | 0 | 0 |

## 今回実行した検証

- scripts/validate_localized_strings.py：XML、重複キー、不足キー、書式引数の整合を検査。
- :app:assembleDebug :app:lintDebug --offline：終了コード0、BUILD SUCCESSFUL。Android Studio同梱JDKを使用。
- Lint：エラー0件、警告73件。MissingTranslationおよびUntranslatableの指摘は0件。
- scripts/validate_store_metadata.py：全10原稿のタイトル・短い説明・詳細説明が上限内。
- git diff --check：元資料にあった行末空白を除去後に確認。

### 残るLint警告

- AndroidGradlePluginVersion: 1件
- DataExtractionRules: 1件
- IconDuplicates: 5件
- IconLauncherShape: 10件
- IconLocation: 4件
- LocaleFolder: 1件
- PluralsCandidate: 1件
- TrustAllX509TrustManager: 3件
- TypographyEllipsis: 16件
- UnusedResources: 25件
- UseKtx: 6件

Lintレポートはapp/build/reports/lint-results-debug.xmlおよび同.html。未使用リソース、表記、アイコン、既存コード等に関する警告は残る。警告を消すための無関係な実装修正は行っていない。

## 確認の限界と残作業

今回の翻訳は不足キーの補完。母語話者による校正、全言語・全端末での目視確認は未実施。技術用語・固有名詞など原文と同文が妥当なものもあり、キー充足を自然な翻訳の認定とはしない。新規プライバシー訳は元の英語文面の内容を訳したもので、実装のデータ取扱い・法令適合を新たに監査・認定したものではない。

単体テストは今回未実行（動作ロジックの変更なし）。国別PRの根拠調査、画像・Web・ストアの完全照合、既存Console登録・公開状態・現行価格の確認は別途残る。今回の外部反映は実施なし。商品ID・購入処理・価格・権利は変更なし。

v4の「不足785項目」「Lint未通過」は当時の結果であり、今回の補完と再検証で解消した。本書を現状の確認結果とする。
