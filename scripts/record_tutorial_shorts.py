#!/usr/bin/env python3
import os
import subprocess
import time
import sys

def run_cmd(cmd):
    print(f"[RUN] {cmd}")
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed: {cmd}\nStdout: {res.stdout}\nStderr: {res.stderr}")
    return res.stdout

def record_workflow(workflow_name, duration_secs, actions_func):
    print(f"\n=== Starting Recording: {workflow_name} ===")
    remote_path = f"/sdcard/{workflow_name}.mp4"
    local_path = f"shorts_work/{workflow_name}_raw.mp4"
    os.makedirs("shorts_work", exist_ok=True)

    # Start screenrecord in background
    print(f"[INFO] Starting adb screenrecord for {duration_secs}s...")
    proc = subprocess.Popen(f"adb shell screenrecord --time-limit {duration_secs} {remote_path}", shell=True)
    time.sleep(2) # Buffer to start

    # Execute app actions
    try:
        actions_func()
    except Exception as e:
        print(f"[WARNING] Action error: {e}")

    # Wait for recording to complete
    proc.wait(timeout=duration_secs + 5)
    time.sleep(1)

    # Pull recording
    run_cmd(f"adb pull {remote_path} {local_path}")
    print(f"[SUCCESS] Recorded raw video saved to {local_path}")
    return local_path

def action_getting_started():
    # Launch app
    run_cmd("adb shell am start -n com.github.tyamada.MihirakiPDFViewer_Android/com.github.tyamada.mihirakipdfviewer_android.MainActivity")
    time.sleep(3)
    run_cmd("adb shell input tap 900 130") # Sample PDFs icon
    time.sleep(2)
    run_cmd("adb shell input tap 500 400")
    time.sleep(4)
    for _ in range(3):
        run_cmd("adb shell input swipe 800 1000 200 1000 300")
        time.sleep(2)

def action_spreading_r2l():
    run_cmd("adb shell am start -n com.github.tyamada.MihirakiPDFViewer_Android/com.github.tyamada.mihirakipdfviewer_android.MainActivity")
    time.sleep(2)
    run_cmd("adb shell input tap 990 130") # Settings icon
    time.sleep(2)
    run_cmd("adb shell input tap 900 320") # Two-page view switch
    time.sleep(1)
    run_cmd("adb shell input tap 500 450") # Reading direction
    time.sleep(1)
    run_cmd("adb shell input tap 80 130") # Back icon
    time.sleep(2)
    for _ in range(3):
        run_cmd("adb shell input swipe 200 1000 800 1000 300") # R2L swipe
        time.sleep(2)

def action_search_highlight():
    run_cmd("adb shell am start -n com.github.tyamada.MihirakiPDFViewer_Android/com.github.tyamada.mihirakipdfviewer_android.MainActivity")
    time.sleep(2)
    run_cmd("adb shell input tap 800 130") # Search icon
    time.sleep(2)
    run_cmd("adb shell input text club")
    time.sleep(2)
    run_cmd("adb shell input keyevent 66") # ENTER
    time.sleep(3)
    run_cmd("adb shell input tap 950 130")
    time.sleep(2)

def action_settings_support():
    run_cmd("adb shell am start -n com.github.tyamada.MihirakiPDFViewer_Android/com.github.tyamada.mihirakipdfviewer_android.MainActivity")
    time.sleep(2)
    run_cmd("adb shell input tap 990 130") # Menu dots
    time.sleep(1.5)
    run_cmd("adb shell input tap 500 380") # Support option
    time.sleep(3)

def main():
    os.makedirs("shorts_work", exist_ok=True)
    record_workflow("short_1_getting_started", 25, action_getting_started)
    record_workflow("short_2_reading_features", 25, action_spreading_r2l)
    record_workflow("short_3_search_highlight", 25, action_search_highlight)
    record_workflow("short_4_settings_support", 25, action_settings_support)
    print("\n[SUCCESS] All 4 workflows recorded successfully!")

if __name__ == "__main__":
    main()
