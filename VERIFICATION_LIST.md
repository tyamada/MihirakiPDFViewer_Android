# Verification List

This is a list of verification results compiled from developer and user information.

## Maintenance rules

- Keep exactly one row per device model, showing its latest verification result. Identify the model by manufacturer and model name/number; spacing differences do not create separate models.
- When a new verification is completed, replace that model's existing row instead of adding another. Use the verification date and confirmed installed app version; the highest version number alone does not establish the latest verification.
- Preserve earlier results in the detailed verification records and Git history. Keep unresolved issues and untested scope explicit; a newer limited check does not automatically resolve earlier issues or validate every feature.

| Category   | Manufacturer | Device Name           | OS Version | App Version |  Result   | Notes |
|:-----------|:-------------|:----------------------|:----------:|:-----------:|:---------:|:------|
| Smartphone | FCNT         | arrows We2 M07        |     16     |  2.1.3(42)  | No issues | -     |
| Smartphone | Google | Pixel 8a | 17 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, existing PDF rendering/page swipe, toolbar, Settings, Help expansion and Version/build display. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored system language order to Japanese then English, and region to Japan; TalkBack remained off. Purchases, restoration, rotation and downloads were not tested on this device in this session. |
| Smartphone | motorola | moto g24 | 14 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, PDF open/render/page swipe, toolbar, portrait/landscape, Settings, Help, sample list, Version and existing purchased badges. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Follow-up: after a data-preserving process restart, BRONZE/SILVER/GOLD returned as Purchased; English TalkBack speech was user-confirmed and Koyomi detail focus returned to its source after rotation/Close (screen-confirmed). Further follow-up: confirmed actual BRONZE detail opened with English TalkBack; after rotation/Close, focus returned to BRONZE (speech and screenshot). In a separate cloned Play emulator, all three paid tiers restored after clearing only Viewer app data with automatic backup restore disabled; Play account/cache retained. Language, TalkBack and rotation settings match the initial state. |
| Smartphone | OPPO         | Reno11 A CPH2603      |     15     |  2.1.3(42)  | No issues | -     |
| Smartphone | Samsung | Galaxy A54 5G SC-53D | 16 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (Japan), en-JP: tutorial, English sample list/download/open, PDF rendering, next-page button, toolbar, Settings, Help expansion and Version/build display. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original Japanese-only language list (ja-JP); TalkBack remained off. Purchases, restoration, TalkBack and rotation were not tested on this device in this session. |
| Smartphone | Sharp        | AQUOS sense4 SH-41A   |     12     |  2.1.3(42)  | No issues | -     |
| Smartphone | Sony | Xperia 10 VI XQ-ES44 | 16 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US): tutorial, English sample download/open, PDF rendering, swipe 1/8 to 2/8, toolbar, Settings, Help expansion and Version. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original language order Japanese then English; TalkBack stayed off and package update time was unchanged. Purchases, restoration, TalkBack and rotation were not tested in this session. |
| Smartphone | Sony         | Xperia 10VIII XQ-GH44 |     17     |  2.1.3(42)  | No issues | -     |
| Tablet | Samsung | Galaxy Tab A SM-T510 | 9 | 2.1.10(53) | Retest passed | 2026-11-25, system English (en-US): basic checks and PDF retests passed. Initial interruption was OS package replacement: installPackageLI killed Viewer at 09:52:37 JST, matching lastUpdateTime. After update, Episode 3 cold resume, new Episode 2 first open/page swipe and Episode 2 cold resume passed. No app-code fix required for this event. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original Japanese-only settings; other holds unchanged. |
| Tablet | aiwa | tab AG10 (JA3-TBA1003) | 13 | 2.1.10(53) | Basic checks passed | 2026-11-25, system English (en-US), landscape: tutorial, English sample download/open, PDF render, swipe 1/8 to 2/8, toolbar, Settings, Help expansion and Version. [Scope and evidence](docs/DEVICE_ENGLISH_BASIC_CHECK_2026-11-25.ja.md). Restored original Japanese-only language list (ja-JP); TalkBack stayed off and package update time was unchanged. Purchases, restoration, TalkBack and rotation transitions were not tested in this session. |
| Desktop    | ASUS         | Chromebook CM30       |     13     |  2.1.3(42)  | No issues | 2     |

## Notes
1. The page cache function is automatically disabled on devices with low RAM capacity.
2. Window mode and tablet (full-screen) mode ready.
