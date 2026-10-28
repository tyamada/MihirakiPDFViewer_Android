#!/usr/bin/env python3
import os
import json

def main():
    package_dir = "shorts_output/chatgpt_upload_package"
    os.makedirs(package_dir, exist_ok=True)

    video_path = "shorts_output/mihiraki_shorts_en.mp4"
    if not os.path.exists(video_path):
        print(f"[ERROR] Video not found at {video_path}. Please run build_english_shorts.py first.")
        return

    metadata = {
        "title": "Mihiraki PDF Viewer - Perfect Right-to-Left & Two-Page Spread Reader #shorts",
        "description": (
            "Struggling to read manga or right-to-left PDFs on Android?\n"
            "Meet Mihiraki PDF Viewer — the privacy-first, local PDF reader designed specifically "
            "for right-to-left documents and two-page spreads!\n\n"
            "Key Features:\n"
            "- 📖 Seamless Two-Page Spread & Right-to-Left (R2L) Support\n"
            "- 📑 Outline Tree Navigation for instant chapter jumps\n"
            "- 🔍 Fast Text Search with precise highlights\n"
            "- 🛡️ 100% Local & Private (No internet required)\n\n"
            "Download on Google Play:\n"
            "https://play.google.com/store/apps/details?id=com.github.tyamada.MihirakiPDFViewer_Android\n\n"
            "#shorts #pdfviewer #manga #android"
        ),
        "tags": ["pdf", "reader", "manga", "right-to-left", "android", "mihiraki", "pdfviewer", "shorts"],
        "video_file": os.path.abspath(video_path)
    }

    meta_path = os.path.join(package_dir, "metadata.json")
    with open(meta_path, "w") as f:
        json.dump(metadata, f, indent=2)

    # Generate a Playwright browser automation script template for YouTube Studio upload
    playwright_script_content = f'''#!/usr/bin/env python3
"""
Playwright Automation Script for YouTube Studio Upload
To run:
    pip install playwright
    playwright install chromium
    python3 upload_via_playwright.py
"""
import os
from playwright.sync_api import sync_playwright

def main():
    video_path = "{os.path.abspath(video_path)}"
    title = "{metadata['title']}"
    description = {repr(metadata['description'])}

    print("Starting browser for YouTube Studio upload...")
    print("If prompted, please log into your Google account in the opened browser window.")

    with sync_playwright() as p:
        # Launch browser with persistent context so login session is saved
        user_data_dir = os.path.expanduser("~/.playwright_youtube_profile")
        browser = p.chromium.launch_persistent_context(
            user_data_dir=user_data_dir,
            headless=False,
            args=["--start-maximized"]
        )
        page = browser.new_page()
        page.goto("https://studio.youtube.com/")

        print("\\nPlease complete the upload manually in the browser window if needed, or follow instructions.")
        print(f"Video file to upload: {{video_path}}")
        print(f"Title: {{title}}")

        # Keep browser open for user interaction
        input("Press Enter here after you finish uploading the video in the browser...")
        browser.close()

if __name__ == "__main__":
    main()
'''
    playwright_script_path = os.path.join(package_dir, "upload_via_playwright.py")
    with open(playwright_script_path, "w") as f:
        f.write(playwright_script_content)

    # Generate ChatGPT Prompt Markdown
    chatgpt_prompt_md = f"""# ChatGPT Upload Package & Instructions

Please share this folder (`shorts_output/chatgpt_upload_package/`) or prompt ChatGPT with the following:

---
**Prompt for ChatGPT:**

I have created an English YouTube Short video introducing our Android app "Mihiraki PDF Viewer".
Here are the details and assets in this package:

- **Video File:** `{os.path.abspath(video_path)}`
- **Title:** `{metadata["title"]}`
- **Description:**
```text
{metadata["description"]}
```
- **Tags:** {", ".join(metadata["tags"])}

Please review this package. If you can assist with browser automation or guide me through the upload process using the included `upload_via_playwright.py` script, please help me execute or refine it!
---
"""
    prompt_path = os.path.join(package_dir, "chatgpt_prompt.md")
    with open(prompt_path, "w") as f:
        f.write(chatgpt_prompt_md)

    print(f"[SUCCESS] ChatGPT upload package prepared successfully in: {os.path.abspath(package_dir)}")
    print(f" - Metadata: {meta_path}")
    print(f" - Playwright Script: {playwright_script_path}")
    print(f" - ChatGPT Prompt: {prompt_path}")

if __name__ == "__main__":
    main()
