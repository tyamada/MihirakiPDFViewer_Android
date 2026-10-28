#!/usr/bin/env python3
import os
import json

def main():
    package_dir = "shorts_output/gemini_upload_package"
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

    prompt_md = f"""# Gemini App Upload Prompt & Instructions

Please use the following prompt and the attached video file to upload this Short to YouTube:

---
**Prompt for Gemini App:**

Please upload the following YouTube Short video and set the metadata accordingly:

- **Video File:** `{os.path.abspath(video_path)}`
- **Title:** `{metadata["title"]}`
- **Description:**
```text
{metadata["description"]}
```
- **Tags:** {", ".join(metadata["tags"])}
- **Visibility:** Public (or Unlisted as preferred)
---
"""
    prompt_path = os.path.join(package_dir, "gemini_prompt.md")
    with open(prompt_path, "w") as f:
        f.write(prompt_md)

    print(f"[SUCCESS] Gemini upload package prepared successfully in: {os.path.abspath(package_dir)}")
    print(f" - Metadata: {meta_path}")
    print(f" - Prompt Instructions: {prompt_path}")

if __name__ == "__main__":
    main()
