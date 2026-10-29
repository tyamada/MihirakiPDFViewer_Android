#!/usr/bin/env python3
import os
import subprocess
import time
import sys
import xml.etree.ElementTree as ET

def run_cmd(cmd, check=True):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
        if check:
            sys.exit(res.returncode)
    return res.stdout

def get_node_center_by_resource_id_or_text(xml_path, keyword):
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()
        for node in root.iter('node'):
            text = node.get('text', '')
            res_id = node.get('resource-id', '')
            content_desc = node.get('content-desc', '')
            if keyword.lower() in text.lower() or keyword.lower() in res_id.lower() or keyword.lower() in content_desc.lower():
                bounds = node.get('bounds') # format: [x1,y1][x2,y2]
                if bounds:
                    parts = bounds.replace('][', ',').replace('[', '').replace(']', '').split(',')
                    x1, y1, x2, y2 = map(int, parts)
                    cx = (x1 + x2) // 2
                    cy = (y1 + y2) // 2
                    return cx, cy
    except Exception as e:
        print(f"[WARNING] Failed to parse UI xml: {e}")
    return None

def main():
    os.makedirs("shorts_work", exist_ok=True)
    timestamp = datetime_str = subprocess.run("date +%Y%m%d_%H%M%S", shell=True, capture_output=True, text=True).stdout.strip()
    output_dir = f".artifacts/videos/run_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)

    print("=== Step 1: Launch App & Set Locale ==/")
    run_cmd("adb shell settings put secure system_locales en-US", check=True)
    launch_cmd = "adb shell am start -n com.github.tyamada.MihirakiPDFViewer_Android/com.github.tyamada.mihirakipdfviewer_android.MainActivity"
    run_cmd(launch_cmd, check=True)
    time.sleep(3)

    print("=== Step 2: Start Screen Recording ===")
    remote_video = "/sdcard/video_1.mp4"
    raw_local = "shorts_work/video_1_raw.mp4"
    # Record for 30 seconds
    record_proc = subprocess.Popen(f"adb shell screenrecord --time-limit 30 {remote_video}", shell=True)
    time.sleep(2) # Buffer

    print("=== Step 3: Executing Actions A-F ===")
    # A & B: Inspect UI and tap Sample PDFs button
    run_cmd("adb shell uiautomator dump /sdcard/window_dump.xml", check=False)
    run_cmd("adb pull /sdcard/window_dump.xml shorts_work/window_dump.xml", check=False)

    sample_coords = get_node_center_by_resource_id_or_text("shorts_work/window_dump.xml", "sample")
    if sample_coords:
        sx, sy = sample_coords
        print(f"[INFO] Found Sample PDFs at ({sx}, {sy})")
        run_cmd(f"adb shell input tap {sx} {sy}", check=True)
    else:
        print("[WARNING] Sample PDFs button not found via XML, falling back to toolbar coordinate (800, 130)")
        run_cmd("adb shell input tap 800 130", check=True)
    time.sleep(3)

    # C: Select sample PDF item (e.g. Episode 1)
    run_cmd("adb shell uiautomator dump /sdcard/window_dump.xml", check=False)
    run_cmd("adb pull /sdcard/window_dump.xml shorts_work/window_dump.xml", check=False)
    item_coords = get_node_center_by_resource_id_or_text("shorts_work/window_dump.xml", "episode")
    if item_coords:
        ix, iy = item_coords
        print(f"[INFO] Found Episode item at ({ix}, {iy})")
        run_cmd(f"adb shell input tap {ix} {iy}", check=True)
    else:
        print("[WARNING] Episode item not found, tapping default list position (500, 400)")
        run_cmd("adb shell input tap 500 400", check=True)
    time.sleep(4)

    # D & E: PDF content rendered and page turning
    print("[INFO] D: PDF rendered. E: Page turning...")
    for i in range(3):
        run_cmd("adb shell input swipe 800 1000 200 1000 300", check=True)
        time.sleep(2.5)

    # Stop recording gracefully
    record_proc.wait(timeout=10)
    time.sleep(1)

    # Pull raw video
    run_cmd(f"adb pull {remote_video} {raw_local}", check=True)
    if not os.path.exists(raw_local) or os.path.getsize(raw_local) == 0:
        print("[ERROR] Raw video recording failed or is empty.")
        sys.exit(1)

    print(f"[SUCCESS] Video 1 raw recording successfully saved to {raw_local}")
    print(f"OUTPUT_DIR={output_dir}")
    print(f"RAW_VIDEO={raw_local}")

if __name__ == "__main__":
    main()
