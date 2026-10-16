import os
import time
import subprocess
import json

DEVICES = [
    {"avd": "Smartphone_6.3", "dir": "Smartphone"},
    {"avd": "Tablet_7", "dir": "Tablet7"},
    {"avd": "Tablet_10", "dir": "Tablet10"}
]
LANGUAGES = ["ja", "en_US"]
PACKAGE = "com.github.tyamada.MihirakiPDFViewer_Android"

OUT_DIR = os.path.join(os.getcwd(), "store_listing", "screenshots")

def run_cmd(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True)

def verify_single_device():
    lines = run_cmd("adb devices").stdout.strip().split('\n')[1:]
    devices = [line.split('\t')[0] for line in lines if 'device' in line and not line.startswith('*')]
    if len(devices) > 1:
        print("ERROR: 複数のデバイスが接続されています。別の実機が接続された可能性があるため、処理を中断します。")
        exit(1)
    if len(devices) == 0:
        return None
    return devices[0]

def wait_for_boot():
    print("仮想デバイスの起動を待機しています...")
    run_cmd("adb wait-for-device")
    while True:
        boot = run_cmd("adb shell getprop sys.boot_completed").stdout.strip()
        if boot == "1":
            break
        time.sleep(2)
    print("仮想デバイスが起動しました。")
    time.sleep(5)
    run_cmd("adb shell input keyevent 82") # ロック解除

def start_emulator(avd_name):
    print(f"[{avd_name}] 仮想デバイスを起動します...")
    run_cmd("adb emu kill")
    time.sleep(5)

    # macOS/Linuxにおけるemulatorの標準的なパス
    emu_path = os.path.expanduser("~/Library/Android/sdk/emulator/emulator")
    if not os.path.exists(emu_path):
        emu_path = "emulator"

    subprocess.Popen([emu_path, "-avd", avd_name, "-no-snapshot-load", "-no-boot-anim"])
    wait_for_boot()
    verify_single_device()

def set_locale(lang):
    locale = "ja-JP" if lang == "ja" else "en-US"
    current = run_cmd("adb shell getprop persist.sys.locale").stdout.strip()
    if current != locale:
        print(f"言語が異なります (現在: {current} / 目標: {locale})。設定して再起動します...")
        run_cmd(f"adb shell setprop persist.sys.locale {locale}")
        run_cmd("adb reboot")
        time.sleep(10)
        wait_for_boot()
    else:
        print(f"システム言語は {locale} に正しく設定されています。")

def set_rotation(landscape=False):
    # 自動回転をオフ
    run_cmd("adb shell settings put system accelerometer_rotation 0")
    rot = "1" if landscape else "0"
    run_cmd(f"adb shell settings put system user_rotation {rot}")
    time.sleep(2)

def is_valid_screen():
    focus = run_cmd("adb shell dumpsys window | grep mCurrentFocus").stdout.strip()

    if "Keyguard" in focus or "NotificationShade" in focus:
        print("エラー: ロック画面が検出されました。")
        return False
    if "Launcher" in focus or "nexuslauncher" in focus:
        print("エラー: ホーム画面が検出されました。")
        return False
    if "Application Error" in focus or "crash" in focus.lower():
        print("エラー: アプリケーションのエラー画面が検出されました。")
        return False
    if "SignInActivity" in focus or "com.google.android.gms" in focus:
        print("エラー: サインイン画面が検出されました。")
        return False

    # 簡易的な黒画面判定（スクリーンショットのファイルサイズによる判定）
    run_cmd("adb shell screencap -p /sdcard/tmp_check.png")
    run_cmd("adb pull /sdcard/tmp_check.png tmp_check.png")
    if os.path.exists("tmp_check.png"):
        size = os.path.getsize("tmp_check.png")
        if size < 15000: # 閾値は要調整
            print(f"エラー: 画像サイズが小さすぎます ({size} bytes)。黒画面の可能性があります。")
            return False

    return True

def push_test_pdf(lang):
    file_suffix = "en" if lang == "en_US" else lang
    pdf_path = f"testdata/tameshibu_episode1_2_{file_suffix}.pdf"
    print(f"テスト用PDF {pdf_path} をデバイスに転送します...")
    run_cmd(f"adb push {pdf_path} /sdcard/Download/tameshibu_episode1_2.pdf")

def capture_screenshot(device_dir, lang, screen_name, landscape=False):
    set_rotation(landscape)
    verify_single_device()

    # 状態の妥当性確認と再試行ループ
    for attempt in range(3):
        if is_valid_screen():
            break
        print("無効な画面状態を検出したため、2秒後に再試行します...")
        time.sleep(2)
        if attempt == 2:
            print("再試行に失敗しました。このスクリーンショットはスキップされます。")
            return

    out_path = os.path.join(OUT_DIR, device_dir, f"{lang}_{screen_name}.png")
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    run_cmd("adb shell screencap -p /sdcard/sc.png")
    run_cmd(f"adb pull /sdcard/sc.png {out_path}")
    print(f"スクリーンショットを保存しました: {out_path}")

def build_and_install_app():
    print("アプリのビルドとインストールを行っています...")
    run_cmd("./gradlew assembleDebug assembleDebugAndroidTest")
    run_cmd("adb install -r app/build/outputs/apk/debug/app-debug.apk")

def main():
    build_and_install_app()

    for dev in DEVICES:
        start_emulator(dev["avd"])

        for lang in LANGUAGES:
            verify_single_device()
            set_locale(lang)
            push_test_pdf(lang)

            # --- 画面ごとの撮影 ---
            # 1. ファイル選択 (Library) - 縦画面
            print(f"[{dev['avd']} - {lang}] 1. ライブラリ画面の撮影")
            run_cmd(f"adb shell am start -n {PACKAGE}/.MainActivity")
            time.sleep(5) # 起動待ち
            capture_screenshot(dev["dir"], lang, "01_Library", landscape=False)

            # 2. 見開き表示 (Spread) - 横画面
            print(f"[{dev['avd']} - {lang}] 2. 見開き表示画面の撮影")
            # 転送したPDFをインテントで開く
            run_cmd(f"adb shell am start -a android.intent.action.VIEW -d \"file:///sdcard/Download/tameshibu_episode1_2.pdf\" -t \"application/pdf\" -n {PACKAGE}/.MainActivity")
            time.sleep(5)
            capture_screenshot(dev["dir"], lang, "02_Mihiraki", landscape=True)

            # 3. 単一ページ表示 (Single) - 縦画面
            print(f"[{dev['avd']} - {lang}] 3. 単一ページ表示画面の撮影")
            capture_screenshot(dev["dir"], lang, "03_Single", landscape=False)

            # 4. 設定画面 (Settings)
            # 実際のアプリの構造に合わせてインテントやUI Automator等で設定画面を開く必要があります
            # 今回は例としてアプリのメイン画面からのメニュー展開を想定
            print(f"[{dev['avd']} - {lang}] 4. 設定画面の撮影")
            run_cmd(f"adb shell input keyevent 82") # メニューボタン
            time.sleep(2)
            capture_screenshot(dev["dir"], lang, "04_Settings", landscape=False)

    print("=== 全ての撮影が完了しました ===")

if __name__ == '__main__':
    main()
