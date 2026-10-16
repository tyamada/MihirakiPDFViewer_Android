# MihirakiPDFViewer 開発・実機テストガイド (Development & Device Testing Guide)

本書は、Android Studio または Terminal（コマンドライン）から実機（またはエミュレータ）を接続し、各種テストの実行とログ・レポートの自動保存を行う方法をまとめたガイドです。

---

## 1. 事前準備 (Prerequisites)

1. **実機またはエミュレータの接続確認**
   - Android 端末の「開発者向けオプション」から **USBデバッグ** を有効にし、PCに接続してください。
   - 端末が正常に認識されているか確認します：
     ```bash
     adb devices
     ```
     （例: `297231200298 device` または `R52MC0JK0WA device` が表示されること）

---

## 2. 自動テスト・ログ保存スクリプトの実行

プロジェクトには、実機テストの自動実行とログの `logs/` ディレクトリへの一括保存を行う自動化スクリプト [`run_device_tests_and_save.py`](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/scripts/run_device_tests_and_save.py) が用意されています。

### 実行方法 (Terminal)
プロジェクトのルートディレクトリから以下のコマンドを実行してください：
```bash
python3 scripts/run_device_tests_and_save.py
```

### スクリプトの動作内容
1. 接続されているADBデバイスを自動検出し、ターゲット端末を特定します。
2. 端末のハードウェアプロパティ (`getprop`)、バッテリー状態、アプリのメモリ使用状況などを取得し、`logs/<timestamp>_<serial>/device_info.txt` に保存します。
3. デバイスの logcat をクリア＆キャプチャし、`logs/<timestamp>_<serial>/logcat.log` に保存します。
4. 単体テスト (`./gradlew app:testDebug`) を実行し、結果を `unit_test_result.log` に保存します。
5. 結合テスト / インストルメンテーションテスト (`./gradlew app:connectedDebugAndroidTest`) を実行し、HTML/XMLレポートを `android_test_reports/` にコピーします。
6. すべての実行結果をまとめた `summary_report.txt` を生成します。

---

## 3. 保存先ディレクトリ構造

生成されるすべてのログ・レポートはプロジェクト直下の `logs/` ディレクトリ（Git管理対象外）にタイムスタンプとデバイスシリアルごとに保存されます：

```text
logs/
└── 20261016_133000_R52MC0JK0WA/
    ├── summary_report.txt
    ├── device_info.txt
    ├── logcat.log
    ├── unit_test_result.log
    ├── connected_android_test_result.log
    └── android_test_reports/
        ├── index.html
        └── ...
```

---

## 4. Android Studioからの実行方法

- **単体テスト (Unit Test)**:
  - `app/src/test/java/.../DomainLogicTest.kt` などを右クリックし、`Run 'DomainLogicTest'` を選択。
- **インインストルメンテーションテスト (Instrumentation Test)**:
  - `app/src/androidTest/java/.../` などのテストクラスやパッケージを右クリックし、接続した実機を指定して `Run tests` を選択。
- **UI自動キャプチャ / スクリーンショットスクリプト**:
  - Terminal から `python3 scripts/capture_store_screenshots.py --device Smartphone_6.3 --language ja` を実行してストアスクリーンショットを生成・検証できます。
