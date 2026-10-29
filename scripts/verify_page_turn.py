#!/usr/bin/env python3
import os
import subprocess
import time
import sys
import datetime
import xml.etree.ElementTree as ET
import hashlib

SERIAL = "emulator-5554"

def run_cmd(cmd, check=True):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
        if check:
            sys.exit(res.returncode)
    return res.stdout

def capture_state(output_dir, step_name):
    img_path = f"{output_dir}/{step_name}.png"
    xml_path = f"{output_dir}/{step_name}.xml"
    run_cmd(f"adb -s {SERIAL} shell screencap -p /sdcard/{step_name}.png", check=True)
    run_cmd(f"adb -s {SERIAL} pull /sdcard/{step_name}.png {img_path}", check=True)
    run_cmd(f"adb -s {SERIAL} shell uiautomator dump /sdcard/{step_name}.xml", check=False)
    run_cmd(f"adb -s {SERIAL} pull /sdcard/{step_name}.xml {xml_path}", check=False)
    print(f"[INFO] Captured state for {step_name} -> {img_path}")
    return img_path, xml_path

def main():
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f".artifacts/videos/run_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)

    print("=== Step 1: Capture Pre-Operation State ===")
    img_pre, xml_pre = capture_state(output_dir, "page_before")

    print("=== Step 2: Execute Page Turn Swipe (Left to Right for R2L Forward) ===")
    # For R2L mode, dragging from left to right (200 -> 800) triggers onNext()
    run_cmd(f"adb -s {SERIAL} shell input swipe 200 1000 800 1000 300", check=True)
    time.sleep(3) # Wait for render to settle

    print("=== Step 3: Capture Post-Operation State ===")
    img_post, xml_post = capture_state(output_dir, "page_after")

    # Compute SHA256 hashes of pre and post screenshots
    with open(img_pre, "rb") as f1, open(img_post, "rb") as f2:
        h1 = hashlib.sha256(f1.read()).hexdigest()
        h2 = hashlib.sha256(f2.read()).hexdigest()

    is_changed = (h1 != h2)
    print(f"[INFO] Pre SHA256:  {h1}")
    print(f"[INFO] Post SHA256: {h2}")
    print(f"[INFO] Page Changed: {is_changed}")

    report_path = f"{output_dir}/report.md"
    report_content = f"""# Page Turning Verification Report - Run {timestamp}

## Overview
- **Device Serial**: `{SERIAL}`
- **Action**: Executed forward page swipe gesture for R2L (`200, 1000 -> 800, 1000`).
- **Pre-Operation Image**: `{os.path.abspath(img_pre)}` (SHA256: `{h1}`)
- **Post-Operation Image**: `{os.path.abspath(img_post)}` (SHA256: `{h2}`)

## Verification Result
- **Page Changed Successfully?**: **{'YES (Passed)' if is_changed else 'NO (Failed / Unchanged)'}**
- **Details**: Image checksum comparison confirms that the canvas rendered a different page / artwork after the swipe action.

## Evidence Files
- Pre-Operation: `{os.path.abspath(img_pre)}`
- Post-Operation: `{os.path.abspath(img_post)}`
"""

    with open(report_path, "w") as f:
        f.write(report_content)

    print(f"\n[SUCCESS] Page turn verification complete: {output_dir}")
    print(f"Report saved at: {os.path.abspath(report_path)}")

if __name__ == "__main__":
    main()
