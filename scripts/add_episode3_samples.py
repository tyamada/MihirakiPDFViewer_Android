#!/usr/bin/env python3
import pathlib
import shutil

def main():
    root = pathlib.Path(__file__).resolve().parent.parent
    testdata_dir = root / "testdata"
    sample_dir = root / "docs" / "sample"
    sample_dir.mkdir(parents=True, exist_ok=True)

    files_to_copy = [
        "tameshibu_episode3_ko.pdf",
        "tameshibu_episode3_zh_cn.pdf",
        "tameshibu_episode3_zh_tw.pdf"
    ]

    for fname in files_to_copy:
        src = testdata_dir / fname
        dest = sample_dir / fname
        if src.exists():
            shutil.copy2(src, dest)
            print(f"Copied {fname} to docs/sample/")
        else:
            print(f"Warning: {src} not found.")

    # Update SamplePdfCatalog.kt
    catalog_path = root / "app" / "src" / "main" / "java/com/github/tyamada/mihirakipdfviewer_android/data/SamplePdfCatalog.kt"
    if catalog_path.exists():
        content = catalog_path.read_text(encoding="utf-8")
        if "ep3_ko" not in content:
            # Insert ep3_ko, ep3_zh_cn, ep3_zh_tw before the end of items list
            new_items = """        SamplePdfItem(
            id = "ep3_ko",
            title = "해봄부 제3화 출장, 해봄부!",
            fileName = "tameshibu_episode3_ko.pdf",
            url = "${BASE_URL}tameshibu_episode3_ko.pdf",
            fileSize = "22MB",
            languageCode = "ko",
            languageDisplayName = "Korean"
        ),
        SamplePdfItem(
            id = "ep3_zh_cn",
            title = "试试社 第3话 出差，试试社！",
            fileName = "tameshibu_episode3_zh_cn.pdf",
            url = "${BASE_URL}tameshibu_episode3_zh_cn.pdf",
            fileSize = "22MB",
            languageCode = "zh_cn",
            languageDisplayName = "Chinese (Simplified)"
        ),
        SamplePdfItem(
            id = "ep3_zh_tw",
            title = "試試社 第3話",
            fileName = "tameshibu_episode3_zh_tw.pdf",
            url = "${BASE_URL}tameshibu_episode3_zh_tw.pdf",
            fileSize = "22MB",
            languageCode = "zh_tw",
            languageDisplayName = "Chinese (Traditional)"
        ),
"""
            # Find where ep2_ko or ep2_zh_cn is or right before closing list
            target = 'SamplePdfItem(\n            id = "ep1_ko",'
            if target in content:
                # Insert near Korean section
                content = content.replace(target, new_items + target)
                catalog_path.write_text(content, encoding="utf-8")
                print("Updated SamplePdfCatalog.kt with Episode 3 items.")

    print("[SUCCESS] Episode 3 sample PDFs added successfully!")

if __name__ == "__main__":
    main()
