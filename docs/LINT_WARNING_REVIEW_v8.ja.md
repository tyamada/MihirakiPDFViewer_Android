# Viewer Lint警告・追加レビュー v8

2026-11-02。対象：2.1.4（43）、AAB SHA-256 `3c5d9330918e47959cd0a21eecf2a4978cd0802c63ab1b9195c9bcc98ee03d6d`。

Debug・Releaseともエラー0、警告9。v7の4件という値は過去の結果である。

- 更新通知6件は今回のリリースでまとめて更新せず、CI同等とR8版実動作を確認した構成を維持。
- TrustAllX509TrustManager 3件の対象ESTクラスは、対応するR8 usageでクラス全体削除を確認。mappingに残存・インライン化記録なし。アプリ独自のTrustManager/SSLContext/証明書検査上書きは見つからず、サンプルHTTPS取得にも成功。今回の3警告について独自TLSコード追加や一括keepは不要と判断。
- 新たな警告抑制は追加していない。対象外の暗号処理や依存関係全体の安全性を認定したものではなく、偽証明書を実アプリへ注入して拒否を試験したわけではない。

証拠：validation/release-preparation/followup/lint_followup_review.json。詳細と残件はRELEASE_PREPARATION_REPORT.ja.mdを参照。

参考：https://developer.android.com/privacy-and-security/security-ssl 、https://support.google.com/faqs/answer/6346016?hl=en
