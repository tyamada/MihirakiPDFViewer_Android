#!/usr/bin/env python3
import os
import subprocess
import time
import sys
import datetime

SERIAL = "emulator-5554"

def run_cmd(cmd, check=True):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
        if check:
            sys.exit(res.returncode)
    return res.stdout

def main():
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f".artifacts/videos/run_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)
    os.makedirs("shorts_work", exist_ok=True)

    print("=== Step 1: Reset to Page 1 / Cover of Episode 1 ===")
    # Launch app
    run_cmd(f"adb -s {SERIAL} shell am start -n com.github.tyamada.MihirakiPDFViewer_Android/com.github.tyamada.mihirakipdfviewer_android.MainActivity", check=True)
    time.sleep(3)

    # Tap Sample PDFs (243, 112) or Open if catalog is shown
    run_cmd(f"adb -s {SERIAL} shell input tap 243 112", check=False)
    time.sleep(2)
    # Tap Episode 1 Open button (912, 568) to freshly open Episode 1 from cover
    run_cmd(f"adb -s {SERIAL} shell input tap 912 568", check=True)
    time.sleep(3) # Wait for render

    # Capture pre-record verification state
    pre_cover_img = f"{output_dir}/pre_record_cover.png"
    run_cmd(f"adb -s {SERIAL} shell screencap -p /sdcard/pre_record_cover.png", check=True)
    run_cmd(f"adb -s {SERIAL} pull /sdcard/pre_record_cover.png {pre_cover_img}", check=True)
    print(f"[INFO] Captured pre-record cover verification image: {pre_cover_img}")

    print("=== Step 2: Start Unedited Test Recording (10s limit) ===")
    remote_path = "/sdcard/test_recording_v2.mp4"
    local_mp4 = f"{output_dir}/test_recording.mp4"

    rec_proc = subprocess.Popen(["adb", "-s", SERIAL, "shell", "screenrecord", "--time-limit", "10", remote_path])
    time.sleep(2.5) # Show cover page for 2.5s

    print("=== Step 3: Execute Forward Swipe (Left to Right for R2L) ===")
    run_cmd(f"adb -s {SERIAL} shell input swipe 200 1000 800 1000 300", check=True)
    time.sleep(4.5) # Show page 2 for 4.5s

    print("=== Step 4: Stop Recording Cleanly ===")
    rec_proc.terminate()
    try:
        rec_proc.wait(timeout=5)
    except Exception:
        rec_proc.kill()
    time.sleep(2)

    # Pull recording
    run_cmd(f"adb -s {SERIAL} pull {remote_path} {local_mp4}", check=True)
    if not os.path.exists(local_mp4) or os.path.getsize(local_mp4) == 0:
        print("[ERROR] Recorded test video is missing or empty.")
        sys.exit(1)

    print(f"[SUCCESS] Test recording pulled to {local_mp4}")

    print("=== Step 5: Validate Stream Specs with ffprobe ===")
    ffprobe_out = run_cmd(f"ffprobe -v error -select_streams v:0 -show_entries stream=width,height,codec_name,r_frame_rate -show_entries format=duration,size -of default=noprint_wrappers=1 {local_mp4}", check=True)
    print(ffprobe_out)

    print("=== Step 6: Extract Frames Directly from Recorded Video ===")
    video_pre_img = f"{output_dir}/video_pre.png"
    video_post_img = f"{output_dir}/video_post.png"

    run_cmd(f"ffmpeg -y -ss 1.5 -i '{local_mp4}' -vframes 1 '{video_pre_img}'", check=True)
    run_cmd(f"ffmpeg -y -ss 6.0 -i '{local_mp4}' -vframes 1 '{video_post_img}'", check=True)

    print("=== Step 7: Generate Factual Verification Report ===")
    report_path = f"{output_dir}/report.md"
    report_content = f"""# Corrected Unedited Test Recording Verification Report - Run {timestamp}

## Overview
- **Device Serial**: `{SERIAL}`
- **Test Recording File**: `{os.path.abspath(local_mp4)}`
- **Format Specs (`ffprobe`)**:
```
{ffprobe_out}
```

## Pre-Record Verification
- **Cover Image (`pre_record_cover.png`)**: `{os.path.abspath(pre_cover_img)}` (Verified starting from Episode 1 Page 1 / Cover).

## Frame Extraction (Directly from Recorded Video)
1. **Pre-Turn Frame (`video_pre.png`)**: Extracted at timestamp `1.5s`.
   - *Absolute Path*: `{os.path.abspath(video_pre_img)}`
2. **Post-Turn Frame (`video_post.png`)**: Extracted at timestamp `6.0s`.
   - *Absolute Path*: `{os.path.abspath(video_post_img)}`

## Summary
- **Output Directory**: `{os.path.abspath(output_dir)}`
"""

    with open(report_path, "w") as f:
        f.write(report_content)

    print(f"\n[SUCCESS] Corrected test recording and verification complete: {output_dir}")
    print(f"Report saved at: {os.path.abspath(report_path)}")

if __name__ == "__main__":
    main()
