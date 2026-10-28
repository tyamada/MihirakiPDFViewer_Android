#!/usr/bin/env python3
import pathlib
import re

def main():
    root = pathlib.Path(__file__).resolve().parent.parent
    release_notes_dir = root / "store_listing" / "release-notes"
    changelog_dir = root / "store_listing" / "changelog"
    changelog_dir.mkdir(parents=True, exist_ok=True)

    if not release_notes_dir.exists():
        print("release-notes directory not found.")
        return

    mapping = {
        "RELEASE_NOTES_v1.0.0.md": "RELEASE_NOTES_v1.0.0_20.md",
        "RELEASE_NOTES_v1.2.3.md": "RELEASE_NOTES_v1.2.3_17.md",
        "RELEASE_NOTES_v1.2.4.md": "RELEASE_NOTES_v1.2.4_18.md",
        "RELEASE_NOTES_v1.3.0.md": "RELEASE_NOTES_v1.3.0_21.md",
        "RELEASE_NOTES_v1.4.0.md": "RELEASE_NOTES_v1.4.0_22.md",
        "RELEASE_NOTES_v2.0.0.md": "RELEASE_NOTES_v2.0.0_24.md",
        "RELEASE_NOTES_v2.0.0_v27.md": "RELEASE_NOTES_v2.0.0_27.md",
        "RELEASE_NOTES_v2.0.0_v28.md": "RELEASE_NOTES_v2.0.0_28.md",
        "RELEASE_NOTES_v2.0.0_v29.md": "RELEASE_NOTES_v2.0.0_29.md",
        "RELEASE_NOTES_v2.0.0_v33.md": "RELEASE_NOTES_v2.0.0_33.md",
        "RELEASE_NOTES_v2.1.0_34.md": "RELEASE_NOTES_v2.1.0_34.md",
    }

    for src_name, dest_name in mapping.items():
        src_path = release_notes_dir / src_name
        if src_path.exists():
            content = src_path.read_text(encoding="utf-8")
            dest_path = changelog_dir / dest_name
            dest_path.write_text(content, encoding="utf-8")
            print(f"Migrated {src_name} -> changelog/{dest_name}")
            src_path.unlink()

    print("[SUCCESS] All old release notes migrated to store_listing/changelog/ with new naming convention!")

if __name__ == "__main__":
    main()
