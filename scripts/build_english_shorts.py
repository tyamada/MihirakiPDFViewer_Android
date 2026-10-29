#!/usr/bin/env python3
import os
import subprocess
import sys

def run_cmd(cmd):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
        sys.exit(res.returncode)
    return res.stdout

def build_short_video(video_id, title_text, narration_text, image_path, output_path):
    os.makedirs("shorts_work", exist_ok=True)
    audio_aiff = f"shorts_work/{video_id}.aiff"
    audio_wav = f"shorts_work/{video_id}.wav"
    video_mp4 = f"shorts_work/{video_id}.mp4"

    if not os.path.exists(image_path):
        print(f"[ERROR] Image not found: {image_path}")
        sys.exit(1)

    # 1. Generate speech audio with macOS 'say' (Samantha - US English, no BGM/SFX)
    run_cmd(f"say -v Samantha -o '{audio_aiff}' '{narration_text}'")
    run_cmd(f"ffmpeg -y -i '{audio_aiff}' -ar 44100 -ac 2 '{audio_wav}'")

    # Get audio duration
    probe_out = run_cmd(f"ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 '{audio_wav}'")
    duration = float(probe_out.strip())
    duration = max(duration + 0.8, 3.0)

    # 2. Generate video clip from image with speech audio
    filter_complex = (
        f"[0:v]scale=1080:1920:force_original_aspect_ratio=decrease,"
        f"pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black[v]"
    )
    cmd_vid = (
        f"ffmpeg -y -loop 1 -i '{image_path}' -i '{audio_wav}' "
        f"-filter_complex \"{filter_complex}\" "
        f"-map '[v]' -map 1:a -c:v libx264 -tune stillimage -c:a aac -b:a 192k "
        f"-pix_fmt yuv420p -t {duration} '{output_path}'"
    )
    run_cmd(cmd_vid)
    print(f"[SUCCESS] Created short video: {output_path}")

def main():
    os.makedirs("shorts_output", exist_ok=True)
    os.makedirs(".artifacts/videos", exist_ok=True)

    shorts_specs = [
        {
            "id": "short_1_getting_started",
            "title": "Getting Started & Opening PDFs",
            "narration": "Welcome to Mihiraki PDF Viewer! Getting started is easy. Open any PDF from your device storage or cloud, try our sample PDFs, and navigate pages smoothly by swiping, using the slider, or clicking screen edges.",
            "image": "store_listing/screenshots/Smartphone_6.3/en_01_reader_portrait.png"
        },
        {
            "id": "short_2_reading_features",
            "title": "Two-Page Spread & R2L Reading",
            "narration": "Designed for right-to-left documents and manga. Mihiraki PDF Viewer automatically detects two-page spreads, supports natural right-to-left reading order, and lets you jump instantly to chapters using outline tree navigation.",
            "image": "store_listing/screenshots/Smartphone_6.3/en_02_spread_landscape.png"
        },
        {
            "id": "short_3_search_highlight",
            "title": "Fast Search & Highlighting",
            "narration": "Need to find specific content? Use fast text search to find keywords instantly, highlighted precisely with striking yellow background and red border frames.",
            "image": "store_listing/screenshots/Smartphone_6.3/en_07_outline_portrait.png"
        },
        {
            "id": "short_4_settings_support",
            "title": "Settings, Customization & Support",
            "narration": "Customize your reading experience with high quality rendering, sharpness, cover settings, and mouse navigation. Support further development and view your supporter badges on Google Play!",
            "image": "store_listing/screenshots/Smartphone_6.3/en_06_display_settings_portrait.png"
        }
    ]

    for spec in shorts_specs:
        out_path = f"shorts_output/{spec['id']}.mp4"
        artifact_path = f".artifacts/videos/{spec['id']}.mp4"
        build_short_video(spec['id'], spec['title'], spec['narration'], spec['image'], out_path)
        # Copy to artifacts for review
        run_cmd(f"cp '{out_path}' '{artifact_path}'")

    print("\n[SUCCESS] All 4 English introduction short videos successfully generated and copied to .artifacts/videos/ for developer review!")

if __name__ == "__main__":
    main()
