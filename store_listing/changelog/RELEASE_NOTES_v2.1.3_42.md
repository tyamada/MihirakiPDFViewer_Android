# Release Notes - v2.1.3 (Code 42)

## English
- Updated the tutorial video and playlist links.
- The playlist opens the first guide in YouTube while retaining the five-video playlist.

## 日本語
- チュートリアルの紹介動画・再生リストへのリンクを更新。
- 再生リストは5本のリストを保持したまま、1本目の動画から開く方式に変更。

## 製品版公開記録
- 記録日: 2026-10-30（日本時間）
- アプリ: MihirakiPDFViewer / 見開きPDFビューア
- applicationId: `com.github.tyamada.MihirakiPDFViewer_Android`
- versionName: `2.1.3` / versionCode: `42`
- 内部テスト配信: 2026-10-30 06:47。
- 製品版の審査承認を確認後、2026-10-30 07:35に完全公開（100%）を実行。
- Play Consoleの製品版トラックで「最新リリース: 42 (2.1.3)」「Google Play で公開」を確認。
- 配信対象は既存の3か国・地域。配信地域の追加は行っていない。
- 一般向けGoogle Play掲載ページに、10月30日の最終更新日と本リリースのリンク更新に関する新機能説明が反映されていることを確認。

### 配信リンク
- 紹介動画: https://www.youtube.com/watch?v=I5tp84abV5o
- チュートリアルの再生リストボタン: https://www.youtube.com/watch?v=KhCn6C-lw7I&list=PLBBAqnUOZR-A
- Google Play: https://play.google.com/store/apps/details?id=com.github.tyamada.MihirakiPDFViewer_Android

## 端末での動作確認

確認対象はチュートリアルから紹介動画・再生リストを開く動作。アプリ全機能の回帰試験を意味しない。

| 端末 | OS | 配信経路・確認者 | 結果 |
|:--|:--|:--|:--|
| Pixel 8a | Android 17 | Google Play内部テスト版42。Codexが実機確認 | 41から42へ更新後、チュートリアルの各ボタンからYouTubeアプリが開き、紹介動画の再生と5本の再生リスト表示・再生を確認。ページ利用不可のエラーなし。 |
| moto g24 | Android 14（既存の端末一覧による） | ユーザーによる確認報告 | ユーザーから正常動作の報告あり。配信経路・インストール済みversionCodeの機械的な確認は未実施。 |
| Galaxy Tab A SM-T510 | Android 9 | 一般向け表示のGoogle Play掲載ページから新規インストール。Codexが実機確認 | 2.1.3 / versionCode 42、インストール元com.android.vending、初回インストール時刻2026-10-30 08:15:25を確認。チュートリアルから紹介動画がYouTubeアプリで正常再生。再生リストは1/5表示と1本目の再生、2/5表示と2本目への連続再生を確認。ページ利用不可のエラーなし。 |

### 確認範囲と留保
- Pixel 8aの確認は内部テスト版42で実施。製品版として別途再インストールした検証ではない。
- Galaxy Tabはユーザーがアプリを削除した後の新規インストールで検証。一般ユーザーの旧版端末で「更新」ボタンを押して42に更新する操作は、対応端末がないため未検証。
- moto g24の結果はユーザー報告として記録し、Codexが直接操作した検証と区別する。
- 過去の再生リストURLで起きたYouTubeアプリのエラーについて、原因を特定したとはしていない。42のアプリ内リンクでは上記の動作確認に成功した。

## 配布物
- AAB: `MihirakiPDFViewer-2.1.3-42.aab`
- SHA-256: `b84a22be9b1c0bec0e127107320c5f1ae947e1793724d7f8825e2f959229e2e9`
- 製品版には内部テストで検証した既存の42を昇格し、再ビルド・別AABへの差し替えは行っていない。

## 検証資料（このMac上）
資料フォルダ: `/Users/tyamada22/Documents/Codex/2026-10-29/android-studio-gemini-gemini-gemini-users/outputs/versionCode42/`

- `PlayConsole-production42-published.png`: 製品版42の100%公開。
- `Pixel42-intro-success.png` / `Pixel42-playlist-success.png`: Pixel 8aのアプリ内リンク確認。
- `GalaxyTab-production-install-check.txt`: Google Playからの新規インストール結果。
- `GalaxyTab42-tutorial-check.txt`: Galaxy Tabのチュートリアル内リンク確認。
- `GalaxyTab42-intro-success.png` / `GalaxyTab42-playlist-success.png`: Galaxy Tabの再生結果。
- moto g24の根拠: 本リリース作業の会話におけるユーザーの正常動作報告。
