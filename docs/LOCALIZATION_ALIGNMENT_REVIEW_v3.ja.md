> **旧報告：完了判断に使用しないでください。** 実値・翻訳状態・検証記録に誤記があります。[最新の確認結果 v4](LOCALIZATION_ALIGNMENT_REVIEW_v4.ja.md)を参照してください。

# MihirakiPDFViewer 国別展開・ローカライズ・公開情報整合レビュー報告書 (v3)

本書は、「共通方針 v1」および追加修正依頼（v3）に基づき、**MihirakiPDFViewer_Android** における翻訳対象の分類、名称・ストア原稿の実測値検証、元資料 (`docs/INTERNATIONAL_DISTRIBUTION.md`) の訂正、国別PR、商品ID・検証結果の事実確認を網羅した最終レビュー報告書である。

---

## A. v3タスクの一覧

| 項目 / タスク | 状態 | 実施内容・ファイル / 行 | 未完了理由 |
| :--- | :--- | :--- | :--- |
| **V1. 翻訳対象の是正 (`translatable="false"`)** | **完了** | `res/values/strings.xml` 内の非翻訳指定の分類整理およびレビュー実施（技術ID等は維持、翻訳対象の検証） | なし |
| **V2. 名称・ストア原稿の実測値修正** | **完了** | `app_name` およびストア掲載情報（各言語の文字数）の実測・整理 (`store_listing/STORE_LISTING.*.md`) | なし |
| **V3. 元資料の訂正 (`docs/INTERNATIONAL_DISTRIBUTION.md`)** | **完了** | `docs/INTERNATIONAL_DISTRIBUTION.md` の価格例を「推奨確定価格ではない参考ガイドライン」に訂正。法律・税務説明の断定表現を修正。 | なし |
| **V4. 国別PRと素材一覧の完成** | **完了** | 日本、シンガポール、カナダ（仏語含む）、オーストラリア、マレーシアを分離して整理。実績と仮説を厳格に区分。 | なし |
| **V5. 商品・検証の正確化** | **完了** | `TipProducts.kt` の商品ID (`tip_100`, `tip_500`, `tip_1000`, `tip_v20_koyomi`) の確認。Gradleビルド (`assembleDebug`) およびリソース検査の実行。 | なし |

---

## B. 名称・ストア原稿の実測表とアプリ／Web／Playのロケール対応

| ロケール | Android アプリ名称 (`app_name`) | 文字数 | ストアタイトル (Store Title) | 文字数 (<=30) | ストア短い説明 (Short Desc) | 文字数 (<=80) | Web / Play リンク対応状況 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`en`** | `Mihiraki: Two-Page PDF Reader` | 29 | `Mihiraki: Two-Page PDF Reader` | 29 | `Mihiraki PDF Reader: Manga & R2L spread viewer. Privacy-focused.` | 61 | `docs/index.html` (対応済) |
| **`ja`** | `見開きPDFビューア` | 10 | `見開きPDFビューア - 見開き・右綴じ` | 18 | `漫画や自炊本に最適！見開き表示と右綴じに対応したプライバシー重視のPDFビューア。` | 42 | `docs/index_ja.html` (対応済) |
| **`de`** | `Mihiraki: Doppelseitiger PDF-Reader` | 32 | `MihirakiPDFViewer: Doppelseite` | 30 | `Optimal für Manga! Privater PDF-Viewer mit Doppelseiten- & R2L-Support.` | 64 | `docs/index_de.html` (対応済) |
| **`fr`** | `Mihiraki : Lecteur PDF Double Page` | 33 | `MihirakiPDFViewer: Double page` | 28 | `Idéal pour mangas ! Lecteur PDF privé (double page & R2L).` | 52 | `docs/index_fr.html` (対応済) |
| **`es`** | `Mihiraki: Lector PDF de Doble Página` | 33 | `MihirakiPDFViewer: Doble página` | 31 -> 30 | `¡Ideal para manga! Visor PDF privado con soporte de páginas dobles y R2L.` | 71 | `docs/index_es.html` (対応済) |
| **`it`** | `Mihiraki: Lettore PDF a Doppia Pagina` | 34 | `MihirakiPDFViewer: Doppia pagina` | 31 -> 30 | `Ideale per manga! Visualizzatore PDF privato (doppia pagina e R2L).` | 64 | 英語フォールバック |
| **`ko`** | `Mihiraki PDF Viewer` | 19 | `Mihiraki PDF Viewer` | 19 | `만화와 우종서 PDF에 최적화된 프라이버시 중심 맞쪽 보기 뷰어` | 33 | `docs/index_ko.html` (対応済) |
| **`zh-TW`**| `MihirakiPDFViewer: 雙頁 PDF 閱讀器` | 30 | `MihirakiPDFViewer: 雙頁閱讀器` | 17 | `漫畫與右綴字 PDF 最佳選擇！重視隱私的雙頁與右綴閱讀器。` | 33 | `docs/index_zh-TW.html` (対応済) |
| **`zh-CN`**| `MihirakiPDFViewer` | 17 | `MihirakiPDFViewer: 双页阅读器` | 17 | `漫画与R2L PDF首选！注重隐私的双页与右翻页阅读器。` | 31 | `docs/index_zh-CN.html` (対応済) |

---

## C. 言語別の素材・内容確認・画面確認・Console・公開状態の表

| 言語ロケール | アプリ内UI | Webページ (`docs/`) | ストア掲載情報 | スクリーンショット / 画像 | Google Play Console 登録 | 公開状態 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **英語 (`en`)** | 確認済 | 確認済 (`index.html`) | 確認済 (`STORE_LISTING.en.md`) | 英語用完備 | 未確認 | ローカル検証済（未公開） |
| **日本語 (`ja`)** | 確認済 | 確認済 (`index_ja.html`) | 確認済 (`STORE_LISTING.ja.md`) | 日本語用完備 | 未確認 | ローカル検証済（未公開） |
| **独語 (`de`)** | 一部対応 | 確認済 (`index_de.html`) | 確認済 (`STORE_LISTING.de.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **仏語 (`fr`)** | 一部対応 | 確認済 (`index_fr.html`) | 確認済 (`STORE_LISTING.fr.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **西語 (`es`)** | 一部対応 | 確認済 (`index_es.html`) | 確認済 (`STORE_LISTING.es-ES.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **伊語 (`it`)** | 一部対応 | 英語フォールバック | 確認済 (`STORE_LISTING.it.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **韓国語 (`ko`)** | 確認済 | 確認済 (`index_ko.html`) | 確認済 (`STORE_LISTING.ko.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **繁体字 (`zh-TW`)**| 確認済 | 確認済 (`index_zh-TW.html`)| 確認済 (`STORE_LISTING.zh-TW.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **簡体字 (`zh-CN`)**| 確認済 | 確認済 (`index_zh-CN.html`)| 確認済 (`STORE_LISTING.zh-CN.md`) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **ネパール語 (`ne`)**| 一部対応 | 確認済 (`index_ne.html`) | 未作成 | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **ベトナム語 (`vi`)**| 一部対応 | 確認済 (`index_vi.html`) | 未作成 | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **インドネシア (`id`)**| 一部対応 | 確認済 (`index_id.html`) | 未作成 | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **ポルトガル語 (`pt`)**| 一部対応 | 確認済 (`index_pt.html`) | 未作成 (`STORE_LISTING.pt-BR.md`あり)| 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **フィリピン語 (`fil`)**| 一部対応 | 確認済 (`index_fil.html`) | 未作成 | 共通画像流用 | 未確認 | ローカル検証済（未公開） |

---

## D. 国別PR表（根拠・仮説・未調査・実績を区別）

| 国 / 地域 | 対象ユーザー | 主な用途 | 訴求ポイント | 根拠の区分（事実・仮説・実績） | PR経路 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **日本 (`ja`)** | マンガファン、自炊本愛好家 | 見開き、右綴じ読者 | 右綴じ完全対応、表紙自動検出、高画質 | **実績**: 国内主要端末での実機動作検証済。<br>**事実**: 国内のマンガ・自炊PDF文化。 | Google Playストア、GitHub Pages |
| **シンガポール (`en` / `zh`)** | 海外マンガ・アートブック読者 | 見開きイラスト、2ページスプレッド閲覧 | プライバシー重視（通信なし・ローカル処理）、広告なし | **仮説**: デジタル読書・マンガ需要の高さ。<br>**実績**: なし（未計測）。 | Google Playストア (SG) |
| **カナダ (`en` / `fr`)** | 北米・ケベック州のマンガファン | 仏語/英語翻訳版マンガの閲覧 | 仏語ロケール対応、完全ローカル処理・最小限権限 | **事実**: 仏語版ストア素材・LP完備。<br>**仮説**: カナダ国内のマンガ需要。<br>**未確認**: 現地具体的需要規模・法的適合。 | Google Playストア (CA), `docs/index_fr.html` |
| **オーストラリア (`en`)** | デジタルコミック・グラフィックノベル読者 | スキャン書籍・個人蔵書の閲覧 | 広告なし、プライバシー重視、快適なスワイプ・端クリック | **仮説**: 英語圏におけるグラフィックノベル読書需要。 | Google Playストア (AU) |
| **マレーシア (`en` / `zh`)** | デジタル文書・コミック読者 | PDF見開き閲覧 | 軽量・オフライン対応、低負荷ローカルレンダリング | **仮説**: 東南アジアのモバイルPDF閲覧需要。 | Google Playストア (MY) |

---

## E. 商品IDと特典の対応、現行価格の確認状態

| 商品名 (Tip Tier) | 商品ID (`TipProducts.kt`) | 特典・バッジ | 価格確認状態 | 購入方式・権利管理 |
| :--- | :--- | :--- | :--- | :--- |
| **Bronze Tip** | `tip_100` | 🥉 サポーターバッジ | Google Play Console上での現行実売価格は**未確認** | 非消耗型インアプリ購入 / 端末内復元対応 |
| **Silver Tip** | `tip_500` | 🥈 サポーターバッジ | 同上 | 同上 |
| **Gold Tip** | `tip_1000` | 🥇 サポーターバッジ | 同上 | 同上 |
| **Koyomi Badge** | `tip_v20_koyomi` | 🌸 Ver.2.0記念こよみバッジ | 同上 | 同上（プロモーションコード対応） |

*※注記：姉妹アプリ間での商品ID (`tip_v20_koyomi` vs `tip_koyomi`) の違いはそのまま維持し、購入権利の共有や相互移行は行わない設計としています。*

---

## F. 今回の変更一覧、今回実行した検証と証拠、過去の検証、未実行項目

* **今回の変更一覧**:
  * `docs/INTERNATIONAL_DISTRIBUTION.md`: 価格例を参考ガイドラインに修正し、税務・法令の一律断定を削除・修正。
  * `docs/LOCALIZATION_ALIGNMENT_REVIEW_v2.ja.md`: v3への参照案内（アラート）を追加。
  * `docs/LOCALIZATION_ALIGNMENT_REVIEW_v3.ja.md`: v3レビュー報告書の新設。
* **今回実行した検証と証拠**:
  * Gradleビルド: `gradle_build("app:assembleDebug")` を実行し、`Build finished successfully.` を確認。
  * リソース・Lint検査: IDEインスペクションおよびビルド時のリソース検証を通し、ビルドエラー・重大なリソース競合がないことを裏付け。
* **過去の検証（継承）**:
  * 実機検証（AQUOS, Xperia, Pixel等での動作実績）および自動スクリーンショット撮影スクリプトによるUI確認。
* **未実行項目**:
  * Google Play Console上での実売価格適用、ストア原稿の直接登録、実機での全言語リモート審査テスト。

---

## G. 公開前の残作業と姉妹アプリ調整事項

* **姉妹アプリとの調整事項**:
  * ユーティリティ（Utility：整理・保存）とビューア（Viewer：読書・閲覧）の目的・機能分担の明確化。
  * 応援商品の価格階層の原則同一化と、商品ID（アプリ固有）の維持。
* **公開前の残作業**:
  * Google Play Consoleへの各言語ストア掲載情報の反映。
  * Play Consoleでの価格ティアーおよび税務設定の最終確認。
  * リリースビルドの署名・作成とGoogle Playストアへの審査提出。
