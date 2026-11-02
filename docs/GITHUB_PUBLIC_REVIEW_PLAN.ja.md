# 公開GitHub確認と計画への追加（2026-11-02）

> **2026-11-02 実施結果**：以下は改善前の調査記録です。公開main 975bfdbcはローカルHEAD 42bf295の祖先（ローカルのみ5コミット、公開mainのみ0コミット）で、繁体字・韓国語等の変更は既に取り込み済みと確定。追加マージ不要。スクリプト・README・39公開HTML・CIの改善と検証を実施しました。詳細はREPOSITORY_IMPROVEMENT_REPORT.ja.mdを参照。pushは未実施です。

対象はViewerの公開リポジトリ。GitHub API経由で公開main、README日英、Pages用index.html、CI、実機テストスクリプト、検証一覧、Releasesを読み取りました。コード全面監査・全リンク確認ではありません。公開先への変更はしていません。

## 現状と改善計画

1. **公開mainと内部テスト版のソース対応を確定（製品版前）**
   - 公開mainは確認時975bfdbc2fe56d6f0749dfe1fe90138f9c8eba38（繁体字の名称・ローカライズ変更）。ローカルHEADは42bf2952d02b81a98a35ce5f1f76e77a2cd5e532に多数の未コミット変更があり、同一ではありません。公開app/build.gradle.ktsはまだ2.1.3（42）。
   - 最新差分を取得してコミットの関係とローカライズ変更を照合し、既存修正を保ちながら統合方針を決める。現在の作業ツリーへ無条件にpull／resetしない。
   - 配信済みAABのソース記録・SHA-256と公開コミットを対応させる。コードやリソースを変更した場合、配信済み43とは別成果物になる。再アップロードには未使用の新versionCodeとビルド・Lint・署名・実機再検証が必要。現在の43は上書きしない。
2. **更新試験を壊さない実機テスト運用（優先）**
   - 公開scripts/run_device_tests_and_save.pyは接続端末の先頭を選び、既存アプリをアンインストールしてDebug版を入れる。今回のPlay更新試験には使用禁止。
   - 改善時は明示的な端末指定、既存データ保持を既定とする。クリーンインストールは別モード・対象確認を必須にし、Play更新とDebug試験を分離。環境の固定された個人パスも設定値へ変更。
3. **検証結果の誤った合格記録を防止（優先）**
   - 同スクリプトのhas_errorsはPython試験のみを参照し、インストール・起動・単体・端末試験の失敗を検証一覧のNo issues判定へ反映しない。失敗時も完了と記載する箇所がある。
   - 全必須工程の終了コード・実施有無を集計し、未実施／失敗を区別、失敗時は非ゼロ終了。実際にインストールされた版を端末から取得し、検証日時・項目・証拠を残す。全体合格と手動未検証を混同しない。
4. **README・公開ページの説明を現行仕様と一致（製品版公開前）**
   - 公開README日英はJDK17、CIはJDK21。実際に検証済みのビルド環境へ統一し、CI説明にLintも記載。
   - READMEはネット接続がクラウドPDFだけ必要と説明、公開index.htmlも通信不要を広く表現。サンプル取得、購入・復元、更新確認、外部リンクなどの通信を現行実装に合わせて説明。
   - ソートの初期値・切替・保存、現在の名称・アイコン・掲載画像・13言語の公開ページとヘルプの整合を確認。ローカルで修正済みの箇所も公開済みとは扱わない。
5. **リリース履歴・互換性情報の信頼性（製品版公開時）**
   - 公開Releasesの最新はv1.4.0。Android用の説明にiOS App Store審査を理由とする記述があり、READMEの複数の旧履歴がすべて2.1.3（42）になっている。
   - タグ・コミット・実際の配信記録に基づいて履歴を再整理。根拠のない過去版番号を作らない。2.1.4は内部テスト中と製品版公開後を区別して案内する。
   - VERIFICATION_LIST.mdは2.1.3（42）の記録。実機結果が届くまで43へ一括置換せず、OS・端末・実施項目・日付とともに追記する。
6. **CIと公開後の確認（許可後）**
   - 確認時の公開mainのCIとPagesは成功。ただし現在の内部テスト候補のリモートCI成功を意味しない。
   - ローカルCIにはSDKの明示セットアップが追加済み、公開CIには未反映。統合後、許可されたpushに対してCIを確認。翻訳・ストア原稿検査、テスト／Lint結果の保存をCIに追加する計画とする。
   - Pages公開後は実URLで言語切替、Play導線、ヘルプ、プライバシー、サンプルのリンク・表示を確認する。

## 出典

- 公開main：https://github.com/tyamada/MihirakiPDFViewer_Android/commit/975bfdbc2fe56d6f0749dfe1fe90138f9c8eba38
- README：https://github.com/tyamada/MihirakiPDFViewer_Android/blob/main/README_ja.md
- Pagesソース：https://github.com/tyamada/MihirakiPDFViewer_Android/blob/main/docs/index.html
- 実機スクリプト：https://github.com/tyamada/MihirakiPDFViewer_Android/blob/main/scripts/run_device_tests_and_save.py
- CI：https://github.com/tyamada/MihirakiPDFViewer_Android/blob/main/.github/workflows/ci.yml
- 成功した公開CI：https://github.com/tyamada/MihirakiPDFViewer_Android/actions/runs/41307659136
- Releases：https://github.com/tyamada/MihirakiPDFViewer_Android/releases
- 実機一覧：https://github.com/tyamada/MihirakiPDFViewer_Android/blob/main/VERIFICATION_LIST.md

今回は計画追加のみ。アプリ実装・配信済みAAB・配信国・価格・GitHubの公開内容は変更せず、push・製品版審査送信・公開も実施しません。
