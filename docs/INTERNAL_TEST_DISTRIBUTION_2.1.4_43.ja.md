# Viewer 2.1.4（43）内部テスト配信記録

Play Consoleで **「内部テスターに公開」** を確認。公開日時のConsole表示は2026年11月2日17:48。

- 配信先：既存の **Internal Testers 2名**。リストの選択・メンバーは変更していない。
- リリース：43 (2.1.4)、内部トラックID 4701546827696869806、リリースID 28。
- 配信したAAB：MihirakiPDFViewer-2.1.4-43.aab（18,190,740 bytes）。
- SHA-256：`3c5d9330918e47959cd0a21eecf2a4978cd0802c63ab1b9195c9bcc98ee03d6d`。アップロード前後にローカルの最終ファイルとの一致を確認。
- 日英韓のリリースノートを登録。ReTraceマッピングの添付をConsoleで確認。
- ネイティブデバッグシンボル未登録の推奨警告1件。配信を妨げるものではなく、内部配信は完了した。
- 対応端末の追加・対象外化はいずれも0件。

## 変更していない項目

製品版は **2.1.3（42）** のまま。製品版の配信国は **米国・日本・韓国** のままで、米国の既存7地域も維持。配信前後のConsoleで確認した。国・価格・テスターのメンバーは編集していない。製品版の審査送信・公開、Gitのpushは行っていない。

## 実機での次の操作

1. 実機で登録済みのGoogleアカウントを使用して、[内部テスト参加リンク](https://play.google.com/apps/internaltest/4701546827696869806)を開く。
2. Google Playから、既存アプリをアンインストールせず更新する。反映には時間がかかる場合がある。
3. アプリのバージョンが2.1.4／43になったことを確認し、保存済みの更新前スクリーンショットと設定・購入済み状態を比較する。
4. 実機のTalkBack、文字拡大、PDF表示・検索・目次・ソート、購入復元を確認する。

利用者から更新前スクリーンショットの保存完了は報告済み。今回、実機への更新そのものや更新後の状態比較は実施していない。

## 記録した画面

- internal-test-published.jpg：内部テスト43の公開完了。
- production-countries-before.jpg / production-countries-after.jpg：製品版42、配信国3か国を維持。
- testers-before.jpg / testers-after.jpg：Internal Testers 2名の選択を維持。
- review-warning.jpg：ネイティブデバッグシンボルに関する推奨警告。

[Play Consoleのリリース詳細](https://play.google.com/console/u/1/developers/8825504212632848490/app/4975203753311543426/tracks/4701546827696869806/releases/28/details)
