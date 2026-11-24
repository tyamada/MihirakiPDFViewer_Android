# Verification List

This is a list of verification results compiled from developer and user information.

| Category   | Manufacturer | Device Name           | OS Version | App Version |  Result   | Notes |
|:-----------|:-------------|:----------------------|:----------:|:-----------:|:---------:|:------|
| Smartphone | FCNT         | arrows We2 M07        |     16     |  2.1.3(42)  | No issues | -     |
| Smartphone | Google       | Pixel 8a              |     17     |  2.1.3(42)  | No issues | -     |
| Smartphone | Google | Pixel 8a | 17 | 2.1.4(43) | Issue found | [Manual results](docs/MANUAL_TEST_AND_SUPPORT_FIX.ja.md): item 7, support screen clipped at enlarged font; fixed in internal Code 44; physical-device retest pending. |
| Smartphone | Google | Pixel 8a | 17 | 2.1.4(44) | Retest passed | User report, 2026-11-02: support scrolling, TalkBack and purchased status passed; items 1–6 passed on 43. [Details](docs/MANUAL_TEST_AND_SUPPORT_FIX.ja.md) |
| Smartphone | motorola     | moto g24              |     14     |  2.1.3(42)  | No issues | -     |
| Smartphone | OPPO         | Reno11 A CPH2603      |     15     |  2.1.3(42)  | No issues | -     |
| Smartphone | Samsung      | Galaxy A54 5G SC-53D  |     16     |  2.1.3(42)  | No issues | -     |
| Smartphone | Sharp        | AQUOS sense4 SH-41A   |     12     |  2.1.3(42)  | No issues | -     |
| Smartphone | Sony         | Xperia 10VI XQ-ES44   |     16     |  2.1.3(42)  | No issues | -     |
| Smartphone | Sony         | Xperia 10VIII XQ-GH44 |     17     |  2.1.3(42)  | No issues | -     |
| Tablet     | Samsung      | Galaxy Tab A SM-T510  |     9      |  2.1.3(42)  | No issues | 1     |
| Tablet     | aiwa         | tab AG10(JA3-TBA1003) |     13     |  2.1.3(42)  | No issues | -     |
| Desktop    | ASUS         | Chromebook CM30       |     13     |  2.1.3(42)  | No issues | 2     |
| Smartphone | motorola | moto g24 | 14 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, PDF open/render/page swipe, toolbar, portrait/landscape, Settings, Help, sample list, Version and existing purchased badges. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Follow-up: after a data-preserving process restart, BRONZE/SILVER/GOLD returned as Purchased; English TalkBack speech was user-confirmed and Koyomi detail focus returned to its source after rotation/Close (screen-confirmed). Further follow-up: confirmed actual BRONZE detail opened with English TalkBack; after rotation/Close, focus returned to BRONZE (speech and screenshot). In a separate cloned Play emulator, all three paid tiers restored after clearing only Viewer app data with automatic backup restore disabled; Play account/cache retained. Language, TalkBack and rotation settings match the initial state. |
| Smartphone | Google | Pixel 8a | 17 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, existing PDF rendering/page swipe, toolbar, Settings, Help expansion and Version/build display. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md#pixel-8aenglish基本操作の追加確認2026-11-25). Restored system language order to Japanese then English, and region to Japan; TalkBack remained off. Purchases, restoration, rotation and downloads were not tested on this device in this session. |

## Notes
1. The page cache function is automatically disabled on devices with low RAM capacity.
2. Window mode and tablet (full-screen) mode ready.
