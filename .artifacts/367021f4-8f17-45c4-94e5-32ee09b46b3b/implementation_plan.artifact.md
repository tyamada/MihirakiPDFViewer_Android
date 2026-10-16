# Implementation Plan - Portrait Single-Page Switch & Orientation Detection

Add a mode to detect device orientation (Portrait vs Landscape) and automatically switch to single-page display when in portrait orientation, with a settings toggle in the settings screen (defaulting to double-page spread / 見開き表示).

## User Review Required

- **Setting Default**: The setting `portraitSpread` (Portrait表示) defaults to `true` (見開き表示 / Spread), meaning by default in portrait mode it does not force single page unless switched to `false` (単一ページ表示).
- **Behavior**: When device is in Portrait orientation and `portraitSpread` is `false`, the viewer forces single-page display (`ViewerLayout.SINGLE`) regardless of the general layout setting.

## Proposed Changes

### Data & Settings Layer

#### [ViewerSettings and SettingsRepository](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/data/Models.kt)
- Add `portraitSpread: Boolean = true` to `ViewerSettings`.

#### [SettingsRepository](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/data/SettingsRepository.kt)
- Add `portrait_spread` boolean DataStore key.
- Read and save `portraitSpread`.

### UI & Strings

#### [String Resources](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/res/values/strings.xml) & [Japanese Strings](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/res/values-ja/strings.xml)
- Add string resources:
  - `portrait_display`: "Portrait display" / "Portrait表示 (縦長時)"
  - `help_portrait_display`: Help documentation for portrait display setting.

#### [SettingsScreens.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/screens/SettingsScreens.kt)
- Add `SwitchRow` for Portrait display in `SettingsScreen`.
- Add help description in `HelpScreen`.

### ViewModel & Orientation Logic

#### [ViewerViewModel.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/viewmodel/ViewerViewModel.kt)
- Add helper method or logic `getEffectiveLayout()` that checks `context.resources.configuration.orientation`:
  - If `Configuration.ORIENTATION_PORTRAIT` and `!settings.portraitSpread`, returns `ViewerLayout.SINGLE`.
  - Otherwise returns `settings.layout`.
- Use `getEffectiveLayout()` in `render()` and `move()`.

## Verification Plan

### Automated Tests
- Run unit tests with `./gradlew test` via `gradle_build`.
- Add test case verifying `ViewerSettings` default `portraitSpread == true`.

### Manual Verification
- Deploy app to device/emulator.
- Open settings screen, verify the "Portrait表示" switch exists and defaults to enabled (見開き表示).
- Test rotating device between Portrait and Landscape and verify layout behavior (single page in portrait when switch is off, spread when switch is on).
