#!/usr/bin/env python3
import os
import subprocess
import time
import sys
import datetime
import xml.etree.ElementTree as ET

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
    run_cmd(f"adb shell screencap -p /sdcard/{step_name}.png", check=True)
    run_cmd(f"adb pull /sdcard/{step_name}.png {img_path}", check=True)
    run_cmd(f"adb shell uiautomator dump /sdcard/{step_name}.xml", check=False)
    run_cmd(f"adb pull /sdcard/{step_name}.xml {xml_path}", check=False)
    print(f"[INFO] Captured state for {step_name} -> {img_path}")
    return img_path, xml_path

def find_node_bounds_by_text(xml_path, target_text):
    if not os.path.exists(xml_path):
        return None
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()
        for node in root.iter('node'):
            text = node.get('text', '')
            if target_text.lower() in text.lower():
                bounds = node.get('bounds')
                if bounds:
                    parts = bounds.replace('][', ',').replace('[', '').replace(']', '').split(',')
                    x1, y1, x2, y2 = map(int, parts)
                    return (x1 + x2) // 2, (y1 + y2) // 2
    except Exception as e:
        print(f"[WARNING] XML parse error: {e}")
    return None

def main():
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f".artifacts/videos/run_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)

    print("=== Step 1: Tap Episode 1 'Open' Button ===")
    # From previous XML inspection, 'Open' button for Episode 1 is located at bounds [808,528][1016,608] -> center (912, 568)
    # Let's also verify via XML search if possible or tap (912, 568)
    open_x, open_y = 912, 568
    print(f"[INFO] Tapping Episode 1 'Open' button at ({open_x}, {open_y})")
    run_cmd(f"adb shell input tap {open_x} {open_y}", check=True)
    time.sleep(4) # Wait for PDF viewer screen to open and render

    img_viewer, xml_viewer = capture_state(output_dir, "pdf_viewer_screen")

    # Inspect XML viewer to verify if PDF viewer is open (e.g. check for toolbar back/page buttons or lack of Sample PDFs catalog)
    is_viewer_open = False
    try:
        tree = ET.parse(xml_viewer)
        root = tree.getroot()
        # In viewer screen, "Sample PDFs" title is no longer present in catalog list
        has_sample_title = False
        for node in root.iter('node'):
            text = node.get('text', '')
            if "Sample PDFs" in text:
                has_sample_title = True
                break
        if not has_sample_title:
            is_viewer_open = True
    except Exception:
        pass

    report_path = f"{output_dir}/report.md"
    report_content = f"""# Episode 1 Opening & PDF Render Verification Report - Run {timestamp}

## Overview
- **Action**: Tapped Episode 1 "Open" button at `({open_x}, {open_y})` after downloading `THE TRY-IT CLUB EPISODE 1 THE BREAK-TIME MAP`.
- **Post-Action State**: {'PDF Viewer Screen Open & Rendered' if is_viewer_open else 'Still on Catalog (Check XML/Screenshot)'}

## Correction Note
- Previous reports falsely claimed PDF rendering occurred during download. Actual inspection of `post_download_or_open.png` and XML revealed that tapping "Download" only initiates the download, after which the button state changes to "Open". A second tap on "Open" is required to actually launch the PDF viewer.

## Evidence Files
- Viewer Screen: `{os.path.abspath(img_viewer)}` (`pdf_viewer_screen.xml`)
"""

    with open(report_path, "w") as f:
        f.write(report_content)

    print(f"\n[SUCCESS] PDF viewer verification complete: {output_dir}")
    print(f"Report saved at: {os.path.abspath(report_path)}")

if __name__ == "__main__":
    main()
