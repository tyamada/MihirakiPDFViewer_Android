#!/usr/bin/env python3
import os
import subprocess
import sys
from PIL import Image, ImageDraw, ImageFont

def run_cmd(cmd):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
        sys.exit(res.returncode)
    return res.stdout

def create_ending_card(output_path="shorts_work/ending_card.png"):
    """Generate a polished ending card image with app icon, title, and description using Pillow."""
    width, height = 1080, 1920
    # Material 3 dark surface background
    bg_color = (24, 28, 36)
    img = Image.new("RGB", (width, height), color=bg_color)
    draw = ImageDraw.Draw(img)

    # Paste App Icon
    icon_path = "store_listing/app-icon/GooglePlay-AppIcon-512.png"
    if os.path.exists(icon_path):
        icon = Image.open(icon_path).convert("RGBA")
        # Resize icon to 300x300
        icon = icon.resize((300, 300), Image.Resampling.LANCZOS)
        # Position at center top
        icon_x = (width - 300) // 2
        icon_y = 500
        img.paste(icon, (icon_x, icon_y), icon)

    # Try to load a clean system font, fallback to default if not available
    try:
        title_font = ImageFont.truetype("/System/Library/Fonts/SFPro.ttc", 60)
        subtitle_font = ImageFont.truetype("/System/Library/Fonts/SFPro.ttc", 36)
        cta_font = ImageFont.truetype("/System/Library/Fonts/SFPro.ttc", 42)
    except Exception:
        try:
            title_font = ImageFont.truetype("/Library/Fonts/Arial.ttf", 60)
            subtitle_font = ImageFont.truetype("/Library/Fonts/Arial.ttf", 36)
            cta_font = ImageFont.truetype("/Library/Fonts/Arial.ttf", 42)
        except Exception:
            title_font = ImageFont.load_default()
            subtitle_font = ImageFont.load_default()
            cta_font = ImageFont.load_default()

    # Draw App Title
    title_text = "Mihiraki PDF Viewer"
    # For default font or truetype
    draw.text((width // 2, 880), title_text, fill=(255, 255, 255), font=title_font, anchor="mm")

    # Draw Subtitle / Description
    sub_text = "The Ultimate Right-to-Left\n& Two-Page Spread PDF Reader"
    draw.text((width // 2, 1020), sub_text, fill=(200, 210, 225), font=subtitle_font, anchor="mm", align="center")

    # Draw Call to Action box / text
    cta_text = "Download on Google Play Today!"
    draw.text((width // 2, 1250), cta_text, fill=(100, 210, 140), font=cta_font, anchor="mm")

    img.save(output_path)
    print(f"[INFO] Created ending card image at {output_path}")

def main():
    os.makedirs("shorts_output", exist_ok=True)
    os.makedirs("shorts_work", exist_ok=True)

    # Create custom ending card image for Scene 4
    ending_card_img = "shorts_work/ending_card.png"
    create_ending_card(ending_card_img)

    scenes = [
        {
            "id": "scene1",
            "text": "Struggling to read manga or right-to-left PDFs on your phone?",
            "image": "store_listing/screenshots/Smartphone_6.3/en_01_reader_portrait.png"
        },
        {
            "id": "scene2",
            "text": "Meet Mihiraki PDF Viewer! Enjoy seamless two-page spreads and full right-to-left support.",
            "image": "store_listing/screenshots/Smartphone_6.3/en_02_spread_landscape.png"
        },
        {
            "id": "scene3",
            "text": "Jump instantly with outline tree navigation, and find text with instant highlight.",
            "image": "store_listing/screenshots/Smartphone_6.3/en_07_outline_portrait.png"
        },
        {
            "id": "scene4",
            "text": "100 percent local and private. Download Mihiraki PDF Viewer on Google Play today!",
            "image": ending_card_img
        }
    ]

    concat_list_path = "shorts_work/concat_list.txt"
    with open(concat_list_path, "w") as f:
        for i, scene in enumerate(scenes):
            s_id = scene["id"]
            text = scene["text"]
            img = scene["image"]

            if not os.path.exists(img):
                print(f"[ERROR] Image not found: {img}")
                sys.exit(1)

            audio_aiff = f"shorts_work/{s_id}.aiff"
            audio_wav = f"shorts_work/{s_id}.wav"
            video_mp4 = f"shorts_work/{s_id}.mp4"

            # 1. Generate audio with macOS 'say' (Samantha - US English)
            run_cmd(f"say -v Samantha -o '{audio_aiff}' '{text}'")
            run_cmd(f"ffmpeg -y -i '{audio_aiff}' -ar 44100 -ac 2 '{audio_wav}'")

            # Get audio duration
            probe_out = run_cmd(f"ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 '{audio_wav}'")
            duration = float(probe_out.strip())
            duration = max(duration + 0.5, 3.0)

            # 2. Generate video clip from image
            filter_complex = (
                f"[0:v]scale=1080:1920:force_original_aspect_ratio=decrease,"
                f"pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black[v]"
            )
            cmd_vid = (
                f"ffmpeg -y -loop 1 -i '{img}' -i '{audio_wav}' "
                f"-filter_complex \"{filter_complex}\" "
                f"-map '[v]' -map 1:a -c:v libx264 -tune stillimage -c:a aac -b:a 192k "
                f"-pix_fmt yuv420p -t {duration} '{video_mp4}'"
            )
            run_cmd(cmd_vid)

            f.write(f"file '{os.path.abspath(video_mp4)}'\n")

    # 3. Concatenate all scene clips into final shorts video
    final_output = "shorts_output/mihiraki_shorts_en.mp4"
    run_cmd(f"ffmpeg -y -f concat -safe 0 -i '{concat_list_path}' -c copy '{final_output}'")

    print(f"\n[SUCCESS] Updated English YouTube Short successfully created at: {os.path.abspath(final_output)}")

if __name__ == "__main__":
    main()
