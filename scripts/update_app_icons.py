#!/usr/bin/env python3
import os
import pathlib
from PIL import Image

def main():
    root = pathlib.Path(__file__).resolve().parent.parent
    source_icon = root / "assets" / "icon" / "MihirakiPDFViewer-AppIcon-MangaBookPose.png"
    source_512 = root / "assets" / "icon" / "MihirakiPDFViewer-AppIcon-MangaBookPose-512.png"

    if not source_icon.exists():
        print(f"Source icon not found at {source_icon}")
        return

    img = Image.open(source_icon).convert("RGBA")

    # Mipmap densities and sizes
    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }

    res_dir = root / "app" / "src" / "main" / "res"

    for folder_name, size in sizes.items():
        folder_path = res_dir / folder_name
        folder_path.mkdir(parents=True, exist_ok=True)

        resized = img.resize((size, size), Image.Resampling.LANCZOS)

        # Save ic_launcher.png
        launcher_path = folder_path / "ic_launcher.png"
        resized.save(launcher_path, "PNG")

        # Save ic_launcher_round.png
        round_path = folder_path / "ic_launcher_round.png"
        resized.save(round_path, "PNG")
        print(f"Updated {folder_name} ({size}x{size})")

    # Update Play Store 512x512 icon
    play_icon_dest = root / "store_listing" / "app-icon" / "GooglePlay-AppIcon-512.png"
    play_icon_dest.parent.mkdir(parents=True, exist_ok=True)
    if source_512.exists():
        img_512 = Image.open(source_512).convert("RGBA")
    else:
        img_512 = img.resize((512, 512), Image.Resampling.LANCZOS)
    img_512.save(play_icon_dest, "PNG")
    print(f"Updated Play Store icon at {play_icon_dest}")

    print("[SUCCESS] App icons successfully updated across all densities and Play Store listing!")

if __name__ == "__main__":
    main()
