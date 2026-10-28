#!/usr/bin/env python3
import pathlib
import shutil

def main():
    root = pathlib.Path(__file__).resolve().parent.parent
    docs_dir = root / "docs"
    assets_dir = docs_dir / "assets"
    assets_dir.mkdir(parents=True, exist_ok=True)

    # 1. Copy app icon
    src_icon = root / "assets" / "app-icon" / "MihirakiPDFViewer-AppIcon-MangaBookPose.png"
    dest_icon = assets_dir / "app-icon.png"
    if src_icon.exists():
        shutil.copy2(src_icon, dest_icon)
        print(f"Copied app icon to {dest_icon}")

    # 2. Write updated style.css
    css_content = """:root {
    color-scheme: light dark;
    --background: #f5faf9;
    --surface: rgba(255, 255, 255, 0.88);
    --text: #173634;
    --muted: #54706d;
    --accent: #087f75;
    --accent-strong: #05675f;
    --line: rgba(8, 127, 117, 0.18);
    --shadow: 0 18px 50px rgba(8, 77, 72, 0.12);
}

* { box-sizing: border-box; }

html { scroll-behavior: smooth; }

body {
    margin: 0;
    color: var(--text);
    background:
        radial-gradient(circle at 10% 0%, rgba(31, 184, 167, 0.17), transparent 32rem),
        linear-gradient(180deg, #f9fcfb 0%, var(--background) 100%);
    font-family: -apple-system, BlinkMacSystemFont, "Hiragino Sans", "Yu Gothic", sans-serif;
    line-height: 1.75;
}

a { color: var(--accent-strong); }
a:hover { color: var(--accent); }

.site-header {
    position: sticky;
    top: 0;
    z-index: 10;
    border-bottom: 1px solid var(--line);
    background: rgba(249, 252, 251, 0.9);
    backdrop-filter: blur(18px);
}

.nav {
    width: min(1080px, calc(100% - 32px));
    min-height: 64px;
    margin: auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
}

.brand {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    color: var(--text);
    font-weight: 750;
    text-decoration: none;
}

.brand img { width: 36px; height: 36px; border-radius: 9px; }

.nav-links { display: flex; gap: 18px; flex-wrap: wrap; }
.nav-links a { color: var(--muted); font-size: 0.92rem; text-decoration: none; }
.nav-links a[aria-current="page"] { color: var(--accent-strong); font-weight: 700; }
.language-picker {
    max-width: 180px;
    padding: 7px 30px 7px 10px;
    border: 1px solid var(--line);
    border-radius: 10px;
    background: var(--surface);
    color: var(--text);
    font: inherit;
}

.hero, main, .footer-inner {
    width: min(1080px, calc(100% - 32px));
    margin-inline: auto;
}

.hero {
    min-height: 620px;
    display: grid;
    grid-template-columns: 1.25fr 0.75fr;
    align-items: center;
    gap: 64px;
    padding-block: 84px;
}

.eyebrow {
    color: var(--accent-strong);
    font-weight: 750;
    letter-spacing: 0.08em;
}

h1 { margin: 12px 0 20px; font-size: clamp(2.6rem, 7vw, 5.4rem); line-height: 1.03; letter-spacing: -0.04em; }
h2 { margin-top: 0; font-size: clamp(1.6rem, 4vw, 2.4rem); line-height: 1.25; }
h3 { margin-bottom: 8px; }
.lead { max-width: 720px; color: var(--muted); font-size: clamp(1.05rem, 2.4vw, 1.28rem); }

.hero-icon {
    width: min(100%, 320px);
    justify-self: center;
    border-radius: 28%;
    box-shadow: var(--shadow);
}

.actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 30px; }
.button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 46px;
    padding: 10px 18px;
    border: 1px solid var(--accent);
    border-radius: 999px;
    background: var(--accent);
    color: white;
    font-weight: 700;
    text-decoration: none;
}
.button:hover { background: var(--accent-strong); color: white; }
.button.secondary { background: transparent; color: var(--accent-strong); }

.section { padding-block: 72px; }
.section > p { max-width: 760px; color: var(--muted); }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; margin-top: 28px; }
.card, .document {
    border: 1px solid var(--line);
    border-radius: 22px;
    background: var(--surface);
    box-shadow: var(--shadow);
}
.card { padding: 26px; }
.card p { margin-bottom: 0; color: var(--muted); }
.symbol { font-size: 1.7rem; }

.document { max-width: 820px; margin: 64px auto; padding: clamp(24px, 6vw, 58px); }
.document h1 { font-size: clamp(2.1rem, 6vw, 3.5rem); }
.document h2 { margin-top: 42px; font-size: 1.45rem; }
.document li + li { margin-top: 8px; }
.meta { color: var(--muted); font-size: 0.92rem; }
.notice { padding: 18px 20px; border-left: 4px solid var(--accent); background: rgba(8, 127, 117, 0.08); }

footer { margin-top: 70px; border-top: 1px solid var(--line); }
.footer-inner { display: flex; justify-content: space-between; gap: 20px; padding-block: 28px; color: var(--muted); font-size: 0.9rem; }

@media (max-width: 760px) {
    .nav { align-items: flex-start; flex-direction: column; padding-block: 12px; gap: 8px; }
    .hero { min-height: auto; grid-template-columns: 1fr; padding-block: 56px; gap: 38px; }
    .hero-icon { width: 190px; grid-row: 1; }
    .grid { grid-template-columns: 1fr; }
    .footer-inner { flex-direction: column; }
}
"""
    (docs_dir / "style.css").write_text(css_content, encoding="utf-8")
    print("Updated docs/style.css")

    # 3. Update index.html (English)
    index_html = """<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Mihiraki PDF Viewer is a privacy-first, local Android PDF viewer designed for two-page spreads and right-to-left documents like manga and light novels.">
    <title>Mihiraki PDF Viewer for Android</title>
    <link rel="icon" href="assets/app-icon.png">
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <header class="site-header">
        <nav class="nav" aria-label="Main navigation">
            <a class="brand" href="index.html" aria-current="page"><img src="assets/app-icon.png" alt=""><span>Mihiraki PDF Viewer</span></a>
            <div class="nav-links"><a href="#features">Features</a><a href="privacy.html">Privacy</a><a href="support.html">Support</a><a href="https://github.com/tyamada/MihirakiPDFViewer_Android">GitHub</a></div>
        </nav>
    </header>

    <main>
        <section class="hero">
            <div>
                <div class="eyebrow">PRIVACY-FOCUSED ANDROID PDF VIEWER</div>
                <h1>The ultimate two-page spread reader for manga and PDFs.</h1>
                <p class="lead">Designed for right-to-left documents and spreads. Enjoy smooth local rendering, outline tree navigation, and precise search highlights without internet required.</p>
                <div class="actions"><a class="button" href="#features">Explore features</a><a class="button secondary" href="support.html">Help and support</a></div>
            </div>
            <img class="hero-icon" src="assets/app-icon.png" alt="Mihiraki PDF Viewer app icon">
        </section>

        <section class="section" id="features">
            <div class="eyebrow">FEATURES</div>
            <h2>Built for comfortable reading</h2>
            <p>Read manga, magazines, and documents with complete right-to-left support and intelligent layout detection.</p>
            <div class="grid">
                <article class="card"><div class="symbol">📖</div><h3>Two-Page Spread</h3><p>Seamlessly display two pages side-by-side with cover mode support.</p></article>
                <article class="card"><div class="symbol">🇯🇵</div><h3>Right-to-Left (R2L)</h3><p>Native support for Japanese and traditional reading directions.</p></article>
                <article class="card"><div class="symbol">📑</div><h3>Outline Navigation</h3><p>Jump instantly to any chapter or section using PDF bookmarks.</p></article>
                <article class="card"><div class="symbol">🔍</div><h3>Fast Text Search</h3><p>Search text with precision yellow background and red outline highlights.</p></article>
                <article class="card"><div class="symbol">🛡️</div><h3>100% Local & Private</h3><p>No internet connection required. Operates via Storage Access Framework.</p></article>
                <article class="card"><div class="symbol">📱</div><h3>Adaptive Layout</h3><p>Optimized for phones, tablets, foldables, and desktop window modes.</p></article>
            </div>
        </section>

        <section class="section">
            <div class="eyebrow">DIAGNOSTICS & LOGGING</div>
            <h2>On-Device Testing & App Logs</h2>
            <p>Run device diagnostic tests and inspect secure local app logs (`AppLogger`) stored safely for up to 24 hours. Never sends data externally automatically.</p>
        </section>

        <section class="section">
            <div class="eyebrow">OPTIONAL SUPPORT</div>
            <h2>Support the Developer</h2>
            <p>All core reading features are completely free. Optional one-time supporter tips (Bronze, Silver, Gold, and Koyomi badges) unlock commemorative supporter badges in the app settings.</p>
        </section>
    </main>

    <footer><div class="footer-inner"><span>© 2026 Takuma Yamada</span><span><a href="privacy.html">Privacy</a> · <a href="support.html">Support</a> · <a href="https://github.com/tyamada/MihirakiPDFViewer_Android">GitHub</a></span></div></footer>
</body>
</html>
"""
    (docs_dir / "index.html").write_text(index_html, encoding="utf-8")
    print("Updated docs/index.html")

    # 4. Update index_ja.html (Japanese)
    index_ja_html = """<!doctype html>
<html lang="ja">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="見開きPDFビューアは、漫画やライトノベルなどの右綴じ・見開きドキュメントを快適に閲覧するために設計された、プライバシー重視のAndroid用ローカルPDFビューアです。">
    <title>見開きPDFビューア for Android</title>
    <link rel="icon" href="assets/app-icon.png">
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <header class="site-header">
        <nav class="nav" aria-label="Main navigation">
            <a class="brand" href="index_ja.html" aria-current="page"><img src="assets/app-icon.png" alt=""><span>見開きPDFビューア</span></a>
            <div class="nav-links"><a href="#features">主な機能</a><a href="privacy_ja.html">プライバシー</a><a href="support_ja.html">サポート</a><a href="https://github.com/tyamada/MihirakiPDFViewer_Android">GitHub</a></div>
        </nav>
    </header>

    <main>
        <section class="hero">
            <div>
                <div class="eyebrow">プライバシー重視のAndroid用PDFビューア</div>
                <h1>漫画や右綴じPDFを最も快適に読むためのビューア。</h1>
                <p class="lead">日本特有の「右綴じ」や「見開き」に完全対応。高速なローカルレンダリング、目次ツリーナビゲーション、高精度な検索ハイライトをインターネット接続なしで実現。</p>
                <div class="actions"><a class="button" href="#features">機能を見る</a><a class="button secondary" href="support_ja.html">ヘルプとサポート</a></div>
            </div>
            <img class="hero-icon" src="assets/app-icon.png" alt="見開きPDFビューア アプリアイコン">
        </section>

        <section class="section" id="features">
            <div class="eyebrow">主な機能</div>
            <h2>快適な読書体験のための設計</h2>
            <p>漫画や雑誌、資料を右綴じ・見開きでスムーズに閲覧できます。</p>
            <div class="grid">
                <article class="card"><div class="symbol">📖</div><h3>見開き表示</h3><p>2つのページを横に並べてシームレスに表示。表紙設定にも対応。</p></article>
                <article class="card"><div class="symbol">🇯🇵</div><h3>右綴じ（R2L）完全対応</h3><p>日本の書籍特有の右から左への読書順序や製本レイアウトをネイティブサポート。</p></article>
                <article class="card"><div class="symbol">📑</div><h3>目次ツリーナビゲーション</h3><p>PDFに埋め込まれた目次やブックマークを自動抽出し、ワンタップで章移動。</p></article>
                <article class="card"><div class="symbol">🔍</div><h3>高精度テキスト検索</h3><p>黄色背景と赤枠による精密なハイライト付きで検索結果を瞬時に表示。</p></article>
                <article class="card"><div class="symbol">🛡️</div><h3>プライバシー重視（ローカル完結）</h3><p>インターネット接続は不要。Storage Access Frameworkで選択したファイルのみアクセス。</p></article>
                <article class="card"><div class="symbol">📱</div><h3>アダプティブUI</h3><p>スマートフォンからタブレット、折りたたみ端末、デスクトップモードまで最適化。</p></article>
            </div>
        </section>

        <section class="section">
            <div class="eyebrow">診断 & ログ管理</div>
            <h2>端末診断テスト & アプリログ</h2>
            <p>設定画面から端末の診断テストを実行し、最大24時間端末内に安全に保存されるローカルログを確認可能。個人情報やファイル内容は一切外部送信されません。</p>
        </section>

        <section class="section">
            <div class="eyebrow">開発者の応援</div>
            <h2>開発の支援</h2>
            <p>すべての閲覧・編集機能は完全無料でご利用いただけます。お気に入りの方向けに、記念こよみバッジなどの応援チップ機能をご用意しています。</p>
        </section>
    </main>

    <footer><div class="footer-inner"><span>© 2026 Takuma Yamada</span><span><a href="privacy_ja.html">プライバシーポリシー</a> · <a href="support_ja.html">サポート</a> · <a href="https://github.com/tyamada/MihirakiPDFViewer_Android">GitHub</a></span></div></footer>
</body>
</html>
"""
    (docs_dir / "index_ja.html").write_text(index_ja_html, encoding="utf-8")
    print("Updated docs/index_ja.html")

if __name__ == "__main__":
    main()
