# 開発・実機テストガイド

## Play内部テスト版の更新確認

Google Playから更新し、[実機確認手順](docs/DEVICE_TEST_CHECKLIST.ja.md)に従います。設定保持・購入権利・TalkBackは実機で手動確認してください。Debug版のインストールやアプリの削除は行いません。

任意で、選択端末の実インストール版・モデル・Android版を収集できます。

```bash
adb devices
python3 scripts/run_device_tests_and_save.py --serial DEVICE_SERIAL
```

`DEVICE_SERIAL`は対象端末の識別子に置き換えます。複数端末があっても先頭を自動選択しません。既定の`collect`モードはアプリの起動・インストール・削除・データ消去・ログ消去・Gradle試験を行いません。収集成功は機能試験の合格ではありません。

## 専用端末のDebug試験

JDK 21、プロジェクトのGradle／Android Gradle Pluginに対応するAndroid Studio、Android SDK 37.0、Build Tools 36.0.0、platform-toolsとPython 3を用意します。`JAVA_HOME`と`ANDROID_HOME`（または`ANDROID_SDK_ROOT`）を環境に合わせて設定し、`adb`を利用可能にします。個人の端末パスはスクリプトに固定していません。

```bash
python3 scripts/run_device_tests_and_save.py --serial DEVICE_SERIAL --mode debug
```

専用テスト端末で実行します。Debug APKとテストAPKをビルドし、`install -r`で既存データを保持してインストールします。起動、単体・端末試験、Python画像検査を実行します。自動試験自体が設定やテスト用ファイルを変更するため、普段使うアプリのデータ保持検証には使いません。Playインストールが見つかった場合は置換を拒否します。

データを削除して初回状態から試すときだけ、対象端末の識別子を再指定します。

```bash
python3 scripts/run_device_tests_and_save.py --serial DEVICE_SERIAL --mode debug --clean-install --confirm-clean DEVICE_SERIAL
```

署名が異なる既存アプリへの上書きに失敗しても、自動で削除することはありません。別の専用端末を使うか、対象と削除の必要性を確認してください。

## 結果の読み方

`logs/<日時>_<端末>/`に工程別ログ、`result.json`、`summary_report.txt`、実際に実行したJUnit結果を保存します。`--output`で新しい保存先を指定できます。既存フォルダは上書きしません。

- `collect`成功：`collected_manual_checks_pending`。情報収集のみ。
- Debug自動試験成功：`automated_checks_passed_manual_checks_pending`。手動確認は未実施。
- 必須工程失敗：`failed`、終了コード1。未実施工程があれば`incomplete`。
- 単体・端末試験はGradle成功だけでなく、実行されたJUnit件数・失敗・エラー・スキップも確認。古い結果は合格根拠にしません。

`VERIFICATION_LIST.md`は自動更新しません。端末・OS・実インストール版・実施日時・実施項目・未実施項目を確認し、手動の結果も揃えて追記します。アプリ版番号はソースの推測値ではなく端末から取得します。

ログはローカルに保存されます。自動送信はしません。共有前に端末識別子などの私的な情報を確認・除去してください。

## PC上の検証

```bash
./gradlew assembleDebug lintDebug testDebugUnitTest
python3 scripts/test_run_device_tests_and_save.py
python3 scripts/validate_localized_strings.py
python3 scripts/audit_english_residue.py
python3 scripts/validate_store_metadata.py
python3 scripts/validate_public_pages.py
```

GitHub Actionsは署名鍵なしのDebug検証と公開資料検査を行い、結果を保存します。ローカル成功とGitHub上の成功は区別します。

## ストア撮影・サンプルPDF

撮影入口は`scripts/capture_store_screenshots.py`。専用AVDを使い、手順は[追加撮影計画](store_listing/SCREENSHOT_ADDITIONAL_CAPTURE_PLAN.ja.md)を参照してください。撮影や端末試験はアプリ状態を変更します。

サンプルは`testdata/`と配布先`docs/sample/`の対応を確認します。カタログの対象件数・言語・話数は実ファイルと試験結果に基づいて記録し、過去の28件／2話の記録を現在の証拠に流用しません。

## コミット前の文書検査

`scripts/update_docs_and_listings.py`は既定で読み取り検査のみを行い、過去の版番号を置換しません。現在版を自動更新したい箇所だけ、`<!-- current-app-version:start -->2.1.4 (44)<!-- current-app-version:end -->`で囲み、明示的に`--write`を指定します。履歴・過去の検証結果にはマーカーを付けません。ローカルのpre-commit hookから引数なしで呼ばれても、履歴を改変しません。回帰試験は`python3 scripts/test_update_docs_and_listings.py`です。
