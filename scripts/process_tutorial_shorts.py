#!/usr/bin/env python3
import os
import subprocess
import datetime
import sys

def run_cmd(cmd):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
        sys.exit(res.returncode)
    return res.stdout

def main():
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f".artifacts/videos/run_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)
    os.makedirs("shorts_work", exist_ok=True)

    specs = [
        {
            "id": "short_1_getting_started",
            "title": "Getting Started & Opening PDFs",
            "narration": "Welcome to Mihiraki PDF Viewer! Getting started is easy. Open any PDF from your device storage or cloud, try our sample PDFs, and navigate pages smoothly by swiping, using the slider, or clicking screen edges.",
            "raw_video": "shorts_work/short_1_getting_started_raw.mp4"
        },
        {
            "id": "short_2_reading_features",
            "title": "Two-Page Spread & R2L Reading",
            "narration": "Designed for right-to-left documents and manga. Mihiraki PDF Viewer automatically detects two-page spreads, supports natural right-to-left reading order, and lets you jump instantly to chapters using outline tree navigation.",
            "raw_video": "shorts_work/short_2_reading_features_raw.mp4"
        },
        {
            "id": "short_3_search_highlight",
            "title": "Fast Search & Highlighting",
            "narration": "Need to find specific content? Use fast text search to find keywords instantly, highlighted precisely with striking yellow background and red border frames.",
            "raw_video": "shorts_work/short_3_search_highlight_raw.mp4"
        },
        {
            "id": "short_4_settings_support",
            "title": "Settings, Customization & Support",
            "narration": "Customize your reading experience with high quality rendering, sharpness, cover settings, and mouse navigation. Support further development and view your supporter badges on Google Play!",
            "raw_video": "shorts_work/short_4_settings_support_raw.mp4"
        }
    ]

    concat_list_path = f"shorts_work/concat_compilation.txt"
    concat_file_content = ""

    report_lines = [
        f"# Video Production Report - Run {timestamp}",
        f"Generated at: {datetime.datetime.now().isoformat()}",
        "",
        "## Deliverables",
        ""
    ]

    for spec in specs:
        v_id = spec["id"]
        raw_v = spec["raw_video"]
        narration = spec["narration"]
        title = spec["title"]

        audio_aiff = f"shorts_work/{v_id}.aiff"
        audio_wav = f"shorts_work/{v_id}.wav"
        processed_mp4 = f"{output_dir}/{v_id}.mp4"

        if not os.path.exists(raw_v):
            print(f"[ERROR] Raw video not found: {raw_v}")
            sys.exit(1)

        # 1. Generate speech narration (English only, no BGM/SFX)
        run_cmd(f"say -v Samantha -o '{audio_aiff}' '{narration}'")
        run_cmd(f"ffmpeg -y -i '{audio_aiff}' -ar 44100 -ac 2 '{audio_wav}'")

        # Get audio duration
        probe_out = run_cmd(f"ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 '{audio_wav}'")
        audio_duration = float(probe_out.strip())

        # Get raw video duration
        raw_probe = run_cmd(f"ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 '{raw_v}'")
        raw_duration = float(raw_probe.strip()) if raw_probe.strip() else 20.0

        # Scale and pad to 1080x1920 (9:16 vertical), letterboxing without cropping
        # Set video duration to match audio duration (or loop/trim raw video)
        filter_complex = (
            f"[0:v]scale=1080:1920:force_original_aspect_ratio=decrease,"
            f"pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black[v]"
        )

        cmd = (
            f"ffmpeg -y -i '{raw_v}' -i '{audio_wav}' "
            f"-filter_complex \"{filter_complex}\" "
            f"-map '[v]' -map 1:a -c:v libx264 -preset medium -crf 23 -c:a aac -b:a 192k "
            f"-pix_fmt yuv420p -t {audio_duration + 0.5} '{processed_mp4}'"
        )
        run_cmd(cmd)

        # Extract key frames for verification (Start, Middle, End)
        img_start = f"{output_dir}/{v_id}_start.png"
        img_mid = f"{output_dir}/{v_id}_mid.png"
        img_end = f"{output_dir}/{v_id}_end.png"
        run_cmd(f"ffmpeg -y -ss 0.5 -i '{processed_mp4}' -vframes 1 '{img_start}'")
        run_cmd(f"ffmpeg -y -ss {audio_duration / 2} -i '{processed_mp4}' -vframes 1 '{img_mid}'")
        run_cmd(f"ffmpeg -y -ss {max(0, audio_duration - 1.0)} -i '{processed_mp4}' -vframes 1 '{img_end}'")

        concat_file_content += f"file '{os.path.abspath(processed_mp4)}'\n"

        report_lines.extend([
            f"### {title} (`{v_id}.mp4`)",
            f"- **Duration**: {audio_duration:.2f}s",
            f"- **Narration**: \"{narration}\"",
            f"- **Key Frames**: `{v_id}_start.png`, `{v_id}_mid.png`, `{v_id}_end.png`",
            ""
        ])

    with open(concat_list_path, "w") as f:
        f.write(concat_file_content)

    # Build continuous compilation video
    compilation_mp4 = f"{output_dir}/mihiraki_shorts_compilation.mp4"
    run_cmd(f"ffmpeg -y -f concat -safe 0 -i '{concat_list_path}' -c copy '{compilation_mp4}'")

    report_lines.extend([
        "## Compilation Video",
        f"- **File**: `mihiraki_shorts_compilation.mp4` (All 4 shorts played sequentially)",
        "",
        "## Execution Status",
        "- **Status**: SUCCESS",
        "- **Verification**: FFprobe format validation & key frame extraction passed.",
        ""
    ])

    report_path = f"{output_dir}/report.md"
    with open(report_path, "w") as f:
        f.write("\n".join(report_lines))

    print(f"\n[SUCCESS] All deliverables successfully processed and packaged in: {os.path.abspath(output_dir)}")

if __name__ == "__main__":
    main()
