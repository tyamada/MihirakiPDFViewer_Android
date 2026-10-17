# Play Consoleスクリーンショット登録の準備

## この環境で用意したもの

- Python 3.14用の専用仮想環境 `.venv-play-console/`
- `google-api-python-client` と `google-auth`（バージョンは [`requirements-play-console.txt`](../requirements-play-console.txt)）
- 画像・メタデータを検査し、対応するPlay掲載枠へ登録する [`play_console_screenshots.py`](../scripts/play_console_screenshots.py)

サービスアカウント鍵はリポジトリに置かないでください。Google CloudとPlay Consoleでのアカウント設定は、アカウント管理者側で行います。

## Play Console / Google Cloud側で必要な設定

1. Google Cloudプロジェクトを作成または選択し、Google Play Developer APIを有効にします。
2. サービスアカウントを作成し、Play Consoleの「ユーザーと権限」に追加します。ユーザーのアプリ権限で対象アプリを選び、「アプリ情報の表示（読み取り専用）」と「ストアのプレゼンスの管理」を付与します。後者が掲載テキスト・画像の編集権限です。リリース権限は画像登録には不要です。
3. サービスアカウントJSON鍵をリポジトリ外の安全な場所へ保存し、環境変数 `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` にそのパスを設定します。鍵をGitへ追加しないでください。
4. 今回の14端末ロケールにはリポジトリ内の掲載文案が用意されています。Play Consoleの掲載APIでは端末ロケールと異なる言語コードを使う場合があり、`stage` は公式のコードへ変換して掲載情報の作成と画像登録を行います。たとえば Filipino は `fil-PH` ではなく `fil`、Indonesian は `id-ID` ではなく `id`、Vietnamese は `vi-VN` ではなく `vi` です。掲載情報がない言語は同じ未確定編集内で文案から先に作成します。今回不足している6言語も対象です。[掲載可能な言語一覧](https://support.google.com/googleplay/android-developer/answer/9844778)

サービスアカウントのメールアドレスは、鍵JSONの`client_email`です。値だけを確認する場合は次を実行できます。JSON鍵の内容は共有しないでください。

```sh
python3 -c 'import json,os; print(json.load(open(os.environ["PLAY_CONSOLE_SERVICE_ACCOUNT_JSON"]))["client_email"])'
```

Play Consoleで権限を変更した後、APIへ反映するまで時間がかかることがあります。すぐに同じ403が出る場合は、対象アプリと権限を再確認してから時間を置いて再試行してください。

公式の設定手順は[Google Play Developer APIの導入ガイド](https://developers.google.com/android-publisher/getting_started)を参照してください。

## ローカル監査とAPI登録

まずローカル画像を検査し、APIへ送る294枚のマニフェストを作ります。監査はPlay Consoleへ接続しません。

```sh
.venv-play-console/bin/python scripts/play_console_screenshots.py audit
```

次のコマンドは不足ロケールの掲載文案を編集に追加してから、スマートフォン・7インチタブレット・10インチタブレットの画像をPlay Consoleの未確定編集に登録します。各ロケール・端末種別の既存スクリーンショットはその編集内で置き換えます。14言語の該当3枠、合計294枚が対象です。

```sh
.venv-play-console/bin/python scripts/play_console_screenshots.py stage
```

`stage`は変更を確定しません。内容を確認後、掲載情報へ反映するときだけ次を実行します。これはストア掲載情報の変更を確定する操作です。

```sh
.venv-play-console/bin/python scripts/play_console_screenshots.py commit \
  --confirm-commit com.github.tyamada.MihirakiPDFViewer_Android:33
```

確定しない場合は、期限切れを待たず次で未確定編集を破棄できます。

```sh
.venv-play-console/bin/python scripts/play_console_screenshots.py discard
```

## Chromebook画像

Google Playの掲載画面にはChromebook専用枠がありますが、Android Publisher APIの画像種別にはChromebookまたはDesktopがありません。そのため、Chromebook分84枚（14言語×6枚）はAPIマニフェストから除外しています。Play Consoleのメインストア掲載情報にあるChromebook用グラフィック欄へ登録してください。公式ヘルプ上、Chromebook・タブレットは各端末種別につき4枚以上、最大8枚です。[プレビュー素材の要件](https://support.google.com/googleplay/android-developer/answer/9866151)

このツールは `audit` と `stage` を標準の安全な手順とし、編集の確定にはアプリIDとversionCodeを含む明示的な確認文字列を要求します。`stage` は編集を確定せずに終了します。
