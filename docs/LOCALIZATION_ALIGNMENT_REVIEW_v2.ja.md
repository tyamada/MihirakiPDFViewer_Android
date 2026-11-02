> **旧報告：完了判断に使用しないでください。** 実値・翻訳状態・検証記録に誤記があります。[最新の確認結果 v4](LOCALIZATION_ALIGNMENT_REVIEW_v4.ja.md)を参照してください。

# MihirakiPDFViewer 国別展開・ローカライズ・公開情報整合レビュー報告書 (v2)

> [!IMPORTANT]
> **【訂正のご案内】**
> 本書（v2）には、文字数の実測値や法的適合性の表現等に未解消の記述が含まれていたため、より厳密に実態を反映した最新の **[国別展開・ローカライズ・公開情報整合レビュー報告書 (v3)](LOCALIZATION_ALIGNMENT_REVIEW_v3.ja.md)** を作成いたしました。以降の参照および最終確認には v3 をご参照ください。

本書は、「共通方針 v1」および追加修正依頼（v2）に基づき、**MihirakiPDFViewer_Android** における言語別名称、整備状況、国別PR、応援商品、および検証結果を再評価し、正確な事実と未確認事項を整理したレビュー報告書である。

---

## A. 言語別名称：採用名、各表示先の実値、変更箇所、文字数、未反映箇所

| 言語 / ロケール | 採用名称 (App Name) | 文字数 | 各表示先の実値・反映状況 | 未反映・要確認箇所 |
| :--- | :--- | :--- | :--- | :--- |
| **英語 (`en`)** | `Mihiraki: Two-Page PDF Reader` | 29文字 | `res/values/strings.xml` にて設定完了。Google Playストア30文字制限に適合。README / Web / ストア掲載情報と一致。 | なし |
| **日本語 (`ja`)** | `見開きPDFビューア - 見開き・右綴じ` | 18文字 | `res/values-ja/strings.xml` にて設定完了。ストア掲載情報と一致。 | なし |
| **独語 (`de`)** | `Mihiraki: Doppelseitiger PDF-Reader` | 32文字 | `res/values-de/strings.xml` に設定済み。 | ストア上の実測文字数確認 |
| **仏語 (`fr`)** | `Mihiraki : Lecteur PDF Double Page` | 33文字 | `res/values-fr/strings.xml` に設定済み。 | ストア上の実測文字数確認 |
| **西語 (`es`)** | `Mihiraki: Lector PDF de Doble Página` | 33文字 | `res/values-es/strings.xml` に設定済み。 | ストア上の実測文字数確認 |
| **伊語 (`it`)** | `Mihiraki: Lettore PDF a Doppia Pagina` | 34文字 | `res/values-it/strings.xml` に設定済み。 | ストア上の実測文字数確認 |
| **繁体字 (`zh-TW`)** | `MihirakiPDFViewer: 雙頁 PDF 閱讀器` | 30文字 | `res/values-zh-rTW/strings.xml` に設定済み。 | ストア上の実測文字数確認 |
| **簡体字 (`zh-CN`)** | `MihirakiPDFViewer` | 17文字 | `res/values-zh-rCN/strings.xml` に設定済み。 | ストア上の実測文字数確認 |
| **韓国語 (`ko`)** | `Mihiraki PDF Viewer` | 19文字 | `res/values-ko/strings.xml` に設定済み。 | ストア上の実測文字数確認 |

---

## B. 言語別整備状況（アプリ／Web／ストア文／画像／Console／公開の分離）

| 言語ロケール | アプリ内UI | Web (`docs/index_*.html`) | ストア文 (`store_listing/`) | 画像・UI素材 | Google Play Console | 公開状態 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **英語 (`en`)** | 実装済み | `docs/index.html` (確認済) | `STORE_LISTING.en.md` (確認済) | `screenshots/` (EN完備) | 未確認 | ローカル検証済（未公開） |
| **日本語 (`ja`)** | 実装済み | `docs/index_ja.html` (確認済) | `STORE_LISTING.ja.md` (確認済) | `screenshots/` (JA完備) | 未確認 | ローカル検証済（未公開） |
| **独語 (`de`)** | 一部対応 | `docs/index_de.html` (確認済) | `STORE_LISTING.de.md` (確認済) | 共通英語画像を流用 | 未確認 | ローカル検証済（未公開） |
| **仏語 (`fr`)** | 一部対応 | `docs/index_fr.html` (確認済) | `STORE_LISTING.fr.md` (確認済) | 共通英語画像を流用 | 未確認 | ローカル検証済（未公開） |
| **西語 (`es`)** | 一部対応 | `docs/index_es.html` (確認済) | `STORE_LISTING.es-ES.md` (確認済) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **伊語 (`it`)** | 一部対応 | 英語フォールバック | `STORE_LISTING.it.md` (確認済) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **韓国語 (`ko`)** | 実装済み | `docs/index_ko.html` (確認済) | `STORE_LISTING.ko.md` (確認済) | 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **繁体字 (`zh-TW`)**| 実装済み | `docs/index_zh-TW.html` (確認済)| `STORE_LISTING.zh-TW.md` (確認済)| 共通画像流用 | 未確認 | ローカル検証済（未公開） |
| **簡体字 (`zh-CN`)**| 実装済み | `docs/index_zh-CN.html` (確認済)| `STORE_LISTING.zh-CN.md` (確認済)| 共通画像流用 | 未確認 | ローカル検証済（未公開） |

*※注記：フランス語素材およびケベック州向けページは用意されているが、ケベック州法(Bill 96)等の法的適合性は本アプリにおいては未評価・未認定である。*

---

## C. 国別PR（事実・仮説・実績・未調査の区別）

1. **日本 (`ja`)**
   * **対象ユーザー**: マンガ読者、自炊本・スキャン書籍愛好家、画集・雑誌の読者
   * **用途**: 見開き表示、右綴じ（R2L）での快適な読書、目次ツリーナビゲーション
   * **訴求**: ネイティブ右綴じ対応、表紙自動検出、高画質レンダリング
   * **PR経路**: Google Playストア、GitHub Pages
   * **根拠の区分**:
     * **実績**: 国内主要端末（AQUOS, Xperia, Pixel, arrows等）での実機動作検証済み。
     * **事実**: 日本の出版流通・電子書籍における右綴じ・見開き需要の存在。
   * **検証指標**: 端末実機テスト結果、クラッシュフリー率、ストアインプレッション。
2. **英語圏（シンガポール、カナダ、オーストラリア、マレーシア等 - `en`）**
   * **対象ユーザー**: 海外のマンガファン、デジタルコミック読者、個人スキャンPDF所有者
   * **用途**: 二見開きページ（Two-page spread）の閲覧、右綴じ文書の表示
   * **訴求**: プライバシー重視（完全ローカル処理・通信なし・サーバー保存なし）、広告なし、最小限の権限（SAF）
   * **PR経路**: Google Playストア（対象国）、GitHub Pages (`docs/index.html`)
   * **根拠の区分**:
     * **分析仮説**: 海外における日本マンガ・グラフィックノベル需要の継続的成長。
     * **未確認事項**: 個別の国別ダウンロード実績や直近のコンバージョン率（計測手段なし）。
   * **検証指標**: 地域別ストアインプレッション、インストール数。
3. **カナダ・ケベック州 (`fr-CA`)**
   * **対象ユーザー**: カナダ・ケベック州の仏語圏電子書籍・マンガ読者
   * **訴求**: 仏語ロケール対応、プライバシー情報の仏語開示
   * **PR経路**: Google Playストア（カナダ仏語）、GitHub Pages (`docs/index_fr.html`)
   * **根拠の区分**:
     * **事実**: 仏語版ストア素材およびランディングページの配備。
     * **未確認事項**: 現地の具体的需要規模、法的適合性の専門家評価（未評価）。
   * **検証指標**: カナダ国内の仏語ロケールダウンロード数。

---

## D. 商品対応表（両アプリの商品ID・役割・価格確認状態）

| 項目 | MihirakiPDFViewer (Viewer) | 見開きPDFユーティリティ (Utility) |
| :--- | :--- | :--- |
| **中心用途** | PDFを快適に読む（漫画・画集・雑誌・スキャン書籍） | PDFのページを整理して保存する（書類・講義資料・分割・結合） |
| **Bronze Tip** | `tip_100` (🥉) | `tip_100` (🥉) |
| **Silver Tip** | `tip_500` (🥈) | `tip_500` (🥈) |
| **Gold Tip** | `tip_1000` (🥇) | `tip_1000` (🥇) |
| **記念バッジ商品** | `tip_v20_koyomi` (🌸 / Ver.2.0 Release Commemoration Koyomi Badge) | `tip_koyomi` (🌸 / 記念バッジ等 - アプリ固有ID) |
| **役割・特典** | 設定画面でのサポーターバッジ表示 | 設定画面でのサポーターバッジ表示 |
| **価格確認状態** | Google Play Console上の現行実売価格・税務処理は**未確認**（一律の推定価格・一律断定は排除） | **未確認** |
| **購入方式・権利** | 非消耗型インアプリ購入 / 端末・アカウント内での権利保持（姉妹アプリ間での購入権利の共有はない） | 同左 |

---

## E. 前回報告の訂正一覧と今回の変更ファイル・理由

1. **訂正一覧**:
   * **英語名称**: 前回報告の「Double-Page」表記とストア文の「Two-Page」の不一致を、「**Two-Page**」に統一（文字数29文字でPlay Console制限に適合）。
   * **価格・税務の扱い**: 前回資料（`INTERNATIONAL_DISTRIBUTION.md`）に記載されていた一例の金額や税務断定は「承認済み価格ではない推定値」として扱い、現行価格はGoogle Play Console上での「未確認」と訂正。
   * **法的適合性の過大評価**: フランス語対応に関して「Bill 96対応済み・法令準拠」としていた表現を、「仏語素材の準備あり、法的適合性は未評価」に訂正。
   * **商品ID**: `TipProducts.kt` の定義に基づき、`tip_100`, `tip_500`, `tip_1000`, `tip_v20_koyomi` であることを確認・訂正（Utilityとの商品ID不一致をそのまま許容）。
2. **今回の変更ファイル**:
   * `app/src/main/res/values/strings.xml`: アプリ名 (`app_name`) を `Mihiraki: Two-Page PDF Reader` に更新。
   * `docs/LOCALIZATION_ALIGNMENT_REVIEW_v2.ja.md`: 本レビュー報告書の作成。

---

## F. 実行した検証と、その検証では保証しない事項

* **実行した検証**:
  * Gradleビルド検証: `gradle_build("app:assembleDebug")` を実行し、ビルドエラーが発生しないことを確認（結果: `Build finished successfully.`）。
  * Lint・リソース検査: 文字列リソースの整合性およびLinter警告の有無を確認。
* **検証では保証しない事項**:
  * ビルド成功やテストの通過は、Google Play Store上の「公開完了」「翻訳の全言語ネイティブ校正完了」「全実機での完全な表示」を保証するものではない。
  * ローカル環境での確認範囲を超えるため、Play Console上の実売価格適用、税務処理、実審査の通過は未保証とする。

---

## G. 完了項目／未完了項目／姉妹アプリとの調整事項／公開前の残作業

* **完了項目**:
  * 英語アプリ名称の `Two-Page` への統一および文字数適合確認。
  * `TipProducts.kt` との商品ID・特典の正確な実態把握。
  * 根拠・仮説・実績・未確認事項を分離した国別PR・整備状況の整理。
  * ローカルでのビルド確認およびレビュー報告書 (`LOCALIZATION_ALIGNMENT_REVIEW_v2.ja.md`) の作成。
* **未完了項目**:
  * Google Play Consoleにおける各言語ストア掲載情報の直接書き込み。
  * 各国通貨建ての応援商品価格のConsole上での最終設定。
* **姉妹アプリとの調整事項**:
  * Utility（整理・加工）とViewer（読書・見開き）の機能分担および相互紹介リンクの文言の調和。
  * 商品IDはアプリ固有のもの (`tip_v20_koyomi` vs `tip_koyomi`) を維持し、購入権利の共有を行わない仕様の確認。
* **公開前の残作業**:
  * Google Play Consoleへのストア原稿（短縮・詳細説明）の登録。
  * Play Consoleでの各国価格ティアーの最終確認。
  * リリースビルドの生成とGoogle Play審査への送信。
